#version 150

in vec2 outTexCoord;
in vec4 outRGBA;
in float fogFragCoord;

out vec4 fragColor;

uniform sampler2D texture_sampler;
uniform int fogmode;
uniform float fogStart;
uniform float fogEnd;
uniform float fogDensity;
uniform vec4 fogColor;

void main() {
    vec4 texColor = texture(texture_sampler, outTexCoord);

    if (texColor.a * outRGBA.a < 0.004) {
        discard;
    }

    fragColor = texColor * outRGBA;

    if (fogmode >= 0) {
        float fogFactor = 1.0;

        if (fogmode == 0 && fogEnd > fogStart) {
            fogFactor = clamp((fogEnd - fogFragCoord) / (fogEnd - fogStart), 0.0, 1.0);
        } else if (fogmode == 1) {
            fogFactor = clamp(exp(-fogDensity * fogFragCoord), 0.0, 1.0);
        } else if (fogmode == 2) {
            float fd = fogDensity * fogFragCoord;
            fogFactor = clamp(exp(-(fd * fd)), 0.0, 1.0);
        }

        fragColor.rgb = mix(fogColor.rgb, fragColor.rgb, fogFactor);
    }
}