precision highp float;
precision highp int;

varying vec2 vTextureCo;
uniform sampler2D uTexture;

//为了简化计算，宽高都必须为8的倍数
uniform float uWidth;			// 纹理宽
uniform float uHeight;			// 纹理高

//转换公式
//Y’= 0.299*R’ + 0.587*G’ + 0.114*B’
//U’= -0.147*R’ - 0.289*G’ + 0.436*B’ = 0.492*(B’- Y’)
//V’= 0.615*R’ - 0.515*G’ - 0.100*B’ = 0.877*(R’- Y’)
//导出原理：采样坐标只作为确定输出位置使用，通过输出纹理计算实际采样位置，进行采样和并转换,
//然后将转换的结果填充到输出位置

float cY(float x,float y){
    vec4 c=texture2D(uTexture,vec2(x,y));
    return c.r*0.2990+c.g*0.5870+c.b*0.1140;
}

float cU(float x,float y){
    vec4 c=texture2D(uTexture,vec2(x,y));
    return -0.1471*c.r - 0.2889*c.g + 0.4360*c.b+0.5000;
}

float cV(float x,float y){
    vec4 c=texture2D(uTexture,vec2(x,y));
    return 0.6150*c.r - 0.5150*c.g - 0.1000*c.b+0.5000;
}

vec2 cPos(float t,float shiftx,float gy){
    vec2 pos=vec2(floor(uWidth*vTextureCo.x),floor(uHeight*gy));
    return vec2(mod(pos.x*shiftx,uWidth),(pos.y*shiftx+floor(pos.x*shiftx/uWidth))*t);
}

//Y分量的计算
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

//U分量的计算
vec4 calculateU(){
    vec2 pos=cPos(2.,8.,vTextureCo.y-0.2500);
    vec4 oColor=vec4(0);
    float textureYPos=pos.y/uHeight;
    oColor[0]= cU(pos.x/uWidth,textureYPos);
    oColor[1]= cU((pos.x+2.)/uWidth,textureYPos);
    oColor[2]= cU((pos.x+4.)/uWidth,textureYPos);
    oColor[3]= cU((pos.x+6.)/uWidth,textureYPos);
    return oColor;
}

//V分量计算
vec4 calculateV(){
    vec2 pos=cPos(2.,8.,vTextureCo.y-0.3125);
    vec4 oColor=vec4(0);
    float textureYPos=pos.y/uHeight;
    oColor[0]=cV(pos.x/uWidth,textureYPos);
    oColor[1]=cV((pos.x+2.)/uWidth,textureYPos);
    oColor[2]=cV((pos.x+4.)/uWidth,textureYPos);
    oColor[3]=cV((pos.x+6.)/uWidth,textureYPos);
    return oColor;
}

//UV的计算，YUV420SP用，test
vec4 calculateUV(){
    vec2 pos=cPos(2.,4.,vTextureCo.y-0.2500);
    vec4 oColor=vec4(0);
    float textureYPos=pos.y/uHeight;
    oColor[0]= cU(pos.x/uWidth,textureYPos);
    oColor[1]= cV(pos.x/uWidth,textureYPos);
    oColor[2]= cU((pos.x+2.)/uWidth,textureYPos);
    oColor[3]= cV((pos.x+2.)/uWidth,textureYPos);
    return oColor;
}

void main() {
    if(vTextureCo.y<0.2500){
        gl_FragColor=calculateY();
    }else if(vTextureCo.y<0.3125){
        gl_FragColor=calculateU();
    }else if(vTextureCo.y<0.3750){
        gl_FragColor=calculateV();
    }else{
        gl_FragColor=vec4(0,0,0,0);
    }
    //gl_FragColor=vec4(rPosX/uWidth,rPosY/uHeight,rPosX/uWidth,rPosY/uHeight);
}