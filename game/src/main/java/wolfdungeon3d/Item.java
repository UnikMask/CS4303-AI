package wolfdungeon3d;

public class Item {
	private String name;
	private Integer price;

	public String getName() {
		return name;
	}

	public boolean isWeapon() {
		return false;
	}

	public boolean isArmor() {
		return false;
	}

	public boolean isSellable() {
		return price != null;
	}

	public int getPrice() {
		return price;
	}

	public Item(String name, Integer price) {
		this.name = name;
		this.price = price;
	}
}
