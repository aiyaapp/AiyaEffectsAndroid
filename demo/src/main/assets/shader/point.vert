attribute vec4 vPosition;
attribute vec2 vCoord;
uniform mat4 vMatrix;

varying vec2 textureCoordinate;

void main(){
    gl_Position = vMatrix*vPosition;
    gl_PointSize=6.0;
    textureCoordinate = vCoord;
}