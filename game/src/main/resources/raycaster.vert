
uniform mat4 transform;

attribute vec4 position;
attribute vec4 color;

varying vec3 fPosition;


void main() {
	gl_Position = transform * position;

	fPosition = vec3(gl_Position.x, gl_Position.x, gl_Position.y);
}
