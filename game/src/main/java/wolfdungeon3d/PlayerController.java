package wolfdungeon3d;

import java.util.HashSet;

import processing.core.PVector;
import wolfdungeon3d.InputSettings.Command;

public class PlayerController implements EntityController {
	Entity e;
	InputSettings inputs;
	HashSet<Command> movementInputs = new HashSet<>();
	Game game;

	public Entity getEntity() {
		return e;
	}

	public void onKeyPressed(Character c) {
		Command comm = inputs.getCommand(c);
		if (comm == Command.INTERACT) {
			game.goToNextFloor();
		}
	}

	public void onKeyHeld(Character c) {
		movementInputs.add(inputs.getCommand(c));
	}

	public void onKeyReleased(Character c) {
	}

	public void onMouseMove(PVector mouseVelocity) {
		float mvt = mouseVelocity.x;
		e.setRotation(e.getRotation() - mvt * inputs.getMouseSensitivity());
	}

	public void onMouseClick(PVector mousePosition) {

	}

	public void update() {
		PVector dir = new PVector();
		for (Command c : movementInputs) {
			if (c == Command.FORWARD)
				dir.y += dir.y == -1f ? 0 : -1;
			else if (c == Command.BACKWARD)
				dir.y += dir.y == 1f ? 0 : 1;
			else if (c == Command.LEFT)
				dir.x += dir.x == 1f ? 0 : 1;
			else if (c == Command.RIGHT)
				dir.x += dir.x == -1f ? 0 : -1;
		}
		movementInputs = new HashSet<>();
		e.move(dir);
	}

	public CombatCommand getCombatTurn(Combat combat) {
		return null;
	}

	/////////////////
	// Constructor //
	/////////////////

	public PlayerController(Entity e, Game game, InputSettings inputs) {
		this.game = game;
		this.inputs = inputs;
		this.e = e;
	}
}
