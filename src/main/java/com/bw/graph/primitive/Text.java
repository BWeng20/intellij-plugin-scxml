package com.bw.graph.primitive;

import com.bw.graph.DrawStyle;
import com.bw.graph.GraphConfiguration;
import com.bw.graph.util.Dimension2DFloat;
import com.bw.graph.visual.VisualFlags;
import com.bw.svg.SVGAttribute;
import com.bw.svg.SVGElement;
import com.bw.svg.SVGWriter;

import java.awt.Color;
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
	protected String _text;

	private Dimension2DFloat _lastStringDimension;

	/**
	 * Creates a new Primitive.
	 *
	 * @param x      The relative x-position
	 * @param y      The relative y-position
	 * @param text   The text to draw.
	 * @param config The configuration to use.
	 * @param style  The style or null if default style shall be used.
	 * @param flags  The initial flags. @see {@link VisualFlags}
	 */
	public Text(float x, float y, String text, GraphConfiguration config, DrawStyle style, int flags)
	{
		super(x, y, config, style, flags);
		this._text = text;
	}

	@Override
	protected void drawIntern(Graphics2D g2)
	{
		g2.setFont(_style._font);
		g2.setPaint(_style._textPaint);
		g2.drawString(_text, 0, g2.getFontMetrics()
								  .getAscent());
	}

	@Override
	protected Dimension2DFloat getInnerDimension(Graphics2D graphics)
	{
		if (graphics != null)
		{
			Rectangle2D r = _style._fontMetrics.getStringBounds(_text, graphics);
			_lastStringDimension = new Dimension2DFloat((float) r.getWidth(), (float) r.getHeight());
		}
		else if (_lastStringDimension == null)
			_lastStringDimension = new Dimension2DFloat(_text.length() * 12, 12);
		return new Dimension2DFloat(_lastStringDimension);
	}

	@Override
	protected void toSVGIntern(SVGWriter sw, Graphics2D g2, Point2D.Float pos)
	{
		sw.startElement(SVGElement.text);
		sw.writeAttribute(SVGAttribute.X, pos.x);
		sw.writeAttribute(SVGAttribute.Y, pos.y + _style._fontMetrics.getAscent());
		sw.startStyle();
		sw.writeAttribute(SVGAttribute.Stroke, (Color) null);
		sw.writeAttribute(SVGAttribute.Fill, _style._textPaint);
		sw.writeAttribute(SVGAttribute.FontFamily, _style._font.getFamily());
		sw.writeAttribute(SVGAttribute.FontSize, _style._font.getSize2D());
		sw.startContent();
		sw.writeEscaped(_text);
		sw.endElement();
	}

	/**
	 * Sets the text to draw.
	 *
	 * @param text The new text
	 */
	public void setText(String text)
	{
		_text = text == null ? "" : text;
	}

	/**
	 * Gets the shown text.
	 *
	 * @return The text.
	 */
	public String getText()
	{
		return _text;
	}
}