/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package wolfdungeon3d;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

public class Runner extends PApplet {
	static private final PVector MAIN_CTX_PERCENT = new PVector(1f, 0.8f);
	static private final PVector MAIN_CTX_POS = new PVector();

	PGraphics mainGraphicsCtx;

	public void setup() {
		frameRate(60);
		mainGraphicsCtx = createGraphics((int) ((float) width * MAIN_CTX_PERCENT.x),
				(int) ((float) height * MAIN_CTX_PERCENT.y), PApplet.P2D);
		Level lvl = Level.generate(new PVector(35, 35), 163292389);
		System.out.println(lvl);

	}

	public void settings() {
		size(1920, 1080, PApplet.P2D);
	}

	public void draw() {
		mainGraphicsCtx.beginDraw();
		mainGraphicsCtx.background(0);
		mainGraphicsCtx.endDraw();
		image(mainGraphicsCtx, MAIN_CTX_POS.x, MAIN_CTX_POS.y);
	}

	public static void main(String[] args) {
		PApplet.main(new String[] { "wolfdungeon3d.Runner" });
	}
}
