package wolfdungeon3d;

import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PShape;
import processing.core.PVector;
import wolfdungeon3d.InputSettings.Command;

public class InventoryPage {
	private final static float ITEM_SIZE = 0.125f;
	private static final int BG_LIGHT_C = 0xff403735;
	private static final int BG_C = 0xff2e2a2b;
	private static final int BG_DARK_C = 0xff2a2625;
	private static final int BG_XDARK_C = 0xff1c0908;
	private static final int FG_C = 0xffe9d49c;
	private static final int BG_SELECTED = 0x22ffffff;
	private static final int BG_MOVING = 0x559eb185;
	private static final String NO_ITEM_DESC = "No item hovered.";
	private static final PVector RIBBON_SIZE = new PVector(1f, 0.2f);
	private static final PVector DETAILS_POS = new PVector(0.7f, 0.2f);
	private static final PVector DETAILS_SIZE = new PVector(0.3f, 0.8f);

	// Item Slots
	private InventoryItem[][] itemList;
	private InventoryItem bin;
	private InventoryItem weaponSlot;
	private InventoryItem armorSlot;

	private Inventory inventory;
	private InputSettings inputs = new InputSettings();
	private InventoryPageState state = InventoryPageState.SELECT;
	private IntTuple selectedCoords;
	private IntTuple cursor;
	private PGraphics g;
	private Entity entity;

	static enum InventoryPageState {
		SELECT, MOVE, ATTRIBUTES
	}

	enum ItemKind {
		ALL, WEAPON, ARMOR, CONSUMABLE, BIN
	}

	class InventoryItem {
		private final static float PER_ITEM_GAP = 0.025f;
		private final static float INSIDE_SIZE = 0.05f;
		private final static int MISC_C = FG_C;
		private final static int WEAPON_C = 0xffc22211;
		private final static int ARMOR_C = 0xff9eb185;
		private final static int CONSUMABLE_C = 0xffff771c;

		IntTuple position;
		Item item;
		ItemKind kind;

		public boolean set(Item newItem) {
			boolean accepts = false;
			switch (kind) {
			case BIN:
			case ALL:
				accepts = true;
				break;
			case WEAPON:
				accepts = newItem.isWeapon();
				break;
			case ARMOR:
				accepts = newItem.isArmor();
				break;
			case CONSUMABLE:
				accepts = newItem.isConsumable();
			}
			if (accepts) {
				item = newItem;
			}
			return accepts;
		}

		public void switchItems(InventoryItem target) {
			Item i = target.item;
			if (target.set(item))
				item = i;
		}

		public void draw(boolean hovered, boolean selected, PVector circlePosition) {
			// Draw main slot
			g.pushStyle();
			g.noStroke();
			g.fill(BG_XDARK_C);
			g.circle(circlePosition.x * g.height, circlePosition.y * g.height, ITEM_SIZE * g.height);
			g.popStyle();

			// Draw hovered square
			if (hovered || selected) {
				g.pushStyle();
				g.noStroke();
				g.fill(selected ? BG_MOVING : BG_SELECTED);
				g.rect((circlePosition.x - (ITEM_SIZE + PER_ITEM_GAP) / 2) * g.height,
						(circlePosition.y - (ITEM_SIZE + PER_ITEM_GAP) / 2) * g.height,
						(ITEM_SIZE + PER_ITEM_GAP) * g.height, (ITEM_SIZE + PER_ITEM_GAP) * g.height);
				g.popStyle();
			}

			// Draw item inside
			if (item == null) {
				return;
			}
			int color = item.isWeapon() ? WEAPON_C
					: item.isArmor() ? ARMOR_C : item.isConsumable() ? CONSUMABLE_C : MISC_C;
			g.pushStyle();
			g.noStroke();
			g.fill(color);
			g.circle(circlePosition.x * g.height, circlePosition.y * g.height, INSIDE_SIZE * g.height);
			g.popStyle();

		}

		public void draw(boolean hovered, boolean selected) {
			PVector circlePosition = new PVector(
					(PER_ITEM_GAP + ITEM_SIZE) * (position.a) + PER_ITEM_GAP + ITEM_SIZE / 2,
					RIBBON_SIZE.y + PER_ITEM_GAP + position.b * (PER_ITEM_GAP + ITEM_SIZE) + ITEM_SIZE / 2);
			draw(hovered, selected, circlePosition);
		}

		public InventoryItem(Item item, IntTuple position, ItemKind kind) {
			this.position = position;
			this.item = item;
			this.kind = kind;
		}
	}

	// Public Handling Methods //

	public void keyPressed(Character c) {
		Command comm = inputs.getCommand(c);
		switch (comm) {
		case FORWARD:
			moveCursor(new IntTuple(0, -1));
			break;
		case BACKWARD:
			moveCursor(new IntTuple(0, 1));
			break;
		case LEFT:
			moveCursor(new IntTuple(-1, 0));
			break;
		case RIGHT:
			moveCursor(new IntTuple(1, 0));
			break;
		case INTERACT:
			select(cursor);
			break;
		case CONSUME:
			if (getItem(cursor).item != null && getItem(cursor).item.isConsumable()) {
				entity.addHP(((Consumable) getItem(cursor).item).hpIncrease);
				getItem(cursor).item = null;
			}
		default:
		}
	}

	public void apply() {
		for (int i = 0; i < itemList.length; i++) {
			for (int j = 0; j < itemList[0].length; j++) {
				inventory.set(j, i, itemList[i][j].item);
			}
		}
		entity.setWeapon((Weapon) weaponSlot.item);
		entity.setArmor((Armor) armorSlot.item);
	}

	// Private Methods //

	private void moveCursor(IntTuple dir) {
		if (cursor.b >= 0 && (cursor.b > 0 || dir.b >= 0) && (cursor.b < itemList.length - 1 || dir.b <= 0)) {
			cursor = new IntTuple((itemList[0].length + cursor.a + dir.a) % itemList[0].length, cursor.b + dir.b);
		} else if (cursor.b == -1) {
			cursor = new IntTuple((2 + cursor.a + dir.a) % 2, cursor.b + Math.max(0, dir.b));
		} else if (cursor.b == 0 && dir.b < 0) {
			cursor = new IntTuple(0, -1);
		}
	}

	private void select(IntTuple pos) {
		switch (state) {
		case SELECT:
			selectedCoords = pos;
			state = InventoryPageState.MOVE;
			break;
		case MOVE:
			if (getItem(selectedCoords).item != null) {
				getItem(selectedCoords).switchItems(getItem(pos));
			}
			selectedCoords = null;
			state = InventoryPageState.SELECT;
			break;
		case ATTRIBUTES:
			break;
		}
	}

	private InventoryItem getItem(IntTuple cursor) {
		if (cursor.b >= 0 && cursor.b < itemList.length) {
			return itemList[cursor.b][cursor.a % itemList[0].length];
		} else if (cursor.b < 0) {
			return cursor.a == 0 ? weaponSlot : armorSlot;
		} else {
			return bin;
		}
	}

	private String[] getDescription(InventoryItem i) {
		if (i.item == null) {
			return new String[] { NO_ITEM_DESC };
		} else if (i.item.isWeapon()) {
			return new String[] { i.item.getName(), "Weapon", "Damage: " + ((Weapon) i.item).getDamage() };
		} else if (i.item.isArmor()) {
			return new String[] { i.item.getName(), "Armor", "Resistance: " + ((Armor) i.item).getRes(),
					"Effect: " + ((Armor) i.item).effectType };
		} else if (i.item.isConsumable()) {
			return new String[] { i.item.getName(), "Consumable",
					"Health Resplenished: " + String.format("%.0f", ((Consumable) i.item).hpIncrease) };
		} else {
			return new String[] { i.item.getName(), "This item has no effect.", "It will contribute",
					"to your final score." };
		}
	}

	// Rendering //

	public void draw() {
		// Draw background
		g.pushStyle();
		g.noStroke();
		g.fill(BG_DARK_C);
		g.rect(0, 0, g.width, g.height);
		g.fill(BG_C);
		g.stroke(BG_LIGHT_C);
		g.strokeWeight(0.005f * g.height);
		g.strokeJoin(PConstants.ROUND);
		g.rect(0, 0, RIBBON_SIZE.x * g.width, RIBBON_SIZE.y * g.height);
		g.fill(BG_C);
		g.rect(DETAILS_POS.x * g.width, DETAILS_POS.y * g.height, DETAILS_SIZE.x * g.width, DETAILS_SIZE.y * g.height);
		g.popStyle();

		// Draw instructions
		g.pushStyle();
		g.textSize(0.02f * g.height);
		g.fill(FG_C);
		g.textAlign(PConstants.LEFT, PConstants.CENTER);
		String[] desc = getDescription(getItem(cursor));
		for (int i = 0; i < desc.length; i++) {
			g.text(desc[i], 0.72f * g.width, (0.25f + i * 0.07f) * g.height);
		}
		g.popStyle();

		// Draw items in inventory
		for (InventoryItem[] row : itemList) {
			for (InventoryItem item : row) {
				item.draw(cursor.equals(item.position), selectedCoords != null && selectedCoords.equals(item.position));
			}
		}

		// Print instructions
		if (getItem(cursor).item != null) {
			g.pushStyle();
			g.textSize(g.height * 0.04f);
			g.fill(FG_C);
			g.textAlign(PConstants.LEFT, PConstants.CENTER);
			String instructions = "[E] Move";
			g.text(instructions, 0.02f * g.width, 0.9f * g.height);
			if (getItem(cursor).item.isConsumable()) {
				instructions = "[F] Consume";
				g.text(instructions, 0.42f * g.width, 0.9f * g.height);
			}
			g.popStyle();
		}

		// Draw special items
		PShape shape = g.createShape(PConstants.RECT, 0, 0, ITEM_SIZE * g.height, ITEM_SIZE * g.height);
		weaponSlot.draw(cursor.equals(weaponSlot.position),
				selectedCoords != null && selectedCoords.equals(weaponSlot.position), new PVector(0.25f, 0.1f));
		shape.setTexture(Assets.getSprite("W_Sword008.png"));
		g.shape(shape, 0.01f * g.width, (ITEM_SIZE / 4) * g.height);
		armorSlot.draw(cursor.equals(armorSlot.position),
				selectedCoords != null && selectedCoords.equals(armorSlot.position), new PVector(0.55f, 0.1f));
		shape.setTexture(Assets.getSprite("E_Wood03.png"));
		g.shape(shape, 0.19f * g.width, (ITEM_SIZE / 4) * g.height);
	}

	/**
	 * Constructor for an inventory page
	 *
	 * @param inventory The inventory the page is based on.
	 * @param e         The entity the weapon, armor and magic slots are on.
	 * @param g         The graphical context.
	 */
	public InventoryPage(Inventory inventory, Entity e, PGraphics g) {
		this.inventory = inventory;
		this.itemList = new InventoryItem[inventory.getSize().b][inventory.getSize().a];
		for (int i = 0; i < itemList.length; i++) {
			for (int j = 0; j < itemList[i].length; j++) {
				itemList[i][j] = new InventoryItem(inventory.get(j, i), new IntTuple(j, i), ItemKind.ALL);
			}
		}
		this.entity = e;
		this.cursor = new IntTuple(0, 0);
		this.bin = new InventoryItem(null, new IntTuple(0, inventory.getSize().b), ItemKind.BIN);
		this.weaponSlot = new InventoryItem(e.getWeapon(), new IntTuple(0, -1), ItemKind.WEAPON);
		this.armorSlot = new InventoryItem(e.getArmor(), new IntTuple(1, -1), ItemKind.ARMOR);
		this.g = g;
	}
}
