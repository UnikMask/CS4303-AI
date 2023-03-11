package wolfdungeon3d;

import java.util.Random;

import processing.core.PVector;

public class Room {
	PVector pos, size;
	private boolean isFilled;

	/////////////////////////
	// Getters And Setters //
	/////////////////////////

	public boolean isFilled() {
		return isFilled;
	}

	////////////////////////
	// Generation Methods //
	////////////////////////

	public void generateElements(int[][] grid, Random randomGen) {

	}

	//////////////////
	// Constructors //
	//////////////////

	Room(PVector pos, PVector size, Level owningLevel) {
		this.pos = pos;
		this.size = size;
	}
}
