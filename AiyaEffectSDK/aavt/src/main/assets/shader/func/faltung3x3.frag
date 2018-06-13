precision mediump float;
varying vec2 vTextureCo;
uniform sampler2D uTexture;
uniform mat3 uFaltung;
uniform float uWidth;
uniform float uHeight;


vec4 getColor(float x,float y,float p){
    return p*texture2D(uTexture,vec2(x/uWidth,y/uHeight)+vTextureCo);
}

void main() {

    vec4 color;

    color+=getColor(-1.,-1.,uFaltung[0][0]);
    color+=getColor(0.,-1.,uFaltung[1][0]);
    color+=getColor(1.,-1.,uFaltung[2][0]);

    color+=getColor(-1.,0.,uFaltung[0][1]);
    color+=getColor(0.,0.,uFaltung[1][1]);
    color+=getColor(1.,0.,uFaltung[2][1]);

    color+=getColor(-1.,1.,uFaltung[0][2]);
    color+=getColor(0.,1.,uFaltung[1][2]);
    color+=getColor(1.,1.,uFaltung[2][2]);

    gl_FragColor = color;
}