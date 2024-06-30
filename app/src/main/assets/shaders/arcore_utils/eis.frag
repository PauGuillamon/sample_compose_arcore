
vec2 TransformTexCoords(vec3 texCoords3D) {
    // Adjusts 3D texture coordinates to 2D - Needed when using EIS, unharmful when not.
    float z = (texCoords3D.z == 0.0) ? 1.0 : texCoords3D.z;
    vec2 texCoords = (texCoords3D / z).xy;
    return texCoords;
}
