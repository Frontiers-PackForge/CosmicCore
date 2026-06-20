#version 150

in vec2 texCoord;
out vec4 fragColor;

uniform float GameTime;
uniform vec2 ScreenSize;
uniform vec2 Center;
uniform vec3 BaseColor;
uniform float Intensity;
uniform float Radius;

void main() {
    float time = GameTime * 1200.0;

    vec2 aspectRatio = vec2(ScreenSize.x / ScreenSize.y, 1.0);
    vec2 uv = (texCoord - Center) * aspectRatio;

    float dist = length(uv);

    float alpha = 0.0;
    vec3 color = BaseColor;

    // Slow water ripples — expanding ring pulses
    float ringSpeed = 0.12;
    for (int i = 0; i < 3; i++) {
        float phase = float(i) * 2.5 + time * ringSpeed;
        float ringDist = mod(phase, Radius * 2.8);
        float ringAlpha = 1.0 - ringDist / (Radius * 2.8);
        ringAlpha = ringAlpha * ringAlpha * ringAlpha;
        float ring = smoothstep(0.06, 0.0, abs(dist - ringDist)) * ringAlpha;
        alpha += ring * 0.15;
    }

    // Aurora flow (cartesian wave bands)
    float flow1 = sin(uv.x * 8.0 + uv.y * 6.0 + time * 0.6 + dist * 4.0) * 0.5 + 0.5;
    flow1 = flow1 * flow1;
    flow1 *= smoothstep(Radius * 1.6, Radius * 0.6, dist);
    flow1 *= smoothstep(Radius * 0.3, Radius * 0.7, dist);

    float flow2 = sin(uv.x * 6.0 - uv.y * 8.0 - time * 0.4 + dist * 3.0 + 2.0) * 0.5 + 0.5;
    flow2 = flow2 * flow2;
    flow2 *= smoothstep(Radius * 1.4, Radius * 0.5, dist);
    flow2 *= smoothstep(Radius * 0.25, Radius * 0.6, dist);

    alpha += (flow1 + flow2) * 0.1;

    // Core glow
    float core = 1.0 - smoothstep(0.0, Radius * 0.6, dist);
    core = pow(core, 1.8);
    alpha += core * 0.25;

    // Outer glow
    float outerGlow = 1.0 - smoothstep(Radius * 0.2, Radius * 1.5, dist);
    outerGlow = outerGlow * outerGlow;
    alpha += outerGlow * 0.15;

    alpha *= Intensity;

    float boundary = 1.0 - smoothstep(Radius * 1.3, Radius * 2.0, dist);
    alpha *= boundary;

    alpha = clamp(alpha, 0.0, 1.0);

    fragColor = vec4(color, alpha);
}
