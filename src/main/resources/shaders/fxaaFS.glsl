#version 330

in vec2 passTextureCoords;

out vec4 outColor;

uniform sampler2D inputTexture;
uniform vec2 inverseScreenSize;

const float FXAA_EDGE_THRESHOLD = 1.0 / 8.0;
const float FXAA_EDGE_THRESHOLD_MIN = 1.0 / 24.0;
const float FXAA_REDUCE_MIN = 1.0 / 128.0;
const float FXAA_REDUCE_MUL = 1.0 / 8.0;
const float FXAA_SPAN_MAX = 8.0;
const float FXAA_SUB_PIXEL = 0.75;

float rgbToLuma(vec3 color) {
	return dot(color, vec3(0.299, 0.587, 0.114));
}

void main() {
	vec2 uv = passTextureCoords;
	
	vec3 rgbNW = texture(inputTexture, uv + vec2(-1.0, -1.0) * inverseScreenSize).rgb;
	vec3 rgbNE = texture(inputTexture, uv + vec2(1.0, -1.0) * inverseScreenSize).rgb;
	vec3 rgbSW = texture(inputTexture, uv + vec2(-1.0, 1.0) * inverseScreenSize).rgb;
	vec3 rgbSE = texture(inputTexture, uv + vec2(1.0, 1.0) * inverseScreenSize).rgb;
	vec3 rgbM  = texture(inputTexture, uv).rgb;
	
	float lumaNW = rgbToLuma(rgbNW);
	float lumaNE = rgbToLuma(rgbNE);
	float lumaSW = rgbToLuma(rgbSW);
	float lumaSE = rgbToLuma(rgbSE);
	float lumaM  = rgbToLuma(rgbM);
	
	float lumaMin = min(lumaM, min(min(lumaNW, lumaNE), min(lumaSW, lumaSE)));
	float lumaMax = max(lumaM, max(max(lumaNW, lumaNE), max(lumaSW, lumaSE)));
	
	float range = lumaMax - lumaMin;
	
	if (range < max(FXAA_EDGE_THRESHOLD_MIN, lumaMax * FXAA_EDGE_THRESHOLD)) {
		outColor = vec4(rgbM, 1.0);
		return;
	}
	
	vec2 edgeDirection;
	edgeDirection.x = -((lumaNW + lumaNE) - (lumaSW + lumaSE));
	edgeDirection.y = ((lumaNW + lumaSW) - (lumaNE + lumaSE));
	
	float directionReduce = max((lumaNW + lumaNE + lumaSW + lumaSE) * (0.25 * FXAA_REDUCE_MUL), FXAA_REDUCE_MIN);
	
	float rcpDirMin = 1.0 / (min(abs(edgeDirection.x), abs(edgeDirection.y)) + directionReduce);
	
	edgeDirection = clamp(edgeDirection * rcpDirMin, vec2(-FXAA_SPAN_MAX), vec2(FXAA_SPAN_MAX)) * inverseScreenSize;
	
	vec3 rgbA = 0.5 * (texture(inputTexture, uv + edgeDirection * (1.0 / 3.0 - 0.5)).rgb + texture(inputTexture, uv + edgeDirection * (2.0 / 3.0 - 0.5)).rgb);
	vec3 rgbB = rgbA * 0.5 + 0.25 * (texture(inputTexture, uv + edgeDirection * -0.5).rgb + texture(inputTexture, uv + edgeDirection * 0.5).rgb);
	
	float lumaB = rgbToLuma(rgbB);
	
	float lumaAvg = (lumaNW + lumaNE + lumaSW + lumaSE) * 0.25;
	float subPixel = clamp(abs(lumaAvg - lumaM) / (range + 1e-5), 0.0, 1.0);
	subPixel = subPixel * subPixel;
	
	float subPixelBlend = subPixel * FXAA_SUB_PIXEL;
	
	vec3 color;
	
	if (lumaB < lumaMin || lumaB > lumaMax) {
		color = rgbA;
	} else {
		color = rgbB;
	}
	
	color = mix(color, rgbM, subPixelBlend);
	
	outColor = vec4(color, 1.0);
}
