precision highp float;

uniform sampler2D u_diffuseTexture;

///////////////////////////////////////////////////////////
// Varyings
varying vec2 v_texCoord;
varying vec3 v_normal;

void main()
{
    gl_FragColor = texture2D(u_diffuseTexture, v_texCoord);
	gl_FragColor.a = 1.0;
}
