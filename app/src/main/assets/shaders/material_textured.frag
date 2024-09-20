#version 300 es

precision mediump float;

uniform sampler2D uColorTexture;
uniform bool uUseLighting;

in vec2 vTexCoords;
in vec3 vNormal;

out vec4 fragColor;

vec3 calculateLighting(vec3 fragmentNormal) {
    vec3 normal = normalize(fragmentNormal);
    // Hardcoded values - should come from a Light object
    vec3 lightDir = vec3(0.0, 1.0, 0.0);
    vec3 lightColor = vec3(1.0);

    // Ambient
    vec3 ambient = vec3(0.3);

    // Diffuse
    float diff = max(dot(normal, lightDir), 0.0);
    vec3 diffuse = diff * lightColor;

    // Specular
    vec3 reflectDir = reflect(-lightDir, normal);
    float shininess = 32.0; // Hardcoded value - should come from material
    float specularStrength = 0.5;
    float spec = pow(max(dot(lightDir, reflectDir), 0.0), shininess);
    vec3 specular = specularStrength * spec * lightColor;

    // Result
    return ambient + diffuse + specular;
}

void main() {
    vec4 color = texture(uColorTexture, vTexCoords);
    if (uUseLighting) {
        color *= vec4(calculateLighting(vNormal), 1.0);
    }
    fragColor = vec4(color.rgb, 1.0);
}
