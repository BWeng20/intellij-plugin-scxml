package com.bw.graph;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * A Text.
 */
public class TextPrimitive extends DrawPrimitive
{
	/**
	 * The text to show.
	 */
	protected String text;

	/**
	 * Creates a new Primitive.
	 *
	 * @param relativePosition The relative position
	 * @param style            The style or null if default style shall be used.
	 * @param scalable         True is user can scale this primitive independent of parent.
	 * @param text             The text to draw.
	 */
	protected TextPrimitive(Point2D.Float relativePosition, DrawStyle style, boolean scalable, String text)
	{
		super(relativePosition, style, scalable);
		this.text = text;
	}

	@Override
	protected void drawIntern(Graphics2D g2, DrawStyle style, Point2D.Float pos)
	{
		g2.setFont(style.font);
		g2.setPaint(style.textPaint);
		g2.drawString(text, pos.x, pos.y + g2.getFontMetrics().getAscent());
	}

	@Override
	protected Dimension2DFloat getDimension(Graphics2D graphics, DrawStyle style)
	{
		Rectangle2D r = style.fontMetrics.getStringBounds(text, graphics);

		return new Dimension2DFloat((float) r.getWidth(), (float) r.getHeight());
	}

	/**
	 * Sets the text to draw.
	 *
	 * @param text The new text
	 */
	public void setText(String text)
	{
		this.text = text == null ? "" : text;
	}

}