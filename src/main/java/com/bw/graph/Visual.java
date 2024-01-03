package com.bw.graph;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;

/**
 * A visual representation in the graph.
 */
public class Visual
{
	/**
	 * True of the visual is high-lighted.
	 */
	protected boolean highlighted;

	/**
	 * The drawing primitives.
	 */
	protected List<DrawPrimitive> primitives = new LinkedList<>();

	/**
	 * The base position of the visual.
	 */
	protected Point2D.Float position = new Point2D.Float(0, 0);

	/**
	 * The maximal x position inside the visual.<br>
	 * The value is lazy calculated and updated if set to a negative value.
	 */
	protected float x2 = -1;

	/**
	 * The maximal y position inside the visual.<br>
	 * The value is lazy calculated and updated if set to a negative value.
	 */
	protected float y2 = -1;

	/**
	 * The drawing context to use for painting and size calculations.
	 */
	protected DrawContext context;

	/**
	 * Create a new empty visual.
	 *
	 * @param context The Drawing context to use.
	 */
	public Visual(DrawContext context)
	{
		this.context = context;
	}

	/**
	 * Draw the visual.
	 *
	 * @param g2 The Graphics context
	 */
	public void draw(Graphics2D g2)
	{
		if (position != null)
		{
			if (x2 < 0)
				updateBounds(g2);

			for (DrawPrimitive primitive : primitives)
			{
				primitive.draw(g2, position, highlighted ? context.highlighted : context.normal);
			}
		}
	}

	/**
	 * Checks if a point is inside the area of the visual.
	 *
	 * @param x X position.
	 * @param y Y position.
	 * @return true if (x,y) is inside the visual.
	 */
	public boolean containsPoint(float x, float y)
	{
		return (x >= position.x && x <= x2) && (y >= position.y && y <= y2);
	}

	/**
	 * Updates {@link #x2} and {@link #y2}.
	 *
	 * @param graphics The graphics context to use for calculations.
	 */
	protected void updateBounds(Graphics2D graphics)
	{
		x2 = position.x;
		y2 = position.y;

		for (DrawPrimitive primitive : primitives)
		{
			Rectangle2D.Float r = primitive.getBounds2D(position, graphics, context.normal);
			final float x2 = r.x + r.width;
			if (this.x2 < x2) this.x2 = x2;
			final float y2 = r.y + r.height;
			if (this.y2 < y2) this.y2 = y2;
		}
	}

	/**
	 * Moves the visual by some delta.
	 *
	 * @param x The X-Delta to move.
	 * @param y The Y-Delta to move.
	 */
	public void moveBy(float x, float y)
	{
		position.x += x;
		position.y += y;
		if (x2 >= 0)
		{
			x2 += x;
			y2 += y;
		}
	}

	/**
	 * Triggers the re-calculation of bounds on next paint.
	 */
	public void resetBounds()
	{
		x2 = -1;
	}

	/**
	 * Gets bounds if available.
	 *
	 * @return The bounds in local coordinates or null.
	 */
	public Rectangle2D.Float getBounds2D()
	{
		if (x2 < 0)
			return null;
		else
			return new Rectangle2D.Float(position.x, position.y, x2 - position.x, y2 - position.y);
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
	 * Adds a new drawing primitive to the visual.<br>
	 * Triggers also bounds-recalculation.
	 *
	 * @param primitive The new primitive to add.
	 * @see #resetBounds()
	 */
	public void addDrawingPrimitive(DrawPrimitive primitive)
	{
		primitives.remove(primitive);
		primitives.add(primitive);
		resetBounds();
	}

	/**
	 * Sets the base position.
	 *
	 * @param x The new X ordinate.
	 * @param y The new Y ordinate.
	 */
	public void setPosition(float x, float y)
	{
		position.x = x;
		position.y = y;
	}
}
