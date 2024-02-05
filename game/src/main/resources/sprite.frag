#ifdef GL_ES
precision mediump float
precision mediump int
#endif

uniform float depth;
uniform float renderDistance;
uniform sampler2D texture;

varying vec4 vertTexCoord;

const int ditherMatrix[16] = int[](0,  8,  2,  10,
                                  12, 4,  14, 6,
                                  3,  11, 1,  9,
                                  15, 7,  13, 5);
const float ditherq = 1.0f;

vec4 dither(vec4 color) {
    float offset = ditherMatrix[int(mod(gl_FragCoord.x, 4)) + int(mod(gl_FragCoord.y, 4)) * 4] / 16.0;
    color += offset * ditherq;
    color -= mod(color, vec4(ditherq));
    return color;
}

void main() {
	gl_FragDepth = depth / renderDistance;
	vec4 tex = texelFetch(texture, ivec2(textureSize(texture, 0) * vertTexCoord.st), 0);
	vec3 texLight = tex.rgb  * pow((renderDistance - depth) / renderDistance, 2);
    float ditherq = 1.0f/8;
	gl_FragColor = dither(vec4(texLight.rgb, tex.a));
}
