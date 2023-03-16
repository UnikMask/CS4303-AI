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
			new IntTuple(0, -1), new IntTuple(-1, 0), new IntTuple(1, 1), new IntTuple(1, -1), new IntTuple(-1, -1),
			new IntTuple(-1, 1));
	private static final float AI_VISIBLE_DIST = 3.0f;

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
			// Check for hostility
			e.setHostile(traceRay(game.getPlayer().getPosition()) < AI_VISIBLE_DIST);
			// Check for path to take
			if (e.isHostile()) {
				currentPath = getPath(new IntTuple(game.getPlayer().getPosition()), wasHostile);
				wasHostile = true;
			} else {
				wasHostile = false;
				if (chasingStart) {
					currentPath = getPath(new IntTuple(idleStart), true);
				} else {
					currentPath = getPath(new IntTuple(idleEnd), true);
				}
			}

			// Clean path and switch idle behaviour
			while (!currentPath.isEmpty() && IntTuple.awayBy(e.getPosition(), currentPath.get(0)) <= 0.8f) {
				currentPath.remove(0);
			}
			if (!currentPath.isEmpty()) {
				PVector dir = PVector.sub(new PVector(currentPath.get(0).a + 0.5f, currentPath.get(0).b + 0.5f),
						e.getPosition());
				e.setRotation(dir.heading());
				e.move(new PVector(-dir.mag(), 0));
			} else if (!e.isHostile()) {
				chasingStart = !chasingStart;
			}
		}
	}

	public CombatCommand getCombatTurn(Combat combat) {
		return new AttackCommand("attack", 10, game.getPlayer(), (e) -> 0);
	}

	/////////////////////
	// Private Methods //
	/////////////////////

	private float traceRay(PVector target) {
		PVector dir = PVector.sub(target, e.getPosition()).normalize();
		PVector deltaDist = new PVector(100, 100);
		if (dir.x != 0) {
			deltaDist.x = 1.0f / Math.abs(dir.x);
		}
		if (dir.y != 0) {
			deltaDist.y = 1.0f / Math.abs(dir.y);
		}
		IntTuple step = new IntTuple(1, 1);
		IntTuple mapPos = new IntTuple(e.getPosition());
		PVector sideDist = PVector.sub(PVector.add(new PVector(mapPos.a, mapPos.b), new PVector(1, 1)),
				e.getPosition());
		sideDist = new PVector(sideDist.x * deltaDist.x, sideDist.y * deltaDist.y);

		if (dir.x < 0) {
			step.a = -1;
			sideDist.x = (float) (e.getPosition().x - mapPos.a) * deltaDist.x;
		}
		if (dir.y < 0) {
			step.b = -1;
			sideDist.y = (float) (e.getPosition().y - mapPos.b) * deltaDist.y;
		}
		boolean hit = false;
		while (!hit && !mapPos.equals(new IntTuple(target))) {
			if (sideDist.x < sideDist.y) {
				sideDist.x += deltaDist.x;
				mapPos.a += step.a;
			} else {
				sideDist.y += deltaDist.y;
				mapPos.b += step.b;
			}
			if (game.getLevel().getTile(mapPos.a, mapPos.b) == Tile.WALL) {
				hit = true;
			}
		}
		return hit ? Float.MAX_VALUE : PVector.sub(target, e.getPosition()).mag();
	}

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
		while (!q.isEmpty() && step < 1000) {
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
			step++;
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
