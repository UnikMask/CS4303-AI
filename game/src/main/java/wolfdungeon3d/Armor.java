package wolfdungeon3d;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import processing.core.PVector;

public class Armor extends Item {
	private static final String ARMOR_SPRITE = "E_Wood03.png";
	private float res;
	Function<Entity, Integer> effects = (e) -> 0;
	String effectType;

	@Override
	public boolean isArmor() {
		return true;
	}

	public float getRes() {
		return res;
	}

	public static Armor getRandomArmor(int floor, int luck, Random randomizer, PVector position) {
		List<Function<Entity, Integer>> effects = getRandomEffects(floor, randomizer);
		int index = randomizer.nextInt(0, effects.size());
		return new Armor(ARMOR_NAMES[randomizer.nextInt(0, ARMOR_NAMES.length)],
				randomizer.nextInt((1 + floor), (1 + floor) * 5), effects.get(index), ARMOR_EFFECTS[index], position);
	}

	public Armor(String name, float res, Function<Entity, Integer> effects, String effectType, PVector position) {
		super(name, ARMOR_SPRITE, position);
		this.effects = effects;
		this.effectType = effectType;
		this.res = res;
	}

	public Armor(String name, float res, Function<Entity, Integer> effects, String effectType) {
		this(name, res, effects, effectType, null);
	}

	// Random armor names
	private static final String[] ARMOR_NAMES = new String[] { "Leather Gloves", "Mightly Helmet", "Iron Chestplate",
			"Wolf's Full Armor" };
	private static final String[] ARMOR_EFFECTS = new String[] { "nothing", "poison", "strength sap" };

	private static List<Function<Entity, Integer>> getRandomEffects(int floor, Random randomizer) {
		int numTurns = randomizer.nextInt(0, 3);
		return Arrays.asList((e) -> {
			return 0;
		}, (e) -> {
			e.takeDamage(floor);
			return numTurns;
		}, (e) -> {
			e.getAttributes().strength -= 1 * ((float) floor + 10) / 10;
			return numTurns;
		});
	}
}
