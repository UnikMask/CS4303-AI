package wolfdungeon3d;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.jogamp.newt.opengl.GLWindow;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;
import wolfdungeon3d.Level.EntityBehaviour;
import wolfdungeon3d.Level.Tile;

public class Game {
	// Level Constants
	private static final PVector BASE_FLOOR_SIZE = new PVector(25, 25);
	private static final PVector FLOOR_SIZE_INCREMENT = new PVector(2, 2);

	// Physics Constants
	private static final PVector MAIN_CTX_PERCENT = new PVector(1f, 0.8f);
	private static final float DIR_L = 0.2f;
	private static final float MAX_V = 3.0f;

	// Color Constants
	private static final int HUD_BG = 0xff2e2a2b;
	private static final int HUD_FG = 0xffe9d49c;
	private static final int HUD_STROKE_C = 0xff584c4c;
	private static final float HUD_STROKE_S = 0.025f;

	// Battle Constants
	private static final float MIN_BATTLE_DIST = 1f;

	// HUD Constants
	private static final float HUD_EXPLORE_LEFT_ANCHOR_W = 0.1f;
	private static final float HUD_EXPLORE_RIGHT_ANCHOR_W = 0.9f;
	private static final float HUD_COMBAT_LEFT_ANCHOR_W = 0.03f;
	private static final float HUD_COMBAT_RIGHT_ANCHOR_W = 0.97f;

	// Inventory Constants
	private static final IntTuple INVENTORY_SIZE = new IntTuple(4, 8);

	private GLWindow nativew;
	private PApplet applet;
	private PGraphics main;
	private GameState state = GameState.LOADING;
	private Entity player;
	private HashMap<Entity, EntityController> entityControllerMap;
	private HashMap<IntTuple, Item> itemPositionsMap;
	private String action = null;

	private PlayerController controller;
	private RaycastingRenderer renderer;
	private int score = 0;
	private Level lvl;
	private int floor = 0;
	private int enemiesKilled = 0;
	private long lastFrameTime = 0;

	// Battle-related vars
	private Combat combatInstance;
	private Entity enemy = null;
	private CombatCommand nextPlayerCommand = null;
	private CombatDialogBox playerCombatDialog;

	// Invenoty variables
	private Inventory inventory;
	private InventoryPage page;

	// Mouse handling
	private PVector mouseMovement = new PVector();

	static enum GameState {
		EXPLORE, BATTLE, LOADING, INVENTORY, END
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

	public int getFloor() {
		return floor;
	}

	public ArrayList<Sprite> getSprites() {
		return new ArrayList<>() {
			{
				addAll(entityControllerMap.keySet());
				addAll(itemPositionsMap.values());
			}
		};
	}

	public Entity getEnemy() {
		return enemy;
	}

	public void setNextPlayerCommand(CombatCommand cc) {
		this.nextPlayerCommand = cc;
	}

	public CombatDialogBox getPlayerCombatDialogBox() {
		return playerCombatDialog;
	}

	public int calculateScore() {
		int numItems = inventory.getNumItems();
		return floor * 100 + numItems * 10 + enemiesKilled * 10;
	}

	////////////
	// Set Up //
	////////////

	public void setUp() {
		lvl = Level.generate(getLevelSize(floor), floor, player != null ? player.getAttributes().luck : 1,
				new Date().getTime() + new Random(floor).nextInt());
		entityControllerMap = new HashMap<>();
		for (EntityBehaviour b : lvl.getEntities()) {
			entityControllerMap.put(b.e, new ComputerController(b, this));
		}
		itemPositionsMap = new HashMap<>();
		for (Item i : lvl.getCollectibleItems()) {
			itemPositionsMap.put(new IntTuple(i.getPosition()), i);
		}

		if (player == null) {
			player = new Entity("You", lvl.getStartPosition(), new PVector(0.5f, 0.5f, 0.5f), null,
					Attributes.getDefaultPlayerAttributes(), 1);
			inventory = new Inventory(INVENTORY_SIZE.a, INVENTORY_SIZE.b);
			controller = new PlayerController(player, this, new InputSettings());
		} else {
			player.setPosition(lvl.getStartPosition());
			player.setVelocity(new PVector(0, 0));
			player.setRotation(0);
		}
		entityControllerMap.put(player, controller);
		renderer.addMessage("Welcome to floor " + floor);
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

		s: switch (state) {
		case LOADING:
			if (lvl == null) {
				setUp();
			} else {
				state = GameState.EXPLORE;
			}
			break;
		case EXPLORE:
			score = calculateScore();
			updateControllers(deltaT);
			for (Entity e : entityControllerMap.keySet()) {
				// Initiate battle
				if (e != player && PVector.sub(e.getPosition(), player.getPosition()).mag() < MIN_BATTLE_DIST) {
					state = GameState.BATTLE;
					enemy = e;
					break s;
				}
			}

			IntTuple playerPosition = new IntTuple(player.getPosition());
			if (lvl.getTile(playerPosition.a, playerPosition.b) == Tile.END) {
				action = "go to the next floor.";
			} else if (itemPositionsMap.containsKey(playerPosition)) {
				action = "collect " + itemPositionsMap.get(playerPosition).getName();
			} else {
				action = null;
			}
			break;
		case BATTLE:
			combatLogic();
		default:
			break;
		}
		confineMouseMovement();
	}

	public void interact() {
		if (state == GameState.EXPLORE) {
			IntTuple playerPos = new IntTuple(player.getPosition());
			if (lvl.getTile(playerPos.a, playerPos.b) == Tile.END) {
				floor += 1;
				state = GameState.LOADING;
				lvl = null;
			} else if (itemPositionsMap.containsKey(playerPos)) {
				Item i = itemPositionsMap.get(playerPos);
				if (inventory.add(i)) {
					itemPositionsMap.remove(playerPos);
					renderer.addMessage("Collected " + i.getName());
				} else {
					renderer.addMessage("Inventory is full!");
				}
			}
		}
	}

	// Update controllers and entity positions based on time passed..
	private void updateControllers(double deltaT) {
		for (Entity e : entityControllerMap.keySet()) {
			EntityController c = entityControllerMap.get(e);
			c.update();
			if (e.getVelocity().mag() > MAX_V) {
				e.setVelocity(PVector.mult(e.getVelocity().normalize(), MAX_V));
			}
			e.setVelocity(PVector.mult(e.getVelocity(), 1 - (2.0f * (float) -deltaT)));
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
			if (lvl.getTile(cornerTilePos.a, cornerTilePos.b) == Tile.WALL) {
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
					if (lvl.getTile(nextTilePos.a, nextTilePos.b) != Tile.WALL) {
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

	/**
	 * Handle combat logic. If the combat instance has messages, wait until all
	 * messages have been consumed. If it is the player's turn, wait until the
	 * player has responded before making an action. If it is the computer's turn,
	 * immediately do an action.
	 */
	private void combatLogic() {
		if (combatInstance == null) {
			combatInstance = new Combat(Set.of(player), Set.of(enemy));
			return;
		}
		PVector dir = PVector.sub(player.getPosition(), enemy.getPosition());
		player.setRotation(dir.heading() + (float) Math.PI / 2);

		// Combat Logic -
		if (combatInstance.hasMessages()) {
			playerCombatDialog = null;
			if (!renderer.hasMessages()) {
				renderer.addMessage(combatInstance.getNewMessage());
			}
		} else if (combatInstance.hasEnded()) {
			for (Entity e : combatInstance.getDefeatedEntities()) {
				if (entityControllerMap.containsKey(e)) {
					entityControllerMap.remove(e);
					enemiesKilled++;
				}
				if (e == player) {
					enemiesKilled = enemiesKilled == 0 ? 0 : enemiesKilled - 1;
					state = GameState.END;
					return;
				}
			}

			// Wrap up battle
			combatInstance = null;
			enemy = null;
			state = GameState.EXPLORE;
		} else {
			Entity e = combatInstance.getCurrentEntity();
			if (e == player) {
				if (nextPlayerCommand != null) {
					combatInstance.nextCommand(nextPlayerCommand);
					nextPlayerCommand = null;
				} else if (playerCombatDialog == null) {
					playerCombatDialog = new CombatDialogBox(player, this);
				}
			} else {
				playerCombatDialog = null;
				combatInstance.nextCommand(entityControllerMap.get(e).getCombatTurn(combatInstance));
			}
		}
	}

	////////////////////
	// Input Handling //
	////////////////////

	public void keyPressed(Character key) {
		switch (state) {
		case EXPLORE:
			if (key == 'i' || key == 'I') {
				state = GameState.INVENTORY;
				page = new InventoryPage(inventory, player, applet.getGraphics());
			} else {
				controller.onKeyPressed(key);
			}
		case BATTLE:
			if (key == '\n') {
				renderer.nextMessage();
			}
			break;
		case INVENTORY:
			if (key == 'i' || key == 'I') {
				page.apply();
				state = GameState.EXPLORE;
				page = null;
			} else {
				page.keyPressed(key);
			}
			break;
		default:
		}
	}

	public void keyReleased(Character key) {
		controller.onKeyReleased(key);
		for (EntityController c : entityControllerMap.values()) {
			c.onKeyReleased(key);
		}
	}

	public void keyHeld(Character key) {
		controller.onKeyHeld(key);
		for (EntityController c : entityControllerMap.values()) {
			c.onKeyHeld(key);
		}
	}

	public void mouseMoved(PVector mvt) {
		controller.onMouseMove(mvt);
		for (EntityController c : entityControllerMap.values()) {
			c.onMouseMove(mvt);
		}
	}

	public void onClick() {
		if (state == GameState.BATTLE && playerCombatDialog != null) {
			playerCombatDialog.onClick(new PVector(applet.mouseX, applet.mouseY),
					new PVector(applet.width, applet.height));
			playerCombatDialog = null;
		}
	}

	///////////////
	// Rendering //
	///////////////

	public void draw() {
		switch (state) {
		case EXPLORE:
		case BATTLE:
			main.colorMode(PConstants.ARGB);
			main.beginDraw();
			renderHUD();
			PVector dir = PVector.mult(PVector.fromAngle((float) Math.PI / 2 + player.getRotation()), DIR_L);
			PVector plane = getPlane(dir, new PVector(main.width, main.height), (float) (Math.PI / 2f));
			if (plane != null) {
				renderer.draw(main, this, dir, plane, action);
			}
			main.endDraw();
			applet.image(main, 0, 0);
			break;
		case INVENTORY:
			if (page != null) {
				page.draw();
			}
			break;
		default:
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
	public void renderHUD() {
		PVector realSize = new PVector(1f, 0.2f);
		PVector hudPosition = new PVector(0f, 0.8f);

		// Draw HUD Box
		applet.pushStyle();
		applet.fill(HUD_BG);
		applet.stroke(HUD_STROKE_C);
		applet.strokeWeight(HUD_STROKE_S * applet.height);
		applet.rect((HUD_STROKE_S / 2) * applet.height, hudPosition.y * applet.height,
				applet.width - HUD_STROKE_S * applet.height, realSize.y * applet.height);
		applet.popStyle();

		// Set up HUD style
		applet.pushStyle();
		applet.textFont(Assets.getFont("FFFFORWA.TTF"));
		applet.fill(HUD_FG);
		applet.textSize((applet.height * realSize.y) / 4);

		// Define text
		float leftAnchor = state == GameState.BATTLE ? HUD_COMBAT_LEFT_ANCHOR_W : HUD_EXPLORE_LEFT_ANCHOR_W;
		float rightAnchor = state == GameState.BATTLE ? HUD_COMBAT_RIGHT_ANCHOR_W : HUD_EXPLORE_RIGHT_ANCHOR_W;
		String hpText = "HP: " + String.format("%.0f", Math.max(0, player.getHP()) * 100 / player.getMaxHP()) + "%";
		String scoreText = "Score: " + score;
		String floorText = "Floor: " + floor;
		String levelText = "Level: " + player.getLevel();
		String xpText = "XP: " + player.getXP() + "/" + player.XPToNextLevel();

		// Print text
		applet.textAlign(PConstants.LEFT, PConstants.CENTER);
		applet.text(hpText, leftAnchor * applet.width, (0.25f * realSize.y + hudPosition.y) * applet.height);
		applet.text(floorText, leftAnchor * applet.width, (0.75f * realSize.y + hudPosition.y) * applet.height);
		applet.textSize((applet.height * realSize.y) / 6);
		applet.textAlign(PConstants.RIGHT, PConstants.CENTER);
		applet.text(scoreText, rightAnchor * applet.width, (0.2f * realSize.y + hudPosition.y) * applet.height);
		applet.text(levelText, rightAnchor * applet.width, (0.5f * realSize.y + hudPosition.y) * applet.height);
		applet.text(xpText, rightAnchor * applet.width, (0.8f * realSize.y + hudPosition.y) * applet.height);
		applet.popStyle();

		// Print Center emblem
		float emblemSize = 0.2f * applet.height;
		applet.pushStyle();
		applet.fill(0xff292223);
		applet.stroke(HUD_STROKE_C);
		applet.strokeWeight(0.01f * applet.height);
		applet.rect(0.5f * applet.width - emblemSize / 2, 0.8f * applet.height, emblemSize, emblemSize);
		applet.popStyle();

		// If battle mode is on, print enemy info in the middle
		if (state == GameState.BATTLE) {
			String enemyHpText = "HP: " + String.format("%.0f", Math.max(0, enemy.getHP()) * 100 / enemy.getMaxHP())
					+ "%";
			applet.pushStyle();
			applet.fill(HUD_FG);
			applet.textAlign(PConstants.CENTER, PConstants.CENTER);
			applet.textFont(Assets.getFont("FFFFORWA.TTF"));
			applet.textSize((applet.height * realSize.y) / 10);
			applet.text("Enemy: ", 0.5f * applet.width, (0.2f * realSize.y + hudPosition.y) * applet.height);
			applet.text(enemy.getName(), 0.5f * applet.width, (0.5f * realSize.y + hudPosition.y) * applet.height);
			applet.text(enemyHpText, 0.5f * applet.width, (0.8f * realSize.y + hudPosition.y) * applet.height);
			applet.popStyle();
		}

		// Print combat dialog if it is required
		if (playerCombatDialog != null) {
			playerCombatDialog.draw(applet.getGraphics(), new PVector(applet.mouseX, applet.mouseY),
					new PVector(applet.width, applet.height));
		}
	}

	//////////////////
	// Constructors //
	//////////////////

	public Game(PApplet applet, GLWindow nativew) {
		this.applet = applet;
		this.main = applet.createGraphics(applet.width, (int) ((float) applet.height * MAIN_CTX_PERCENT.y),
				PConstants.P3D);
		this.nativew = nativew;
		renderer = new RaycastingRenderer(applet);
		setUp();
	}
}
