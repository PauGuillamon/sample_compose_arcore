#version 300 es

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aTexCoords3D; // This is data is received from Vertex's normal
layout (location = 2) in vec2 aTexCoords2D;

uniform bool uUseTexCoords3D;

out vec3 vTexCoords;

void main() {
    gl_Position = vec4(aPos, 1.0);
    if (uUseTexCoords3D) {
        vTexCoords = aTexCoords3D;
    } else {
        vTexCoords = vec3(aTexCoords2D, 1.0);
    }
}
