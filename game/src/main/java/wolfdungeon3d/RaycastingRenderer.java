package wolfdungeon3d;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PShape;
import processing.core.PVector;
import processing.opengl.PShader;
import wolfdungeon3d.Level.Tile;

public class RaycastingRenderer {
	PShape canvas;
	PShader shader;
	PImage levelTex;
	PImage[] tileTextures;

	private void generateCanvas(PGraphics graphics) {
		canvas = graphics.createShape(PGraphics.RECT, 0, 0, graphics.width, graphics.height);
		canvas.setFill(graphics.color(255, 255, 255, 255));
		canvas.setTexture(levelTex);
	}

	public void setLevelImage(PImage tex) {
		this.levelTex = tex;
		this.canvas = null;

	}

	public void draw(PGraphics graphics, PVector pos, PVector dir, PVector plane, float rotation) {
		if (canvas == null) {
			generateCanvas(graphics);
		}
		graphics.background(0);
		shader.set("pos", pos.x, pos.y, 0.35f);
		shader.set("dir", dir.x, dir.y, 0);
		shader.set("plane", plane.x, plane.y, plane.z);
		shader.set("tile0", tileTextures[0]);
		shader.set("tile1", tileTextures[1]);
		shader.set("tile2", tileTextures[2]);

		graphics.shader(shader);
		graphics.shape(canvas, 0, 0);
	}

	public RaycastingRenderer(PApplet applet, Level level) {
		shader = applet.loadShader("raycaster.frag", "raycaster.vert");
		levelTex = level.getGridImage(applet);
		tileTextures = new PImage[3];
		tileTextures[0] = applet.loadImage(Tile.WALL.tex);
		tileTextures[1] = applet.loadImage(Tile.ROOM.tex);
		tileTextures[2] = applet.loadImage(Tile.CENTER.tex);
	}
}
