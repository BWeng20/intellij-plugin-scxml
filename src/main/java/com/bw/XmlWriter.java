package com.bw;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Stack;

/**
 * Printer to build XML.
 */
public class XmlWriter extends PrintWriter
{
	/**
	 * Precision factor for float values.
	 */
	protected final float precisionFactor = 10 * 10 * 10;

	private StringBuilder lineIndent = new StringBuilder(20);

	/**
	 * The stack of currently open tags.
	 */
	protected final Stack<String> tagStack = new Stack<>();

	/**
	 * True if the current element has some content.
	 */
	protected boolean elementHasContent = false;

	/**
	 * Creates an XML Writer on top of some other writer.
	 *
	 * @param out The output writer
	 */
	public XmlWriter(Writer out)
	{
		super(out);
		lineIndent.append("\n ");
	}

	/**
	 * Starts a new element.
	 *
	 * @param tag A element tag name.
	 */
	public void startElement(String tag)
	{
		if ((!tagStack.empty()) && !elementHasContent)
			write(">");
		tagStack.push(tag);
		write(lineIndent.toString());
		write('<');
		write(tag);
		lineIndent.append('\t');
		elementHasContent = false;
	}

	/**
	 * Starts content area.
	 */
	public void startContent()
	{
		if (!elementHasContent)
		{
			write('>');
			write(lineIndent.toString());
		}
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
		lineIndent.setLength(lineIndent.length() - 1);
		if (elementHasContent)
		{
			write(lineIndent.toString());
			write("</");
			write(element);
			write('>');
		}
		else
		{
			write("/>");
		}
		elementHasContent = true;
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
		write(floatToStringRestrictedPrecision(value, precisionFactor));
	}

	/**
	 * Gets a string representation with restricted number of digits.
	 *
	 * @param value           The value to convert.
	 * @param precisionFactor The precision factor. E.g. 1000 for 3 digits
	 * @return The string representation.
	 */
	public static String floatToStringRestrictedPrecision(float value, float precisionFactor)
	{
		value = (float) Math.floor(0.5f + (value * precisionFactor)) / precisionFactor;
		return (value == Math.ceil(value)) ? Long.toString((long) value) : Float.toString(value);
	}

	/**
	 * Writes the prolog of an attribute.
	 */
	protected void writeAttributeProlog()
	{
		write(' ');
	}

	/**
	 * Writes the end of an attribute.
	 */
	protected void writeAttributeEpilog()
	{
		write("'");
	}

	/**
	 * Writes an attribute assignment "=".<br>
	 * Can be overwritten to apply a mapping to e.g. style attributes.
	 */
	protected void writeAssign()
	{
		write("='");
	}
}
