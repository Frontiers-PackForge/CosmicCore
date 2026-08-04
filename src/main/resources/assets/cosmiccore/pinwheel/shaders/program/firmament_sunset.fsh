#include veil:space_helper

uniform sampler2D DiffuseSampler0;
uniform sampler2D DiffuseDepthSampler;
uniform vec2 InSize;

in vec2 texCoord;

out vec4 fragColor;

const vec3 SUN_DIRECTION = vec3(0.999388, 0.034979, 0.0);
const vec3 SUN_GOLD = vec3(1.0, 0.39, 0.085);
const vec3 SUN_PEACH = vec3(0.88, 0.245, 0.155);
const vec3 SHADOW_VIOLET = vec3(0.105, 0.065, 0.245);

bool connectedPosition(vec3 center, vec3 neighbor, float maximumSpan) {
    vec3 separation = neighbor - center;
    return dot(separation, separation) <= maximumSpan * maximumSpan;
}

vec3 macroNormal(vec3 center, vec3 localNormal, vec3 cameraDirection, float cameraDistance) {
    if (cameraDistance < 24.0) return localNormal;

    vec2 texel = 1.0 / InSize;
    float radius = mix(1.0, 2.0, smoothstep(72.0, 288.0, cameraDistance));
    vec2 offsetX = vec2(texel.x * radius, 0.0);
    vec2 offsetY = vec2(0.0, texel.y * radius);
    float depthLeft = texture(DiffuseDepthSampler, texCoord - offsetX).r;
    float depthRight = texture(DiffuseDepthSampler, texCoord + offsetX).r;
    float depthDown = texture(DiffuseDepthSampler, texCoord - offsetY).r;
    float depthUp = texture(DiffuseDepthSampler, texCoord + offsetY).r;
    vec3 positionLeft = screenToWorldSpace(texCoord - offsetX, depthLeft).xyz;
    vec3 positionRight = screenToWorldSpace(texCoord + offsetX, depthRight).xyz;
    vec3 positionDown = screenToWorldSpace(texCoord - offsetY, depthDown).xyz;
    vec3 positionUp = screenToWorldSpace(texCoord + offsetY, depthUp).xyz;
    float maximumSpan = 1.5 + cameraDistance * 0.028;
    bool validLeft = depthLeft < 0.99999 && connectedPosition(center, positionLeft, maximumSpan);
    bool validRight = depthRight < 0.99999 && connectedPosition(center, positionRight, maximumSpan);
    bool validDown = depthDown < 0.99999 && connectedPosition(center, positionDown, maximumSpan);
    bool validUp = depthUp < 0.99999 && connectedPosition(center, positionUp, maximumSpan);

    vec3 tangentX = validLeft && validRight ? positionRight - positionLeft : dFdx(center);
    vec3 tangentY = validDown && validUp ? positionUp - positionDown : dFdy(center);
    vec3 normalCross = cross(tangentX, tangentY);
    if (dot(normalCross, normalCross) < 1.0e-20) return localNormal;

    vec3 result = normalize(normalCross);
    if (dot(result, cameraDirection) < 0.0) result = -result;
    return result;
}

void main() {
    vec4 scene = texture(DiffuseSampler0, texCoord);
    float depth = texture(DiffuseDepthSampler, texCoord).r;
    if (depth >= 0.99999) {
        fragColor = scene;
        gl_FragDepth = depth;
        return;
    }

    vec3 worldPosition = screenToWorldSpace(texCoord, depth).xyz;
    vec3 normalCross = cross(dFdx(worldPosition), dFdy(worldPosition));
    if (dot(normalCross, normalCross) < 1.0e-20) {
        fragColor = scene;
        gl_FragDepth = depth;
        return;
    }

    vec3 worldNormal = normalize(normalCross);
    vec3 cameraDirection = normalize(VeilCamera.CameraPosition - worldPosition);
    if (dot(worldNormal, cameraDirection) < 0.0) {
        worldNormal = -worldNormal;
    }

    float cameraDistance = distance(VeilCamera.CameraPosition, worldPosition);
    vec3 terrainNormal = macroNormal(worldPosition, worldNormal, cameraDirection, cameraDistance);
    float macroBlend = smoothstep(32.0, 176.0, cameraDistance) * 0.78;
    float facing = mix(dot(worldNormal, SUN_DIRECTION), dot(terrainNormal, SUN_DIRECTION), macroBlend);
    float direct = smoothstep(-0.20, 0.76, facing);
    float corridorScatter = 0.20 + 0.20 * (1.0 - abs(worldNormal.y));

    float materialPeak = max(scene.r, max(scene.g, scene.b));
    vec3 materialTint = materialPeak > 0.004 ? scene.rgb / materialPeak : vec3(0.0);
    vec3 material = mix(vec3(0.54), clamp(materialTint, 0.0, 1.0), 0.34);
    vec3 warmPalette = mix(SUN_PEACH, SUN_GOLD, direct);
    float warmStrength = corridorScatter * 0.14 + direct * 0.92;
    vec3 warmLight = material * warmPalette * warmStrength * 0.52;
    vec3 coolFill = material * SHADOW_VIOLET * (0.20 + 0.16 * (1.0 - direct));
    vec3 result = scene.rgb + (warmLight + coolFill) * (vec3(1.0) - scene.rgb);

    fragColor = vec4(clamp(result, 0.0, 1.0), scene.a);
    gl_FragDepth = depth;
}
