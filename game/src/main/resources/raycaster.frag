#ifdef GL_ES
precision mediump float
precision mediump int
#endif


uniform vec3 pos;
uniform vec2 dir;
uniform sampler2D texture;

varying vec4 vertCoords;
varying vec4 vertColor;
varying vec3 ray;

void main() {
	vec2 tcoords = (vertCoords.st + vec2(1,1)) / 2;

	// Get ray delta distances.
	vec3 nray = normalize(ray);
	vec3 deltaDist = vec3(10000, 10000, 10000);
	if (nray.x != 0) {
		deltaDist.x = abs(1 / nray.x);
	}
	if (nray.y != 0) {
		deltaDist.y = abs(1 / nray.y);
	}
	if (nray.z != 0) {
		deltaDist.z = abs(1 / nray.z);
	}

	ivec3 tstep = ivec3(1, 1, 1);

	ivec3 map = ivec3(int(pos.x), int(pos.y), int(pos.z));
	vec3 sideDist = map + ivec3(1, 1, 1) - pos;
	sideDist.x *= deltaDist.x;
	sideDist.y *= deltaDist.y;
	sideDist.z *= deltaDist.z;


	// Inverse steps on negative directions
	if (nray.x < 0) {
		tstep.x = -1;
		sideDist.x = (pos.x - map.x) * deltaDist.x;
	}
	if (nray.y < 0) {
		tstep.y = -1;
		sideDist.y = (pos.y - map.y) * deltaDist.y;
	}
	if (nray.z < 0) {
		tstep.z = -1;
		sideDist.z = (pos.z - map.z) * deltaDist.z;
	}

	bool hit = false;
	int side = 0;
	while (!hit) {
		if (sideDist.x < sideDist.y && sideDist.x < sideDist.z) {
			sideDist.x += deltaDist.x;
			map.x += tstep.x;
			side = 0;
		} else if (sideDist.y < sideDist.x && sideDist.y < sideDist.z) {
			sideDist.y += deltaDist.y;
			map.y += tstep.y;
			side = 1;
		} else if (sideDist.z < sideDist.x && sideDist.z < sideDist.y) {
			sideDist.z += deltaDist.z;
			map.z += tstep.z;
			side = 2;
		}
		vec4 tile = texelFetch(texture, map.xy, 0);
		if (tile.rgb == vec3(0, 0, 0) || side == 2) {
			hit = true;
		}
	}
	if (side == 0) {
		gl_FragColor = vec4(0, 0, 1, 1);
	} else if (side == 1) {
		gl_FragColor = vec4(0, 0, 0.8, 1);
	} else if (side == 2) {
		gl_FragColor = vec4(0.3, 0.3, 0.3, 1);
	}
}
