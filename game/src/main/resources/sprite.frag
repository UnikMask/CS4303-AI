#ifdef GL_ES
precision mediump float
precision mediump int
#endif

uniform float depth;
uniform float renderDistance;
//uniform sampler2D texture;

//varying vec2 verTexCoord;

void main() {
	gl_FragDepth = depth / renderDistance;
	// gl_FragColor = sampler2D(texture, verTexCoord.st);
	gl_FragColor = vec4(1, 1, 1, 1);
}
