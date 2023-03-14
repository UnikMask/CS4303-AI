package wolfdungeon3d;

import processing.core.PApplet;
import processing.core.PConstants;
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
		canvas = graphics.createShape();

		canvas.beginShape();
		canvas.vertex(0, 0, 0);
		canvas.vertex(0, graphics.height, 0);
		canvas.vertex(graphics.width, graphics.height, 0);
		canvas.vertex(graphics.width, 0, 0);
		canvas.endShape(PConstants.CLOSE);
	}

	public void draw(PGraphics graphics, Game game, PVector dir, PVector plane) {
		if (canvas == null) {
			generateCanvas(graphics);
		}
		canvas.setTexture(game.getLevelImage(applet));

		// Draw depth buffer
		if (depthg == null) {
			depthg = applet.createGraphics(graphics.width, graphics.height);
		}
		PVector pos = game.getPlayer().getPosition();

		// Draw main graphics window
		raycastingShader.set("pos", pos.x, pos.y, 0.35f);
		raycastingShader.set("dir", dir.x, dir.y, 0);
		raycastingShader.set("plane", plane.x, plane.y, plane.z);
		raycastingShader.set("tile0", tileTextures[0]);
		raycastingShader.set("tile1", tileTextures[1]);
		raycastingShader.set("tile2", tileTextures[2]);
		raycastingShader.set("screenSize", (float) graphics.width, (float) graphics.height);

		graphics.background(0);
		graphics.shader(raycastingShader);
		graphics.shape(canvas, 0, 0);

		graphics.resetShader();

		graphics.pushMatrix();
		graphics.translate(0, 0, 0.5f);
		PShape newShape = graphics.createShape();

		newShape.beginShape();
		newShape.vertex(0, 0, 0);
		newShape.vertex(0, graphics.height, 0);
		newShape.vertex(graphics.width, graphics.height, 0);
		newShape.vertex(graphics.width, 0, 0);
		newShape.fill(graphics.color(255, 128, 0, 255));
		newShape.endShape(PConstants.CLOSE);
		graphics.shape(canvas, 0, 0);
		graphics.popMatrix();

	}

	public RaycastingRenderer(PApplet applet, Level level) {
		this.applet = applet;
		// raycastingShader = applet.loadShader("raycaster.frag", "raycaster.vert");
		raycastingShader = applet.loadShader("raycaster.frag");
		tileTextures = new PImage[3];
		tileTextures[0] = applet.loadImage(Tile.WALL.tex);
		tileTextures[1] = applet.loadImage(Tile.ROOM.tex);
		tileTextures[2] = applet.loadImage(Tile.CENTER.tex);
	}
}
