package wolfdungeon3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

public class Combat {
	private Set<Entity> lhs;
	private Set<Entity> rhs;
	private long initiativeSeed;
	private HashMap<Entity, HashMap<Function<Entity, Integer>, Integer>> buffs = new HashMap<>();
	private HashMap<Entity, Function<Float, Float>> defenses = new HashMap<>();

	private ArrayList<Entity> turnOrder;
	private Iterator<Entity> turnCursor;
	private Entity currentEntity = null;

	public Entity getCurrentEntity() {
		return currentEntity;
	}

	public Set<Entity> getAllies(Entity e) {
		return lhs.contains(e) ? rhs : rhs.contains(e) ? lhs : Set.of();
	}

	private void endRound(Entity e) {
		// Update components of the current entity in the combat for its own turn.
		if (defenses.containsKey(e)) {
			defenses.remove(e);
		}
		currentEntity.resetEffects();
		ArrayList<Function<Entity, Integer>> removals = new ArrayList<>();
		for (Function<Entity, Integer> effect : buffs.get(e).keySet()) {
			effect.apply(e);
			buffs.get(e).replace(effect, buffs.get(e).get(effect) - 1);
			if (buffs.get(e).get(effect) <= 0) {
				removals.add(effect);
			}
		}
		for (Function<Entity, Integer> effect : removals) {
			buffs.get(e).remove(effect);
		}

	}

	public void nextCommand(CombatCommand command) {

		switch (command.getCommandType()) {
		case ATTACK:
			AttackCommand ac = (AttackCommand) command;
			Entity target = ac.getTarget();
			Set<Entity> side = getAllies(target);
			float damage = ac.getDamage();
			if (defenses.containsKey(target)) {
				damage = defenses.get(target).apply(damage);
			}
			target.takeDamage(damage);
			if (target.getHP() <= 0) {
				side.remove(target);
				for (Entity e : getAllies(currentEntity)) {
					e.addXP(target);
				}
			}
			break;
		case DEFEND:
			DefendCommand dc = (DefendCommand) command;
			defenses.put(currentEntity, dc.getDefenseEffect());
			break;
		case FLEE:
			break;
		case CAST:
			CastCommand cc = (CastCommand) command;
		}
		endRound(currentEntity);
		do {
			if (!turnCursor.hasNext()) {
				turnCursor = turnOrder.iterator();
			}
			currentEntity = turnCursor.next();
		} while (currentEntity.getHP() <= 0);
	}

	private void setUpBattle() {
		ArrayList<Entity> entities = new ArrayList<>() {
			{
				addAll(lhs);
				addAll(rhs);
			}
		};
		entities.sort(new Comparator<Entity>() {
			public int compare(Entity o1, Entity o2) {
				return o1.getNewInitiative(new Random(initiativeSeed))
						- o2.getNewInitiative(new Random(initiativeSeed));
			}
		});
		turnOrder = entities;
		turnCursor = turnOrder.iterator();
		currentEntity = turnCursor.next();

		for (Entity e : entities) {
			buffs.put(e, new HashMap<>());
		}
	}

	public Combat(Set<Entity> lhs, Set<Entity> rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
		this.initiativeSeed = new Date().getTime();
		setUpBattle();
	}
}
