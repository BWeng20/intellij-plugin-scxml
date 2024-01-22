package com.bw.graph.visual;

import com.bw.graph.DrawContext;
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
	 * @param g2 The graphics context
	 */
	@Override
	protected void drawRelative(Graphics2D g2)
	{
		if (isHighlighted())
		{
			primitive.draw(g2);
		}
	}

	@Override
	public DrawPrimitive getEditablePrimitiveAt(float x, float y)
	{
		primitive.setVisual(this);
		return primitive;
	}

	@Override
	protected void updateBounds(Graphics2D graphics)
	{
		Rectangle2D.Float primitiveBounds = primitive.getBounds2D(absoluteBounds.x, absoluteBounds.y, graphics);
		absoluteBounds.width = primitiveBounds.x + primitiveBounds.width;
		absoluteBounds.height = primitiveBounds.y + primitiveBounds.height;
	}

	@Override
	public void toSVG(SVGWriter sw, Graphics2D g2)
	{
		// Nothing to do
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
		getAbsolutePosition(pt);
		pt.x += radius;
		pt.y += radius;
	}

	public <T extends DrawPrimitive> T getPrimitiveOf(Class<T> primitiveClass)
	{
		if (primitiveClass.isAssignableFrom(primitive.getClass()))
			return (T) primitive;
		return
				null;

	}
}