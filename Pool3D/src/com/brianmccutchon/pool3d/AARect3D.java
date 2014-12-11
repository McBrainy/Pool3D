package com.brianmccutchon.pool3d;

import java.awt.Color;

import geometry.EnvironmentObject;
import geometry.Point3D;
import geometry.Triangle3D;

/**
 * Defines an axis-aligned rectangle in 3-space.
 * This will have its four sides aligned with two of the three axes.
 * @author Brian McCutchon
 */
public class AARect3D extends EnvironmentObject {

	public AARect3D(Point3D center, Point3D dimensions) {
		this.center = center;
		// top-left, top-right, bottom-left, bottom-right
		Point3D tl, tr, bl, br;

		tl = Physics.ORIGIN.subtract(dimensions.divide(2));
		br = dimensions.divide(2);

		if (dimensions.x == 0) {
			tr = new Point3D(0, br.y, tl.z);
			bl = new Point3D(0, tl.y, br.z);
		} else if (dimensions.y == 0) {
			tr = new Point3D(tl.x, 0, br.z);
			bl = new Point3D(br.x, 0, tl.z);
		} else if (dimensions.z == 0) {
			tr = new Point3D(tl.x, br.y, 0);
			bl = new Point3D(br.x, tl.y, 0);
		} else {
			throw new IllegalArgumentException(
					"dimensions must have one 0 component");
		}

		triangles.add(new Triangle3D(tl, br, tr));
		triangles.add(new Triangle3D(tl, bl, br));
	}

	public void setColor(Color c) {
		for (Triangle3D t : triangles) {
			t.triColor = c;
		}
	}

}
