#extension GL_OES_EGL_image_external : require
precision highp float;
varying vec2 textureCoordinate;
uniform samplerExternalOES vTexture;
void main() {
    gl_FragColor = texture2D( vTexture, textureCoordinate );
}