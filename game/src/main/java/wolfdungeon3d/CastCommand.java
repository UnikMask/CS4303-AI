package wolfdungeon3d;

import java.util.function.Function;

public class CastCommand extends CombatCommand {
	public CommandType getCommandType() {
		return CommandType.CAST;
	}

	public CastCommand(String name, Function<Entity, Integer> effect) {
		this.name = name;
		this.effect = effect;
	}
}
