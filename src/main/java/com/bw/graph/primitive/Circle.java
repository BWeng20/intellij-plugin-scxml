package com.bw.graph.primitive;

import com.bw.graph.DrawStyle;
import com.bw.graph.GraphConfiguration;
import com.bw.graph.util.Dimension2DFloat;
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
	private float _diameter;

	/**
	 * Creates a new Circle Primitive.
	 *
	 * @param cx     The relative center x-position
	 * @param cy     The relative center y-position
	 * @param radius Radius in pixel.
	 * @param config The configuration to use.
	 * @param style  The style or null if default style shall be used.
	 * @param flags  Bitwise combination of flags.
	 */
	public Circle(float cx, float cy, float radius,
				  GraphConfiguration config,
				  DrawStyle style, int flags)
	{
		super(cx - radius, cy - radius, config, style, flags);
		this._diameter = 2f * radius;
		this._shape = new Ellipse2D.Float(0, 0, _diameter, _diameter);
	}

	@Override
	protected Dimension2DFloat getInnerDimension(Graphics2D graphics)
	{
		return new Dimension2DFloat(_diameter, _diameter);
	}

	@Override
	protected void toSVGIntern(SVGWriter sw, Graphics2D g2, Point2D.Float pos)
	{
		sw.startElement(SVGElement.circle);
		sw.writeAttribute(SVGAttribute.Cx, pos.x + (_diameter / 2));
		sw.writeAttribute(SVGAttribute.Cy, pos.y + (_diameter / 2));
		sw.writeAttribute(SVGAttribute.R, (_diameter / 2));
		sw.startStyle();
		if (isFill())
			sw.writeAttribute(SVGAttribute.Fill, _style._fillPaint);
		sw.writeAttribute(SVGAttribute.Stroke, _style._linePaint);
		sw.writeStrokeWidth(_style.getStrokeWidth());
		sw.endElement();
	}
}