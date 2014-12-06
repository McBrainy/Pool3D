package com.brianmccutchon.pool3d;

import java.util.ArrayList;

import geometry.EnvironmentObject;
import geometry.Point3D;
import geometry.Triangle3D;

public class BallWireFrame extends EnvironmentObject {

	private static Point3D[] tetraCoords = {
		new Point3D( 1, 0, -1.0/Math.sqrt(2)).normalize(),
		new Point3D(-1, 0, -1.0/Math.sqrt(2)).normalize(),
		new Point3D(0,  1,  1.0/Math.sqrt(2)).normalize(),
		new Point3D(0, -1,  1.0/Math.sqrt(2)).normalize()
	};

	private static int[][] tetraSides = {
		{ 0, 1, 2 }, { 1, 3, 2 }, { 0, 2, 3 }, { 0, 3, 1 }
	};

	public BallWireFrame(int depth) {
		depth--;

		for (int[] side : tetraSides) {
			ArrayList<Triangle3D> dome = makeDome(depth,
					new Triangle3D(tetraCoords[side[0]],
							tetraCoords[side[1]], tetraCoords[side[2]]));

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

	/**
	 * Returns the total number of polygons of which the wireframe is composed.
	 */
	public int numPolys() {
		return triangles.size();
	}

}
