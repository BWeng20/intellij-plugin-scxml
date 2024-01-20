package com.bw.graph.visual;

import com.bw.graph.DrawContext;
import com.bw.graph.DrawStyle;
import com.bw.graph.GraphConfiguration;
import com.bw.graph.primitive.DrawPrimitive;
import com.bw.graph.util.Dimension2DFloat;
import com.bw.graph.util.ImageUtil;
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
 * A visual that is build of generic primitives.
 */
public class GenericVisual extends Visual
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
	public GenericVisual(Object id, DrawContext context)
	{
		super(id, context);
	}

	/**
	 * Draw the visual.
	 *
	 * @param g2    The Graphics context
	 * @param style The style to use.
	 */
	@Override
	public void drawIntern(Graphics2D g2, final DrawStyle style)
	{
		if (x2 < 0)
			updateBounds(g2);

		final GraphConfiguration graphConfiguration = context.configuration;
		final GraphicsConfiguration graphicsConfiguration = g2.getDeviceConfiguration();

		if (graphConfiguration.buffered && graphicsConfiguration.getDevice().getType() != GraphicsDevice.TYPE_PRINTER)
		{
			// Double buffering needs to consider the current scale, otherwise the result will get blurry.
			// The buffer-image needs to use native, unscaled coordinates.
			float offset = 1;
			float scaleX = (float) g2.getTransform()
									 .getScaleX();
			float scaleY = (float) g2.getTransform()
									 .getScaleY();
			if (buffer == null || repaintTriggered)
			{
				Rectangle2D.Float bounds = getBounds2D(g2);

				int scaledBoundsWidth = (int) Math.ceil((bounds.width + 2 * offset) * scaleX);
				int scaledBoundsHeight = (int) Math.ceil((bounds.height + 2 * offset) * scaleY);
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

					if (false)
					{
						g2Buffered.setPaint(Color.RED);
						g2Buffered.fillRect(0, 0, buffer.getWidth(), buffer.getHeight());
						g2Buffered.setPaint(Color.BLUE);
						g2Buffered.drawRect((int) offset, (int) offset, (int) bounds.width - 1, (int) bounds.height - 1);
					}

					g2Buffered.scale(scaleX, scaleY);

					// g2Buffered.translate(offset, offset);
					forAllPrimitives(g2Buffered, DrawPrimitive::draw,
							new Rectangle2D.Float(offset, offset, bounds.width, bounds.height),
							style);
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
			try
			{
				forAllPrimitives(g2, DrawPrimitive::draw, null, style);
			}
			finally
			{
			}
		}
	}

	@Override
	public DrawPrimitive getEditablePrimitiveAt(float x, float y)
	{
		Point2D.Float pt = new Point2D.Float();
		getPosition(pt);
		Rectangle2D.Float bounds = getBounds2D(null);
		DrawStyle style = getStyle();
		DrawPrimitive p = null;
		Point2D.Float alignedPos = new Point2D.Float();
		for (DrawPrimitive pw : primitives)
		{
			if (pw.isEditable())
			{
				Rectangle2D.Float rt = pw.getBounds2D(pt, null, style);
				alignPosition(null, pw, bounds, style, alignedPos);
				rt.x = alignedPos.x;
				rt.y = alignedPos.y;

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
	 * Updates {@link #x2} and {@link #y2}.
	 *
	 * @param graphics The graphics context to use for calculations.
	 */
	protected void updateBounds(Graphics2D graphics)
	{
		Dimension2DFloat dim = getPreferredDimension();
		if (dim == null)
		{
			x2 = position.x;
			y2 = position.y;

			if (subModel != null)
			{
				GraphConfiguration cfg = context.configuration;
				x2 += cfg.innerModelBoxMinDimension.width + cfg.innerModelBoxInsets.left + cfg.innerModelBoxInsets.right;
				y2 += cfg.innerModelBoxMinDimension.height + cfg.innerModelBoxInsets.top + cfg.innerModelBoxInsets.bottom;
			}

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
		else
		{
			x2 = position.x + dim.width;
			y2 = position.y + dim.height;
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

		forAllPrimitives(g2, consumer, null, null);
	}

	/**
	 * Calls a function on all primitives.
	 * Position is adapted according to alignment.
	 *
	 * @param g2       The graphics context.
	 * @param consumer The consumer to call.
	 * @param bounds   The bounds to adapt relative positions to.
	 * @param style    The style to use.
	 */
	protected void forAllPrimitives(Graphics2D g2, PrimitiveConsumer consumer, Rectangle2D.Float bounds, DrawStyle style)
	{
		if (bounds == null && position != null)
			bounds = new Rectangle2D.Float(position.x, position.y, x2 - position.x, y2 - position.y);

		if (bounds != null)
		{
			final Point2D.Float pt = new Point2D.Float();
			final Point2D.Float pos = new Point2D.Float(bounds.x, bounds.y);
			final DrawStyle actualStyle = style == null ? (highlighted ? context.highlighted : context.normal) : style;
			for (DrawPrimitive primitive : primitives)
			{
				switch (primitive.getAlignment())
				{
					case Left:
						consumer.consume(primitive, g2, pos, actualStyle);
						break;
					case Center:
					case Right:
					{
						alignPosition(g2, primitive, bounds, actualStyle, pt);
						consumer.consume(primitive, g2, pt, actualStyle);
					}
					break;
					case Hidden:
						break;
				}
			}
		}
	}

	@Override
	public void dispose()
	{
		super.dispose();
		buffer = null;
		primitives.clear();
	}
}
