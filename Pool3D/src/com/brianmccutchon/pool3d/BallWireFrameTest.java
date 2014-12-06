package com.brianmccutchon.pool3d;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Test;

public class BallWireFrameTest {

	@Test
	public void test() {
		for (int i : range(1, 9))
			assertEquals((int) Math.pow(4, i), new BallWireFrame(i).numPolys());
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
