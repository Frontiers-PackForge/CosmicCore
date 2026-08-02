#version 150

in vec2 worldXZ;
in float layerDepth;
in float layerOpacity;
in float waveBody;
in float waveRidge;
in float organicMacro;
in float organicDetail;
in float horizonProgress;
in float worldY;

out vec4 fragColor;

uniform vec2 CameraXZ;
uniform float CameraY;
uniform float EdgeRadius;
uniform float HorizonPass;

void main() {
    float surfaceWeight = 1.0 - layerDepth;
    float body = smoothstep(0.18, 0.78, waveBody);
    float shoulder = smoothstep(0.50, 0.86, waveRidge);
    float bioluminescence = smoothstep(0.68, 0.94, waveRidge);
    float hotCrest = smoothstep(0.86, 0.98, waveRidge);
    float macroCentered = organicMacro * 2.0 - 1.0;
    float detailCentered = organicDetail * 2.0 - 1.0;
    float pigment = clamp(
            0.5 + 0.46 * macroCentered - 0.34 * detailCentered + 0.28 * macroCentered * detailCentered,
            0.0, 1.0);
    float shoulderPigment = clamp(0.5 + 0.55 * detailCentered - 0.20 * macroCentered, 0.0, 1.0);
    float colonyField = clamp(
            0.5 + 0.55 * detailCentered + 0.22 * macroCentered - 0.18 * macroCentered * detailCentered,
            0.0, 1.0);
    float colony = smoothstep(0.20, 0.82, colonyField);

    vec3 deep = mix(vec3(0.008, 0.020, 0.078), vec3(0.018, 0.082, 0.205), surfaceWeight);
    vec3 bodyColor = mix(vec3(0.018, 0.125, 0.350), vec3(0.025, 0.245, 0.510), pigment);
    vec3 shoulderColor = mix(vec3(0.025, 0.275, 0.650), vec3(0.030, 0.510, 0.850), shoulderPigment);
    vec3 crestColor = mix(vec3(0.070, 0.570, 0.900), vec3(0.180, 0.850, 0.980), colony);
    vec3 color = mix(deep, bodyColor, 0.20 + body * 0.34);
    color = mix(color, shoulderColor, shoulder * (0.22 + surfaceWeight * 0.30));
    float luminousWeight = bioluminescence * (0.40 + surfaceWeight * 0.30) * mix(0.62, 1.08, colony);
    color = mix(color, crestColor, clamp(luminousWeight, 0.0, 0.82));
    float bacterialFleck = smoothstep(0.72, 0.94, organicDetail) * hotCrest;
    color += vec3(0.130, 0.310, 0.350) * bacterialFleck * bacterialFleck * 0.11;
    color *= mix(0.72, 1.0, surfaceWeight);

    float density = (0.81 + shoulder * 0.08 + bioluminescence * 0.09) * mix(0.97, 1.03, organicMacro);
    if (HorizonPass > 0.5) {
        float innerOverlap = smoothstep(0.0, 0.18, horizonProgress);
        float distantHaze = smoothstep(0.24, 0.92, horizonProgress);
        float subsurfaceFade = 1.0 - step(0.01, layerDepth) * smoothstep(0.68, 0.96, horizonProgress);
        color = mix(color, vec3(0.018, 0.145, 0.360), distantHaze * 0.42);
        density = mix(density, 0.98, distantHaze * 0.58) * innerOverlap * subsurfaceFade;
    } else {
        float radial = length(worldXZ - CameraXZ) / EdgeRadius;
        density *= 1.0 - smoothstep(0.91, 1.0, radial);
        float eyeSeparation = smoothstep(0.75, 8.0, abs(worldY - CameraY));
        density *= mix(0.42, 1.0, eyeSeparation);
    }
    fragColor = vec4(color, clamp(density * layerOpacity, 0.0, 0.60));
}
