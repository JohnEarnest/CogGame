package coggame;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Image;

/**
* TiledImage is a Layer that loops an Image
* on one or both axes, in a manner suitable for
* producing parallaxed backgrounds.
*
* @author John Earnest
**/
public class TiledImage extends Layer {

	private final Image image;
	private final boolean wrapVertical;
	private final boolean wrapHorizontal;
	private final Rectangle clip;

	/**
	* Create a new TiledImage.
	*
	* @param image the source image to tile
	* @param wrapHorizontal should this image be tiled horizontally?
	* @param wrapVertical should this image be tiled vertically?
	* @param clip a Rectangle defining the drawing region
	**/
	public TiledImage(Image image, boolean wrapHorizontal, boolean wrapVertical, Rectangle clip) {
		this.image = image;
		this.wrapVertical = wrapVertical;
		this.wrapHorizontal = wrapHorizontal;
		this.clip = clip;
	}

	/**
	* Returns the width of the source image in pixels.
	**/
	public int getWidth() { return image.getWidth(null); }

	/**
	* Returns the height of the source image in pixels.
	**/
	public int getHeight() { return image.getHeight(null); }

	/**
	* Draws the tiled image.
	*
	* @param g the destination Graphics surface
	**/
	public void paint(Graphics g) {
		if (!isVisible()) { return; }		
		int tx = getX() % getWidth();
		int ty = getY() % getHeight();
		while (tx < getWidth())		{ tx += getWidth(); }
		while (ty < getHeight())	{ ty += getHeight(); }

		if (wrapVertical && wrapHorizontal) {
			int dx = 0;
			while(dx < clip.width) {
				int chunkWidth = Math.min(clip.width - dx, getWidth() - tx);
				int dy = 0;
				while(dy < clip.height) {
					int chunkHeight = Math.min(clip.height - dy, getHeight() - ty);
					g.drawImage(image,
								dx + clip.x, dy + clip.y, dx + chunkWidth + clip.x, dy + chunkHeight + clip.y,
								tx, ty, tx + chunkWidth, ty + chunkHeight, null);
	
					dy += chunkHeight;
					ty = (ty + chunkHeight) % getHeight();
				}
				dy = 0;			
				dx += chunkWidth;
				tx = (tx + chunkWidth) % getWidth();
			}
		}
		else if (wrapVertical && !wrapHorizontal) {
			int dy = 0;
			while(dy < clip.height) {
				int chunkHeight = Math.min(clip.height - dy, getHeight() - ty);
				g.drawImage(image,
							getX() + clip.x, dy + clip.y, getX() + getWidth() + clip.x, dy + chunkHeight + clip.y,
							0, ty, getWidth(), ty + chunkHeight, null);
	
				dy += chunkHeight;
				ty = (ty + chunkHeight) % getHeight();
			}
		}
		else if (!wrapVertical && wrapHorizontal) {
			int dx = 0;
			while(dx < clip.width) {
				int chunkWidth = Math.min(clip.width - dx, getWidth() - tx);
				g.drawImage(image,
							dx + clip.x, getY() + clip.y, dx + chunkWidth + clip.x, getY() + getHeight() + clip.y,
							tx, 0, tx + chunkWidth, getHeight(), null);

				dx += chunkWidth;
				tx = (tx + chunkWidth) % getWidth();
			}
		}
		else {
			g.drawImage(image, getX(), getY(), null);
		}
	}
}