#version 300 es

layout (location = 0) in vec3 aPos;
// layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoords;

out vec2 vTexCoords;

void main() {
    gl_Position = vec4(aPos, 1.0);
    vTexCoords = aTexCoords;
}
