#version 330

in vec2 passTextureCoords;
in vec3 passNormal;
in mat3 TBN;

layout (location = 0) out vec2 gNormal;
layout (location = 1) out vec3 gAlbedo;
layout (location = 2) out vec3 gARM;
layout (location = 3) out vec3 gEmissive;

struct Material {
	vec3 albedo;
	float roughness;
	float metallic;
	
	sampler2D albedoMap;
	bool hasAlbedoMap;
	sampler2D metallicMap;
	bool hasMetallicMap;
	sampler2D roughnessMap;
	bool hasRoughnessMap;
	sampler2D aoMap;
	bool hasAoMap;
	sampler2D emissiveMap;
	bool hasEmissiveMap;
	sampler2D normalMap;
	bool hasNormalMap;
};

uniform Material material;

vec2 encodeNormal(vec3 normal) {
	vec2 p = normal.xy * (1.0 / (abs(normal.x) + abs(normal.y) + abs(normal.z)));
	vec2 sign = vec2((p.x >= 0.0) ? 1.0 : -1.0, (p.y >= 0.0) ? 1.0 : -1.0);
	
	return (normal.z <= 0.0) ? ((1.0 - abs(p.yx)) * sign) : p;
}

void main() {
	if (material.hasNormalMap) {
		vec3 tangentNormal = texture(material.normalMap, passTextureCoords).rgb * 2.0 - 1.0;
		gNormal = encodeNormal(normalize(TBN * tangentNormal));
	} else {
		gNormal = encodeNormal(normalize(passNormal));
	}
	
	gAlbedo = material.hasAlbedoMap ? vec3(texture(material.albedoMap, passTextureCoords)) : material.albedo;
	
	float metallic = material.hasMetallicMap ? texture(material.metallicMap, passTextureCoords).b : material.metallic;
	float roughness = material.hasRoughnessMap ? texture(material.roughnessMap, passTextureCoords).g : material.roughness;
	float ao = material.hasAoMap ? texture(material.aoMap, passTextureCoords).r : 1.0;
	
	gARM = vec3(ao, roughness, metallic);
	
	gEmissive = material.hasEmissiveMap ? vec3(texture(material.emissiveMap, passTextureCoords)) : vec3(0);
}
