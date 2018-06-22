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

vec2 cPos(float t,float shiftx,float shifty){
    vec2 pos=vec2(uWidth*vTextureCo.x,uHeight*(vTextureCo-shifty));
    return vec2(mod(pos.x*shiftx,uWidth),(pos.y*shiftx+floor(pos.x*shiftx/uWidth))*t);
}

//Y分量的计算
vec4 calculateY(){
    //填充点对应图片的位置
    float posX=floor(uWidth*vTextureCo.x);
    float posY=floor(uHeight*vTextureCo.y);
    //实际采样起始点对应图片的位置
    float rPosX=mod(posX*4.,uWidth);
    float rPosY=posY*4.+floor(posX*4./uWidth);
    vec4 oColor=vec4(0);
    float textureYPos=rPosY/uHeight;
    oColor[0]=cY(rPosX/uWidth,textureYPos);
    oColor[1]=cY((rPosX+1.)/uWidth,textureYPos);
    oColor[2]=cY((rPosX+2.)/uWidth,textureYPos);
    oColor[3]=cY((rPosX+3.)/uWidth,textureYPos);
    return oColor;
}

//UV的计算，YUV420SP用，test
vec4 calculateUV(){
    float posX=floor(uWidth*vTextureCo.x);
    float posY=floor(uHeight*(vTextureCo.y-0.2500));
    //实际采样起始点对应图片的位置
    float rPosX=mod(posX*4.,uWidth);
    float rPosY=posY*8.+floor(posX*4./uWidth)*2.;
    vec4 oColor=vec4(0);
    oColor[0]= cU((rPosX+1.)/uWidth,(rPosY+1.)/uHeight);
    oColor[1]= cV((rPosX+1.)/uWidth,(rPosY+1.)/uHeight);
    oColor[2]= cU((rPosX+3.)/uWidth,(rPosY+1.)/uHeight);
    oColor[3]= cV((rPosX+3.)/uWidth,(rPosY+1.)/uHeight);
    return oColor;
}

void main() {
    if(vTextureCo.y<0.2500){
        gl_FragColor=calculateY();
    }else if(vTextureCo.y<0.3750){
        gl_FragColor=calculateUV();
    }else{
        gl_FragColor=vec4(0,0,0,0);
    }
}