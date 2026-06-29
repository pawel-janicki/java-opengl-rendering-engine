#version 330

in vec3 position;

out vec3 textureCoords;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;

void main() {
	textureCoords = position;
	
	mat4 view = mat4(mat3(viewMatrix));
	vec4 pos = projectionMatrix * view * vec4(position, 1.0);
	gl_Position = pos.xyww;
}
