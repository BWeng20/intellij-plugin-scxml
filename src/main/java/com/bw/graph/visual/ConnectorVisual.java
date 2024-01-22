package com.bw.graph.visual;

import com.bw.graph.DrawContext;
import com.bw.graph.primitive.Circle;
import com.bw.graph.primitive.DrawPrimitive;
import com.bw.graph.primitive.PathControlPoint;
import com.bw.graph.util.Dimension2DFloat;
import com.bw.svg.SVGWriter;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;

/**
 * Used to visualize connectors in different modes.
 */
public class ConnectorVisual extends Visual implements PathControlPoint
{

	private DrawPrimitive primitive;
	private float radius;
	private EdgeVisual edgeVisual;

	private Point2D.Float relativePosition = new Point2D.Float(0, 0);

	private Visual parent;

	/**
	 * Creates a new Primitive.
	 *
	 * @param parent  The connected visual (not the edge itself!).
	 * @param context The draw context. Must not be null.
	 */
	public ConnectorVisual(Visual parent, DrawContext context)
	{
		super(null, context);
		this.parent = parent;
		this.radius = context.configuration.connectorSize;
		this.primitive = new Circle(radius, radius, radius, context.configuration, context.normal);
	}

	/**
	 * Sets the edge.
	 *
	 * @param edgeVisual The connected edge.
	 */
	public void setEdgeVisual(EdgeVisual edgeVisual)
	{
		this.edgeVisual = edgeVisual;
	}


	/**
	 * Connectors don't use relative positions or insets.
	 *
	 * @param g2 The graphics context
	 */
	@Override
	public void draw(Graphics2D g2)
	{
		if ((parent != null && parent.isHighlighted()) || (edgeVisual != null && edgeVisual.isHighlighted()))
		{
			Point2D.Float pt = parent.getAbsolutePosition();

			pt.x += relativePosition.x;
			pt.y += relativePosition.y;

			absoluteBounds.x = pt.x;
			absoluteBounds.y = pt.y;

			g2.translate(pt.x, pt.y);
			try
			{
				primitive.draw(g2);
			}
			finally
			{
				g2.translate(-pt.x, -pt.y);
			}
		}
	}

	/**
	 * Draws for given context.
	 *
	 * @param g2 The graphics context
	 */
	@Override
	protected void drawRelative(Graphics2D g2)
	{
	}

	@Override
	public DrawPrimitive getEditablePrimitiveAt(float x, float y)
	{
		Point2D.Float pt = new Point2D.Float();
		getAbsolutePosition(pt);
		if (primitive.getBounds2D(pt, null)
					 .contains(x, y))
		{
			primitive.setVisual(this);
			return primitive;
		}
		else
			return null;
	}


	@Override
	protected void updateBounds(Graphics2D graphics)
	{
		Dimension2DFloat dim = primitive.getDimension(graphics);
		absoluteBounds.width = dim.width;
		absoluteBounds.height = dim.height;
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
		parent.getAbsolutePosition(pt);
		pt.x += relativePosition.x + radius;
		pt.y += relativePosition.y + radius;
	}

	public <T extends DrawPrimitive> T getPrimitiveOf(Class<T> primitiveClass)
	{
		if (primitiveClass.isAssignableFrom(primitive.getClass()))
			return (T) primitive;
		return
				null;
	}

	public void setRelativePosition(float x, float y)
	{
		relativePosition.x = x;
		relativePosition.y = y;
	}
}

