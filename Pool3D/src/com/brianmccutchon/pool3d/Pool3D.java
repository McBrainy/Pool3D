package com.brianmccutchon.pool3d;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;

import javax.media.j3d.*;
import javax.swing.*;
import javax.vecmath.*;

import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.utils.universe.*;

import static com.brianmccutchon.pool3d.Physics.*;

/**
 * This class represents the main class and GUI of 3D pool.
 * Currently, the game has two modes, shooting and not shooting, as
 * represented by {@link #shooting}. Each has its own set of event handlers.
 *
 * @author Brian McCutchon
 */
public class Pool3D {

	/** {@code true} iff we are in shooting mode **/
	boolean shooting = false;

	/** Timer for rendering loop. **/
	private Timer t;

	private HashMap<PoolBall, TransformGroup> ballsToSpheres = new HashMap<>();

	/** The corners of the table. **/
	static Point3d[] corners = {
			new Point3d( TABLE_X,  TABLE_Y,  TABLE_Z),
			new Point3d( TABLE_X,  TABLE_Y, -TABLE_Z),
			new Point3d( TABLE_X, -TABLE_Y,  TABLE_Z),
			new Point3d( TABLE_X, -TABLE_Y, -TABLE_Z),
			new Point3d(-TABLE_X,  TABLE_Y,  TABLE_Z),
			new Point3d(-TABLE_X,  TABLE_Y, -TABLE_Z),
			new Point3d(-TABLE_X, -TABLE_Y,  TABLE_Z),
			new Point3d(-TABLE_X, -TABLE_Y, -TABLE_Z),
	};

	// Divide each of the points above by two here so that we don't have to
	// have "/2" 24 times and the code looks cleaner
	static {
		Arrays.asList(corners).forEach(p -> {
			p.x /= 2;
			p.y /= 2;
			p.z /= 2;
		});
	}

	Controller controls;

	/**
	 * The vertices of each of the triangles as indices into {@link #corners}.
	 */
	static int[][] triangles = {
			{ 0, 1, 3 }, { 0, 3, 2 },
			{ 4, 7, 5 }, { 4, 6, 7 },
			{ 0, 5, 1 }, { 0, 4, 5 },
			{ 2, 3, 7 }, { 2, 7, 6 },
			{ 1, 7, 3 }, { 1, 5, 7 },
			{ 0, 2, 6 }, { 0, 6, 4 },
	};

	/** Constructs a new Pool3D JFrame and starts the game. **/
	public Pool3D() {
		SimpleUniverse univ = new SimpleUniverse();
		BranchGroup group = new BranchGroup();

		controls = new Controller(this, univ.getCanvas(),
				univ.getViewingPlatform().getViewPlatformTransform());

		for (PoolBall ball : balls) {
			TransformGroup ballSphere = makeBallSphere(ball);
			ballsToSpheres.put(ball, ballSphere);
			group.addChild(ballSphere);
		}

		// Add a directional light
		DirectionalLight light1 = new DirectionalLight(
				new Color3f(1, 1, 1),
				new Vector3f(-4.0f, -7.0f, -3.0f));
		light1.setInfluencingBounds(
				new BoundingSphere(new Point3d(0, 0, 0), 100));
		group.addChild(light1);

		univ.getViewingPlatform().setNominalViewingTransform();
		univ.addBranchGraph(group);
		univ.getViewer().getView().setBackClipDistance(100);

		//for (int[] tri : triangles) {
		//	env.addTriangle(new Triangle3D(corners[tri[0]],
		//			corners[tri[1]], corners[tri[2]], Color.GREEN));
		//}

		t = new Timer(16, (e) -> {
			Physics.nextFrame();
			updateGraphics();
			controls.processEvents();
		});

		t.start();
	}

	private void updateGraphics() {
		for (PoolBall b : balls) {
			Transform3D trans = new Transform3D();
			trans.set(b.getTranslation());
			ballsToSpheres.get(b).setTransform(trans);
		}
	}

	public static void main(String[] args) {
		new Pool3D();
	}

	static TransformGroup makeBallSphere(PoolBall ball) {
		Color3f white = new Color3f(Color.WHITE);
		Color3f black = new Color3f(Color.BLACK);

		Appearance appear = new Appearance();

		// Make a material so that shading can work
		Material mat = new Material(white, black, white, black, 1);
		appear.setMaterial(mat);

		// Apply texture
		BufferedImage img = makeTextureImage(ball);
		Texture tex = new TextureLoader(img).getTexture();
		tex.setBoundaryModeS(Texture.WRAP);
		tex.setBoundaryModeT(Texture.WRAP);
		appear.setTexture(tex);

		// Set the mode for the texture so that it can be shaded properly
		TextureAttributes texAttr = new TextureAttributes();
		texAttr.setTextureMode(TextureAttributes.MODULATE);
		appear.setTextureAttributes(texAttr);

		// Create a ball and add it to the group of objects
		Sphere sphere = new Sphere(1, Primitive.GENERATE_NORMALS |
				Primitive.GENERATE_TEXTURE_COORDS, 200, appear);
		TransformGroup group = new TransformGroup();
		group.addChild(sphere);
		group.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

		// Set the proper translation
		Transform3D trans = new Transform3D();
		trans.set(ball.getTranslation());
		group.setTransform(trans);

		return group;
	}

	static BufferedImage makeTextureImage(PoolBall ball) {
		int height = 1 << 9;
		int width  = 2 * height;

		BufferedImage img = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.fillRect(0, 0, width, height);

		if (ball.type != BallType.CUE) {
			g.setColor(ball.hue.get());
			int margin = (ball.type == BallType.STRIPE) ?
					(int) (height / 3.5) : 0;
			g.fillRect(0, margin, width, height - 2*margin);

			drawNumber(g, ball.ballNum, width/4,   height/2, height/7);
			drawNumber(g, ball.ballNum, width/4*3, height/2, height/7);
		}

		return img;
	}

	private static void drawNumber(Graphics2D g,
			int number, int x, int y, int radius) {
		g.setColor(Color.WHITE);
		g.fillOval(x-radius, y-radius, radius*2, radius*2);

		g.setColor(Color.BLACK);
		g.setFont(g.getFont().deriveFont(100f));
		drawStringCentered(g, Integer.toString(number), x, y);
	}

	private static void drawStringCentered(Graphics2D g,
			String string, int x, int y) {
		FontMetrics fm = g.getFontMetrics();
		g.drawString(string, x - fm.stringWidth(string)/2, y -
				(fm.getAscent() + fm.getDescent())/2 + fm.getAscent());
	}

}
