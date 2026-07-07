#version 330

in vec3 position;
in vec2 textureCoords;
in vec3 normal;
in vec4 tangent;

out vec2 passTextureCoords;
out vec3 passNormal;
out mat3 TBN;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 transformationMatrix;

void main() {
	passTextureCoords = textureCoords;
	
	mat3 normalMatrix = transpose(inverse(mat3(transformationMatrix)));
	passNormal = normalize(normalMatrix * normal);
	
	vec3 N = passNormal;
	vec3 T = normalize(normalMatrix * tangent.xyz);
	T = normalize(T - dot(T, N) * N);
	vec3 B = cross(N, T) * tangent.w;
	TBN = mat3(T, B, N);
	
	gl_Position = projectionMatrix * viewMatrix * transformationMatrix * vec4(position, 1.0);
}
