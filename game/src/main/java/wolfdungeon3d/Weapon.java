package wolfdungeon3d;

import java.util.Random;

import processing.core.PVector;

public class Weapon extends Item {
	private static final String WEAPON_SPRITE = "W_Sword008.png";
	private float damage;
	private float damageScale;
	private int cooldown;
	private boolean isRanged;

	/////////////////////////
	// Getters and Setters //
	/////////////////////////

	public float getDamage() {
		return damage;
	}

	public float getDamageScale() {
		return damageScale;
	}

	public void setDamage(float damage) {
		this.damage = damage;
	}

	public int getCooldown() {
		return cooldown;
	}

	public void setCooldown(int cooldown) {
		this.cooldown = cooldown;
	}

	public boolean isRanged() {
		return isRanged;
	}

	@Override
	public boolean isWeapon() {
		return true;
	}

	//////////////////
	// Constructors //
	//////////////////

	public static Weapon getRandomWeapon(int floor, int luck, Random randomizer, PVector position) {
		return new Weapon(WEAPON_NAMES[randomizer.nextInt(0, WEAPON_NAMES.length)],
				randomizer.nextInt((floor + 1), 2 * (floor + 1)), position);
	}

	public Weapon(String name, float damage, PVector position) {
		super(name, WEAPON_SPRITE, position);
		this.damage = damage;
		this.damageScale = damage / 5.0f;
	}

	public Weapon(String name, float damage) {
		this(name, damage, null);
	}

	// Random Weapons
	private static final String[] WEAPON_NAMES = new String[] { "Wolf's bow", "Adept's Sword", "Wolf's Claws",
			"Eastern Assassin's dagger", "Rusty Axe", "Eastern Hallberd" };
}
