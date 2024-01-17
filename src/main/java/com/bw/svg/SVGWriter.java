package com.bw.svg;

import com.bw.XmlWriter;

import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.io.Writer;

/**
 * Printer to build SVG.
 */
public class SVGWriter extends XmlWriter
{
	private boolean inSvg = false;

	private boolean inStyle = false;
	private boolean atStyleStart = false;

	/**
	 * Creates an SVG Writer on top of some other writer.
	 *
	 * @param out The output writer
	 */
	public SVGWriter(Writer out)
	{
		super(out);
	}

	/**
	 * Appends a paint as style value. E.g. "#FFFFFF" for white.<br>
	 * Currently only {@link Color} is supported.
	 *
	 * @param paint The paint to append.
	 */
	public void write(Paint paint)
	{
		if (paint instanceof Color)
		{
			write('#');
			int rgb = ((Color) paint).getRGB();
			String hex = Integer.toHexString(rgb & 0xFFFFFF);
			for (int i = hex.length(); i < 6; ++i)
				write('0');
			write(hex);
		}
		else
		{
			// @TODO Handle for more complex paints via defs
			write("black");
		}
	}

	/**
	 * Write "stroke-width:" with the line width of the given stroke.
	 *
	 * @param strokeWith The stroke.
	 */
	public void writeStrokeWith(float strokeWith)
	{
		if (strokeWith > 0f)
		{
			writeAttributeProlog();
			write("stroke-width");
			writeAssign();
			writeRestrictedFloat(strokeWith);
			writeAttributeEpilog();
		}
	}

	/**
	 * Starts style definition inside xml element.
	 */
	public void startStyle()
	{
		if (inStyle)
			throw new IllegalStateException("startStyle called inside style definition.");
		if (tagStack.empty())
			throw new IllegalStateException("startStyle called outside element.");
		if (elementHasContent)
			throw new IllegalStateException("startStyle called inside content.");
		inStyle = true;
		atStyleStart = true;
	}

	/**
	 * Ends the style definition.<br>
	 * Implicit done with {@link #endElement()} or {@link #startContent()}.
	 */
	public void endStyle()
	{
		if (!inStyle)
			throw new IllegalStateException("endStyle called outside style definition.");
		if (!atStyleStart)
			write("'");
		inStyle = false;
	}

	/**
	 * Starts the SVG document. Must be the first call.
	 *
	 * @param width  The Width of the document.
	 * @param height The height of the document.
	 * @param title  The optional title. Can be null.
	 */
	public void startSVG(float width, float height, String title)
	{
		inSvg = true;
		write("<svg xmlns=\"http://www.w3.org/2000/svg\"" +
				" version=\"1.1\" width=\"");
		writeRestrictedFloat(width);
		write("\" height=\"");
		writeRestrictedFloat(height);
		write("\">");
		if (title != null)
		{
			startElement("title");
			startContent();
			writeEscaped(title);
			endElement();
		}
	}

	/**
	 * Starts the SVG document. Must be the first call.
	 *
	 * @param viewport The viewport coordinates of the document.
	 * @param title    The optional title. Can be null.
	 */
	public void startSVG(Rectangle2D viewport, String title)
	{
		inSvg = true;
		write("<svg xmlns=\"http://www.w3.org/2000/svg\"" +
				" version=\"1.1\" viewBox=\"");
		writeRestrictedFloat((float) viewport.getX());
		write(' ');
		writeRestrictedFloat((float) viewport.getY());
		write(' ');
		writeRestrictedFloat((float) viewport.getWidth());
		write(' ');
		writeRestrictedFloat((float) viewport.getHeight());
		write("\">");
		if (title != null)
		{
			startElement("title");
			startContent();
			writeEscaped(title);
			endElement();
		}
	}

	/**
	 * Starts content area.
	 */
	@Override
	public void startContent()
	{
		if (inStyle)
			endStyle();
		super.startContent();
	}

	/**
	 * Ends the current element.
	 */
	@Override
	public void endElement()
	{
		if (inStyle)
		{
			endStyle();
		}
		super.endElement();

	}

	/**
	 * Ends the SVG document.<br>
	 * Must be the last call. Implicitly ends all open elements.
	 */
	public void endSVG()
	{
		if (!inSvg)
			throw new IllegalStateException("endSVG called outside svg");
		while (!tagStack.empty())
		{
			endElement();
		}
		write("\n</svg>");
		inSvg = false;
	}

	/**
	 * Appends the attribute with the paint as value.
	 *
	 * @param attribute The name of the attribute (without '=').
	 * @param paint     The paint.
	 */
	public void writeAttribute(String attribute, Paint paint)
	{
		if (paint != null)
		{
			writeAttributeProlog();
			write(attribute);
			writeAssign();
			write(paint);
			writeAttributeEpilog();
		}
	}

	@Override
	protected void writeAttributeProlog()
	{
		if (inStyle)
		{
			if (atStyleStart)
			{
				write(" style='");
				atStyleStart = false;
			}
			else
				write(';');
		}
		else
			super.writeAttributeProlog();
	}

	@Override
	protected void writeAttributeEpilog()
	{
		if (!inStyle)
		{
			super.writeAttributeEpilog();
		}
	}

	@Override
	protected void writeAssign()
	{
		write(inStyle ? ":" : "='");
	}
}
