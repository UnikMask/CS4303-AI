package wolfdungeon3d;

import java.util.Random;
import java.util.stream.IntStream;

import processing.core.PVector;

public class Entity {
	private static final int INITIATIVE_DICE = 20;
	private static final float HP_PER_LEVEL = 5;
	private static final int XP_BASE = 128;
	private static final int XP_PER_LEVEL = 32;

	private PVector position;
	private PVector velocity = new PVector();
	private float direction = 0;
	private float hp;
	private int level = 1;
	private int xp = 0;
	Attributes attributes;
	Attributes affectAttributes;

	static class Attributes {
		int strength = 1;
		int endurance = 1;
		int dexterity = 1;
		int luck = 1;
		int focus = 1;
		int intellect = 1;

		public Attributes copy() {
			return new Attributes(strength, endurance, dexterity, luck, focus, intellect);
		}

		public Attributes(int str, int end, int dex, int luc, int foc, int inte) {
			strength = str;
			endurance = end;
			dexterity = dex;
			luck = luc;
			focus = foc;
			intellect = inte;
		}
	}

	/////////////////////////
	// Getters and Setters //
	/////////////////////////

	public float getDirection() {
		return direction;
	}

	public void setDirection(float direction) {
		this.direction = direction;
	}

	public PVector getVelocity() {
		return velocity;
	}

	public void setVelocity(PVector velocity) {
		this.velocity = velocity;
	}

	public PVector getPosition() {
		return position;
	}

	public void setPosition(PVector position) {
		this.position = position;
	}

	public float getMaxHP() {
		return HP_PER_LEVEL * (1 + attributes.endurance);
	}

	public float getHP() {
		return hp;
	}

	public float getXP() {
		return xp;
	}

	public void addXP(Entity e) {
		xp += e.level * XP_PER_LEVEL;
		while (xp / XPToNextLevel() > 1) {
			xp -= XPToNextLevel();
			level++;
		}
	}

	public Attributes getAttributes() {
		return affectAttributes;
	}

	////////////////////
	// Public Methods //
	////////////////////

	public int getNewInitiative(Random randomizer) {
		return randomizer.nextInt() % INITIATIVE_DICE + attributes.dexterity;
	}

	public int XPToNextLevel() {
		return IntStream.range(0, level).map((i) -> ((int) Math.round(Math.pow(2, i))) * XP_BASE).sum();
	}

	public void resetEffects() {
		this.affectAttributes = attributes.copy();
	}

	public void takeDamage(float damage) {
		hp -= damage;
	}

	public void move(PVector dir) {
	}

	//////////////////
	// Constructors //
	//////////////////

	public Entity(PVector position, Attributes attr) {
		this.position = position;
		this.attributes = attr;
		this.affectAttributes = attr.copy();
	}
}
