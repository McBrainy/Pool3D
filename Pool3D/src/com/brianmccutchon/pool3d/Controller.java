package com.brianmccutchon.pool3d;

import static java.awt.event.KeyEvent.*;

import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.HashSet;

import org.scijava.java3d.*;
import org.scijava.vecmath.*;

public class Controller {
	private static final double MOVE_SPEED = 0.5;

	private static final double ROT_SPEED = 0.02;

	/**
	 * The translation of the camera from the ball when {@link #camDeg1} and
	 * {@link #camDeg2} are both zero.
	 */
	private static final Vector3d SHOOTING_TRANS = new Vector3d(0, 0, -6);

	/** Holds the keys that are currently being pressed. **/
	private HashSet<Integer> keysDown = new HashSet<>();

	/** Key handlers for the moving mode. **/
	private HashMap<Integer, Runnable> moveHandlers = new HashMap<>();

	/** Key handlers for the shooting mode. **/
	private HashMap<Integer, Runnable> shootHandlers  = new HashMap<>();

	private TransformGroup cam;
	private Matrix3d       camRotMat;
	private Transform3D    camTransform;
	private Pool3D pool;

	/**
	 * The camera's first degree of freedom, a rotation around the y axis.
	 * This only applies in shooting mode.
	 */
	public double camDeg1 = 0.0;

	/**
	 * The camera's second degree of freedom, a rotation towards the y axis.
	 * This only applies in shooting mode.
	 */
	public double camDeg2 = 0.0;

	public Controller(Pool3D pool, Component comp, TransformGroup camera) {
		this.pool = pool;
		cam = camera;
		camTransform = new Transform3D();
		cam.getTransform(camTransform);
		camRotMat    = new Matrix3d();
		camTransform.getRotationScale(camRotMat);

		moveHandlers.put(VK_RIGHT,  this::rotateRight);
		moveHandlers.put(VK_LEFT,   this::rotateLeft);
		moveHandlers.put(VK_DOWN,   this::moveBackward);
		moveHandlers.put(VK_UP,     this::moveForward);
		moveHandlers.put(VK_S,      this::moveDown);
		moveHandlers.put(VK_W,      this::moveUp);
		moveHandlers.put(VK_D,      this::moveRight);
		moveHandlers.put(VK_A,      this::moveLeft);

		shootHandlers.put(VK_RIGHT, this::rotateRightShooting);
		shootHandlers.put(VK_D,     this::rotateRightShooting);
		shootHandlers.put(VK_LEFT,  this::rotateLeftShooting);
		shootHandlers.put(VK_A,     this::rotateLeftShooting);
		shootHandlers.put(VK_DOWN,  this::rotateDownShooting);
		shootHandlers.put(VK_S,     this::rotateDownShooting);
		shootHandlers.put(VK_UP,    this::rotateUpShooting);
		shootHandlers.put(VK_W,     this::rotateUpShooting);
		shootHandlers.put(VK_SPACE, this::shoot);

		comp.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent ke) {
				int code = ke.getKeyCode();

				if (code == KeyEvent.VK_Q) {
					switchMode();
				}

				keysDown.add(code);
			}

			@Override
			public void keyReleased(KeyEvent ke) {
				keysDown.remove(ke.getKeyCode());
			}
		});
	}

	public void processEvents() {
		HashMap<Integer, Runnable> handlers =
				pool.shooting ? shootHandlers : moveHandlers;

		for (int i : keysDown) {
			if (handlers.containsKey(i)) {
				handlers.get(i).run();
			}
		}
	}

	void switchMode() {
		// Can't switch to shooting mode when balls are moving
		if (Physics.ballsAreMoving) {
			return;
		}

		pool.shooting = !pool.shooting;

		if (pool.shooting) {
			camDeg1 = 0;
			camDeg2 = 0;
			applyShootingRotation();
		}
	}

	void shoot() {
		Vector3d translation = new Vector3d();
		camTransform.get(translation);
		Physics.balls[0].velocity.sub(Physics.balls[0].getTranslation(), translation);
		Physics.balls[0].velocity.normalize();
		pool.shooting = false;
	}

	void moveForward() {
		moveForward(new Vector3d(0, 0, -MOVE_SPEED));
	}

	void moveBackward() {
		moveForward(new Vector3d(0, 0,  MOVE_SPEED));
	}

	void moveLeft() {
		moveForward(new Vector3d(-MOVE_SPEED, 0, 0));
	}

	void moveRight() {
		moveForward(new Vector3d( MOVE_SPEED, 0, 0));
	}

	void moveUp() {
		moveForward(new Vector3d(0,  MOVE_SPEED, 0));
	}

	void moveDown() {
		moveForward(new Vector3d(0, -MOVE_SPEED, 0));
	}

	/**
	 * Translates the camera by the vector in a coordinate system where the z
	 * axis is the camera's line of sight, y is up from the camera's point of
	 * view, and x is to the camera's right.
	 * @param trans The vector by which to translate the camera
	 */
	private void moveForward(Vector3d trans) {
		// Rotate the vector by the camera's rotation matrix
		vecMatMult(camRotMat, trans);

		// Create a translation matrix from the vector
		Transform3D transform = new Transform3D();
		transform.set(trans);

		// Add the new translation to the camera
		camTransform.mul(transform);
		cam.setTransform(camTransform);
	}

	/**
	 * Sets v to m*v.
	 * @param m A matrix
	 * @param v A vector
	 */
	private void vecMatMult(Matrix3d m, Vector3d v) {
		v.set(
			v.x*m.m00 + v.y*m.m01 + v.z*m.m02,
			v.x*m.m10 + v.y*m.m11 + v.z*m.m12,
			v.x*m.m20 + v.y*m.m21 + v.z*m.m22);
	}

	void rotateLeft() {
		rotateRight(-ROT_SPEED);
	}

	void rotateRight() {
		rotateRight(ROT_SPEED);
	}

	void rotateRight(double angle) {
		Transform3D trans = new Transform3D();
		trans.rotY(-angle);

		camTransform.mul(trans);
		cam.setTransform(camTransform);
	}

	void rotateLeftShooting() {
		rotateRightShooting(-ROT_SPEED);
	}

	void rotateRightShooting() {
		rotateRightShooting(ROT_SPEED);
	}

	void rotateRightShooting(double angle) {
		camDeg1 = (camDeg1 + angle) % (Math.PI * 2);
		applyShootingRotation();
	}

	private void applyShootingRotation() {
		Matrix3d rot = new Matrix3d();
		rot.rotY(camDeg1);
		Matrix3d temp = new Matrix3d();
		temp.rotX(camDeg2);
		rot.mul(temp);
		rotateAroundCue(rot);
	}

	void rotateAroundCue(Matrix3d rot) {
		// Rotate the camera around the cue ball
		// Get the translational component of the camera's transform
		Vector3d translateVec = new Vector3d(SHOOTING_TRANS);

		// Rotate the vector
		vecMatMult(rot, translateVec);

		translateVec.negate();

		// Add the cue ball back
		translateVec.add(Physics.balls[0].getTranslation());

		// Set the camera's transform to the rotation and translation
		camTransform = new Transform3D(rot, translateVec, 1);
		cam.setTransform(camTransform);
	}

	void rotateDownShooting() {
		rotateUpShooting(-ROT_SPEED);
	}

	void rotateUpShooting() {
		rotateUpShooting(ROT_SPEED);
	}

	void rotateUpShooting(double angle) {
		camDeg2 = (camDeg2 - angle) % (Math.PI * 2);
		applyShootingRotation();
	}

}
