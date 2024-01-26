package com.bw.graph.visual;

import com.bw.graph.DrawContext;
import com.bw.graph.primitive.Circle;
import com.bw.graph.primitive.DrawPrimitive;
import com.bw.graph.primitive.PathControlPoint;
import com.bw.graph.util.Dimension2DFloat;
import com.bw.svg.SVGWriter;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.List;

/**
 * Used to visualize connectors in different modes.
 */
public class ConnectorVisual extends Visual implements PathControlPoint
{

	private DrawPrimitive _primitive;
	private float _radius;
	private EdgeVisual _edgeVisual;

	private Point2D.Float _relativePosition = new Point2D.Float(0, 0);

	private Visual _parent;

	/**
	 * Creates a new Primitive.
	 *
	 * @param parent  The connected visual (not the edge itself!).
	 * @param context The draw context. Must not be null.
	 * @param flags   The initial flags. @see {@link VisualFlags}
	 */
	public ConnectorVisual(Visual parent, DrawContext context, int flags)
	{
		super(null, context);
		this._parent = parent;
		this._radius = context._configuration.connectorSize;
		this._primitive = new Circle(_radius, _radius, _radius, context._configuration, context._style, flags);
	}

	/**
	 * Sets the edge.
	 *
	 * @param edgeVisual The connected edge.
	 */
	public void setEdgeVisual(EdgeVisual edgeVisual)
	{
		this._edgeVisual = edgeVisual;
	}

	/**
	 * Get parent state.
	 *
	 * @return The parent state or null.
	 */
	public Visual getParent()
	{
		return _parent;
	}

	/**
	 * Connectors don't use relative positions or insets.
	 *
	 * @param g2 The graphics context
	 */
	@Override
	public void draw(Graphics2D g2)
	{
		if ((_parent != null && _parent.isFlagSet(VisualFlags.SELECTED)) ||
				(_edgeVisual != null && _edgeVisual.isFlagSet(VisualFlags.SELECTED)))
		{
			Point2D.Float pt = _parent.getAbsolutePosition();

			pt.x += _relativePosition.x;
			pt.y += _relativePosition.y;

			_absoluteBounds.x = pt.x;
			_absoluteBounds.y = pt.y;

			g2.translate(pt.x, pt.y);
			try
			{
				_primitive.draw(g2);
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
		if (_primitive.getBounds2D(pt, null)
					  .contains(x, y))
		{
			_primitive.setVisual(this);
			return _primitive;
		}
		else
			return null;
	}


	@Override
	protected void updateBounds(Graphics2D graphics)
	{
		Dimension2DFloat dim = _primitive.getDimension(graphics);
		_absoluteBounds.x = _absolutePosition.x;
		_absoluteBounds.y = _absolutePosition.y;
		_absoluteBounds.width = dim.width;
		_absoluteBounds.height = dim.height;
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
		_parent.getAbsolutePosition(pt);
		pt.x += _relativePosition.x + _radius;
		pt.y += _relativePosition.y + _radius;
	}

	@Override
	public List<DrawPrimitive> getPrimitives()
	{
		return Collections.singletonList(_primitive);
	}

	/**
	 * Sets the position of the connector, relative to its parent.
	 *
	 * @param x The X offset.
	 * @param y The Y offset.
	 */
	public void setRelativePosition(float x, float y)
	{
		_relativePosition.x = x;
		_relativePosition.y = y;
		resetBounds();
	}
}

