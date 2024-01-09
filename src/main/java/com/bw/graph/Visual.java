package com.bw.graph;

import com.bw.svg.SVGWriter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
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

	private BufferedImage buffer;
	private boolean redrawNeeded = true;

	/**
	 * Statistical counter for double buffer creation.
	 */
	private long buffersRecreated = 0;

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
		if (x2 < 0)
			updateBounds(g2);

		final GraphConfiguration graphConfiguration = context.configuration;
		final GraphicsConfiguration graphicsConfiguration = g2.getDeviceConfiguration();


		if (graphConfiguration.doubleBuffered && graphicsConfiguration.getDevice().getType() != GraphicsDevice.TYPE_PRINTER)
		{
			// Double buffering needs to consider the current scale, otherwise the result will get blurry.
			// The buffer-image needs to use native, unscaled coordinates.
			float offset = 1;
			float scaleX = (float) g2.getTransform().getScaleX();
			float scaleY = (float) g2.getTransform().getScaleY();
			if (buffer == null || redrawNeeded)
			{
				Rectangle2D.Float bounds = getBounds2D(g2);

				int scaledBoundsWidth = (int) Math.ceil((bounds.width + 2 * offset) * scaleX);
				int scaledBoundsHeight = (int) Math.ceil((bounds.height + 2 * offset) * scaleY);
				if (buffer == null || buffer.getWidth() != scaledBoundsWidth || buffer.getHeight() != scaledBoundsHeight)
				{
					++buffersRecreated;
					buffer = ImageUtil.createCompatibleImage(graphicsConfiguration, scaledBoundsWidth, scaledBoundsHeight);
				}
				redrawNeeded = false;
				Graphics2D g2Buffered = buffer.createGraphics();
				try
				{
					if (graphConfiguration.antialiasing)
						g2Buffered.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

					if (false)
					{
						g2Buffered.setPaint(Color.RED);
						g2Buffered.fillRect(0, 0, buffer.getWidth(), buffer.getHeight());
						g2Buffered.setPaint(Color.BLUE);
						g2Buffered.drawRect((int) offset, (int) offset, (int) bounds.width - 1, (int) bounds.height - 1);
					}

					g2Buffered.scale(scaleX, scaleY);

					// g2Buffered.translate(offset, offset);
					forAllPrimitives(g2Buffered, DrawPrimitive::draw, new Rectangle2D.Float(offset, offset, bounds.width, bounds.height));
				}
				finally
				{
					g2Buffered.dispose();
				}
			}
			AffineTransform orgTransform = g2.getTransform();
			try
			{
				// Scale to 1:1
				g2.scale(1 / scaleX, 1 / scaleY);
				// Draw to "scaled" position but with native pixels sizes.
				g2.drawImage(buffer, null,
						(int) ((position.x - offset) * scaleX),
						(int) ((position.y - offset) * scaleY));
			}
			finally
			{
				g2.setTransform(orgTransform);
			}
		}
		else
		{
			forAllPrimitives(g2, DrawPrimitive::draw);
		}
	}

	/**
	 * Consumer used for {@link #forAllPrimitives(Graphics2D, PrimitiveConsumer)}
	 */
	protected interface PrimitiveConsumer
	{

		/**
		 * Called for each primitive.
		 *
		 * @param primitive The primitive.
		 * @param g2        The graphics context to use.
		 * @param position  The calculated effective position.
		 * @param style     The effective style to use.
		 */
		void consume(DrawPrimitive primitive,
					 Graphics2D g2, Point2D.Float position, DrawStyle style);

	}


	/**
	 * Calls a function on all primitives.
	 * Relative positions are adapted to current visual position.
	 *
	 * @param g2       The graphics context.
	 * @param consumer The consumer to call.
	 */
	protected void forAllPrimitives(Graphics2D g2, PrimitiveConsumer consumer)
	{
		if (x2 < 0)
			updateBounds(g2);

		forAllPrimitives(g2, consumer, new Rectangle2D.Float(position.x, position.y, x2 - position.x, y2 - position.y));
	}

	/**
	 * Calls a function on all primitives.
	 *
	 * @param g2       The graphics context.
	 * @param consumer The consumer to call.
	 * @param bounds   The bounds to adapt relative positions to.
	 */
	protected void forAllPrimitives(Graphics2D g2, PrimitiveConsumer consumer, final Rectangle2D.Float bounds)
	{
		if (bounds != null)
		{
			final Point2D.Float pt = new Point2D.Float();
			final Point2D.Float pos = new Point2D.Float(bounds.x, bounds.y);
			for (DrawPrimitive primitive : primitives)
			{
				DrawStyle style = highlighted ? context.highlighted : context.normal;
				switch (primitive.getAlignment())
				{
					case Left:
						consumer.consume(primitive, g2, pos, style);
						break;
					case Center:
					{
						Dimension2DFloat dim = primitive.getDimension(g2, style);
						pt.x = bounds.x + (bounds.width - dim.width) / 2f - 1;
						pt.y = bounds.y;
						consumer.consume(primitive, g2, pt, style);
					}
					break;
					case Right:
					{
						Dimension2DFloat dim = primitive.getDimension(g2, style);
						pt.x = bounds.x + bounds.width - dim.width;
						pt.y = bounds.y;
						consumer.consume(primitive, g2, pt, style);
					}
					break;
					case Hidden:
						break;
				}
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
			Rectangle2D.Float primitiveBounds = primitive.getBounds2D(position, graphics, context.normal);
			switch (primitive.getAlignment())
			{
				case Left:
					break;
				case Center:
				case Right:
					primitiveBounds.width += primitive.getRelativePosition().x;
					break;
				case Hidden:
					continue;
			}
			final float x2 = primitiveBounds.x + primitiveBounds.width;
			if (this.x2 < x2) this.x2 = x2;
			final float y2 = primitiveBounds.y + primitiveBounds.height;
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
		redraw();
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

	/**
	 * Writes the visual as SVG to the writer.
	 *
	 * @param sw The Writer to write to.
	 * @param g2 The graphics context - only for calculation. Must not be modified in any way.
	 */
	public void toSVG(SVGWriter sw, Graphics2D g2)
	{
		forAllPrimitives(g2,
				(primitive, g, position, style) -> primitive.toSVG(sw, position, style));
	}

	/**
	 * Mark the visual to redraw.
	 */
	public void redraw()
	{
		redrawNeeded = true;
	}

}
