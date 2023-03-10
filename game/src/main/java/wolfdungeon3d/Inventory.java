package wolfdungeon3d;

public class Inventory {
	private Item[][] itemGrid;

	public Item get(int row, int col) {
		if (row >= 0 && col >= 0 && row < itemGrid.length && col < itemGrid[0].length) {
			return itemGrid[row][col];
		} else {
			return null;
		}
	}

	public Inventory(int rows, int cols) {
		itemGrid = new Item[rows][cols];
	}
}
