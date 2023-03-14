package wolfdungeon3d;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;
import wolfdungeon3d.Level.Tile;

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

	/////////////////////////
	// Getters and Setters //
	/////////////////////////

	public PVector getLevelSize(int floor) {
		return PVector.add(BASE_FLOOR_SIZE, PVector.mult(FLOOR_SIZE_INCREMENT, floor));
	}

	public GameState getState() {
		return state;
	}

	public Entity getPlayer() {
		return player;
	}

	///////////////
	// Rendering //
	///////////////

	public void setUp() {
		lvl = Level.generate(getLevelSize(floor), new Date().getTime() + new Random(floor).nextInt());
		entities = new HashSet<>(lvl.getEntities().stream().map((b) -> b.e).collect(Collectors.toSet()));

		player = new Entity(lvl.getStartPosition(), new Entity.Attributes(1, 1, 1, 1, 1, 1));
		controller = new PlayerController(player, new InputSettings());
		controllers.add(controller);
		entities.add(player);
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
				if (e == player) {
					System.out.println("Player moved!");
				}
				correctCollisions(e);
			}
		}
	}

	public PImage getLevelImage(PApplet applet) {
		return lvl.getGridImage(applet);
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

	private void correctCollisions(Entity e) {
		PVector minBounds = PVector.sub(e.getPosition(), ENTITY_AABB);
		PVector maxBounds = PVector.add(e.getPosition(), ENTITY_AABB);

		List<PVector> corners = Arrays.asList(minBounds, new PVector(minBounds.x, maxBounds.y), maxBounds,
				new PVector(maxBounds.x, minBounds.y));
		List<PVector> normals = Arrays.asList(new PVector(0, 1), new PVector(1, 0), new PVector(0, -1),
				new PVector(-1, 0));

		for (int i = 0; i < corners.size(); i++) {
			IntTuple cornerTilePos = new IntTuple(corners.get(i));

			// Collision happened on corner
			if (lvl.getTile(cornerTilePos.a, cornerTilePos.b) != Tile.ROOM) {
				PVector nx = Math.abs(normals.get(i).x) > 0.1f ? normals.get(i) : normals.get((i + 1) % corners.size());
				PVector ny = Math.abs(normals.get(i).y) > 0.1f ? normals.get(i) : normals.get((i + 1) % corners.size());

				float pushX = nx.x > 0 ? 1 - (corners.get(i).x % 1.0f) : -corners.get(i).x % 1.0f;
				float pushY = nx.y > 0 ? 1 - (corners.get(i).y % 1.0f) : -corners.get(i).y % 1.0f;
				ArrayDeque<PVector> s = new ArrayDeque<>(Arrays.asList(nx, ny));
				if (Math.abs(pushY) < Math.abs(pushX)) {
					s = new ArrayDeque<>(Arrays.asList(ny, nx));
				}

				boolean resolved = false;
				while (!s.isEmpty() && !resolved) {
					PVector n = s.pop();
					IntTuple nextTilePos = IntTuple.add(cornerTilePos, new IntTuple(n));
					if (lvl.getTile(nextTilePos.a, nextTilePos.b) == Tile.ROOM) {
						float j = PVector.dot(n, e.getVelocity());
						e.setVelocity(PVector.add(e.getVelocity(), PVector.mult(n, -(j + 1))));
						resolved = true;
					}
				}
				if (!resolved) {
					PVector n = PVector.add(nx, ny);
					float j = PVector.dot(n, e.getVelocity());
					e.setVelocity(PVector.add(e.getVelocity(), PVector.mult(n, -(j + 1))));
				}
			}
		}
	}

	///////////////
	// Rendering //
	///////////////

	public void draw(PGraphics graphics) {
		if (state == GameState.EXPLORE) {
			PVector dir = PVector.mult(PVector.fromAngle((float) Math.PI / 2 + player.getRotation()), DIR_L);
			PVector plane = getPlane(dir, new PVector(graphics.width, graphics.height), (float) (Math.PI / 1.7f));
			if (plane != null) {
				renderer.draw(graphics, this, dir, plane);
			}
		}
	}

	private PVector getPlane(PVector dir, PVector canvasDimensions, float fov) {
		fov = (float) (fov % (2 * Math.PI));
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
