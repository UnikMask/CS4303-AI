package wolfdungeon3d;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PShape;
import processing.core.PVector;
import processing.opengl.PShader;
import wolfdungeon3d.Level.Tile;

public class RaycastingRenderer {
	PApplet applet;
	PShape canvas;
	PGraphics depthg;
	PShader raycastingShader;
	PImage[] tileTextures;

	private void generateCanvas(PGraphics graphics) {
		canvas = graphics.createShape(PGraphics.RECT, 0, 0, graphics.width, graphics.height);
		canvas.setFill(graphics.color(255, 255, 255, 255));
	}

	public void draw(PGraphics graphics, Level lvl, PVector pos, PVector dir, PVector plane, float rotation) {
		if (canvas == null) {
			generateCanvas(graphics);
		}
		canvas.setTexture(lvl.getGridImage(applet));

		// Draw depth buffer
		if (depthg == null) {
			depthg = applet.createGraphics(graphics.width, graphics.height);
		}

		// Draw main graphics window
		raycastingShader.set("pos", pos.x, pos.y, 0.35f);
		raycastingShader.set("dir", dir.x, dir.y, 0);
		raycastingShader.set("plane", plane.x, plane.y, plane.z);
		raycastingShader.set("tile0", tileTextures[0]);
		raycastingShader.set("tile1", tileTextures[1]);
		raycastingShader.set("tile2", tileTextures[2]);

		graphics.background(0);
		graphics.shader(raycastingShader);
		graphics.shape(canvas, 0, 0);

	}

	public RaycastingRenderer(PApplet applet, Level level) {
		this.applet = applet;
		raycastingShader = applet.loadShader("raycaster.frag", "raycaster.vert");
		tileTextures = new PImage[3];
		tileTextures[0] = applet.loadImage(Tile.WALL.tex);
		tileTextures[1] = applet.loadImage(Tile.ROOM.tex);
		tileTextures[2] = applet.loadImage(Tile.CENTER.tex);
	}
}
