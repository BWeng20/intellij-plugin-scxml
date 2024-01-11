package com.bw.graph.primitive;

import com.bw.graph.DrawStyle;
import com.bw.graph.GraphConfiguration;
import com.bw.graph.util.Dimension2DFloat;
import com.bw.svg.SVGWriter;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;

/**
 * A Path.
 */
public class Path extends DrawPrimitive
{
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
	}


	@Override
	protected void drawIntern(Graphics2D g2, DrawStyle style)
	{
		g2.setPaint(style.linePaint);
		g2.setStroke(style.lineStroke);
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

	}

}