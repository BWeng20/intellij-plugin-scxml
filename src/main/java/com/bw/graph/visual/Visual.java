package com.bw.graph.visual;

import com.bw.graph.DrawContext;
import com.bw.graph.DrawStyle;
import com.bw.graph.VisualModel;
import com.bw.graph.util.Dimension2DFloat;
import com.bw.graph.util.InsetsFloat;
import com.bw.svg.SVGWriter;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
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
	 * A sub-model or null.
	 */
	protected VisualModel innerModel;

	/**
	 * The inner model target dimension inside this visual.
	 */
	protected Dimension2DFloat innerModelDimension = new Dimension2DFloat(200, 200);

	/**
	 * Insets for the inner model.
	 */
	protected InsetsFloat innerModelInsets = new InsetsFloat(20, 5, 5, 5);

	/**
	 * The parent the visual is bound to or null.
	 */
	protected Visual parent;

	/**
	 * True of the visual is high-lighted.
	 */
	protected boolean highlighted;

	/**
	 * The base position of the visual, relative to parent.
	 */
	protected Point2D.Float position = new Point2D.Float(0, 0);

	/**
	 * The maximal x position inside the visual, relative to parent.<br>
	 * The value is lazy calculated and updated if set to a negative value.
	 */
	protected float x2 = -1;

	/**
	 * The maximal y position inside the visual, relative to parent.<br>
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
		draw(g2, getStyle());
	}

	/**
	 * Draw the visual and (if availanble) the sub-model inside.<br>
	 *
	 * @param g2    The Graphics context
	 * @param style The style.
	 */
	public void draw(Graphics2D g2, DrawStyle style)
	{
		drawIntern(g2, style);

		if (innerModel != null)
		{
			Rectangle2D.Float bounds = getBounds2D(g2);
			Rectangle2D.Float subBounds = innerModel.getBounds2D(g2);

			// Calc scale, use minimum to keep aspect ratio
			float scale = Math.min(innerModelDimension.width / subBounds.width, innerModelDimension.height / subBounds.height);
			if (scale > 1f)
				scale = 1f;
			// Get resulting box for the sub-model.
			subBounds.x *= scale;
			subBounds.y *= scale;
			subBounds.width *= scale;
			subBounds.height *= scale;

			Rectangle2D.Float subModelBox = new Rectangle2D.Float(
					bounds.x + (bounds.width - (subBounds.width + innerModelInsets.left + innerModelInsets.right)) / 2f,
					bounds.y + bounds.height - (subBounds.height + innerModelInsets.bottom),
					subBounds.width,
					subBounds.height
			);

			AffineTransform orgAft = g2.getTransform();
			try
			{
				g2.translate(subModelBox.x, subModelBox.y);
				g2.scale(scale, scale);
				innerModel.draw(g2);
			}
			finally
			{
				g2.setTransform(orgAft);
			}
		}
	}


	/**
	 * Draw the visual.<br>
	 * If a sub-model is set, the area described by {@link #innerModelDimension} and {@link #innerModelInsets} shall be spared,
	 * as this area will be over-drawn by {@link #draw(Graphics2D, DrawStyle)}.
	 *
	 * @param g2    The Graphics context
	 * @param style The style.
	 */
	protected abstract void drawIntern(Graphics2D g2, DrawStyle style);

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
	 * Updates {@link #x2} and {@link #y2}.<br>
	 * If the visual supports sub-models, {@link #innerModelDimension} and {@link #innerModelInsets} needs to be considered.
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
	 * Gets the bounds of the visual.
	 *
	 * @param g2 The Graphic context to use for calculations. Will not be modified.
	 * @return The bounds in absolute coordinates, never null.
	 */
	public Rectangle2D.Float getBounds2D(Graphics2D g2)
	{
		if (x2 < 0)
			updateBounds(g2);
		Point2D.Float pos = getPosition();
		return new Rectangle2D.Float(pos.x, pos.y, x2 - position.x + 1, y2 - position.y + 1);
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
	 * Sets the inner model.
	 *
	 * @param model The model
	 */
	public void setInnerModel(VisualModel model)
	{
		innerModel = model;
	}

	/**
	 * Gets the inner model.
	 *
	 * @return The model
	 */
	public VisualModel getInnerModel()
	{
		return innerModel;
	}

	/**
	 * Sets the insert for drawing the inner model.
	 *
	 * @param insets The insets
	 */
	public void setInnerModelInsets(InsetsFloat insets)
	{
		if (innerModel != null)
			resetBounds();
		this.innerModelInsets = insets;
	}


	/**
	 * Gets the insert for drawing the inner model.
	 *
	 * @return The insets
	 */
	public InsetsFloat getInnerModelInsets()
	{
		return innerModelInsets;
	}

	/**
	 * Gets the dimension of the box for the inner model.
	 *
	 * @return The dimension.
	 */
	public Dimension2DFloat getInnerModelDimension()
	{
		return innerModelDimension;
	}

	/**
	 * Sets the dimension of the box for the inner model.
	 *
	 * @param innerModelDimension The dimension.
	 */
	public void setInnerModelDimension(Dimension2DFloat innerModelDimension)
	{
		this.innerModelDimension = innerModelDimension;
		if (innerModel != null)
			resetBounds();
	}

	/**
	 * Gets the absolute center position.
	 *
	 * @param g2 The graphics context to use for calculations.
	 * @return the position of the center in absolute coordinates.
	 */
	public Point2D.Float getCenterPosition(Graphics2D g2)
	{
		Rectangle2D.Float bounds = getBounds2D(g2);
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
	 * Dispose all resources.
	 */
	public void dispose()
	{
		if (innerModel != null)
		{
			innerModel.dispose();
			innerModel = null;
		}
		parent = null;
		id = null;
		context = null;
	}
}
