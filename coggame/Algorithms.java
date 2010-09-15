package coggame;

import java.util.List;
import java.util.ArrayList;

/**
* Algorithms contains a collection
* of miscellaneous algorithms and utility
* routines useful for game engines.
*
* @author John Earnest
**/
public class Algorithms {
	
	private static final int SKY_WIDTH_MIN = 2;
	private static final int SKY_WIDTH_MAX = 6;
	private static final int SKY_HEIGHT_DELTA = 4;

	private static boolean relEquals(int x, int y, TiledLayer grid, int testval) {
		if (x < 0) {return false;}
		if (y < 0) {return false;}
		if (x >= grid.getColumns()) {return false;}
		if (y >= grid.getRows()) {return false;}
		return (grid.getCell(x, y) == testval);
	}

	private static int rand(int[] src) {
		return src[(int)(Math.random() * src.length)];
	}

	/**
	* Fill a provided TiledLayer with a procedurally
	* generated skyline. Tiles are randomly selected
	* from the lists provided.
	*
	* @param layer the TiledLayer to fill.
	* @param fill tile indices for solid regions of the skyline
	* @param left tile indices for left edges of the skyline
	* @param right tile indices for right edges of the skyline
	* @param top tile indices for the top edge of the skyline
	**/
	public static void skyline(TiledLayer layer, int[] fill, int[] left, int[] right, int[] top) {
		layer.fillCells(0, 0, layer.getColumns(), layer.getRows(), 0);
		
		// fill solid skyline
		int col = layer.getColumns() - 1;
		int height = layer.getRows() / 2;
		do {
			height += (int)(Math.random() * SKY_HEIGHT_DELTA) * 2 - SKY_HEIGHT_DELTA;
			if (height > layer.getRows() - 2) { height = (int)(Math.random() * layer.getRows()) - 2; }
			int width = (int)(Math.random() * (SKY_WIDTH_MAX - SKY_WIDTH_MIN)) + SKY_WIDTH_MIN;

			height = Math.max(height, 2);
			width = Math.min(width, col);
			col -= width;

			//layer.fillCells(col, height, width, layer.getRows() - height - 1, SKY_SOLID);

			for(int x = 0; x <= width; x++) {
				for(int y = 0; y < layer.getRows() - height; y++) {
					layer.setCell(x + col, y + height, rand(fill));
				}
			}
		} while (col > 0);

		// add edge decorations
		for(int x = layer.getColumns() - 1; x >= 0; x--) {
			for(int y = layer.getRows() - 1; y >= 0; y--) {
				
				if (relEquals(x, y - 1, layer, 0)) {
					layer.setCell(x, y - 1, rand(top));
					break;
				}
				else if (relEquals(x + 1, y, layer, 0)) {
					layer.setCell(x, y, rand(right));
				}
				else if (relEquals(x - 1, y, layer, 0)) {
					layer.setCell(x, y, rand(right));
				}
			}
		}
	}

	/**
	* Find a path from a starting location in a TiledLayer
	* to a given goal location. Nonzero tiles are considered
	* impassable, for consistency with how sprite collision
	* deals with TiledLayers.
	*
	* Returns a sequence of pairs of coordinates representing
	* absolute tile positions. If no path is found, returns null.
	*
	* @param layer the TiledLayer to search
	* @param xStart the x-position of the starting tile
	* @param yStart the y-position of the starting tile
	* @param xGoal the x-position of the goal tile
	* @param yGoal the y-position of the goal tile
	* @param useDiagonal should we consider diagonal movement as well as orthogonal?
	**/
	public static List<int[]> path(TiledLayer layer, int xStart, int yStart, int xGoal, int yGoal, boolean useDiagonal) {

		List<int[]> frontier = new ArrayList<int[]>();
		frontier.add(new int[] { xGoal, yGoal, 1 });

		int[][] grid = new int[layer.getColumns()][layer.getRows()];
		int[][] deltas = (!useDiagonal) ? new int[][] { {-1, 0}, {1, 0}, {0, -1}, {0, 1} } :
										  new int[][] { {-1, 0}, {1, 0}, {0, -1}, {0, 1},
														{-1,-1}, {1, 1}, {-1, 1}, {1,-1} };

		while (!frontier.isEmpty()) {
			int[] coords = frontier.remove(0);
			int x = coords[0];
			int y = coords[1];
			int c = coords[2];
			
			if (x < 0 || x >= grid.length)		{ continue; }	// x off board
			if (y < 0 || y >= grid[0].length)	{ continue; }	// y off board
			if (layer.getCell(x, y) != 0)		{ continue; }	// impassable tile
			if (grid[x][y] == 0) {								// if unvisited, fan out
				for(int[] delta : deltas) {
					frontier.add(new int[] { x + delta[0], y + delta[1], c + 1 });
				}
				grid[x][y] = c;
			}
			else {
				grid[x][y] = Math.min(grid[x][y], c);
			}
		}

		// Now every unreachable grid cell should be at 0
		// and every other grid cell should contain their
		// distance from the goal position.

		if (grid[xStart][yStart] == 0) { return null; }

		List<int[]> ret = new ArrayList<int[]>();
		int x = xStart;
		int y = yStart;
		while (x != xGoal || y != yGoal) {
			int dx = 0;
			int dy = 0;
			int dc = Integer.MAX_VALUE;
			for(int[] delta : deltas) {
				int nx = x + delta[0];
				int ny = y + delta[1];
				if (nx < 0 || nx >= grid.length)	{ continue; } // x off board
				if (ny < 0 || ny >= grid[0].length)	{ continue; } // y off board
				int value = grid[nx][ny];
				if (value == 0 || value >= dc)		{ continue; } // unreachable / suboptimal
				dx = delta[0];
				dy = delta[1];
				dc = value;
			}
			ret.add(new int[] {dx, dy});
			x += dx;
			y += dy;
		}
		return ret;
	}
}