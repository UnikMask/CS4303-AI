package wolfdungeon3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

import processing.core.PVector;
import wolfdungeon3d.Level.Tile;

public class ComputerController implements EntityController {
	private Entity e;
	private Level level;

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

	public ArrayList<IntTuple> getPath(PVector target) {
		IntTuple startT = new IntTuple(Math.round(e.getPosition().x), Math.round(e.getPosition().y));
		ArrayList<IntTuple> path = new ArrayList<>(Arrays.asList(startT));
		IntTuple targetT = new IntTuple(Math.round(target.x), Math.round(target.y));

		PriorityQueue<ArrayList<IntTuple>> q = new PriorityQueue<>(new Comparator<ArrayList<IntTuple>>() {
			public int compare(ArrayList<IntTuple> o1, ArrayList<IntTuple> o2) {
				return getAScore(o1, targetT) - getAScore(o2, targetT);
			}
		});

		q.add(path);

		int step = 0;
		HashSet<ArrayList<IntTuple>> prevPaths = new HashSet<>();
		while (!q.peek().get(q.peek().size() - 1).equals(targetT) && step < 50000) {
			path = q.poll();
			System.out.println("Path: " + path);
			System.out.println("Current path score: " + getAScore(path, targetT) + ", list size: " + q.size());
			IntTuple currPos = path.get(path.size() - 1);
			HashSet<IntTuple> prevPositions = new HashSet<>(path);
			for (int i = -1; i <= 1; i++) {
				for (int j = -1; j <= 1; j++) {
					IntTuple newPos = new IntTuple(currPos.b + i, currPos.a + j);
					if ((Math.abs(i) > 0 && Math.abs(j) > 0) || prevPositions.contains(newPos)) {
						continue;
					}
					if (level.getTile(newPos.b, newPos.a) == Tile.ROOM) {
						ArrayList<IntTuple> nPath = new ArrayList<>(path);
						nPath.add(newPos);
						if (!prevPaths.contains(nPath)) {
							q.add(nPath);
							prevPaths.add(nPath);
						}
					}
				}
			}
			step += 1;
		}

		return path;
	}

	public int getAScore(ArrayList<IntTuple> path, IntTuple target) {
		return getManhattanDist(new IntTuple(Math.round(e.getPosition().x), Math.round(e.getPosition().y)), target)
				+ getPathCost(path);
	}

	public int getManhattanDist(IntTuple start, IntTuple end) {
		return end.a - start.a + end.b - start.b;
	}

	public int getPathCost(ArrayList<IntTuple> path) {
		return path.stream().mapToInt((p) -> 1).sum();
	}

	/////////////////
	// Constructor //
	/////////////////

	public ComputerController(Entity e, Level level) {
		this.e = e;
		this.level = level;
	}

}
