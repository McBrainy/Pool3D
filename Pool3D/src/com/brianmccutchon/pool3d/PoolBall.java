package com.brianmccutchon.pool3d;

import java.util.*;

import javax.media.j3d.Transform3D;
import javax.vecmath.*;

import static com.brianmccutchon.pool3d.BallType.*;
import static java.awt.Color.*;

public class PoolBall {

	public static final int RADIUS = 1;

	public static final int DIAMETER = RADIUS*2;

	private static final double DIAMETER_SQUARED = DIAMETER*DIAMETER;

	/** The display color of this pool ball. **/
	public final Color3f hue;

	/** The type of this ball. **/
	public final BallType type;

	/** The number of this ball. 0 if it is the cue ball. **/
	public final int ballNum;

	/** The translation of this PoolBall from the origin. **/
	private Vector3d translation;

	/** The velocity of the ball. **/
	public Vector3d velocity;

	public Transform3D transform;

	/** Just a couple of color constants. **/
	private static final Color3f
			BROWN  = new Color3f(0.55f, 0.27f, 0.07f),
			PURPLE = new Color3f(0.5f ,    0f, 0.5f);

	/**
	 * Possible locations of all balls except the cue and eight balls,
	 * which have different placement rules.
	 */
	private static List<Point3d> rackLocations = Arrays.asList(
			new Point3d(   1.633,    -1.0, -0.5774),
			new Point3d(   1.633,     1.0, -0.5774),
			new Point3d(   1.633,     0.0,  1.1547),
			new Point3d(     0.0,    -2.0,     0.0),
			new Point3d(     0.0,     2.0,     0.0),
			new Point3d(     0.0,     1.0,  1.7321),
			new Point3d(     0.0,    -1.0,  1.7321),
			new Point3d(     0.0,     1.0, -1.7321),
			new Point3d(     0.0,    -1.0, -1.7321),
			new Point3d(  -1.633,    -1.0,  0.5774),
			new Point3d(  -1.633,     1.0,  0.5774),
			new Point3d(  -1.633,     0.0, -1.1547),
			new Point3d(  -3.267,     0.0,     0.0),
			new Point3d(   3.267,     0.0,     0.0));

	/** This array holds information from which balls can be constructed. **/
	private static final PoolBall[] balls = {
		new PoolBall(0, 0, 0, new Color3f( WHITE),    CUE,  0),
		new PoolBall(0, 0, 0, new Color3f(YELLOW),  SOLID,  1),
		new PoolBall(0, 0, 0, new Color3f(  BLUE),  SOLID,  2),
		new PoolBall(0, 0, 0, new Color3f(   RED),  SOLID,  3),
		new PoolBall(0, 0, 0,              PURPLE,  SOLID,  4),
		new PoolBall(0, 0, 0, new Color3f(ORANGE),  SOLID,  5),
		new PoolBall(0, 0, 0, new Color3f( GREEN),  SOLID,  6),
		new PoolBall(0, 0, 0,               BROWN,  SOLID,  7),
		new PoolBall(0, 0, 0, new Color3f( BLACK),  EIGHT,  8),
		new PoolBall(0, 0, 0, new Color3f(YELLOW), STRIPE,  9),
		new PoolBall(0, 0, 0, new Color3f(  BLUE), STRIPE, 10),
		new PoolBall(0, 0, 0, new Color3f(   RED), STRIPE, 11),
		new PoolBall(0, 0, 0,              PURPLE, STRIPE, 12),
		new PoolBall(0, 0, 0, new Color3f(ORANGE), STRIPE, 13),
		new PoolBall(0, 0, 0, new Color3f( GREEN), STRIPE, 14),
		new PoolBall(0, 0, 0,               BROWN, STRIPE, 15),
	};

	/**
	 * The smoothness of the wireframe. The resulting sphere
	 * will be composed of {@code 5*4^n} polygons, where n is the desired
	 * smoothness provided. This should be a small number, such as 6 or 7.
	 */
	static final int SMOOTHNESS = 4;

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
			Color3f hue, BallType type, int ballNum) {
		this.translation    = new Vector3d(x, y, z);
		this.hue       = hue;
		this.type      = type;
		this.ballNum   = ballNum;
		this.velocity  = new Vector3d(0, 0, 0);
		this.transform = new Transform3D();
		transform.set(new Vector3d(x, y, z));
	}

	public Vector3d getTranslation() {
		return new Vector3d(translation);
	}

	public void setTranslation(Vector3d trans) {
		this.translation.set(trans);
		transform.set(trans);
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
	 * Returns a new PoolBall with the given ball number and the default values
	 * for a ball with this number.
	 *
	 * @param ballNum The number of this ball. 0 if it is the cue ball.
	 */
	public static PoolBall create(int ballNum) {
		PoolBall ball = balls[ballNum];
		return new PoolBall(ball.translation.x, ball.translation.y, ball.translation.z,
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
				retVal[i].translation = new Vector3d(rackLocations.get(counter++));
			}
		}

		retVal[0].translation = new Vector3d(10, 0, 0);
		retVal[8].translation = new Vector3d(0, 0, 0);

		return retVal;
	}

	/** Determines if this pool ball intersects with another pool ball. **/
	public boolean intersects(PoolBall pb) {
		double xDiff = translation.x - pb.translation.x;
		double yDiff = translation.y - pb.translation.y;
		double zDiff = translation.z - pb.translation.z;
		return xDiff*xDiff + yDiff*yDiff + zDiff*zDiff < DIAMETER_SQUARED;
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
				"; " + translation;
	}

	@Override
	public Object clone() {
		return new PoolBall(translation.x, translation.y, translation.z,
				hue, type, ballNum);
	}

}
