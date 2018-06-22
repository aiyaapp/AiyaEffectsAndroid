precision mediump float;

uniform sampler2D uTexture;
uniform sampler2D uNoiseTexture;
uniform float uWidth;
uniform float uHeight;
varying vec2 vTextureCo;

vec4 valueAdd(vec2 pos,float shiftX,float shiftY,float p){
    vec2 newPos=vec2((pos.x+shiftX)/uWidth,(pos.y+shiftY)/uHeight);
    return texture2D(uTexture,newPos)/p;
}

void main(){
    float step=floor(uWidth/128.);
    vec2 xy = vec2(vTextureCo.x * uWidth, vTextureCo.y * uHeight);
    vec4 color=valueAdd(xy,0.,0.,4.);
    color+=valueAdd(xy,-step,-step,16.);
    color+=valueAdd(xy,-step,step,8.);
    color+=valueAdd(xy,-step,step,16.);
    color+=valueAdd(xy,step,-step,8.);
    color+=valueAdd(xy,step,step,8.);
    color+=valueAdd(xy,step,-step,16.);
    color+=valueAdd(xy,step,step,8.);
    color+=valueAdd(xy,step,step,16.);
    gl_FragColor = color;
}