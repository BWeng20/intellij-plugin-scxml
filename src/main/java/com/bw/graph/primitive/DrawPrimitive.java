package com.bw.graph.primitive;

import com.bw.graph.Alignment;
import com.bw.graph.DrawStyle;
import com.bw.graph.GraphConfiguration;
import com.bw.graph.util.Dimension2DFloat;
import com.bw.graph.util.InsetsFloat;
import com.bw.graph.visual.Visual;
import com.bw.svg.SVGWriter;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

/**
 * A draw primitive.<br>
 * Used by Visuals to draw.
 */
public abstract class DrawPrimitive
{
	protected final DrawStyle style;
	private final Point2D.Float relativePosition;
	private final Point2D.Float tempPosition = new Point2D.Float();
	private boolean editable;
	private Visual visual;

	private Object userData;

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
	 * The alignment of this primitive.
	 */
	protected Alignment alignment;

	/**
	 * Creates a new Primitive.
	 *
	 * @param x      The relative x-position
	 * @param y      The relative y-position
	 * @param config The configuration to use.
	 * @param style  The local style.
	 */
	protected DrawPrimitive(float x, float y, GraphConfiguration config, DrawStyle style)
	{
		Objects.requireNonNull(style);
		this.style = style;
		this.relativePosition = new Point2D.Float(x, y);
		this.config = config;
	}

	/**
	 * Sets visual.<br>
	 * The visual is only set during event handling or similar operations.
	 *
	 * @param v The visual or null.
	 */
	public void setVisual(Visual v)
	{
		visual = v;
	}

	/**
	 * Gets the current assigned visual.<br>
	 * The visual is only set during event handling or similar operations.
	 *
	 * @return The visual or null.
	 */
	public Visual getVisual()
	{
		return visual;
	}

	/**
	 * Sets a payload object.
	 *
	 * @param userData The data object or null.
	 */
	public void setUserData(Object userData)
	{
		this.userData = userData;
	}

	/**
	 * Gets the payload object.
	 *
	 * @return The data object or null.
	 */
	public Object getUserData()
	{
		return userData;
	}


	/**
	 * Draws for given context.<br>
	 * This method calls {@link #drawIntern(Graphics2D)}
	 * with adapted position.
	 *
	 * @param g2 The graphics context
	 */
	public void draw(Graphics2D g2)
	{
		tempPosition.x = relativePosition.x + insets.left;
		tempPosition.y = relativePosition.y + insets.top;

		AffineTransform orgTransform = g2.getTransform();
		try
		{
			g2.translate(tempPosition.x, tempPosition.y);
			drawIntern(g2);
		}
		finally
		{
			g2.setTransform(orgTransform);
		}
	}

	/**
	 * Draws at given absolute position.
	 *
	 * @param g2 The graphics context, with (0,0) at final position.
	 */
	protected abstract void drawIntern(Graphics2D g2);


	/**
	 * Force a repaint.
	 */
	public void repaint()
	{
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
	public Rectangle2D.Float getBounds2D(Point2D.Float basePosition, Graphics2D graphics)
	{
		return getBounds2D(basePosition.x, basePosition.y, graphics);
	}

	public Rectangle2D.Float getBounds2D(float basePositionX, float basePositionY, Graphics2D graphics)
	{
		final Dimension2DFloat dim = getDimension(graphics);
		return new Rectangle2D.Float(
				basePositionX + relativePosition.x,
				basePositionY + relativePosition.y, dim.width, dim.height);
	}

	/**
	 * Get the alignment.
	 *
	 * @return The alignment-mode. Never null.
	 */
	public Alignment getAlignment()
	{
		return (alignment == null) ? DEFAULT_ALIGNMENT : alignment;
	}

	/**
	 * Set the alignment.
	 *
	 * @param alignment The alignment-mode. Can be null.
	 */
	public void setAlignment(Alignment alignment)
	{
		this.alignment = alignment;
	}

	/**
	 * Gets the bounds of the primitive, including insets.
	 *
	 * @param graphics The graphics context to use for calculations.
	 * @return The bounds as rectangle.
	 */
	public Dimension2DFloat getDimension(Graphics2D graphics)
	{
		Dimension2DFloat dim = getInnerDimension(graphics);
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
	protected abstract Dimension2DFloat getInnerDimension(Graphics2D graphics);

	/**
	 * Adds the primitive as SVG element to the string builder.
	 *
	 * @param sw          The Writer to write to.
	 * @param position    Base position.
	 * @param parentStyle Style of parent.
	 */
	public void toSVG(SVGWriter sw, Graphics2D g2,
					  Point2D.Float position)
	{
		tempPosition.x = relativePosition.x + insets.left;
		tempPosition.y = relativePosition.y + insets.top;

		if (position != null)
		{
			tempPosition.x += position.x;
			tempPosition.y += position.y;
		}

		toSVGIntern(sw, g2, tempPosition);

	}

	/**
	 * Internal implementation from inheritances.
	 *
	 * @param sw    The Writer to write to.
	 * @param style The resulting style to use.
	 * @param pos   The calculated position (including the relative position).
	 */
	protected abstract void toSVGIntern(SVGWriter sw, Graphics2D g2, Point2D.Float pos);

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

	/**
	 * Checks if this primitive is editable.
	 *
	 * @return True if editable, false if readonly.
	 */
	public boolean isEditable()
	{
		return editable;
	}

	/**
	 * Sets editable property.
	 *
	 * @param editable true if editable.
	 */
	public void setEditable(boolean editable)
	{
		this.editable = editable;
	}

	/**
	 * Release resource.
	 */
	public void dispose()
	{
		visual = null;
	}

	public boolean isModified()
	{
		return false;
	}

	public void setModified(boolean modified)
	{
	}

}