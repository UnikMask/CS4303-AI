
package wolfdungeon3d;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;

public class Assets {
	private static Assets instance;

	// Textures
	private static final List<String> textureSrcs = Arrays.asList("bonewall.png", "dirt.png", "floor.png", "wall.png",
			"ceiling.jpg");
	public HashMap<String, PImage> texMap;

	// Sprites
	private static final List<String> spriteSrcs = Arrays.asList("sphere.png", "P_Red07.png", "W_Sword008.png",
			"l_Chest01.png", "l_Chest02.png", "E_Wood03.png", "Ac_Ring02.png");
	public HashMap<String, PImage> spriteMap;

	// Fonts
	private static final List<String> fontSrces = Arrays.asList("FFFFORWA.TTF");
	public HashMap<String, PFont> fontMap;

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

	public static PFont getFont(String fp) {
		if (instance.fontMap.containsKey(fp)) {
			return instance.fontMap.get(fp);
		} else {
			return null;
		}
	}

	public Assets(PApplet applet) {
		texMap = new HashMap<>();
		spriteMap = new HashMap<>();
		fontMap = new HashMap<>();
		for (String src : textureSrcs) {
			texMap.put(src, applet.loadImage(src));
		}
		for (String src : spriteSrcs) {
			spriteMap.put(src, applet.loadImage(src));
		}
		for (String src : fontSrces) {
			fontMap.put(src, applet.createFont(src, 128));
		}
	}
}
