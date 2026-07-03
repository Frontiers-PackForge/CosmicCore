#version 150

uniform float DiskTime;
uniform float InnerR;
uniform float PixelGrid;
uniform float Alpha;

in vec2 texCoord;
out vec4 fragColor;

const vec3 GOLD = vec3(0.910, 0.753, 0.478);
const vec3 SILVER = vec3(0.604, 0.659, 0.761);
const vec3 DEEP = vec3(0.690, 0.537, 0.282);
const vec3 HOT = vec3(1.0, 0.953, 0.839);

void main() {
    vec2 p = texCoord * 2.0 - 1.0;
    p = (floor(p * PixelGrid) + 0.5) / PixelGrid;
    float r = length(p);
    if (r > 0.98 || r < InnerR) discard;
    float ang = atan(p.y, p.x);
    float band = sin(ang * 7.0 + r * 18.0 - DiskTime * 2.0);
    float band2 = sin(ang * 3.0 - r * 11.0 + DiskTime * 1.25);
    float streak = max(band, 0.0) * 0.75 + max(band2, 0.0) * 0.45;
    float fadeOut = smoothstep(0.98, 0.62, r);
    float fadeIn = smoothstep(InnerR, InnerR + 0.10, r);
    float v = streak * fadeOut * fadeIn;
    v = floor(v * 4.0 + 0.5) / 4.0;
    if (v <= 0.01) discard;
    vec3 col = mix(SILVER, GOLD, clamp(max(band, 0.0) + 0.2, 0.0, 1.0));
    col = mix(col, DEEP, smoothstep(0.5, 0.95, r) * 0.4);
    col = mix(col, HOT, smoothstep(InnerR + 0.14, InnerR, r));
    fragColor = vec4(col, v * Alpha);
}
