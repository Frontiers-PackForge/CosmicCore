#version 150

in vec2 texCoord;
out vec4 fragColor;

uniform float GameTime;
uniform vec2 ScreenSize;
uniform float Intensity;

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

float hash21(vec2 p) {
    p = fract(p * vec2(234.34, 435.345));
    p += dot(p, p + 34.23);
    return fract(p.x * p.y);
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
    float value = 0.0;
    float amplitude = 0.5;
    for (int i = 0; i < 6; i++) {
        value += amplitude * noise(p);
        p *= 2.0;
        amplitude *= 0.5;
    }
    return value;
}

float stars(vec2 uv, float density, float seed) {
    vec2 gv = fract(uv * density) - 0.5;
    vec2 id = floor(uv * density);

    float star = 0.0;
    float rnd = hash21(id + seed);

    if (rnd > 0.8) {
        float size = (rnd - 0.8) * 5.0;
        star = smoothstep(0.1 * size + 0.02, 0.0, length(gv));
    }

    return star;
}

void main() {
    float time = GameTime * 1200.0;

    vec2 uv = texCoord;
    vec2 aspect = vec2(ScreenSize.x / ScreenSize.y, 1.0);
    vec2 centered = (uv - 0.5) * aspect;

    vec3 color = vec3(0.01, 0.01, 0.02);

    // Nebula clouds
    vec2 nebulaUV1 = centered * 2.0 + vec2(time * 0.005, time * 0.003);
    vec2 nebulaUV2 = centered * 1.5 + vec2(-time * 0.004, time * 0.006);
    vec2 nebulaUV3 = centered * 3.0 + vec2(time * 0.003, -time * 0.004);

    float nebula1 = fbm(nebulaUV1);
    float nebula2 = fbm(nebulaUV2);
    float nebula3 = fbm(nebulaUV3);

    nebula1 = pow(nebula1, 2.0) * smoothstep(0.3, 0.7, nebula1);
    nebula2 = pow(nebula2, 2.5) * smoothstep(0.35, 0.75, nebula2);
    nebula3 = pow(nebula3, 2.0) * smoothstep(0.25, 0.6, nebula3);

    vec3 nebulaColor1 = vec3(0.15, 0.05, 0.25);
    vec3 nebulaColor2 = vec3(0.05, 0.10, 0.20);
    vec3 nebulaColor3 = vec3(0.20, 0.08, 0.12);

    color += nebulaColor1 * nebula1 * 0.6;
    color += nebulaColor2 * nebula2 * 0.5;
    color += nebulaColor3 * nebula3 * 0.4;

    // Bright nebula cores
    float brightCore1 = pow(nebula1, 4.0) * 2.0;
    float brightCore2 = pow(nebula2, 4.0) * 1.5;

    color += vec3(0.4, 0.2, 0.5) * brightCore1 * 0.3;
    color += vec3(0.2, 0.3, 0.5) * brightCore2 * 0.25;

    // Cosmic dust
    vec2 dustUV = centered * 4.0 + vec2(time * 0.002, 0.0);
    float dust = fbm(dustUV);
    dust = smoothstep(0.4, 0.6, dust);
    color *= 1.0 - dust * 0.3;

    // Star layers
    float starLayer1 = stars(uv + vec2(0.0, time * 0.001), 80.0, 1.0);
    color += vec3(0.6, 0.6, 0.7) * starLayer1 * 0.3;

    float starLayer2 = stars(uv + vec2(time * 0.002, 0.0), 40.0, 2.0);
    color += vec3(0.8, 0.8, 0.9) * starLayer2 * 0.5;

    float starLayer3 = stars(uv, 20.0, 3.0);
    float twinkle = sin(time * 0.5 + hash(floor(uv * 20.0)) * 6.28) * 0.3 + 0.7;
    color += vec3(1.0, 0.95, 0.9) * starLayer3 * twinkle * 0.8;

    // Colored stars
    float coloredStar = stars(uv + 0.5, 15.0, 4.0);
    vec3 starColor = mix(
        vec3(1.0, 0.7, 0.5),
        vec3(0.7, 0.8, 1.0),
        hash(floor(uv * 15.0 + 0.5))
    );
    color += starColor * coloredStar * 0.6;

    // Central glow
    float coreGlow = exp(-length(centered) * 2.0) * 0.15;
    color += vec3(0.3, 0.25, 0.35) * coreGlow;

    // Shimmer
    float shimmer = sin(time * 0.1 + fbm(centered * 5.0) * 6.28) * 0.02 + 1.0;
    color *= shimmer;

    // Vignette
    float vignette = 1.0 - length(centered) * 0.4;
    vignette = clamp(vignette, 0.0, 1.0);
    vignette = pow(vignette, 1.2);
    color *= 0.7 + vignette * 0.3;

    color *= Intensity;
    color = clamp(color, 0.0, 1.0);

    fragColor = vec4(color, 1.0);
}
