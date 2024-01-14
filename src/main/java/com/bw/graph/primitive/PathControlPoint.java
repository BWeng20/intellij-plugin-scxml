package com.bw.graph.primitive;

import java.awt.geom.Point2D;

/**
 * Provides a control point on a path.
 */
public interface PathControlPoint
{

	/**
	 * Get the absolute position.
	 *
	 * @return The position.
	 */
	void getControlPosition(Point2D.Float pt);

}
