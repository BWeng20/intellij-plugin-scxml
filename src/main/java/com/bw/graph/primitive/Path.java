package com.bw.graph.primitive;

import com.bw.graph.DrawStyle;
import com.bw.graph.GraphConfiguration;
import com.bw.graph.util.Dimension2DFloat;
import com.bw.graph.util.Geometry;
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
	 * The buffered translated arrow shape.
	 */
	protected Shape arrowTranslated;

	/**
	 * Buffered control point coordinates.
	 */
	protected Point2D.Float[] coordinates = new Point2D.Float[0];

	/**
	 * Arrow path.
	 */
	protected static Path2D arrow = new Path2D.Float();

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

		Point2D.Float pt = new Point2D.Float();
		boolean recreatePath = path2D == null;

		final int LI = coordinates.length - 1;

		for (int i = 0; i <= LI; ++i)
		{
			if (recreatePath)
			{
				controlPoints.get(i)
							 .getControlPosition(coordinates[i]);
			}
			else
			{
				controlPoints.get(i)
							 .getControlPosition(pt);
				if (pt.x != coordinates[i].x || pt.y != coordinates[i].y)
				{
					recreatePath = true;
					coordinates[i].x = pt.x;
					coordinates[i].y = pt.y;
				}
			}
		}

		float theta = 0;


		if (recreatePath)
		{
			path2D = new Path2D.Float();
			arrowTranslated = null;

			if (LI >= 0)
			{
				path2D.moveTo(coordinates[0].x, coordinates[0].y);
				if (LI > 0)
				{
					for (int i = 1; i <= LI; ++i)
					{
						path2D.lineTo(coordinates[i].x, coordinates[i].y);
					}
					theta = Geometry.getAngle(coordinates[LI - 1].x, coordinates[LI - 1].y, coordinates[LI].x, coordinates[LI].y);

					AffineTransform aft = new AffineTransform();
					aft.translate(coordinates[LI].x, coordinates[LI].y);
					aft.rotate(theta);
					arrowTranslated = aft.createTransformedShape(arrow);
				}
			}
		}

		g2.setStroke(actualStyle.lineStroke);
		g2.setPaint(actualStyle.linePaint);
		g2.draw(path2D);
		if (arrowTranslated != null)
			g2.fill(arrowTranslated);
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

		coordinates = new Point2D.Float[controlPoints.size()];
		for (int i = 0; i < coordinates.length; ++i)
			coordinates[i] = new Point2D.Float(0, 0);
	}

	/**
	 * Get the shortest distance from path to the point.
	 *
	 * @param pt The reference point.
	 * @return The calculated distance.
	 */
	public float getDistanceTo(Point2D.Float pt)
	{
		if (pt != null && coordinates.length > 1)
		{
			return (float) pt.distance(Geometry.getClosestPointOnPolygon(pt, coordinates, null));
		}
		return Float.MAX_VALUE;
	}
}