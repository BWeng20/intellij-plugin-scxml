package com.bw.graph.visual;

import com.bw.graph.DrawContext;
import com.bw.graph.DrawStyle;
import com.bw.graph.primitive.DrawPrimitive;
import com.bw.graph.primitive.PathControlPoint;
import com.bw.graph.primitive.Rectangle;
import com.bw.svg.SVGWriter;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Used to visualize connectors in different modes.
 */
public class PathControlVisual extends Visual implements PathControlPoint
{
	private DrawPrimitive primitive;
	private float radius = 3;

	/**
	 * Creates a new Primitive.
	 *
	 * @param context The draw context. Must not be null.
	 */
	public PathControlVisual(DrawContext context)
	{
		super(null, context);
		this.primitive = new Rectangle(-radius, -radius, 2 * radius, 2 * radius, 0, context.configuration, context.normal);
	}

	/**
	 * Draws for given context.
	 *
	 * @param g2          The graphics context
	 * @param parentStyle The style of parent, used if primitive has no own style.
	 */
	@Override
	protected void drawIntern(Graphics2D g2, DrawStyle parentStyle)
	{
		if (isHighlighted())
		{
			final DrawStyle actualStyle = context == null ? parentStyle : context.highlighted;
			primitive.draw(g2, position, actualStyle);
		}
	}
	@Override
	public DrawPrimitive getEditablePrimitiveAt(float x, float y) {
		primitive.setVisual(this);
		return primitive;
	}

	@Override
	protected void updateBounds(Graphics2D graphics)
	{
		Rectangle2D.Float primitiveBounds = primitive.getBounds2D(position, graphics, context.highlighted);
		x2 = primitiveBounds.x + primitiveBounds.width;
		y2 = primitiveBounds.y + primitiveBounds.height;
	}

	@Override
	public void toSVG(SVGWriter sw, Graphics2D g2)
	{
	}

	@Override
	public void dispose()
	{
		super.dispose();
		primitive = null;
	}

	@Override
	public void getControlPosition(Point2D.Float pt)
	{
		getPosition(pt);
		pt.x += radius;
		pt.y += radius;
	}
}