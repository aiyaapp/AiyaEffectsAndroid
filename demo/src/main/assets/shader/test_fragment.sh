precision mediump float;
varying vec2 textureCoordinate;
uniform sampler2D vTexture;
void main() {
    gl_FragColor = vec4(1.0,0.5,0.0,1.0);//texture2D( vTexture, textureCoordinate );
}