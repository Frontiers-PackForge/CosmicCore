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

float fbm(vec2 p) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 4; i++) {
        v += a * noise(p);
        p *= 2.1;
        a *= 0.5;
    }
    return v;
}

float thread(vec2 uv, float angle, float seed, float time, float radius) {
    float c = cos(angle);
    float s = sin(angle);
    vec2 ruv = vec2(uv.x * c + uv.y * s, -uv.x * s + uv.y * c);

    float startDist = radius * 0.6;
    float reachMult = 0.5 + seed * 0.8;
    float reach = radius * (0.3 + Erosion * 0.9) * reachMult;
    float along = (ruv.x - startDist) / reach;
    if (along < 0.0 || along > 1.0) return 0.0;

    float wiggle = sin(along * 25.0 + time * 1.5 + seed * 6.28) * 0.003 * (0.5 + along * 1.5);
    wiggle += sin(along * 40.0 - time * 2.0 + seed * 3.14) * 0.0015 * along;
    wiggle += sin(along * 4.0 + time * 0.3 + seed * 12.0) * 0.002;

    float width = mix(0.005, 0.0012, along) * (0.4 + Erosion * 0.6);
    float lateralDist = abs(ruv.y - wiggle);
    float threadAlpha = smoothstep(width, width * 0.2, lateralDist);

    float glow = smoothstep(width * 4.0, width * 0.5, lateralDist) * 0.3;
    threadAlpha += glow;

    float lengthFade = pow(1.0 - along, 1.5);
    float pulse = 0.6 + 0.4 * sin(along * 12.0 - time * 3.0 + seed * 10.0);

    return threadAlpha * lengthFade * pulse;
}

void main() {
    float time = GameTime * 1200.0;

    vec2 aspectRatio = vec2(ScreenSize.x / ScreenSize.y, 1.0);
    vec2 uv = (texCoord - Center) * aspectRatio;

    float dist = length(uv);
    float normDist = dist / Radius;

    if (normDist > 3.5) {
        fragColor = vec4(0.0);
        return;
    }

    float alpha = 0.0;
    vec3 color = vec3(0.0);

    int threadCount = int(mix(5.0, 18.0, Erosion));

    for (int i = 0; i < 18; i++) {
        if (i >= threadCount) break;

        float seed = hash(vec2(float(i) * 73.156, float(i) * 31.77));
        // Golden angle distribution + jitter for organic but non-uniform spacing
        float angle = float(i) * 2.39996 + seed * 0.6 + time * 0.015;

        float t = thread(uv, angle, seed, time, Radius);
        t *= Erosion * Erosion;

        float ruv_x = uv.x * cos(angle) + uv.y * sin(angle);
        float reachMult = 0.5 + seed * 0.8;
        float reach = Radius * (0.3 + Erosion * 0.9) * reachMult;
        float along = clamp((ruv_x - Radius * 0.6) / reach, 0.0, 1.0);
        vec3 threadCol = mix(CoreColor * 1.5, ShellColor * 0.6, along * 0.7);
        threadCol = mix(vec3(1.0, 0.97, 0.90), threadCol, smoothstep(0.0, 0.25, along));

        color += threadCol * t;
        alpha += t;
    }

    // Inner haze
    float haze = smoothstep(Radius * 1.2, Radius * 0.7, dist);
    haze *= smoothstep(Radius * 0.4, Radius * 0.8, dist);
    float hazeNoise = fbm(uv * 15.0 + vec2(time * 0.1, -time * 0.08));
    haze *= hazeNoise * Erosion * 0.12;
    color += CoreColor * haze;
    alpha += haze;

    alpha *= Intensity;
    alpha = clamp(alpha, 0.0, 0.7);

    if (alpha > 0.001) {
        color /= alpha;
    }

    fragColor = vec4(color, alpha);
}
