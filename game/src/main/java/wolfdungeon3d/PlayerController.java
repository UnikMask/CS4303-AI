package wolfdungeon3d;

import java.util.HashSet;

import processing.core.PVector;
import wolfdungeon3d.InputSettings.Command;

public class PlayerController implements EntityController {
	Entity e;
	InputSettings inputs;
	HashSet<Command> movementInputs = new HashSet<>();

	public void onKeyPressed(Character c) {

	}

	public void onKeyHeld(Character c) {
		movementInputs.add(inputs.getCommand(c));
	}

	public void onKeyReleased(Character c) {

	}

	public void onMouseMove(PVector mouseVelocity) {
		float mvt = mouseVelocity.x;
		e.setRotation(e.getRotation() + mvt * (-3));
	}

	public void onMouseClick(PVector mousePosition) {

	}

	public void update() {
		PVector dir = new PVector();
		for (Command c : movementInputs) {
			if (c == Command.FORWARD)
				dir.y = dir.y == -1f ? 0 : -1;
			else if (c == Command.BACKWARD)
				dir.y = dir.y == 1f ? 0 : -1;
			else if (c == Command.LEFT)
				dir.x = dir.x == 1f ? 0 : -1;
			else if (c == Command.RIGHT)
				dir.x = dir.x == -1f ? 0 : 1;
		}
		e.move(dir);
	}

	public void getCombatTurn(Combat combat) {

	}

	/////////////////
	// Constructor //
	/////////////////

	public PlayerController(Entity e, InputSettings inputs) {
		this.inputs = inputs;
		this.e = e;
	}
}
