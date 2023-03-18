package wolfdungeon3d;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PMatrix;
import processing.core.PMatrix3D;
import processing.core.PShape;
import processing.core.PVector;
import processing.opengl.PShader;
import wolfdungeon3d.Game.GameState;

public class RaycastingRenderer {
	private final static float MAX_SPRITE_DRAW_DISTANCE = 18.0f;
	private PApplet applet;
	private PShape canvas;
	private int lastFloor;
	private PImage tex;
	private PGraphics depthg;
	private PShader raycastingShader;
	private PShader spriteShader;
	private PShader onTopShader;

	// Message Display
	private static final PVector MSG_POSITION = new PVector(0.025f, 0.925f);
	private static final int INIT_FRAMES_MESSAGE = 120;
	private ArrayDeque<String> messageQueue = new ArrayDeque<>();
	private HashMap<Sprite, PShape> spriteShapeMap = new HashMap<>();
	private int framesUntilNextMessage = 0;

	// Interaction display
	private static final String INTERACT_MSG = "Press <E> to ";

	public void draw(PGraphics g, Game game, PVector dir, PVector plane, String action) {
		if (canvas == null) {
			generateCanvas(g);
		}
		if (tex == null || game.getFloor() != lastFloor) {
			tex = game.getLevelImage(applet);
			lastFloor = game.getFloor();
			spriteShapeMap = new HashMap<>();
		}
		List<PImage> tileTextures = game.getLevel().getLevelTextures().stream().map((s) -> Assets.getTex(s))
				.collect(Collectors.toList());

		// Draw depth buffer
		if (depthg == null) {
			depthg = applet.createGraphics(g.width, g.height);
		}
		PVector pos = game.getPlayer().getPosition();

		// Draw main graphics window
		raycastingShader.set("pos", pos.x, pos.y, pos.z);
		raycastingShader.set("dir", dir.x, dir.y, 0);
		raycastingShader.set("plane", plane.x, plane.y, plane.z);
		raycastingShader.set("tile0", tileTextures.get(0));
		raycastingShader.set("tile1", tileTextures.get(1));
		raycastingShader.set("tile2", tileTextures.get(2));
		raycastingShader.set("tile3", tileTextures.get(3));
		raycastingShader.set("renderDistance", MAX_SPRITE_DRAW_DISTANCE);
		raycastingShader.set("screenSize", (float) g.width, (float) g.height);

		// Draw map
		g.background(0);
		canvas.setTexture(tex);
		g.shader(raycastingShader);
		g.shape(canvas, 0, 0);
		g.resetShader();

		// Draw entities
		g.shader(spriteShader);
		spriteShader.set("renderDistance", MAX_SPRITE_DRAW_DISTANCE);
		spriteShader.set("dimensions", (float) g.width, (float) g.height);
		spriteShader.set("cot", new PVector(plane.x, plane.y).mag() * (g.width / g.height) / dir.mag(),
				new PVector(plane.x, plane.y).mag() / dir.mag());
		List<Sprite> sprites = game.getSprites();
		sprites.sort(new Comparator<Sprite>() {
			public int compare(Sprite s1, Sprite s2) {
				return (int) ((PVector.dot(PVector.sub(s2.getPosition(), game.getPlayer().getPosition()),
						game.getPlayer().getPosition())
						- PVector.dot(PVector.sub(s1.getPosition(), game.getPlayer().getPosition()),
								game.getPlayer().getPosition()))
						* 24);

			}
		});
		for (Sprite s : game.getSprites()) {
			if (s == game.getPlayer()) {
				continue;
			} else if (!spriteShapeMap.containsKey(s)) {
				ArrayList<PVector> verts = new ArrayList<>(
						Arrays.asList(new PVector(0, 0), new PVector(0, 1), new PVector(1, 1), new PVector(1, 0)));
				PShape spriteShape = g.createShape();
				spriteShape.beginShape();
				for (PVector vt : verts) {
					PVector v = new PVector(vt.x * s.getSize().x, vt.y * s.getSize().y);
					v.sub(new PVector(s.getSize().x / 2, 0));
					spriteShape.vertex(v.x, v.y, vt.x, 1 - vt.y);
				}
				if (s.getImage() != null) {
					spriteShape.texture(s.getImage());
				}
				spriteShape.endShape(PConstants.CLOSE);
				spriteShapeMap.put(s, spriteShape);
			}
			PVector dist = PVector.sub(s.getPosition(), game.getPlayer().getPosition());
			float depth = PVector.dot(dist, PVector.div(dir, dir.mag()));

			PMatrix3D mat = new PMatrix3D(rotateY(game.getPlayer().getRotation()));
			mat.apply(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, dist.x, dist.z, dist.y, 1);
			mat.apply(rotateY(-game.getPlayer().getRotation()));
			if (depth < 0.1f) {
				continue;
			}
			spriteShader.set("modelMatrix", mat);
			spriteShader.set("depth", depth);
			g.shape(spriteShapeMap.get(s));
		}
		g.resetShader();

		// Draw messages
		displayMessages(g);
		if (action != null) {
			displayInteractionMessage(g, action);
		}
	}

	//////////////////////
	// Message Handling //
	//////////////////////

	public boolean hasMessages() {
		return !messageQueue.isEmpty();
	}

	public void addMessage(String msg) {
		if (messageQueue.isEmpty()) {
			framesUntilNextMessage = INIT_FRAMES_MESSAGE;
		}
		messageQueue.addLast(msg);
	}

	public void nextMessage() {
		if (!messageQueue.isEmpty()) {
			messageQueue.pollFirst();
			framesUntilNextMessage = INIT_FRAMES_MESSAGE;
		}
	}

	/////////////////////
	// Private Methods //
	/////////////////////

	private void displayMessages(PGraphics g) {
		if (messageQueue.isEmpty()) {
			return;
		}
		framesUntilNextMessage--;
		if (framesUntilNextMessage == 0) {
			messageQueue.pollFirst();
			framesUntilNextMessage = INIT_FRAMES_MESSAGE;
			if (messageQueue.isEmpty()) {
				return;
			}
		}
		onTopShader.set("usingTexture", true);
		g.shader(onTopShader);
		String msg = messageQueue.peekFirst();
		g.pushStyle();
		g.fill(g.color(255, ((float) framesUntilNextMessage / (float) INIT_FRAMES_MESSAGE) * 255));
		g.textFont(Assets.getFont("FFFFORWA.TTF"));
		g.textAlign(PConstants.LEFT, PConstants.CENTER);
		g.textSize(g.height / 20);
		g.text(msg, MSG_POSITION.x * g.width, MSG_POSITION.y * g.height, -0.01f);
		g.popStyle();
		g.resetShader();
	}

	private void displayInteractionMessage(PGraphics g, String action) {
		String msg = INTERACT_MSG + action;

		onTopShader.set("usingTexture", true);
		g.shader(onTopShader);
		g.pushStyle();
		g.fill(0xffffffff);
		g.textFont(Assets.getFont("FFFFORWA.TTF"));
		g.textAlign(PConstants.CENTER, PConstants.CENTER);
		g.textSize(g.height / 20);
		g.text(msg, 0.5f * g.width, 0.5f * g.height);
		g.popStyle();
		g.resetShader();
	}

	private PVector rotateY(PVector v, float theta) {
		return new PVector(v.x * (float) Math.cos(theta) + v.z * (float) Math.sin(theta), v.y,
				-v.x * (float) Math.sin(theta) + v.z * (float) Math.cos(theta));
	}

	private PMatrix3D rotateY(float theta) {
		return new PMatrix3D((float) Math.cos(theta), 0, (float) Math.sin(theta), 0, 0, 1, 0, 0,
				(float) -Math.sin(theta), 0, (float) Math.cos(theta), 0, 0, 0, 0, 1);
	}

	private void generateCanvas(PGraphics graphics) {
		canvas = graphics.createShape();
		canvas.beginShape();
		canvas.vertex(0, 0, 0);
		canvas.vertex(0, graphics.height, 0);
		canvas.vertex(graphics.width, graphics.height, 0);
		canvas.vertex(graphics.width, 0, 0);
		canvas.endShape(PConstants.CLOSE);
	}

	private PMatrix getFrustrum(PVector near, PVector far) {
		return new PMatrix3D(2 * near.z / (far.x - near.x), 0, 0, -near.z * (far.x + near.x) / (far.x - near.x), 0,
				2 * near.z / (far.y - near.y), 0, -near.z * (far.y + near.y) / (far.y - near.y), 0, 0,
				-(far.z + near.z) / (far.z - near.z), 2 * far.z * near.z / (near.z - far.z), 0, 0, 1, 0);
	}

	/**
	 * Constructor for a raycasting renderer.
	 *
	 * @param applet The applet to attach the renderer to and load the shaders from.
	 */
	public RaycastingRenderer(PApplet applet) {
		this.applet = applet;
		// raycastingShader = applet.loadShader("raycaster.frag", "raycaster.vert");
		raycastingShader = applet.loadShader("raycaster.frag");
		spriteShader = applet.loadShader("sprite.frag", "sprite.vert");
		onTopShader = applet.loadShader("onTop.frag");
	}
}
