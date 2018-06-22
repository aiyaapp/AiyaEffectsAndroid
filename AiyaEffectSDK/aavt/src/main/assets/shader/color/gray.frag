precision mediump float;
varying vec2 vTextureCo;
uniform sampler2D uTexture;
const highp vec3 CO = vec3(0.2125, 0.7154, 0.0721);

void main() {
    gl_FragColor=vec4(vec3(dot(texture2D( uTexture, vTextureCo).rgb,CO)),1.0);
}