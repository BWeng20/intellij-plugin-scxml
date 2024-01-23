package com.bw.graph.primitive;

import com.bw.graph.DrawStyle;
import com.bw.graph.GraphConfiguration;
import com.bw.svg.SVGAttribute;
import com.bw.svg.SVGElement;
import com.bw.svg.SVGWriter;

import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

/**
 * A Circle.
 */
public class Circle extends ShapePrimitiveBase
{
	private float diameter;

	/**
	 * Creates a new Circle Primitive.
	 *
	 * @param cx     The relative center x-position
	 * @param cy     The relative center y-position
	 * @param radius Radius in pixel.
	 * @param config The configuration to use.
	 * @param style  The style or null if default style shall be used.
	 */
	public Circle(float cx, float cy, float radius,
				  GraphConfiguration config,
				  DrawStyle style)
	{
		super(cx - radius, cy - radius, config, style);
		this.diameter = 2f * radius;
		this.shape = new Ellipse2D.Float(0, 0, diameter, diameter);
	}

	@Override
	protected void toSVGIntern(SVGWriter sw, Graphics2D g2, Point2D.Float pos)
	{
		sw.startElement(SVGElement.circle);
		sw.writeAttribute(SVGAttribute.Cx, pos.x + (diameter / 2));
		sw.writeAttribute(SVGAttribute.Cy, pos.y + (diameter / 2));
		sw.writeAttribute(SVGAttribute.R, (diameter / 2));
		sw.startStyle();
		if (isFill())
			sw.writeAttribute(SVGAttribute.Fill, style.fillPaint);
		sw.writeAttribute(SVGAttribute.Stroke, style.linePaint);
		sw.writeStrokeWidth(style.getStrokeWidth());
		sw.endElement();
	}
}