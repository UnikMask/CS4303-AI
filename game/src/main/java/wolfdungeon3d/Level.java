package wolfdungeon3d;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.common.base.Function;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

public class Level {
	private static final PVector MAX_ROOM_SIZE = new PVector(20, 20);
	private static final PVector MIN_ROOM_SIZE = new PVector(5, 5);
	private static final PVector MIN_DIV_SIZE = new PVector(10, 10);
	private PVector size;
	private int[][] grid;
	private PVector startPosition;
	private List<EntityBehaviour> behaviours;
	private Entity player;

	// Enum for tiles - maps tile to number.
	enum Tile {
		WALL(0, "wall.png", 0), ROOM(1, "floor.png", 0xffffffff), CENTER(2, "ceiling.jpg", 0xff00ff00),
		E_IDLE(3, "", 0xffff0000), E_ATTACK(4, "", 0xffff0000);

		int num;
		String tex;
		int color;

		static Tile getTile(int n) {
			if (n == 0) {
				return WALL;
			} else if (n == 1) {
				return ROOM;
			} else if (n == 2) {
				return CENTER;
			} else if (n == 3) {
				return E_IDLE;
			} else if (n == 4) {
				return E_ATTACK;
			}
			return WALL;
		}

		Tile(int num, String tex, int color) {
			this.num = num;
			this.tex = tex;
			this.color = color;
		}
	}

	class EntityBehaviour {
		Entity e;
		PVector startPoint;
		PVector endPoint;
	}

	/////////////////////////
	// Getters and Setters //
	/////////////////////////

	public PVector getSize() {
		return size;
	}

	public Tile getTile(int x, int y) {
		if (y >= 0 && y < grid.length && x >= 0 && x < grid[0].length) {
			return Tile.getTile(grid[y][x]);
		}
		return Tile.WALL;
	}

	public Tile getTile(int x, int y, int[][] grid) {
		if (y >= 0 && y < grid.length && x >= 0 && x < grid[0].length) {
			return Tile.getTile(grid[y][x]);
		}
		return Tile.WALL;
	}

	public PVector getStartPosition() {
		return startPosition;
	}

	public List<EntityBehaviour> getEntities() {
		return behaviours;
	}

	public void setPlayer(Entity e) {
		this.player = e;
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

		BinaryNode root = ret.generatePartitions(rGenRandom, size);
		ArrayList<Room> rooms = ret.generateRoomsFromPartition(root, rGenRandom);
		ret.generateCorridors(root, rGenRandom);
		Room startingRoom = rooms.get(Math.abs(rGenRandom.nextInt()) % rooms.size());
		ret.startPosition = PVector.add(startingRoom.pos, new PVector(1.5f, 1.5f));
		ret.generateEntities(rGenRandom, rooms, startingRoom);
		return ret;
	}

	////////////////////
	// Public Methods //
	////////////////////

	public PImage getGridImage(PApplet applet) {
		IntTuple size = new IntTuple(grid[0].length, grid.length);
		PImage image = applet.createImage(size.a, size.b, PApplet.RGB);
		image.loadPixels();
		int[][] finalGrid = getGridWithItems();
		for (int i = 0; i < image.pixels.length; i++) {
			image.pixels[i] = getTile(i % size.a, i / size.a, finalGrid).color;
		}
		image.updatePixels();
		return image;
	}

	public int[][] getGridWithItems() {
		int[][] retGrid = new int[grid.length][];
		for (int i = 0; i < grid.length; i++) {
			retGrid[i] = Arrays.copyOf(grid[i], grid[i].length);
		}
		for (Entity e : behaviours.stream().map((b) -> b.e).collect(Collectors.toList())) {
			if (!e.equals(player)) {
				IntTuple pos = new IntTuple(e.getPosition());
				System.out.println(e.getPosition());
				retGrid[pos.b][pos.a] = e.isHostile() ? Tile.E_ATTACK.num : Tile.E_IDLE.num;
			}
		}
		return retGrid;
	}

	/////////////////////
	// Private Methods //
	/////////////////////

	private BinaryNode generatePartitions(Random randomizer, PVector size) {
		BinaryNode root = new BinaryNode(null, new PVector(0, 0), new PVector(size.x, size.y));
		ArrayDeque<BinaryNode> q = new ArrayDeque<>(Arrays.asList(root));
		while (!q.isEmpty()) {
			BinaryNode next = q.pollFirst();
			if (!(next.size.x > MIN_DIV_SIZE.x && next.size.y > MIN_DIV_SIZE.y)) {
				continue;
			} else if (!randomizer.nextBoolean()
					&& (next.size.x <= MAX_ROOM_SIZE.x && next.size.y <= MAX_ROOM_SIZE.y)) {
				continue;
			}

			boolean vSplit;
			if (next.size.x < MAX_ROOM_SIZE.x) {
				vSplit = false;
			} else if (next.size.y < MAX_ROOM_SIZE.y) {
				vSplit = true;
			} else {
				vSplit = randomizer.nextBoolean();
			}

			float ratio = randomizer.nextFloat();
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
		return root;
	}

	private ArrayList<Room> generateRoomsFromPartition(BinaryNode rootNode, Random randomizer) {
		// 2. Create rooms for each leaf nodes
		ArrayDeque<BinaryNode> roomStack = new ArrayDeque<>(Arrays.asList(rootNode));
		ArrayList<Room> rooms = new ArrayList<>();
		while (!roomStack.isEmpty()) {
			BinaryNode next = roomStack.pollFirst();
			if (!next.isLeaf()) {
				roomStack.addFirst(next.r);
				roomStack.addFirst(next.l);
			} else {
				// Create a room and reduce leaf size and bounds to room
				int roomSizeX = Math.round(
						MIN_ROOM_SIZE.x + (0.5f + randomizer.nextFloat() / 2f) * (next.size.x - MIN_ROOM_SIZE.x));
				int roomSizeY = Math.round(
						MIN_ROOM_SIZE.y + (0.5f + randomizer.nextFloat() / 2f) * (next.size.y - MIN_ROOM_SIZE.y));

				int offsetX = Math.round(next.boundMin.x + randomizer.nextFloat() * ((float) next.size.x - roomSizeX));
				int offsetY = Math.round(next.boundMin.y + randomizer.nextFloat() * ((float) next.size.y - roomSizeY));
				for (int i = 1; i < roomSizeY - 1; i++) {
					for (int j = 1; j < roomSizeX - 1; j++) {
						grid[i + offsetY][j + offsetX] = Tile.ROOM.num;
					}
				}
				PVector newBoundMin = new PVector(offsetX + 1, offsetY + 1);
				next.setRoomBounds(newBoundMin, PVector.add(newBoundMin, new PVector(roomSizeX, roomSizeY)));
				rooms.add(new Room(newBoundMin, new PVector(roomSizeX, roomSizeY), this));
			}
		}
		return rooms;
	}

	private void generateCorridors(BinaryNode rootNode, Random randomizer) {
		ArrayDeque<BinaryNode> corridorStack = new ArrayDeque<>(Arrays.asList(rootNode));
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
			while (!connect.isLeaf()) {
				connect = connect.r;
			}
			ptL = connect.getRoomMiddle();

			connect = next.r;
			while (!connect.isLeaf()) {
				connect = next.vSplit == connect.vSplit ? connect.l : connect.r;
			}
			ptR = connect.getRoomMiddle();

			// Draw corridor
			PVector midPoint = this.drawCorridor(ptL, ptR, connect.vSplit);

			// Set current node as corridored
			PVector corridorCenter = next.vSplit ? new PVector(next.getNodeMiddle().x, midPoint.y)
					: new PVector(midPoint.x, next.getNodeMiddle().y);
			next.roomBoundMax = corridorCenter;
			next.roomBoundMin = corridorCenter;
			next.corridored = true;
		}

	}

	private PVector drawCorridor(PVector ptA, PVector ptB, boolean xFirst) {
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

	private void generateEntities(Random randomizer, List<Room> rooms, Room playerRoom) {
		behaviours = new ArrayList<>();
		for (Room room : rooms.stream().filter((r) -> r != playerRoom).collect(Collectors.toList())) {
			EntityBehaviour behaviour = new EntityBehaviour();
			boolean xdir = randomizer.nextBoolean();
			if (xdir) {
				behaviour.startPoint = new PVector(1, Math.abs(randomizer.nextInt()) % (room.size.y - 1));
				behaviour.endPoint = new PVector(Math.abs(randomizer.nextInt()) % (room.size.y - 1) - 1,
						behaviour.startPoint.y);
			} else {
				behaviour.startPoint = new PVector(Math.abs(randomizer.nextInt()) % (room.size.x - 1), 1);
				behaviour.endPoint = new PVector(behaviour.startPoint.x,
						Math.abs(randomizer.nextInt()) % (room.size.y - 1) - 1);
			}
			behaviour.e = new Entity(behaviour.startPoint, new Entity.Attributes(1, 1, 1, 1, 1, 1));
			behaviours.add(behaviour);
		}
	}

	//////////////////////
	// Object Overrides //
	//////////////////////

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		int[][] finalGrid = getGridWithItems();
		for (int[] row : finalGrid) {
			for (int cell : row) {
				sb.append(cell == 1 ? "  " : cell == 2 ? "CC" : cell == 3 ? "EE" : "██");
			}
			sb.append('\n');
		}
		return sb.toString();
	}

	public String stringWithSpecials(IntTuple... specials) {
		StringBuilder sb = new StringBuilder();
		int[][] grid = getGridWithItems();
		for (int i = 0; i < grid.length; i++) {
			loop: for (int j = 0; j < grid[i].length; j++) {
				for (int k = 0; k < specials.length; k++) {
					if (i == specials[k].b && j == specials[k].a) {
						sb.append(k == 0 ? "11" : k == 1 ? "22" : "33");
						continue loop;
					}
				}
				sb.append(grid[i][j] == 1 ? "  " : grid[i][j] == 2 ? "CC" : grid[i][j] == 3 ? "EE" : "██");
			}
			sb.append('\n');
		}
		return sb.toString();
	}

	//////////////////
	// Constructors //
	//////////////////

	public Level(PVector size) {
		this.size = size;
		grid = new int[Math.round(size.y)][Math.round(size.x)];
	}
}
