uniform mat4 transformMatrix;
uniform mat4 modelMatrix;
uniform mat4 texMatrix;
uniform float depth;
uniform vec2 dimensions;
uniform vec2 cot;

attribute vec4 position;
attribute vec4 color;
attribute vec2 texCoord;

varying vec4 vertColor;
varying vec4 vertTexCoord;

void main() {
	vec4 nposition = modelMatrix * position;
	nposition.x *= dimensions.x / (depth * cot.x);
	nposition.y *= -dimensions.y / (depth * cot.y);
	nposition.x += dimensions.x / 2;
	nposition.y += dimensions.y / 2;

	gl_Position = transformMatrix * nposition;

	vertColor = color;
	vertTexCoord = texMatrix * vec4(texCoord, 1.0, 1.0);
}
