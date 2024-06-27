
// From https://developers.google.com/ar/develop/java/depth/developer-guide
float DepthGetMillimeters(sampler2D depth_texture, vec2 depth_uv) {
    // Depth is packed into the red and green components of its texture.
    // The texture is a normalized format, storing millimeters.
    vec3 packedDepthAndVisibility = texture(depth_texture, depth_uv).xyz;
    return dot(packedDepthAndVisibility.xy, vec2(255.0, 256.0 * 255.0));
}


// Based on:
// https://github.com/google-ar/arcore-android-sdk/blob/v1.43.0/samples/hello_ar_kotlin/app/src/main/assets/shaders/occlusion.frag#L55-L67
float GetVirtualSceneDepthMillimeters(
    float depthClipSpace,
    float zNear,
    float zFar)
{
  // Determine the depth of the virtual scene fragment in millimeters.
  const float kMetersToMillimeters = 1000.0;
  // This value was empirically chosen to correct errors with objects appearing
  // to phase through the floor. In millimeters.
  const float kBias = -80.0;
  float ndc = 2.0 * depthClipSpace - 1.0;
  return 2.0 * zNear * zFar / (zFar + zNear - ndc * (zFar - zNear)) *
             kMetersToMillimeters +
         kBias;
}

