package wolfdungeon3d;

import processing.core.PGraphics;
import processing.core.PVector;

public class InventoryPage {
	private static final int BG_LIGHT_C = 0xff403735;
	private static final int BG_C = 0xff2e2a2b;
	private static final int BG_DARK_C = 0xff2a2625;
	private static final int BG_XDARK_C = 0xff1c0908;
	private static final int FG_C = 0xffe9d49c;
	private static final PVector RIBBON_SIZE = new PVector(0.7f, 0.2f);
	private static final PVector DETAILS_POS = new PVector(0.7f, 0);
	private static final PVector DETAILS_SIZE = new PVector(0.3f, 1);

	private IntTuple cursor;
	private InventoryItem[][] itemList;
	private PGraphics g;
	private Entity entity;

	enum ItemKind {
		ALL, WEAPON, ARMOR, MAGIC, BIN
	}

	class InventoryItem {
		private final static float PER_ITEM_GAP = 0.05f;
		private final static float SIZE = 0.1f;
		private final static float INSIDE_SIZE = 0.05f;
		private final static int MISC_C = FG_C;
		private final static int WEAPON_C = 0xffc22211;
		private final static int ARMOR_C = 0xff9eb185;
		private final static int MAGIC_C = 0xffff771c;

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
			case MAGIC:
				accepts = newItem.isMagic();
			}
			if (accepts) {
				item = newItem;
			}
			return accepts;
		}

		public void switchItems(InventoryItem target) {
			Item i = target.item;
			target.item = item;
			item = i;
		}

		public void draw(boolean hovered) {
			PVector circlePosition = new PVector((PER_ITEM_GAP + SIZE) * (position.a) + PER_ITEM_GAP + SIZE / 2,
					RIBBON_SIZE.y + PER_ITEM_GAP + position.b * (PER_ITEM_GAP + SIZE) + SIZE / 2);
			// Draw hovered square
			if (hovered) {

			}
			// Draw main slot
			g.pushStyle();
			g.noStroke();
			g.fill(BG_XDARK_C);
			g.circle(circlePosition.x * g.height, circlePosition.y, SIZE * g.height);
			g.popStyle();

			// Draw item inside
			if (item == null) {
				return;
			}
			int color = item.isWeapon() ? WEAPON_C : item.isArmor() ? ARMOR_C : item.isMagic() ? MAGIC_C : MISC_C;
			g.pushStyle();
			g.noStroke();
			g.circle(circlePosition.x * g.height, circlePosition.y, INSIDE_SIZE * g.height);
			g.fill(color);
			g.popStyle();
		}

		public InventoryItem(Item item, IntTuple position, ItemKind kind) {
			this.position = position;
			this.item = item;
			this.kind = kind;
		}
	}

	public void draw() {
		// Draw background
		g.pushStyle();
		g.noStroke();
		g.fill(BG_DARK_C);
		g.rect(0, 0, g.width, g.height);
		g.fill(BG_C);
		g.rect(0, 0, RIBBON_SIZE.x * g.width, RIBBON_SIZE.y * g.height);
		g.fill(BG_LIGHT_C);
		g.rect(DETAILS_POS.x * g.width, DETAILS_POS.y * g.height, DETAILS_SIZE.x * g.width, DETAILS_SIZE.y * g.height);
		g.popStyle();

		// Draw items in inventory
		for (InventoryItem[] row : itemList) {
			for (InventoryItem item : row) {
				item.draw(cursor.equals(item.position));
			}
		}
	}

	public InventoryPage(Inventory inventory, Entity e, PGraphics g) {
		this.itemList = new InventoryItem[inventory.getSize().b][inventory.getSize().a];
		for (int i = 0; i < itemList.length; i++) {
			for (int j = 0; j < itemList[i].length; j++) {
				itemList[i][j] = new InventoryItem(inventory.get(j, i), new IntTuple(j, i), ItemKind.ALL);
			}
		}
		this.entity = e;
		this.cursor = new IntTuple(0, 0);
		this.g = g;
	}
}
