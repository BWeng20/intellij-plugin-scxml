package com.bw.graph.primitive;

import com.bw.graph.DrawStyle;
import com.bw.graph.GraphConfiguration;
import com.bw.graph.util.Dimension2DFloat;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

/**
 * A draw primitive base class for all primitives that use shapes to draw.<br>
 */
public abstract class ShapeBase extends DrawPrimitive
{
	/**
	 * The shape to draw.
	 */
	protected Shape shape;


	/**
	 * If true the shape is filled.
	 */
	protected boolean fill;

	/**
	 * Creates a new Shape Primitive.
	 *
	 * @param x      The relative x-position
	 * @param y      The relative y-position
	 * @param config The configuration to use.
	 * @param style  The local style or null if parent style shall be used.
	 */
	protected ShapeBase(float x, float y, GraphConfiguration config, DrawStyle style)
	{
		super(x, y, config, style);
		this.fill = false;
	}

	/**
	 * Draws the shape.
	 *
	 * @param g2 The graphics context, translates to target position.
	 */
	@Override
	protected void drawIntern(Graphics2D g2)
	{
		if (shape != null)
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
	}

	/**
	 * Gets the bounds of the shape. Without insets.
	 *
	 * @param graphics The graphics context to use for calculations.
	 * @return The dimension.
	 */
	@Override
	protected Dimension2DFloat getInnerDimension(Graphics2D graphics)
	{
		if (shape != null)
		{
			Rectangle2D bounds = shape.getBounds2D();
			return new Dimension2DFloat((float) bounds.getWidth(), (float) bounds.getHeight());
		}
		else
			return new Dimension2DFloat(0, 0);
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

	/**
	 * Check if shape is filled.
	 *
	 * @return If true, the shape is filled
	 */
	public boolean isFill()
	{
		return fill;
	}

}