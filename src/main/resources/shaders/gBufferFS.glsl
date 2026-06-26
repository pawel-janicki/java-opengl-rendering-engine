#version 330

in vec2 passTextureCoords;
in vec3 passNormal;

layout (location = 0) out vec4 gNormal;
layout (location = 1) out vec4 gAlbedo;
layout (location = 2) out vec4 gARM;
layout (location = 3) out vec4 gEmissive;

struct Material {
	vec3 albedo;
	float metallic;
	float roughness;
	
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
};

uniform Material material;

void main() {
	gNormal = vec4(passNormal, 1);
	gAlbedo = vec4(material.hasAlbedoMap ? vec3(texture(material.albedoMap, passTextureCoords)) : material.albedo, 1);
	
	float metallic = material.hasMetallicMap ? texture(material.metallicMap, passTextureCoords).b : material.metallic;
	float roughness = material.hasRoughnessMap ? texture(material.roughnessMap, passTextureCoords).g : material.roughness;
	float ao = material.hasAoMap ? texture(material.aoMap, passTextureCoords).r : 1;
	
	gARM = vec4(ao, roughness, metallic, 1);
	
	gEmissive = vec4(material.hasEmissiveMap ? vec3(texture(material.emissiveMap, passTextureCoords)) : vec3(0), 1);
}
