
uniform vec3 pos;
uniform vec2 dir;
uniform vec3 plane;
uniform mat4 transform;

attribute vec4 position;
attribute vec4 color;

varying vec4 vertCoords;
varying vec4 vertColor;
varying vec3 ray;

void main() {
	gl_Position = transform * position;

	vertCoords = gl_Position;
	vertColor = color;
	ray =  vec3(dir, 0) + vec3(gl_Position.x * plane.x, gl_Position.x * plane.y, gl_Position.y * plane.z);
}
