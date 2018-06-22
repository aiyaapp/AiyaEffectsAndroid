///////////////////////////////////////////////////////////
// Atributes
attribute vec4 a_position;
attribute vec2 a_texCoord;
attribute vec3 a_normal;

///////////////////////////////////////////////////////////
// Uniforms
uniform mat4 u_worldViewProjectionMatrix;
///////////////////////////////////////////////////////////
// Varyings
varying vec2 v_texCoord;
varying vec3 v_normal;

void main()
{
    gl_Position = u_worldViewProjectionMatrix * a_position;
    v_texCoord = a_texCoord;
	v_normal = a_normal;
}
