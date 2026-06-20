#version 150

in vec2 texCoord;
out vec4 fragColor;

uniform float GameTime;
uniform vec2 ScreenSize;
uniform vec2 Center;
uniform vec3 CoreColor;
uniform vec3 ShellColor;
uniform float Intensity;
uniform float Radius;
uniform float Erosion;

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

vec2 hash2(vec2 p) {
    return vec2(
        fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453),
        fract(sin(dot(p, vec2(269.5, 183.3))) * 43758.5453)
    );
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));
    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

vec2 voronoi(vec2 uv) {
    vec2 cell = floor(uv);
    vec2 frac = fract(uv);

    float minDist = 10.0;
    float secondDist = 10.0;
    float cellId = 0.0;

    for (int y = -1; y <= 1; y++) {
        for (int x = -1; x <= 1; x++) {
            vec2 neighbor = vec2(float(x), float(y));
            vec2 point = hash2(cell + neighbor);
            vec2 diff = neighbor + point - frac;
            float d = length(diff);

            if (d < minDist) {
                secondDist = minDist;
                minDist = d;
                cellId = hash(cell + neighbor);
            } else if (d < secondDist) {
                secondDist = d;
            }
        }
    }

    return vec2(secondDist - minDist, cellId);
}

void main() {
    float time = GameTime * 1200.0;

    vec2 aspectRatio = vec2(ScreenSize.x / ScreenSize.y, 1.0);
    vec2 uv = (texCoord - Center) * aspectRatio;

    float dist = length(uv);
    float normDist = dist / Radius;
    vec2 nuv = uv / Radius;

    float maxExpand = 1.0 + Erosion * 0.4;
    if (normDist > maxExpand + 0.15) {
        fragColor = vec4(0.0);
        return;
    }

    // === CORE — Compressed at low erosion, blinding at max ===

    float coreCompression = mix(0.35, 0.75, Erosion);
    float corePulse = 0.5 + 0.5 * sin(time * 0.25 + dist * 15.0);
    float coreBase = 1.0 - smoothstep(0.0, coreCompression, normDist);
    coreBase = pow(coreBase, mix(3.0, 1.2, Erosion));

    float heat1 = noise(nuv * 5.0 + vec2(time * 0.08, time * 0.06));
    float heat2 = noise(nuv * 8.0 - vec2(time * 0.05, time * 0.09));
    float heat = heat1 * heat2;
    heat *= smoothstep(0.0, 0.2, normDist) * smoothstep(0.85, 0.3, normDist);

    float coreAlpha = coreBase + heat * 0.4 + corePulse * coreBase * 0.15;
    vec3 coreCol = CoreColor * (0.8 + 0.2 * corePulse);
    float whiteness = mix(0.3, 0.8, Erosion);
    coreCol = mix(coreCol, vec3(1.0, 0.98, 0.92), coreBase * whiteness);
    coreCol = mix(coreCol, vec3(1.0, 0.5, 0.1), heat * 0.3);

    // === SHELL — Voronoi fragments that explode outward ===

    vec2 v1 = voronoi(nuv * 6.0 + vec2(time * 0.01, -time * 0.008));
    vec2 v2 = voronoi(nuv * 10.0 - vec2(time * 0.006, time * 0.01));

    float edge1 = v1.x;
    float id1 = v1.y;
    float edge2 = v2.x;
    float id2 = v2.y;

    float cellDisplace1 = hash(vec2(id1 * 127.3, id1 * 53.7));
    float cellDisplace2 = hash(vec2(id2 * 89.1, id2 * 41.3));

    float pushAmount1 = cellDisplace1 * Erosion * 0.35;
    float pushAmount2 = cellDisplace2 * Erosion * 0.25;

    float cellNorm1 = normDist - pushAmount1;
    float cellNorm2 = normDist - pushAmount2;

    // Fracture lines widen with erosion
    float gapWidth1 = mix(0.02, 0.06, Erosion);
    float gapWidth2 = mix(0.02, 0.05, Erosion);
    float gap1 = 1.0 - smoothstep(gapWidth1, gapWidth1 + 0.04, edge1);
    float gap2 = 1.0 - smoothstep(gapWidth2, gapWidth2 + 0.03, edge2);

    float frag1 = smoothstep(gapWidth1 + 0.02, gapWidth1 + 0.08, edge1);
    float frag2 = smoothstep(gapWidth2 + 0.01, gapWidth2 + 0.07, edge2);

    float fragments = max(frag1 * 0.7, frag2 * 0.5);

    float cellBright1 = 0.5 + 0.5 * sin(id1 * 47.0 + time * 0.15);
    float cellBright2 = 0.4 + 0.6 * sin(id2 * 31.0 + time * 0.12 + 1.0);

    float metallic = fragments * mix(cellBright1, cellBright2, 0.4);

    float shellRadial1 = smoothstep(0.1, 0.3, cellNorm1) * smoothstep(maxExpand, 0.55, cellNorm1);
    float shellRadial2 = smoothstep(0.1, 0.3, cellNorm2) * smoothstep(maxExpand, 0.55, cellNorm2);
    float shellRadial = max(shellRadial1, shellRadial2 * 0.6);

    float erosionCutoff = Erosion * 0.85;
    float cellSurvival1 = step(erosionCutoff, id1);
    float cellSurvival2 = step(erosionCutoff * 0.9, id2);
    float survival = max(cellSurvival1, cellSurvival2 * 0.6);

    float shellAlpha = metallic * shellRadial * survival;

    float cellBoundary1 = 1.0 - smoothstep(maxExpand - 0.12, maxExpand, cellNorm1);
    float cellBoundary2 = 1.0 - smoothstep(maxExpand - 0.12, maxExpand, cellNorm2);
    float cellBoundary = max(cellBoundary1 * cellSurvival1, cellBoundary2 * cellSurvival2 * 0.6);
    float boundary = max(cellBoundary, 1.0 - smoothstep(0.85, 1.0, normDist));

    vec3 shellCol = ShellColor * (0.4 + 0.6 * metallic);
    float edgeLight = smoothstep(0.12, 0.06, edge1) * smoothstep(0.02, 0.06, edge1);
    shellCol = mix(shellCol, CoreColor * 0.7, edgeLight * 0.5);

    // === FRACTURE GLOW — White-hot at high erosion ===

    float gapMask = max(gap1, gap2 * 0.7);
    gapMask *= shellRadial * boundary;
    float gapIntensity = 0.3 + Erosion * 0.7;
    vec3 gapCol = mix(CoreColor * 1.2, vec3(1.0, 0.95, 0.85), Erosion * 0.6);
    gapCol *= 1.0 + Erosion * 0.8;

    float edgeBloom1 = smoothstep(gapWidth1 + 0.06, gapWidth1, edge1) * smoothstep(gapWidth1 * 0.5, gapWidth1, edge1);
    float edgeBloom2 = smoothstep(gapWidth2 + 0.05, gapWidth2, edge2) * smoothstep(gapWidth2 * 0.5, gapWidth2, edge2);
    float edgeBloom = max(edgeBloom1, edgeBloom2 * 0.6) * shellRadial * boundary;
    float bloomStrength = Erosion * 0.4;

    // === COMPOSITE ===

    float coreVisibility = (1.0 - shellAlpha * 0.85) * boundary;
    float erosionGlow = 1.0 + Erosion * 1.0;
    coreAlpha *= coreVisibility * erosionGlow;

    float outerGlow = smoothstep(1.2, 0.75, normDist) * smoothstep(0.0, 0.3, normDist);
    outerGlow *= (1.0 - shellAlpha * 0.7);
    float glowStrength = 0.04 + Erosion * 0.12;

    vec3 color = vec3(0.0);
    float alpha = 0.0;

    color += coreCol * coreAlpha;
    alpha += coreAlpha;

    color += shellCol * shellAlpha * boundary;
    alpha += shellAlpha * boundary;

    color += gapCol * gapMask * gapIntensity;
    alpha += gapMask * gapIntensity * 0.6;

    color += vec3(1.0, 0.95, 0.9) * edgeBloom * bloomStrength;
    alpha += edgeBloom * bloomStrength * 0.5;

    color += CoreColor * outerGlow * glowStrength;
    alpha += outerGlow * glowStrength;

    alpha *= Intensity;
    alpha = clamp(alpha, 0.0, 1.0);

    if (alpha > 0.001) {
        color /= alpha;
    }

    fragColor = vec4(color, alpha);
}
