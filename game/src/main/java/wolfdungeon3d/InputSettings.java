package wolfdungeon3d;

import java.util.HashMap;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class InputSettings {
	HashMap<Character, Command> charCommandMap = Maps.newHashMap(ImmutableMap.of('w', Command.FORWARD, 's',
			Command.BACKWARD, 'a', Command.LEFT, 'd', Command.RIGHT, 'p', Command.PAUSE));

	enum Command {
		FORWARD, BACKWARD, LEFT, RIGHT, PAUSE, NONE
	}

	public Command getCommand(Character input) {
		if (charCommandMap.containsKey(input)) {
			return charCommandMap.get(input);
		} else {
			return Command.NONE;
		}
	}
}
