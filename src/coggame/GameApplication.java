package coggame;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GraphicsDevice;
import java.awt.DisplayMode;
import java.awt.RenderingHints;
import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.Toolkit;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Dimension;
import java.awt.BorderLayout;

/**
* GameApplication is an abstract class that deals
* with much of the boilerplate logic required
* for creating a raster-graphics game in Java,
* including double-buffering, timing, fullscreen
* functionality, upscaling and buffered key input.
*
* @author John Earnest
**/

public abstract class GameApplication {
	
	/**
	* The up arrow key is depressed.
	**/
	public static final int UP = 1;
	/**
	* The down arrow key is depressed.
	**/
	public static final int DOWN = 2;
	/**
	* The left arrow key is depressed.
	**/
	public static final int LEFT = 4;
	/**
	* The right arrow key is depressed.
	**/
	public static final int RIGHT = 8;
	/**
	* The 'fire' key (space) is depressed.
	**/
	public static final int FIRE = 16;
	/**
	* Game key A (Z) is depressed.
	**/
	public static final int GAME_A = 32;
	/**
	* Game key B (X) is depressed.
	**/
	public static final int GAME_B = 64;

	private int keys = 0;

	private GraphicsEnvironment ge;
	private final JFrame frame = new JFrame();
	private final InnerPainter panel = new InnerPainter(this);
	private final InnerEventPump pump = new InnerEventPump(this, Thread.currentThread());
	private final InnerListener listener;
	private final Thread painterThread;
	private final Thread pumpThread;
	private final Image buffer;
	private final int width;
	private final int height;

	/**
	* Construct a new GameApplication. The width and height
	* given here are the number of pixels in the drawing buffer
	* that will be passed to painting logic. The scaling factor
	* indicates how many times this buffer should be scaled up
	* for the actual game window or fullscreen resolution.
	*
	* For example, a GameApplication with width 320, height 240
	* and scaleFactor 2 will have a 640x480 window.
	*
	* @param width the width of the graphics buffer in pixels
	* @param height the height of the graphics buffer in pixels
	* @param scaleFactor see above
	* @param fullscreen should this application launch in fullscreen mode?
	**/
	public GameApplication(int width, int height, int scaleFactor, boolean fullscreen) {
		
		this.width = width * scaleFactor;
		this.height = height * scaleFactor;
		buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		// hide mouse cursor
		showCursor(false);

		// configure frame
		panel.setPreferredSize(new Dimension(width * scaleFactor, height * scaleFactor));
		listener = new InnerListener(this);
		frame.addKeyListener(listener);
		frame.setLayout(new BorderLayout());
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);

		// configure fullscreen stuff
		if (fullscreen) {
			frame.setUndecorated(true);
			ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice gd = ge.getDefaultScreenDevice();
			gd.setFullScreenWindow(frame);
			if (gd.isDisplayChangeSupported()) {
				try {
					gd.setDisplayMode(new DisplayMode(	width * scaleFactor,
														height * scaleFactor,
														32,
														DisplayMode.REFRESH_RATE_UNKNOWN));
				}
				catch (IllegalArgumentException iae) {
					System.out.format("Unable to support a %dx%d resolution. Available display modes:%n",
						width * scaleFactor,
						height * scaleFactor
					);
					for(DisplayMode m : gd.getDisplayModes()) {
						System.out.format("%dx%d- %d bit%n", m.getWidth(), m.getHeight(), m.getBitDepth());
					}
				}
			}
		}

		frame.pack();
		frame.setVisible(true);
		painterThread = new Thread(panel);
		pumpThread = new Thread(pump);

		painterThread.start();
		pumpThread.start();
	}

	/**
	* Return the JFrame for this application.
	* mess with it at your own peril.
	**/
	public JFrame getWindow() { return frame; }

	/**
	* Control the visibility of the mouse cursor.
	* By default, it is made invisible.
	*
	* @param show should the cursor be shown?
	**/
	public void showCursor(boolean show) {
		if (show) {
			frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
		else {
			Toolkit toolbox = Toolkit.getDefaultToolkit();
			Image cursorImage = new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB);
			Cursor blankCursor = toolbox.createCustomCursor(cursorImage,new Point(0,0),"");
			frame.setCursor(blankCursor);
		}
	}

	/**
	* Enable mouse events for this application.
	**/
	public void enableMouseEvents() {
		frame.getContentPane().addMouseListener(listener);
		frame.getContentPane().addMouseMotionListener(listener);
	}

	/**
	* Return the width of the application in pixels before scaling.
	**/
	public int getWidth() { return buffer.getWidth(null); }

	/**
	* Return the height of the application in pixels before scaling.
	**/
	public int getHeight() { return buffer.getHeight(null); }

	/**
	* Return a bitvector containing status bits
	* corresponding to the static key constants.
	*
	* For example, if you wanted to check that the
	* fire key was pressed, you could do the following:
	* <pre>if ((getKeys() & FIRE) != 0) { ... }</pre>
	**/
	public int getKeys() { return keys; }

	/**
	* Terminate the application.
	**/
	private void exit() {
		panel.stop();
		pump.stop();
		if (ge != null) {
			ge.getDefaultScreenDevice().setFullScreenWindow(null);
		}
		System.exit(0);
	}

	/**
	* Called whenever a keyboard key is depressed.
	* Values returned correspond to the constants
	* exposed in java.awt.event.KeyEvent.
	*
	* If the Escape key is pressed, the game will
	* immediately quit as from a call to exit().
	*
	* @param key the keycode of the depressed key
	**/
	protected void keyPressed(int key) {}

	/**
	* Called whenever a keyboard key is released.
	* Values returned correspond to the constants
	* exposed in java.awt.event.KeyEvent.
	*
	* @param key the keycode of the released key
	**/
	protected void keyReleased(int key) {}

	/**
	* Called whenever a keyboard key mapped to
	* a displayable character is pressed and
	* released.
	*
	* @param c the character represented by the key
	**/
	protected void keyTyped(char c) {}

	/**
	* Called whenever the mouse is moved.
	* Mouse events must be enabled first.
	*
	* @param x the x position of the mouse, in pixels
	* @param y the y position of the mouse, in pixels
	**/
	protected void mouseMoved(int x, int y) {}

	/**
	* Called whenever the mouse is clicked.
	* The button returned corresponds to the constants
	* exposed in java.awt.event.MouseEvent.
	* Mouse events must be enabled first.
	*
	* @param x the x position of the mouse, in pixels
	* @param y the y position of the mouse, in pixels
	* @param button the button or buttons depressed
	**/
	protected void mouseClicked(int x, int y, int button) {}

	/**
	* A game's rendering code should go here.
	*
	* @param g the destination Graphics surface.
	**/
	public abstract void paint(Graphics g);

	/**
	* A game's logic should go here.
	*
	* @param time the number of seconds since the last tick() call
	**/
	public abstract void tick(double time);

	private class InnerListener implements KeyListener, MouseListener, MouseMotionListener {
		private final GameApplication app;

		public InnerListener(GameApplication app) {
			this.app = app;
		}

		public void keyPressed(KeyEvent ke) {
			int k = ke.getKeyCode();
			if		(k == KeyEvent.VK_ESCAPE)	{ app.exit(); }
			else if (k == KeyEvent.VK_UP)		{ app.keys |= GameApplication.UP; }
			else if (k == KeyEvent.VK_DOWN)		{ app.keys |= GameApplication.DOWN; }
			else if (k == KeyEvent.VK_LEFT)		{ app.keys |= GameApplication.LEFT; }
			else if (k == KeyEvent.VK_RIGHT)	{ app.keys |= GameApplication.RIGHT; }
			else if (k == KeyEvent.VK_SPACE)	{ app.keys |= GameApplication.FIRE; }
			else if (k == KeyEvent.VK_Z)		{ app.keys |= GameApplication.GAME_A; }
			else if (k == KeyEvent.VK_X)		{ app.keys |= GameApplication.GAME_B; }
			app.keyPressed(k);
		}
		public void keyReleased(KeyEvent ke) {
			int k = ke.getKeyCode();
			if		(k == KeyEvent.VK_UP)		{ app.keys &= ~GameApplication.UP; }
			else if	(k == KeyEvent.VK_DOWN)		{ app.keys &= ~GameApplication.DOWN; }
			else if	(k == KeyEvent.VK_LEFT)		{ app.keys &= ~GameApplication.LEFT; }
			else if	(k == KeyEvent.VK_RIGHT)	{ app.keys &= ~GameApplication.RIGHT; }
			else if	(k == KeyEvent.VK_SPACE)	{ app.keys &= ~GameApplication.FIRE; }
			else if	(k == KeyEvent.VK_Z)		{ app.keys &= ~GameApplication.GAME_A; }
			else if	(k == KeyEvent.VK_X)		{ app.keys &= ~GameApplication.GAME_B; }
			app.keyPressed(k);
		}
		public void keyTyped(KeyEvent ke) {
			app.keyTyped(ke.getKeyChar());
		}

		public void mouseClicked(MouseEvent e) {
			app.mouseClicked(e.getX(), e.getY(), e.getButton());
		}

		public void mouseMoved(MouseEvent e) {
			app.mouseMoved(e.getX(), e.getY());
		}

		public void mouseEntered(MouseEvent e)  {}
		public void mouseExited(MouseEvent e)   {}
		public void mousePressed(MouseEvent e)  {}
		public void mouseReleased(MouseEvent e) {}
		public void mouseDragged(MouseEvent e)  {}
	}

	private class InnerPainter extends JPanel implements Runnable {
		private final GameApplication app;
		private boolean running = true;

		public InnerPainter(GameApplication app) {
			this.app = app;
		}

		public void run() {
			while(running) {
				repaint();
				try {Thread.sleep(100);}
				catch(InterruptedException ie) { ie.printStackTrace(); }
			}
		}

		public void stop() {
			running = false;
		}

		public void paint(Graphics g) {
			if (g instanceof Graphics2D) {
				// If it's a Graphics2D we can configure rendering options
				// to improve overall performance and pixeliness.
				Graphics2D gd = (Graphics2D) g;
				gd.setRenderingHint(	RenderingHints.KEY_RENDERING,
										RenderingHints.VALUE_RENDER_SPEED);
				gd.setRenderingHint(	RenderingHints.KEY_INTERPOLATION,
										RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			}
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, g.getClipBounds().width, g.getClipBounds().height);
			synchronized(app.buffer) {
				g.drawImage(app.buffer, 0, 0, app.width, app.height,
										0, 0, app.buffer.getWidth(this), app.buffer.getHeight(this), this);
			}
		}
	}

	private class InnerEventPump implements Runnable {
		private final GameApplication app;
		private final Thread parent;
		private boolean running = true;
		private long lastTick = System.nanoTime();

		public InnerEventPump(GameApplication app, Thread parent) {
			this.app = app;
			this.parent = parent;
		}

		public void run() {
			try {
				parent.join();
			}
			catch(InterruptedException ie) { ie.printStackTrace(); }
			while(true) {
				long thisTick = System.nanoTime();
				double tickTime = ((double)( (thisTick - lastTick) / 1000000)) / 1024;
				lastTick = thisTick;
				app.tick(tickTime);

				synchronized(app.buffer) {
					app.paint(buffer.getGraphics());
				}
				try {Thread.sleep(100);}
				catch(InterruptedException ie) { ie.printStackTrace(); }
			}
		}

		public void stop() {
			running = false;
		}
	}
}