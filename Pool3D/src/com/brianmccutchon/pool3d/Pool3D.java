package com.brianmccutchon.pool3d;

import java.awt.*;
import java.awt.event.KeyAdapter;
import static java.awt.event.KeyEvent.*;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.*;

import geometry.Environment;
import geometry.Point3D;

public class Pool3D extends JPanel {

	private static final long serialVersionUID = 2316556066963532682L;
	Environment env;
	static HashSet<Integer> keysDown = new HashSet<>();

	HashMap<Integer, Runnable> handlers = new HashMap<>();

	Timer t;
	private long lastTime;

	public Pool3D() {
		env = new Environment();
		Arrays.asList(Physics.balls).forEach(env::addObject);
		Physics.balls[0].velocity.x = -0.4;
		env.ambientLight = 0.5;
		env.tempLightSource = new Point3D(500, -1000, 200);
		
		handlers.put(VK_RIGHT, env::rotateRight);
		handlers.put(VK_LEFT,  env::rotateLeft);
		handlers.put(VK_DOWN,  env::moveBackward);
		handlers.put(VK_UP,    env::moveForward);
		handlers.put(VK_S,     env::moveDown);
		handlers.put(VK_W,     env::moveUp);
		handlers.put(VK_D,     env::moveRight);
		handlers.put(VK_A,     env::moveLeft);
		handlers.put(VK_OPEN_BRACKET,  env::nearFarther);
		handlers.put(VK_CLOSE_BRACKET, env::nearCloser);

		t = new Timer(16, (e) -> {
			long time = System.currentTimeMillis();
			Physics.nextFrame(time - lastTime);

			for (int i : keysDown) {
				if (handlers.containsKey(i)) {
					handlers.get(i).run();
				}
			}

			/* This works too
			keysDown.stream()
				.filter(handlers::containsKey)
				.map(handlers::get)
				.forEach(Runnable::run);
			*/

			repaint();
		});

		lastTime = System.currentTimeMillis();

		t.start();
	}

	public static void main(String[] args) {
		Pool3D pool = new Pool3D();
		for (int i = 0; i < 20; i++) {
			pool.env.moveBackward();
		}

		JFrame frame = new JFrame();
		frame.setSize(new Dimension(700, 700));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(pool);
		frame.setVisible(true);

		frame.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent ke) {
				keysDown.add(ke.getKeyCode());
			}

			@Override
			public void keyReleased(KeyEvent ke) {
				keysDown.remove(ke.getKeyCode());
			}
		});
	}

	@Override
	public void paint(Graphics g) {
		env.render((Graphics2D) g);
	}

}
