#version 300 es

precision mediump float;

#include "shaders/depth_api/depthutils.frag"

uniform sampler2D uDepthTexture;
uniform sampler2D uDepthColorPaletteTexture;
uniform bool uDepthRawData;

in vec2 vTexCoords;

out vec4 fragColor;

// Returns linear interpolation position of value between min and max bounds.
// E.g. InverseLerp(1100, 1000, 2000) returns 0.1.
float InverseLerp(float value, float min_bound, float max_bound) {
    return clamp((value - min_bound) / (max_bound - min_bound), 0.0, 1.0);
}

// Based on official ARCore sample hello_ar_kotlin
float GetDepthMapColorPalette(float depthMillimeters) {
    const float kMidDepthMeters = 8.0;
    const float kMaxDepthMeters = 30.0;
    float depth_meters = depthMillimeters / 1000.0;
    float normalizedDepth = 0.0;
    if (depth_meters < kMidDepthMeters) {
        // Short-range depth (0m to 8m) maps to first half of the color palette.
        normalizedDepth = InverseLerp(depth_meters, 0.0, kMidDepthMeters) * 0.5;
    } else {
        // Long-range depth (8m to 30m) maps to second half of the color palette.
        normalizedDepth = InverseLerp(depth_meters, kMidDepthMeters, kMaxDepthMeters) * 0.5 + 0.5;
    }
    return normalizedDepth;
}

void main() {
    // Might be more performant to split this shader into two different
    // shaders to avoid this unneeded branching.
    if (uDepthRawData) {
        vec2 packedDepthAndVisibility = texture(uDepthTexture, vTexCoords).xy;
        fragColor.rg = packedDepthAndVisibility;
        fragColor.b = 0.0;
        fragColor.a = 0.0;
    } else {
        float depthMillimeters = DepthGetMillimeters(uDepthTexture, vTexCoords);
        float colorPaletteCoord = GetDepthMapColorPalette(depthMillimeters);
        vec3 color = texture(uDepthColorPaletteTexture, vec2(colorPaletteCoord, 0.0)).rgb;
        fragColor = vec4(color, 1.0);
    }
}
