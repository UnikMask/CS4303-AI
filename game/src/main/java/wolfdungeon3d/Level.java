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
	private static final IntTuple MAX_ROOM_SIZE = new IntTuple(20, 20);
	private static final IntTuple MIN_ROOM_SIZE = new IntTuple(5, 5);
	private static final IntTuple MIN_DIV_SIZE = new IntTuple(10, 10);
	private static final String ENEMY_SPRITE = "sphere.png";
	private PVector size;
	private List<String> textures = Arrays.asList("wall.png", "floor.png", "ceiling.jpg", "bonewall.png");
	private int[][] grid;
	private PVector startPosition;
	private List<EntityBehaviour> behaviours;

	// Enum for tiles - maps tile to number.
	enum Tile {
		WALL(0, 0, "██"), ROOM(1, 0xffffffff, "  "), CENTER(2, 0xff00ff00, "CC"), END(3, 0xffff0000, "FF");

		int num;
		int color;
		String print;

		static Tile getTile(int n) {
			if (n == 0) {
				return WALL;
			} else if (n == 1) {
				return ROOM;
			} else if (n == 2) {
				return CENTER;
			} else if (n == 3) {
				return END;
			}
			return WALL;
		}

		Tile(int num, int color, String print) {
			this.num = num;
			this.color = color;
			this.print = print;
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

	public List<String> getLevelTextures() {
		return textures;
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

	/**
	 * Generate a new game level based on a seed.
	 *
	 * @param size  The size of the level to generate.
	 * @param floor The floor it is on. Affects mob difficulty.
	 * @param seed  The random seed to use.
	 */
	public static Level generate(PVector size, int floor, long seed) {
		Level ret = new Level(size);

		Random rGenRandom = new Random(seed);

		// Generate dungeon using BSP
		BinaryNode root = ret.generatePartitions(rGenRandom, size);
		ArrayList<Room> rooms = ret.generateRoomsFromPartition(root, rGenRandom);
		ret.generateCorridors(root, rGenRandom);

		// Get a starting room and a player
		Room startingRoom = rooms.get(Math.abs(rGenRandom.nextInt()) % rooms.size());
		ret.startPosition = PVector.add(startingRoom.pos, new PVector(1.5f, 1.5f, 0.35f));
		ret.generateEntities(rGenRandom, floor, rooms, startingRoom);

		// Get and ending room
		Room endRoom = rooms.get(Math.abs(rGenRandom.nextInt()) % rooms.size());
		IntTuple endLocation = IntTuple.add(new IntTuple(endRoom.pos), new IntTuple(
				rGenRandom.nextInt(0, (int) endRoom.size.x - 1), rGenRandom.nextInt(0, (int) endRoom.size.y - 1)));
		ret.grid[endLocation.b][endLocation.a] = Tile.END.num;
		return ret;
	}

	////////////////////
	// Public Methods //
	////////////////////

	public PImage getGridImage(PApplet applet) {
		IntTuple size = new IntTuple(grid[0].length, grid.length);
		PImage image = applet.createImage(size.a, size.b, PApplet.RGB);
		image.loadPixels();
		for (int i = 0; i < image.pixels.length; i++) {
			image.pixels[i] = getTile(i % size.a, i / size.a, grid).color;
		}
		image.updatePixels();
		return image;
	}

	/////////////////////
	// Private Methods //
	/////////////////////

	// Generate partitions via Binary Space Partitioning split of the grid
	// until a satisfactory number of room splits are made.
	private BinaryNode generatePartitions(Random randomizer, PVector size) {
		BinaryNode root = new BinaryNode(null, new PVector(0, 0), new PVector(size.x, size.y));
		ArrayDeque<BinaryNode> q = new ArrayDeque<>(Arrays.asList(root));
		while (!q.isEmpty()) {
			BinaryNode next = q.pollFirst();
			if (!(next.size.x > MIN_DIV_SIZE.a && next.size.y > MIN_DIV_SIZE.b)) {
				continue;
			} else if (!randomizer.nextBoolean()
					&& (next.size.x <= MAX_ROOM_SIZE.a && next.size.y <= MAX_ROOM_SIZE.b)) {
				continue;
			}

			boolean vSplit;
			if (next.size.x < MAX_ROOM_SIZE.a) {
				vSplit = false;
			} else if (next.size.y < MAX_ROOM_SIZE.b) {
				vSplit = true;
			} else {
				vSplit = randomizer.nextBoolean();
			}

			if (vSplit) {
				int split = randomizer.nextInt(MIN_ROOM_SIZE.a, (int) next.size.x - MIN_ROOM_SIZE.a + 1);
				next.l = new BinaryNode(next, next.boundMin, new PVector(next.boundMin.x + split, next.boundMax.y));
				next.r = new BinaryNode(next, new PVector(next.boundMin.x + split, next.boundMin.y), next.boundMax);
			} else {
				int split = randomizer.nextInt(MIN_ROOM_SIZE.b, (int) next.size.y - MIN_ROOM_SIZE.b + 1);
				next.l = new BinaryNode(next, next.boundMin, new PVector(next.boundMax.x, next.boundMin.y + split));
				next.r = new BinaryNode(next, new PVector(next.boundMin.x, next.boundMin.y + split), next.boundMax);
			}
			next.vSplit = vSplit;
			q.addLast(next.l);
			q.addLast(next.r);
		}
		return root;
	}

	// Generate a set of rooms based on a tree of grid BSP splits.
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
				int roomSizeX = MIN_ROOM_SIZE.a + ((int) next.size.x - MIN_ROOM_SIZE.a) / 2
						+ (((int) next.size.x - MIN_ROOM_SIZE.a) / 2 > 0
								? randomizer.nextInt(0, ((int) next.size.x - MIN_ROOM_SIZE.a)) / 2
								: 0)
						- 2;
				int roomSizeY = MIN_ROOM_SIZE.b + ((int) next.size.y - MIN_ROOM_SIZE.b) / 2
						+ (((int) next.size.y - MIN_ROOM_SIZE.b) / 2 > 0
								? randomizer.nextInt(0, ((int) next.size.y - MIN_ROOM_SIZE.b)) / 2
								: 0)
						- 2;

				int offsetX = (int) next.boundMin.x + randomizer.nextInt(0, (int) next.size.x - roomSizeX - 1);
				int offsetY = (int) next.boundMin.y + randomizer.nextInt(0, (int) next.size.y - roomSizeY - 1);
				for (int i = 1; i <= roomSizeY; i++) {
					for (int j = 1; j <= roomSizeX; j++) {
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

	// Generate corridors between rooms on a grid.
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

	// Draw a corridor betweeen 2 points.
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

	// Generate entities on each rooms except the starting room.
	private void generateEntities(Random randomizer, int floor, List<Room> rooms, Room playerRoom) {
		behaviours = new ArrayList<>();
		for (Room room : rooms.stream().filter((r) -> r != playerRoom).collect(Collectors.toList())) {
			EntityBehaviour behaviour = new EntityBehaviour();

			boolean xdir = randomizer.nextBoolean();
			if (xdir) {
				behaviour.startPoint = new PVector(room.pos.x, room.pos.y + randomizer.nextInt(0, (int) room.size.y));
				behaviour.endPoint = new PVector(room.pos.x + room.size.x - 1, behaviour.startPoint.y);
			} else {
				behaviour.startPoint = new PVector(room.pos.x + randomizer.nextInt(0, (int) room.size.x), room.pos.y);
				behaviour.endPoint = new PVector(behaviour.startPoint.x, room.pos.y + room.size.y - 1);
			}

			behaviour.e = new Entity(PVector.add(behaviour.startPoint, new PVector(1, 1, 0.25f)),
					new PVector(0.5f, 0.5f, 0.5f), Assets.getSprite(ENEMY_SPRITE),
					Attributes.getRandomAttributes(floor, randomizer));
			behaviours.add(behaviour);
		}
	}

	//////////////////////
	// Object Overrides //
	//////////////////////

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int[] row : grid) {
			for (int cell : row) {
				sb.append(Tile.getTile(cell).print);
			}
			sb.append('\n');
		}
		return sb.toString();
	}

	public String toStringWithSpecials(List<IntTuple> specials) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < grid.length; i++) {
			loop: for (int j = 0; j < grid[i].length; j++) {
				for (IntTuple s : specials) {
					if (s.a == j && s.b == i) {
						sb.append("EE");
						continue loop;
					}
				}
				sb.append(Tile.getTile(grid[i][j]).print);
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
