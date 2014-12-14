package com.brianmccutchon.pool3d;

import geometry.DSArrayList;
import geometry.EnvironmentObject;
import geometry.Point3D;
import geometry.Triangle3D;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.awt.Color.*;
import static com.brianmccutchon.pool3d.BallType.*;

public class PoolBall extends EnvironmentObject {

	public static final int RADIUS = 1;

	public static final int DIAMETER = RADIUS*2;

	private static final double DIAMETER_SQUARED = DIAMETER*DIAMETER;

	/** The display color of this pool ball. **/
	public final Color hue;

	/** The type of this ball. **/
	public final BallType type;

	/** The number of this ball. 0 if it is the cue ball. **/
	public final int ballNum;

	/** The golden ratio. **/
	private static final double PHI = (1 + Math.sqrt(5)) / 2;

	/** Just a couple of color constants. **/
	private static final Color
			BROWN  = new Color(139,  69,  19),
			PURPLE = new Color(128,   0, 128);

	/**
	 * Possible locations of all balls except the cue and eight balls,
	 * which have different placement rules.
	 */
	private static List<Point3D> rackLocations = Arrays.asList(
			new Point3D(   1.633,    -1.0, -0.5774),
			new Point3D(   1.633,     1.0, -0.5774),
			new Point3D(   1.633,     0.0,  1.1547),
			new Point3D(     0.0,    -2.0,     0.0),
			new Point3D(     0.0,     2.0,     0.0),
			new Point3D(     0.0,     1.0,  1.7321),
			new Point3D(     0.0,    -1.0,  1.7321),
			new Point3D(     0.0,     1.0, -1.7321),
			new Point3D(     0.0,    -1.0, -1.7321),
			new Point3D(  -1.633,    -1.0,  0.5774),
			new Point3D(  -1.633,     1.0,  0.5774),
			new Point3D(  -1.633,     0.0, -1.1547),
			new Point3D(  -3.267,     0.0,     0.0),
			new Point3D(   3.267,     0.0,     0.0));

	/**
	 * The coordinates of vertices of the polyhedron that the wireframe
	 * generation algorithm starts with. These are normalized in the static
	 * code block below.
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
	 * The sides of the polyhedron that the wireframe generation algorithm
	 * starts with as arrays of indices into {@link #polyCoords}.
	 */
	private static int[][] polySides = {
		{  0,  1,  8 }, {  0,  9,  1 }, {  0,  5,  9 }, {  0,  4,  5 },
		{  0,  8,  4 }, {  3,  6,  7 }, {  3, 10,  6 }, {  3,  2, 10 },
		{  3, 11,  2 }, {  3,  7, 11 }, {  1,  9,  7 }, {  1,  7,  6 },
		{  1,  6,  8 }, { 10,  8,  6 }, { 10,  4,  8 }, { 10,  2,  4 },
		{  5,  4,  2 }, {  5,  2, 11 }, {  5, 11,  9 }, {  7,  9, 11 },
	};

	/** This array holds information from which balls can be constructed. **/
	private static final PoolBall[] balls = {
		new PoolBall(0, 0, 0,  WHITE,    CUE,  0),
		new PoolBall(0, 0, 0, YELLOW,  SOLID,  1),
		new PoolBall(0, 0, 0,   BLUE,  SOLID,  2),
		new PoolBall(0, 0, 0,    RED,  SOLID,  3),
		new PoolBall(0, 0, 0, PURPLE,  SOLID,  4),
		new PoolBall(0, 0, 0, ORANGE,  SOLID,  5),
		new PoolBall(0, 0, 0,  GREEN,  SOLID,  6),
		new PoolBall(0, 0, 0,  BROWN,  SOLID,  7),
		new PoolBall(0, 0, 0,  BLACK,  EIGHT,  8),
		new PoolBall(0, 0, 0, YELLOW, STRIPE,  9),
		new PoolBall(0, 0, 0,   BLUE, STRIPE, 10),
		new PoolBall(0, 0, 0,    RED, STRIPE, 11),
		new PoolBall(0, 0, 0, PURPLE, STRIPE, 12),
		new PoolBall(0, 0, 0, ORANGE, STRIPE, 13),
		new PoolBall(0, 0, 0,  GREEN, STRIPE, 14),
		new PoolBall(0, 0, 0,  BROWN, STRIPE, 15),
	};

	/**
	 * The smoothness of the wireframe. The resulting sphere
	 * will be composed of {@code 5*4^n} polygons, where n is the desired
	 * smoothness provided. This should be a small number, such as 6 or 7.
	 */
	static final int SMOOTHNESS = 4;

	/**
	 * The global sphere wireframe. This is referenced in each of the balls'
	 * {@link EnvironmentObject#triangles triangles} lists.
	 */
	private static final DSArrayList<Triangle3D> WIREFRAME = new DSArrayList<>();

	// Construct the wireframe
	static {
		// For each side of the polygon, recursively subdivide its faces into
		// equilateral triangles. This results in a sphere wireframe with
		// nearly equilateral triangles of similar size.
		for (int[] side : polySides) {
			ArrayList<Triangle3D> dome = makeDome(SMOOTHNESS - 1,
					new Triangle3D(polyCoords[side[0]],
							polyCoords[side[1]], polyCoords[side[2]]));

			dome.forEach(WIREFRAME::add);
		}
	}

	/**
	 * Constructs a new PoolBall, requiring the caller to supply data about it.
	 * To have the data automatically determined by the ball number, use
	 * {@link PoolBall#create(int)}.
	 * 
	 * @param x The x-coordinate of the center of this pool ball.
	 * @param y The y-coordinate of the center of this pool ball.
	 * @param z The z-coordinate of the center of this pool ball.
	 * @param hue The x-coordinate of the center of this pool ball.
	 * @param solid {@code true} if and only if the ball is solid.
	 * @param ballNum The number of this ball. 0 if it is the cue ball.
	 * @param smoothness The smoothness of the wireframe. The resulting sphere
	 * will be composed of {@code 5*4^n} polygons, where n is the desired
	 * smoothness provided. This should be a small number, such as 6 or 7.
	 */
	public PoolBall(double x, double y, double z,
			Color hue, BallType type, int ballNum) {
		this.center    = new Point3D(x, y, z);
		this.hue       = hue;
		this.type      = type;
		this.ballNum   = ballNum;
		this.velocity  = new Point3D(0, 0, 0);
		this.triangles = WIREFRAME;
	}

	/**
	 * Constructs a new PoolBall using the default values for a ball with this
	 * number.
	 * 
	 * @param ballNum The number of this ball. 0 if it is the cue ball.
	 */
	public PoolBall(int ballNum) {
		// TODO Write a PoolBall constructor that will, given the number of a
		// ball, read all the other data from a file.
		throw new RuntimeException("Not yet implemented.");
	}

	/**
	 * Splits the given triangle into smaller equilateral triangles, which are
	 * normalized to the unit circle. This is not really a "dome," I just
	 * couldn't think of a better name for it.
	 * 
	 * @param depth The depth to which to recurse.
	 * @param tri The triangle to subdivide.
	 * @return An ArrayList containing {@code Math.pow(4, depth)} triangles.
	 */
	private static ArrayList<Triangle3D> makeDome(int depth, Triangle3D tri) {
		ArrayList<Triangle3D> triangles = new ArrayList<>();

		if (depth == 0) { // Depth limit reached, stop recursing
			triangles.add(tri);
		} else {
			depth--;
			Point3D[] ps = tri.points;

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
	private static Point3D normAvg(Point3D p1, Point3D p2) {
		return p1.add(p2).divide(2).normalize();
	}

	/**
	 * The transformation matrix applied in the getTriangles method.
	 * A translation matrix is used because I may wish to add spinning later
	 * and it would be fairly easy to combine this translation matrix with a
	 * rotation matrix.
	 */
	private double[][] transformMat = {
			{ 1, 0, 0, center.x },
			{ 0, 1, 0, center.y },
			{ 0, 0, 1, center.z },
			{ 0, 0, 0,    1     },			
	};

	@Override
	public DSArrayList<Triangle3D> getTriangles() {
		// Update the transformation matrix
		transformMat[0][3] = center.x;
		transformMat[1][3] = center.y;
		transformMat[2][3] = center.z;

		// Compute the return value in parallel
		DSArrayList<Triangle3D> retVal = new DSArrayList<>();
		triangles.stream().parallel()
			.map(t -> {
				// Apply transformMat to each coord of the triangle
				Triangle3D transformed = new Triangle3D(
						transform(t.points[0]),
						transform(t.points[1]),
						transform(t.points[2]));

				// Color the triangle if it is within a certain range.
				// The range depends on what type of ball this is
				if (type != BallType.CUE &&
						Math.abs((t.points[0].x+t.points[1].x+t.points[2].x)/3) <
						(type == BallType.STRIPE ? 0.5 : 0.8)) {
					transformed.triColor = hue;
				}

				return transformed;
			})
			.forEachOrdered(retVal::add);;

		return retVal;
	}

	/**
	 * Transforms a Point3D using {@link #transformMat}.
	 * @param p The point to transform
	 * @return The transformed point
	 */
	private Point3D transform(Point3D p) {
		return new Point3D(
				p.x*transformMat[0][0] + p.y*transformMat[0][1] +
					p.z*transformMat[0][2] + transformMat[0][3],
				p.x*transformMat[1][0] + p.y*transformMat[1][1] +
					p.z*transformMat[1][2] + transformMat[1][3],
				p.x*transformMat[2][0] + p.y*transformMat[2][1] +
					p.z*transformMat[2][2] + transformMat[2][3]);
	}

	/**
	 * Returns a new PoolBall with the given ball number and the default values
	 * for a ball with this number.
	 * 
	 * @param ballNum The number of this ball. 0 if it is the cue ball.
	 */
	public static PoolBall create(int ballNum) {
		PoolBall ball = balls[ballNum];
		return new PoolBall(ball.center.x, ball.center.y, ball.center.z,
				ball.hue, ball.type, ballNum);
	}

	/**
	 * Returns an ordered array of PoolBalls. Their order is determined by
	 * their ball numbers, but their locations will vary according to the
	 * following rules:
	 * <ul>
	 *   <li>They form a sort of 3D diamond shape.</li>
	 *   <li>The 1 ball is in front.</li>
	 *   <li>The 8 ball is in the middle.</li>
	 * </ul>
	 * There will always be 15 balls.
	 */
	public static PoolBall[] rack() {
		Collections.shuffle(rackLocations);

		PoolBall[] retVal = new PoolBall[16];

		int counter = 0; // current index into rackLocations
		for (int i = 0; i < retVal.length; i++) {
			retVal[i] = PoolBall.create(i);
			if (i != 8 && i != 0) { // The two balls w/ set posns
				retVal[i].center = rackLocations.get(counter++);
			}
		}

		retVal[0].center = new Point3D(10, 0, 0);
		retVal[8].center = new Point3D(0, 0, 0);

		return retVal;
	}

	/** Determines if this pool ball intersects with another pool ball. **/
	public boolean intersects(PoolBall pb) {
		return center.distSq(pb.center) < DIAMETER_SQUARED;
	}

	/**
	 * PoolBalls are hashed according to their numbers.
	 * @see #ballNum
	 */
	@Override
	public int hashCode() {
		return ballNum;
	}

	/**
	 * Two PoolBalls are equal if and only if their ball numbers are equal.
	 * @see #ballNum
	 */
	@Override
	public boolean equals(Object obj) {
		return (obj instanceof PoolBall) &&
				((PoolBall) obj).ballNum == this.ballNum;
	}

	@Override
	public String toString() {
		return "PoolBall: " + (ballNum == 0 ? "Cue" : ballNum) +
				"; " + center;
	}

	@Override
	public Object clone() {
		return new PoolBall(center.x, center.y, center.z,
				hue, type, ballNum);
	}

}
