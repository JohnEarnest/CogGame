package coggame;

import java.awt.Graphics;

/**
* A Layer is an abstract class representing
* a graphical object with a position and size.
* Layers can be stacked together in a LayerManager
* to build up a scene.
*
* @author John Earnest
**/
public abstract class Layer {
	
	private int x = 0;
	private int y = 0;
	private boolean visible = true;

	/**
	* Returns the x-position of the Layer.
	**/
	public int getX() { return x; }

	/**
	* Returns the y-position of the Layer.
	**/
	public int getY() { return y; }

	/**
	* Returns true if the Layer is visible.
	**/
	public boolean isVisible() { return visible; }

	/**
	* Specify the visibility of the Layer.
	* Implementations are responsible for honoring this value.
	*
	* @param value true if the Layer should be visible
	**/
	public void setVisible(boolean value) { visible = value; }
	
	/**
	* Translate the Layer.
	*
	* @param dx the x-displacement in pixels
	* @param dy the y-displacement in pixels
	**/
	public void move(int dx, int dy) { x += dx; y += dy; }

	/**
	* Set the absolute position of the Layer.
	*
	* @param px the x-position in pixels
	* @param py the y-position in pixels
	**/
	public void setPosition(int px, int py) { x = px; y = py; }

	/**
	* Returns the width of the Layer in pixels.
	**/
	public int getWidth() { return -1; }

	/**
	* Returns the height of the Layer in pixels.
	**/
	public int getHeight() { return -1; }

	/**
	* Draw this layer to a Graphics surface.
	*
	* @param g the target Graphics surface.
	**/
	public abstract void paint(Graphics g);
}