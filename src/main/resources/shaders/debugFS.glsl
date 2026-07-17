#version 330

in vec2 passTextureCoords;

out vec4 outColor;

uniform sampler2D outputTexture;
uniform sampler2D depthTexture;

uniform bool showNormals;

vec3 decodeNormal(vec2 p) {
	vec3 normal = vec3(p.xy, 1.0 - abs(p.x) - abs(p.y));
	vec2 sign = vec2((normal.x >= 0.0) ? 1.0 : -1.0, (normal.y >= 0.0) ? 1.0 : -1.0);
	
	if (normal.z < 0.0)
		normal.xy = (1.0 - abs(normal.yx)) * sign;
	
	return normalize(normal);
}

void main() {
	vec4 textureData = texture(outputTexture, passTextureCoords);
	
	if (showNormals) {
		float depth = texture(depthTexture, passTextureCoords).r;
		if (depth >= 1)
			discard;
		
		outColor = vec4(decodeNormal(textureData.rg), 1.0);
	} else {
		outColor = textureData;
	}
}
