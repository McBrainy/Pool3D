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

public class Pool3D extends JPanel {

	private static final long serialVersionUID = 2316556066963532682L;
	Environment env   = new Environment();
	static HashSet<Integer> keysDown = new HashSet<>();

	/** {@code true} iff we are in shooting mode **/
	private boolean shooting = false;

	/** Key handlers for the moving mode. **/
	HashMap<Integer, Runnable> moveHandlers = new HashMap<>();

	/** Key handlers for the shooting mode. **/
	HashMap<Integer, Runnable> shootHandlers  = new HashMap<>();

	Timer t;
	private long lastTime;

	Point3D[] corners = {
			new Point3D( TABLE_X,  TABLE_Y,  TABLE_Z),
			new Point3D( TABLE_X,  TABLE_Y, -TABLE_Z),
			new Point3D( TABLE_X, -TABLE_Y,  TABLE_Z),
			new Point3D( TABLE_X, -TABLE_Y, -TABLE_Z),
			new Point3D(-TABLE_X,  TABLE_Y,  TABLE_Z),
			new Point3D(-TABLE_X,  TABLE_Y, -TABLE_Z),
			new Point3D(-TABLE_X, -TABLE_Y,  TABLE_Z),
			new Point3D(-TABLE_X, -TABLE_Y, -TABLE_Z),
	};

	{
		Arrays.asList(corners).replaceAll(p -> p.divide(2));
	}

	int[][] triangles = {
			{ 0, 1, 3 }, { 0, 3, 2 },
			{ 4, 7, 5 }, { 4, 6, 7 },
			{ 0, 5, 1 }, { 0, 4, 5 },
			{ 2, 3, 7 }, { 2, 7, 6 },
			{ 1, 7, 3 }, { 1, 5, 7 },
			{ 0, 2, 6 }, { 0, 6, 4 },
	};

	private double rotAngle = 0.05;

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
			long time = System.currentTimeMillis();
			Physics.nextFrame(time - lastTime);

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

		lastTime = System.currentTimeMillis();

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

				keysDown.add(ke.getKeyCode());
			}

			@Override
			public void keyReleased(KeyEvent ke) {
				keysDown.remove(ke.getKeyCode());
			}
		});
	}

	@Override
	public void paint(Graphics gg) {
		Graphics2D g = (Graphics2D) gg;
		env.render(g);
	}

	public void shoot() {
		balls[0].velocity = env.getCamera().position.subtract(balls[0].center).divide(-4);
		switchMode();
	}

	private void switchMode() {
		if (Physics.ballsAreMoving) {
			return;
		}

		shooting = !shooting;

		if (shooting) {
			Camera c = new Camera(balls[0].center.add(
					new Point3D(4, 0, 0)), new Point3D(-1, 0, 0));
			env.setCamera(c);
		}
	}

	private void rotateRightShooting() {
		rotateLR(rotAngle);
	}

	private void rotateLR(double angle) {
		Camera cam    = env.getCamera();
		cam.position  = cam.position.subtract(balls[0].center);
		Physics.rotateVec(cam.position, Environment.makeLRRotation(angle));
		cam.direction = cam.position.divide(-1).normalize();
//		double tmp = cam.direction.x;
//		cam.direction.x = -cam.direction.y;
//		cam.direction.y = tmp;
		//System.out.println("" + Math.signum(cam.direction.x) + Math.signum(cam.direction.y));
		cam.position  = cam.position.add(balls[0].center);
		env.setCamera(cam);
	}

	private void rotateLeftShooting() {
		rotateLR(-rotAngle);
	}

	// XXX Neither of the below methods can be implemented currently
	// because our camera has only one degree of freedom.
	private void rotateUpShooting() {
		rotateUD(rotAngle);
	}

	private void rotateUD(double angle) {
		Camera cam = env.getCamera();
		cam.position = cam.position.subtract(balls[0].center);
		Physics.rotateVec(cam.position, makeUDRotation(angle, cam.position));
		cam.direction = cam.position.divide(-1).normalize();
		cam.position  = cam.position.add(balls[0].center);
		env.setCamera(cam);
	}

	private double[][] makeUDRotation(double angle, Point3D cameraPos) {
		// Vector representing the axis of rotation
		Point3D a = cameraPos.cross(new Point3D(0, 0, 1));

		// The matrix below only works if the axis is a unit vector
		if (almostEq(a.dist(ORIGIN), 0)) {
			a = Y_UNIT_VEC;
		} else {
			a = a.normalize();
			a.z = 0;
		}

		double cos = Math.cos(angle);
		double sin = Math.sin(angle);

		return new double[][] {
			{cos+a.x*a.x*(1-cos), a.x*a.y*(1-cos)-a.z*sin, a.x*a.z*(1-cos)+a.y*sin},
			{a.y*a.x*(1-cos)+a.z*sin, cos+a.y*a.y*(1-cos), a.y*a.z*(1-cos)-a.x*sin},
			{a.z*a.x*(1-cos)-a.y*sin, a.z*a.y*(1-cos)+a.x*sin, cos+a.z*a.z*(1-cos)}
		};
	}

	private void rotateDownShooting() {
		rotateUD(-rotAngle);
	}

}
