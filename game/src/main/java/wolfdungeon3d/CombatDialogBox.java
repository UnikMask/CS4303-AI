package wolfdungeon3d;

import processing.core.PVector;

public class CombatDialogBox {
	private static final ButtonStyle STYLE = new ButtonStyle(0xff2e2a2b, 0xffe9d49c, 0xffe9d49c, 0xff2e2a2b, 0x00,
			0x00);
	private static final int DIAG_BG = 0xff2e2a2b;
	private static final int DIAG_FG = 0xffe9d49c;

	private Button attackButton;
	private Button defendButton;
	private Button castButton;
	private Button fleeButton;

	public CombatDialogBox(Entity e, Game game) {
		this.attackButton = new Button("Attack", new PVector(0.25f, 0.6f), new PVector(0.7f, 0.1f),
				new EventCallback() {
					public void call() {
						game.setNextPlayerCommand(new AttackCommand("attack", 10, game.getEnemy(), (e) -> 0));
					}
				});
	}
}
