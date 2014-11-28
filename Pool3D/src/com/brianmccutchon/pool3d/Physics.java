package com.brianmccutchon.pool3d;

import geometry.Point3D;

public class Physics {

	public static final double EPSILON = Math.pow(10.0, -15);

	/**
	 * Computes the new velocity vectors of two pool balls that have been
	 * determined to be intersecting.
	 */
	public static void handleCollision(PoolBall ball1, PoolBall ball2) {
		double[][] rotMatrix = findCollisionRotationMat(ball1, ball2);

		// Rotate so only x values matter
		rotateVec(ball1.velocity, rotMatrix);
		rotateVec(ball2.velocity, rotMatrix);

		// switch x values
		double tmp = ball1.velocity.x;
		ball1.velocity.x = ball2.velocity.x;
		ball2.velocity.x = tmp;

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
	 * two balls, ball2's x and y coordinates will equal 0.
	 * Also, ball2's x coordinate should then be greater than ball1's x
	 * coordinate.
	 * 
	 * @param ball1 A pool ball.
	 * @param ball2 Another pool ball.
	 * @return The collision rotation matrix.
	 */
	static double[][] findCollisionRotationMat(
			PoolBall ball1, PoolBall ball2) {
		Point3D ball2loc = ball2.location.subtract(ball1.location);
		//double distance = ball1.location.dist(ball2.location);
		double distanceY = Math.hypot(ball2loc.x, ball2loc.y);
		double distanceZ = Math.hypot(ball2loc.x, ball2loc.z);

		// TODO Get rid of check for zero, if possible
		double sinZ = almostEq(0.0, distanceY) ? 0.0 : -ball2loc.y / distanceY;
		double sinY = almostEq(0.0, distanceZ) ? 0.0 : -ball2loc.z / distanceZ;
		double cosZ = almostEq(0.0, distanceY) ? 1.0 :  ball2loc.x / distanceY;
		double cosY = almostEq(0.0, distanceZ) ? 1.0 :  ball2loc.x / distanceZ;

		// TODO Make sure this works for rotations around the y axis
		return new double[][] {
				{  cosY*cosZ, -sinZ*cosY, sinY },
				{       sinZ,       cosZ,    0 },
				{ -sinY*cosZ,  sinY*sinZ, cosY }
			};

//		return new double[][] {
//				{ cosY*cosZ, -sinZ, sinY*cosZ },
//				{ sinZ*cosY,  cosZ, sinY*sinZ },
//				{     -sinY,     0,      cosY }
//			};
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
