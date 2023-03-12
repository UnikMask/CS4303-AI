package wolfdungeon3d;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

public class Game {
	private static final PVector BASE_FLOOR_SIZE = new PVector(25, 25);
	private static final PVector FLOOR_SIZE_INCREMENT = new PVector(2, 2);
	private static final float DIR_L = 0.2f;
	private static final float MAX_V = 3.0f;
	private static final PVector ENTITY_AABB = new PVector(0.25f, 0.25f);

	private GameState state = GameState.LOADING;
	private Entity player;
	private HashSet<Entity> entities = new HashSet<>();
	private HashSet<EntityController> controllers = new HashSet<>();

	private PlayerController controller;
	private RaycastingRenderer renderer;
	private Level lvl;
	private int floor = 10;
	private long lastFrameTime = 0;

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
		controller = new PlayerController(player, new InputSettings());
		controllers.add(controller);
		entities.add(player);
	}

	////////////////////
	// Input Handling //
	////////////////////

	public void keyPressed(Character key) {
		controller.onKeyPressed(key);
		for (EntityController c : controllers) {
			c.onKeyPressed(key);
		}
	}

	public void keyReleased(Character key) {
		controller.onKeyReleased(key);
		for (EntityController c : controllers) {
			c.onKeyReleased(key);
		}
	}

	public void keyHeld(Character key) {
		controller.onKeyHeld(key);
		for (EntityController c : controllers) {
			c.onKeyHeld(key);
		}
	}

	public void mouseMoved(PVector mvt) {
		controller.onMouseMove(mvt);
		for (EntityController c : controllers) {
			c.onMouseMove(mvt);
		}
	}

	public void update() {
		if (lastFrameTime == 0) {
			lastFrameTime = new Date().getTime();
		}
		double deltaT = ((float) (lastFrameTime - new Date().getTime())) / 1000.0;
		lastFrameTime = new Date().getTime();

		if (state == GameState.LOADING && lvl == null) {
			setUp();
		} else if (state == GameState.LOADING && lvl != null) {
			state = GameState.EXPLORE;
		} else if (state == GameState.EXPLORE) {
			for (EntityController c : controllers) {
				c.update();
			}

			for (Entity e : entities) {
				if (e.getVelocity().mag() > MAX_V) {
					e.setVelocity(PVector.mult(e.getVelocity().normalize(), MAX_V));
				}
				e.setVelocity(PVector.mult(e.getVelocity(), 0.8f));
				e.setPosition(PVector.add(e.getPosition(), PVector.mult(e.getVelocity(), (float) deltaT)));
			}
		}
	}

	public void correctCollisions(Entity e) {
		PVector minBounds = PVector.sub(e.getPosition(), ENTITY_AABB);
		PVector maxBounds = PVector.add(e.getPosition(), ENTITY_AABB);

		IntTuple pos = new IntTuple(e.getPosition());
		List<PVector> corners = Arrays.asList(minBounds, new PVector(minBounds.x, maxBounds.y),
				new PVector(maxBounds.x, minBounds.y), maxBounds);

		PVector push = new PVector();
		for (PVector corner : corners) {
			IntTuple cornerPos = new IntTuple(corner);
		}
	}

	///////////////
	// Rendering //
	///////////////

	public void draw(PGraphics graphics) {
		if (state == GameState.EXPLORE) {
			PVector dir = PVector.mult(PVector.fromAngle((float) Math.PI / 2 + player.getRotation()), DIR_L);

			PVector plane = getPlane(dir, new PVector(graphics.width, graphics.height), (float) Math.PI / 2);
			renderer.draw(graphics, player.getPosition(), dir, plane, player.getRotation());
		}
	}

	private PVector getPlane(PVector dir, PVector canvasDimensions, float fov) {
		fov = (float) ((fov % 2 * Math.PI) - Math.PI);
		if (fov >= Math.PI || fov <= 0) {
			return null;
		}
		float yRatio = canvasDimensions.y / canvasDimensions.x;
		float dist = PApplet.tan(fov / 2);
		PVector plane = dir.cross(new PVector(0, 0, dist));
		return PVector.add(plane, new PVector(0, 0, dir.mag() * dist * yRatio));
	}

	//////////////////
	// Constructors //
	//////////////////

	public Game(PApplet applet) {
		setUp();
		renderer = new RaycastingRenderer(applet, lvl);
	}
}
