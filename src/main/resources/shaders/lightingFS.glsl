#version 330

in vec2 passTextureCoords;

out vec4 outColor;

uniform sampler2D gDepth;
uniform sampler2D gNormal;
uniform sampler2D gAlbedo;
uniform sampler2D gARM;
uniform sampler2D gEmissive;

uniform samplerCube irradianceMap;
uniform samplerCube prefilterMap;
uniform sampler2D brdfLUT;

uniform sampler2DShadow shadowMap;
uniform sampler2D blueNoise;

uniform vec3 lightDirection;
uniform vec3 lightColor;
uniform mat4 lightSpaceMatrix;

uniform vec3 cameraPosition;
uniform mat4 inverseProjectionMatrix;
uniform mat4 inverseViewMatrix;

const float PI = 3.14159265359;

const float MAX_REFLECTION_LOD = 4.0;

const vec2[] POISSON_DISK = vec2[] (
	vec2(-0.94201624, -0.39906216),
	vec2(0.94558609, -0.76890725),
	vec2(-0.094184101, -0.92938870),
	vec2(0.34495938, 0.29387760),
	vec2(-0.91588581, 0.45771432),
	vec2(-0.81544232, -0.87912464),
	vec2(-0.38277543, 0.27676845),
	vec2(0.97484398, 0.75648379),
	vec2(0.44323325, -0.97511554),
	vec2(0.53742981, -0.47373420),
	vec2(-0.26496911, -0.41893023),
	vec2(0.79197514, 0.19090188),
	vec2(-0.24188840, 0.99706507),
	vec2(-0.81409955, 0.91437590),
	vec2(0.19984126, 0.78641367),
	vec2(0.14383161, -0.14100790)
);

float distributionGGX(vec3 N, vec3 H, float roughness) {
	float a = roughness * roughness;
	float a2 = a * a;
	float NdotH = max(dot(N, H), 0.0);
	float NdotH2 = NdotH * NdotH;
	
	float nom = a2;
	float denom = (NdotH2 * (a2 - 1.0) + 1.0);
	denom = PI * denom * denom;
	
	return nom / denom;
}

float geometrySchlickGGX(float NdotV, float roughness) {
	float r = (roughness + 1.0);
	float k = (r * r) / 8.0;
	
	float nom = NdotV;
	float denom = NdotV * (1.0 - k) + k;
	
	return nom / denom;
}

float geometrySmith(vec3 N, vec3 V, vec3 L, float roughness) {
	float NdotV = max(dot(N, V), 0.0);
	float NdotL = max(dot(N, L), 0.0);
	float ggx2 = geometrySchlickGGX(NdotV, roughness);
	float ggx1 = geometrySchlickGGX(NdotL, roughness);
	
	return ggx1 * ggx2;
}

vec3 fresnelSchlick(float cosTheta, vec3 F0) {
	return F0 + (1.0 - F0) * pow(clamp(1.0 - cosTheta, 0.0, 1.0), 5.0);
}

vec3 fresnelSchlickRoughness(float cosTheta, vec3 F0, float roughness) {
	return F0 + (max(vec3(1.0 - roughness), F0) - F0) * pow(clamp(1.0 - cosTheta, 0.0, 1.0), 5.0);
}

vec3 reconstructWorldPosition(vec2 uv, float depth) {
	vec4 clipSpacePosition = vec4(uv.x * 2.0 - 1.0, uv.y * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
	
	vec4 viewSpacePosition = inverseProjectionMatrix * clipSpacePosition;
	viewSpacePosition /= viewSpacePosition.w;
	
	vec4 worldSpacePosition = inverseViewMatrix * viewSpacePosition;
	
	return worldSpacePosition.xyz;
}

vec3 decodeNormal(vec2 p) {
	vec3 normal = vec3(p.xy, 1.0 - abs(p.x) - abs(p.y));
	vec2 sign = vec2((normal.x >= 0.0) ? 1.0 : -1.0, (normal.y >= 0.0) ? 1.0 : -1.0);
	
	if (normal.z < 0.0)
		normal.xy = (1.0 - abs(normal.yx)) * sign;
	
	return normalize(normal);
}

float calculateShadow(vec4 lightSpacePosition, vec3 normal) {
	vec3 projCoords = lightSpacePosition.xyz / lightSpacePosition.w;
	projCoords = projCoords * 0.5 + 0.5;
	
	if (projCoords.z > 1.0)
		return 0.0;
	
	float bias = max(0.003 * (1.0 - max(dot(normal, lightDirection), 0.0)), 0.0003);
	float lightFactor = 0.0;
	
	vec2 texelSize = 1.0 / textureSize(shadowMap, 0);
	float filterRadius = 2.0;
	
	vec2 noiseUV = gl_FragCoord.xy / textureSize(blueNoise, 0);
	float noiseValue = texture(blueNoise, noiseUV).r;
	
	float angle = noiseValue * 2 * PI;
	float c = cos(angle);
	float s = sin(angle);
	mat2 rotationMatrix = mat2(c, -s, s, c);
	
	float depthToCompare = projCoords.z - bias;
	
	for (int i = 0; i < 16; i++) {
		vec2 offset = rotationMatrix * POISSON_DISK[i] * texelSize * filterRadius;
		
		lightFactor += texture(shadowMap, vec3(projCoords.xy + offset, depthToCompare));
	}
	
	lightFactor /= 16.0;
	
	return lightFactor;
}

void main() {
	float depth = texture(gDepth, passTextureCoords).r;
	if (depth >= 1.0) {
		outColor = vec4(0.0);
		return;
	}
	
	// Material Properties
	vec3 albedo = texture(gAlbedo, passTextureCoords).rgb;
	vec3 ARM = texture(gARM, passTextureCoords).rgb;
	
	float ao = ARM.r;
	float roughness = ARM.g;
	float metallic = ARM.b;
	
	// Base Reflectance
	vec3 F0 = vec3(0.04);
	F0 = mix(F0, albedo, metallic);
	
	vec3 worldPosition = reconstructWorldPosition(passTextureCoords, depth);
	vec3 N = decodeNormal(texture(gNormal, passTextureCoords).rg);
	vec3 V = normalize(cameraPosition - worldPosition);
	
	// Light Properties
	vec3 L = normalize(-lightDirection);
	vec3 H = normalize(V + L);
	vec3 radiance = lightColor;
	
	// Cook-Torrance BRDF
	float D = distributionGGX(N, H, roughness);
	vec3 F = fresnelSchlick(clamp(dot(H, V), 0.0, 1.0), F0);
	float G = geometrySmith(N, V, L, roughness);
	
	vec3 numerator = D * F * G;
	float denominator = 4.0 * max(dot(N, V), 0.0) * max(dot(N, L), 0.0) + 0.0001;
	vec3 specular = numerator / denominator;
	
	// Energy conservation
	vec3 kS = F;
	vec3 kD = vec3(1.0) - kS;
	kD *= 1.0 - metallic;
	
	float NdotL = max(dot(N, L), 0.0);
	vec3 Lo = (kD * albedo / PI + specular) * radiance * NdotL;
	
	// Ambient Lighting
	F = fresnelSchlickRoughness(max(dot(N, V), 0.0), F0, roughness);
	
	kS = F;
	kD = vec3(1.0) - kS;
	kD *= 1.0 - metallic;
	
	vec3 irradiance = texture(irradianceMap, N).rgb;
	vec3 ambientDiffuse = irradiance * albedo;
	
	vec3 R = reflect(-V, N);
	vec3 prefilteredColor = textureLod(prefilterMap, R, roughness * MAX_REFLECTION_LOD).rgb;
	vec2 brdf = texture(brdfLUT, vec2(max(dot(N, V), 0.0), roughness)).rg;
	vec3 ambientSpecular = prefilteredColor * (F * brdf.x + brdf.y);
	
	vec3 ambient = (kD * ambientDiffuse + ambientSpecular) * ao;
	
	// Emissive Lighting
	vec3 emissive = texture(gEmissive, passTextureCoords).rgb;
	
	// Shadow
	vec4 lightSpacePosition = lightSpaceMatrix * vec4(worldPosition, 1.0);
	float lightFactor = calculateShadow(lightSpacePosition, N);
	
	outColor = vec4(ambient + lightFactor * Lo + emissive, 1.0);
}
