package wolfdungeon3d;

import processing.core.PImage;
import processing.core.PVector;

public class Item implements Sprite {
	private static final PVector ITEM_SIZE = new PVector(0.3f, 0.3f);
	private static final float ITEM_Z_POS = 0f;
	private String spriteFile;
	private PVector position;
	private String name;

	public String getName() {
		return name;
	}

	public boolean isWeapon() {
		return false;
	}

	public boolean isArmor() {
		return false;
	}

	public boolean isConsumable() {
		return false;
	}

	//////////////////////
	// Inteface Methods //
	//////////////////////

	public PVector getSize() {
		return ITEM_SIZE;
	}

	public PVector getPosition() {
		return new PVector(position.x, position.y, ITEM_Z_POS);
	}

	public PImage getImage() {
		return Assets.getSprite(spriteFile);
	}

	public static Item generateMisc(PVector position) {
		return new Item("Miscallaneous item", "Ac_Ring02.png", position);
	}

	public Item(String name, String sprite) {
		this(name, sprite, null);
	}

	public Item(String name, String sprite, PVector position) {
		this.name = name;
		this.position = position;
		this.spriteFile = sprite;
	}
}
