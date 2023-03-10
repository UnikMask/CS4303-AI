package wolfdungeon3d;

import java.util.function.Function;

public abstract class CombatCommand {
	protected String name;
	protected CommandType type;
	protected Function<Entity, Integer> effect;

	enum CommandType {
		ATTACK, DEFEND, FLEE, CAST
	}

	public String getName() {
		return name;
	}

	public abstract CommandType getCommandType();
}
