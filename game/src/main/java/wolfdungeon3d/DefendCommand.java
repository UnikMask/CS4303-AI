package wolfdungeon3d;

import java.util.function.Function;

public class DefendCommand extends CombatCommand {
	private Function<Float, Float> defenseEffect;

	public Function<Float, Float> getDefenseEffect() {
		return defenseEffect;
	}

	public CommandType getCommandType() {
		return CommandType.DEFEND;
	}

	public DefendCommand(String name, Function<Entity, Integer> effect, Function<Float, Float> defenseEffect) {
		this.name = name;
		this.effect = effect;
		this.defenseEffect = defenseEffect;
	}
}
