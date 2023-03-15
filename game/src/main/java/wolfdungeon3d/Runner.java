/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package wolfdungeon3d;

import java.util.HashSet;

import com.jogamp.newt.opengl.GLWindow;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import wolfdungeon3d.Game.GameState;

public class Runner extends PApplet {
	static private final PVector MAIN_CTX_PERCENT = new PVector(1f, 0.8f);
	static private final PVector MAIN_CTX_POS = new PVector(0, 0);

	PGraphics mainGraphicsCtx;
	Game game;
	RunnerState state = RunnerState.GAME;
	HashSet<Character> heldKeys = new HashSet<>();
	PVector mouseMovement = new PVector();
	PVector lastMousePosition = new PVector();

	static enum RunnerState {
		MENU, GAME
	}

	// Main update loop
	public void update() {
		if (state == RunnerState.GAME && game == null) {
			game = new Game(this);
		} else if (state == RunnerState.GAME && game != null) {
			for (Character k : heldKeys) {
				game.keyHeld(k);
			}
			game.update();
		}
		if (state == RunnerState.GAME && game != null && game.getState() == GameState.EXPLORE) {
			if (!((GLWindow) surface.getNative()).isPointerConfined()) {
				((GLWindow) surface.getNative()).confinePointer(true);
				((GLWindow) surface.getNative()).setPointerVisible(false);
			}
			PVector currentMousePosition = new PVector(mouseX, mouseY);
			if (mouseX != width / 2 || mouseY != height / 2) {
				((GLWindow) surface.getNative()).warpPointer(width / 2, height / 2);
				PVector mvt = PVector.sub(currentMousePosition, new PVector(width / 2, height / 2));
				mvt.x /= width;
				mvt.y /= height;
				mouseMovement = PVector.lerp(mouseMovement, mvt, 0.3f);
			}
			game.mouseMoved(mouseMovement);
			mouseMovement = PVector.lerp(mouseMovement, new PVector(), 0.3f);
			lastMousePosition = currentMousePosition;
		} else {
			if (((GLWindow) surface.getNative()).isPointerConfined()) {
				((GLWindow) surface.getNative()).confinePointer(false);
				((GLWindow) surface.getNative()).setPointerVisible(true);
			}
		}
	}

	////////////////////
	// Input Handling //
	////////////////////

	public void keyPressed() {
		if (!heldKeys.contains(key)) {
			heldKeys.add(key);
		}
		if (game != null) {
			game.keyPressed(key);
		}
	}

	public void keyReleased() {
		if (heldKeys.contains(key)) {
			heldKeys.remove(key);
		}
		if (game != null) {
			game.keyReleased(key);
		}
	}

	///////////////////////
	// PApplet Functions //
	///////////////////////

	public void setup() {
		frameRate(60);
		mainGraphicsCtx = createGraphics((int) ((float) width * MAIN_CTX_PERCENT.x),
				(int) ((float) height * MAIN_CTX_PERCENT.y), PApplet.P3D);
		Assets.createInstance(this);
	}

	public void settings() {
		size(1920, 1080, PApplet.P3D);
		fullScreen();
	}

	public void draw() {
		update();
		background(128);
		mainGraphicsCtx.colorMode(PGraphics.ARGB);
		mainGraphicsCtx.beginDraw();
		mainGraphicsCtx.blendMode(PGraphics.BLEND);
		if (state == RunnerState.GAME && game != null) {
			game.draw(mainGraphicsCtx);
		}
		mainGraphicsCtx.endDraw();
		image(mainGraphicsCtx, MAIN_CTX_POS.x, MAIN_CTX_POS.y);
	}

	public static void main(String[] args) {
		PApplet.main(new String[] { "wolfdungeon3d.Runner" });
	}
}
