#version 150

in vec3 Position;
in vec2 UV0;
in vec4 Color;

out vec2 worldXZ;
out float layerDepth;
out float layerOpacity;
out float waveBody;
out float waveRidge;
out float organicMacro;
out float organicDetail;
out float horizonProgress;
out float worldY;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform float StormTime;
uniform float HorizonPass;

const float TAU = 6.28318530718;
const float CURRENT_PERIOD = 768.0;
const float CURRENT_SPEED = 7.5;

float hash21(vec2 value) {
    vec2 point = fract(value * vec2(123.34, 456.21));
    point += dot(point, point + 45.32);
    return fract(point.x * point.y);
}

float periodicValueNoise(vec2 worldPosition, float cellSize, float latticePeriod) {
    float worldPeriod = cellSize * latticePeriod;
    vec2 point = mod(worldPosition, vec2(worldPeriod)) / cellSize;
    vec2 base = floor(point);
    vec2 blend = fract(point);
    blend = blend * blend * (3.0 - 2.0 * blend);
    vec2 base0 = mod(base, vec2(latticePeriod));
    vec2 base1 = mod(base + 1.0, vec2(latticePeriod));
    float southwest = hash21(base0);
    float southeast = hash21(vec2(base1.x, base0.y));
    float northwest = hash21(vec2(base0.x, base1.y));
    float northeast = hash21(base1);
    return mix(mix(southwest, southeast, blend.x), mix(northwest, northeast, blend.x), blend.y);
}

void main() {
    float angularScale = TAU / CURRENT_PERIOD;
    vec2 position = mod(UV0, vec2(CURRENT_PERIOD)) * angularScale;
    float advection = mod(StormTime * CURRENT_SPEED, CURRENT_PERIOD) * angularScale;
    layerDepth = Color.r;
    layerOpacity = Color.a;
    horizonProgress = Color.g;
    vec2 macroLayerOffset = vec2(317.0, 911.0) * layerDepth;
    vec2 detailLayerOffset = vec2(683.0, 257.0) * layerDepth;
    organicMacro = periodicValueNoise(
            UV0 + vec2(StormTime * 0.8, -StormTime * 0.2) + macroLayerOffset, 128.0, 32.0);
    organicDetail = periodicValueNoise(
            UV0 + vec2(StormTime * CURRENT_SPEED, StormTime * 0.5) + detailLayerOffset, 64.0, 32.0);
    float macroCentered = organicMacro * 2.0 - 1.0;
    float detailCentered = organicDetail * 2.0 - 1.0;
    float phaseWarp = macroCentered * 4.2 + detailCentered * 1.35;
    float layerPhase = (layerDepth - 0.5) * 1.35;
    vec2 flowingPosition = vec2(position.x + advection, position.y);
    float primary = sin(dot(flowingPosition, vec2(7.0, 1.0)) + phaseWarp + layerPhase);
    float crossing = sin(
            dot(flowingPosition, vec2(11.0, -3.0)) - phaseWarp * 0.61 - layerPhase * 1.25);
    float swell = sin(dot(flowingPosition, vec2(3.0, 2.0)) + macroCentered * 1.9 + layerPhase * 0.40);
    float family = smoothstep(0.18, 0.82, organicDetail);
    float secondary = mix(swell, crossing, family);
    float heightSignal = primary * 0.66 + secondary * 0.24 + swell * 0.10;
    waveRidge = 0.5 + 0.5 * (primary * 0.56 + secondary * 0.34 + swell * 0.10);
    waveBody = 0.5 + 0.5 * (primary * 0.46 + secondary * 0.31 + swell * 0.23);

    float layerBase = -24.0 * layerDepth + 5.0 * layerDepth * layerDepth;
    float layerAmplitude = 6.0 - 6.75 * layerDepth + 2.5 * layerDepth * layerDepth;
    float amplitudeVariation = mix(0.78, 1.18, organicMacro) * mix(0.94, 1.06, organicDetail);
    layerAmplitude *= clamp(amplitudeVariation, 0.78, 1.10);
    float localDisplacement = layerBase + heightSignal * layerAmplitude;
    float horizonEased = horizonProgress * horizonProgress * horizonProgress;
    float horizonRadius = mix(320.0, 16384.0, horizonEased);
    float horizonBend = horizonProgress * horizonProgress * (3.0 - 2.0 * horizonProgress);
    float horizonDisplacement = heightSignal * layerAmplitude * (92.0 / horizonRadius) * (1.0 - horizonBend);
    vec3 displacedPosition = Position;
    displacedPosition.y += mix(localDisplacement, horizonDisplacement, HorizonPass);
    worldXZ = UV0;
    worldY = 24.0 + localDisplacement;
    gl_Position = ProjMat * ModelViewMat * vec4(displacedPosition, 1.0);
}
