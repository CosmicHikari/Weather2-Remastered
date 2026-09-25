#version 150

in vec3 position;
in vec2 texCoord;
in mat4 modelMatrix;
in float brightness;
in vec4 rgba;

out vec2 outTexCoord;
out vec4 outRGBA;
out float fogFragCoord;

uniform mat4 modelViewMatrixCamera;

void main() {
    gl_Position = modelViewMatrixCamera * modelMatrix * vec4(position, 1.0);
    fogFragCoord = abs(gl_Position.z);
    outTexCoord = texCoord;
    outRGBA = rgba;
    outRGBA.r = rgba.r * brightness;
    outRGBA.g = rgba.g * brightness;
    outRGBA.b = rgba.b * brightness;
}