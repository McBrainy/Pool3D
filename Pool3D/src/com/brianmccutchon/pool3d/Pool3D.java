package com.brianmccutchon.pool3d;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.HashSet;

import javax.swing.*;

import geometry.Environment;
import geometry.Point3D;

public class Pool3D extends JPanel {

	private static final long serialVersionUID = 2316556066963532682L;
	Environment env;
	static HashSet<Character> keysDown = new HashSet<>();
	
	Timer t;
	private long lastTime;

	public Pool3D() {
		env = new Environment();
		//PoolBall ball = new PoolBall(0, 0, 0, null, null, 0, 5);
		//env.addObject(ball);
		Arrays.asList(Physics.balls).forEach(env::addObject);
		Physics.balls[0].velocity.x = -0.4;
		env.ambientLight = 0.5;
		env.tempLightSource = new Point3D(500, -1000, 200);

		t = new Timer(16, (e) -> {
			long time = System.currentTimeMillis();
			Physics.nextFrame(time - lastTime);

			if(keysDown.contains('d'))
				env.rotateRight();
			if(keysDown.contains('a'))
				env.rotateLeft();
			if(keysDown.contains('s'))
				env.moveBackward();
			if(keysDown.contains('w'))
				env.moveForward();
			if(keysDown.contains('j'))
				env.moveDown();
			if(keysDown.contains('l'))
				env.moveUp();
			if(keysDown.contains('['))
				env.nearFarther();
			if(keysDown.contains(']'))
				env.nearCloser();
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
				keysDown.add(ke.getKeyChar());
			}

			@Override
			public void keyReleased(KeyEvent ke) {
				keysDown.remove(ke.getKeyChar());
			}
		});
	}

	@Override
	public void paint(Graphics g) {
		env.render((Graphics2D) g);
	}

}
