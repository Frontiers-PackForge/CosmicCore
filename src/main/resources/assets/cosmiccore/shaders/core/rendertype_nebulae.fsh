#version 150

#moj_import <matrix.glsl>

//CBS
//Parallax scrolling fractal galaxy.
//Inspired by JoshP's Simplicity shader: https://www.shadertoy.com/view/lslGWr

// Source: https://www.shadertoy.com/view/MslGWN
// Modified by Screret to work within Minecraft

// GameTime is how many days have passed in fractional days. mutliply by 1200 to get seconds, or 24000 to get ticks.
uniform float GameTime;
// framebuffer width & height
uniform vec2 ScreenSize;
// Modify these (in rendertype_nebulae.json) to get different colors/brightnesses
uniform vec4 Frequencies;
// Multiplier for GameTime, changes how fast the effect scrolls
uniform float ScrollSpeed;
uniform int Layer0Iterations;
uniform int Layer1Iterations;

// http://www.fractalforums.com/new-theories-and-research/very-simple-formula-for-fractal-patterns/
float field(in vec3 p, float s, int iterations) {
    float strength = 7.0 + 0.03 * log(1.e-6 + fract(sin(GameTime * ScrollSpeed) * 4373.11));
    float accum = s / 4.0;
    float prev = 0.0;
    float tw = 0.0;
    for (int i = 0; i < iterations; ++i) {
        float mag = dot(p, p);
        p = abs(p) / mag + vec3(-0.5, -0.4, -1.5);
        float w = exp(-float(i) / 7.0);
        accum += w * exp(-strength * pow(abs(mag - prev), 2.2));
        tw += w;
        prev = mag;
    }
    return max(0.0, 5.0 * accum / tw - 0.7);
}

vec3 nrand3(vec2 co) {
    vec3 a = fract(cos(co.x * 8.3e-3 + co.y) * vec3(1.3e5, 4.7e5, 2.9e5));
    vec3 b = fract(sin(co.x * 0.3e-3 + co.y) * vec3(8.1e5, 1.0e5, 0.1e5));
    vec3 c = mix(a, b, 0.5);
    return c;
}
out vec4 fragColor;


void main() {
    // multiplied by ScrollSpeed to get a reasonable speed
    float GameTime = GameTime * ScrollSpeed;

    vec2 uv = gl_FragCoord.xy / ScreenSize - 1.0;
    vec2 uvs = uv * ScreenSize / max(ScreenSize.x, ScreenSize.y);
    vec3 p = vec3(uvs / 4.0, 0.0) + vec3(1.0, -1.3, 0.0);
    p += 0.2 * vec3(sin(GameTime / 16.0), sin(GameTime / 12.0),  sin(GameTime / 128.0));

    float t = field(p, Frequencies[2], Layer0Iterations);
    float v = (1.0 - exp((abs(uv.x) - 1.0) * 6.0)) * (1.0 - exp((abs(uv.y) - 1.0) * 6.0));

    //Second Layer
    vec3 p2 = vec3(uvs / (4.0 + sin(GameTime * 0.11) * 0.2 + 0.2 + sin(GameTime * 0.15) * 0.3 + 0.4), 1.5) + vec3(2.0, -1.3, -1.0);
    p2 += 0.25 * vec3(sin(GameTime / 16.0), sin(GameTime / 12.0), sin(GameTime / 128.0));
    float t2 = field(p2, Frequencies[3], Layer1Iterations);
    vec4 c2 = mix(0.4, 1.0, v) * vec4(1.3 * t2 * t2 * t2, 1.8 * t2 * t2, t2 * Frequencies[0], t2);


    //Let's add some stars
    //Thanks to http://glsl.heroku.com/e#6904.0
    vec2 seed = p.xy * 2.0;
    seed = floor(seed * ScreenSize.x);
    vec3 rnd = nrand3(seed);
    vec4 starcolor = vec4(pow(rnd.y, 40.0));

    //Second Layer
    vec2 seed2 = p2.xy * 2.0;
    seed2 = floor(seed2 * ScreenSize.x);
    vec3 rnd2 = nrand3(seed2);
    starcolor += vec4(pow(rnd2.y, 40.0));

    fragColor = mix(Frequencies[3] - 0.3, 1.0, v) * vec4(1.5 * Frequencies[2] * t * t * t, 1.2 * Frequencies[1] * t * t, Frequencies[3] * t, 1.0) + c2 + starcolor;
}
