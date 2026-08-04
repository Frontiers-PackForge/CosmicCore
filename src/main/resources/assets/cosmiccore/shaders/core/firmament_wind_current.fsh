#version 150

in vec2 currentUv;
in vec3 worldPosition;
in float currentStrength;
in float currentPhase;
in float currentMode;
in float currentOpacity;

out vec4 fragColor;

uniform float CurrentTime;
uniform vec3 CameraPos;

const float TAU = 6.28318530718;
const float TIME_PERIOD = 64.0;

float hash21(vec2 value) {
    vec2 point = fract(value * vec2(123.34, 456.21));
    point += dot(point, point + 45.32);
    return fract(point.x * point.y);
}

float valueNoise(vec2 value) {
    vec2 base = floor(value);
    vec2 blend = fract(value);
    blend = blend * blend * (3.0 - 2.0 * blend);
    float southwest = hash21(base);
    float southeast = hash21(base + vec2(1.0, 0.0));
    float northwest = hash21(base + vec2(0.0, 1.0));
    float northeast = hash21(base + vec2(1.0, 1.0));
    return mix(mix(southwest, southeast, blend.x), mix(northwest, northeast, blend.x), blend.y);
}

void main() {
    float stormMode = smoothstep(0.30, 0.46, currentMode) * (1.0 - smoothstep(0.58, 0.74, currentMode));
    float updraftMode = smoothstep(0.74, 0.94, currentMode);
    float edge = sin(clamp(currentUv.y, 0.0, 1.0) * 3.14159265);
    edge = pow(max(edge, 0.0), mix(1.55, 1.85, stormMode));
    float travelSpeed = mix(10.0, 17.0, updraftMode);
    travelSpeed = mix(travelSpeed, 22.0, stormMode);
    float packetPeriod = mix(40.0, 64.0, max(stormMode, updraftMode));
    float travel = currentUv.x - CurrentTime * travelSpeed;
    float flow = travel * TAU / packetPeriod + currentPhase * TAU;
    float timeCycle = CurrentTime * TAU / TIME_PERIOD;
    float macro = valueNoise(worldPosition.xz * 0.012 + vec2(cos(timeCycle), sin(timeCycle)) * 1.25);
    float detail = 0.5 + 0.25 * sin(flow * 2.0 + worldPosition.y * 0.061 + currentPhase * 17.0) +
            0.25 * sin(flow * 3.0 - currentUv.y * 4.4 + currentPhase * 9.0);
    float shred = 0.5 + 0.25 * sin(flow * 5.0 + currentUv.y * 4.2 + currentPhase * 23.0) +
            0.25 * sin(flow * 7.0 - currentUv.y * 2.7 + currentPhase * 13.0);
    float broad = 0.5 + 0.5 * sin(flow + (macro - 0.5) * 4.8);
    float crossing = 0.5 + 0.5 * sin(flow * 1.73 - detail * 5.2 + currentPhase * 5.0);
    float packetPhase = fract(travel / packetPeriod + currentPhase * 3.7);
    float packet = smoothstep(0.0, 0.14, packetPhase) * (1.0 - smoothstep(0.30, 0.98, packetPhase));
    float streakSignal = broad * 0.42 + crossing * 0.22 + packet * 0.48 + (shred - 0.5) * 0.14;
    float streak = smoothstep(0.50, 0.89, streakSignal);
    float filamentSignal = crossing * 0.30 + packet * 0.70;
    float filament = smoothstep(0.58, 0.90, filamentSignal) *
            mix(0.68, 1.0, detail) * mix(0.76, 1.0, shred);
    float stormProminence = smoothstep(0.18, 0.60, currentOpacity);
    filament *= mix(1.0, mix(0.12, 1.0, stormProminence), stormMode);
    float irregular = mix(0.54, 1.0, macro) * mix(0.76, 1.0, detail);
    float distanceFade = 1.0 - smoothstep(mix(150.0, 190.0, stormMode),
            mix(230.0, 290.0, stormMode), distance(worldPosition, CameraPos));
    float heightFade = mix(1.0, smoothstep(0.0, 0.13, currentUv.y) *
            (1.0 - smoothstep(0.87, 1.0, currentUv.y)), updraftMode);

    vec3 shadow = vec3(0.018, 0.090, 0.230);
    vec3 body = mix(vec3(0.030, 0.280, 0.550), vec3(0.040, 0.520, 0.740), macro);
    vec3 crest = mix(vec3(0.060, 0.380, 0.680), vec3(0.130, 0.660, 0.800), detail);
    vec3 color = mix(shadow, body, 0.18 + streak * 0.55);
    color = mix(color, crest, clamp(filament * 0.38 + streak * 0.16, 0.0, 0.58));
    color = mix(color, vec3(0.055, 0.330, 0.690), stormMode * 0.18);
    color = mix(color, vec3(0.180, 0.760, 0.980), updraftMode * 0.12);

    float alpha = edge * heightFade * distanceFade * currentOpacity * currentStrength * irregular;
    float stormAlpha = mix(0.065 + streak * 0.42,
            0.10 + streak * 0.62 + filament * 0.18, stormProminence);
    alpha *= mix(0.16 + streak * 0.56 + filament * 0.28, stormAlpha, stormMode);
    if (alpha < 0.004) discard;
    fragColor = vec4(color, clamp(alpha, 0.0, mix(0.46, 0.48, stormMode)));
}
