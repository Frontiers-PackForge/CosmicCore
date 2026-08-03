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
    float edge = sin(clamp(currentUv.y, 0.0, 1.0) * 3.14159265);
    edge = pow(max(edge, 0.0), 1.55);
    float speed = mix(0.62, 1.08, currentMode);
    float flow = currentUv.x * 0.085 - CurrentTime * speed + currentPhase * 11.0;
    float macro = valueNoise(worldPosition.xz * 0.012 + vec2(CurrentTime * 0.025, -CurrentTime * 0.011));
    float detail = valueNoise(
            vec2(currentUv.x * 0.055, worldPosition.y * 0.047) +
            vec2(-CurrentTime * 0.18, CurrentTime * 0.07 + currentPhase * 17.0));
    float broad = 0.5 + 0.5 * sin(flow + (macro - 0.5) * 4.8);
    float crossing = 0.5 + 0.5 * sin(flow * 1.73 - detail * 5.2 + currentPhase * 5.0);
    float streak = smoothstep(0.48, 0.94, broad * 0.68 + crossing * 0.32);
    float filament = smoothstep(0.78, 0.985, crossing) * smoothstep(0.36, 0.82, detail);
    float irregular = mix(0.54, 1.0, macro) * mix(0.76, 1.0, detail);
    float distanceFade = 1.0 - smoothstep(150.0, 230.0, distance(worldPosition, CameraPos));
    float heightFade = mix(1.0, smoothstep(0.0, 0.13, currentUv.y) *
            (1.0 - smoothstep(0.87, 1.0, currentUv.y)), currentMode);

    vec3 shadow = vec3(0.018, 0.095, 0.250);
    vec3 body = mix(vec3(0.030, 0.260, 0.520), vec3(0.040, 0.500, 0.720), macro);
    vec3 crest = mix(vec3(0.105, 0.610, 0.900), vec3(0.260, 0.920, 0.940), detail);
    vec3 color = mix(shadow, body, 0.32 + streak * 0.46);
    color = mix(color, crest, clamp(filament * 0.72 + streak * 0.18, 0.0, 0.78));
    color = mix(color, vec3(0.150, 0.740, 0.980), currentMode * 0.12);

    float alpha = edge * heightFade * distanceFade * currentOpacity * currentStrength * irregular;
    alpha *= 0.18 + streak * 0.54 + filament * 0.28;
    if (alpha < 0.004) discard;
    fragColor = vec4(color, clamp(alpha, 0.0, 0.46));
}
