package wolfdungeon3d;

import processing.core.PVector;

public class Consumable extends Item {
	private static final String POTION_SPRITE = "P_Red07.png";
	private static final String CONSUMABLE_NAME = "potion";
	float hpIncrease;

	@Override
	public boolean isConsumable() {
		return true;
	}

	public Consumable(float hpIncrease, PVector position) {
		super(CONSUMABLE_NAME, POTION_SPRITE, position);
		this.hpIncrease = hpIncrease;
	}
}
