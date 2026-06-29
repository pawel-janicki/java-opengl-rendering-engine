#version 330

in vec3 position;

out vec3 worldPosition;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;

void main() {
	worldPosition = position;
	gl_Position = projectionMatrix * viewMatrix * vec4(worldPosition, 1.0);
}
