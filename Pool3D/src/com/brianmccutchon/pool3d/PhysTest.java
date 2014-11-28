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

	private double[][] cloneMat(double[][] testMat) {
		double[][] retVal = new double[testMat.length][testMat[0].length];
		for (int i = 0; i < testMat.length; i++) {
			for (int j = 0; j < testMat.length; j++) {
				retVal[i][j] = testMat[i][j];
			}
		}
		return retVal;
	}

	@Test
	public void testRotationMat() {
		PoolBall ball1 = new PoolBall(0, 0, 0, null, null, 0);
		PoolBall ball2 = new PoolBall(
				PoolBall.DIAMETER - Physics.EPSILON, 0, 0, null, null, 1);

		ball1.velocity.setLocation( 1, 0, 0);
		ball2.velocity.setLocation(-1, 0, 0);

		checkRotationMat(ball1, ball2);

		// Now with a nonzero y in velocity
		ball1.location.setLocation(2, 2, 2);
		ball2.location.setLocation(2 + Math.sqrt(PoolBall.DIAMETER),
				2 + Math.sqrt(PoolBall.DIAMETER), 2);
		ball1.velocity.setLocation(0, 0, 0);
		ball2.setVelocity(new Point3D(-Math.sqrt(2), -Math.sqrt(2), 0));

		checkRotationMat(ball1, ball2);

		// Now a rotation around the y axis
		ball1.location.setLocation(-2, -3, -5);

		// Now with more irregular coords -- dist is about 1.73
		ball1.location.setLocation(5, 4, 3);
		ball2.location.setLocation(6, 5, 2);

		ball1.velocity.setLocation(2, 2, 2);
		ball2.velocity.setLocation(0, 0, 0);

		checkRotationMat(ball1, ball2);
	}

	private void checkRotationMat(PoolBall ball1, PoolBall ball2) {
		double[][] rotationMat = Physics.findCollisionRotationMat(ball1, ball2);

		// Every rotation matrix should have a determinant of 1.0
		assertEquals(1.0, determinant(rotationMat), Physics.EPSILON);

		Point3D p = ball2.location.subtract(ball1.location);
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

		// Going the other way
		Physics.handleCollision(ball1, ball2);

		assertEquals(new Point3D( 1, 0, 0), ball1.velocity);
		assertEquals(new Point3D(-1, 0, 0), ball2.velocity);

		// Now with a nonzero y in velocity
		ball1.location.setLocation(2, 2, 2);
		ball2.location.setLocation(2 + Math.sqrt(PoolBall.DIAMETER),
				2 + Math.sqrt(PoolBall.DIAMETER), 2);
		ball1.velocity.setLocation(0, 0, 0);
		ball2.setVelocity(new Point3D(-Math.sqrt(2), -Math.sqrt(2), 0));

		Physics.handleCollision(ball1, ball2);

		assertEquals(-Math.sqrt(2), ball1.velocity.x, Physics.EPSILON);
		assertEquals(-Math.sqrt(2), ball1.velocity.y, Physics.EPSILON);
		assertEquals(          0.0, ball1.velocity.z, Physics.EPSILON);

		// Now with more irregular coords -- dist is about 1.73
		ball1.location.setLocation(5, 4, 3);
		ball2.location.setLocation(6, 5, 2);

		ball1.velocity.setLocation(2, 2, 2);
		ball2.velocity.setLocation(0, 0, 0);

		Physics.handleCollision(ball2, ball1);

		assertEquals( 1.33, ball2.velocity.x, 0.01);
		assertEquals( 1.33, ball2.velocity.y, 0.01);
		assertEquals( 2.67, ball2.velocity.z, 0.01);

		assertEquals( 0.67, ball1.velocity.x, 0.01);
		assertEquals( 0.67, ball1.velocity.y, 0.01);
		assertEquals(-0.67, ball1.velocity.z, 0.01);
	}

}
