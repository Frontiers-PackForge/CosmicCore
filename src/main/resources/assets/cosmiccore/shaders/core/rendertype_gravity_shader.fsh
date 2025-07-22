#version 150

uniform sampler2D DiffuseSampler;
uniform float Time;
in vec2 texCoord;
out vec4 fragColor;

void main() {
    //Help.
    //Gravity Lens
    float dist = distance(texCoord, vec2(0.5));
    float str = 0.05 / (dist + 0.1);

    vec2 offset = normalize(texCoord - vec2(0.5)) * str * sin(Time * 2);
    vec4 color = texture(DiffuseSampler,texCoord + offset);

    fragColor = color;
}