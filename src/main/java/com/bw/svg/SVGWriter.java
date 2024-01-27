package com.bw.svg;

import com.bw.XmlWriter;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Writer;

/**
 * Printer to build SVG.
 */
public class SVGWriter extends XmlWriter
{
	private boolean _inStyle = false;
	private boolean _atStyleStart = false;

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
		else if (paint == null)
		{
			write("none");
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
	 * @param strokeWidth The stroke.
	 */
	public void writeStrokeWidth(float strokeWidth)
	{
		if (strokeWidth > 0f)
		{
			writeAttribute(SVGAttribute.StrokeWidth, strokeWidth);
		}
	}

	/**
	 * Starts style definition inside xml element.
	 */
	public void startStyle()
	{
		if (_inStyle)
			throw new IllegalStateException("startStyle called inside style definition.");
		if (_tagStack.empty())
			throw new IllegalStateException("startStyle called outside element.");
		if (_elementHasContent)
			throw new IllegalStateException("startStyle called inside content.");
		_inStyle = true;
		_atStyleStart = true;
	}

	/**
	 * Ends the style definition.<br>
	 * Implicit done with {@link #endElement()} or {@link #startContent()}.
	 */
	public void endStyle()
	{
		if (!_inStyle)
			throw new IllegalStateException("endStyle called outside style definition.");
		if (!_atStyleStart)
			write("'");
		_inStyle = false;
	}

	/**
	 * Starts the SVG document. Must be the first call.
	 *
	 * @param width  The Width of the document.
	 * @param height The height of the document.
	 */
	public void startSVG(float width, float height)
	{
		startElement(SVGElement.svg);
		writeAttribute("xmlns", "http://www.w3.org/2000/svg");
		writeAttribute("version", "1.1");
		writeAttribute(SVGAttribute.Width, width);
		writeAttribute(SVGAttribute.Height, height);
	}

	/**
	 * Starts the SVG document. Must be the first call.
	 *
	 * @param viewport The viewport coordinates of the document.
	 */
	public void startSVG(Rectangle2D.Float viewport)
	{
		startElement(SVGElement.svg);
		writeAttribute("xmlns", "http://www.w3.org/2000/svg");
		writeAttribute("version", "1.1");
		writeAttribute(SVGAttribute.ViewBox, toBox(viewport));
	}

	/**
	 * Starts a new element.
	 *
	 * @param element A element.
	 */
	public void startElement(SVGElement element)
	{
		super.startElement(element.name());
	}

	/**
	 * Appends the attribute with the value.
	 *
	 * @param attribute The attribute.
	 * @param value     The value.
	 */
	public void writeAttribute(SVGAttribute attribute, String value)
	{
		super.writeAttribute(attribute.xmlName(), value);
	}

	/**
	 * Starts content area.
	 */
	@Override
	public void startContent()
	{
		if (_inStyle)
			endStyle();
		super.startContent();
	}

	/**
	 * Ends the current element.
	 */
	@Override
	public void endElement()
	{
		if (_inStyle)
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
		while (!_tagStack.empty())
		{
			endElement();
		}
	}

	/**
	 * Appends the attribute with the float as value.
	 *
	 * @param attribute The attribute.
	 * @param value     The value.
	 */
	public void writeAttribute(SVGAttribute attribute, float value)
	{
		writeAttribute(attribute.xmlName(), value);
	}

	/**
	 * Appends the attribute with the paint as value.
	 *
	 * @param attribute The name of the attribute (without '=').
	 * @param paint     The paint.
	 */
	public void writeAttribute(SVGAttribute attribute, Paint paint)
	{
		writeAttributeProlog();
		write(attribute.xmlName());
		writeAssign();
		write(paint);
		writeAttributeEpilog();
	}

	@Override
	protected void writeAttributeProlog()
	{
		if (_inStyle)
		{
			if (_atStyleStart)
			{
				write(" style='");
				_atStyleStart = false;
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
		if (!_inStyle)
		{
			super.writeAttributeEpilog();
		}
	}

	@Override
	protected void writeAssign()
	{
		write(_inStyle ? ":" : "='");
	}

	/**
	 * Writes a shapes as approximated path.
	 *
	 * @param shape       The shape.
	 * @param flatness    Flatness factor. See {@link Shape#getPathIterator(AffineTransform, double)}.
	 * @param fill        Fill paint or null for none.
	 * @param stroke      Stroke paint or null for none.
	 * @param strokeWidth Stroke width.
	 */
	public void writeShape(Shape shape, float flatness, Paint fill, Paint stroke, float strokeWidth)
	{
		startElement(SVGElement.path);
		writeAttribute(SVGAttribute.Fill, fill);
		writeAttribute(SVGAttribute.Stroke, stroke);
		if (stroke != null)
			writeAttribute(SVGAttribute.StrokeWidth, strokeWidth);

		PathIterator pi = shape.getPathIterator(null, flatness);
		double[] seg = new double[6];

		write(" d='");

		while (!pi.isDone())
		{
			final int type = pi.currentSegment(seg);
			switch (type)
			{
				case PathIterator.SEG_MOVETO:
					write('M');
					writePathCoordinates(seg[0], seg[1]);
					break;
				case PathIterator.SEG_LINETO:
					write(' ');
					writePathCoordinates(seg[0], seg[1]);
					break;
				case PathIterator.SEG_CLOSE:
					write('Z');
					break;
			}
			pi.next();
		}
		write('\'');
		endElement();
	}

	private void writePathCoordinates(double x, double y)
	{
		writeRestrictedFloat((float) x);
		write(' ');
		writeRestrictedFloat((float) y);
	}

	/**
	 * Create a box string from the rectangle.
	 *
	 * @param box The rectangle.
	 * @return The box string
	 */
	public String toBox(Rectangle2D.Float box)
	{
		return toBox(box, _precisionFactor);
	}

	/**
	 * Create a box string from the rectangle.
	 *
	 * @param box             The rectangle.
	 * @param precisionFactor The precision factor.
	 * @return The box string
	 * @see #floatToString(float, float)
	 */
	public static String toBox(Rectangle2D.Float box, float precisionFactor)
	{
		return floatToString(box.x, precisionFactor) + " " +
				floatToString(box.y, precisionFactor) + " " +
				floatToString(box.width, precisionFactor) + " " +
				floatToString(box.height, precisionFactor);
	}

	/**
	 * Create a point string from the point.
	 *
	 * @param pt              The point.
	 * @param precisionFactor The precision factor.
	 * @return The point string
	 * @see #floatToString(float, float)
	 */
	public static String toPoint(Point2D.Float pt, float precisionFactor)
	{
		return floatToString(pt.x, precisionFactor) + " " +
				floatToString(pt.y, precisionFactor);
	}
}
