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
	vec4 tex = texture2D(texture, vertTexCoord.st);
	vec3 texLight = tex.rgb  * (renderDistance - depth) / renderDistance;
	gl_FragColor = vec4(texLight.rgb, tex.a) ;
}
