attribute vec4 vPosition;
attribute vec2 vCoord;
varying vec2 textureCoordinate;

void main(){
    gl_Position = vPosition;
    textureCoordinate = vCoord;
}