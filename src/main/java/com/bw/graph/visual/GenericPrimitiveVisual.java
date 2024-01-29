package com.bw.graph.visual;

import com.bw.graph.DrawContext;
import com.bw.graph.GraphConfiguration;
import com.bw.graph.primitive.DrawPrimitive;
import com.bw.graph.util.Dimension2DFloat;
import com.bw.graph.util.ImageUtil;
import com.bw.svg.SVGElement;
import com.bw.svg.SVGWriter;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Collections;
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
	protected List<DrawPrimitive> _primitives = new LinkedList<>();

	private BufferedImage _buffer;

	/**
	 * Statistical counter for double buffer creation.
	 */
	private long _buffersRecreated = 0;

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

	private PrimitiveConsumer _drawWithAlignment = (primitive, g2, offset) ->
	{
		if ((getFlags() & primitive.getFlags()) != 0)
		{
			g2.translate(offset.x, offset.y);
			primitive.draw(g2);
			g2.translate(-offset.x, -offset.y);
		}
	};

	/**
	 * Draw the visual.
	 *
	 * @param g2 The Graphics context
	 */
	@Override
	protected void drawRelative(Graphics2D g2)
	{
		if (_absoluteBounds.width < 0)
			updateBounds(g2);

		final GraphConfiguration graphConfiguration = _context._configuration;
		final GraphicsConfiguration graphicsConfiguration = g2.getDeviceConfiguration();

		if (graphConfiguration._buffered && graphicsConfiguration.getDevice()
																 .getType() != GraphicsDevice.TYPE_PRINTER)
		{
			// The buffer-image needs to use native, unscaled coordinates,
			// otherwise the result will get blurry.

			float inset = getStyle().getStrokeWidth() + 1;

			// If the actual bounds starts not on base position, we need to translate
			// to get inside the buffer.
			float offsetX = inset + _absolutePosition.x - _absoluteBounds.x;
			float offsetY = inset + _absolutePosition.y - _absoluteBounds.y;


			float scaleX = (float) g2.getTransform()
									 .getScaleX();
			float scaleY = (float) g2.getTransform()
									 .getScaleY();
			if (_buffer == null || _offscreenBuffersInvalid)
			{
				float width = _absoluteBounds.width;
				float height = _absoluteBounds.height;

				boolean needsToBeCleared = true;

				int scaledBoundsWidth = (int) Math.ceil((width + 2 * inset) * scaleX);
				int scaledBoundsHeight = (int) Math.ceil((height + 2 * inset) * scaleY);
				if (_buffer == null || _buffer.getWidth() != scaledBoundsWidth || _buffer.getHeight() != scaledBoundsHeight)
				{
					++_buffersRecreated;
					_buffer = ImageUtil.createCompatibleImage(graphicsConfiguration, scaledBoundsWidth, scaledBoundsHeight);
					needsToBeCleared = false;
				}
				Graphics2D g2Buffered = _buffer.createGraphics();
				try
				{
					if (needsToBeCleared)
					{
						Composite c = g2Buffered.getComposite();
						g2Buffered.setComposite(AlphaComposite.Clear);
						g2Buffered.fillRect(0, 0, scaledBoundsWidth, scaledBoundsHeight);
						g2Buffered.setComposite(c);
					}

					if (graphConfiguration._antialiasing)
						g2Buffered.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

					g2Buffered.scale(scaleX, scaleY);

					g2Buffered.translate(offsetX, offsetY);

					forAllPrimitives(g2Buffered, _drawWithAlignment,
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
				g2.drawImage(_buffer, null, (int) (-offsetX * scaleX), (int) (-offsetY * scaleY));
			}
			finally
			{
				g2.setTransform(orgTransform);
			}
		}
		else
		{
			forAllPrimitives(g2, _drawWithAlignment, null);
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

		for (DrawPrimitive pw : _primitives)
		{
			if (pw.isFlagSet(VisualFlags.EDITABLE))
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
	 * Updates {@link #_absoluteBounds}.
	 *
	 * @param graphics The graphics context to use for calculations.
	 */
	protected void updateBounds(Graphics2D graphics)
	{
		Dimension2DFloat dim = getPreferredDimension();
		if (dim == null)
		{
			Rectangle2D.Float localBounds = new Rectangle2D.Float(Float.MAX_VALUE, Float.MAX_VALUE, 0, 0);

			if (_primitives.isEmpty())
			{
				localBounds.x = 0;
				localBounds.y = 0;
			}
			else
			{
				for (DrawPrimitive primitive : _primitives)
				{
					Rectangle2D.Float primitiveBounds = primitive.getBounds2D(this._absolutePosition.x, this._absolutePosition.y, graphics);
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

					if (localBounds.x > primitiveBounds.x)
						localBounds.x = primitiveBounds.x;
					if (localBounds.y > primitiveBounds.y)
						localBounds.y = primitiveBounds.y;

					if ((localBounds.x + localBounds.width) < x2)
						localBounds.width = x2 - localBounds.x;
					if ((localBounds.y + localBounds.height) < y2)
						localBounds.height = y2 - localBounds.y;
				}
			}
			this._absoluteBounds.x = localBounds.x;
			this._absoluteBounds.y = localBounds.y;
			this._absoluteBounds.width = localBounds.width;
			this._absoluteBounds.height = localBounds.height;
		}
		else
		{
			this._absoluteBounds.x = _absolutePosition.x;
			this._absoluteBounds.y = _absolutePosition.y;
			this._absoluteBounds.width = dim._width;
			this._absoluteBounds.height = dim._height;
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
		_primitives.remove(primitive);
		_primitives.add(primitive);
		resetBounds();
		invalidateBuffers();
	}

	/**
	 * Removes all Draw-Primitives.
	 */
	public void removeAllDrawingPrimitives()
	{
		_primitives.clear();
		resetBounds();
		invalidateBuffers();
	}

	@Override
	public void invalidateBuffers()
	{
		super.invalidateBuffers();
		for (DrawPrimitive primitive : _primitives)
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
		final Point2D.Float pt = getAbsolutePosition();
		forAllPrimitives(g2, (primitive, g, offset) ->
		{
			if (primitive.isFlagSet(VisualFlags.ALWAYS))
			{
				Point2D.Float tempPos = new Point2D.Float(offset.x + pt.x, offset.y + pt.y);
				primitive.toSVG(sw, g, tempPos);
			}
		});
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
		 * @param offset    Additional alignment offset
		 */
		void consume(DrawPrimitive primitive, Graphics2D g2, Point2D.Float offset);

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
		if (_absoluteBounds.width < 0)
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
			dimension = new Dimension2DFloat(_absoluteBounds);

		final Point2D.Float pt = new Point2D.Float();
		final Point2D.Float zero = new Point2D.Float();
		for (DrawPrimitive primitive : _primitives)
		{
			// Optimized to spare the call to getAlignmentOffset if not needed.
			switch (primitive.getAlignment())
			{
				case Left:
					consumer.consume(primitive, g2, zero);
					break;
				case Center:
				case Right:
				{
					getAlignmentOffset(g2, primitive, dimension, pt);
					consumer.consume(primitive, g2, pt);
				}
				break;
				case Hidden:
					break;
			}
		}
	}

	@Override
	public boolean isModified()
	{
		return isFlagSet(VisualFlags.MODIFIED);
	}

	@Override
	public void dispose()
	{
		super.dispose();
		_buffer = null;
		for (DrawPrimitive primitive : _primitives)
		{
			primitive.dispose();
		}
		_primitives.clear();
	}

	@Override
	public List<DrawPrimitive> getPrimitives()
	{
		return Collections.unmodifiableList(_primitives);
	}

	@Override
	public void setFlags(int flags)
	{
		if ((flags | this._flags) != this._flags)
		{
			super.setFlags(flags);
			for (DrawPrimitive primitive : _primitives)
				primitive.setFlags(flags);
		}
	}


	@Override
	public void clearFlags(int flags)
	{
		if ((this._flags & flags) != 0)
		{
			super.clearFlags(flags);
			for (DrawPrimitive primitive : _primitives)
				primitive.clearFlags(flags);
		}
	}

}
