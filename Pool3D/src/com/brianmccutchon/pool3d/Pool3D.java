package com.brianmccutchon.pool3d;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashSet;

import javax.swing.*;

import geometry.Environment;

public class Pool3D extends JPanel {

	private static final long serialVersionUID = 2316556066963532682L;
	Environment env;
	static HashSet<Character> keysDown = new HashSet<>();
	
	Timer t;
	private int frames = 0;
	private long startTime;

	public Pool3D() {
		env = new Environment();
		BallWireFrame bwf = new BallWireFrame(8);
		env.addObject(bwf);

		t = new Timer(20, (e) -> {
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

			frames++;
			System.out.println(frames * 1000.0 / (System.currentTimeMillis() - startTime));
		});

		startTime = System.currentTimeMillis();

		t.start();
	}

	public static void main(String[] args) {
		Pool3D pool = new Pool3D();
		for (int i = 0; i < 2; i++) {
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
		Graphics2D g2 = (Graphics2D) g;
		//g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		//		RenderingHints.VALUE_ANTIALIAS_ON);
		env.render(g2);
	}

}
