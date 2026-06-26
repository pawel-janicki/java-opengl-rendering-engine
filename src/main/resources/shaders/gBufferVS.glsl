#version 330

in vec3 position;
in vec2 textureCoords;
in vec3 normal;

out vec2 passTextureCoords;
out vec3 passNormal;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 transformationMatrix;

void main() {
	passTextureCoords = textureCoords;
	mat3 normalMatrix = transpose(inverse(mat3(transformationMatrix)));
	passNormal = normalize(normalMatrix * normal);
	
	gl_Position = projectionMatrix * viewMatrix * transformationMatrix * vec4(position, 1);
}
