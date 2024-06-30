#version 300 es

precision mediump float;

#include "shaders/arcore_utils/depthutils.frag"

uniform sampler2D uSceneColorTexture;
uniform sampler2D uSceneDepthTexture;
uniform sampler2D uDepthMapTexture;
uniform bool uDepthMapAvailable;
uniform float uCameraNearPlane;
uniform float uCameraFarPlane;

in vec2 vTexCoords;

out vec4 fragColor;

void main() {
    vec4 sceneColor = texture(uSceneColorTexture, vTexCoords);
    if (sceneColor.a > 0.0) {
        if (uDepthMapAvailable) {
            vec4 sceneDepth = texture(uSceneDepthTexture, vTexCoords);
            float virtualSceneDepthMillimeters = GetVirtualSceneDepthMillimeters(sceneDepth.x, uCameraNearPlane, uCameraFarPlane);
            float depthMapMillimeters = DepthGetMillimeters(uDepthMapTexture, vTexCoords);
            float occlusionDistance = virtualSceneDepthMillimeters - depthMapMillimeters;
            // This will make a gradient transition of 3 cm instead of a hard cut.
            float occlusionPercentage = clamp(occlusionDistance / 100.0, 0.0, 1.0);
            if (occlusionPercentage > 0.95) {
                discard;
            }
            sceneColor.a = 1.0 - occlusionPercentage;
        }
        fragColor = sceneColor;
    } else {
        discard;
    }
}
