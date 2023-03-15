package wolfdungeon3d;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

import processing.core.PVector;
import wolfdungeon3d.Game.GameState;
import wolfdungeon3d.Level.EntityBehaviour;
import wolfdungeon3d.Level.Tile;

public class ComputerController implements EntityController {
	private static final List<IntTuple> neighbours = Arrays.asList(new IntTuple(0, 1), new IntTuple(1, 0),
			new IntTuple(0, -1), new IntTuple(-1, 0), new IntTuple(1, 1), new IntTuple(-1, 1), new IntTuple(-1, -1),
			new IntTuple(1, -1));
	private Entity e;
	private Game game;
	private PVector idleStart;
	private PVector idleEnd;
	private ArrayList<IntTuple> currentPath;
	private boolean wasHostile = false;
	private boolean chasingStart = false;

	/////////////////////////
	// Getters and Setters //
	/////////////////////////

	public Entity getEntity() {
		return e;
	}

	public void setEntity(Entity e) {
		this.e = e;
	}

	///////////////////////
	// Interface Methods //
	///////////////////////

	// Unused
	public void onKeyPressed(Character c) {
		// ignored
	}

	// Unused
	public void onKeyHeld(Character c) {
		// ignored
	}

	// Unused
	public void onKeyReleased(Character c) {
		// ignored
	}

	// Unused
	public void onMouseMove(PVector mouseVelocity) {
		// ignored
	}

	// Unused
	public void onMouseClick(PVector mousePosition) {
		// ignored
	}

	public void update() {
		if (game.getState() == GameState.EXPLORE) { // Get next decision
			currentPath = getPath(new IntTuple(game.getPlayer().getPosition()), true);
			while (!currentPath.isEmpty() && IntTuple.awayBy(e.getPosition(), currentPath.get(0)) <= Math.sqrt(2)) {
				currentPath.remove(0);
			}

			/*
			 * if (e.isHostile()) { currentPath = getPath(new
			 * IntTuple(game.getPlayer().getPosition()), wasHostile); wasHostile = true; }
			 * else { wasHostile = false; if (chasingStart) { currentPath = getPath(new
			 * IntTuple(idleEnd), false); } else { currentPath = getPath(new
			 * IntTuple(idleStart), false); } }
			 */
			if (!currentPath.isEmpty()) {
				PVector dir = PVector.sub(new PVector(currentPath.get(0).a, currentPath.get(0).b), e.getPosition());
				e.setRotation(dir.heading());
				e.move(new PVector(-dir.mag(), 0));
			}
		}
	}

	public CombatCommand getCombatTurn(Combat combat) {
		return null;
	}

	////////////////////
	// Public Methods //
	////////////////////

	/////////////////////
	// Private Methods //
	/////////////////////

	private ArrayList<IntTuple> getPath(IntTuple target, IntTuple start, HashMap<IntTuple, IntTuple> pathMap) {
		ArrayDeque<IntTuple> path = new ArrayDeque<>(Arrays.asList(target));
		IntTuple current = target;
		while (!current.equals(start)) {
			current = pathMap.get(current);
			path.push(current);
		}
		return new ArrayList<>(path);
	}

	public ArrayList<IntTuple> getPath(IntTuple target, boolean keepPath) {
		IntTuple start = new IntTuple(e.getPosition());
		ArrayList<IntTuple> path = new ArrayList<>();
		if (!currentPath.isEmpty() && keepPath) {
			int nPos = 0;
			int dist = Integer.MAX_VALUE;
			for (int i = 0; i < currentPath.size(); i++) {
				int ndist = i + getManhattanDist(currentPath.get(i), target);
				if (ndist < dist) {
					nPos = i;
					dist = ndist;
				}
			}
			path.addAll(currentPath.subList(0, nPos + 1));
			start = currentPath.get(nPos);
		}

		HashMap<IntTuple, Integer> g = new HashMap<>();
		HashMap<IntTuple, IntTuple> lastNode = new HashMap<>();
		g.put(start, 0);

		PriorityQueue<IntTuple> q = new PriorityQueue<>(new Comparator<IntTuple>() {
			public int compare(IntTuple o1, IntTuple o2) {
				int res = (getG(o1, g) + getManhattanDist(o1, target) - getG(o2, g) - getManhattanDist(o2, target));
				if (res == 0) {
					return getManhattanDist(o1, target) - getManhattanDist(o2, target);
				} else {
					return res;
				}
			}
		});
		q.add(start);

		int step = 0;
		for (int z = 0; !q.isEmpty() && step < 1000; step++) {
			IntTuple next = q.poll();
			if (next.equals(target)) {
				path.addAll(getPath(target, start, lastNode));
				return path;
			}
			for (IntTuple neighbour : neighbours) {
				IntTuple newNode = IntTuple.add(next, neighbour);
				if (game.getLevel().getTile(newNode.a, newNode.b) == Tile.ROOM
						&& getG(next, g) + 1 < getG(newNode, g)) {
					lastNode.put(newNode, next);
					g.put(newNode, getG(next, g) + (Math.abs(neighbour.a) + Math.abs(neighbour.b)));
					q.add(newNode);
				}
			}
		}
		return path;
	}

	private int getG(IntTuple o1, HashMap<IntTuple, Integer> g) {
		return g.containsKey(o1) ? g.get(o1) : Integer.MAX_VALUE;
	}

	private int getManhattanDist(IntTuple start, IntTuple end) {
		return Math.abs(end.a - start.a) + Math.abs(end.b - start.b);
	}

	/////////////////
	// Constructor //
	/////////////////

	public ComputerController(EntityBehaviour b, Game game) {
		this.e = b.e;
		this.currentPath = new ArrayList<>();
		this.idleStart = b.startPoint;
		this.idleEnd = b.endPoint;
		this.game = game;
	}

}
