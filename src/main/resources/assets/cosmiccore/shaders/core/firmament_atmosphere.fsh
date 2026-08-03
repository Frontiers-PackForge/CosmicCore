#version 150

in vec3 skyDirection;

out vec4 fragColor;

uniform float SolarAzimuth;

float hash31(vec3 point) {
    point = fract(point * 0.1031);
    point += dot(point, point.yzx + 33.33);
    return fract((point.x + point.y) * point.z);
}

float noise3(vec3 point) {
    vec3 cell = floor(point);
    vec3 local = fract(point);
    local = local * local * (3.0 - 2.0 * local);

    float n000 = hash31(cell);
    float n100 = hash31(cell + vec3(1.0, 0.0, 0.0));
    float n010 = hash31(cell + vec3(0.0, 1.0, 0.0));
    float n110 = hash31(cell + vec3(1.0, 1.0, 0.0));
    float n001 = hash31(cell + vec3(0.0, 0.0, 1.0));
    float n101 = hash31(cell + vec3(1.0, 0.0, 1.0));
    float n011 = hash31(cell + vec3(0.0, 1.0, 1.0));
    float n111 = hash31(cell + vec3(1.0, 1.0, 1.0));

    float lower = mix(mix(n000, n100, local.x), mix(n010, n110, local.x), local.y);
    float upper = mix(mix(n001, n101, local.x), mix(n011, n111, local.x), local.y);
    return mix(lower, upper, local.z);
}

float fbm3(vec3 point) {
    float value = 0.0;
    float amplitude = 0.53;
    for (int octave = 0; octave < 4; octave++) {
        value += noise3(point) * amplitude;
        point = point * 2.03 + vec3(1.71, -2.13, 0.83);
        amplitude *= 0.47;
    }
    return value;
}

float stepped(float value, float levels) {
    return floor(clamp(value, 0.0, 1.0) * levels + 0.5) / levels;
}

void main() {
    vec3 direction = normalize(skyDirection);
    float height = direction.y;
    vec3 color;

    if (height >= 0.48) {
        color = mix(vec3(0.095, 0.075, 0.225), vec3(0.018, 0.020, 0.090),
                (height - 0.48) / 0.52);
    } else if (height >= 0.0) {
        color = mix(vec3(0.315, 0.135, 0.365), vec3(0.095, 0.075, 0.225), height / 0.48);
    } else if (height >= -0.42) {
        color = mix(vec3(0.315, 0.135, 0.365), vec3(0.025, 0.175, 0.620), -height / 0.42);
    } else {
        color = mix(vec3(0.025, 0.175, 0.620), vec3(0.008, 0.025, 0.155),
                (-height - 0.42) / 0.58);
    }

    float upperSky = smoothstep(0.08, 0.52, height);
    float broad = fbm3(direction * 2.45 + vec3(1.20, -0.35, 2.10));
    float broken = noise3(direction * 5.20 + vec3(-2.30, 1.70, 0.40));
    float ridge = 1.0 - abs(broad * 2.0 - 1.0);
    float shoal = smoothstep(0.48, 0.76, broad * 0.68 + ridge * 0.32);
    float filament = smoothstep(0.61, 0.82, ridge * 0.72 + broken * 0.28);
    float dust = smoothstep(0.56, 0.78, broken);
    shoal = stepped(shoal, 4.0);
    filament = stepped(filament, 5.0);
    dust = stepped(dust, 4.0);

    vec3 sunDirection = normalize(vec3(cos(SolarAzimuth), -0.035, sin(SolarAzimuth)));
    float solarSuppression = 1.0 - smoothstep(0.68, 0.94, dot(direction, sunDirection));
    float nebula = upperSky * solarSuppression;
    color += vec3(0.035, 0.090, 0.235) * shoal * nebula * 0.58;
    color += vec3(0.100, 0.310, 0.480) * filament * nebula * 0.23;
    color += vec3(0.190, 0.130, 0.320) * shoal * filament * nebula * 0.16;
    color *= 1.0 - dust * upperSky * 0.10;

    float horizon = max(0.0, 1.0 - abs(height) * 3.4);
    horizon *= horizon;
    float horizontalLength = max(length(direction.xz), 0.0001);
    float solarFacing = pow(max(0.0, dot(direction.xz / horizontalLength, sunDirection.xz)), 6.0);
    float warmth = horizon * solarFacing * 0.82;
    color = mix(color, vec3(1.0, 0.40, 0.11), warmth);

    float dither = hash31(floor((direction + 1.0) * 96.0)) - 0.5;
    color += dither / 255.0;
    color = floor(clamp(color, 0.0, 1.0) * 64.0 + 0.5) / 64.0;
    fragColor = vec4(color, 1.0);
}
