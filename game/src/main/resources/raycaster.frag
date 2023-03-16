#ifdef GL_ES
precision mediump float
precision mediump int
#endif

uniform vec3 pos;
uniform vec3 dir;
uniform vec3 plane;
uniform vec2 screenSize;
uniform float renderDistance;
uniform sampler2D texture;
uniform sampler2D tile0;
uniform sampler2D tile1;
uniform sampler2D tile2;
uniform sampler2D tile3;

void main() {
	vec3 coords = vec3(gl_FragCoord.xyz / vec3(screenSize.x, screenSize.y, 1)) * 2 - vec3(1, 1, 1);
	vec3 ray = normalize(dir + (vec3(coords.x, coords.x, coords.y) * plane));
	// Get ray delta distances.
	vec3 deltaDist = vec3(100, 100, 100);
	if (ray.x != 0) {
		deltaDist.x = abs(1.0 / ray.x);
	}
	if (ray.y != 0) {
		deltaDist.y = abs(1.0 / ray.y);
	}
	if (ray.z != 0) {
		deltaDist.z = abs(1.0 / ray.z);
	}

	ivec3 map = ivec3(int(pos.x), int(pos.y), 0);
	ivec3 tstep = ivec3(1, 1, 1);
	vec3 sideDist = (vec3(map) + vec3(1, 1, 1) - pos) * deltaDist;


	// Inverse steps on negative directions
	if (ray.x < 0) {
		tstep.x = -1;
		sideDist.x = (pos.x - map.x) * deltaDist.x;
	}
	if (ray.y < 0) {
		tstep.y = -1;
		sideDist.y = (pos.y - map.y) * deltaDist.y;
	}
	if (ray.z < 0) {
		tstep.z = -1;
		sideDist.z = (pos.z - map.z) * deltaDist.z;
	}

	bool hit = false;
	int side = 0;
	vec4 tile = vec4(0, 0, 0, 1);
	while (!hit) {
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
			side = tstep.z == 1? 3: 2;
			hit = tstep.z == 1? map.z > 2 : true;
		}
		tile = texelFetch(texture, ivec2(map.x, map.y), 0);
		if (tile.rgb == vec3(0,0,0)) { // Hit something that isn't space.
			hit = true;
		}
	}

	float dist;
	if (side == 0) {
		dist = sideDist.x - deltaDist.x;
	} else if (side == 1) {
		dist = sideDist.y - deltaDist.y;
	} else {
		dist = sideDist.z - deltaDist.z;
	}
	vec3 texPosp = fract(pos + dist * ray);
	vec2 texPos = side == 0? vec2(texPosp.y, 1 - texPosp.z):
		side == 1? vec2(texPosp.x, 1 - texPosp.z):
		vec2(texPosp.x, 1 - texPosp.y);
	float depth = dot(normalize(dir), ray) * dist;

	if (side == 0 || side == 1) {
		if (tile.rgb == vec3(1, 1, 1) || tile.rgb == vec3(0, 0, 0)) {
			gl_FragColor = texture2D(tile0, vec2(texPos.x, 1 - texPos.y));
		} else {
			gl_FragColor = tile;
		}
	} else if (tile.rgb == vec3(1, 0, 0)) {
		gl_FragColor = texture2D(tile3, vec2(texPos.x, 1 - texPos.y));
	} else if (side == 2) {
		gl_FragColor = texture2D(tile1, vec2(texPos.x, 1 - texPos.y));
	} else {
		gl_FragColor = texture2D(tile2, vec2(texPos.x, 1 - texPos.y));
	}

	vec4 light = vec4(1, 1, 0.6, 1);
	float intensity = ((24.0 - dist) / 24.0) * dot(normalize(dir), ray);
	gl_FragColor *= intensity * light;
	gl_FragDepth = depth / renderDistance;
}
