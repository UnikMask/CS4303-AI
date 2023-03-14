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
			new IntTuple(0, -1), new IntTuple(-1, 0));
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
		/*
		 * if (game.getState() == GameState.EXPLORE) { // Get next decision if
		 * (!currentPath.isEmpty() && currentPath.get(0).equals(new
		 * IntTuple(e.getPosition()))) { currentPath.remove(0); }
		 *
		 * if (e.isHostile()) { currentPath = getPath(new
		 * IntTuple(game.getPlayer().getPosition()), wasHostile); wasHostile = true; }
		 * else { wasHostile = false; if (chasingStart) { currentPath = getPath(new
		 * IntTuple(idleEnd), false); } else { currentPath = getPath(new
		 * IntTuple(idleStart), false); } } }
		 */
	}

	public void getCombatTurn(Combat combat) {
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
		while (current != start) {
			current = pathMap.get(current);
			path.push(current);
		}
		return new ArrayList<>(path);
	}

	public ArrayList<IntTuple> getPath(IntTuple target, boolean keepPath) {
		IntTuple start = new IntTuple(e.getPosition());
		ArrayList<IntTuple> path = new ArrayList<>();
		if (keepPath && currentPath != null) {
			int nPos = 0;
			int dist = Integer.MAX_VALUE;
			for (int i = 0; i < currentPath.size(); i++) {
				if (getManhattanDist(currentPath.get(i), target) < dist) {
					nPos = i;
					dist = getManhattanDist(currentPath.get(i), target);
				}
			}
			path.addAll(currentPath.subList(0, nPos));
		}

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
				if (game.getLevel().getTile(newNode.a, newNode.b) == Tile.ROOM
						&& getG(next, g) + 1 < getG(newNode, g)) {
					lastNode.put(newNode, next);
					g.put(newNode, getG(next, g) + 1);
					q.add(newNode);
				}
			}
		}

		path.addAll(getPath(q.peek(), start, lastNode));
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
		this.idleStart = b.startPoint;
		this.idleEnd = b.endPoint;
		this.game = game;
	}

}
