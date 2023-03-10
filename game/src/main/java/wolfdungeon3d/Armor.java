package wolfdungeon3d;

public class Armor extends Item {
	private float res;

	public float getRes() {
		return res;
	}

	public Armor(String name, Integer price, float res) {
		super(name, price);
		this.res = res;
	}
}
