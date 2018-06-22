precision highp float;

void main()
{
	float near = 0.1;
	float far = 1000.0;
	float depth = gl_FragCoord.z;
	float lineDistance = 2.0*near*far / (far + near - (2.0*depth-1.0)*(far-near) );
	float alpha = 1.0 - clamp(lineDistance / 900.0, 0.0, 1.0);
	gl_FragColor = vec4( vec3(1.0), alpha );
}
