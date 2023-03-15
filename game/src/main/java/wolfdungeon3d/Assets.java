
package wolfdungeon3d;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import processing.core.PApplet;
import processing.core.PImage;

public class Assets {
	private static Assets instance;

	// Textures
	private static final List<String> textureSrcs = Arrays.asList("bonewall.png", "dirt.png", "floor.png", "wall.png",
			"ceiling.jpg");
	public HashMap<String, PImage> texMap;

	// Sprites
	private static final List<String> spriteSrcs = Arrays.asList("sphere.png");
	public HashMap<String, PImage> spriteMap;

	public static void createInstance(PApplet applet) {
		instance = new Assets(applet);
	}

	public static PImage getTex(String fp) {
		if (instance.texMap.containsKey(fp)) {
			return instance.texMap.get(fp);
		} else {
			return null;
		}
	}

	public static PImage getSprite(String fp) {
		if (instance.spriteMap.containsKey(fp)) {
			return instance.spriteMap.get(fp);
		} else {
			return null;
		}
	}

	public Assets(PApplet applet) {
		texMap = new HashMap<>();
		spriteMap = new HashMap<>();
		for (String src : textureSrcs) {
			texMap.put(src, applet.loadImage(src));
		}
		for (String src : spriteSrcs) {
			spriteMap.put(src, applet.loadImage(src));
		}
	}
}
