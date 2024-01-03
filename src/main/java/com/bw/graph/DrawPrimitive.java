package com.bw.graph;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * A draw primitive.<br>
 * Used by Visuals to draw.
 */
public abstract class DrawPrimitive
{
	private final boolean scalable;
	private final DrawStyle style;
	private final Point2D.Float relativePosition;

	/**
	 * Creates a new Primitive.
	 *
	 * @param relativePosition The relative position
	 * @param style            The local style or null if parent style shall be used.
	 * @param scalable         True is user can scale this primitive independent of parent.
	 */
	protected DrawPrimitive(Point2D.Float relativePosition, DrawStyle style, boolean scalable)
	{
		this.style = style;
		this.relativePosition = new Point2D.Float(relativePosition.x, relativePosition.y);
		this.scalable = scalable;
	}

	/**
	 * Draws for given context.<br>
	 * This method calls {@link #drawIntern(Graphics2D, DrawStyle, Point2D.Float)}
	 * with adapted position and DrawStyle.
	 *
	 * @param g2          The graphics context
	 * @param position    The base position to draw at.
	 * @param parentStyle The style of parent, used of primitive has no own style.
	 */
	public void draw(Graphics2D g2, Point2D.Float position, DrawStyle parentStyle)
	{
		Point2D.Float pos = new Point2D.Float(
				position.x + relativePosition.x,
				position.y + relativePosition.y);
		drawIntern(g2,
				style == null ? parentStyle : style,
				pos);
	}

	/**
	 * Draws at given absolute position.
	 *
	 * @param g2    The graphics context
	 * @param style The style to use.
	 * @param pos   Absolute position.
	 */
	protected abstract void drawIntern(Graphics2D g2, DrawStyle style, Point2D.Float pos);

	/**
	 * Check if primitive can be scaled independent of parent.
	 *
	 * @return true if primitive is scalable.
	 */
	public boolean isScalable()
	{
		return scalable;
	}

	/**
	 * Get the local style if independent of parent.
	 *
	 * @return The style or null.
	 */
	public DrawStyle getStyle()
	{
		return style;
	}

	/**
	 * Gets the relative position.
	 *
	 * @return The relative position.
	 */
	public Point2D.Float getRelativePosition()
	{
		return relativePosition;
	}

	/**
	 * Gets the bounds of the primitive.
	 *
	 * @param basePosition The absolute base position
	 * @param graphics     The graphics context to use for calculations.
	 * @param parentStyle  The style of parent, used of primitive has no own style.
	 * @return The bounds as rectangle.
	 */
	public Rectangle2D.Float getBounds2D(Point2D.Float basePosition, Graphics2D graphics, DrawStyle parentStyle)
	{
		final Dimension2DFloat dim = getDimension(graphics, style == null ? parentStyle : style);
		return new Rectangle2D.Float(
				basePosition.x + relativePosition.x,
				basePosition.y + relativePosition.y,
				dim.width, dim.height);
	}

	/**
	 * Gets the bounds of the primitive.
	 *
	 * @param graphics The graphics context to use for calculations.
	 * @param style    The style to use.
	 * @return The bounds as rectangle.
	 */
	protected abstract Dimension2DFloat getDimension(Graphics2D graphics, DrawStyle style);
}