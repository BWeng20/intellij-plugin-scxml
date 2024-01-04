package com.bw.graph;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Locale;

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
	 * Creates a new Primitive.
	 *
	 * @param x        The relative x-position
	 * @param y        The relative y-position
	 * @param style    The local style or null if parent style shall be used.
	 * @param scalable True is user can scale this primitive independent of parent.
	 */
	protected DrawPrimitive(float x, float y, DrawStyle style, boolean scalable)
	{
		this.style = style;
		this.relativePosition = new Point2D.Float(x, y);
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
		tempPosition.x = position.x + relativePosition.x;
		tempPosition.y = position.y + relativePosition.y;
		drawIntern(g2,
				style == null ? parentStyle : style,
				tempPosition);
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
	 * Get the orientation.
	 *
	 * @return The mode or null.
	 */
	public Orientation getOrientation()
	{
		if (style != null)
			return style.orientation;
		return null;
	}

	/**
	 * Gets the bounds of the primitive.
	 *
	 * @param graphics The graphics context to use for calculations.
	 * @param style    The style to use.
	 * @return The bounds as rectangle.
	 */
	protected abstract Dimension2DFloat getDimension(Graphics2D graphics, DrawStyle style);

	/**
	 * The primitive as SVG element.
	 *
	 * @return The SVG element. E.g. <code>&lt;circle style='stroke:blue;stroke-with:2' cx='1' cy='1' r=50&gt;</code>
	 */
	public void toSVG(StringBuilder sb,
					  Point2D.Float position, DrawStyle parentStyle)
	{
		tempPosition.x = position.x + relativePosition.x;
		tempPosition.y = position.y + relativePosition.y;
		toSVGIntern(sb,
				style == null ? parentStyle : style,
				tempPosition);

	}

	protected abstract void toSVGIntern(StringBuilder sb, DrawStyle style, Point2D.Float pos);

	private final static Locale svgLocale = Locale.ENGLISH;
	private final float precisionFactor = 10 * 10 * 10;

	protected void toSVGColorValue(StringBuilder sb, Paint paint)
	{
		if (paint instanceof Color)
		{
			sb.append('#').append(((Color) paint).getRGB());
		}
		else
		{
			// @TODO Handle for more complex paints via defs
			sb.append("black");
		}
	}

	protected void toSVGStokeWidth(StringBuilder sb, Stroke stroke)
	{
		if (stroke != null)
		{
			sb.append("stroke-width:");
			if (stroke instanceof BasicStroke)
			{
				toSVG(sb, ((BasicStroke) stroke).getLineWidth());
			}
			else
			{
				// Any other implementation to support?
				toSVG(sb, '1');
			}
		}
	}

	protected void toSVGStyle(StringBuilder sb, String attribute, Paint paint)
	{
		if (paint != null)
		{
			sb.append(attribute);
			sb.append(':');
			toSVGColorValue(sb, paint);
			sb.append(';');
		}

	}


	protected void toSVG(StringBuilder sb, float value)
	{
		value = (float) Math.ceil(0.5f + (value * precisionFactor)) / precisionFactor;
		sb.append(value);
	}
}