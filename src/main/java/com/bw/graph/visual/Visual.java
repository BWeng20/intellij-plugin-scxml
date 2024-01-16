package com.bw.graph.visual;

import com.bw.graph.DrawContext;
import com.bw.graph.DrawStyle;
import com.bw.graph.GraphConfiguration;
import com.bw.graph.VisualModel;
import com.bw.graph.util.Dimension2DFloat;
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
	 * The preferred dimension of the visual.
	 */
	protected Dimension2DFloat dimension;


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

			final GraphConfiguration cfg = context.configuration;

			// Calc scale, use minimum to keep aspect ratio
			float scale = Math.min(
					(cfg.innerModelBoxDimension.width - cfg.innerModelInsets.left - cfg.innerModelInsets.right) / subBounds.width,
					(cfg.innerModelBoxDimension.height - cfg.innerModelInsets.top - cfg.innerModelInsets.bottom) / subBounds.height);
			if (scale > 1f)
				scale = 1f;
			// Get resulting box for the sub-model.
			subBounds.x *= scale;
			subBounds.y *= scale;
			subBounds.width *= scale;
			subBounds.height *= scale;


			Rectangle2D.Float subModelBox = new Rectangle2D.Float(
					bounds.x + cfg.innerModelBoxInsets.left,
					bounds.y + cfg.innerModelBoxInsets.top, cfg.innerModelBoxDimension.width, cfg.innerModelBoxDimension.height);
			g2.setPaint(style.background);
			g2.fill(subModelBox);
			g2.setStroke(style.lineStroke);
			g2.setPaint(style.linePaint);
			g2.draw(subModelBox);

			AffineTransform orgAft = g2.getTransform();
			try
			{
				g2.translate(subModelBox.x + (subModelBox.width - subBounds.width) / 2f,
						subModelBox.y + (subModelBox.width - subBounds.height) / 2f);
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
	 * If a sub-model is set, the area described by {@link GraphConfiguration#innerModelBoxInsets} and {@link GraphConfiguration#innerModelBoxDimension} shall be spared,
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
	 * Gets the bounds of the visual.
	 *
	 * @param g2 The Graphic context to use for calculations. Will not be modified.
	 * @return The bounds in absolute coordinates, never null.
	 */
	public Rectangle2D.Float getBounds2D(Graphics2D g2)
	{
		if (x2 < 0)
			updateBounds(g2);
		Point2D.Float pos = new Point2D.Float();
		getPosition(pos);
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
	 * @param pt The Point to set.
	 */
	public void getPosition(Point2D.Float pt)
	{
		if (parent == null)
			pt.setLocation(0, 0);
		else
			parent.getPosition(pt);
		if (position != null)
		{
			pt.x += position.x;
			pt.y += position.y;
		}
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
		if (innerModel != null)
		{
			innerModel.repaint();
		}
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
	 * Get the parent.
	 *
	 * @return The parent or null.
	 */
	public Visual getParent()
	{
		return parent;
	}

}
