#version 300 es

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoords;

uniform mat4 uProjectionMatrix;
uniform mat4 uViewMatrix;
uniform mat4 uModelMatrix;

out vec2 vTexCoords;

void main() {
    gl_Position = uProjectionMatrix * uViewMatrix * uModelMatrix * vec4(aPos, 1.0);
    vTexCoords = aTexCoords;
}
