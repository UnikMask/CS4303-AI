package wolfdungeon3d;

import java.util.ArrayList;
import java.util.Arrays;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PShape;
import processing.core.PVector;
import processing.opengl.PShader;
import wolfdungeon3d.Level.Tile;

public class RaycastingRenderer {
	private final static float MAX_SPRITE_DRAW_DISTANCE = 24.0f;
	PApplet applet;
	PShape canvas;
	PGraphics depthg;
	PShader raycastingShader;
	PShader spriteShader;
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
		System.out.println("\n\n New render: Player at pos - " + game.getPlayer().getPosition());
		System.out.println("Player rotation: " + game.getPlayer().getRotation());
		System.out.println("Dir: " + dir);
		for (Sprite s : game.getSprites()) {
			if (s == game.getPlayer()) {
				continue;
			}
			ArrayList<PVector> verts = new ArrayList<>(Arrays.asList(new PVector(0, 0), new PVector(0, s.getSize().y),
					new PVector(s.getSize().x, s.getSize().y), new PVector(s.getSize().x, 0)));
			PVector dist = PVector.sub(game.getPlayer().getPosition(), s.getPosition());
			float theta = game.getPlayer().getRotation();
			PShape spriteShape = graphics.createShape();
			spriteShape.beginShape();
			float depth = -PVector.dot(dist, PVector.div(dir, dir.mag()));
			for (PVector v : verts) {
				v.sub(new PVector(s.getSize().x / 2, 0));
				v = rotateY(v, theta);
				v.add(new PVector(-dist.x, -0.35f, -dist.y));
				v = rotateY(v, -theta);
				depth = v.z;
				v = new PVector(v.x / (depth * graphics.width / graphics.height), v.y / (depth), 0);
				v = new PVector(v.x * graphics.width, -v.y * graphics.height);
				v.add(graphics.width / 2, graphics.height / 2);
				spriteShape.vertex(v.x, v.y);
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

	public RaycastingRenderer(PApplet applet, Level level) {
		this.applet = applet;
		// raycastingShader = applet.loadShader("raycaster.frag", "raycaster.vert");
		raycastingShader = applet.loadShader("raycaster.frag");
		spriteShader = applet.loadShader("sprite.frag");
		tileTextures = new PImage[3];
		tileTextures[0] = applet.loadImage(Tile.WALL.tex);
		tileTextures[1] = applet.loadImage(Tile.ROOM.tex);
		tileTextures[2] = applet.loadImage(Tile.CENTER.tex);
	}
}
