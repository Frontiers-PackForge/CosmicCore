#version 150

in vec2 texCoord;
out vec4 fragColor;

uniform float GameTime;
uniform vec2 ScreenSize;
uniform vec2 Center;
uniform vec3 BaseColor;
uniform float Intensity;
uniform float Radius;

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

void main() {
    float time = GameTime * 1200.0;

    vec2 aspectRatio = vec2(ScreenSize.x / ScreenSize.y, 1.0);
    vec2 uv = (texCoord - Center) * aspectRatio;

    float dist = length(uv);

    float alpha = 0.0;
    vec3 color = BaseColor;

    // Slow water ripples
    float ringSpeed = 0.12;
    for (int i = 0; i < 3; i++) {
        float phase = float(i) * 2.5 + time * ringSpeed;
        float ringDist = mod(phase, Radius * 2.8);
        float ringAlpha = 1.0 - ringDist / (Radius * 2.8);
        ringAlpha = ringAlpha * ringAlpha * ringAlpha;
        float ring = smoothstep(0.06, 0.0, abs(dist - ringDist)) * ringAlpha;
        alpha += ring * 0.15;
    }

    // Orbiting wisps
    for (int i = 0; i < 4; i++) {
        float fi = float(i);
        float orbitSpeed = 0.3 + fi * 0.1;
        float orbitRadius = Radius * (0.7 + fi * 0.15);
        float orbitAngle = time * orbitSpeed + fi * 1.571;

        vec2 wispPos = vec2(cos(orbitAngle), sin(orbitAngle)) * orbitRadius;
        float wispDist = length(uv - wispPos);

        float wispCore = smoothstep(Radius * 0.2, 0.0, wispDist);
        wispCore = wispCore * wispCore;

        float trailAngle = orbitAngle - 0.4;
        vec2 trailPos = vec2(cos(trailAngle), sin(trailAngle)) * orbitRadius;
        float trailDist = length(uv - trailPos);
        float trail = smoothstep(Radius * 0.25, 0.0, trailDist) * 0.3;

        alpha += (wispCore + trail) * 0.2;
    }

    // Aurora flow (cartesian to avoid atan discontinuity)
    float flow1 = sin(uv.x * 8.0 + uv.y * 6.0 + time * 0.6 + dist * 4.0) * 0.5 + 0.5;
    flow1 = flow1 * flow1;
    flow1 *= smoothstep(Radius * 1.6, Radius * 0.6, dist);
    flow1 *= smoothstep(Radius * 0.3, Radius * 0.7, dist);

    float flow2 = sin(uv.x * 6.0 - uv.y * 8.0 - time * 0.4 + dist * 3.0 + 2.0) * 0.5 + 0.5;
    flow2 = flow2 * flow2;
    flow2 *= smoothstep(Radius * 1.4, Radius * 0.5, dist);
    flow2 *= smoothstep(Radius * 0.25, Radius * 0.6, dist);

    alpha += (flow1 + flow2) * 0.1;

    // Floating embers
    for (int i = 0; i < 6; i++) {
        float fi = float(i);
        float seed = fi * 127.1;

        float px = (hash(vec2(seed, 0.0)) - 0.5) * Radius * 1.6;
        float baseY = hash(vec2(seed, 1.0)) * Radius * 2.0;
        float py = mod(baseY - time * 0.15 - fi * 0.15, Radius * 2.5) - Radius * 0.3;
        px += sin(time * 0.3 + fi * 2.0) * Radius * 0.08;

        vec2 emberPos = vec2(px, -py);
        float emberDist = length(uv - emberPos);

        float ember = smoothstep(Radius * 0.1, 0.0, emberDist);
        ember = ember * ember;

        float heightFade = smoothstep(Radius * 1.8, Radius * 0.3, -py);

        alpha += ember * heightFade * 0.25;
        color = mix(color, vec3(1.0, 0.95, 0.85), ember * heightFade * 0.2);
    }

    // Soft tendrils
    for (int i = 0; i < 4; i++) {
        float fi = float(i);
        float tendrilAngle = fi * 1.571 + time * 0.1 + sin(time * 0.2 + fi) * 0.2;

        float alongTendril = dot(uv, vec2(cos(tendrilAngle), sin(tendrilAngle)));
        float perpTendril = length(uv - vec2(cos(tendrilAngle), sin(tendrilAngle)) * max(alongTendril, 0.0));

        float tendrilWidth = Radius * 0.1 * (1.0 - alongTendril / (Radius * 1.8));
        tendrilWidth = max(tendrilWidth, 0.02);

        float tendril = smoothstep(tendrilWidth, 0.0, perpTendril);
        tendril *= smoothstep(0.0, Radius * 0.5, alongTendril);
        tendril *= smoothstep(Radius * 1.6, Radius * 0.7, alongTendril);
        tendril *= 0.6 + 0.4 * sin(alongTendril * 5.0 - time * 1.5);

        alpha += tendril * 0.1;
    }

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
