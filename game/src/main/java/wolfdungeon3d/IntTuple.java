package wolfdungeon3d;

import processing.core.PVector;

public class IntTuple {
	int a, b;

	public static IntTuple add(IntTuple a, IntTuple b) {
		return new IntTuple(a.a + b.a, a.b + b.b);
	}

	public static IntTuple sub(IntTuple a, IntTuple b) {
		return new IntTuple(a.a - b.a, a.b - b.b);
	}

	public static float awayBy(PVector pos, IntTuple target) {
		PVector targetReal = new PVector(target.a + 0.5f, target.b + 0.5f);
		return Math.abs(PVector.sub(pos, targetReal).mag());
	}

	@Override
	public int hashCode() {
		return Integer.hashCode(a) + Integer.hashCode(b) * 65535;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof IntTuple)) {
			return false;
		} else {
			IntTuple t = (IntTuple) o;
			return t.a == this.a && t.b == this.b;
		}
	}

	@Override
	public String toString() {
		return "(" + a + ", " + b + ")";
	}

	public IntTuple(int a, int b) {
		this.a = a;
		this.b = b;
	}

	public IntTuple(PVector vec) {
		this(vec, false);
	}

	public IntTuple(PVector vec, boolean round) {
		if (round) {
			this.a = Math.round(vec.x);
			this.b = Math.round(vec.y);
		} else {
			this.a = (int) vec.x;
			this.b = (int) vec.y;
		}
	}
}
