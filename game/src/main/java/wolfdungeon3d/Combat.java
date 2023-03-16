package wolfdungeon3d;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
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
	private boolean ended = false;

	// Message-handling
	private ArrayDeque<String> messages = new ArrayDeque<>();

	/////////////////////////
	// Getters and Setters //
	/////////////////////////

	public boolean hasMessages() {
		return !messages.isEmpty();
	}

	public String getNewMessage() {
		return !messages.isEmpty() ? messages.pollFirst() : null;
	}

	public boolean hasEnded() {
		return ended;
	}

	public Entity getCurrentEntity() {
		return currentEntity;
	}

	public Set<Entity> getAllies(Entity e) {
		return lhs.contains(e) ? rhs : rhs.contains(e) ? lhs : Set.of();
	}

	///////////////////////////
	// Combat Round handling //
	///////////////////////////

	public void nextCommand(CombatCommand command) {
		boolean isLhs = lhs.contains(currentEntity);

		switch (command.getCommandType()) {
		case ATTACK:
			AttackCommand ac = (AttackCommand) command;
			Entity target = ac.getTarget();
			Set<Entity> side = getAllies(target);
			float damage = ac.getDamage();
			if (defenses.containsKey(target)) {
				damage = defenses.get(target).apply(damage);
			}
			float takenDmg = target.takeDamage(damage);
			messages.addLast((isLhs ? "Creature " : "You ") + "hit " + (isLhs ? "you " : "creature ") + "for "
					+ takenDmg + " damage!");
			if (target.getHP() <= 0) {
				messages.addLast((isLhs ? "You " : "Creature ") + "died!");
				side.remove(target);
				for (Entity e : getAllies(currentEntity)) {
					int newXP = e.addXP(target);
					messages.addLast((isLhs ? "Creature " : "You ") + "get " + newXP + " experience points!");
				}
			}
			break;
		case DEFEND:
			DefendCommand dc = (DefendCommand) command;
			defenses.put(currentEntity, dc.getDefenseEffect());
			messages.add((isLhs ? "Creature " : "You ") + "defended " + (isLhs ? "itself!" : "yourself!"));
			break;
		case FLEE:
			break;
		case CAST:
		}

		// End the round.
		endRound(currentEntity);
		do {
			if (!turnCursor.hasNext()) {
				turnCursor = turnOrder.iterator();
			}
			currentEntity = turnCursor.next();
		} while (currentEntity.getHP() <= 0);
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
		if (lhs.isEmpty() || rhs.isEmpty()) {
			ended = true;
		}
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
		messages.addLast("You are being attacked! ");
	}

	/**
	 * Constructor for a combat instance.
	 *
	 * @param lhs Left hand side of the combat - the player
	 * @param rhs The right hand side of the combat - the enemies
	 */
	public Combat(Set<Entity> lhs, Set<Entity> rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
		this.initiativeSeed = new Date().getTime();
		setUpBattle();
	}
}
