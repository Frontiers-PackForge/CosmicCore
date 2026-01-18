#version 150

in vec2 texCoord;
out vec4 fragColor;

uniform float GameTime;
uniform vec2 ScreenSize;
uniform float Intensity;

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
    float value = 0.0;
    float amplitude = 0.5;
    for (int i = 0; i < 5; i++) {
        value += amplitude * noise(p);
        p *= 2.0;
        amplitude *= 0.5;
    }
    return value;
}

void main() {
    float time = GameTime * 1200.0;

    vec2 uv = texCoord;
    vec2 aspect = vec2(ScreenSize.x / ScreenSize.y, 1.0);
    vec2 centered = (uv - 0.5) * aspect;

    vec3 color = vec3(0.0);

    // Void mist
    vec2 mistUV1 = centered * 1.5 + vec2(time * 0.02, time * 0.01);
    vec2 mistUV2 = centered * 2.0 + vec2(-time * 0.015, time * 0.025);

    float mist1 = fbm(mistUV1) * 0.5 + 0.5;
    float mist2 = fbm(mistUV2) * 0.5 + 0.5;
    float mist = mist1 * mist2;

    vec3 mistColor = vec3(0.08, 0.04, 0.12);
    color += mistColor * mist * 0.4;

    // Drifting wisps
    for (int i = 0; i < 4; i++) {
        float fi = float(i);
        float wispTime = time * 0.03 + fi * 1.57;

        vec2 wispCenter = vec2(
            sin(wispTime * 0.7 + fi * 2.0) * 0.3,
            cos(wispTime * 0.5 + fi * 1.5) * 0.25
        );

        vec2 toWisp = centered - wispCenter;
        float wispDist = length(toWisp);
        float wisp = exp(-wispDist * 4.0) * 0.3;

        vec3 wispColor = mix(
            vec3(0.15, 0.08, 0.20),
            vec3(0.10, 0.12, 0.18),
            sin(fi * 1.5) * 0.5 + 0.5
        );

        color += wispColor * wisp;
    }

    // Energy tendrils
    float tendrilNoise = fbm(centered * 3.0 + vec2(time * 0.05, 0.0));
    tendrilNoise = pow(tendrilNoise, 3.0);

    vec3 tendrilColor = vec3(0.12, 0.06, 0.15);
    color += tendrilColor * tendrilNoise * 0.2;

    // Floating dust
    for (int i = 0; i < 8; i++) {
        float fi = float(i);
        float seed = fi * 127.1;

        float px = (hash(vec2(seed, 0.0)) - 0.5) * 1.5;
        float py = mod(hash(vec2(seed, 1.0)) - time * 0.02 - fi * 0.05, 1.5) - 0.75;
        px += sin(time * 0.1 + fi * 2.0) * 0.05;

        vec2 particlePos = vec2(px, py);
        float particleDist = length(centered - particlePos);

        float particle = exp(-particleDist * 50.0) * 0.3;
        float twinkle = sin(time * (0.5 + fi * 0.2) + fi * 3.0) * 0.3 + 0.7;

        color += vec3(0.3, 0.25, 0.35) * particle * twinkle;
    }

    // Ambient pulse
    float pulse = sin(time * 0.15) * 0.02 + 0.98;
    color *= pulse;

    // Vignette
    float vignette = 1.0 - length(centered) * 0.6;
    vignette = clamp(vignette, 0.0, 1.0);
    vignette = pow(vignette, 1.5);
    color *= vignette;

    color *= Intensity;

    fragColor = vec4(color, 1.0);
}
