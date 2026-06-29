#version 330

in vec3 textureCoords;

out vec4 outColor;

uniform samplerCube cubemap;

void main() {
	outColor = texture(cubemap, textureCoords);
}
