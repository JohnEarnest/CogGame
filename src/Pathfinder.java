import coggame.*;
import java.awt.*;
import java.awt.image.*;
import java.util.List;

public class Pathfinder extends GameApplication {

	public static final int TILESIZE = 48;
	private static final int TILES_X = 15;
	private static final int TILES_Y = 10;

	private final TiledLayer gridfloor;
	private final TiledLayer gridwalls;
	private final PathRobot robot;
	private final TextTool text;

	private int ax = 0;
	private int ay = 0;
	private int bx = 0;
	private int by = 0;
	private List<int[]> dirs;

	public static void main(String[] args) { new Pathfinder(); }

	public Pathfinder() {
		super(TILES_X * TILESIZE, TILES_Y * TILESIZE, 1, false);
		getWindow().setTitle("Pathfinding Demo");
		Image tiles = ImageTool.loadImage("assets/steel.png");
		gridfloor = new TiledLayer(TILES_X, TILES_Y, tiles, TILESIZE, TILESIZE);
		gridwalls = new TiledLayer(TILES_X, TILES_Y, tiles, TILESIZE, TILESIZE);
		text = new TextTool(ImageTool.loadImage("assets/text.png"), 8, 8, true);
		robot = new PathRobot();
		initBoard();
	}

	public void keyTyped(char key) {
		initBoard();
	}

	public void tick(double time) {
		robot.tick(time);
	}

	public void paint(Graphics g) {
		gridfloor.paint(g);
		gridwalls.paint(g);

		if (dirs != null) {
			g.setColor(Color.RED);
			g.drawOval(ax * TILESIZE, ay * TILESIZE, TILESIZE, TILESIZE);
			int x = ax;
			int y = ay;
			for(int[] deltas : dirs) {
				g.drawLine( (TILESIZE * x) + TILESIZE / 2,
							(TILESIZE * y) + TILESIZE / 2,
							(TILESIZE * (x + deltas[0])) + TILESIZE / 2,
							(TILESIZE * (y + deltas[1])) + TILESIZE / 2
				);
				x += deltas[0];
				y += deltas[1];
			}
		}
		else {
			g.setColor(Color.RED);
			g.drawLine(TILESIZE * bx, TILESIZE * by, TILESIZE * (bx+1), TILESIZE * (by+1));
			g.drawLine(TILESIZE * (bx+1), TILESIZE * by, TILESIZE * bx, TILESIZE * (by+1));
		}

		robot.paint(g);

		text.drawString("Press any key to generate a new map...", 1, 1, g);
	}

	private void initBoard() {
		gridwalls.fillCells(0, 0, gridwalls.getColumns(), gridwalls.getRows(), 0);
		int solid = ((gridwalls.getColumns() * gridwalls.getRows()) / 8) * 3;
		for(int z = 0; z < solid; z++) {
			int x = (int)(Math.random() * gridwalls.getColumns());
			int y = (int)(Math.random() * gridwalls.getRows());
			gridwalls.setCell(x, y, 1);
		}
		gridfloor.fillCells(0, 0, gridfloor.getColumns(), gridfloor.getRows(), 4);
		while(true) {
			int x = (int)(Math.random() * gridfloor.getColumns());
			int y = (int)(Math.random() * gridfloor.getRows());
			if (gridwalls.getCell(x, y) == 0) {
				gridfloor.setCell(x, y, 2);
				ax = x;
				ay = y;
				break;
			}
		}
		while(true) {
			int x = (int)(Math.random() * gridfloor.getColumns());
			int y = (int)(Math.random() * gridfloor.getRows());
			if (gridwalls.getCell(x, y) == 0 && gridfloor.getCell(x, y) != 2) {
				gridfloor.setCell(x, y, 3);
				bx = x;
				by = y;
				break;
			}
		}
		dirs = TiledLayer.path(gridwalls, ax, ay, bx, by, false);
		robot.setPath(dirs, ax, ay);
	}
}