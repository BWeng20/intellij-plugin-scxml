package com.bw.svg;

import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Stack;

/**
 * Printer to build SVG.
 */
public class SVGWriter extends PrintWriter
{
	private final float precisionFactor = 10 * 10 * 10;

	private final Stack<String> tagStack = new Stack<>();

	private boolean inSvg = false;

	private boolean inStyle = false;
	private boolean atStyleStart = false;

	private boolean elementHasContent = false;

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
	 * Starts a new element.
	 *
	 * @param tag A SVG element tag name.
	 */
	public void startElement(String tag)
	{
		if (!(tagStack.empty() || elementHasContent))
			write(">");
		tagStack.push(tag);
		write("\n<");
		write(tag);
		elementHasContent = false;
	}

	/**
	 * Starts content area.
	 */
	public void startContent()
	{
		if (inStyle)
			endStyle();
		if (!elementHasContent)
			write('>');
		elementHasContent = true;
	}

	/**
	 * Ends the current element.
	 */
	public void endElement()
	{
		if (tagStack.empty())
		{
			throw new IllegalStateException("endElement called on empty element-stack.");
		}
		final String element = tagStack.pop();
		if (inStyle)
		{
			endStyle();
		}
		if (elementHasContent)
		{
			write("</");
			write(element);
			write('>');
		}
		else
		{
			write("/>");
		}

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

	/**
	 * Appends the attribute with the value.
	 *
	 * @param attribute The name of the attribute (without '=').
	 * @param value     The value.
	 */
	public void writeAttribute(String attribute, String value)
	{
		if (value != null)
		{
			writeAttributeProlog();
			write(attribute);
			writeAssign();
			writeEscapedAttributeValue(value);
			writeAttributeEpilog();
		}
	}


	/**
	 * Appends the attribute with float value.
	 *
	 * @param attribute The name of the attribute (without ':').
	 * @param value     The value.
	 */
	public void writeAttribute(String attribute, float value)
	{
		writeAttributeProlog();
		write(attribute);
		writeAssign();
		writeRestrictedFloat(value);
		writeAttributeEpilog();
	}

	/**
	 * Write XML escaped Text. Escapes &lt;, &gt; and &amp;.
	 * For attribute-values use {@link #writeEscapedAttributeValue(CharSequence)}
	 *
	 * @param text The unescaped text.
	 */
	public void writeEscaped(CharSequence text)
	{
		final int N = text.length();
		for (int i = 0; i < N; ++i)
		{
			final char c = text.charAt(i);
			switch (c)
			{
				case '<' -> write("&lt;");
				case '>' -> write("&gt;");
				case '&' -> write("&amp;");
				default -> write(c);
			}
		}
	}

	/**
	 * Writes an escaped attribute value. Only ' will be escaped as all attributes are enclosed with it.
	 * For content text use {@link #writeEscaped(CharSequence)}.
	 *
	 * @param text The unescaped attribute text.
	 */
	public void writeEscapedAttributeValue(CharSequence text)
	{
		final int N = text.length();
		for (int i = 0; i < N; ++i)
		{
			final char c = text.charAt(i);
			if (c == '\'')
				write("&apos;");
			else
				write(c);
		}
	}

	/**
	 * Appends a float with restricted precision to the SVG buffer.
	 *
	 * @param value The value.
	 */
	public void writeRestrictedFloat(float value)
	{
		value = (float) Math.floor(0.5f + (value * precisionFactor)) / precisionFactor;
		write((value == Math.ceil(value))
				? Long.toString((long) value) : Float.toString(value));
	}

	private void writeAttributeProlog()
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
			write(' ');
	}

	private void writeAttributeEpilog()
	{
		if (!inStyle)
		{
			write("'");
		}
	}

	private void writeAssign()
	{
		write(inStyle ? ":" : "='");
	}
}
