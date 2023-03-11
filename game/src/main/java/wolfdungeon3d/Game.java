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

	public void keyPressed() {

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

	public void draw(PGraphics graphics) {
		if (state == GameState.EXPLORE) {
			PVector dir = PVector.mult(PVector.fromAngle(player.getRotation()), DIR_L);

			PVector plane = PVector.add(
					PVector.mult(PVector.fromAngle((float) (Math.PI / 2.0f) + player.getRotation()), PLANE_L),
					new PVector(0, 0, PLANE_H));
			renderer.draw(graphics, player.getPosition(), dir, plane, player.getRotation());
		}
	}

	public Game(PApplet applet) {
		setUp();
		renderer = new RaycastingRenderer(applet, lvl);
	}
}
