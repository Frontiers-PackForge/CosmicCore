#version 150

uniform float DepthsTime;
uniform float Awaken;
uniform float Vortex;
uniform float PixelGrid;
uniform float Alpha;

in vec2 texCoord;
out vec4 fragColor;

const vec3 GOLD = vec3(0.941, 0.851, 0.659);
const vec3 SILVER = vec3(0.784, 0.831, 0.910);
const vec3 DEEPBLUE = vec3(0.075, 0.098, 0.180);

void main() {
    vec2 p = texCoord * 2.0 - 1.0;
    p = (floor(p * PixelGrid) + 0.5) / PixelGrid;
    float r = length(p);
    if (r > 0.97) discard;
    float ang = atan(p.y, p.x);
    float lr = log(r + 0.05);
    float iv1 = max(sin(ang * 3.0 + lr * 10.0 + DepthsTime * 2.2), 0.0);
    float iv2 = max(sin(ang * 5.0 - lr * 14.0 - DepthsTime * 3.1), 0.0);
    float iv3 = max(sin(ang * 2.0 + lr * 7.0 + DepthsTime * 1.4), 0.0);
    float pull = smoothstep(0.97, 0.10, r);
    float v = (iv1 * 0.9 + iv2 * 0.55 + iv3 * 0.45) * pull * Vortex;
    v *= 1.0 + (1.0 - r) * 0.8;
    v = floor(v * 5.0 + 0.5) / 5.0;
    float warm = smoothstep(0.5, 0.0, r) * 0.35;
    warm = floor(warm * 4.0 + 0.5) / 4.0;
    float a = (v * 1.1 + warm * 0.5) * Awaken * Alpha;
    if (a <= 0.01) discard;
    a = min(a, 1.0);
    vec3 outc = mix(DEEPBLUE, GOLD, clamp(v, 0.0, 1.0));
    outc = mix(outc, SILVER, iv2 * 0.25);
    fragColor = vec4(outc, a);
}
