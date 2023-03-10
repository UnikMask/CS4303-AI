package wolfdungeon3d;

import java.util.function.Function;

public class FleeCommand extends CombatCommand {
	private static final String FLEE_NAME = "flee";
	private static Function<Entity, Integer> FLEE_EFFECT = (e) -> 0;

	public CommandType getCommandType() {
		return CommandType.FLEE;
	}

	public FleeCommand() {
		this.name = FLEE_NAME;
		this.effect = FLEE_EFFECT;
	}
}
