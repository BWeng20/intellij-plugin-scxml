package com.bw.graph.visual;

import com.bw.graph.DrawContext;
import com.bw.graph.primitive.DrawPrimitive;
import com.bw.graph.primitive.Path;
import com.bw.svg.SVGWriter;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Used to visualize edges in different modes.<br>
 * A path have a line (along the path), one connector at each end, and a number of control-points.
 */
public class SingleTargetEdgeVisual extends EdgeVisual
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
	 * Creates a new EdgeVisual.
	 *
	 * @param id      The Identification, can be null.
	 * @param source  The start connector.
	 * @param target  The end connector.
	 * @param context The  context. Must not be null.
	 */
	public SingleTargetEdgeVisual(Object id, ConnectorVisual source, ConnectorVisual target,
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
	 * Draws the edge. Edges itself are not relative to parent visuals.
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
		if (_sourceConnector != null)
		{
			_sourceConnector.draw(g2);
		}
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
		return _path.getDistanceTo(x, y) < _context._configuration._selectMaxDistance;
	}


	@Override
	public List<DrawPrimitive> getPrimitives()
	{
		return Collections.emptyList();
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

	@Override
	public List<ConnectorVisual> getTargetConnectors()
	{
		return Collections.singletonList(_targetConnector);
	}

	@Override
	public Visual getSourceVisual()
	{
		return _sourceConnector == null ? null : _sourceConnector.getParentVisual();
	}

	/**
	 * Gets the target visual. Same as {@link #getTargetConnectors()} and {@link ConnectorVisual#getParentVisual()}
	 *
	 * @return The list of target visuals. Possibly empty, but never null.
	 */
	@Override
	public List<Visual> getTargetVisuals()
	{
		return _targetConnector == null ? Collections.emptyList() : Collections.singletonList(_targetConnector.getParentVisual());
	}

	/**
	 * Checks if {@link #getSourceVisual()} or {@link #getTargetVisuals()} is/contains v.
	 *
	 * @param v The visual to check for.
	 * @return true if the visual is the target- or source-visual.
	 */
	@Override
	public boolean isConnectedTo(Visual v)
	{
		if (v != null)
		{
			return (_sourceConnector != null && _sourceConnector.getParentVisual() == v) ||
					(_targetConnector != null && _targetConnector.getParentVisual() == v);
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
			if (_sourceConnector != null && _sourceConnector.getParentVisual() == v) return _sourceConnector;
			if (_targetConnector != null && _targetConnector.getParentVisual() == v) return _targetConnector;
		}
		return null;
	}

	/**
	 * Gets the control points of the edge.
	 *
	 * @return The list of control points, possibly empty, but never null.
	 */
	public List<PathControlVisual> getControlPoints()
	{
		return _controlVisual;
	}

	/**
	 * Gets the connector and path-control visuals.
	 *
	 * @return The collection of associated visuals.
	 */
	@Override
	public List<Visual> getVisuals()
	{
		List<Visual> v = new ArrayList<>(2 + _controlVisual.size());
		if (_targetConnector != null)
			v.add(_targetConnector);
		if (_sourceConnector != null)
			v.add(_sourceConnector);
		v.addAll(_controlVisual);
		return v;
	}
}