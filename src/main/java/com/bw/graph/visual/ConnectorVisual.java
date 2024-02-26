package com.bw.graph.visual;

import com.bw.graph.DrawContext;
import com.bw.graph.editor.action.EditAction;
import com.bw.graph.editor.action.MoveAction;
import com.bw.graph.primitive.Circle;
import com.bw.graph.primitive.DrawPrimitive;
import com.bw.graph.primitive.PathControlPoint;
import com.bw.graph.util.Geometry;
import com.bw.svg.SVGWriter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Used to visualize connectors in different modes.<br>
 * Draws a circle at base position.
 */
public class ConnectorVisual extends Visual implements PathControlPoint
{
	/**
	 * All primitives of this connector.<br>
	 * Additional primitives may be added by inheritances.
	 */
	protected List<DrawPrimitive> _primitives = new ArrayList<>();

	/**
	 * The radius of the connector circle.
	 */
	protected float _radius;

	private SingleTargetEdgeVisual _edgeVisual;

	/**
	 * Relative position to parent visual.
	 */
	protected Point2D.Float _relativePosition;

	private Point2D.Float _dragPosition = new Point2D.Float(0, 0);

	private Point2D.Float _startDragRelativePosition = new Point2D.Float(0, 0);

	private Visual _parent;

	private Visual _targetedParentChild;

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
		this._radius = context._configuration._connectorSize;
		this._primitives.add(new Circle(0, 0, _radius, context._configuration, context._style, flags));
	}

	/**
	 * Sets the edge.
	 *
	 * @param edgeVisual The connected edge.
	 */
	public void setEdgeVisual(SingleTargetEdgeVisual edgeVisual)
	{
		this._edgeVisual = edgeVisual;
	}

	/**
	 * Gets the edge.
	 *
	 * @return The connected edge or null.
	 */
	public SingleTargetEdgeVisual getEdgeVisual()
	{
		return _edgeVisual;
	}


	/**
	 * Sets the inner child of the target visual that is the real target.
	 *
	 * @param parentChild The targeted visual or null
	 */
	public void setTargetedParentChild(Visual parentChild)
	{
		_targetedParentChild = parentChild;
	}

	/**
	 * Gets the inner child of the target visual that is the real target.
	 *
	 * @return The targeted visual or null.
	 */
	public Visual getTargetedParentChild()
	{
		return _targetedParentChild;
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
		if (isFlagSet(VisualFlags.SELECTED) ||
				(_parent != null && _parent.isFlagSet(VisualFlags.SELECTED)) ||
				(_edgeVisual != null && _edgeVisual.isFlagSet(VisualFlags.SELECTED)))
		{

			g2.setPaint(Color.RED);
			g2.drawRect((int) _dragPosition.x - 5, (int) _dragPosition.y - 5, 10, 10);

			Point2D.Float pt = _parent.getAbsolutePosition();

			if (_relativePosition != null)
			{
				pt.x += _relativePosition.x;
				pt.y += _relativePosition.y;
			}

			_absolutePosition.x = pt.x;
			_absolutePosition.y = pt.y;
			_absoluteBounds.x = pt.x - _radius;
			_absoluteBounds.y = pt.y - _radius;

			g2.translate(pt.x, pt.y);
			try
			{
				for (DrawPrimitive primitive : _primitives)
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
		for (DrawPrimitive primitive : _primitives)
		{
			if (primitive.getBounds2D(pt, null)
						 .contains(x, y))
			{
				primitive.setVisual(this);
				return primitive;
			}
		}
		return null;
	}

	@Override
	public void startDrag(float x, float y)
	{
		super.startDrag(x, y);
		_dragPosition.x = x;
		_dragPosition.y = y;
		if (_relativePosition == null)
		{
			_relativePosition = new Point2D.Float(0, 0);
		}
		_startDragRelativePosition.x = _relativePosition.x;
		_startDragRelativePosition.y = _relativePosition.y;
	}

	@Override
	public EditAction endDrag()
	{
		if (_startDragRelativePosition.x != _relativePosition.x ||
				_startDragRelativePosition.y != _relativePosition.y)
		{

			return new MoveAction(this, _startDragRelativePosition, _relativePosition, true);
		}

		return null;
	}

	/**
	 * Moves the visual by some delta.
	 *
	 * @param x The X-Delta to move.
	 * @param y The Y-Delta to move.
	 */
	@Override
	public void dragBy(float x, float y)
	{
		if (x != 0 || y != 0)
		{
			setModified();

			Shape connectorShape = _parent.getConnectorShape();
			if (connectorShape != null)
			{
				_dragPosition.x += x;
				_dragPosition.y += y;

				Point2D.Float absParent = _parent.getAbsolutePosition();

				Point2D.Float pt = new Point2D.Float(_dragPosition.x - absParent.x, _dragPosition.y - absParent.y);
				float distance = Geometry.getClosestPointOnShape(pt, connectorShape, 1);
				if (getConfiguration()._snapMaxSquaredDistance > distance)
				{
					// pt is the calculated closed point on the shape in local parent coordinates
					_absolutePosition.x += pt.x - _relativePosition.x;
					_absolutePosition.y += pt.y - _relativePosition.y;
					_relativePosition.x = pt.x;
					_relativePosition.y = pt.y;
				}
				else
				{
					float xd = _dragPosition.x - _absolutePosition.x;
					float yd = _dragPosition.y - _absolutePosition.y;
					_absolutePosition.x += xd;
					_absolutePosition.y += yd;
					_relativePosition.x += xd;
					_relativePosition.y += yd;
				}
				_absoluteBounds.x = _absolutePosition.x - _radius;
				_absoluteBounds.y = _absolutePosition.y - _radius;
			}
			else
			{
				_relativePosition.x += x;
				_relativePosition.y += y;
				_absolutePosition.x += x;
				_absolutePosition.y += y;
				_absoluteBounds.x = _absolutePosition.x - _radius;
				_absoluteBounds.y = _absolutePosition.y - _radius;
			}
		}
	}

	@Override
	protected void updateBounds(Graphics2D graphics)
	{
		Rectangle2D.Float localBounds;
		if (_primitives.isEmpty())
		{
			localBounds = new Rectangle2D.Float(0, 0, 0, 0);
		}
		else
		{
			localBounds = Geometry.getUnion(
					_primitives.stream().map(primitive -> primitive.getBounds2D(_absolutePosition, graphics)));
		}
		_absoluteBounds.x = localBounds.x;
		_absoluteBounds.y = localBounds.y;
		_absoluteBounds.width = localBounds.width;
		_absoluteBounds.height = localBounds.height;
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
		_primitives.forEach(DrawPrimitive::dispose);
		_primitives.clear();
	}

	@Override
	public void getControlPosition(Point2D.Float pt)
	{
		_parent.getAbsolutePosition(pt);
		if (_relativePosition != null)
		{
			pt.x += _relativePosition.x;
			pt.y += _relativePosition.y;
		}
	}

	@Override
	public List<DrawPrimitive> getPrimitives()
	{
		return Collections.unmodifiableList(_primitives);
	}

	/**
	 * Sets the position of the connector, relative to its parent.
	 *
	 * @param x The X offset.
	 * @param y The Y offset.
	 */
	public void setRelativePosition(float x, float y)
	{
		if (_relativePosition == null)
			_relativePosition = new Point2D.Float(x, y);
		else
		{
			_relativePosition.x = x;
			_relativePosition.y = y;
		}
		resetBounds();
	}

	/**
	 * Gets the relative position of the connector.
	 *
	 * @return The position or null (if not yet set).
	 */
	public Point2D.Float getRelativePosition()
	{
		return _relativePosition;
	}


	@Override
	public String toString()
	{
		return "->" + _parent;
	}
}

