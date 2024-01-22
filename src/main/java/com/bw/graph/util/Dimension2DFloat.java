package com.bw.graph.util;

import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;

/**
 * The class implements Dimension2D via float values.
 */
public final class Dimension2DFloat extends Dimension2D
{
	/**
	 * The width in floating point precision.
	 */
	public float width;

	/**
	 * The height in floating point precision.
	 */
	public float height;

	/**
	 * Creates a new Dimension object.
	 *
	 * @param width  The width
	 * @param height The height
	 */
	public Dimension2DFloat(float width, float height)
	{
		this.width = width;
		this.height = height;
	}

	/**
	 * Creates a dimension from Rectangle2D width and height.
	 *
	 * @param rect The Rectangle.
	 */
	public Dimension2DFloat(Rectangle2D rect)
	{
		if (rect == null)
		{
			width = height = 0f;
		}
		else
		{
			width = (float) rect.getWidth();
			height = (float) rect.getHeight();
		}
	}

	public Dimension2DFloat(Dimension2DFloat other)
	{
		width = other.width;
		height = other.height;
	}


	@Override
	public double getWidth()
	{
		return width;
	}

	@Override
	public double getHeight()
	{
		return height;
	}

	@Override
	public void setSize(double width, double height)
	{
		this.width = (float) width;
		this.height = (float) height;
	}

	@Override
	public String toString()
	{
		return "Dimension[" + width + "," + height + "]";
	}

	public Rectangle2D.Float getBounds()
	{
		return new Rectangle2D.Float(0, 0, width, height);
	}
}
