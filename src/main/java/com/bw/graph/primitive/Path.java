package com.bw.graph.primitive;

import com.bw.graph.DrawStyle;
import com.bw.graph.GraphConfiguration;
import com.bw.graph.util.Dimension2DFloat;
import com.bw.graph.util.Geometry;
import com.bw.jtools.svg.ShapeHelper;
import com.bw.svg.SVGWriter;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * A Path.
 */
public class Path extends DrawPrimitive
{
	/**
	 * Control points.
	 */
	protected List<PathControlPoint> controlPoints = new ArrayList<>();

	/**
	 * The edge path, created during draw.
	 */
	protected Path2D path2D;

	/**
	 * Arrow path.
	 */
	protected Path2D arrow = new Path2D.Float();

	/**
	 * Creates a new Path Primitive.<br>
	 * Control Points are dynamically created and bound.
	 *
	 * @param config The configuration to use.
	 * @param style  The style or null if default style shall be used.
	 */
	public Path(GraphConfiguration config,
				DrawStyle style)
	{
		super(0, 0, config, style);

		arrow.moveTo(-2f * config.connectorSize, -config.connectorSize);
		arrow.lineTo(0, 0);
		arrow.lineTo(-2f * config.connectorSize, config.connectorSize);
		arrow.closePath();

	}

	/**
	 * Draws for given context.<br>
	 * Paths don't use relative positions or insets. This override draw along the control points.
	 *
	 * @param g2          The graphics context
	 * @param position    not used.
	 * @param parentStyle The style of parent, used if primitive has no own style.
	 */
	@Override
	public void draw(Graphics2D g2, Point2D.Float position, DrawStyle parentStyle)
	{
		DrawStyle style = getStyle();
		final DrawStyle actualStyle = style == null ? parentStyle : style;

		path2D = new Path2D.Float();

		Point2D.Float pt;
		var it = controlPoints.iterator();
		if (it.hasNext())
		{
			pt = it.next().getControlPosition();
			path2D.moveTo(pt.x, pt.y);
		}
		else
		{
			pt = null;
		}
		while (it.hasNext())
		{
			pt = it.next().getControlPosition();
			path2D.lineTo(pt.x, pt.y);
		}

		if (pt != null)
		{
			g2.setStroke(actualStyle.lineStroke);
			g2.setPaint(actualStyle.linePaint);
			g2.draw(path2D);

			ShapeHelper h = new ShapeHelper(path2D);

			var s = h.pointAtLength(h.getOutlineLength());

			AffineTransform aft = new AffineTransform();
			aft.translate(s.x_, s.y_);
			aft.rotate(s.angle_);
			Shape p = aft.createTransformedShape(arrow);
			g2.fill(p);
		}
	}


	@Override
	protected void drawIntern(Graphics2D g2, DrawStyle style)
	{
		// unused
	}

	@Override
	protected Dimension2DFloat getInnerDimension(Graphics2D graphics, DrawStyle style)
	{
		return new Dimension2DFloat(0, 0);
	}

	@Override
	protected void toSVGIntern(SVGWriter sw, DrawStyle style, Point2D.Float pos)
	{
		// @TODO
	}

	/**
	 * Adds a point to the path.
	 *
	 * @param pt The new point
	 */
	public void addPoint(PathControlPoint pt)
	{
		controlPoints.add(pt);
		path2D = null;
	}

	/**
	 * Get the shortest distance from path to the point.
	 *
	 * @param pt The reference point.
	 * @return The calculated distance.
	 */
	public float getDistanceTo(Point2D.Float pt)
	{
		if (path2D != null)
		{
			Point2D.Float p = Geometry.getClosestPointOnShape(pt, path2D, 5);
			return (float) p.distance(pt);
		}
		return Float.MAX_VALUE;
	}
}