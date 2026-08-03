#version 150

in vec3 Position;
in vec2 UV0;
in vec4 Color;

out vec2 currentUv;
out vec3 worldPosition;
out float currentStrength;
out float currentPhase;
out float currentMode;
out float currentOpacity;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec3 CameraPos;

void main() {
    currentUv = UV0;
    worldPosition = Position + CameraPos;
    currentStrength = Color.r;
    currentPhase = Color.g;
    currentMode = Color.b;
    currentOpacity = Color.a;
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
}
