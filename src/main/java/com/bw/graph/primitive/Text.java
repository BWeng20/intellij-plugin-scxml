package com.bw.graph.primitive;

import com.bw.graph.DrawStyle;
import com.bw.graph.GraphConfiguration;
import com.bw.graph.util.Dimension2DFloat;
import com.bw.svg.SVGWriter;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * A Text.
 */
public class Text extends DrawPrimitive
{
	/**
	 * The text to show.
	 */
	protected String text;

	/**
	 * Creates a new Primitive.
	 *
	 * @param x      The relative x-position
	 * @param y      The relative y-position
	 * @param text   The text to draw.
	 * @param config The configuration to use.
	 * @param style  The style or null if default style shall be used.
	 */
	public Text(float x, float y, String text, GraphConfiguration config, DrawStyle style)
	{
		super(x, y, config, style);
		this.text = text;
	}

	@Override
	protected void drawIntern(Graphics2D g2, DrawStyle style)
	{
		g2.setFont(style.font);
		g2.setPaint(style.textPaint);
		g2.drawString(text, 0, g2.getFontMetrics()
								 .getAscent());
	}

	@Override
	protected Dimension2DFloat getInnerDimension(Graphics2D graphics, DrawStyle style)
	{
		if (graphics != null)
		{
			Rectangle2D r = style.fontMetrics.getStringBounds(text, graphics);

			return new Dimension2DFloat((float) r.getWidth(), (float) r.getHeight());
		}
		else
		{
			return new Dimension2DFloat(text.length() * 12, 12);
		}
	}

	@Override
	protected void toSVGIntern(SVGWriter sw, DrawStyle style, Point2D.Float pos)
	{
		sw.startElement("text");
		sw.writeAttribute("x", pos.x);
		sw.writeAttribute("y", pos.y + style.fontMetrics.getAscent());
		sw.startStyle();
		sw.writeAttribute("font-family", style.font.getFamily());
		sw.writeAttribute("font-size", style.font.getSize2D());
		sw.startContent();
		sw.writeEscaped(text);
		sw.endElement();
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