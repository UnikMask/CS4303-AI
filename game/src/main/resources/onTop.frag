#ifdef GL_ES
precision mediump float
precision mediump int
#endif

uniform sampler2D texture;

varying vec4 vertTexCoord;
varying vec4 vertColor;

void main() {
	gl_FragDepth = 0;
	gl_FragColor = texture2D(texture, vertTexCoord.st) * vertColor;
}
