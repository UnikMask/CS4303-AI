
uniform vec3 pos;
uniform vec3 dir;
uniform vec3 plane;
uniform float rotation;
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
	vec3 rdir = rotateZ(dir, rotation);
	vec3 rplane = rotateZ(plane, rotation);
	ray = rdir + vec3(gl_Position.x * rplane.x, gl_Position.x * rplane.y, gl_Position.y * rplane.z);
}

vec3 rotateZ(vec3 v, float radians) {
	mat3 rz = mat3(cos(radians), -sin(radians), 0,
			   sin(radians), cos(radians), 0,
			   0, 0, 1);
	return rz * v;
}
