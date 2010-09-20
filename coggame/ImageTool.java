package coggame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.util.List;
import java.util.ArrayList;

/**
* The ImageTool is a collection of utility
* routines for dealing with Images.
*
* @author John Earnest
**/
public class ImageTool {
	
	/**
	* Unpack an image into a 2D grid of AWT Color
	* values for easy manipulation and inspection.
	*
	* @param i the source image
	**/
	public static Color[][] getPixels(Image i) {
		final int w = i.getWidth(null);
		final int h = i.getHeight(null);
		final int a[] = new int[w * h];
		final PixelGrabber pg = new PixelGrabber(i,0,0,w,h,a,0,w);
		try { pg.grabPixels(); }
		catch(InterruptedException ie) { ie.printStackTrace(); }

		final Color[][] ret = new Color[w][h];
		for(int x = 0; x < w; x++) {
			for(int y = 0; y < h; y++) {
				int c = a[x + (w * y)];
				ret[x][y] = new Color(	(c >> 24) & 0xFF,
										(c >> 16) & 0xFF,
										(c >>  8) & 0xFF,
										(c >>  0) & 0xFF );
			}
		}
		return ret;
	}

	/**
	* Scan an image and compile a table
	* of all the colors used.
	*
	* @param i the source image
	**/
	public static Color[] getColors(Image i) {
		final List<Color> ret = new ArrayList<Color>();
		for(Color[] a : getPixels(i)) {
			for(Color b : a) {
				if (!ret.contains(b)) { ret.add(b); }
			}
		}
		return (Color[]) ret.toArray();
	}

	/**
	* Replace every instance of a color in the
	* first table with the corresponding color
	* in the second table. Any colors not present
	* in the first table will be unchanged.
	*
	* @param i the source image
	* @param find the colors to scan for
	* @param replace the resplacement colors
	**/
	public static Image recolor(Image i, Color[] find, Color[] replace) {
		int w = i.getWidth(null);
		int h = i.getHeight(null);
		int a[] = new int[w * h];
		int c[] = new int[find.length];
		Image ret = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics g = ret.getGraphics();
		PixelGrabber pg = new PixelGrabber(i,0,0,w,h,a,0,w);
		try {pg.grabPixels();}
		catch(InterruptedException ie){ie.printStackTrace();}
		for(int z=0;z<find.length;z++) {
			c[z] = find[z].getRGB();
		}
		g.drawImage(i,0,0,null);
		for(int y = 0; y < h; y++) {
			for(int x = 0; x < w; x++) {
				for(int z = 0; z < c.length; z++) {
					if (a[x + (y * w)] == c[z]) {
						g.setColor(replace[z]);
						g.drawLine(x, y, x, y);
						break;
					}
				}
			}
		}
		return ret;
	}

	/**
	* Returns true if the supplied image has
	* any pixels that are not completely opaque.
	*
	* @param i the source image
	**/
	public static boolean hasTransparency(Image i) {
		return hasTransparency(i, i.getWidth(null), i.getHeight(null), 0);
	}

	/**
	* Returns true if a given 0-indexed tile within
	* the supplied image has any pixels that are not
	* completely opaque.
	*
	* @param i the source image
	* @param tileWidth the width of a tile in pixels
	* @param tileHeight the height of a tile in pixels
	* @param tile the index of the tile to examine
	**/
	public static boolean hasTransparency(Image i, int tileWidth, int tileHeight, int tile) {
		final int w = i.getWidth(null);
		final int a[] = new int[tileWidth * tileHeight];
		final int tx = (tile % (w / tileWidth)) * tileWidth;
		final int ty = (tile / (w / tileWidth)) * tileHeight;
		final PixelGrabber pg = new PixelGrabber(i,tx,ty,tx+tileWidth,ty+tileHeight,a,0,w);
		try { pg.grabPixels(); }
		catch(InterruptedException ie) { ie.printStackTrace(); }

		for(int x : a) {
			if ((x & 0xFF000000) != 0xFF000000) { return true; }
		}
		return false;
	}
}