package wolfdungeon3d;

import java.util.Date;
import java.util.Random;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

public class Game {
	private static final PVector BASE_FLOOR_SIZE = new PVector(25, 25);
	private static final PVector FLOOR_SIZE_INCREMENT = new PVector(2, 2);
	private static final PVector BASE_DIR = new PVector(0.2f, 0, 0);
	private static final PVector BASE_PLANE = new PVector(0, -0.2f, 0.125f);

	private GameState state;
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
		lvl = Level.generate(getLevelSize(floor), new Date().getTime() + new Random(floor).nextInt());
		player = new Entity(lvl.getStartPosition(), new Entity.Attributes(1, 1, 1, 1, 1, 1));
	}

	public void keyPressed() {

	}

	public void update() {
		if (state == GameState.LOADING && lvl == null) {
			setUp();
		} else if (state == GameState.EXPLORE) {
			player.setRotation(player.getRotation() + 0.1f);
		}
	}

	public void draw(PGraphics graphics) {
		renderer.draw(graphics, player.getPosition(), BASE_DIR, BASE_PLANE, player.getRotation());
	}

	public Game(PApplet applet) {
		setUp();
		renderer = new RaycastingRenderer(applet, lvl);
	}
}
