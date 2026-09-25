#version 150

in vec3 position;
in vec2 texCoord;
in vec2 alphaBrightness;
in mat4 modelMatrix;
in vec4 rgba;
in vec4 meta;

out vec2 outTexCoord;
out vec4 outRGBA;
out float fogFragCoord;

uniform mat4 modelViewMatrixCamera;
uniform int time;
uniform float partialTick;
uniform float windDir;
uniform float windSpeed;

vec3 computeCorner(vec3 sway, vec3 angle, vec3 center) {
    return center + normalize(cross(sway, angle)) * 0.5;
}

mat4 rotationMatrix(vec3 axis, float angle) {
    axis = normalize(axis);
    float s = sin(angle);
    float c = cos(angle);
    float oc = 1.0 - c;
    return mat4(
        oc * axis.x * axis.x + c,
        oc * axis.x * axis.y - axis.z * s,
        oc * axis.z * axis.x + axis.y * s,
        0.0,
        oc * axis.x * axis.y + axis.z * s,
        oc * axis.y * axis.y + c,
        oc * axis.y * axis.z - axis.x * s,
        0.0,
        oc * axis.z * axis.x - axis.y * s,
        oc * axis.y * axis.z + axis.x * s,
        oc * axis.z * axis.z + c,
        0.0,
        0.0,
        0.0,
        0.0,
        1.0
    );
}

void main() {
    float radian = 0.0174533;
    int swayLag = 20;

    float index = meta.x;
    float animationID = meta.y;
    float heightIndex = meta.z;
    float antiStiffness = meta.w;
    float rotation = rgba.w;

    float baseTimeChangeRate = 60.0 * windSpeed;
    float timeSmooth = float(time - baseTimeChangeRate) + (baseTimeChangeRate * partialTick);
    timeSmooth += index * 200.0;

    vec3 pos = vec3(0.0);

    mat4 finalMat = modelViewMatrixCamera * modelMatrix;
    vec3 posTestAdj = position;
    posTestAdj.y = posTestAdj.y + heightIndex + 0.5;
    vec4 posTest = finalMat * vec4(posTestAdj, 1.0);

    if (windSpeed > 0.00001 && posTest.w < 999.0) {

        if (animationID == 0.0) {

            vec3 usePos = position;
            usePos.y = usePos.y + 0.5;
            float heightFromBase = heightIndex + usePos.y;

            swayLag = int(heightFromBase * -baseTimeChangeRate * 0.2);

            float windSpeedAdj = windSpeed * 0.05 * heightFromBase * (antiStiffness * 2.0);

            if (antiStiffness == 1.0) {
                windSpeedAdj = windSpeed * 0.5;
            }

            float adjDir = windDir;

            vec3 windAdj = vec3(-sin(adjDir * radian) * windSpeedAdj, 0.0, cos(adjDir * radian) * windSpeedAdj);

            if (rotation == 45.0) {
                windAdj = vec3(-cos(adjDir * radian) * windSpeedAdj, 0.0, -sin(adjDir * radian) * windSpeedAdj);
            }

            int timeModTop = int(mod((timeSmooth * 0.2) + float(swayLag), 360.0));

            float variance = (0.02 + 0.05 * windSpeed) * antiStiffness;

            vec3 chaosAdj = vec3(-sin(float(timeModTop) * radian) * variance, 0.0, cos(float(timeModTop) * radian) * variance);

            if (rotation == 45.0) {
                chaosAdj = vec3(-cos(float(timeModTop) * radian) * variance, 0.0, -sin(float(timeModTop) * radian) * variance);
            }

            windAdj = windAdj * heightFromBase;
            chaosAdj = chaosAdj * heightFromBase;

            pos = usePos + windAdj + chaosAdj;
            pos.y = pos.y + heightIndex;

        } else if (false && animationID == 1.0) {

            float variance = 0.6;

            vec3 angle = vec3(-1.0, 0.0, 1.0);
            if (rotation == 1.0) {
                angle = vec3(1.0, 0.0, 1.0);
            }

            vec3 baseHeight = vec3(0.0, heightIndex - 1.0, 0.0);
            vec3 baseHeight2 = vec3(0.0, heightIndex, 0.0);

            int timeModBottom = int(mod(((timeSmooth + ((heightIndex - 1.0 + 1.0) * float(swayLag))) * 2.0) + rotation, 360.0));
            vec3 swayBottom = vec3(sin(float(timeModBottom) * radian) * variance, 1.0, cos(float(timeModBottom) * radian) * variance);
            vec3 prevSway = swayBottom;
            vec3 bottom = baseHeight + swayBottom;

            int timeModTop = int(mod(((timeSmooth + ((heightIndex + 1.0) * float(swayLag))) * 2.0) + rotation, 360.0));
            vec3 sway = vec3(sin(float(timeModTop) * radian) * variance, 1.0, cos(float(timeModTop) * radian) * variance);
            vec3 top = baseHeight2 + sway;

            if (heightIndex == 0.0) {
                bottom = vec3(0.0);
                prevSway = vec3(0.0, 1.0, 0.0);
            }

            if (gl_VertexID == 0) { pos = computeCorner(sway, angle, top); }
            else if (gl_VertexID == 1) { pos = computeCorner(prevSway, angle, bottom); }
            else if (gl_VertexID == 2) { pos = computeCorner(prevSway, angle * -1.0, bottom); }
            else if (gl_VertexID == 3) { pos = computeCorner(sway, angle * -1.0, top); }
        }

        gl_Position = finalMat * vec4(pos, 1.0);

    } else {
        gl_Position = posTest;
    }

    fogFragCoord = abs(gl_Position.z);

    outTexCoord = texCoord;

    float brightness = alphaBrightness.y;

    outRGBA = vec4(rgba.x * brightness, rgba.y * brightness, rgba.z * brightness, alphaBrightness.x);
}