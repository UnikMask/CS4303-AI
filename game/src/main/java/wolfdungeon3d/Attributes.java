package wolfdungeon3d;

import java.util.Random;

public class Attributes {
	int strength = 1;
	int endurance = 1;
	int dexterity = 1;
	int luck = 1;
	int focus = 1;
	int intellect = 1;

	public Attributes copy() {
		return new Attributes(strength, endurance, dexterity, luck, focus, intellect);
	}

	public void scale(int floor, Random randomizer) {
		while (floor > 0) {
			switch (Math.abs(randomizer.nextInt()) % 6) {
			case 0:
				strength++;
				break;
			case 1:
				endurance++;
				break;
			case 2:
				dexterity++;
				break;
			case 3:
				luck++;
				break;
			case 4:
				focus++;
				break;
			case 5:
				intellect++;
			}
		}
	}

	/////////////////////////////
	// Commmon Attribute Kinds //
	/////////////////////////////

	public static Attributes getRandomAttributes(int floor, Random randomizer) {
		Attributes attrs = new Attributes(1, 1, 1, 1, 1, 1);
		attrs.scale(floor, randomizer);
		return attrs;
	}

	//////////////////
	// Constructors //
	//////////////////

	public Attributes(int str, int end, int dex, int luc, int foc, int inte) {
		strength = str;
		endurance = end;
		dexterity = dex;
		luck = luc;
		focus = foc;
		intellect = inte;
	}
}
