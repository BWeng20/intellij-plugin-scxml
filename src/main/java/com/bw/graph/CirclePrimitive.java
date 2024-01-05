package com.bw.graph;

import com.bw.svg.SVGWriter;

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

		final int radiusInt = (int) (2f * radius + 0.5f);
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
	protected void toSVGIntern(SVGWriter sw, DrawStyle style, Point2D.Float pos)
	{
		sw.startElement("circle");
		sw.writeAttribute("cx", pos.x + radius);
		sw.writeAttribute("cy", pos.y + radius);
		sw.writeAttribute("r", radius);
		sw.startStyle();
		if (fill)
			sw.writeAttribute("fill", style.fillPaint);
		sw.writeAttribute("stroke", style.linePaint);
		sw.writeStrokeWith(style.lineStroke);
		sw.endElement();
	}

}