attribute vec4 aVertexCo;
attribute vec2 aTextureCo;
varying vec2 vTextureCo;
varying vec2 vBlurCoord1s[14];
uniform float uWidth;
uniform float uHeight;
uniform mat4 uVertexMatrix;
uniform mat4 uTextureMatrix;
void main()
{
    gl_Position = uVertexMatrix*aVertexCo;
    vTextureCo = (uTextureMatrix*vec4(aTextureCo,0,1)).xy;

    highp float mul_x = 2.0 / uWidth;
    highp float mul_y = 2.0 / uHeight;

    vBlurCoord1s[0] = vTextureCo + vec2( 0.0 * mul_x, -10.0 * mul_y );
    vBlurCoord1s[1] = vTextureCo + vec2( 8.0 * mul_x, -5.0 * mul_y );
    vBlurCoord1s[2] = vTextureCo + vec2( 8.0 * mul_x, 5.0 * mul_y );
    vBlurCoord1s[3] = aTextureCo + vec2( 0.0 * mul_x, 10.0 * mul_y );
    vBlurCoord1s[4] = aTextureCo + vec2( -8.0 * mul_x, 5.0 * mul_y );
    vBlurCoord1s[5] = aTextureCo + vec2( -8.0 * mul_x, -5.0 * mul_y );

    mul_x = 1.2 / uWidth;
    mul_y = 1.2 / uHeight;

    vBlurCoord1s[6] = aTextureCo + vec2( 0.0 * mul_x, -6.0 * mul_y );
    vBlurCoord1s[7] = aTextureCo + vec2( -4.0 * mul_x, -4.0 * mul_y );
    vBlurCoord1s[8] = aTextureCo + vec2( -6.0 * mul_x, 0.0 * mul_y );
    vBlurCoord1s[9] = aTextureCo + vec2( -4.0 * mul_x, 4.0 * mul_y );
    vBlurCoord1s[10] = aTextureCo + vec2( 0.0 * mul_x, 6.0 * mul_y );
    vBlurCoord1s[11] = aTextureCo + vec2( 4.0 * mul_x, 4.0 * mul_y );
    vBlurCoord1s[12] = aTextureCo + vec2( 6.0 * mul_x, 0.0 * mul_y );
    vBlurCoord1s[13] = aTextureCo + vec2( 4.0 * mul_x, -4.0 * mul_y );
}