#version 460

in vec3 position;
in vec2 textureCoords;
in vec3 normal;
in vec4 tangent;

out vec2 passTextureCoords;
out vec3 passNormal;
out mat3 TBN;
out flat int materialId;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;

layout(std430, binding = 0) readonly buffer objectDataBuffer {
	int[] materialIds;
};

layout(std430, binding = 1) readonly buffer instanceDataBuffer {
	mat4[] transformations;
};

void main() {
	mat4 transformationMatrix = transformations[gl_BaseInstance + gl_InstanceID];
	
	passTextureCoords = textureCoords;
	
	mat3 normalMatrix = transpose(inverse(mat3(transformationMatrix)));
	passNormal = normalize(normalMatrix * normal);
	
	vec3 N = passNormal;
	vec3 T = normalize(normalMatrix * tangent.xyz);
	T = normalize(T - dot(T, N) * N);
	vec3 B = cross(N, T) * tangent.w;
	TBN = mat3(T, B, N);
	
	materialId = materialIds[gl_BaseInstance];
	
	gl_Position = projectionMatrix * viewMatrix * transformationMatrix * vec4(position, 1.0);
}
