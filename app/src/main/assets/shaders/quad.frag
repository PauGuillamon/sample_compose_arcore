#version 300 es

precision mediump float;

uniform sampler2D uColorTexture;

in vec2 vTexCoords;

out vec4 fragColor;

void main() {
    vec4 color = texture(uColorTexture, vTexCoords);
    fragColor = vec4(color.rgb, 1.0);
}
