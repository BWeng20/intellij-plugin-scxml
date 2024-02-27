package com.bw.graph.primitive;

import com.bw.graph.DrawStyle;
import com.bw.graph.GraphConfiguration;
import com.bw.graph.util.Dimension2DFloat;
import com.bw.graph.visual.VisualFlags;
import com.bw.jtools.shape.AbstractShape;
import com.bw.jtools.shape.ShapePainter;
import com.bw.jtools.svg.SVGConverter;
import com.bw.jtools.svg.SVGException;
import com.bw.svg.SVGWriter;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;

/**
 * Draws an SVG Shape.<br>
 * <b>DRAFT and absolutely untested. Don't use yet!</b>
 */
public class Svg extends DrawPrimitive
{
	private ShapePainter _painter;
	private String _svgSource;
	private boolean _gray;

	/**
	 * Creates a new SVG Primitive.
	 *
	 * @param x      The relative x-position
	 * @param y      The relative y-position
	 * @param shape  SVG Shape
	 * @param config The configuration to use.
	 * @param style  The style or null if default style shall be used.
	 * @param flags  The initial flags. @see {@link VisualFlags}
	 */
	public Svg(float x, float y,
			   AbstractShape shape,
			   GraphConfiguration config,
			   DrawStyle style, int flags)
	{
		super(x, y, config, style, flags);
		this._painter = new ShapePainter(shape);
		this._gray = false;
	}

	/**
	 * Creates a new SVG Primitive.
	 *
	 * @param x      The relative x-position
	 * @param y      The relative y-position
	 * @param svg    SVG source.
	 * @param config The configuration to use.
	 * @param style  The style or null if default style shall be used.
	 * @throws SVGException In case the source has errors.
	 */
	public Svg(float x, float y, String svg, GraphConfiguration config,
			   DrawStyle style) throws SVGException
	{
		this(x, y, SVGConverter.convert(svg), config, style, VisualFlags.ALWAYS);
		this._svgSource = svg;
	}


	/**
	 * Sets gray mode.
	 *
	 * @param gray If true, the shape is painted in gray mode
	 */
	public void setGray(boolean gray)
	{
		this._gray = gray;
	}


	@Override
	protected void drawRelative(Graphics2D g2)
	{
		_painter.paint(g2, null, _style.getFillPaint(), false, _gray);
	}

	@Override
	protected Dimension2DFloat getInnerDimension(Graphics2D graphics)
	{
		return new Dimension2DFloat(_painter.getArea());
	}

	@Override
	protected void toSVGIntern(SVGWriter sw, Graphics2D g2, Point2D.Float pos)
	{
		if (_svgSource != null)
		{
			sw.write(_svgSource);
		}
	}
}