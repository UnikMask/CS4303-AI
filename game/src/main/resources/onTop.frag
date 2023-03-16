#ifdef GL_ES
precision mediump float
precision mediump int
#endif

#define PROCESSING_TEXTURE_SHADER;

uniform sampler2D texture;
uniform bool usingTexture;

varying vec4 vertTexCoord;
varying vec4 vertColor;

void main() {
	gl_FragDepth = 0;
	if (usingTexture) {
		gl_FragColor = texture2D(texture, vertTexCoord.st) * vertColor;
	} else {
		gl_FragColor = vertColor;
	}
}
