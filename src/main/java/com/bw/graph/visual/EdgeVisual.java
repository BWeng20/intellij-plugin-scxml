package com.bw.graph.visual;

import com.bw.graph.DrawContext;
import com.bw.graph.primitive.DrawPrimitive;
import com.bw.graph.primitive.Path;
import com.bw.svg.SVGWriter;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Used to visualize edges in different modes.<br>
 * A path have a line (along the path), one connector at each end, and a number of control-points.
 */
public class EdgeVisual extends Visual
{
	/**
	 * The source connector at the source visual where the edge starts.
	 */
	protected ConnectorVisual _sourceConnector;

	/**
	 * The target connector at the target visual where the edge ends.
	 */
	protected ConnectorVisual _targetConnector;

	/**
	 * The path to draw
	 */
	protected Path _path;

	/**
	 * The list of control points.
	 */
	protected java.util.List<PathControlVisual> _controlVisual = new LinkedList<>();

	/**
	 * Creates a new Primitive.
	 *
	 * @param id      The Identification, can be null.
	 * @param source  The start connector.
	 * @param target  The end connector.
	 * @param context The  context. Must not be null.
	 */
	public EdgeVisual(Object id, ConnectorVisual source, ConnectorVisual target,
					  DrawContext context)
	{
		super(id, context);

		this._path = new Path(context._configuration, context._style, VisualFlags.ALWAYS);
		this._sourceConnector = source;
		this._targetConnector = target;

		_sourceConnector.setEdgeVisual(this);
		_targetConnector.setEdgeVisual(this);

		this._path.addPoint(_sourceConnector);
		this._path.addPoint(_targetConnector);
	}

	/**
	 * Draws for given context.
	 *
	 * @param g2 The graphics context
	 */
	@Override
	public void draw(Graphics2D g2)
	{
		if (_targetConnector != null)
		{
			_path.draw(g2);
			_targetConnector.draw(g2);
		}
		_sourceConnector.draw(g2);
	}

	@Override
	protected void drawRelative(Graphics2D g2)
	{

	}

	@Override
	public DrawPrimitive getEditablePrimitiveAt(float x, float y)
	{
		DrawPrimitive p = _path;
		if (_targetConnector != null)
			p = _targetConnector.getEditablePrimitiveAt(x, y);
		if (p == null && _sourceConnector != null)
			p = _sourceConnector.getEditablePrimitiveAt(x, y);
		if (p == null)
		{
			p = _path;
			if (p != null)
				p.setVisual(this);
		}
		return p;
	}

	@Override
	protected void updateBounds(Graphics2D graphics)
	{
		_absoluteBounds.x = _absolutePosition.x;
		_absoluteBounds.y = _absolutePosition.y;
		_absoluteBounds.width = 0;
		_absoluteBounds.height = 0;
	}

	@Override
	public void toSVG(SVGWriter sw, Graphics2D g2)
	{
		_path.toSVG(sw, g2, new Point2D.Float(0, 0));
	}

	@Override
	public void dispose()
	{
		super.dispose();
		_path = null;
		if (_sourceConnector != null)
		{
			_sourceConnector.dispose();
			_sourceConnector = null;
		}
		if (_targetConnector != null)
		{
			_targetConnector.dispose();
			_targetConnector = null;
		}
		_controlVisual.forEach(Visual::dispose);
		_controlVisual.clear();
	}

	/**
	 * Checks if a point is inside the area of the visual.
	 *
	 * @param x X position.
	 * @param y Y position.
	 * @return true if (x,y) is inside the visual.
	 */
	public boolean containsPoint(float x, float y)
	{
		return _path.getDistanceTo(new Point2D.Float(x, y)) < _context._configuration._selectEdgeMaxDistance;
	}


	@Override
	public List<DrawPrimitive> getPrimitives()
	{
		return Collections.emptyList();
	}


	@Override
	public void moveBy(float x, float y)
	{
	}

	/**
	 * Gets the connector at source side.
	 *
	 * @return The connector or null.
	 */
	public ConnectorVisual getSourceConnector()
	{
		return _sourceConnector;
	}

	/**
	 * Gets the connector at target side.
	 *
	 * @return The connector or null.
	 */
	public ConnectorVisual getTargetConnector()
	{
		return _targetConnector;
	}


	/**
	 * Gets the source visual. Same as {@link #getSourceVisual()} ()} and {@link ConnectorVisual#getParent()}
	 *
	 * @return The source visual or null.
	 */
	public Visual getSourceVisual()
	{
		return _sourceConnector == null ? null : _sourceConnector.getParent();
	}

	/**
	 * Gets the target visual. Same as {@link #getTargetConnector()} and {@link ConnectorVisual#getParent()}
	 *
	 * @return The target visual or null.
	 */
	public Visual getTargetVisual()
	{
		return _targetConnector == null ? null : _targetConnector.getParent();
	}

	/**
	 * Checks if {@link #getSourceVisual()} or {@link #getTargetVisual()} is v.
	 *
	 * @param v The visual to check for.
	 * @return true if the visual is the target- or source-visual.
	 */
	public boolean isConnectedTo(Visual v)
	{
		if (v != null)
		{
			return (_sourceConnector != null && _sourceConnector.getParent() == v) ||
					(_targetConnector != null && _targetConnector.getParent() == v);
		}
		return false;
	}

	/**
	 * Gets connector for visual.
	 *
	 * @param v The visual to check for.
	 * @return The connector or null.
	 */
	public ConnectorVisual getConnector(Visual v)
	{
		if (v != null)
		{
			if (_sourceConnector != null && _sourceConnector.getParent() == v) return _sourceConnector;
			if (_targetConnector != null && _targetConnector.getParent() == v) return _targetConnector;
		}
		return null;
	}

}