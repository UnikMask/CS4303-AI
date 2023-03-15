package wolfdungeon3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PShape;
import processing.core.PVector;
import processing.opengl.PShader;

public class RaycastingRenderer {
	private final static float MAX_SPRITE_DRAW_DISTANCE = 24.0f;
	PApplet applet;
	PShape canvas;
	PGraphics depthg;
	PShader raycastingShader;
	PShader spriteShader;

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
		List<PImage> tileTextures = game.getLevel().getLevelTextures().stream().map((s) -> Assets.getTex(s))
				.collect(Collectors.toList());

		// Draw depth buffer
		if (depthg == null) {
			depthg = applet.createGraphics(graphics.width, graphics.height);
		}
		PVector pos = game.getPlayer().getPosition();

		// Draw main graphics window
		raycastingShader.set("pos", pos.x, pos.y, pos.z);
		raycastingShader.set("dir", dir.x, dir.y, 0);
		raycastingShader.set("plane", plane.x, plane.y, plane.z);
		raycastingShader.set("tile0", tileTextures.get(0));
		raycastingShader.set("tile1", tileTextures.get(1));
		raycastingShader.set("tile2", tileTextures.get(2));
		raycastingShader.set("renderDistance", MAX_SPRITE_DRAW_DISTANCE);
		raycastingShader.set("screenSize", (float) graphics.width, (float) graphics.height);

		// Draw map
		graphics.background(0);

		canvas.setTexture(game.getLevelImage(applet));
		graphics.shader(raycastingShader);
		graphics.shape(canvas, 0, 0);

		graphics.resetShader();

		// Draw entities
		graphics.shader(spriteShader);
		spriteShader.set("renderDistance", MAX_SPRITE_DRAW_DISTANCE);
		for (Sprite s : game.getSprites()) {
			if (s == game.getPlayer()) {
				continue;
			}
			ArrayList<PVector> verts = new ArrayList<>(
					Arrays.asList(new PVector(0, 0), new PVector(0, 1), new PVector(1, 1), new PVector(1, 0)));
			PVector dist = PVector.sub(s.getPosition(), game.getPlayer().getPosition());
			float theta = game.getPlayer().getRotation();
			PShape spriteShape = graphics.createShape();
			spriteShape.beginShape();
			float depth = -PVector.dot(dist, PVector.div(dir, dir.mag()));
			float fovCot = new PVector(plane.x, plane.y).mag() / dir.mag();
			for (PVector vt : verts) {
				PVector v = new PVector(vt.x * s.getSize().x, vt.y * s.getSize().y);
				v.sub(new PVector(s.getSize().x / 2, 0));
				v = rotateY(v, theta);
				v.add(new PVector(dist.x, dist.z, dist.y));
				v = rotateY(v, -theta);
				depth = v.z;
				v = new PVector(v.x / (fovCot * depth * graphics.width / graphics.height), v.y / (fovCot * depth), 0);
				v = new PVector(v.x * graphics.width, -v.y * graphics.height);
				v.add(graphics.width / 2, graphics.height / 2);
				spriteShape.vertex(v.x, v.y, vt.x, vt.y);
			}
			if (s.getImage() != null) {
				spriteShape.texture(s.getImage());
			}
			spriteShape.endShape(PConstants.CLOSE);
			spriteShader.set("depth", depth);

			if (depth < 0.1f) {
				continue;
			}

			graphics.shape(spriteShape);
		}
	}

	private PVector rotateY(PVector v, float theta) {
		return new PVector((float) (Math.cos(-theta) * v.x + Math.sin(-theta) * v.z), v.y,
				(float) (Math.cos(-theta) * v.z - Math.sin(-theta) * v.x));
	}

	public RaycastingRenderer(PApplet applet) {
		this.applet = applet;
		// raycastingShader = applet.loadShader("raycaster.frag", "raycaster.vert");
		raycastingShader = applet.loadShader("raycaster.frag");
		spriteShader = applet.loadShader("sprite.frag");
	}
}
