package com.bw.graph.primitive;

import com.bw.graph.DrawStyle;
import com.bw.graph.GraphConfiguration;
import com.bw.graph.util.Dimension2DFloat;
import com.bw.graph.visual.VisualFlags;
import com.bw.svg.SVGAttribute;
import com.bw.svg.SVGElement;
import com.bw.svg.SVGWriter;

import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

/**
 * A Line.
 */
public class Line extends DrawPrimitive
{

	private Line2D.Float _shape;

	/**
	 * Creates a new Line Primitive.
	 *
	 * @param x1     The relative x1-position
	 * @param y1     The relative y1-position
	 * @param x2     The relative x2-position
	 * @param y2     The relative y2-position
	 * @param config The configuration to use.
	 * @param style  The style or null if default style shall be used.
	 * @param flags  The initial flags. @see {@link VisualFlags}
	 */
	public Line(float x1, float y1, float x2, float y2,
				GraphConfiguration config,
				DrawStyle style, int flags)
	{
		super(x1, y1, config, style, flags);
		_shape = new Line2D.Float(0, 0, x2 - x1, y2 - y1);
	}


	@Override
	protected void drawIntern(Graphics2D g2)
	{
		g2.setPaint(_style._linePaint);
		g2.setStroke(_style._lineStroke);
		g2.draw(_shape);
	}

	@Override
	protected Dimension2DFloat getInnerDimension(Graphics2D graphics)
	{
		return new Dimension2DFloat(_shape.x2, _shape.y2);
	}

	@Override
	protected void toSVGIntern(SVGWriter sw, Graphics2D g2, Point2D.Float pos)
	{
		sw.startElement(SVGElement.line);
		sw.writeAttribute(SVGAttribute.X1, pos.x);
		sw.writeAttribute(SVGAttribute.Y1, pos.y);
		sw.writeAttribute(SVGAttribute.X2, _shape.x2 + pos.x);
		sw.writeAttribute(SVGAttribute.Y2, _shape.y2 + pos.y);
		sw.writeAttribute(SVGAttribute.Stroke, _style._linePaint);
		sw.writeStrokeWidth(_style.getStrokeWidth());
		sw.endElement();
	}

}