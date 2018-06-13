precision highp float;
precision highp int;

varying vec2 vTextureCo;
uniform sampler2D uTexture;

uniform float uWidth;
uniform float uHeight;

float cY(float x,float y){
    vec4 c=texture2D(uTexture,vec2(x,y));
    return c.r*0.2126+c.g*0.7152+c.b*0.0722;
}

vec4 cC(float x,float y,float dx,float dy){
    vec4 c0=texture2D(uTexture,vec2(x,y));
    vec4 c1=texture2D(uTexture,vec2(x+dx,y));
    vec4 c2=texture2D(uTexture,vec2(x,y+dy));
    vec4 c3=texture2D(uTexture,vec2(x+dx,y+dy));
    return (c0+c1+c2+c3)/4.;
}

float cU(float x,float y,float dx,float dy){
    vec4 c=cC(x,y,dx,dy);
    return -0.09991*c.r - 0.33609*c.g + 0.43600*c.b+0.5000;
}

float cV(float x,float y,float dx,float dy){
    vec4 c=cC(x,y,dx,dy);
    return 0.61500*c.r - 0.55861*c.g - 0.05639*c.b+0.5000;
}

vec2 cPos(float t,float shiftx,float gy){
    vec2 pos=vec2(floor(uWidth*vTextureCo.x),floor(uHeight*gy));
    return vec2(mod(pos.x*shiftx,uWidth),(pos.y*shiftx+floor(pos.x*shiftx/uWidth))*t);
}

vec4 calculateY(){
    vec2 pos=cPos(1.,4.,vTextureCo.y);
    vec4 oColor=vec4(0);
    float textureYPos=pos.y/uHeight;
    oColor[0]=cY(pos.x/uWidth,textureYPos);
    oColor[1]=cY((pos.x+1.)/uWidth,textureYPos);
    oColor[2]=cY((pos.x+2.)/uWidth,textureYPos);
    oColor[3]=cY((pos.x+3.)/uWidth,textureYPos);
    return oColor;
}
vec4 calculateU(float gy,float dx,float dy){
    vec2 pos=cPos(2.,8.,vTextureCo.y-gy);
    vec4 oColor=vec4(0);
    float textureYPos=pos.y/uHeight;
    oColor[0]= cU(pos.x/uWidth,textureYPos,dx,dy);
    oColor[1]= cU((pos.x+2.)/uWidth,textureYPos,dx,dy);
    oColor[2]= cU((pos.x+4.)/uWidth,textureYPos,dx,dy);
    oColor[3]= cU((pos.x+6.)/uWidth,textureYPos,dx,dy);
    return oColor;
}
vec4 calculateV(float gy,float dx,float dy){
    vec2 pos=cPos(2.,8.,vTextureCo.y-gy);
    vec4 oColor=vec4(0);
    float textureYPos=pos.y/uHeight;
    oColor[0]=cV(pos.x/uWidth,textureYPos,dx,dy);
    oColor[1]=cV((pos.x+2.)/uWidth,textureYPos,dx,dy);
    oColor[2]=cV((pos.x+4.)/uWidth,textureYPos,dx,dy);
    oColor[3]=cV((pos.x+6.)/uWidth,textureYPos,dx,dy);
    return oColor;
}
vec4 calculateUV(float dx,float dy){
    vec2 pos=cPos(2.,4.,vTextureCo.y-0.2500);
    vec4 oColor=vec4(0);
    float textureYPos=pos.y/uHeight;
    oColor[0]= cU(pos.x/uWidth,textureYPos,dx,dy);
    oColor[1]= cV(pos.x/uWidth,textureYPos,dx,dy);
    oColor[2]= cU((pos.x+2.)/uWidth,textureYPos,dx,dy);
    oColor[3]= cV((pos.x+2.)/uWidth,textureYPos,dx,dy);
    return oColor;
}
vec4 calculateVU(float dx,float dy){
    vec2 pos=cPos(2.,4.,vTextureCo.y-0.2500);
    vec4 oColor=vec4(0);
    float textureYPos=pos.y/uHeight;
    oColor[0]= cV(pos.x/uWidth,textureYPos,dx,dy);
    oColor[1]= cU(pos.x/uWidth,textureYPos,dx,dy);
    oColor[2]= cV((pos.x+2.)/uWidth,textureYPos,dx,dy);
    oColor[3]= cU((pos.x+2.)/uWidth,textureYPos,dx,dy);
    return oColor;
}
