#version 330

layout(location = 0) in vec2 position;
layout(location = 1) in vec2 textureCoords;

out vec2 passTextureCoords;

void main() {
	passTextureCoords = textureCoords;
	gl_Position = vec4(position, 0, 1);
}
