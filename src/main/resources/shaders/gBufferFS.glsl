#version 430

#extension GL_ARB_bindless_texture : require

in vec2 passTextureCoords;
in vec3 passNormal;
in mat3 TBN;
in flat int materialId;

layout (location = 0) out vec2 gNormal;
layout (location = 1) out vec3 gAlbedo;
layout (location = 2) out vec3 gARM;
layout (location = 3) out vec3 gEmissive;

struct Material {
	vec4 albedo;
	vec4 roughnessMetallic;
	
	sampler2D albedoMap;
	sampler2D metallicMap;
	sampler2D roughnessMap;
	sampler2D aoMap;
	sampler2D emissiveMap;
	sampler2D normalMap;
};

layout(std430, binding = 2) readonly buffer materialBuffer {
	Material[] materials;
};

vec2 encodeNormal(vec3 normal) {
	vec2 p = normal.xy * (1.0 / (abs(normal.x) + abs(normal.y) + abs(normal.z)));
	vec2 sign = vec2((p.x >= 0.0) ? 1.0 : -1.0, (p.y >= 0.0) ? 1.0 : -1.0);
	
	return (normal.z <= 0.0) ? ((1.0 - abs(p.yx)) * sign) : p;
}

void main() {
	Material material = materials[materialId];
	
	if (uvec2(material.normalMap) != uvec2(0, 0)) {
		vec3 tangentNormal = texture(material.normalMap, passTextureCoords).rgb * 2.0 - 1.0;
		gNormal = encodeNormal(normalize(TBN * tangentNormal));
	} else {
		gNormal = encodeNormal(normalize(passNormal));
	}
	
	gAlbedo = uvec2(material.albedoMap) != uvec2(0, 0) ? texture(material.albedoMap, passTextureCoords).rgb : material.albedo.rgb;
	
	float ao = uvec2(material.aoMap) != uvec2(0, 0) ? texture(material.aoMap, passTextureCoords).r : 1.0;
	float roughness = uvec2(material.roughnessMap) != uvec2(0, 0) ? texture(material.roughnessMap, passTextureCoords).g : material.roughnessMetallic.r;
	float metallic = uvec2(material.metallicMap) != uvec2(0, 0) ? texture(material.metallicMap, passTextureCoords).b : material.roughnessMetallic.g;
	
	gARM = vec3(ao, roughness, metallic);
	
	gEmissive = uvec2(material.emissiveMap) != uvec2(0, 0) ? texture(material.emissiveMap, passTextureCoords).rgb : vec3(0.0);
}
