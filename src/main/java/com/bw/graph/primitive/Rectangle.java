package com.bw.graph.primitive;

import com.bw.graph.DrawStyle;
import com.bw.graph.GraphConfiguration;
import com.bw.svg.SVGAttribute;
import com.bw.svg.SVGElement;
import com.bw.svg.SVGWriter;

import java.awt.Graphics2D;
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
	protected void toSVGIntern(SVGWriter sw, Graphics2D g2, Point2D.Float pos)
	{
		sw.startElement(SVGElement.rect);
		sw.writeAttribute(SVGAttribute.X, pos.x);
		sw.writeAttribute(SVGAttribute.Y, pos.y);
		sw.writeAttribute(SVGAttribute.Width, (float) shape.getBounds2D()
														   .getWidth());
		sw.writeAttribute(SVGAttribute.Height, (float) shape.getBounds2D()
															.getHeight());
		if (shape instanceof RoundRectangle2D.Float)
			sw.writeAttribute(SVGAttribute.Rx, ((RoundRectangle2D.Float) shape).arcwidth / 2f);

		sw.startStyle();
		if (isFill())
			sw.writeAttribute(SVGAttribute.Fill, style.fillPaint);
		sw.writeAttribute(SVGAttribute.Stroke, style.linePaint);
		sw.writeStrokeWidth(style.getStrokeWidth());
		sw.endElement();
	}

}