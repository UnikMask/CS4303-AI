package wolfdungeon3d;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PShape;
import processing.core.PVector;

public class Button {
	// Constants
	private static final ButtonStyle DEFAULT_STYLE = new ButtonStyle(0xffc8c8c8, 0xff2e2a2b, 0xff202020, 0xffe9d49c,
			0x00, 0x00);
	private static final float ANIM_LERP_COEFF = 0.2f;

	// Attributes
	private PShape shape;
	private String text;
	private PVector size;
	private PVector position;
	private EventCallback onClickCallback;
	private ButtonStyle style;
	private IntTuple currentColours;

	static class ButtonStyle {
		int hoveredBgColor;
		int hoveredFgColor;
		int normalBgColor;
		int normalFgColor;
		int strokeSize;
		int strokeColor;

		ButtonStyle(int hbg, int hfg, int bg, int fg, int ss, int sc) {
			hoveredBgColor = hbg;
			hoveredFgColor = hfg;
			normalBgColor = bg;
			normalFgColor = fg;
			strokeSize = ss;
			strokeColor = sc;
		}
	}

	/**
	 * Draw the button on the given graphical context
	 *
	 * @param g         The graphical context to draw on.
	 * @param rMousePos The real position of the mouse on the screen
	 */
	public void draw(PGraphics g, PVector rMousePos, PVector screenBounds) {
		if (isHovered(scaleMousePosition(rMousePos, screenBounds))) {
			currentColours = new IntTuple((int) g.lerpColor(currentColours.a, style.hoveredBgColor, ANIM_LERP_COEFF),
					(int) g.lerpColor(currentColours.b, style.hoveredFgColor, ANIM_LERP_COEFF));
		} else {
			currentColours = new IntTuple((int) g.lerpColor(currentColours.a, style.normalBgColor, ANIM_LERP_COEFF),
					(int) g.lerpColor(currentColours.b, style.normalFgColor, ANIM_LERP_COEFF));
		}

		g.pushStyle();
		g.fill(g.color(currentColours.a));
		if (style.strokeSize == 0) {
			g.noStroke();
		}
		g.rect(position.x * g.width, position.y * g.height, size.x * g.width, size.y * g.height);
		g.popStyle();

		// Draw button text
		float textSize = (size.y * g.height / 2);
		g.pushStyle();
		g.fill(g.color(currentColours.b));
		g.textAlign(PConstants.CENTER, PConstants.CENTER);
		g.textFont(Assets.getFont("FFFFORWA.TTF"));
		g.textSize(textSize);
		g.text(text, (position.x + size.x / 2) * g.width, (position.y + size.y / 2) * g.height);
		g.popStyle();
	}

	/**
	 * Check if a button is being hovered.
	 *
	 * @param adaptedMousePosition the position of the mouse scaled to the screen.
	 */
	public boolean isHovered(PVector adaptedMousePosition) {
		PVector dist = PVector.sub(adaptedMousePosition, position);
		return dist.x > 0 && dist.x < size.x && dist.y > 0 && dist.y < size.y;
	}

	/**
	 * On click event of a button.
	 *
	 * @param rMousePos    Real mouse position on screen - used to calculate if a
	 *                     button is hovered.
	 * @param screenBounds The bounds of the screen for mouse position scaling.
	 */
	public void onClick(PVector rMousePos, PVector screenBounds) {
		if (isHovered(scaleMousePosition(rMousePos, screenBounds))) {
			onClickCallback.call();
		}
	}

	private PVector scaleMousePosition(PVector rMousePos, PVector screenBounds) {
		return new PVector(rMousePos.x / screenBounds.x, rMousePos.y / screenBounds.y);

	}

	/**
	 * Constructor for a button
	 *
	 * @param text     The text inside the button.
	 * @param size     The size of the button.
	 * @param position The position of the button on the graphical context.
	 * @param cb       The callback run on click.
	 */
	public Button(String text, PVector size, PVector position, EventCallback cb) {
		this(text, size, position, cb, DEFAULT_STYLE);
	}

	/**
	 * Constructor for a button
	 *
	 * @param text     The text inside the button.
	 * @param size     The size of the button.
	 * @param position The position of the button on the graphical context.
	 * @param cb       The callback run on click.
	 * @param bs       The button style to use.
	 */
	public Button(String text, PVector size, PVector position, EventCallback cb, ButtonStyle bs) {
		this.text = text;
		this.size = size;
		this.position = position;
		onClickCallback = cb;
		this.style = bs;
		this.currentColours = new IntTuple(style.normalBgColor, style.normalFgColor);
	}
}
