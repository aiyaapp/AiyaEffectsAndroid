#version 300 es
precision highp float;
precision highp int;

uniform sampler2D tex1;

//in vec3 ourColor;
in vec2 TexCoord;

out vec4 frag_color;

void main()
{
    frag_color = texture(tex1, TexCoord);
}