#ifdef GL_ES
precision mediump float
precision mediump int
#endif

uniform vec3 pos;
uniform vec3 dir;
uniform vec3 plane;
uniform sampler2D texture;

varying vec3 fPosition;

void main() {
	// Get ray delta distances.
	vec3 nray = normalize(dir + (fPosition * plane));
	vec3 deltaDist = vec3(100, 100, 100);
	if (nray.x != 0) {
		deltaDist.x = abs(1.0 / nray.x);
	}
	if (nray.y != 0) {
		deltaDist.y = abs(1.0 / nray.y);
	}
	if (nray.z != 0) {
		deltaDist.z = abs(1.0 / nray.z);
	}

	ivec3 map = ivec3(int(pos.x), int(pos.y), 0);
	ivec3 tstep = ivec3(1, 1, 1);

	vec3 sideDist = (vec3(map) + vec3(1, 1, 1) - pos) * deltaDist;


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
	int step = 0;
	while (!hit && step < 20) {
		if (sideDist.x < sideDist.y && sideDist.x < sideDist.z) {
			sideDist.x += deltaDist.x;
			map.x += tstep.x;
			side = 0;
		} else if (sideDist.y < sideDist.x && sideDist.y < sideDist.z) {
			sideDist.y += deltaDist.y;
			map.y += tstep.y;
			side = 1;
		} else {
			sideDist.z += deltaDist.z;
			map.z += tstep.z;
			side = 2;
			hit = true;
		}
		vec4 tile = texelFetch(texture, ivec2(map.x, map.y), 0);
		if (tile.xyz == vec3(0, 0, 0)) {
			hit = true;
		}
		step += 1;
	}
	if (step >= 20) {
		side = 2;
	}

	float intensity;
	if (side == 0) {
		intensity = (12.0 - sideDist.x) / 12;
	} else if (side == 1) {
		intensity = (12.0 - sideDist.y) / 12;
	} else {
		intensity = (12.0 - sideDist.z) / 12;
	}

	vec4 light = vec4(1, 0.5, 0.2, 1);
	if (side == 0) {
		gl_FragColor = vec4(0, 0, 1, 1) * intensity * light;
	} else if (side == 1) {
		gl_FragColor = vec4(0, 0, 0.8, 1) * intensity * light;
	} else if (side == 2) {
		gl_FragColor = vec4(0.3, 0.3, 0.3, 1) * intensity * light;
	}
}
