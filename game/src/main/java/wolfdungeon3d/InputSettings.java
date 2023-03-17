package wolfdungeon3d;

import java.util.HashMap;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class InputSettings {
	private HashMap<Character, Command> charCommandMap = Maps
			.newHashMap(ImmutableMap.of('w', Command.FORWARD, 's', Command.BACKWARD, 'a', Command.LEFT, 'd',
					Command.RIGHT, 'p', Command.PAUSE, 'e', Command.INTERACT, 'f', Command.CONSUME));
	private float mouseSensitivity = 3.0f;

	enum Command {
		FORWARD, BACKWARD, LEFT, RIGHT, PAUSE, INTERACT, CONSUME, NONE
	}

	public float getMouseSensitivity() {
		return mouseSensitivity;
	}

	public void setMouseSensitivity(float mouseSensitivity) {
		this.mouseSensitivity = mouseSensitivity;
	}

	public Command getCommand(Character input) {
		if (charCommandMap.containsKey(input)) {
			return charCommandMap.get(input);
		} else {
			return Command.NONE;
		}
	}
}
