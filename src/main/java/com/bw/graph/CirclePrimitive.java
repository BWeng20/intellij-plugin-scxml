package com.bw.graph;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;

/**
 * A Circle.
 */
public class CirclePrimitive extends DrawPrimitive
{
	private float centerX;
	private float centerY;
	private float radius;

	private boolean fill;

	/**
	 * Creates a new Circle Primitive.
	 *
	 * @param cx       The relative center x-position
	 * @param cy       The relative center y-position
	 * @param style    The style or null if default style shall be used.
	 * @param scalable True is user can scale this primitive independent of parent.
	 * @param radius   Radius in pixel.
	 */
	public CirclePrimitive(float cx, float cy,
						   DrawStyle style,
						   boolean scalable, float radius)
	{
		super(cx - radius, cy - radius, style, scalable);
		this.centerX = cx;
		this.centerY = cy;
		this.radius = radius;
		this.fill = false;
	}

	/**
	 * Sets filled.
	 *
	 * @param fill If true, the circle is filled
	 */
	public void setFill(boolean fill)
	{
		this.fill = fill;
	}


	@Override
	protected void drawIntern(Graphics2D g2, DrawStyle style, Point2D.Float pos)
	{

		final int radiusInt = (int) (radius + 0.5);
		if (fill)
		{
			g2.setPaint(style.fillPaint);
			g2.fillOval((int) (pos.x + 0.5), (int) (pos.y + 0.5),
					radiusInt, radiusInt);
		}
		g2.setPaint(style.linePaint);
		g2.drawOval((int) (pos.x + 0.5), (int) (pos.y + 0.5),
				radiusInt, radiusInt);
	}

	@Override
	protected Dimension2DFloat getDimension(Graphics2D graphics, DrawStyle style)
	{
		return new Dimension2DFloat(radius, radius);
	}

	@Override
	protected void toSVGIntern(StringBuilder sb, DrawStyle style, Point2D.Float pos)
	{
		sb.append("<circle cx='");
		toSVG(sb, pos.x + radius);
		sb.append("' cy='");
		toSVG(sb, pos.y + radius);
		sb.append("' r='");
		toSVG(sb, radius);
		sb.append("' style='");
		if (fill)
			toSVGStyle(sb, "fill", style.fillPaint);
		toSVGStyle(sb, "stroke", style.linePaint);
		toSVGStokeWidth(sb, style.lineStroke);
		sb.append("'/>");
	}

}