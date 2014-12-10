package com.brianmccutchon.pool3d;

import static org.junit.Assert.*;
import geometry.Point3D;

import org.junit.Test;

public class PhysTest {
	private static double[][] identity = {
			{ 1, 0, 0 },
			{ 0, 1, 0 },
			{ 0, 0, 1 }
		};

	@Test
	public void testDeterminant() {
		assertEquals(1.0, determinant(identity), 0.0);
		assertEquals(1.0, determinant(
				new double[][]{{1, 0, 0}, {0, 0, -1}, {0, 1, 0}}), 0.0);
		assertEquals(1.0, determinant(
				new double[][]{{1, 0, 0}, {0, Math.cos(Math.PI/6), -0.5},
						{0, 0.5, Math.cos(Math.PI/6)}}), 0.0);
		assertEquals(1.0, determinant(
				new double[][]{{0, 1, 0}, {0, 0, -1}, {-1, 0, 0}}), 0.0);
	}

	/**
	 * Computes the determinant of a 3x3 matrix.
	 * @param mat A 3x3 matrix.
	 * @return The determinant of mat.
	 */
	private double determinant(double[][] mat) {
		int n = mat.length;
		if (n != 3) {
			throw new IllegalArgumentException("3x3 matrix required");
		}
		double det = 0;
		for (int i = 0; i < n; i++) {
			double diag1 = 1;
			double diag2 = 1;
			for (int j = 0; j < n; j++) {
				diag1 *= mat[j][(i+j)%n];
				diag2 *= mat[n-1-j][(i+j)%n];
			}
			det += diag1 - diag2;
		}
		return det;
	}

	@Test
	public void testRotationMat() {
		PoolBall ball1 = new PoolBall(0, 0, 0, null, null, 0);
		PoolBall ball2 = new PoolBall(
				PoolBall.DIAMETER - Physics.EPSILON, 0, 0, null, null, 1);

		// No rotation required, should return the identity matrix
		assertArrayEquals(identity,
				Physics.findCollisionRotationMat(ball1, ball2));

		checkRotationMat(new Point3D(2, 2, 2),
				new Point3D(2 + Math.sqrt(PoolBall.DIAMETER),
						2 + Math.sqrt(PoolBall.DIAMETER), 2));

		checkRotationMat(new Point3D(-2, -3, -5),
				new Point3D(-2, -3 - Math.sqrt(PoolBall.DIAMETER),
						-5 + Math.sqrt(PoolBall.DIAMETER)));

		checkRotationMat(new Point3D(5, 4, 3), new Point3D(6, 5, 2));
	}

	/**
	 * Performs assertions to ensure that the rotation matrix is computed
	 * correctly for the two pool balls provided.
	 */
	private void checkRotationMat(Point3D p1, Point3D p2) {
		PoolBall ball1 = new PoolBall(p1.x, p1.y, p1.z, null, null, 0);
		PoolBall ball2 = new PoolBall(p2.x, p2.y, p2.z, null, null, 0);

		double[][] rotationMat =
				Physics.findCollisionRotationMat(ball1, ball2);

		// Every rotation matrix should have a determinant of 1.0
		assertEquals(1.0, determinant(rotationMat), Physics.EPSILON);

		Point3D p = ball2.center.subtract(ball1.center);
		Physics.rotateVec(p, rotationMat);

		assertEquals(0.0, p.y, Physics.EPSILON);
		assertEquals(0.0, p.z, Physics.EPSILON);
	}

	@Test
	public void testHandleCollision() {
		PoolBall ball1 = new PoolBall(0, 0, 0, null, null, 0);
		PoolBall ball2 = new PoolBall(
				PoolBall.DIAMETER - Physics.EPSILON, 0, 0, null, null, 1);

		ball1.velocity.setLocation( 1, 0, 0);
		ball2.velocity.setLocation(-1, 0, 0);

		Physics.handleCollision(ball1, ball2);

		assertEquals(new Point3D(-1, 0, 0), ball1.velocity);
		assertEquals(new Point3D( 1, 0, 0), ball2.velocity);

		// Now they're headed in opposite directions; a collision check
		// shouldn't do anything
		Physics.handleCollision(ball1, ball2);

		assertEquals(new Point3D(-1, 0, 0), ball1.velocity);
		assertEquals(new Point3D( 1, 0, 0), ball2.velocity);

		// Now with a nonzero y in velocity
		ball1 = new PoolBall(2, 2, 2, null, null, 0);
		ball2 = new PoolBall(2 + Math.sqrt(PoolBall.DIAMETER),
				2 + Math.sqrt(PoolBall.DIAMETER), 2, null, null, 0);
		ball1.velocity.setLocation(0, 0, 0);
		ball2.velocity.setLocation(-Math.sqrt(2), -Math.sqrt(2), 0);

		Physics.handleCollision(ball1, ball2);

		assertEquals(-Math.sqrt(2), ball1.velocity.x, Physics.EPSILON);
		assertEquals(-Math.sqrt(2), ball1.velocity.y, Physics.EPSILON);
		assertEquals(          0.0, ball1.velocity.z, Physics.EPSILON);

		// Now with more irregular coords -- dist is about 1.73
		ball1 = new PoolBall(5, 4, 3, null, null, 0);
		ball2 = new PoolBall(6, 5, 2, null, null, 0);

		ball1.velocity.setLocation(2, 2, 2);
		ball2.velocity.setLocation(0, 0, 0);

		Physics.handleCollision(ball2, ball1);

		assertEquals( 1.33, ball1.velocity.x, 0.01);
		assertEquals( 1.33, ball1.velocity.y, 0.01);
		assertEquals( 2.67, ball1.velocity.z, 0.01);

		assertEquals( 0.67, ball2.velocity.x, 0.01);
		assertEquals( 0.67, ball2.velocity.y, 0.01);
		assertEquals(-0.67, ball2.velocity.z, 0.01);

		// Now the two balls are going in opposite directions
		Physics.handleCollision(ball2, ball1);

		assertEquals( 1.33, ball1.velocity.x, 0.01);
		assertEquals( 1.33, ball1.velocity.y, 0.01);
		assertEquals( 2.67, ball1.velocity.z, 0.01);

		assertEquals( 0.67, ball2.velocity.x, 0.01);
		assertEquals( 0.67, ball2.velocity.y, 0.01);
		assertEquals(-0.67, ball2.velocity.z, 0.01);

		// What if the balls are going in the same direction,
		// but still getting farther apart?
		ball2.velocity.setLocation(1.3, 1.3, 2.0);

		Physics.handleCollision(ball1, ball2);

		assertEquals( 1.33, ball1.velocity.x, 0.01);
		assertEquals( 1.33, ball1.velocity.y, 0.01);
		assertEquals( 2.67, ball1.velocity.z, 0.01);

		// TODO Make this more precise by eliminating rounding error
		assertEquals(1.3, ball2.velocity.x, Physics.EPSILON);
		assertEquals(1.3, ball2.velocity.y, Physics.EPSILON);
		assertEquals(2.0, ball2.velocity.z, Physics.EPSILON);
	}

}
