package wolfdungeon3d;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PShape;
import processing.core.PVector;

public class Button {
	// Constants
	private static final int NORMAL_BG_GRAY = 32;
	private static final int HOVERED_BG_GRAY = 200;
	private static final int NORMAL_FG_GRAY = 255;
	private static final int HOVERED_FG_GRAY = 28;
	private static final float ANIM_LERP_COEFF = 0.2f;

	private PShape shape;
	private String text;
	private PVector size;
	private PVector position;
	private EventCallback onClickCallback;
	private IntTuple currentColours = new IntTuple(NORMAL_BG_GRAY, NORMAL_FG_GRAY);

	public void draw(PGraphics g, PVector rMousePos) {
		PVector scaledMousePos = new PVector(rMousePos.x / g.width, rMousePos.y / g.height);
		if (isHovered(scaledMousePos)) {
			currentColours = new IntTuple((int) PApplet.lerp(currentColours.a, HOVERED_BG_GRAY, ANIM_LERP_COEFF),
					(int) PApplet.lerp(currentColours.b, HOVERED_FG_GRAY, ANIM_LERP_COEFF));
		} else {
			currentColours = new IntTuple((int) PApplet.lerp(currentColours.a, NORMAL_BG_GRAY, ANIM_LERP_COEFF),
					(int) PApplet.lerp(currentColours.b, NORMAL_FG_GRAY, ANIM_LERP_COEFF));
		}

		// Draw button shape
		if (shape == null) {
			shape = g.createShape(PConstants.RECT, 0, 0, size.x * g.width, size.y * g.height);
		}
		g.pushStyle();
		g.fill(currentColours.a);
		g.shape(shape, position.x * g.width, position.y * g.height);
		g.popStyle();

		// Draw button text
		float textSize = (size.y * g.height / 2);
		g.pushStyle();
		g.fill(g.color(currentColours.b));
		g.textAlign(PConstants.CENTER, PConstants.CENTER);
		g.text(text, (position.x + size.x / 2) * g.width, -textSize / 4 + (position.y + size.y / 2) * g.height);
		g.popStyle();
	}

	/**
	 * Check if a button is being hovered.
	 */
	public boolean isHovered(PVector adaptedMousePosition) {
		PVector dist = PVector.sub(adaptedMousePosition, position);
		return dist.x > 0 && dist.x < size.x && dist.y > 0 && dist.y < size.y;
	}
}
