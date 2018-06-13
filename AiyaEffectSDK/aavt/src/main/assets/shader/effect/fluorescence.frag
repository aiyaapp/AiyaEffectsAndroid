precision highp float;

uniform sampler2D uTexture;
uniform sampler2D uTexture2;
uniform float uWidth;
uniform float uHeight;
varying vec2 vTextureCo;

uniform vec4 uBorderColor;
uniform float uStep;

void main(){
    vec4 baseColor=texture2D(uTexture,vTextureCo);
    float sobelColor=texture2D(uTexture2,vTextureCo).r;
    gl_FragColor=(1.-sobelColor*uStep)*baseColor+sobelColor*uBorderColor*uStep;
}