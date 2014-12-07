package com.brianmccutchon.pool3d;

import java.util.ArrayList;
import java.util.Arrays;

import geometry.EnvironmentObject;
import geometry.Point3D;
import geometry.Triangle3D;

public class BallWireFrame extends EnvironmentObject {

	private static final double PHI = (1 + Math.sqrt(5)) / 2;

	/**
	 * The coordinates of vertices of the polyhedron the algorithm starts with.
	 */
	private static Point3D[] polyCoords = {
		new Point3D(   0,    1,  PHI),
		new Point3D(   0,   -1,  PHI),
		new Point3D(   0,    1, -PHI),
		new Point3D(   0,   -1, -PHI),
		new Point3D(   1,  PHI,    0),
		new Point3D(  -1,  PHI,    0),
		new Point3D(   1, -PHI,    0),
		new Point3D(  -1, -PHI,    0),
		new Point3D( PHI,    0,    1),
		new Point3D(-PHI,    0,    1),
		new Point3D( PHI,    0,   -1),
		new Point3D(-PHI,    0,   -1),
	};

	// Normalize the coordinates of the polyhedron
	static {
		Arrays.asList(polyCoords).replaceAll(Point3D::normalize);
	}

	/**
	 * The sides of the initial polyhedron as arrays of indices into polyCoords.
	 */
	private static int[][] polySides = {
		{  0,  1,  8 }, {  0,  9,  1 }, {  0,  5,  9 }, {  0,  4,  5 },
		{  0,  8,  4 }, {  3,  6,  7 }, {  3, 10,  6 }, {  3,  2, 10 },
		{  3, 11,  2 }, {  3,  7, 11 }, {  1,  9,  7 }, {  1,  7,  6 },
		{  1,  6,  8 }, { 10,  8,  6 }, { 10,  4,  8 }, { 10,  2,  4 },
		{  5,  4,  2 }, {  5,  2, 11 }, {  5, 11,  9 }, {  7,  9, 11 },
	};

	public BallWireFrame(int depth) {
		depth--;

		for (int[] side : polySides) {
			ArrayList<Triangle3D> dome = makeDome(depth,
					new Triangle3D(polyCoords[side[0]],
							polyCoords[side[1]], polyCoords[side[2]]));

			dome.forEach(triangles::add);
		}
	}

	/**
	 * Splits the given triangle into smaller equilateral triangles, which are
	 * then normalized to the unit circle.
	 * @param depth The depth to which to recurse.
	 * @param triangle3d The triangle to subdivide.
	 * @return An ArrayList containing {@code Math.pow(4, depth)} triangles.
	 */
	private ArrayList<Triangle3D> makeDome(int depth, Triangle3D triangle3d) {
		ArrayList<Triangle3D> triangles = new ArrayList<>();

		if (depth == 0) {
			triangles.add(triangle3d);
		} else {
			depth--;
			Point3D[] ps = triangle3d.points;

			// The center triangle
			triangles.addAll(makeDome(depth, new Triangle3D(
					normAvg(ps[0], ps[1]),
					normAvg(ps[1], ps[2]),
					normAvg(ps[2], ps[0]))));

			// The three smaller triangles
			int n = ps.length;
			for (int i = 0; i < n; i++) {
				triangles.addAll(makeDome(depth, new Triangle3D(ps[i],
						normAvg(ps[i], ps[(i+1)%n]),
						normAvg(ps[i], ps[(i+2)%n]))));
			}
		}

		return triangles;
	}

	/** Computes the normalized average of two points. **/
	private Point3D normAvg(Point3D p1, Point3D p2) {
		return p1.add(p2).divide(2).normalize();
	}

}
