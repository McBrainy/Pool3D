package com.brianmccutchon.pool3d;

import javax.vecmath.*;

/**
 * This is a purely static class containing information related to the physics
 * of 3D pool. However, it also holds information about the table and the pool
 * balls.
 * 
 * @author Brian McCutchon
 */
public class Physics {

	/**
	 * The default leeway allowed in comparison using
	 * {@link #almostEq(double, double)} and related methods.
	 */
	public static final double EPSILON = Math.pow(10.0, -15);

	/** A unit vector along the x axis. **/
	static final Vector3d X_UNIT_VEC = new Vector3d(1, 0, 0);

	/** A unit vector along the y axis. **/
	static final Vector3d Y_UNIT_VEC = new Vector3d(0, 1, 0);

	/** A unit vector along the y axis. **/
	static final Vector3d Z_UNIT_VEC = new Vector3d(0, 0, 1);

	/** The origin of the coordinate system: (0, 0, 0) **/
	static final Point3d ORIGIN = new Point3d(0, 0, 0);

	// TODO Get rid of global variables like this one.
	// Maybe make Physics instantiable? or create a new class.
	/**
	 * The pool balls, in order of their ball numbers.
	 * balls[0] is the cue ball,
	 * balls[1] is the 1 ball,
	 * balls[2] is the 2 ball,
	 * ...
	 */
	public static PoolBall[] balls = PoolBall.rack();

	/** The dimensions of the pool "table." **/
	public static final int TABLE_X = 40, TABLE_Y = 20, TABLE_Z = 20;

	/** The amount by which a ball slows down each frame. **/
	private static final double AIR_RESISTANCE = 0.003;

	/**
	 * How close each component of a ball's velocity must be to 0 for it to be
	 * considered stationary.
	 */
	private static final double MOVEMENT_EPSILON = 0.001;

	/** {@code true} iff at least one ball is moving. **/
	public static boolean ballsAreMoving = false;

	/**
	 * Computes the new velocity vectors of two pool balls that have been
	 * determined to be intersecting.
	 */
	public static void handleCollision(PoolBall ball1, PoolBall ball2) {
		double[][] rotMatrix = findCollisionRotationMat(
				ball1.getTranslation(), ball2.getTranslation());

		// Rotate so only x values matter
		rotateVec(ball1.velocity, rotMatrix);
		rotateVec(ball2.velocity, rotMatrix);

		// Check that the balls really are colliding; that is, if left to
		// themselves and no other balls/walls/pockets interfere, they will be
		// closer together in x seconds, as x approaches 0 from the positive
		// side of 0. Mathematically, lim_(x->0+) f(x) = true, where f(x) is
		// true iff the balls will be closer in x seconds.
		if (ball1.velocity.x > ball2.velocity.x) {
			// switch x values
			double tmp = ball1.velocity.x;
			ball1.velocity.x = ball2.velocity.x;
			ball2.velocity.x = tmp;
		}

		// Reverse the rotation matrix
		transposeSquareMat(rotMatrix);

		// rotate back
		rotateVec(ball1.velocity, rotMatrix);
		rotateVec(ball2.velocity, rotMatrix);
	}

	/**
	 * Transposes a square matrix in place.
	 * @param m The n*n matrix to transpose.
	 */
	static void transposeSquareMat(double[][] m) {
		for (int i = 0; i < m.length; i++) {
			for (int j = i+1; j < m.length; j++) {
				double tmp = m[i][j];
				m[i][j] = m[j][i];
				m[j][i] = tmp;
			}
		}
	}

	/**
	 * Computes the rotation matrix that, if the balls are translated so that
	 * ball1 is at (0, 0, 0) and the matrix is applied to the locations of the
	 * two balls, ball2's x and y coordinates will equal 0. Also, ball2's x
	 * coordinate should then be greater than ball1's x coordinate.
	 * 
	 * @param center A pool ball.
	 * @param center2 Another pool ball.
	 * @return The collision rotation matrix.
	 */
	static double[][] findCollisionRotationMat(
			Vector3d center, Vector3d center2) {
		Vector3d ball2loc = new Vector3d();
		ball2loc.sub(center2, center);
		ball2loc.normalize();

		// Vector representing the axis of rotation
		Vector3d a = new Vector3d();
		a.cross(ball2loc, X_UNIT_VEC);

		// Since ball2Loc and X_UNIT_VEC are unit vectors, the following hold:

		// Their dot product is the cos of the angle between them
		double cos = ball2loc.dot(X_UNIT_VEC);

		// The magnitude of their cross product is the sin of the rotation angle
		double sin = a.length();

		// The matrix below only works if the axis is a unit vector
		if (almostEq(sin, 0)) {
			a = Y_UNIT_VEC;
		} else {
			a.normalize();
		}

		// Rotation matrix given an axis and an angle. Source:
		//http://en.wikipedia.org/wiki/Rotation_matrix#Rotation_matrix_from_axis_and_angle
		return new double[][] {
			{cos+a.x*a.x*(1-cos), a.x*a.y*(1-cos)-a.z*sin, a.x*a.z*(1-cos)+a.y*sin},
			{a.y*a.x*(1-cos)+a.z*sin, cos+a.y*a.y*(1-cos), a.y*a.z*(1-cos)-a.x*sin},
			{a.z*a.x*(1-cos)-a.y*sin, a.z*a.y*(1-cos)+a.x*sin, cos+a.z*a.z*(1-cos)}
		};
	}

	/**
	 * Rotates a vector using a provided rotation matrix.
	 * @param velocity The vector to rotate.
	 * @param m The 3x3 rotation matrix.
	 */
	static void rotateVec(Vector3d velocity, double[][] m) {
		velocity.set(
				velocity.x * m[0][0] + velocity.y * m[0][1] + velocity.z * m[0][2],
				velocity.x * m[1][0] + velocity.y * m[1][1] + velocity.z * m[1][2],
				velocity.x * m[2][0] + velocity.y * m[2][1] + velocity.z * m[2][2]);
	}

	/**
	 * Computes the state of the balls after the next frame.
	 */
	public static void nextFrame() {
		ballsAreMoving = false;

		for (PoolBall b : balls) {
			if (b.velocity.epsilonEquals(ORIGIN, MOVEMENT_EPSILON)) {
				// "Close enough" to (0, 0, 0).
				b.velocity.set(0, 0, 0);
			} else {
				ballsAreMoving = true; // We found a ball that is moving
				Vector3d trans = b.getTranslation();
				trans.add(b.velocity);
				b.setTranslation(trans);
				doAirResistance(b.velocity);
			}
		}

		for (int i = 0; i < balls.length; i++) {
			for (int j = i+1; j < balls.length; j++) {
				if (balls[i].intersects(balls[j])) {
					handleCollision(balls[i], balls[j]);
				}
			}
		}

		// Check whether it is hitting a wall
		for (PoolBall b : balls) {
			if (hitsWall(b.getTranslation().x, b.velocity.x, TABLE_X)) {
				b.velocity.x = -b.velocity.x;
			}
			if (hitsWall(b.getTranslation().y, b.velocity.y, TABLE_Y)) {
				b.velocity.y = -b.velocity.y;
			}
			if (hitsWall(b.getTranslation().z, b.velocity.z, TABLE_Z)) {
				b.velocity.z = -b.velocity.z;
			}
		}
	}

	/**
	 * Determines if a ball is hitting a wall using the specified component.
	 * @param comp     The component of the ball's translation
	 * @param velComp  The same component of the ball's velocity
	 * @param tableDim The dimension of the table along this component
	 * @return {@code true} iff the ball hits one of the two walls along this
	 *   component.
	 */
	private static boolean hitsWall(double comp, double velComp, int tableDim) {
		return (Math.abs(comp) + PoolBall.RADIUS > tableDim/2 &&
				Math.signum(comp) == Math.signum(velComp));
	}

	/**
	 * Returns true if the values provided are approximately equal.
	 * That is, they are within {@link #EPSILON} of each other.
	 * 
	 * @param d1 A value to compare.
	 * @param d2 The other value to compare.
	 * @return {@code true} if and only if d1 and d2 are within
	 * {@link Physics#EPSILON} of each other.
	 */
	static boolean almostEq(double d1, double d2) {
		return Math.abs(d1 - d2) < EPSILON;
	}

	/**
	 * Checks whether two numbers are almost equal to each other. That is,
	 * within {@link Physics#EPSILON}.
	 * @param x A number to compare.
	 * @param n A number to compare.
	 * @return {@code true} iff they are almost equal.
	 */
	private static boolean almostEq(double x, double n, double epsilon) {
		return Math.abs(x - n) < epsilon;
	}

	/**
	 * Computes linear "air resistance" on a ball's velocity.
	 * @param p The ball's velocity.
	 */
	private static void doAirResistance(Vector3d p) {
		p.x = Math.signum(p.x) * Math.max(0, Math.abs(p.x) - AIR_RESISTANCE);
		p.y = Math.signum(p.y) * Math.max(0, Math.abs(p.y) - AIR_RESISTANCE);
		p.z = Math.signum(p.z) * Math.max(0, Math.abs(p.z) - AIR_RESISTANCE);
	}

}
