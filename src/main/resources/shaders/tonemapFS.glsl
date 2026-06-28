#version 330

in vec2 passTextureCoords;

out vec4 outColor;

uniform sampler2D inputTexture;
uniform float exposure;

vec3 aces(vec3 x) {
	const float a = 2.51;
	const float b = 0.03;
	const float c = 2.43;
	const float d = 0.59;
	const float e = 0.14;
	
	return clamp((x * (a * x + b)) / (x * (c * x + d) + e), 0.0, 1.0);
}

void main() {
	vec3 color = texture(inputTexture, passTextureCoords).rgb;
	color = aces(color * exposure);
	
	outColor = vec4(color, 1.0);
}
