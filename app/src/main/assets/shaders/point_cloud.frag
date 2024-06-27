#version 300 es

precision mediump float;

in vec3 vConfidence;

out vec4 fragColor;

void main() {
    //vec3 color = mix(vec3(0.4f, 0.1f, 0.0f), vec3(0.0f, 1.0f, 0.0f), vConfidence.x);
    fragColor = vec4(vec3(0.0f, 1.0f, 0.0f), vConfidence.x);
}
