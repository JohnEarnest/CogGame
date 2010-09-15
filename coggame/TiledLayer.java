package coggame;

import java.awt.Graphics;
import java.awt.Image;
import java.util.List;
import java.util.ArrayList;

/**
* The TiledLayer represents a grid made up of
* tiles. Tiles can be animated and modified on
* the fly. A tile with index 0 is not drawn.
*
* @author John Earnest
**/
public class TiledLayer extends Layer {

	private final int[][] cells;
	private final Image tiles;
	private final int tileWidth;
	private final int tileHeight;
	private final int sheetWidth;
	private final List<Integer> animatedTiles = new ArrayList<Integer>();

	/**
	* Create a new TiledLayer.
	*
	* @param columns the number of columns in the grid
	* @param rows the number of rows in the grid
	* @param tiles an Image containing a grid of equal-sized tiles
	* @param tileWidth the width of each tile in pixels
	* @param tileHeight the height of each tile in pixels
	**/
	public TiledLayer(int columns, int rows, Image tiles, int tileWidth, int tileHeight) {
		cells = new int[columns][rows];
		this.tiles = tiles;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
		sheetWidth = tiles.getWidth(null) / tileWidth;
	}

	/**
	* Returns the width of the TiledLayer in pixels.
	**/
	public int getWidth()		{ return getColumns() * getCellWidth(); }

	/**
	* Returns the height of the TiledLayer in pixels.
	**/
	public int getHeight()		{ return getRows() * getCellHeight(); }

	/**
	* Returns the number of columns in the TiledLayer.
	**/
	public int getColumns()		{ return cells.length; }

	/**
	* Returns the number of rows in the TiledLayer.
	**/
	public int getRows()		{ return cells[0].length; }	
	
	/**
	* Returns the width of a single tile in pixels.
	**/
	public int getCellWidth()	{ return tileWidth; }

	/**
	* Returns the height of a single tile in pixels.
	**/
	public int getCellHeight()	{ return tileHeight; }

	/**
	* Returns the tile index at a given position.
	*
	* @param col the 0-indexed column number of the cell
	* @param row the 0-indexed row number of the cell
	**/
	public int getCell(int col, int row) {
		return cells[col][row];
	}

	/**
	* Sets the tile index at a given position.
	*
	* @param col the 0-indexed column number of the cell
	* @param row the 0-indexed row number of the cell
	**/
	public void setCell(int col, int row, int tile) {
		cells[col][row] = tile;
	}

	/**
	* Fill a rectangular region of cells.
	*
	* @param col the 0-indexed column number of the cell
	* @param row the 0-indexed row number of the cell
	* @param numCols the number of columns to fill
	* @param numRows the number of rows to fill
	* @param tile the tile to fill the region with
	**/
	public void fillCells(int col, int row, int numCols, int numRows, int tile) {
		for(int x = 0; x < numCols; x++) {
			for(int y = 0; y < numRows; y++) {
				cells[col + x][row + y] = tile;
			}
		}
	}

	/**
	* Draw this TiledLayer.
	*
	* @param g the destination Graphics object
	**/
	public void paint(Graphics g) {
		if (!isVisible()) { return; }
		for(int x = 0; x < getColumns(); x++) {
			for(int y = 0; y < getRows(); y++) {
				
				int tile = cells[x][y];
				if (tile == 0) { continue; }
				if (tile < 0) { tile = getAnimatedTile(tile); }

				final int tx = ((tile - 1) % sheetWidth) * tileWidth;
				final int ty = ((tile - 1) / sheetWidth) * tileHeight;
				final int dx = x * tileWidth + getX();
				final int dy = y * tileHeight + getY();
				g.drawImage(tiles,
								dx, dy, dx + tileWidth, dy + tileHeight,
								tx, ty, tx + tileWidth, ty + tileHeight, null);
			}
		}
	}

	/**
	* Create an animated tile.
	* Returns the index of the new animated tile.
	*
	* @param staticTileIndex the static tile index the animated tile appears as
	**/
	public int createAnimatedTile(int staticTileIndex) {
		animatedTiles.add(staticTileIndex);
		return -animatedTiles.size();
	}

	/**
	* Returns the static tile index an animated tile currently appears as
	*
	* @param animatedTileIndex the animated tile to examine
	**/
	public int getAnimatedTile(int animatedTileIndex) {
		return animatedTiles.get(-animatedTileIndex - 1);
	}

	/**
	* Change the appearance of an animated tile.
	*
	* @param animatedTileIndex the animated tile to change
	* @param staticTileIndex the static tile the animated tile should appear as
	**/
	public void setAnimatedTile(int animatedTileIndex, int staticTileIndex) {
		animatedTiles.set(-animatedTileIndex - 1, staticTileIndex);
	}

}