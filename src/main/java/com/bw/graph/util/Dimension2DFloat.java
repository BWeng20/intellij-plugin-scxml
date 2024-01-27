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
	public float _width;

	/**
	 * The height in floating point precision.
	 */
	public float _height;

	/**
	 * Creates a new Dimension object.
	 *
	 * @param width  The width
	 * @param height The height
	 */
	public Dimension2DFloat(float width, float height)
	{
		this._width = width;
		this._height = height;
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
			_width = _height = 0f;
		}
		else
		{
			_width = (float) rect.getWidth();
			_height = (float) rect.getHeight();
		}
	}

	/**
	 * Create a copy.
	 *
	 * @param other The source dimension to copy from.
	 */
	public Dimension2DFloat(Dimension2DFloat other)
	{
		_width = other._width;
		_height = other._height;
	}


	@Override
	public double getWidth()
	{
		return _width;
	}

	@Override
	public double getHeight()
	{
		return _height;
	}

	@Override
	public void setSize(double width, double height)
	{
		this._width = (float) width;
		this._height = (float) height;
	}

	@Override
	public String toString()
	{
		return "Dimension[" + _width + "," + _height + "]";
	}

	/**
	 * Gets a rectangle with same dimensions, which position at (0,0).
	 *
	 * @return The rectangle.
	 */
	public Rectangle2D.Float getBounds()
	{
		return new Rectangle2D.Float(0, 0, _width, _height);
	}
}
