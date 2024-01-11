package com.bw.graph.visual;

import com.bw.graph.DrawContext;
import com.bw.graph.DrawStyle;
import com.bw.graph.primitive.Circle;
import com.bw.graph.primitive.DrawPrimitive;
import com.bw.graph.primitive.PathControlPoint;
import com.bw.graph.util.Dimension2DFloat;
import com.bw.svg.SVGWriter;

import java.awt.Graphics2D;

/**
 * Used to visualize connectors in different modes.
 */
public class ConnectorVisual extends Visual implements PathControlPoint
{

	private DrawPrimitive primitive;

	/**
	 * Creates a new Primitive.
	 *
	 * @param parent  The connected visual (not the edge itself!)
	 * @param context The draw context. Must not be null.
	 */
	public ConnectorVisual(Visual parent, DrawContext context)
	{
		super(context);
		this.parent = parent;
		this.primitive = new Circle(5, 5, 5, context.configuration, null);
	}

	/**
	 * Draws for given context.
	 *
	 * @param g2          The graphics context
	 * @param parentStyle The style of Edge, used if primitive has no own style.
	 */
	public void draw(Graphics2D g2, DrawStyle parentStyle)
	{
		if (parent != null && parent.isHighlighted())
		{
			DrawStyle style = getStyle();
			final DrawStyle actualStyle = style == null ? parentStyle : style;
			primitive.draw(g2, getPosition(), actualStyle);
		}
	}

	@Override
	protected void updateBounds(Graphics2D graphics)
	{
		Dimension2DFloat dim = primitive.getDimension(graphics, getStyle());
		x2 = position.x + dim.width;
		y2 = position.y + dim.height;
	}

	@Override
	public void toSVG(SVGWriter sw, Graphics2D g2)
	{
	}
}