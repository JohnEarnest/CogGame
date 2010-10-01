package coggame;

import java.util.List;
import java.util.ArrayList;
import java.awt.Graphics;
import java.awt.Image;

/**
* The TextTool is a utility for working with
* monospaced bitmapped fonts. The "font" must be provided
* in the form of a grid of characters representing the
* ASCII characters from 32 to 126 (' ' to '~'), inclusive.
*
* @author John Earnest
**/
public class TextTool {
	
	private final Image font;
	private final int charWidth;
	private final int charHeight;
	private final int sheetWidth;
	private final boolean tileCoords;

	/**
	* Construct a new TextTool.
	* If tileCoords is true, units given as x and y coordinates
	* for drawChar(), drawString() and drawBox() will be interpreted
	* as character units. Otherwise, these units will be interpreted as pixels.
	*
	* @param font the character grid
	* @param charWidth the width of a character in pixels
	* @param charHeight the width of a character in pixels
	* @param tileCoords select drawing units
	**/
	public TextTool(Image font, int charWidth, int charHeight, boolean tileCoords) {
		this.font = font;
		this.charWidth = charWidth;
		this.charHeight = charHeight;
		sheetWidth = font.getWidth(null) / charWidth;
		this.tileCoords = tileCoords;
	}

	/**
	* Draw a single character
	*
	* @param c the character to draw
	* @param x the x-offset at which to draw the character
	* @param y the y-offset at which to draw the character
	* @param g the destination Graphics surface
	**/
	public void drawChar(char c, int x, int y, Graphics g) {
		if (c < ' ' || c > '~') { throw new IllegalArgumentException("Character out of range. ("+((int)c)+")"); }
		
		final int tx = ((c - ' ') % sheetWidth) * charWidth;
		final int ty = ((c - ' ') / sheetWidth) * charHeight;
		final int dx = (tileCoords) ? (x * charWidth) : x;
		final int dy = (tileCoords) ? (y * charHeight) : y;
		g.drawImage( font,
						dx, dy, dx + charWidth, dy + charHeight,
						tx, ty, tx + charWidth, ty + charHeight, null);
	}

	/**
	* Draw an entire string
	*
	* @param text the string to draw
	* @param x the x-offset at which to draw the character
	* @param y the y-offset at which to draw the character
	* @param g the destination Graphics surface
	**/
	public void drawString(String text, int x, int y, Graphics g) {
		for(char c : text.toCharArray()) {
			drawChar(c, x, y, g);
			x += (tileCoords) ? 1 : charWidth;
		}
	}

	/**
	* Draw a rectangular region of characters
	* using given border tiles:
	* <pre>
	* 0 1 2
	* 3 4 5
	* 6 7 8
	* </pre>
	*
	* @param g the destination Graphics surface
	* @param x the x-offset of the top-left corner of the box
	* @param y the y-offset of the top-left corner of the box
	* @param w the width in characters of the interior of the box
	* @param h the height in characters of the interior of the box
	* @param t an array of character indices for the border and fill
	**/
	public void drawBox(int x, int y, int w, int h, char[] t, Graphics g) {
		final int hscale = (tileCoords) ? 1 : charWidth;
		final int vscale = (tileCoords) ? 1 : charHeight;
		final int nedge = y;
    	final int sedge = y + (vscale * (h + 1));
    	final int wedge = x;
    	final int eedge = x + (hscale * (w + 1));
		drawChar(t[0], wedge, nedge, g);
    	drawChar(t[2], eedge, nedge, g);
    	drawChar(t[6], wedge, sedge, g);
    	drawChar(t[8], eedge, sedge, g);
		for (int z = 1; z <= h; z++) {
			drawChar(t[3], wedge, y + (vscale * z), g);
			drawChar(t[5], eedge, y + (vscale * z), g);
		}
		for (int z = 1; z <= w; z++) {
			drawChar(t[1], x + (hscale * z), nedge, g);
			drawChar(t[7], x + (hscale * z), sedge, g);
			for (int a = 1; a <= h; a++) {
				drawChar(t[4], x + (hscale * z), y + (vscale * a), g);
			}
		}
	}

	/**
	* Split a string into a series of lines no longer
	* than a specified width by breaking on spaces between words.
	* Useful for word-wrapped dialog boxes, etc.
	*
	* @param text the string to word-wrap
	* @param width the maximum width in characters of a wrapped line
	**/
	public static List<String> wrap(String text, int width) {
		final List<String> ret = new ArrayList<String>();

		int line = 0;
		int head = width;
		
		while(head < text.length()) {
			while(head > line && text.charAt(head) != ' ') {
				head--;
			}
			ret.add(text.substring(line, head));
			line = head + 1;
			head = line + width;
		}
		if (line < text.length()) {
			ret.add(text.substring(line));
		}
		
		return ret;
	}
}