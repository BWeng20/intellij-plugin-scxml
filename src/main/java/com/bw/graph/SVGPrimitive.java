package com.bw.graph;

import com.bw.jtools.shape.AbstractShape;
import com.bw.jtools.shape.ShapePainter;
import com.bw.jtools.svg.SVGConverter;
import com.bw.jtools.svg.SVGException;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;

/**
 * Draws a SVG Shape.
 */
public class SVGPrimitive extends DrawPrimitive
{
	private ShapePainter painter;
	private String svgSource;
	private boolean gray;

	/**
	 * Creates a new SVG Primitive.
	 *
	 * @param x        The relative x-position
	 * @param y        The relative y-position
	 * @param style    The style or null if default style shall be used.
	 * @param scalable True is user can scale this primitive independent of parent.
	 * @param shape    SVG Shape
	 */
	public SVGPrimitive(float x, float y,
						DrawStyle style,
						boolean scalable, AbstractShape shape)
	{
		super(x, y, style, scalable);
		this.painter = new ShapePainter(shape);
		this.gray = false;
	}

	/**
	 * Creates a new SVG Primitive.
	 *
	 * @param x        The relative x-position
	 * @param y        The relative y-position
	 * @param style    The style or null if default style shall be used.
	 * @param scalable True is user can scale this primitive independent of parent.
	 * @param svg      SVG source.
	 */
	public SVGPrimitive(float x, float y,
						DrawStyle style,
						boolean scalable, String svg) throws SVGException
	{
		this(x, y, style, scalable, SVGConverter.convert(svg));
		this.svgSource = svg;
	}


	/**
	 * Sets gray mode.
	 *
	 * @param gray If true, the shape is painted in gray mode
	 */
	public void setGray(boolean gray)
	{
		this.gray = gray;
	}


	@Override
	protected void drawIntern(Graphics2D g2, DrawStyle style, Point2D.Float pos)
	{
		painter.paint(g2, null, style.fillPaint, false, gray);
	}

	@Override
	protected Dimension2DFloat getDimension(Graphics2D graphics, DrawStyle style)
	{
		return new Dimension2DFloat(painter.getArea());
	}

	@Override
	protected void toSVGIntern(StringBuilder sb, DrawStyle style, Point2D.Float pos)
	{
		if (svgSource != null)
			sb.append(svgSource);
	}

}