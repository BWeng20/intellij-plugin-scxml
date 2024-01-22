package com.bw.graph.visual;

import com.bw.graph.DrawContext;
import com.bw.graph.GraphConfiguration;
import com.bw.graph.primitive.DrawPrimitive;
import com.bw.graph.util.Dimension2DFloat;
import com.bw.graph.util.ImageUtil;
import com.bw.svg.SVGElement;
import com.bw.svg.SVGWriter;

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
 * A visual that is build of generic primitives.
 */
public class GenericPrimitiveVisual extends Visual
{
	/**
	 * The drawing primitives.
	 */
	protected List<DrawPrimitive> primitives = new LinkedList<>();

	private BufferedImage buffer;

	/**
	 * Statistical counter for double buffer creation.
	 */
	private long buffersRecreated = 0;

	/**
	 * Create a new empty visual.
	 *
	 * @param id      The identification. Can be null.
	 * @param context The Drawing context to use.
	 */
	public GenericPrimitiveVisual(Object id, DrawContext context)
	{
		super(id, context);
	}

	/**
	 * Draw the visual.
	 *
	 * @param g2 The Graphics context
	 */
	@Override
	protected void drawRelative(Graphics2D g2)
	{
		if (absoluteBounds.width < 0)
			updateBounds(g2);

		final GraphConfiguration graphConfiguration = context.configuration;
		final GraphicsConfiguration graphicsConfiguration = g2.getDeviceConfiguration();

		if (graphConfiguration.buffered && graphicsConfiguration.getDevice()
																.getType() != GraphicsDevice.TYPE_PRINTER)
		{
			// The buffer-image needs to use native, unscaled coordinates,
			// otherwise the result will get blurry.
			float offset = getStyle().getStrokeWidth() + 1;
			float scaleX = (float) g2.getTransform()
									 .getScaleX();
			float scaleY = (float) g2.getTransform()
									 .getScaleY();
			if (buffer == null || repaintTriggered)
			{
				float width = absoluteBounds.width;
				float height = absoluteBounds.height;

				int scaledBoundsWidth = (int) Math.ceil((width + 2 * offset) * scaleX);
				int scaledBoundsHeight = (int) Math.ceil((height + 2 * offset) * scaleY);
				if (buffer == null || buffer.getWidth() != scaledBoundsWidth || buffer.getHeight() != scaledBoundsHeight)
				{
					++buffersRecreated;
					buffer = ImageUtil.createCompatibleImage(graphicsConfiguration, scaledBoundsWidth, scaledBoundsHeight);
				}
				repaintTriggered = false;
				Graphics2D g2Buffered = buffer.createGraphics();
				try
				{
					if (graphConfiguration.antialiasing)
						g2Buffered.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

					g2Buffered.scale(scaleX, scaleY);
					g2Buffered.translate(offset, offset);

					// g2Buffered.translate(offset, offset);
					forAllPrimitives(g2Buffered, DrawPrimitive::draw,
							new Dimension2DFloat(width, height));
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
				g2.drawImage(buffer, null, (int) (-offset * scaleX), (int) (-offset * scaleY));
			}
			finally
			{
				g2.setTransform(orgTransform);
			}
		}
		else
		{
			forAllPrimitives(g2, DrawPrimitive::draw, null);
		}
	}

	@Override
	public DrawPrimitive getEditablePrimitiveAt(float x, float y)
	{
		Point2D.Float pt = new Point2D.Float();
		getAbsolutePosition(pt);
		Dimension2DFloat dimension = new Dimension2DFloat(getAbsoluteBounds2D(null));
		DrawPrimitive p = null;
		Point2D.Float alignedPos = new Point2D.Float();

		for (DrawPrimitive pw : primitives)
		{
			if (pw.isEditable())
			{
				Rectangle2D.Float rt = pw.getBounds2D(pt, null);
				getAlignmentOffset(null, pw, dimension, alignedPos);
				rt.x += alignedPos.x;
				rt.y += alignedPos.y;

				if (rt.contains(x, y))
				{
					p = pw;
				}
			}
		}
		if (p != null)
			p.setVisual(this);
		return p;
	}

	/**
	 * Updates {@link #absoluteBounds}.
	 *
	 * @param graphics The graphics context to use for calculations.
	 */
	protected void updateBounds(Graphics2D graphics)
	{
		Dimension2DFloat dim = getPreferredDimension();
		if (dim == null)
		{
			absoluteBounds.width = 0;
			absoluteBounds.height = 0;

			for (DrawPrimitive primitive : primitives)
			{
				Rectangle2D.Float primitiveBounds = primitive.getBounds2D(absoluteBounds.x, absoluteBounds.y, graphics);
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
				final float y2 = primitiveBounds.y + primitiveBounds.height;

				if ((this.absoluteBounds.x + this.absoluteBounds.width) < x2) this.absoluteBounds.width = x2 - this.absoluteBounds.x;
				if ((this.absoluteBounds.y + this.absoluteBounds.height) < y2) this.absoluteBounds.height = y2 - this.absoluteBounds.y;
			}
		}
		else
		{
			this.absoluteBounds.width = dim.width;
			this.absoluteBounds.height = dim.height;
		}
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
		repaint();
	}

	/**
	 * Removes all Draw-Primitives.
	 */
	public void removeAllDrawingPrimitives()
	{
		primitives.clear();
		resetBounds();
		repaint();
	}

	@Override
	public void repaint()
	{
		super.repaint();
		for (DrawPrimitive primitive : primitives)
		{
			primitive.repaint();
		}
	}


	/**
	 * Writes the visual as SVG to the writer.
	 *
	 * @param sw The Writer to write to.
	 * @param g2 The graphics context - only for calculation. Must not be modified in any way.
	 */
	@Override
	public void toSVG(SVGWriter sw, Graphics2D g2)
	{
		sw.startElement(SVGElement.g);
		Point2D.Float pt = getAbsolutePosition();
		forAllPrimitives(g2,
				(primitive, g) -> primitive.toSVG(sw, g, pt));
		sw.endElement();
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
		 */
		void consume(DrawPrimitive primitive, Graphics2D g2);

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
		if (absoluteBounds.width < 0)
			updateBounds(g2);

		forAllPrimitives(g2, consumer, null);
	}

	/**
	 * Calls a function on all primitives.
	 * Position is adapted according to alignment.
	 *
	 * @param g2        The graphics context.
	 * @param consumer  The consumer to call.
	 * @param dimension The dimension to adapt position to.
	 */
	protected void forAllPrimitives(Graphics2D g2, PrimitiveConsumer consumer, Dimension2DFloat dimension)
	{
		if (dimension == null)
			dimension = new Dimension2DFloat(absoluteBounds);

		if (dimension != null)
		{
			final Point2D.Float pt = new Point2D.Float();
			for (DrawPrimitive primitive : primitives)
			{
				// Optimized to spare the call to getAlignmentOffset if not needed.
				switch (primitive.getAlignment())
				{
					case Left:
						consumer.consume(primitive, g2);
						break;
					case Center:
					case Right:
					{
						getAlignmentOffset(g2, primitive, dimension, pt);
						g2.translate(pt.x, pt.y);
						consumer.consume(primitive, g2);
						g2.translate(-pt.x, -pt.y);
					}
					break;
					case Hidden:
						break;
				}
			}
		}
	}

	@Override
	public boolean isModified()
	{
		if (dirty)
			return true;
		else
		{
			for (DrawPrimitive primitive : primitives)
			{
				if (primitive.isModified())
				{
					dirty = true;
					return true;
				}
			}
		}
		return false;
	}


	@Override
	public void dispose()
	{
		super.dispose();
		buffer = null;
		for (DrawPrimitive primitive : primitives)
		{
			primitive.dispose();
		}
		primitives.clear();
	}

	public <T extends DrawPrimitive> T getPrimitiveOf(Class<T> primitiveClass)
	{
		for (DrawPrimitive primitive : primitives)
		{
			if (primitiveClass.isAssignableFrom(primitive.getClass()))
				return (T) primitive;
		}
		return null;
	}

	@Override
	public void setModified(boolean modified)
	{
		super.setModified(modified);
		if (!modified)
		{
			for (DrawPrimitive primitive : primitives)
				primitive.setModified(false);
		}
	}

}
