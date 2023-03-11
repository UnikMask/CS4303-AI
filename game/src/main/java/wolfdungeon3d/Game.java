package wolfdungeon3d;

import java.util.Date;
import java.util.Random;

import processing.core.PVector;

public class Game {
	private static final PVector BASE_FLOOR_SIZE = new PVector(25, 25);
	private static final PVector FLOOR_SIZE_INCREMENT = new PVector(2, 2);
	private GameState state;
	private Level lvl;
	private int floor = 0;

	static enum GameState {
		EXPLORE, BATTLE, LOADING, INTRO
	}

	public PVector getLevelSize(int floor) {
		return PVector.add(BASE_FLOOR_SIZE, PVector.mult(FLOOR_SIZE_INCREMENT, floor));
	}

	public void setUp() {
		lvl = Level.generate(getLevelSize(floor), new Date().getTime() + new Random(floor).nextInt());
	}

	public void update() {
		if (state == GameState.LOADING && lvl == null) {
			setUp();
		}
	}
}
