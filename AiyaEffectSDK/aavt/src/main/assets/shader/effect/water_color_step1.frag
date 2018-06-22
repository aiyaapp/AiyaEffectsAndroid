precision mediump float;

uniform sampler2D uTexture;
uniform sampler2D uNoiseTexture;
uniform float uWidth;
uniform float uHeight;
varying vec2 vTextureCo;

vec4 quant(vec4 cl, float n) {
   cl.x = floor(cl.x * 255./n)*n/255.;  
   cl.y = floor(cl.y * 255./n)*n/255.;  
   cl.z = floor(cl.z * 255./n)*n/255.;  
     
   return cl;  
}
  
void main(void){
   vec4 noiseColor = texture2D(uNoiseTexture, vTextureCo);
   vec2 newUV = vec2(vTextureCo.x + noiseColor.x / uWidth, vTextureCo.y + noiseColor.y / uHeight);
   vec4 fColor = texture2D(uTexture, newUV);
    
   vec4 color = quant(fColor, 255./pow(2., 4.));
   //vec4 color = vec4(1., 1., .5, 1.);  
   gl_FragColor = color;  
}  