package com.bw.graph.visual;

import com.bw.graph.DrawContext;
import com.bw.graph.DrawStyle;
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
	 * The parent of the visual is bound.
	 */
	protected Visual parent;


	/**
	 * True of the visual is high-lighted.
	 */
	protected boolean highlighted;

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
	 * Terue if a repaint was triggered.
	 */
	protected boolean repaintTriggered = false;

	/**
	 * Create a new empty visual.
	 *
	 * @param context The Drawing context to use.
	 */
	protected Visual(DrawContext context)
	{
		Objects.requireNonNull(context);
		this.context = context;
	}

	/**
	 * Draw the visual.
	 *
	 * @param g2 The Graphics context
	 */
	public void draw(Graphics2D g2)
	{
		draw(g2, getStyle());
	}


	/**
	 * Draw the visual.
	 *
	 * @param g2          The Graphics context
	 * @param parentStyle The style of the parent, used in case this visual has not own style.
	 */
	public abstract void draw(Graphics2D g2, DrawStyle parentStyle);

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
	protected abstract void updateBounds(Graphics2D graphics);

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
	 * @param g2 The Graphic context to use for calculations. Will not be modified.
	 * @return The bounds in local coordinates or null.
	 */
	public Rectangle2D.Float getBounds2D(Graphics2D g2)
	{
		if (x2 < 0)
			updateBounds(g2);
		return new Rectangle2D.Float(position.x, position.y, x2 - position.x + 1, y2 - position.y + 1);
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
	 * Get the local style if independent of parent.
	 *
	 * @return The style or null.
	 */
	public DrawStyle getStyle()
	{
		return context == null ? null : (isHighlighted() ? context.highlighted : context.normal);
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
		resetBounds();
	}

	/**
	 * Gets the absolute base position.
	 *
	 * @return the position.
	 */
	public Point2D.Float getPosition()
	{
		Point2D.Float parentPos = (parent == null) ? null : parent.getPosition();
		if (position != null)
		{
			return (parentPos == null) ? position : new Point2D.Float(parentPos.x + position.x, parentPos.y + position.y);
		}
		else
			return parentPos;
	}

	/**
	 * Gets the absolute center position.
	 *
	 * @return the position of the center.
	 */
	public Point2D.Float getCenterPosition()
	{
		Point2D.Float center = new Point2D.Float();
		center.setLocation(getPosition());
		center.x += (x2 - position.x) / 2f;
		center.y += (y2 - position.y) / 2f;

		return center;
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

}
