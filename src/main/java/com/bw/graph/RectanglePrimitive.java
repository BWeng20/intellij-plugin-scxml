package com.bw.graph;

import com.bw.svg.SVGWriter;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * A Rectangle.
 */
public class RectanglePrimitive extends DrawPrimitive
{
	private Rectangle2D.Float shape;

	private boolean fill;

	/**
	 * Creates a new Rectangle Primitive.
	 *
	 * @param x        The relative x-position
	 * @param y        The relative y-position
	 * @param config   The configuration to use.
	 * @param style    The style or null if default style shall be used.
	 * @param scalable True is user can scale this primitive independent of parent.
	 * @param width    Width in pixel.
	 * @param height   Height in pixel
	 */
	public RectanglePrimitive(float x, float y,
							  GraphConfiguration config,
							  DrawStyle style,
							  boolean scalable, float width, float height)
	{
		super(x, y, config, style, scalable);
		this.fill = false;
		shape = new Rectangle2D.Float(0, 0, width, height);
	}

	/**
	 * Sets filled.
	 *
	 * @param fill If true, the rectangle is filled
	 */
	public void setFill(boolean fill)
	{
		this.fill = fill;
	}


	@Override
	protected void drawIntern(Graphics2D g2, DrawStyle style)
	{
		if (fill && style.fillPaint != null)
		{
			g2.setPaint(style.fillPaint);
			g2.fill(shape);
		}
		g2.setPaint(style.linePaint);
		g2.setStroke(style.lineStroke);
		g2.draw(shape);
	}

	@Override
	protected Dimension2DFloat getInnerDimension(Graphics2D graphics, DrawStyle style)
	{
		return new Dimension2DFloat(shape.width, shape.height);
	}

	@Override
	protected void toSVGIntern(SVGWriter sw, DrawStyle style, Point2D.Float pos)
	{
		sw.startElement("rect");
		sw.writeAttribute("x", pos.x);
		sw.writeAttribute("y", pos.y);
		sw.writeAttribute("width", shape.width);
		sw.writeAttribute("height", shape.height);
		sw.startStyle();
		if (fill)
			sw.writeAttribute("fill", style.fillPaint);
		sw.writeAttribute("stroke", style.linePaint);
		sw.writeStrokeWith(style.getStrokeWidth());
		sw.endElement();
	}

}