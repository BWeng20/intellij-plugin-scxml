package com.bw.graph;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;

/**
 * A Rectangle.
 */
public class RectanglePrimitive extends DrawPrimitive
{
	private float width;
	private float height;

	private boolean fill;

	/**
	 * Creates a new Rectangle Primitive.
	 *
	 * @param x        The relative x-position
	 * @param y        The relative y-position
	 * @param style    The style or null if default style shall be used.
	 * @param scalable True is user can scale this primitive independent of parent.
	 * @param width    Width in pixel.
	 * @param height   Height in pixel
	 */
	public RectanglePrimitive(float x, float y,
							  DrawStyle style,
							  boolean scalable, float width, float height)
	{
		super(x, y, style, scalable);
		this.width = width;
		this.height = height;
		this.fill = false;
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
	protected void drawIntern(Graphics2D g2, DrawStyle style, Point2D.Float pos)
	{
		final int x = (int) (pos.x + 0.5);
		final int y = (int) (pos.y + 0.5);
		final int w = (int) (width + 0.5);
		final int h = (int) (height + 0.5);

		if (fill && style.fillPaint != null)
		{
			g2.setPaint(style.fillPaint);
			g2.fillRect(x, y, w, h);
		}
		g2.setPaint(style.linePaint);
		g2.draw3DRect(x, y, w, h, style.highlighted);
	}

	@Override
	protected Dimension2DFloat getDimension(Graphics2D graphics, DrawStyle style)
	{
		return new Dimension2DFloat(width, height);
	}

	@Override
	protected void toSVGIntern(StringBuilder sb, DrawStyle style, Point2D.Float pos)
	{
		sb.append("<rect x='");
		toSVG(sb, pos.x);
		sb.append("' y='");
		toSVG(sb, pos.y);
		sb.append("' width='");
		toSVG(sb, width);
		sb.append("' height='");
		toSVG(sb, height);
		sb.append("' style='");
		if (fill)
			toSVGStyle(sb, "fill", style.fillPaint);
		toSVGStyle(sb, "stroke", style.linePaint);
		toSVGStokeWidth(sb, style.lineStroke);
		sb.append("'/>");
	}

}