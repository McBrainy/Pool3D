package com.brianmccutchon.pool3d;

import geometry.Point3D;

public class Physics {

	public static final double EPSILON = Math.pow(10.0, -15);

	/** A unit vector along the x axis. **/
	private static final Point3D X_UNIT_VEC = new Point3D(1, 0, 0);

	/** A unit vector along the y axis. **/
	private static final Point3D Y_UNIT_VEC = new Point3D(0, 1, 0);

	/** The origin of the coordinate system: (0, 0, 0) **/
	private static final Point3D ORIGIN = new Point3D(0, 0, 0);

	/**
	 * Computes the new velocity vectors of two pool balls that have been
	 * determined to be intersecting.
	 */
	public static void handleCollision(PoolBall ball1, PoolBall ball2) {
		double[][] rotMatrix = findCollisionRotationMat(ball1, ball2);

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
	 * Normalizes a vector in place. This means that the vector will be
	 * converted into a unit vector with the same direction as the original.
	 * @param p The vector to nomalize.
	 */
	private static void normalize(Point3D p) {
		double norm = p.dist(new Point3D(0, 0, 0));
		p.x /= norm;
		p.y /= norm;
		p.z /= norm;
	}

	/**
	 * Computes the rotation matrix that, if the balls are translated so that
	 * ball1 is at (0, 0, 0) and the matrix is applied to the locations of the
	 * two balls, ball2's x and y coordinates will equal 0. Also, ball2's x
	 * coordinate should then be greater than ball1's x coordinate.
	 * 
	 * @param ball1 A pool ball.
	 * @param ball2 Another pool ball.
	 * @return The collision rotation matrix.
	 */
	static double[][] findCollisionRotationMat(
			PoolBall ball1, PoolBall ball2) {
		Point3D ball2loc = ball2.location.subtract(ball1.location);
		normalize(ball2loc);

		// Vector representing the axis of rotation
		Point3D a = ball2loc.cross(X_UNIT_VEC);

		// The magnitude is the sin of the rotation angle
		double sin = a.dist(ORIGIN);

		// The dot product gives the cos of rotation
		double cos = ball2loc.dot(X_UNIT_VEC);

		// The matrix below only works if the axis is a unit vector
		if (almostEq(a.dist(ORIGIN), 0)) {
			a = Y_UNIT_VEC;
		} else {
			normalize(a);
		}

		return new double[][] {
			{cos+a.x*a.x*(1-cos), a.x*a.y*(1-cos)-a.z*sin, a.x*a.z*(1-cos)+a.y*sin},
			{a.y*a.x*(1-cos)+a.z*sin, cos+a.y*a.y*(1-cos), a.y*a.z*(1-cos)-a.x*sin},
			{a.z*a.x*(1-cos)-a.y*sin, a.z*a.y*(1-cos)+a.x*sin, cos+a.z*a.z*(1-cos)}
		};
	}

	/**
	 * Rotates a vector using a provided rotation matrix.
	 * @param v The vector to rotate.
	 * @param m The 3x3 rotation matrix.
	 */
	static void rotateVec(Point3D v, double[][] m) {
		v.setLocation(
				v.x * m[0][0] + v.y * m[0][1] + v.z * m[0][2],
				v.x * m[1][0] + v.y * m[1][1] + v.z * m[1][2],
				v.x * m[2][0] + v.y * m[2][1] + v.z * m[2][2]);
	}

	/**
	 * Returns true if the values provided are approximately equal.
	 * 
	 * @param d1 A value to compare.
	 * @param d2 The other value to compare.
	 * @return {@code true} if and only if d1 and d2 are within
	 * {@link Physics#EPSILON} of each other.
	 */
	private static boolean almostEq(double d1, double d2) {
		return Math.abs(d1 - d2) < EPSILON;
	}

}
