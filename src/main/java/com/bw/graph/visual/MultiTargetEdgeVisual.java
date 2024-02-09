package com.bw.graph.visual;

import com.bw.graph.DrawContext;
import com.bw.graph.primitive.DrawPrimitive;
import com.bw.svg.SVGWriter;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Visualize an edges to multiple targets.<br>
 */
public class MultiTargetEdgeVisual extends EdgeVisual
{

	protected ConnectorVisual _innerConnector;

	/**
	 * The Source edge
	 */
	protected SingleTargetEdgeVisual _sourceEdgeVisual;

	/**
	 * The target edges
	 */
	protected List<SingleTargetEdgeVisual> _targetEdgeVisuals = new ArrayList<>();

	/**
	 * Creates a new Edge Visual.
	 *
	 * @param id      The Identification, can be null.
	 * @param source  The start connector.
	 * @param targets  The end connectors.
	 * @param context The  context. Must not be null.
	 */
	public MultiTargetEdgeVisual(Object id, ConnectorVisual source, Collection<ConnectorVisual> targets,
								 DrawContext context)
	{
		super(id, context);
		_innerConnector = new ConnectorVisual(this, context, VisualFlags.SELECTED);
		_sourceEdgeVisual = new SingleTargetEdgeVisual(null, source, _innerConnector, context);
		for ( ConnectorVisual connectorVisual : targets)
			_targetEdgeVisuals.add( new SingleTargetEdgeVisual(null, _innerConnector, connectorVisual, context) );
	}

	/**
	 * Draws the edge. Edges itself are not relative to parent visuals.
	 *
	 * @param g2 The graphics context
	 */
	@Override
	public void draw(Graphics2D g2)
	{
		_sourceEdgeVisual.draw(g2);
		for ( SingleTargetEdgeVisual edgeVisual : _targetEdgeVisuals)
			edgeVisual.draw(g2);
	}

	@Override
	public DrawPrimitive getEditablePrimitiveAt(float x, float y)
	{
		return null;
	}

	@Override
	protected void updateBounds(Graphics2D graphics)
	{
		_sourceEdgeVisual.updateBounds(graphics);
		_targetEdgeVisuals.forEach(ev -> ev.updateBounds(graphics));
		super.updateBounds(graphics);
	}

	@Override
	public void toSVG(SVGWriter sw, Graphics2D g2)
	{
		_sourceEdgeVisual.toSVG(sw, g2);
		_targetEdgeVisuals.forEach(ev -> ev.toSVG(sw, g2));
	}

	@Override
	public void dispose()
	{
		_sourceEdgeVisual.dispose();
		_targetEdgeVisuals.forEach(ev -> ev.dispose());
		_targetEdgeVisuals.clear();
		super.dispose();
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
		return _sourceEdgeVisual.containsPoint(x,y) || _targetEdgeVisuals.stream().anyMatch(ev -> ev.containsPoint(x,y));
	}

	@Override
	public List<DrawPrimitive> getPrimitives()
	{
		return Collections.emptyList();
	}

	@Override
	public ConnectorVisual getSourceConnector()
	{
		return _sourceEdgeVisual._sourceConnector;
	}

	/**
	 * Gets the connectors at target side.
	 *
	 * @return The connector or null.
	 */
	public List<ConnectorVisual> getTargetConnectors()
	{
		List<ConnectorVisual> targets =new ArrayList<>(_targetEdgeVisuals.size());
		for ( SingleTargetEdgeVisual edgeVisual : _targetEdgeVisuals)
			targets.add(edgeVisual._targetConnector);
		return targets;
	}

	/**
	 * Gets the source visual.
	 *
	 * @return The source visual or null.
	 */
	public Visual getSourceVisual()
	{
		return _sourceEdgeVisual.getSourceVisual();
	}

	/**
	 * Gets the target visuals.
	 *
	 * @return The target visual or null.
	 */
	public List<Visual> getTargetVisuals()
	{
		List<Visual> targets =new ArrayList<>(_targetEdgeVisuals.size());
		for ( SingleTargetEdgeVisual edgeVisual : _targetEdgeVisuals)
		{
			Visual v = edgeVisual._targetConnector.getParent();
			if ( v != null )
				targets.add(v);
		}
		return targets;
	}

	/**
	 * Checks if {@link #getSourceVisual()} or {@link #getTargetVisuals()} is/contains v.
	 *
	 * @param v The visual to check for.
	 * @return true if the visual is the target- or source-visual.
	 */
	public boolean isConnectedTo(Visual v)
	{
		if (v != null)
		{
			return _sourceEdgeVisual.getSourceVisual() == v ||
					_targetEdgeVisuals.stream().anyMatch(s -> s.getTargetVisuals().contains(v));
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
		ConnectorVisual connectorVisual;
		if (v != null)
		{
			connectorVisual = _sourceEdgeVisual.getConnector(v);
			if ( connectorVisual == null ) {
				connectorVisual = _targetEdgeVisuals.stream()
													.filter(ev -> ev.getTargetVisuals().contains(v) )
													.map(ev -> ev._targetConnector)
													.findAny().orElse(null);
			}
		} else
			connectorVisual = null;
		return connectorVisual;
	}

	/**
	 * Gets the control points of the edge.
	 * @return The list of control points, possibly empty, but never null.
	 */
	public List<PathControlVisual> getControlPoints()
	{
		return _sourceEdgeVisual.getControlPoints();
	}

	/**
	 * Gets the edge visuals.
	 *
	 * @return The collection of associated visuals.
	 */
	@Override
	public List<Visual> getVisuals()
	{
		List<Visual> v = new ArrayList<>(1 + _targetEdgeVisuals.size());
		v.add(_sourceEdgeVisual);
		v.addAll(_targetEdgeVisuals);
		return v;
	}


}