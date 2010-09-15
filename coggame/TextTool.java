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

	/**
	* Construct a new TextTool
	*
	* @param font the character grid
	* @param charWidth the width of a character in pixels
	* @param charHeight the width of a character in pixels
	**/
	public TextTool(Image font, int charWidth, int charHeight) {
		this.font = font;
		this.charWidth = charWidth;
		this.charHeight = charHeight;
		sheetWidth = font.getWidth(null) / charWidth;
	}

	/**
	* Draw a single character
	*
	* @param c the character to draw
	* @param x the x-offset in tiles at which to draw the character
	* @param y the y-offset in tiles at which to draw the character
	* @param g the destination Graphics surface
	**/
	public void drawChar(char c, int x, int y, Graphics g) {
		if (c < ' ' || c > '~') { throw new IllegalArgumentException("Character out of range. ("+((int)c)+")"); }
		
		final int tx = ((c - ' ') % sheetWidth) * charWidth;
		final int ty = ((c - ' ') / sheetWidth) * charHeight;
		final int dx = x * charWidth;
		final int dy = y * charHeight;
		g.drawImage( font,
						dx, dy, dx + charWidth, dy + charHeight,
						tx, ty, tx + charWidth, ty + charHeight, null);
	}

	/**
	* Draw an entire string
	*
	* @param text the string to draw
	* @param x the x-offset in tiles at which to draw the character
	* @param y the y-offset in tiles at which to draw the character
	* @param g the destination Graphics surface
	**/
	public void drawString(String text, int x, int y, Graphics g) {
		for(char c : text.toCharArray()) {
			drawChar(c, x++, y, g);
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