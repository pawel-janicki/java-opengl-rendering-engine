#version 330

in vec2 passTextureCoords;

out vec4 outColor;

uniform sampler2D inputTexture;

void main() {
	vec3 color = texture(inputTexture, passTextureCoords).rgb;
	color = pow(color, vec3(1.0 / 2.2));
	
	outColor = vec4(color, 1);
}
