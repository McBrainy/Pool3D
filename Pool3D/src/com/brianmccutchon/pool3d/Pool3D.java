package com.brianmccutchon.pool3d;

import java.awt.*;
import java.awt.event.KeyAdapter;

import static java.awt.event.KeyEvent.*;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.*;

import geometry.Camera;
import geometry.Environment;
import geometry.Point3D;
import geometry.Triangle3D;
import static com.brianmccutchon.pool3d.Physics.*;

/**
 * This class represents the main class and GUI of 3D pool.
 * Currently, the game has two modes, shooting and not shooting, as
 * represented by {@link #shooting}. Each has its own set of event handlers.
 * 
 * @author Brian McCutchon
 */
public class Pool3D extends JPanel {

	private static final long serialVersionUID = 2316556066963532682L;

	/** The rendering environment of this game **/
	Environment env = new Environment();

	/**
	 * Holds the keys that are currently being pressed.
	 * Idea taken from Arena.java
	 */
	HashSet<Integer> keysDown = new HashSet<>();

	/** {@code true} iff we are in shooting mode **/
	private boolean shooting = false;

	/** Key handlers for the moving mode. **/
	private HashMap<Integer, Runnable> moveHandlers = new HashMap<>();

	/** Key handlers for the shooting mode. **/
	private HashMap<Integer, Runnable> shootHandlers  = new HashMap<>();

	/** Timer for rendering loop. **/
	private Timer t;

	/** The corners of the table. **/
	static Point3D[] corners = {
			new Point3D( TABLE_X,  TABLE_Y,  TABLE_Z),
			new Point3D( TABLE_X,  TABLE_Y, -TABLE_Z),
			new Point3D( TABLE_X, -TABLE_Y,  TABLE_Z),
			new Point3D( TABLE_X, -TABLE_Y, -TABLE_Z),
			new Point3D(-TABLE_X,  TABLE_Y,  TABLE_Z),
			new Point3D(-TABLE_X,  TABLE_Y, -TABLE_Z),
			new Point3D(-TABLE_X, -TABLE_Y,  TABLE_Z),
			new Point3D(-TABLE_X, -TABLE_Y, -TABLE_Z),
	};

	// Divide each of the points above by two here so that we don't have to
	// have "/2" 24 times and the code looks cleaner
	static {
		Arrays.asList(corners).replaceAll(p -> p.divide(2));
	}

	/**
	 * The vertices of each of the triangles as indices into {@link #corners}.
	 */
	static int[][] triangles = {
			{ 0, 1, 3 }, { 0, 3, 2 },
			{ 4, 7, 5 }, { 4, 6, 7 },
			{ 0, 5, 1 }, { 0, 4, 5 },
			{ 2, 3, 7 }, { 2, 7, 6 },
			{ 1, 7, 3 }, { 1, 5, 7 },
			{ 0, 2, 6 }, { 0, 6, 4 },
	};

	/**
	 * The angle by which the camera moves around the ball each frame when in
	 * shooting mode.
	 */
	private static final double ROTATE_ANGLE = 0.05;

	/** Constructs a new Pool3D JFrame and starts the game. **/
	public Pool3D() {
		env.ambientLight = 0.5;
		env.tempLightSource = new Point3D(20, -50, 20);

		Arrays.asList(balls).forEach(env::addObject);
		for (int[] tri : triangles) {
			env.addTriangle(new Triangle3D(corners[tri[0]],
					corners[tri[1]], corners[tri[2]], Color.GREEN));
		}

		moveHandlers.put(VK_RIGHT, env::rotateRight);
		moveHandlers.put(VK_LEFT,  env::rotateLeft);
		moveHandlers.put(VK_DOWN,  env::moveBackward);
		moveHandlers.put(VK_UP,    env::moveForward);
		moveHandlers.put(VK_S,     env::moveDown);
		moveHandlers.put(VK_W,     env::moveUp);
		moveHandlers.put(VK_D,     env::moveRight);
		moveHandlers.put(VK_A,     env::moveLeft);
		moveHandlers.put(VK_OPEN_BRACKET,  env::nearFarther);
		moveHandlers.put(VK_CLOSE_BRACKET, env::nearCloser);

		shootHandlers.put(VK_RIGHT, this::rotateRightShooting);
		shootHandlers.put(VK_D,     this::rotateRightShooting);
		shootHandlers.put(VK_LEFT,  this::rotateLeftShooting);
		shootHandlers.put(VK_A,     this::rotateLeftShooting);
		shootHandlers.put(VK_DOWN,  this::rotateDownShooting);
		shootHandlers.put(VK_S,     this::rotateDownShooting);
		shootHandlers.put(VK_UP,    this::rotateUpShooting);
		shootHandlers.put(VK_W,     this::rotateUpShooting);
		shootHandlers.put(VK_SPACE, this::shoot);

		t = new Timer(16, (e) -> {
			Physics.nextFrame();

			HashMap<Integer, Runnable> handlers =
					shooting ? shootHandlers : moveHandlers;

			for (int i : keysDown) {
				if (handlers.containsKey(i)) {
					handlers.get(i).run();
				}
			}

			/* This works too (same as above)
			keysDown.stream()
				.filter(handlers::containsKey)
				.map(handlers::get)
				.forEach(Runnable::run);
			*/

			repaint();
		});

		t.start();
	}

	public static void main(String[] args) {
		Pool3D pool = new Pool3D();
		for (int i = 0; i < 20; i++) {
			pool.env.moveBackward();
		}

		JFrame frame = new JFrame();
		frame.setSize(new Dimension(800, 800));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(pool);
		frame.setVisible(true);

		frame.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent ke) {
				if (ke.getKeyCode() == VK_Q) {
					pool.switchMode();
				}

				pool.keysDown.add(ke.getKeyCode());
			}

			@Override
			public void keyReleased(KeyEvent ke) {
				pool.keysDown.remove(ke.getKeyCode());
			}
		});
	}

	@Override
	public void paint(Graphics gg) {
		Graphics2D g = (Graphics2D) gg;
		env.render(g);
	}

	public void shoot() {
		// To get the velocity of the ball after a shot,
		balls[0].velocity = env.getCamera().position // get the difference
				.subtract(balls[0].center) // between the cue ball and the camera,
				.divide(-1)   // negate it
				.normalize(); // and normalize it

		switchMode();
	}

	private void switchMode() {
		// Can't switch to shooting mode when balls are moving
		if (Physics.ballsAreMoving) {
			return;
		}

		shooting = !shooting;

		if (shooting) {
			// Move the camera to the cue ball's position
			Camera c = new Camera(balls[0].center.add(
					new Point3D(4, 0, 0)), new Point3D(-1, 0, 0));
			env.setCamera(c);
		}
	}

	private void rotateRightShooting() {
		rotateLR(ROTATE_ANGLE);
	}

	private void rotateLR(double angle) {
		rotateAroundCue(Environment.makeLRRotation(angle));
	}

	private void rotateLeftShooting() {
		rotateLR(-ROTATE_ANGLE);
	}

	/**
	 * Rotates the camera "up" or "down" around the ball by the specified
	 * angle.
	 * @param angle
	 */
	private void rotateUD(double angle) {
		Camera cam = env.getCamera();
		cam.position = cam.position.subtract(balls[0].center);
		rotateAroundCue(makeUDRotation(angle, cam.position));
	}

	/**
	 * Rotates the cmera around the cue ball in shooting mode.
	 * @param rotationMat The rotation matrix to use.
	 */
	private void rotateAroundCue(double[][] rotationMat) {
		Camera cam    = env.getCamera();
		// Translate so that cue ball is at the origin
		cam.position  = cam.position.subtract(balls[0].center);
		// Rotate with the matrix provided
		Physics.rotateVec(cam.position, rotationMat);
		cam.direction = cam.position.divide(-1).normalize();
		cam.position  = cam.position.add(balls[0].center);
		env.setCamera(cam);
	}

	/**
	 * Creates a rotation matrix to rotate the camera "up" or "down" around the
	 * origin.
	 * @param angle The angle by which to rotate.
	 * @param cameraPos The position of the camera.
	 * @return The rotation matrix.
	 */
	private double[][] makeUDRotation(double angle, Point3D cameraPos) {
		// Vector representing the axis of rotation
		Point3D a = cameraPos.cross(Z_UNIT_VEC);

		// The matrix below only works if the axis is a unit vector
		if (almostEq(a.dist(ORIGIN), 0)) {
			a = Y_UNIT_VEC;
		} else {
			a = a.normalize();
		}

		double cos = Math.cos(angle);
		double sin = Math.sin(angle);

		// Same source as in Physics.findCollisionRotationMat()
		return new double[][] {
			{cos+a.x*a.x*(1-cos), a.x*a.y*(1-cos)-a.z*sin, a.x*a.z*(1-cos)+a.y*sin},
			{a.y*a.x*(1-cos)+a.z*sin, cos+a.y*a.y*(1-cos), a.y*a.z*(1-cos)-a.x*sin},
			{a.z*a.x*(1-cos)-a.y*sin, a.z*a.y*(1-cos)+a.x*sin, cos+a.z*a.z*(1-cos)}
		};
	}

	// XXX Neither of the below methods can be fully implemented currently
	// because our camera has only one degree of freedom.
	
	/** Rotates the camera "up" around the cue ball in shooting mode. **/
	private void rotateUpShooting() {
		rotateUD(ROTATE_ANGLE);
	}

	/** Rotates the camera "down" around the cue ball in shooting mode. **/
	private void rotateDownShooting() {
		rotateUD(-ROTATE_ANGLE);
	}

}
