package wolfdungeon3d;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;
import wolfdungeon3d.Runner.RunnerState;

public class MainMenu {
	private static final float ALPHA = 0.025f;
	private static final float BUTTON_HEIGHT = 0.2f;

	private Button playButton;
	private Button helpButton;
	private Button quitButton;

	public void draw(PApplet p, PVector realMousePosition, int lastScore) {
		p.background(0xff2e2a2b);
		p.pushStyle();
		p.fill(0xff9c9c9c);
		p.textSize((float) p.height / 100 * 25);
		p.textAlign(PConstants.CENTER, PConstants.CENTER);
		p.textFont(Assets.getFont("FFFFORWA.TTF"));
		p.text("Wolf Dungeon 3D", p.width / 2, 1.5f * p.height / 8);
		p.popStyle();

		if (lastScore != -1) {
			p.pushStyle();
			p.fill(0xffffffff);
			p.textSize(p.height * 0.02f);
			p.textAlign(PConstants.CENTER, PConstants.CENTER);
			p.textFont(Assets.getFont("FFFFORWA.TTF"));
			p.text("Last Score: " + lastScore, p.width * 0.5f, 0.4f * p.height);
			p.popStyle();
		}

		playButton.draw(p.getGraphics(), realMousePosition, new PVector(p.width, p.height));
		// helpButton.draw(p.getGraphics(), realMousePosition, new PVector(p.width,
		// p.height));
		quitButton.draw(p.getGraphics(), realMousePosition, new PVector(p.width, p.height));
	}

	public void handleOnClick(PVector realMousePosition, PVector screenBounds) {
		playButton.onClick(realMousePosition, screenBounds);
		// helpButton.onClick(realMousePosition, screenBounds);
		quitButton.onClick(realMousePosition, screenBounds);
	}

	public MainMenu(Runner runner) {
		float bxsize = (1 - 6 * ALPHA) / 3;
		playButton = new Button("Play", new PVector(bxsize, BUTTON_HEIGHT), new PVector(ALPHA, 0.5f),
				new EventCallback() {
					public void call() {
						runner.setState(RunnerState.GAME);
					}
				});
		helpButton = new Button("Help", new PVector(bxsize, BUTTON_HEIGHT), new PVector(bxsize + 3 * ALPHA, 0.5f),
				new EventCallback() {
					public void call() {
						runner.setState(RunnerState.HELP);
					}
				});
		quitButton = new Button("Quit", new PVector(bxsize, BUTTON_HEIGHT), new PVector(2 * bxsize + 5 * ALPHA, 0.5f),
				new EventCallback() {
					public void call() {
						runner.exit();
					}
				});
	}
}
