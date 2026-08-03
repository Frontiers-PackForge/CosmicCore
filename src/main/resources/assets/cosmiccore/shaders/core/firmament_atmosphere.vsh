#version 150

in vec3 Position;

out vec3 skyDirection;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform mat4 InverseViewMat;

void main() {
    skyDirection = normalize((InverseViewMat * vec4(Position, 1.0)).xyz);
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
}
