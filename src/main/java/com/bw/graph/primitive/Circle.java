package com.bw.graph.primitive;

import com.bw.graph.DrawStyle;
import com.bw.graph.GraphConfiguration;
import com.bw.svg.SVGWriter;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

/**
 * A Circle.
 */
public class Circle extends ShapeBase
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
	protected void toSVGIntern(SVGWriter sw, DrawStyle style, Point2D.Float pos)
	{
		sw.startElement("circle");
		sw.writeAttribute("cx", pos.x + (diameter / 2));
		sw.writeAttribute("cy", pos.y + (diameter / 2));
		sw.writeAttribute("r", (diameter / 2));
		sw.startStyle();
		if (fill)
			sw.writeAttribute("fill", style.fillPaint);
		sw.writeAttribute("stroke", style.linePaint);
		sw.writeStrokeWith(style.getStrokeWidth());
		sw.endElement();
	}
}