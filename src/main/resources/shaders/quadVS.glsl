#version 330

in vec2 position;
in vec2 textureCoords;

out vec2 passTextureCoords;

void main() {
	passTextureCoords = textureCoords;
	gl_Position = vec4(position, 0, 1);
}
