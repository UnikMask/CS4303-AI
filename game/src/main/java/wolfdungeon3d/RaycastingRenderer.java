package wolfdungeon3d;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PShape;
import processing.core.PVector;
import processing.opengl.PShader;

public class RaycastingRenderer {
	PShape canvas;
	PShader shader;
	PImage levelTex;

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
		System.out.println("Player position: " + pos);
		shader.set("pos", pos.x, pos.y, 0.25f);
		shader.set("dir", dir.x, dir.y, 0);
		shader.set("plane", plane.x, plane.y, plane.z);
		graphics.shader(shader);
		graphics.shape(canvas, 0, 0);
	}

	public RaycastingRenderer(PApplet applet, Level level) {
		shader = applet.loadShader("raycaster.frag", "raycaster.vert");
		levelTex = level.getGridImage(applet);
	}
}
