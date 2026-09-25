#version 150

uniform sampler2D texture_sampler;
uniform int fogmode;
uniform float fogStart;
uniform float fogEnd;
uniform float fogScale;
uniform float fogDensity;
uniform vec4 fogColor;

in vec2 outTexCoord;
in vec4 outRGBA;
in float fogFragCoord;

out vec4 outFragColor;

void main() {
    float fogFactor = 0.0;

    if (fogmode == 0) {
        fogFactor = (fogEnd - fogFragCoord) * fogScale;
    } else if (fogmode == 1) {
        fogFactor = exp(-fogDensity * fogFragCoord);
    } else if (fogmode == 2) {
        fogFactor = exp(-(fogDensity * fogFragCoord) * (fogDensity * fogFragCoord));
    }

    vec4 fragColor = texture(texture_sampler, outTexCoord);
    fragColor.r *= outRGBA.r;
    fragColor.g *= outRGBA.g;
    fragColor.b *= outRGBA.b;
    fragColor.a *= outRGBA.a;

    if (outRGBA.a > 0.0) {
        fogFactor = clamp(fogFactor, 0.0, 1.0);

        outFragColor = mix(fogColor, fragColor, fogFactor);
        outFragColor.a = fragColor.a;
    } else {
        outFragColor = fragColor;
    }
}