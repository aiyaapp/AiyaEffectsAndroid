#version 300 es
precision highp float;
precision highp int;

layout (location = 0) in vec2 position;
layout (location = 1) in vec3 color;
layout (location = 2) in vec2 texCoord;

out vec2 TexCoord;
out vec3 ourColor;

void main()
{
    gl_Position = vec4(position, 0, 1);
    ourColor = color;
    TexCoord = texCoord;
}