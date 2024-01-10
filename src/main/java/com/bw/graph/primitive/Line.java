package com.bw.graph.primitive;

import com.bw.graph.Dimension2DFloat;
import com.bw.graph.DrawPrimitive;
import com.bw.graph.DrawStyle;
import com.bw.graph.GraphConfiguration;
import com.bw.svg.SVGWriter;

import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

/**
 * A Line.
 */
public class Line extends DrawPrimitive
{

	private Line2D.Float shape;

	/**
	 * Creates a new Line Primitive.
	 *
	 * @param x1     The relative x1-position
	 * @param y1     The relative y1-position
	 * @param x2     The relative x2-position
	 * @param y2     The relative y2-position
	 * @param config The configuration to use.
	 * @param style  The style or null if default style shall be used.
	 */
	public Line(float x1, float y1, float x2, float y2,
				GraphConfiguration config,
				DrawStyle style)
	{
		super(x1, y1, config, style, false);
		shape = new Line2D.Float(0, 0, x2 - x1, y2 - y1);
	}


	@Override
	protected void drawIntern(Graphics2D g2, DrawStyle style)
	{
		g2.setPaint(style.linePaint);
		g2.setStroke(style.lineStroke);
		g2.draw(shape);
	}

	@Override
	protected Dimension2DFloat getInnerDimension(Graphics2D graphics, DrawStyle style)
	{
		return new Dimension2DFloat(shape.x2, shape.y2);
	}

	@Override
	protected void toSVGIntern(SVGWriter sw, DrawStyle style, Point2D.Float pos)
	{
		sw.startElement("line");
		sw.writeAttribute("x1", pos.x);
		sw.writeAttribute("y1", pos.y);
		sw.writeAttribute("x2", shape.x2 + pos.x);
		sw.writeAttribute("y2", shape.y2 + pos.y);
		sw.writeAttribute("stroke", style.linePaint);
		sw.writeStrokeWith(style.getStrokeWidth());
		sw.endElement();
	}

}