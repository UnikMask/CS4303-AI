package wolfdungeon3d;

import java.util.function.Function;

public class AttackCommand extends CombatCommand {
	private float damage;
	private Entity target;

	public float getDamage() {
		return damage;
	}

	public Entity getTarget() {
		return target;
	}

	public CommandType getCommandType() {
		return CommandType.ATTACK;
	}

	public AttackCommand(String name, float damage, Entity target, Function<Entity, Integer> attackEffect) {
		this.name = name;
		this.damage = damage;
		this.target = target;
		this.effect = attackEffect;
	}
}
