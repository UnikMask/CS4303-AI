package wolfdungeon3d;

import java.util.Date;
import java.util.Random;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

public class Game {
	private static final PVector BASE_FLOOR_SIZE = new PVector(25, 25);
	private static final PVector FLOOR_SIZE_INCREMENT = new PVector(2, 2);
	private static final float DIR_L = 0.2f;
	private static final float PLANE_L = 0.15f;
	private static final float PLANE_H = 0.1f;

	private GameState state = GameState.LOADING;
	private Entity player;
	private PlayerController controller;
	private RaycastingRenderer renderer;
	private Level lvl;
	private int floor = 0;

	static enum GameState {
		EXPLORE, BATTLE, LOADING, INTRO
	}

	public PVector getLevelSize(int floor) {
		return PVector.add(BASE_FLOOR_SIZE, PVector.mult(FLOOR_SIZE_INCREMENT, floor));
	}

	public GameState getState() {
		return state;
	}

	public void setUp() {
		lvl = Level.generate(getLevelSize(floor), 69 + new Random(floor).nextInt());
		player = new Entity(lvl.getStartPosition(), new Entity.Attributes(1, 1, 1, 1, 1, 1));
	}

	public void keyPressed(Character key) {
		controller.onKeyPressed(key);
	}

	public void keyReleased(Character key) {
		controller.onKeyReleased(key);
	}

	public void keyHeld(Character key) {
		controller.onKeyHeld(key);
	}

	public void update() {
		if (state == GameState.LOADING && lvl == null) {
			setUp();
		} else if (state == GameState.LOADING && lvl != null) {
			state = GameState.EXPLORE;
		} else if (state == GameState.EXPLORE) {
			player.setRotation(player.getRotation() + 0.01f);

		}
	}

	public PVector getPlane(PVector dir, PVector canvasDimensions, float fov) {
		fov = (float) ((fov % 2 * Math.PI) - Math.PI);
		if (fov >= Math.PI || fov <= 0) {
			return null;
		}
		float yRatio = canvasDimensions.y / canvasDimensions.x;
		float dist = PApplet.tan(fov / 2);
		PVector plane = dir.cross(new PVector(0, 0, dist));
		return PVector.add(plane, new PVector(0, 0, dir.mag() * dist * yRatio));
	}

	public void draw(PGraphics graphics) {
		if (state == GameState.EXPLORE) {
			PVector dir = PVector.mult(PVector.fromAngle(player.getRotation()), DIR_L);

			PVector plane = getPlane(dir, new PVector(graphics.width, graphics.height), (float) Math.PI / 2);
			renderer.draw(graphics, player.getPosition(), dir, plane, player.getRotation());
		}
	}

	public Game(PApplet applet) {
		setUp();
		renderer = new RaycastingRenderer(applet, lvl);
	}
}
