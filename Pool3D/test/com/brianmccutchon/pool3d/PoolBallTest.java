package com.brianmccutchon.pool3d;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.scijava.vecmath.Point3d;

import org.junit.Test;

public class PoolBallTest {

	@Test
	public void testRack() {
		PoolBall[] balls = PoolBall.rack();

		assertEquals(16, balls.length);

		int counter = 0;
		for (PoolBall b : balls) {
			assertNotNull(b);

			assertEquals(counter++, b.ballNum);
		}

		assertEquals(new Point3d(0, 0, 0), balls[8].getTranslation());

		for (int i : range(0, balls.length-1))
			for (int j : range(i+1, balls.length-1))
				assertFalse(balls[i].intersects(balls[j]));
	}

	/**
	 * Python style range method. Use to iterate through the set
	 * <code>{i, i+1,..., j-1, j}</code>. There are two ways to use this:
	 * <pre>
	 * for (int i : range(1, 5))
	 *     System.out.println(i);
	 * </pre>
	 * <pre>
	 * range(1, 20).forEach(System.out::println);
	 * </pre>
	 * @param i The start of the range.
	 * @param j The end of the range.
	 * @return An Iterable whose iterator iterates over each element of the range.
	 */
	private static Iterable<Integer> range(int i, int j) {
		return () -> new Iterator<Integer>() {
			int next = i;

			@Override
			public boolean hasNext() {
				return next <= j;
			}

			@Override
			public Integer next() {
				return next++;
			}
		};
	}

}
