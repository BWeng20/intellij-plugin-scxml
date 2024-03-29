package com.bw.graph.util;


import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

/**
 * Geometry helper.
 */
public interface Geometry
{
	/**
	 * Get the closest point on the border of a shape.
	 *
	 * @param rp       The reverence point to get the closest point for.
	 * @param shape    The shape to search on.
	 * @param flatness The "flatness" of iteration, see {@link Shape#getPathIterator(AffineTransform, double)}.
	 * @return The closest point. Never null.
	 */
	static Point2D.Float getClosestPointOnShape(Point2D.Float rp, Shape shape, float flatness)
	{
		//@TODO: find some smarter solution.

		Point2D.Float p0 = new Point2D.Float();
		Point2D.Float p1 = new Point2D.Float();
		Point2D.Float p2 = new Point2D.Float();
		Point2D.Float r = new Point2D.Float();
		Point2D.Float bestp = new Point2D.Float(rp.x, rp.y);

		double d, bestd = Double.MAX_VALUE, dx, dy;

		double[] seg = new double[6];
		for (PathIterator pi = shape.getPathIterator(null, flatness); !pi.isDone(); pi.next())
		{
			final int type = pi.currentSegment(seg);
			switch (type)
			{
				case PathIterator.SEG_MOVETO:
					p0.x = p1.x = p2.x = (float) seg[0];
					p0.y = p1.y = p2.y = (float) seg[1];
					continue;
				case PathIterator.SEG_LINETO:
					p1.x = p2.x;
					p1.y = p2.y;
					p2.x = (float) seg[0];
					p2.y = (float) seg[1];
					break;
				case PathIterator.SEG_CLOSE:
					p1.x = p2.x;
					p1.y = p2.y;
					p2.x = p0.x;
					p2.y = p0.y;
					break;
				default:
					continue;
			}

			getClosestPointOnLineSegment(rp, p1, p2, r);
			dx = rp.x - r.x;
			dy = rp.y - r.y;
			d = dx * dx + dy * dy;
			if (d < bestd)
			{
				bestd = d;
				bestp.x = r.x;
				bestp.y = r.y;
			}
		}
		return bestp;
	}

	/**
	 * Get closest point from p1 to line segment lp1,lp2
	 *
	 * @param p1     reference point to get the closest point for.
	 * @param lp1    First point of line segment.
	 * @param lp2    Second point of line segment.
	 * @param result point to be set to the location of the found closest point.
	 */
	static void getClosestPointOnLineSegment(Point2D.Float p1, Point2D.Float lp1, Point2D.Float lp2, Point2D.Float result)
	{
		final float xd = lp2.x - lp1.x;
		final float yd = lp2.y - lp1.y;

		if (xd == 0 && yd == 0)
		{
			result.setLocation(lp1);
		}
		else
		{
			final float u = ((p1.x - lp1.x) * xd + (p1.y - lp1.y) * yd) / (xd * xd + yd * yd);
			if (u < 0)
			{
				result.setLocation(lp1.x, lp1.y);
			}
			else if (u > 1)
			{
				result.setLocation(lp2.x, lp2.y);
			}
			else
			{
				result.setLocation(lp1.x + u * xd, lp1.y + u * yd);
			}
		}
	}

	/**
	 * Get closest point from p1 to line segment lp1,lp2
	 *
	 * @param p1x    X ordinate of the reference point to get the closest point for.
	 * @param p1y    Y ordinate of the reference point to get the closest point for.
	 * @param lp1x   X ordinate of the first point of line segment.
	 * @param lp1y   Y ordinate of the first point of line segment.
	 * @param lp2x   X ordinate of the second point of line segment.
	 * @param lp2y   Y ordinate of the second point of line segment.
	 * @param result point to be set to the location of the found closest point.
	 */
	static void getClosestPointOnLineSegment(float p1x, float p1y, float lp1x, float lp1y, float lp2x, float lp2y, Point2D.Float result)
	{
		final float xd = lp2x - lp1x;
		final float yd = lp2y - lp1y;

		if (xd == 0 && yd == 0)
		{
			result.setLocation(lp1x, lp1y);
		}
		else
		{
			final float u = ((p1x - lp1x) * xd + (p1y - lp1y) * yd) / (xd * xd + yd * yd);
			if (u < 0)
			{
				result.setLocation(lp1x, lp1y);
			}
			else if (u > 1)
			{
				result.setLocation(lp2x, lp2y);
			}
			else
			{
				result.setLocation(lp1x + u * xd, lp1y + u * yd);
			}
		}
	}

	/**
	 * Get the closest point on a polygon.
	 *
	 * @param rp      The reverence point to get the closest point for.
	 * @param polygon The polygon
	 * @param result  The point to use for result or null.
	 * @return The closest point.
	 */
	static Point2D.Float getClosestPointOnPolygon(Point2D.Float rp, Point2D.Float[] polygon, Point2D.Float result)
	{
		float p1x = 0;
		float p1y = 0;
		float p2x = 0;
		float p2y = 0;
		Point2D.Float r = new Point2D.Float();

		if (result == null)
			result = new Point2D.Float();
		result.x = rp.x;
		result.y = rp.y;

		p2x = polygon[0].x;
		p2y = polygon[0].y;

		double d, bestd = Double.MAX_VALUE, dx, dy;

		for (int i = 1; i < polygon.length; ++i)
		{
			p1x = p2x;
			p1y = p2y;
			p2x = polygon[i].x;
			p2y = polygon[i].y;

			getClosestPointOnLineSegment(rp.x, rp.y, p1x, p1y, polygon[i].x, polygon[i].y, r);
			dx = rp.x - r.x;
			dy = rp.y - r.y;
			d = dx * dx + dy * dy;
			if (d < bestd)
			{
				bestd = d;
				result.x = r.x;
				result.y = r.y;
			}
		}
		return result;
	}


	/**
	 * Get the angle in Radians of the line.
	 *
	 * @param x1 X of first point.
	 * @param y1 Y of first point.
	 * @param x2 X of second point.
	 * @param y2 Y of second point.
	 * @return The angle.
	 */
	static float getAngle(float x1, float y1, float x2, float y2)
	{
		return (float) Math.atan2(y2 - y1, x2 - x1);
	}
}