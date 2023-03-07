package wolfdungeon3d;

import processing.core.PVector;

public class Entity {
	private PVector position;
	private PVector velocity = new PVector();
	private float direction = 0;
	Attributes attributes;

	static class Attributes {
		int strength = 1;
		int endurance = 1;
		int dexterity = 1;
		int luck = 1;
		int focus = 1;
		int intellect = 1;

		public Attributes(int str, int end, int dex, int luc, int foc, int inte) {
			strength = str;
			endurance = end;
			dexterity = dex;
			luck = luc;
			focus = foc;
			intellect = inte;
		}
	}

	/////////////////////////
	// Getters and Setters //
	/////////////////////////

	public float getDirection() {
		return direction;
	}

	public void setDirection(float direction) {
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

	public void move(PVector dir) {

	}

	//////////////////
	// Constructors //
	//////////////////

	public Entity(PVector position, Attributes attr) {
		this.position = position;
		this.attributes = attr;
	}
}
