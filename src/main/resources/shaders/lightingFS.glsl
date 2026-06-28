#version 330

in vec2 passTextureCoords;

out vec4 outColor;

uniform sampler2D gDepth;
uniform sampler2D gNormal;
uniform sampler2D gAlbedo;
uniform sampler2D gARM;
uniform sampler2D gEmissive;

uniform vec3 lightDirection;
uniform vec3 lightColor;

uniform vec3 cameraPosition;
uniform mat4 inverseProjectionMatrix;
uniform mat4 inverseViewMatrix;

const float PI = 3.14159265359;

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

vec3 reconstructWorldPosition(vec2 uv, float depth) {
	vec4 clipSpacePosition = vec4(uv.x * 2.0 - 1.0, uv.y * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
	
	vec4 viewSpacePosition = inverseProjectionMatrix * clipSpacePosition;
	viewSpacePosition /= viewSpacePosition.w;
	
	vec4 worldSpacePosition = inverseViewMatrix * viewSpacePosition;
	
	return worldSpacePosition.xyz;
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
	vec3 N = normalize(texture(gNormal, passTextureCoords).rgb);
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
	vec3 ambient = vec3(0.1) * albedo * ao;
	
	// Emissive Lighting
	vec3 emissive = texture(gEmissive, passTextureCoords).rgb;
	
	outColor = vec4(ambient + Lo + emissive, 1.0);
}
