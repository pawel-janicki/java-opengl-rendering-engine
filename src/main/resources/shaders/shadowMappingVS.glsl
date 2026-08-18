#version 460

layout(location = 0) in vec3 position;

uniform mat4 lightSpaceMatrix;

layout(std430, binding = 0) readonly buffer instanceDataBuffer {
	mat4[] transformations;
};

void main() {
	mat4 transformationMatrix = transformations[gl_BaseInstance + gl_InstanceID];
	gl_Position = lightSpaceMatrix * transformationMatrix * vec4(position, 1.0);
}
