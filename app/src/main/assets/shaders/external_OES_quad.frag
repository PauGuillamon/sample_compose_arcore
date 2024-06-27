#version 300 es

#extension GL_OES_EGL_image_external_essl3 : require

precision mediump float;

uniform samplerExternalOES uColorTexture;

in vec2 vTexCoords;

out vec4 fragColor;

void main() {
    vec4 color = texture(uColorTexture, vTexCoords);
    fragColor = vec4(color.rgb, 1.0);
}
