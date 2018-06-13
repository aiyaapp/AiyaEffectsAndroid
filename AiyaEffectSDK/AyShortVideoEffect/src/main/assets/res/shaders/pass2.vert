///////////////////////////////////////////////////////////
// Atributes
attribute vec4 a_position;
attribute vec3 a_normal;
attribute vec2 a_texCoord;

///////////////////////////////////////////////////////////
// Varyings
varying vec3 v_normal;
varying vec2 v_texCoord;

void main()
{
    gl_Position = a_position;
	v_normal = a_normal;
    v_texCoord = a_texCoord;
}
