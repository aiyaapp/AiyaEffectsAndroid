precision mediump float;
varying vec2 vTextureCo;
uniform sampler2D uTexture;
uniform float uWidth;
uniform float uHeight;


vec4 getColor(float x,float y,float p){
    return p*texture2D(uTexture,vec2(x/uWidth,y/uHeight)+vTextureCo);
}

void main() {

    vec4 color;

    color+=getColor(-2.,-2.,2.);
    color+=getColor(-1.,-2.,4.);
    color+=getColor(0.,-2.,5.);
    color+=getColor(1.,-2.,4.);
    color+=getColor(2.,-2.,2.);

    color+=getColor(-2.,-1.,4.);
    color+=getColor(-1.,-1.,9.);
    color+=getColor(0.,-1.,12.);
    color+=getColor(1.,-1.,9.);
    color+=getColor(2.,-1.,4.);

    color+=getColor(-2.,1.,4.);
    color+=getColor(-1.,1.,9.);
    color+=getColor(0.,1.,12.);
    color+=getColor(1.,1.,9.);
    color+=getColor(2.,1.,4.);

    color+=getColor(-2.,0.,9.);
    color+=getColor(-1.,0.,12.);
    color+=getColor(0.,0.,15.);
    color+=getColor(1.,0.,12.);
    color+=getColor(2.,0.,9.);

    color+=getColor(-2.,2.,2.);
    color+=getColor(-1.,2.,4.);
    color+=getColor(0.,2.,5.);
    color+=getColor(1.,2.,4.);
    color+=getColor(2.,2.,2.);

    gl_FragColor = color/159.;
}