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

	/**
	 * Creates a new Rectangle Primitive.
	 *
	 * @param relativePosition The relative position
	 * @param style            The style or null if default style shall be used.
	 * @param scalable         True is user can scale this primitive independent of parent.
	 * @param width            Width in pixel.
	 * @param height           Height in pixel
	 */
	public RectanglePrimitive(Point2D.Float relativePosition,
							  DrawStyle style,
							  boolean scalable, float width, float height)
	{
		super(relativePosition, style, scalable);
		this.width = width;
		this.height = height;
	}


	@Override
	protected void drawIntern(Graphics2D g2, DrawStyle style, Point2D.Float pos)
	{
		g2.setPaint(style.linePaint);
		g2.draw3DRect((int) (pos.x + 0.5), (int) (pos.y + 0.5),
				(int) (width + 0.5), (int) (height + 0.5),
				style.highlighted);
	}

	@Override
	protected Dimension2DFloat getDimension(Graphics2D graphics, DrawStyle style)
	{
		return new Dimension2DFloat(width, height);
	}

}