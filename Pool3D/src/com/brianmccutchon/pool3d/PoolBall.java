package com.brianmccutchon.pool3d;

import geometry.Point3D;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.awt.Color.*;
import static com.brianmccutchon.pool3d.BallType.*;

public class PoolBall {

	public static final int RADIUS = 1;

	public static final int DIAMETER = RADIUS*2;

	private static final double DIAMETER_SQUARED = DIAMETER*DIAMETER;

	/** The current location the center of this pool ball. **/
	public final Point3D location;

	/** The velocity vector of this ball. **/
	public final Point3D velocity;

	/** The display color of this pool ball. **/
	public final Color hue;

	/** The type of this ball. **/
	public final BallType type;

	/** The number of this ball. 0 if it is the cue ball. **/
	public final int ballNum;

	private static final Color
			BROWN  = new Color(139,  69,  19),
			PURPLE = new Color(128,   0, 128);

	private static List<Point3D> rackLocations = Arrays.asList(
			new Point3D( 1.633,    -1.0, -0.5774),
			new Point3D( 1.633,     1.0, -0.5774),
			new Point3D( 1.633,     0.0,   3.465),
			new Point3D(  -2.0,     0.0,     0.0),
			new Point3D(   2.0,     0.0,     0.0),
			new Point3D(   1.0,  1.7321,     0.0),
			new Point3D(  -1.0,  1.7321,     0.0),
			new Point3D(   1.0, -1.7321,     0.0),
			new Point3D(  -1.0, -1.7321,     0.0),
			new Point3D(-1.633,    -1.0,  0.5774),
			new Point3D(-1.633,     1.0,  0.5774),
			new Point3D(-1.633,     0.0,  -3.465),
			new Point3D(-3.267,     0.0,     0.0));

	private static final PoolBall[] balls = {
		new PoolBall(   0.0,     0.0,     0.0,  WHITE,    CUE,  0),
		new PoolBall( 3.267,     0.0,     0.0, YELLOW,  SOLID,  1),
		new PoolBall( 1.633,    -1.0, -0.5774,   BLUE,  SOLID,  2),
		new PoolBall( 1.633,     1.0, -0.5774,    RED,  SOLID,  3),
		new PoolBall( 1.633,     0.0,   3.465, PURPLE,  SOLID,  4),
		new PoolBall(  -2.0,     0.0,     0.0, ORANGE,  SOLID,  5),
		new PoolBall(   2.0,     0.0,     0.0,  GREEN,  SOLID,  6),
		new PoolBall(   1.0,  1.7321,     0.0,  BROWN,  SOLID,  7),
		new PoolBall(   0.0,     0.0,     0.0,  BLACK,  EIGHT,  8),
		new PoolBall(  -1.0,  1.7321,     0.0, YELLOW, STRIPE,  9),
		new PoolBall(   1.0, -1.7321,     0.0,   BLUE, STRIPE, 10),
		new PoolBall(  -1.0, -1.7321,     0.0,    RED, STRIPE, 11),
		new PoolBall(-1.633,    -1.0,  0.5774, PURPLE, STRIPE, 12),
		new PoolBall(-1.633,     1.0,  0.5774, ORANGE, STRIPE, 13),
		new PoolBall(-1.633,     0.0,  -3.465,  GREEN, STRIPE, 14),
		new PoolBall(-3.267,     0.0,     0.0,  BROWN, STRIPE, 15),
	};

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
	 */
	public PoolBall(double x, double y, double z,
			Color hue, BallType type, int ballNum) {
		location = new Point3D(x, y, z);
		this.hue = hue;
		this.type = type;
		this.ballNum = ballNum;
		velocity = new Point3D(0, 0, 0);
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
		return (PoolBall) balls[ballNum].clone();
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

		PoolBall[] retVal = new PoolBall[15];

		int counter = 0;
		for (int i = 1; i < 15; i++) {
			retVal[i] = PoolBall.create(i);
			if (counter != 7 && counter != 1) { // The two balls w/ set posns
				retVal[i].setLocation(rackLocations.get(counter++));
			}
		}

		return retVal;
	}

	/** Determines if this pool ball intersects with another pool ball. **/
	public boolean intersects(PoolBall pb) {
		return location.distSq(pb.location) < DIAMETER_SQUARED;
	}

	/**
	 * Sets the ball's location. While the location is publicly visible, it is
	 * also final in order to ensure that less garbage is generated.
	 * @param p The location to set.
	 */
	public void setLocation(Point3D p) {
		location.setLocation(p.x, p.y, p.z);
	}

	/**
	 * Sets the ball's velocity. While the velocity is publicly visible, it is
	 * also final in order to ensure that less garbage is generated.
	 * @param p The velocity to set.
	 */
	public void setVelocity(Point3D p) {
		velocity.setLocation(p.x, p.y, p.z);
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
				"; " + location;
	}

	@Override
	public Object clone() {
		return new PoolBall(location.x, location.y, location.z,
				hue, type, ballNum);
	}

}
