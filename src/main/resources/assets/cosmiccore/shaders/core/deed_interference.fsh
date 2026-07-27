#version 150

uniform float GlitchTime;
uniform float Seed;
uniform float Intensity;
uniform float Aspect;

in vec2 texCoord;
out vec4 fragColor;

float hash21(vec2 value) {
    vec3 p = fract(vec3(value.xyx) * 0.1031);
    p += dot(p, p.yzx + 33.33);
    return fract((p.x + p.y) * p.z);
}

float valueNoise(vec2 value) {
    vec2 base = floor(value);
    vec2 blend = fract(value);
    blend = blend * blend * (3.0 - 2.0 * blend);
    float a = hash21(base);
    float b = hash21(base + vec2(1.0, 0.0));
    float c = hash21(base + vec2(0.0, 1.0));
    float d = hash21(base + vec2(1.0, 1.0));
    return mix(mix(a, b, blend.x), mix(c, d, blend.x), blend.y);
}

void main() {
    vec2 uv = texCoord;
    vec2 centered = uv * 2.0 - 1.0;
    centered.x *= Aspect;
    float radius = length(centered);
    float mask = 1.0 - smoothstep(0.78, 1.08, radius);
    float edgeDistance = min(min(uv.x, 1.0 - uv.x), min(uv.y, 1.0 - uv.y));
    mask *= smoothstep(0.01, 0.10, edgeDistance);

    float slowTime = GlitchTime * 0.62;
    float row = floor(uv.y * 11.0);
    float rowNoise = valueNoise(vec2(row * 0.31 + Seed * 17.0, slowTime * 0.32));
    float wave = 0.5 + 0.5 * sin((uv.y * 16.0 - slowTime + rowNoise * 1.8) * 6.2831853);
    float scan = smoothstep(0.70, 0.98, wave);

    float bandCenter = 0.5 + 0.30 * sin(slowTime * 0.47 + Seed * 9.0);
    float band = 1.0 - smoothstep(0.035, 0.13, abs(uv.y - bandCenter));
    float fracture = hash21(floor(uv * vec2(7.0, 10.0)) + Seed * 23.0);
    float breathing = 0.5 + 0.5 * sin(slowTime * 0.58 + fracture * 6.2831853);
    float shards = smoothstep(0.60, 0.92, breathing) * smoothstep(0.54, 0.78, fracture);

    float grain = valueNoise(uv * vec2(13.0, 17.0) + vec2(Seed * 11.0, slowTime * 0.20));
    float chroma = smoothstep(0.48, 0.88, rowNoise + band * 0.25);
    vec3 violet = vec3(0.53, 0.39, 0.72);
    vec3 verdigris = vec3(0.34, 0.72, 0.64);
    vec3 ember = vec3(0.82, 0.65, 0.34);
    vec3 color = mix(violet, verdigris, chroma);
    color = mix(color, ember, band * (0.32 + grain * 0.18));

    float alpha = 0.105 + scan * 0.105 + band * 0.145 + shards * 0.085 + grain * 0.040;
    alpha *= mix(0.74, 1.0, Intensity) * mask;
    fragColor = vec4(color, min(alpha, 0.42));
}
