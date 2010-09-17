package coggame;

import java.awt.Graphics;
import java.util.List;
import java.util.ArrayList;

/**
* The LayerManager is specialized collection
* for aggregating Layers, drawing them
* and scrolling them together in a manner
* appropriate for parallax effects.
*
* @author John Earnest
**/
public class LayerManager {
	
	private List<LayerShift> layers = new ArrayList<LayerShift>();

	/**
	* Add a new layer to the stack.
	*
	* @param layer the new layer
	**/
	public void add(Layer layer) {
		add(layer, 1, 1);
	}

	/**
	* Add a new layer at a specific position in the stack.
	*
	* @param layer the new layer
	* @param index the index (counting from 0) of the new layer
	**/
	public void put(Layer layer, int index) {
		put(layer, index, 1, 1);
	}

	/**
	* Returns a reference to a given layer within the stack.
	*
	* @param index the index (counting from 0) of the layer
	**/
	public Layer get(int index) {
		return layers.get(index).layer;
	}

	/**
	* Returns the number of layers in the stack.
	**/
	public int size() {
		return layers.size();
	}

	/**
	* Add a new layer to the stack,
	* with given scroll multipliers.
	*
	* @param layer the new layer
	* @param sx the scroll multiplier for the x-axis
	* @param sy the scroll multiplier for the y-axis
	**/
	public void add(Layer layer, double sx, double sy) {
		layers.add(new LayerShift(layer, sx, sy));
	}

	/**
	* Add a new layer at a specific position in the stack,
	* with given scroll multipliers.
	*
	* @param layer the new layer
	* @param sx the scroll multiplier for the x-axis
	* @param sy the scroll multiplier for the y-axis
	**/
	public void put(Layer layer, int index, double sx, double sy) {
		layers.add(index, new LayerShift(layer, sx, sy));
	}

	/**
	* Remove a layer from the stack.
	*
	* @param layer the layer to remove
	**/
	public void remove(Layer layer) {
		for(int x = 0; x < layers.size(); x++) {
			if (layers.get(x).layer.equals(layer)) {
				layers.remove(x);
				return;
			}
		}
	}

	/**
	* Draw all layers, from the lowest-indexed
	* to the highest-indexed.
	*
	* @param g the destination Graphics surface
	**/
	public void paint(Graphics g) {
		for(LayerShift shift : layers) {
			shift.layer.paint(g);
		}
	}

	/**
	* Scroll all layers. The scrolling amounts
	* given here are multiplied by the scaling
	* factors specified when the layers were added
	* to the LayerManager. If no scaling factors
	* were provided, they default to 1.
	*
	* Fractional scrolling will accumulate between
	* calls, so a layer with an x-multiplier of .5
	* and a layer with a y-multiplier of .25
	* being scrolled with move(1, 1) will shift right
	* 1 pixel every 2 calls, and down 1 pixel
	* every 4 calls.
	*
	* @param dx the amount to scroll horizontally, in pixels
	* @param dy the amount to scroll vertically, in pixels
	**/
	public void move(double dx, double dy) {
		for(LayerShift shift : layers) {
			shift.move(dx, dy);
		}
	}

	private class LayerShift {
		public final Layer layer;
		private final double sx;
		private final double sy;

		public LayerShift(Layer layer, double sx, double sy) {
			this.layer = layer;
			this.sx = sx;
			this.sy = sy;
		}

		private void move(double x, double y) {
			layer.move(x * sx, y * sy);
		}
	}
}