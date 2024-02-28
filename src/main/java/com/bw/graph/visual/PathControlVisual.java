package com.bw.graph.visual;

import com.bw.graph.DrawContext;
import com.bw.graph.primitive.DrawPrimitive;
import com.bw.graph.primitive.PathControlPoint;
import com.bw.graph.primitive.Rectangle;
import com.bw.svg.SVGWriter;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.List;

/**
 * Used to visualize connectors in different modes.
 */
public class PathControlVisual extends Visual implements PathControlPoint
{
	private DrawPrimitive _primitive;
	private float _radius = 3;

	/**
	 * Sets the edge.
	 *
	 * @param edgeVisual The connected edge.
	 */
	public void setEdgeVisual(SingleTargetEdgeVisual edgeVisual)
	{
		this._parent = edgeVisual;
	}

	/**
	 * Gets the edge.
	 *
	 * @return The connected edge or null.
	 */
	public SingleTargetEdgeVisual getEdgeVisual()
	{
		return (SingleTargetEdgeVisual) _parent;
	}


	/**
	 * Creates a new Primitive.
	 *
	 * @param context The draw context. Must not be null.
	 */
	public PathControlVisual(DrawContext context)
	{
		super(null, context);
		this._primitive = new Rectangle(-_radius, -_radius, 2 * _radius, 2 * _radius, 0, context._configuration, context._style, VisualFlags.ALWAYS);
	}

	/**
	 * Draws for given context.
	 *
	 * @param g2 The graphics context
	 */
	@Override
	protected void drawRelative(Graphics2D g2)
	{
		if (isFlagSet(VisualFlags.SELECTED))
		{
			_primitive.draw(g2);
		}
	}

	@Override
	public DrawPrimitive getEditablePrimitiveAt(float x, float y)
	{
		_primitive.setVisual(this);
		return _primitive;
	}

	@Override
	protected void updateBounds(Graphics2D graphics)
	{
		_absoluteBounds.setRect(_primitive.getBounds2D(_absolutePosition.x, _absolutePosition.y, graphics));
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
		_primitive = null;
	}

	@Override
	public void getControlPosition(Point2D.Float pt)
	{
		getAbsolutePosition(pt);
		pt.x += _radius;
		pt.y += _radius;
	}

	@Override
	public List<DrawPrimitive> getPrimitives()
	{
		return Collections.singletonList(_primitive);
	}
}

