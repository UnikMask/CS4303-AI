package wolfdungeon3d;

public class Inventory {
	private Item[][] itemGrid;

	/////////////////////////
	// Getters and Setters //
	/////////////////////////

	public IntTuple getSize() {
		return new IntTuple(itemGrid[0].length, itemGrid.length);
	}

	public Item get(int row, int col) {
		if (row >= 0 && col >= 0 && row < itemGrid.length && col < itemGrid[0].length) {
			return itemGrid[col][row];
		} else {
			return null;
		}
	}

	public void set(int row, int col, Item item) {
		if (row >= 0 && col >= 0 && row < itemGrid.length && col < itemGrid[0].length) {
			itemGrid[col][row] = item;
		}
	}

	public boolean add(Item item) {
		for (int i = 0; i < itemGrid.length; i++) {
			for (int j = 0; j < itemGrid[i].length; j++) {
				if (itemGrid[i][j] == null) {
					itemGrid[i][j] = item;
					return true;
				}
			}
		}
		return false;
	}

	public Inventory(int rows, int cols) {
		itemGrid = new Item[rows][cols];
	}
}
