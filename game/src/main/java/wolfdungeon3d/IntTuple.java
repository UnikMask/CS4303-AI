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
		this.a = (int) vec.x;
		this.b = (int) vec.y;
	}
}
