precision highp float;

uniform sampler2D uTexture;
uniform float uWidth;
uniform float uHeight;
varying vec2 vTextureCo;
const float step=1.;
const mat3 GX=mat3(-1.,0., +1., -1., 0., +1., -1., 0., +1.);
const mat3 GY=mat3(-1., -1., -1., 0., 0., 0., +1., +1., +1.);

//sobel 算子有两个滤波矩阵Gx和Gy，注意：
//边缘检测时Gx为检测纵向边缘，Gy为检测横向边缘
//计算法线时Gx为计算法线的横向偏移，Gy为计算法线的纵向偏移
//Gx为[-1 0 +1 -2 0 +2 -1 0 +1] 3*3矩阵
//Gy为[-1 -2 -1 0 0 0 +1 +2 +1] 3*3矩阵

float colorR(vec2 center,float shiftX,float shiftY){
    return texture2D(uTexture,vec2(vTextureCo.x+shiftX/uWidth,vTextureCo.y+shiftY/uHeight)).r;
}

void main(){
    vec2 center=vec2(vTextureCo.x*uWidth,vTextureCo.y*uHeight);
    float leftTop=colorR(center,-step,-step);
    float centerTop=colorR(center,0.,-step);
    float rightTop=colorR(center,step,-step);
    float leftCenter=colorR(center,-step,0.);
    float rightCenter=colorR(center,step,0.);
    float leftBottom=colorR(center,-step,step);
    float centerBottom=colorR(center,0.,step);
    float rightBottom=colorR(center,step,step);
    mat3 d=mat3(colorR(center,-step,-step),colorR(center,0.,-step),colorR(center,step,-step),
                 colorR(center,-step,0.),colorR(center,0.,0.),colorR(center,step,0.),
                 colorR(center,-step,step),colorR(center,0.,step),colorR(center,step,step));
    float x = d[0][0]*GX[0][0]+d[1][0]*GX[1][0]+d[2][0]*GX[2][0]+
               d[0][1]*GX[0][1]+d[1][1]*GX[1][1]+d[2][1]*GX[2][1]+
               d[0][2]*GX[0][2]+d[1][2]*GX[1][2]+d[2][2]*GX[2][2];
    float y = d[0][0]*GY[0][0]+d[1][0]*GY[1][0]+d[2][0]*GY[2][0]+
               d[0][1]*GY[0][1]+d[1][1]*GY[1][1]+d[2][1]*GY[2][1]+
               d[0][2]*GY[0][2]+d[1][2]*GY[1][2]+d[2][2]*GY[2][2];
    gl_FragColor=vec4(vec3(1.)-vec3(length(vec2(x,y))),1.);
}