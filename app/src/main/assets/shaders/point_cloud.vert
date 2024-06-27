#version 300 es

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoords;

uniform mat4 uProjectionMatrix;
uniform mat4 uViewMatrix;

out vec3 vConfidence;

void main() {
    gl_Position = uProjectionMatrix * uViewMatrix * vec4(aPos, 1.0);
    vConfidence = aNormal;
    gl_PointSize = 10.0;
}
