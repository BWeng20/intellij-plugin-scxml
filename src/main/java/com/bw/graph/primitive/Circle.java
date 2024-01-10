package com.bw.graph.primitive;

import com.bw.graph.Dimension2DFloat;
import com.bw.graph.DrawPrimitive;
import com.bw.graph.DrawStyle;
import com.bw.graph.GraphConfiguration;
import com.bw.svg.SVGWriter;

import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

/**
 * A Circle.
 */
public class Circle extends DrawPrimitive
{
	private float diameter;

	private Ellipse2D.Float shape;

	private boolean fill;

	/**
	 * Creates a new Circle Primitive.
	 *
	 * @param cx       The relative center x-position
	 * @param cy       The relative center y-position
	 * @param config   The configuration to use.
	 * @param style    The style or null if default style shall be used.
	 * @param scalable True is user can scale this primitive independent of parent.
	 * @param radius   Radius in pixel.
	 */
	public Circle(float cx, float cy,
				  GraphConfiguration config,
				  DrawStyle style,
				  boolean scalable, float radius)
	{
		super(cx - radius, cy - radius, config, style, scalable);
		this.diameter = 2f * radius;
		this.fill = false;

		this.shape = new Ellipse2D.Float(0, 0, diameter, diameter);
	}

	/**
	 * Sets filled.
	 *
	 * @param fill If true, the circle is filled
	 */
	public void setFill(boolean fill)
	{
		this.fill = fill;
	}


	@Override
	protected void drawIntern(Graphics2D g2, DrawStyle style)
	{
		if (fill)
		{
			g2.setPaint(style.fillPaint);
			g2.fill(shape);
		}
		g2.setStroke(style.lineStroke);
		g2.setPaint(style.linePaint);
		g2.draw(shape);
	}

	@Override
	protected Dimension2DFloat getInnerDimension(Graphics2D graphics, DrawStyle style)
	{
		return new Dimension2DFloat(shape.width, shape.height);
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