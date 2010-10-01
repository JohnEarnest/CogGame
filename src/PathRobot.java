import coggame.*;
import java.util.*;
import java.awt.Graphics;

public class PathRobot {
	
	private final Sprite sprite;
	private int[][] path;
	private int state = 0;
	private int pathindex = 0;
	private int pathdir = 1;

	private static final int TILESIZE = Pathfinder.TILESIZE;
    private static final int n = Sprite.TRANS_NONE;
    private static final int f = Sprite.TRANS_MIRROR_HORIZ;
    private static final double SPEED = TILESIZE * 4;

    // 0-down, 1-left, 2-up, 3-right
    //                                               0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15
    private static final byte[] flag = new byte[]  { 0, 0, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 5, 5 };
    private static final byte[] frame = new byte[] { 1, 2, 3, 2, 1, 4, 2, 5, 3, 6, 2, 5, 1, 2, 3, 2 };
    private static final byte[] next = new byte[]  { 0, 1, 2, 3, 5, 4, 7, 6, 9, 8,11,10,13,14,15,12 };
    private static final int[] trans = new int[]   { n, n, n, f, n, n, n, n, n, n, f, f, n, n, n, f };

	public PathRobot() {
		sprite = new Sprite(ImageTool.loadImage("assets/robot.png"), 48, 48);
	}

	public void tick(double time) {

		// if we've reached a waypoint,
		// switch our goal to the next waypoint.
		if (sprite.getX() == path[pathindex][0] &&
			sprite.getY() == path[pathindex][1]) {

			pathindex += pathdir;
			if (pathindex < 0) {
				pathindex = 0;
				pathdir = 1;
			}
			else if (pathindex >= path.length) {
				pathindex = path.length - 1;
				pathdir = -1;
			}
		}

		boolean lf = (sprite.getX() > path[pathindex][0]);
		boolean rt = (sprite.getX() < path[pathindex][0]);
		boolean up = (sprite.getY() > path[pathindex][1]);
		boolean dn = (sprite.getY() < path[pathindex][1]);

        // If rolling, move the robot.
        double deltaX = 0;
        double deltaY = 0;
        if      (flag[state] == 1) { deltaY = Math.min(SPEED * time, path[pathindex][1] - sprite.getY()); }
        else if (flag[state] == 2) { deltaX = Math.max(-SPEED * time, path[pathindex][0] - sprite.getX()); }
        else if (flag[state] == 3) { deltaY = Math.max(-SPEED * time, path[pathindex][1] - sprite.getY()); }
        else if (flag[state] == 4) { deltaX = Math.min(SPEED * time, path[pathindex][0] - sprite.getX()); }
        sprite.move(deltaX, deltaY);

        // If we aren't rolling in a direction, switch to idle.
        if      (!dn && flag[state] == 1) { state = 0; }
        else if (!lf && flag[state] == 2) { state = 1; }
        else if (!up && flag[state] == 3) { state = 2; }
        else if (!rt && flag[state] == 4) { state = 3; }

        // If we're idling and directed to move, start walking.
        else if (dn && flag[state] == 0) { state = 4; }
        else if (lf && flag[state] == 0) { state = 6; }
        else if (up && flag[state] == 0) { state = 8; }
        else if (rt && flag[state] == 0) { state = 10; }

        // Otherwise, loop the current animation.
        else { state = next[state]; }

        sprite.setTransform(trans[state]);
        sprite.setFrame(frame[state]);
	}

	public void paint(Graphics g) { sprite.paint(g); }

	public void setPath(List<int[]> path, int startX, int startY) {
		startX *= TILESIZE;
		startY *= TILESIZE;
		sprite.setPosition(startX, startY);
		pathindex = 0;
		pathdir = 1;
		if (path == null) {
			this.path = new int[1][2];
			this.path[0][0] = startX;
			this.path[0][1] = startY;
			state = 12;
			return;
		}
		this.path = new int[path.size() + 1][2];
		for(int z = 0; z < path.size(); z++) {
			this.path[z][0] = startX;
			this.path[z][1] = startY;
			startX += path.get(z)[0] * TILESIZE;
			startY += path.get(z)[1] * TILESIZE;
		}
		this.path[path.size()][0] = startX;
		this.path[path.size()][1] = startY;
		state = 0;
	}
}