package wolfdungeon3d;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;

import processing.core.PVector;
import wolfdungeon3d.Level.Tile;

public class ComputerController implements EntityController {
	private Entity e;
	private Level level;
	private static final List<IntTuple> neighbours = Arrays.asList(new IntTuple(0, 1), new IntTuple(1, 0),
			new IntTuple(0, -1), new IntTuple(-1, 0));

	/////////////////////////
	// Getters and Setters //
	/////////////////////////

	public Level getLevel() {
		return level;
	}

	public Entity getEntity() {
		return e;
	}

	public void setEntity(Entity e) {
		this.e = e;
	}

	public void setLevel(Level level) {
		this.level = level;
	}

	///////////////////////
	// Interface Methods //
	///////////////////////

	// Unused
	public void onKeyPressed(Character c) {

	}

	// Unused
	public void onKeyHeld(Character c) {

	}

	// Unused
	public void onKeyReleased(Character c) {

	}

	// Unused
	public void onMouseMove(PVector mouseVelocity) {

	}

	// Unused
	public void onMouseClick(PVector mousePosition) {

	}

	public void update() {

	}

	////////////////////
	// Public Methods //
	////////////////////

	private int getG(IntTuple o1, HashMap<IntTuple, Integer> g) {
		return g.containsKey(o1) ? g.get(o1) : Integer.MAX_VALUE;
	}

	private ArrayList<IntTuple> getPath(IntTuple target, IntTuple start, HashMap<IntTuple, IntTuple> pathMap) {
		ArrayDeque<IntTuple> path = new ArrayDeque<>(Arrays.asList(target));
		IntTuple current = target;
		while (current != start) {
			current = pathMap.get(current);
			path.push(current);
		}
		return new ArrayList<>(path);
	}

	public ArrayList<IntTuple> getPath(IntTuple target) {
		IntTuple start = new IntTuple(e.getPosition());
		HashMap<IntTuple, Integer> g = new HashMap<>();
		HashMap<IntTuple, IntTuple> lastNode = new HashMap<>();
		g.put(start, 0);

		PriorityQueue<IntTuple> q = new PriorityQueue<>(new Comparator<IntTuple>() {
			public int compare(IntTuple o1, IntTuple o2) {
				return (getG(o1, g) + getManhattanDist(o1, target) - getG(o2, g) - getManhattanDist(o2, target));
			}
		});
		q.add(start);

		for (int step = 0; !q.isEmpty() && step < 10000; step++) {
			IntTuple next = q.poll();
			if (next.equals(target)) {
				System.out.println("Number of steps: " + step);
				return getPath(target, start, lastNode);
			}
			for (IntTuple neighbour : neighbours) {
				IntTuple newNode = IntTuple.add(neighbour, next);
				if (level.getTile(newNode.a, newNode.b) == Tile.ROOM && getG(next, g) + 1 < getG(newNode, g)) {
					lastNode.put(newNode, next);
					g.put(newNode, getG(next, g) + 1);
					q.add(newNode);
				}
			}
		}

		return getPath(q.peek(), start, lastNode);
	}

	public int getManhattanDist(IntTuple start, IntTuple end) {
		return Math.abs(end.a - start.a) + Math.abs(end.b - start.b);
	}

	/////////////////
	// Constructor //
	/////////////////

	public ComputerController(Entity e, Level level) {
		this.e = e;
		this.level = level;
	}

}
