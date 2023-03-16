package wolfdungeon3d;

public class Weapon extends Item {
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

	//////////////////
	// Constructors //
	//////////////////

	public Weapon(String name, Integer price, float damage, int cooldown, boolean isRanged) {
		super(name, price);
		this.damage = damage;
		this.damageScale = damage / 5.0f;
		this.cooldown = cooldown;
		this.isRanged = isRanged;
	}
}
