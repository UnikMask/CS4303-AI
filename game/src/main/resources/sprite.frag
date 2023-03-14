#ifdef GL_ES
precision mediump float
precision mediump int
#endif

uniform float depth;
uniform float renderDistance;
uniform sampler2D texture;

varying vec4 vertTexCoord;

void main() {
	gl_FragDepth = depth / renderDistance;
	gl_FragColor = texture2D(texture, vertTexCoord.st) * (renderDistance - depth) / renderDistance;
}
