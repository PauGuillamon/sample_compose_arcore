#version 300 es

#extension GL_OES_EGL_image_external_essl3 : require

precision mediump float;

#include "shaders/arcore_utils/eis.frag"


uniform samplerExternalOES uColorTexture;

in vec3 vTexCoords;

out vec4 fragColor;

void main() {
    vec2 texCoords = TransformTexCoords(vTexCoords);
    vec4 color = texture(uColorTexture, texCoords);
    fragColor = vec4(color.rgb, 1.0);
}
