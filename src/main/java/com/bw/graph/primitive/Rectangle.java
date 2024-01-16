package com.bw.graph.primitive;

import com.bw.graph.DrawStyle;
import com.bw.graph.GraphConfiguration;
import com.bw.svg.SVGWriter;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

/**
 * A Rectangle.
 */
public class Rectangle extends ShapeBase
{
	/**
	 * Creates a new Rectangle Primitive.
	 *
	 * @param x       The relative x-position
	 * @param y       The relative y-position
	 * @param width   Width in pixel.
	 * @param height  Height in pixel
	 * @param arcSize Size of arc to use to round off the corners.
	 * @param config  The configuration to use.
	 * @param style   The style or null if default style shall be used.
	 */
	public Rectangle(float x, float y, float width, float height, float arcSize,
					 GraphConfiguration config,
					 DrawStyle style)
	{
		super(x, y, config, style);

		if (arcSize == 0)
			shape = new Rectangle2D.Float(0, 0, width, height);
		else
			shape = new RoundRectangle2D.Float(0, 0, width, height, arcSize, arcSize);
	}

	@Override
	protected void toSVGIntern(SVGWriter sw, DrawStyle style, Point2D.Float pos)
	{
		sw.startElement("rect");
		sw.writeAttribute("x", pos.x);
		sw.writeAttribute("y", pos.y);
		sw.writeAttribute("width", ((Rectangle2D.Float) shape).width);
		sw.writeAttribute("height", ((Rectangle2D.Float) shape).height);
		sw.startStyle();
		if (isFill())
			sw.writeAttribute("fill", style.fillPaint);
		sw.writeAttribute("stroke", style.linePaint);
		sw.writeStrokeWith(style.getStrokeWidth());
		sw.endElement();
	}

}