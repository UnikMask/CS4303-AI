package wolfdungeon3d;

import com.jogamp.newt.opengl.GLWindow;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;
import wolfdungeon3d.Level.Tile;

public class Game {
	private static final PVector BASE_FLOOR_SIZE = new PVector(25, 25);
	private static final PVector FLOOR_SIZE_INCREMENT = new PVector(2, 2);
	private static final float DIR_L = 0.2f;
	private static final float MAX_V = 3.0f;
	private static final int HUD_BG = 0xff2e2a2b;
	private static final int HUD_FG = 0xffedd49f;
	private static final int HUD_STROKE_C = 0xff584c4c;
	private static final float HUD_STROKE_S = 0.025f;

	private GLWindow nativew;
	private PApplet applet;
	private GameState state = GameState.LOADING;
	private Entity player;
	private HashSet<Entity> entities = new HashSet<>();
	private HashSet<EntityController> controllers = new HashSet<>();

	private PlayerController controller;
	private RaycastingRenderer renderer;
	private int score = 0;
	private Level lvl;
	private int floor = 0;
	private long lastFrameTime = 0;

	// Mouse handling
	private PVector mouseMovement = new PVector();

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

	public Level getLevel() {
		return lvl;
	}

	public ArrayList<Sprite> getSprites() {
		return new ArrayList<>(entities);
	}

	////////////
	// Set Up //
	////////////

	public void setUp() {
		lvl = Level.generate(getLevelSize(floor), applet, floor, new Date().getTime() + new Random(floor).nextInt());
		controllers = new HashSet<>(
				lvl.getEntities().stream().map((b) -> new ComputerController(b, this)).collect(Collectors.toSet()));
		entities = new HashSet<>(controllers.stream().map((c) -> c.getEntity()).collect(Collectors.toSet()));

		player = new Entity(lvl.getStartPosition(), new PVector(0.5f, 0.5f, 0.5f), null,
				new Attributes(1, 1, 1, 1, 1, 1));
		controller = new PlayerController(player, new InputSettings());
		controllers.add(controller);
		entities.add(player);
		renderer.addMessage("Welcome to floor " + floor + "!");
	}

	// Get the image of the game level as an appliable texture for the renderer.
	public PImage getLevelImage(PApplet applet) {
		return lvl.getGridImage(applet);
	}

	/////////////
	// Updates //
	/////////////

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
			updateControllers(deltaT);
		}
		confineMouseMovement();
	}

	// Update controllers and entity positions based on time passed..
	private void updateControllers(double deltaT) {
		for (EntityController c : controllers) {
			c.update();
		}
		for (Entity e : entities) {
			if (e.getVelocity().mag() > MAX_V) {
				e.setVelocity(PVector.mult(e.getVelocity().normalize(), MAX_V));
			}
			e.setVelocity(PVector.mult(e.getVelocity(), 0.8f));
			e.setPosition(PVector.add(e.getPosition(), PVector.mult(e.getVelocity(), (float) deltaT)));
			correctCollisions(e);
		}
	}

	// Confine or unconfine mouse movement based on game state.
	private void confineMouseMovement() {
		if (state == GameState.EXPLORE) {
			if (!nativew.isPointerConfined()) {
				nativew.confinePointer(true);
				nativew.setPointerVisible(false);
			}
			PVector currentMousePosition = new PVector(applet.mouseX, applet.mouseY);
			if (applet.mouseX != applet.width / 2 || applet.mouseY != applet.height / 2) {
				nativew.warpPointer(applet.width / 2, applet.height / 2);
				PVector mvt = PVector.sub(currentMousePosition, new PVector(applet.width / 2, applet.height / 2));
				mvt.x /= applet.width;
				mvt.y /= applet.height;
				mouseMovement = PVector.lerp(mouseMovement, mvt, 0.3f);
			}
			mouseMoved(mouseMovement);
			mouseMovement = PVector.lerp(mouseMovement, new PVector(), 0.3f);
		} else if (nativew.isPointerConfined()) {
			nativew.confinePointer(false);
			nativew.setPointerVisible(true);
		}
	}

	// Apply impulse resolutions on a given entity if it has collided with a wall.
	private void correctCollisions(Entity e) {
		PVector minBounds = PVector.sub(e.getPosition(), new PVector(e.getSize().x / 2, e.getSize().z / 2));
		PVector maxBounds = PVector.add(e.getPosition(), new PVector(e.getSize().x / 2, e.getSize().z / 2));

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

	///////////////
	// Rendering //
	///////////////

	public void draw(PGraphics main, PGraphics hud) {
		if (state == GameState.EXPLORE) {
			renderHUD(hud);
			PVector dir = PVector.mult(PVector.fromAngle((float) Math.PI / 2 + player.getRotation()), DIR_L);
			PVector plane = getPlane(dir, new PVector(main.width, main.height), (float) (Math.PI / 2f));
			if (plane != null) {
				renderer.draw(main, this, dir, plane);
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

	// Render the HUD on screen
	public void renderHUD(PGraphics appletCtx) {
		PVector realSize = new PVector(1f, 0.2f);
		PVector hudPosition = new PVector(0f, 0.8f);

		// Draw HUD Box
		appletCtx.pushStyle();
		appletCtx.fill(HUD_BG);
		appletCtx.stroke(HUD_STROKE_C);
		appletCtx.strokeWeight(HUD_STROKE_S * appletCtx.height);
		appletCtx.rect((HUD_STROKE_S / 2) * appletCtx.height, hudPosition.y * appletCtx.height,
				appletCtx.width - HUD_STROKE_S * appletCtx.height, realSize.y * appletCtx.height);
		appletCtx.popStyle();

		// Set up HUD style
		appletCtx.pushStyle();
		appletCtx.textAlign(PConstants.CENTER, PConstants.CENTER);
		appletCtx.textFont(Assets.getFont("FFFFORWA.TTF"));
		appletCtx.fill(HUD_FG);
		appletCtx.textSize((appletCtx.height * realSize.y) / 4);

		// Print text
		String hpText = "HP: " + (player.getHP() * 100 / player.getMaxHP()) + "%";
		String scoreText = "Score: " + score;
		appletCtx.text(hpText, 0.2f * appletCtx.width, (0.5f * realSize.y + hudPosition.y) * appletCtx.height);
		appletCtx.text(scoreText, 0.8f * appletCtx.width, (0.5f * realSize.y + hudPosition.y) * appletCtx.height);
		appletCtx.popStyle();
	}

	//////////////////
	// Constructors //
	//////////////////

	public Game(PApplet applet, GLWindow nativew) {
		this.applet = applet;
		this.nativew = nativew;
		renderer = new RaycastingRenderer(applet);
		setUp();
	}
}
