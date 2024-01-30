package com.bw.graph.primitive;

import com.bw.graph.DrawStyle;
import com.bw.graph.GraphConfiguration;
import com.bw.graph.util.Dimension2DFloat;
import com.bw.graph.visual.VisualFlags;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

/**
 * A draw primitive base class for all primitives that use shapes to draw.<br>
 */
public abstract class ShapePrimitiveBase extends DrawPrimitive
{
	/**
	 * The shape to draw.
	 */
	protected Shape _shape;


	/**
	 * If true the shape is filled.
	 */
	protected boolean _fill;

	/**
	 * Creates a new Shape Primitive.
	 *
	 * @param x      The relative x-position
	 * @param y      The relative y-position
	 * @param config The configuration to use.
	 * @param style  The local style or null if parent style shall be used.
	 * @param flags  The initial flags. @see {@link VisualFlags}
	 */
	protected ShapePrimitiveBase(float x, float y, GraphConfiguration config, DrawStyle style, int flags)
	{
		super(x, y, config, style, flags);
		this._fill = false;
	}

	@Override
	public Rectangle2D.Float getBounds2D(float basePositionX, float basePositionY, Graphics2D graphics)
	{
		final Rectangle2D bounds = _shape.getBounds2D();
		final var relPos = getRelativePosition();
		return new Rectangle2D.Float(
				basePositionX + relPos.x + (float) bounds.getX(),
				basePositionY + relPos.y + (float) bounds.getY(),
				(float) bounds.getWidth(), (float) bounds.getHeight());
	}

	/**
	 * Draws the shape.
	 *
	 * @param g2 The graphics context, translates to target position.
	 */
	@Override
	protected void drawIntern(Graphics2D g2)
	{
		if (_shape != null)
		{
			if (_fill && _style._fillPaint != null)
			{
				g2.setPaint(_style._fillPaint);
				g2.fill(_shape);
			}
			g2.setPaint(_style._linePaint);
			g2.setStroke(_style._lineStroke);
			g2.draw(_shape);
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
		if (_shape != null)
		{
			Rectangle2D bounds = _shape.getBounds2D();
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
		this._fill = fill;
	}

	/**
	 * Check if shape is filled.
	 *
	 * @return If true, the shape is filled
	 */
	public boolean isFill()
	{
		return _fill;
	}

	/**
	 * Gets the shape.
	 *
	 * @return The shape.
	 */
	public Shape getShape()
	{
		return _shape;
	}

}