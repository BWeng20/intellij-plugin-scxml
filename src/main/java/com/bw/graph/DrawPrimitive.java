package com.bw.graph;

import com.bw.svg.SVGWriter;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
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
	private final Point2D.Float tempPosition = new Point2D.Float();

	/**
	 * The insets of the component.
	 */
	private InsetsFloat insets = DEFAULT_INSERTS;

	/**
	 * Shared default insets instance.
	 */
	public static final InsetsFloat DEFAULT_INSERTS = new InsetsFloat(0, 0, 0, 0);


	/**
	 * Graph configuration to use.
	 */
	protected GraphConfiguration config;

	/**
	 * The default alignment.
	 */
	// @TODO: Shall be configured from system defaults.
	public static Alignment DEFAULT_ALIGNMENT = Alignment.Left;

	/**
	 * Creates a new Primitive.
	 *
	 * @param x        The relative x-position
	 * @param y        The relative y-position
	 * @param config   The configuration to use.
	 * @param style    The local style or null if parent style shall be used.
	 * @param scalable True is user can scale this primitive independent of parent.
	 */
	protected DrawPrimitive(float x, float y, GraphConfiguration config, DrawStyle style, boolean scalable)
	{
		this.style = style;
		this.relativePosition = new Point2D.Float(x, y);
		this.scalable = scalable;
		this.config = config;
	}

	/**
	 * Draws for given context.<br>
	 * This method calls {@link #drawIntern(Graphics2D, DrawStyle)}
	 * with adapted position and DrawStyle.
	 *
	 * @param g2          The graphics context
	 * @param position    The base position to draw at.
	 * @param parentStyle The style of parent, used of primitive has no own style.
	 */
	public void draw(Graphics2D g2, Point2D.Float position, DrawStyle parentStyle)
	{
		final DrawStyle actualStyle = style == null ? parentStyle : style;

		tempPosition.x = position.x + relativePosition.x + insets.left;
		tempPosition.y = position.y + relativePosition.y + insets.top;

		AffineTransform orgTransform = g2.getTransform();
		try
		{
			g2.translate(tempPosition.x, tempPosition.y);
			drawIntern(g2, actualStyle);
		}
		finally
		{
			g2.setTransform(orgTransform);
		}
	}

	/**
	 * Draws at given absolute position.
	 *
	 * @param g2    The graphics context, with (0,0) at final position.
	 * @param style The style to use.
	 */
	protected abstract void drawIntern(Graphics2D g2, DrawStyle style);

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
	 * Gets the bounds of the primitive, including space for insets.
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
				basePosition.y + relativePosition.y, dim.width, dim.height);
	}

	/**
	 * Get the alignment.
	 *
	 * @return The alignment-mode. Never null.
	 */
	public Alignment getAlignment()
	{
		return (style == null || style.alignment == null) ? DEFAULT_ALIGNMENT : style.alignment;
	}

	/**
	 * Gets the bounds of the primitive, including insets.
	 *
	 * @param graphics The graphics context to use for calculations.
	 * @param style    The style to use.
	 * @return The bounds as rectangle.
	 */
	protected Dimension2DFloat getDimension(Graphics2D graphics, DrawStyle style)
	{
		Dimension2DFloat dim = getInnerDimension(graphics, style);
		dim.width += insets.left + insets.right;
		dim.height += insets.top + insets.bottom;

		return dim;
	}


	/**
	 * Gets the bounds of the primitive. Without insets.
	 *
	 * @param graphics The graphics context to use for calculations.
	 * @param style    The style to use.
	 * @return The dimension.
	 */
	protected abstract Dimension2DFloat getInnerDimension(Graphics2D graphics, DrawStyle style);

	/**
	 * Adds the primitive as SVG element to the string builder.
	 *
	 * @param sw          The Writer to write to.
	 * @param position    Base position.
	 * @param parentStyle Style of parent.
	 */
	public void toSVG(SVGWriter sw,
					  Point2D.Float position, DrawStyle parentStyle)
	{
		tempPosition.x = position.x + relativePosition.x + insets.left;
		tempPosition.y = position.y + relativePosition.y + insets.top;
		toSVGIntern(sw,
				style == null ? parentStyle : style,
				tempPosition);

	}

	/**
	 * Internal implementation from inheritances.
	 *
	 * @param sw    The Writer to write to.
	 * @param style The resulting style to use.
	 * @param pos   The calculated position (including the relative position).
	 */
	protected abstract void toSVGIntern(SVGWriter sw, DrawStyle style, Point2D.Float pos);

	/**
	 * Sets new insets.
	 *
	 * @param top    from the top.
	 * @param left   from the left.
	 * @param bottom from the bottom.
	 * @param right  from the right.
	 */
	public void setInsets(float top, float left, float bottom, float right)
	{
		if (insets == DEFAULT_INSERTS)
			insets = new InsetsFloat(top, left, bottom, right);
		else
		{
			insets.top = top;
			insets.left = left;
			insets.bottom = bottom;
			insets.right = right;
		}
	}

	/**
	 * Sets insets.
	 *
	 * @param insets The new insets or null to reset to 0,0,0,0.
	 */
	public void setInsets(InsetsFloat insets)
	{
		if (insets != null)
		{
			if (this.insets == DEFAULT_INSERTS)
				this.insets = new InsetsFloat(insets.top, insets.left, insets.bottom, insets.right);
			else
			{
				this.insets.top = insets.top;
				this.insets.left = insets.left;
				this.insets.bottom = insets.bottom;
				this.insets.right = insets.right;
			}
		}
		else
		{
			this.insets = DEFAULT_INSERTS;
		}
	}

	/**
	 * Gets insets.
	 *
	 * @return The insets, never null.
	 */
	public InsetsFloat getInsets()
	{
		return insets;
	}

}