precision mediump float;

uniform sampler2D yTexture;
uniform sampler2D uTexture;
uniform sampler2D vTexture;

varying vec2 aCoordinate;

void main(){
    vec4 color = vec4((texture2D(yTexture, aCoordinate).r - 16.0/255.0) * 1.164);
    vec4 U = vec4(texture2D(uTexture, aCoordinate).r - 128.0/255.0);
    vec4 V = vec4(texture2D(vTexture, aCoordinate).r - 128.0/255.0);
    color += V * vec4(1.596, -0.813, 0, 0);
    color += U * vec4(0, -0.392, 2.017, 0);
    color.a = 1.0;
    gl_FragColor = color;
}