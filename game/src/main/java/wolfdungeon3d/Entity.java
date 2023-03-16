package wolfdungeon3d;

import java.util.Random;
import java.util.stream.IntStream;

import processing.core.PVector;
import processing.core.PImage;

public class Entity implements Sprite {
	private static final int INITIATIVE_DICE = 20;
	private static final float HP_PER_LEVEL = 5;
	private static final int XP_BASE = 128;
	private static final int XP_PER_LEVEL = 32;
	private static final float BASE_ENTITY_DMG = 5.0f;
	private static final float DMG_PER_STR = 1;
	private static final float BASE_RES = 2;

	private String name;
	private PImage tex;
	private PVector position;
	private PVector velocity = new PVector();
	private float direction = 0;
	private float hp;
	private int level = 1;
	private int xp = 0;
	private boolean hostile = false;
	private PVector size;

	private Weapon weapon;
	private Armor armor;
	Attributes attributes;
	Attributes affectAttributes;

	/////////////////////////
	// Getters and Setters //
	/////////////////////////

	public float getRotation() {
		return direction;
	}

	public void setRotation(float direction) {
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

	public int addXP(Entity e) {
		int extraXp = e.level * XP_PER_LEVEL;
		xp += extraXp;
		while (xp / XPToNextLevel() >= 1) {
			xp -= XPToNextLevel();
			level++;
		}
		return extraXp;
	}

	public Attributes getAttributes() {
		return affectAttributes;
	}

	public boolean isHostile() {
		return hostile;
	}

	public void setHostile(boolean hostility) {
		this.hostile = hostility;
	}

	public PImage getImage() {
		return tex;
	}

	public int getLevel() {
		return level;
	}

	public String getName() {
		return name;
	}

	public Weapon getWeapon() {
		return weapon;
	}

	public Armor getArmor() {
		return armor;
	}

	@Override
	public PVector getSize() {
		return size;
	}

	////////////////////
	// Combat Methods //
	////////////////////

	public float getDamage() {
		return Math.max(BASE_ENTITY_DMG, weapon != null ? weapon.getDamage() : 0)
				+ (DMG_PER_STR * attributes.strength * (weapon != null ? weapon.getDamageScale() : 1));
	}

	public float getResistance() {
		return Math.max(BASE_RES, armor != null ? armor.getRes() : 0) * (1 + ((float) attributes.endurance)) / 5.0f;
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

	public float takeDamage(float damage) {
		hp -= damage;
		return damage;
	}

	public void move(PVector dir) {
		PVector ndir = dir.copy().normalize();
		ndir.rotate(direction);
		velocity = PVector.add(velocity, ndir);
	}

	//////////////////
	// Constructors //
	//////////////////

	public Entity(String name, PVector position, PVector size, PImage tex, Attributes attr) {
		this.position = position;
		this.size = size;
		this.name = name;
		this.tex = tex;
		this.attributes = attr;
		this.affectAttributes = attr.copy();
		this.hp = this.getMaxHP();
	}
}
