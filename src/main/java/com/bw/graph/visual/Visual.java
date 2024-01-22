package com.bw.graph.visual;

import com.bw.graph.DrawContext;
import com.bw.graph.DrawStyle;
import com.bw.graph.GraphConfiguration;
import com.bw.graph.primitive.DrawPrimitive;
import com.bw.graph.util.Dimension2DFloat;
import com.bw.svg.SVGWriter;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

/**
 * A visual representation in the graph.
 */
public abstract class Visual
{
	/**
	 * Identification object from caller. Can be null.
	 */
	protected Object id;

	/**
	 * True of the visual is high-lighted.
	 */
	protected boolean highlighted;

	/**
	 * The preferred dimension of the visual.
	 */
	protected Dimension2DFloat dimension;

	/**
	 * The absilute bounds of the visual.<br>
	 * The width and height are lazy calculated and updated if set to a negative value.
	 */
	protected Rectangle2D.Float absoluteBounds = new Rectangle2D.Float(0, 0, -1, -1);

	/**
	 * The drawing context to use for painting and size calculations.
	 */
	protected DrawContext context;

	/**
	 * Terue if a repaint was triggered.
	 */
	protected boolean repaintTriggered = false;

	/**
	 * "Dirty" marker, true if model was externally modified.
	 */
	protected boolean dirty = false;

	/**
	 * Create a new empty visual.
	 *
	 * @param id      The identification. Can be null.
	 * @param context The Drawing context to use.
	 */
	protected Visual(Object id, DrawContext context)
	{
		Objects.requireNonNull(context);
		this.id = id;
		this.context = context;
	}

	/**
	 * Draw the visual.
	 *
	 * @param g2 The Graphics context
	 */
	public void draw(Graphics2D g2)
	{
		g2.translate(absoluteBounds.x, absoluteBounds.y);
		try
		{
			drawRelative(g2);
		}
		finally
		{
			g2.translate(-absoluteBounds.x, -absoluteBounds.y);
		}
	}


	/**
	 * Draw the visual.<br>
	 * If a sub-model is set, the area described by {@link GraphConfiguration#innerModelBoxInsets} and {@link GraphConfiguration#innerModelBoxMinDimension} shall be spared,
	 * as this area will be over-drawn by {@link #draw(Graphics2D)}.
	 *
	 * @param g2 The Graphics context
	 */
	protected abstract void drawRelative(Graphics2D g2);

	/**
	 * Checks if a point is inside the area of the visual.
	 *
	 * @param x X position.
	 * @param y Y position.
	 * @return true if (x,y) is inside the visual.
	 */
	public boolean containsPoint(float x, float y)
	{
		return absoluteBounds.contains(x, y);
	}

	/**
	 * Updates {@link #absoluteBounds}.
	 *
	 * @param graphics The graphics context to use for calculations.
	 */
	protected abstract void updateBounds(Graphics2D graphics);

	/**
	 * Moves the visual by some delta.
	 *
	 * @param x The X-Delta to move.
	 * @param y The Y-Delta to move.
	 */
	public void moveBy(float x, float y)
	{
		if (x != 0 || y != 0)
		{
			dirty = true;
			absoluteBounds.x += x;
			absoluteBounds.y += y;
		}
	}

	/**
	 * Triggers the re-calculation of bounds on next paint.
	 */
	public void resetBounds()
	{
		absoluteBounds.width = -1;
	}

	/**
	 * Gets the bounds of the visual.
	 *
	 * @param g2 The Graphic context to use for calculations. Will not be modified.
	 * @return The bounds in absolute coordinates, never null.
	 */
	public Rectangle2D.Float getAbsoluteBounds2D(Graphics2D g2)
	{
		if (absoluteBounds.width < 0)
		{
			updateBounds(g2);
		}
		return new Rectangle2D.Float(absoluteBounds.x, absoluteBounds.y, absoluteBounds.width, absoluteBounds.height);
	}

	/**
	 * Sets property highlighted.
	 *
	 * @param highlighted The new value.
	 */
	public void setHighlighted(boolean highlighted)
	{
		this.highlighted = highlighted;
	}

	/**
	 * Gets property highlighted.
	 *
	 * @return true if highlighted.
	 */
	public boolean isHighlighted()
	{
		return highlighted;
	}

	/**
	 * Get the style.
	 *
	 * @return The style or null.
	 */
	public DrawStyle getStyle()
	{
		return context == null ? null : (isHighlighted() ? context.highlighted : context.normal);
	}

	/**
	 * Sets the base position.<br>
	 * Doesn't set dirty as this method is used to set up the visual.
	 *
	 * @param x The new X ordinate.
	 * @param y The new Y ordinate.
	 */
	public void setPosition(float x, float y)
	{
		if (absoluteBounds.x != x || absoluteBounds.y != y)
		{
			absoluteBounds.x = x;
			absoluteBounds.y = y;
			resetBounds();
		}
	}

	/**
	 * Gets the absolute base position.
	 *
	 * @return pt The Point to set.
	 */
	public Point2D.Float getAbsolutePosition()
	{
		return new Point2D.Float(absoluteBounds.x, absoluteBounds.y);
	}

	/**
	 * Gets the absolute base position.
	 *
	 * @param pt The Point to set.
	 */
	public void getAbsolutePosition(Point2D.Float pt)
	{
		pt.x = absoluteBounds.x;
		pt.y = absoluteBounds.y;
	}

	/**
	 * Gets the primitive at the absolute position.
	 *
	 * @param x The x ordinate.
	 * @param y The y ordinate.
	 * @return The found primitive or null.
	 */
	public abstract DrawPrimitive getEditablePrimitiveAt(float x, float y);

	/**
	 * Get bounds of primitive according to style and alignment.
	 *
	 * @param g2        The graphics context.
	 * @param primitive The primitive to measure.
	 * @return The bounds or null.
	 */
	public Rectangle2D.Float getBoundsOfPrimitive(Graphics2D g2, DrawPrimitive primitive)
	{
		if (absoluteBounds.width < 0)
			updateBounds(g2);

		if (primitive != null)
		{
			Dimension2DFloat dim = new Dimension2DFloat(absoluteBounds);
			final Point2D.Float pt = getAlignmentOffset(g2, primitive, dim, new Point2D.Float());
			pt.x += absoluteBounds.x;
			pt.y += absoluteBounds.y;
			return primitive.getBounds2D(pt, g2);
		}
		return null;
	}

	/**
	 * Helper to get a aligned position according to alignment mode.
	 *
	 * @param g2        The graphics context to use for dimension calculations.
	 * @param primitive The primitive
	 * @param pt        The point to store the result.
	 * @return The aligned base point or null if {@link com.bw.graph.Alignment#Hidden}. Always same as pt if not null.
	 * @see DrawPrimitive#getAlignment()
	 */
	protected Point2D.Float getAlignmentOffset(Graphics2D g2, DrawPrimitive primitive,
											   Dimension2DFloat dimension, Point2D.Float pt)
	{
		switch (primitive.getAlignment())
		{
			case Left:
				pt.x = 0;
				break;
			case Center:
			{
				Dimension2DFloat dim = primitive.getDimension(g2);
				pt.x = (dimension.width - dim.width) / 2f - 1;
			}
			break;
			case Right:
			{
				Dimension2DFloat dim = primitive.getDimension(g2);
				pt.x = dimension.width - dim.width;
			}
			break;
			case Hidden:
				return null;
		}
		pt.y = 0;
		return pt;
	}

	/**
	 * Gets the absolute center position.
	 *
	 * @param g2 The graphics context to use for calculations.
	 * @return the position of the center in absolute coordinates.
	 */
	public Point2D.Float getCenterPosition(Graphics2D g2)
	{
		Rectangle2D.Float bounds = getAbsoluteBounds2D(g2);
		return new Point2D.Float((float) bounds.getCenterX(), (float) bounds.getCenterY());
	}

	/**
	 * Marks the visual for repaint.
	 */
	public void repaint()
	{
		repaintTriggered = true;
	}

	/**
	 * Writes the visual as SVG to the writer.
	 *
	 * @param sw The Writer to write to.
	 * @param g2 The graphics context - only for calculation. Must not be modified in any way.
	 */
	public abstract void toSVG(SVGWriter sw, Graphics2D g2);

	/**
	 * Gets the Id of the visual
	 *
	 * @return The id.
	 */
	public Object getId()
	{
		return id;
	}

	/**
	 * Set the identification.
	 *
	 * @param id The identification Object.
	 */
	public void setId(Object id)
	{
		this.id = id;
	}

	/**
	 * Dispose all resources.
	 */
	public void dispose()
	{
		id = null;
		context = null;
	}

	/**
	 * Sets the preferred dimension.<br>
	 * If no preferred dimension is set, the dimension of the visual is calculated from its contents.
	 *
	 * @param dimension The dimension or null to reset to calculated dimension.
	 */
	public void setPreferredDimension(Dimension2DFloat dimension)
	{
		if (dimension == null)
		{
			if (this.dimension != null)
			{
				this.dimension = null;
				resetBounds();
				repaint();
			}
		}
		else
			setPreferredDimension(dimension.width, dimension.height);
	}

	/**
	 * Sets the preferred dimension.
	 *
	 * @param width  The width.
	 * @param height The height.
	 */
	public void setPreferredDimension(float width, float height)
	{
		if (dimension == null || dimension.width != width || dimension.height != height)
		{
			dirty = true;
			this.dimension = new Dimension2DFloat(width, height);
			resetBounds();
			repaint();
		}
	}

	/**
	 * Gets the preferred dimension or null.
	 *
	 * @return The dimension or null.
	 */
	public Dimension2DFloat getPreferredDimension()
	{
		return dimension;
	}

	/**
	 * Get the GraphConfiguration from current context.<br>
	 * Same as {@link #getDrawContext()}.configuration.
	 *
	 * @return the GraphConfiguration
	 */
	public GraphConfiguration getConfiguration()
	{
		return context.configuration;
	}

	/**
	 * Get DrawContext
	 *
	 * @return The context.
	 */
	public DrawContext getDrawContext()
	{
		return context;
	}


	/**
	 * Sets modified status.
	 *
	 * @param modified The new modified.
	 */
	public void setModified(boolean modified)
	{
		dirty = modified;
	}

	/**
	 * Checks if the visual itself or the inner model was modified
	 *
	 * @return true if modified.
	 */
	public boolean isModified()
	{
		return dirty;
	}

	@Override
	public String toString()
	{
		return String.valueOf(id);
	}

	public abstract <T extends DrawPrimitive> T getPrimitiveOf(Class<T> primitiveClass);
}
