package wolfdungeon3d;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Consumer;

import com.google.common.base.Function;

import processing.core.PVector;

public class Level {
	private static final PVector MAX_ROOM_SIZE = new PVector(20, 20);
	private static final PVector MIN_ROOM_SIZE = new PVector(5, 5);
	private static final PVector MIN_DIV_SIZE = new PVector(10, 10);
	private PVector size;
	private int[][] grid;

	// Enum for tiles - maps tile to number.
	enum Tile {
		WALL(0), ROOM(1), CENTER(2);

		int num;

		Tile(int num) {
			this.num = num;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int[] row : grid) {
			for (int cell : row) {
				sb.append(cell == 1 ? "  " : cell == 2 ? "CC" : "██");
			}
			sb.append('\n');
		}
		return sb.toString();
	}

	//////////////////////
	// Static Functions //
	//////////////////////

	static class BinaryNode {
		boolean vSplit = false;
		BinaryNode l, r;
		BinaryNode root;
		boolean corridored = false;
		PVector boundMin;
		PVector boundMax;
		PVector size;
		PVector roomBoundMin;
		PVector roomBoundMax;

		boolean isLeaf() {
			return l == null && r == null;
		}

		boolean isRoomed() {
			return roomBoundMax != null && roomBoundMin != null;
		}

		PVector getRoomMiddle() {
			return PVector.div(PVector.add(roomBoundMin, roomBoundMax), 2);
		}

		PVector getNodeMiddle() {
			return PVector.div(PVector.add(boundMin, boundMax), 2);
		}

		void setRoomBounds(PVector min, PVector max) {
			roomBoundMin = min;
			roomBoundMax = max;
			corridored = true;
		}

		BinaryNode(BinaryNode root, PVector boundMin, PVector boundMax) {
			this.root = root;
			this.boundMin = boundMin;
			this.boundMax = boundMax;
			this.size = PVector.sub(boundMax, boundMin);
		}
	}

	public static Level generate(PVector size, long seed) {
		Level ret = new Level(size);

		Random rGenRandom = new Random(seed);
		BinaryNode root = new BinaryNode(null, new PVector(0, 0), new PVector(size.x, size.y));

		// 1. Split until all nodes are lower than max.
		ArrayDeque<BinaryNode> q = new ArrayDeque<>(Arrays.asList(root));
		while (!q.isEmpty()) {
			BinaryNode next = q.pollFirst();
			if (!(next.size.x > MIN_DIV_SIZE.x && next.size.y > MIN_DIV_SIZE.y)) {
				continue;
			} else if (!rGenRandom.nextBoolean()
					&& (next.size.x <= MAX_ROOM_SIZE.x && next.size.y <= MAX_ROOM_SIZE.y)) {
				continue;
			}

			boolean vSplit;
			if (next.size.x < MAX_ROOM_SIZE.x) {
				vSplit = false;
			} else if (next.size.y < MAX_ROOM_SIZE.y) {
				vSplit = true;
			} else {
				vSplit = rGenRandom.nextBoolean();
			}

			float ratio = rGenRandom.nextFloat();
			if (vSplit) {
				PVector splitSize = new PVector(Math.min(ratio * (next.size.x - MIN_ROOM_SIZE.x) + MIN_ROOM_SIZE.x,
						next.size.x - MIN_ROOM_SIZE.x), 0);
				next.l = new BinaryNode(next, next.boundMin,
						PVector.add(new PVector(next.boundMin.x, next.boundMax.y), splitSize));
				next.r = new BinaryNode(next, PVector.add(next.boundMin, splitSize), next.boundMax);
			} else {
				PVector splitSize = new PVector(0, Math.min(ratio * (next.size.y - MIN_ROOM_SIZE.y) + MIN_ROOM_SIZE.x,
						next.size.y - MIN_ROOM_SIZE.y));
				next.l = new BinaryNode(next, next.boundMin,
						PVector.add(new PVector(next.boundMax.x, next.boundMin.y), splitSize));
				next.r = new BinaryNode(next, PVector.add(next.boundMin, splitSize), next.boundMax);
			}
			next.vSplit = vSplit;
			q.addLast(next.l);
			q.addLast(next.r);
		}

		// 2. Create rooms for each leaf nodes
		ArrayDeque<BinaryNode> roomStack = new ArrayDeque<>(Arrays.asList(root));
		while (!roomStack.isEmpty()) {
			BinaryNode next = roomStack.pollFirst();
			if (!next.isLeaf()) {
				roomStack.addFirst(next.r);
				roomStack.addFirst(next.l);
			} else {
				// Create a room and reduce leaf size and bounds to room
				int roomSizeX = Math.round(
						MIN_ROOM_SIZE.x + (0.5f + rGenRandom.nextFloat() / 2f) * (next.size.x - MIN_ROOM_SIZE.x));
				int roomSizeY = Math.round(
						MIN_ROOM_SIZE.y + (0.5f + rGenRandom.nextFloat() / 2f) * (next.size.y - MIN_ROOM_SIZE.y));

				int offsetX = Math.round(next.boundMin.x + rGenRandom.nextFloat() * ((float) next.size.x - roomSizeX));
				int offsetY = Math.round(next.boundMin.y + rGenRandom.nextFloat() * ((float) next.size.y - roomSizeY));
				for (int i = 1; i < roomSizeY - 1; i++) {
					for (int j = 1; j < roomSizeX - 1; j++) {
						ret.grid[i + offsetY][j + offsetX] = Tile.ROOM.num;
					}
				}
				PVector newBoundMin = new PVector(offsetX + 1, offsetY + 1);
				next.setRoomBounds(newBoundMin, PVector.add(newBoundMin, new PVector(roomSizeX, roomSizeY)));
			}
		}

		// 3. Link rooms
		ArrayDeque<BinaryNode> corridorStack = new ArrayDeque<>(Arrays.asList(root));
		while (!corridorStack.isEmpty()) {
			BinaryNode next = corridorStack.pollFirst();
			if (next.isLeaf()) {
				continue;
			} else if (!next.l.corridored || !next.r.corridored) {
				corridorStack.addFirst(next);
				corridorStack.addFirst(next.r);
				corridorStack.addFirst(next.l);
				continue;
			}

			PVector ptL, ptR;
			BinaryNode connect = next.l;
			while (!connect.isLeaf() && next.vSplit == connect.vSplit) {
				connect = connect.l;
			}
			ptL = connect.getRoomMiddle();

			connect = next.r;
			while (!connect.isLeaf() && next.vSplit == connect.vSplit) {
				connect = connect.r;
			}
			ptR = connect.getRoomMiddle();

			// Draw corridor
			PVector midPoint = drawCorridor(ret.grid, ptL, ptR, next.vSplit);

			// Set current node as corridored
			PVector corridorCenter = next.vSplit ? new PVector(next.getNodeMiddle().x, midPoint.y)
					: new PVector(midPoint.x, next.getNodeMiddle().y);
			next.roomBoundMax = corridorCenter;
			next.roomBoundMin = corridorCenter;
			next.corridored = true;
		}

		return ret;
	}

	static PVector drawCorridor(int[][] grid, PVector ptA, PVector ptB, boolean xFirst) {
		Function<PVector, Consumer<PVector>> xLoop = (start) -> (end) -> {
			int dist = (int) (end.x - start.x);
			if (dist != 0) {
				for (int i = (int) (start.x); i != (int) (end.x); i += (dist > 0 ? 1 : -1)) {
					grid[(int) (start.y)][i] = Tile.ROOM.num;
				}
			}
		};
		Function<PVector, Consumer<PVector>> yLoop = (start) -> (end) -> {
			int dist = Math.round(end.y - start.y);
			if (dist != 0) {
				for (int i = (int) (start.y); i != (int) (end.y); i += (dist > 0 ? 1 : -1)) {
					grid[i][(int) (start.x)] = Tile.ROOM.num;
				}
			}
		};
		if (xFirst) {
			PVector midPoint = new PVector(ptB.x, ptA.y);
			xLoop.apply(ptA).accept(midPoint);
			yLoop.apply(midPoint).accept(ptB);
			return midPoint;
		} else {
			PVector midPoint = new PVector(ptA.x, ptB.y);
			yLoop.apply(ptA).accept(midPoint);
			xLoop.apply(midPoint).accept(ptB);
			return midPoint;
		}
	}

	//////////////////
	// Constructors //
	//////////////////

	public Level(PVector size) {
		this.size = size;
		grid = new int[Math.round(size.y)][Math.round(size.x)];
	}
}
