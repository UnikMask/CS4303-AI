package wolfdungeon3d;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import processing.core.PGraphics;
import processing.core.PVector;

public class CombatDialogBox {
	private Button attackButton;
	private Button defendButton;
	// private Button castButton;
	private Button fleeButton;

	/**
	 * Draw the dialog box on the given PGraphics object.
	 *
	 * @param g The graphics context to draw on.
	 * @param s The shader used to keep the dialog box on top - must support
	 *          selecting texture via "usingTexture" uniform.
	 */
	public void draw(PGraphics g, PVector realMousePosition, PVector screenBounds) {
		// Draw buttons
		List<Button> drawButtons = Arrays.asList(attackButton, defendButton, fleeButton);
		g.pushMatrix();
		for (Button b : drawButtons) {
			b.draw(g, realMousePosition, screenBounds);
		}
		g.popMatrix();
	}

	public void onClick(PVector realMousePosititon, PVector screenBounds) {
		attackButton.onClick(realMousePosititon, screenBounds);
		defendButton.onClick(realMousePosititon, screenBounds);
		fleeButton.onClick(realMousePosititon, screenBounds);
	}

	/**
	 * Constructor for a combat dialog box.
	 *
	 * @param e    The entity that controls the dialog box.
	 * @param game The game the dialog's context is on.
	 */
	public CombatDialogBox(Entity e, Game game) {
		this.attackButton = new Button("Attack", new PVector(0.15f, 0.08f), new PVector(0.25f, 0.81f),
				new EventCallback() {
					public void call() {
						game.setNextPlayerCommand(
								new AttackCommand("attack", e.getDamage(), game.getEnemy(), (e) -> 0));
					}
				});
		this.defendButton = new Button("Defend", new PVector(0.15f, 0.08f), new PVector(0.25f, 0.91f),
				new EventCallback() {
					Function<Entity, Integer> effect = e.getArmor() != null ? e.getArmor().effects : (e) -> 0;

					public void call() {
						game.setNextPlayerCommand(new DefendCommand("defend", effect, (f) -> {
							return f / e.getResistance();
						}, e.getArmor().effectType));
					}
				});
		this.fleeButton = new Button("Flee", new PVector(0.15f, 0.08f), new PVector(0.6f, 0.81f), new EventCallback() {
			public void call() {
				game.setNextPlayerCommand(new FleeCommand());
			}
		});
	}

}
