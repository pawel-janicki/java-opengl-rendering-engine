#version 330

in vec2 passTextureCoords;

out vec4 outColor;

uniform sampler2D outputTexture;

void main() {
	outColor = texture(outputTexture, passTextureCoords);
}
