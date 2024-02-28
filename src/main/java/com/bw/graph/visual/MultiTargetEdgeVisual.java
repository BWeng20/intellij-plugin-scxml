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
	/**
	 * The target edges
	 */
	protected List<SingleTargetEdgeVisual> _edgeVisuals = new ArrayList<>();

	/**
	 * The source connector.
	 */
	protected ConnectorVisual _source;

	/**
	 * Constructur to be used by inheritances.
	 *
	 * @param id      The Identification, can be null.
	 * @param context The  context. Must not be null.
	 */
	protected MultiTargetEdgeVisual(Object id, DrawContext context)
	{
		super(id, context);
	}

	/**
	 * Creates a new Edge Visual.
	 *
	 * @param id      The Identification, can be null.
	 * @param source  The start connector.
	 * @param targets The end connectors.
	 * @param context The  context. Must not be null.
	 */
	public MultiTargetEdgeVisual(Object id, ConnectorVisual source, Collection<ConnectorVisual> targets,
								 DrawContext context)
	{
		super(id, context);
		_source = source;
		targets.forEach(this::addTarget);
	}

	/**
	 * Adds a target connector (and creates an edge for it).
	 *
	 * @param connectorVisual The connector to add.
	 */
	public void addTarget(ConnectorVisual connectorVisual)
	{
		SingleTargetEdgeVisual singleEdgeVisual = new SingleTargetEdgeVisual(null, _source, connectorVisual,
				_context);
		singleEdgeVisual.setParentEdge(this);
		_edgeVisuals.add(singleEdgeVisual);
	}

	/**
	 * Draws the edge. Edges itself are not relative to parent visuals.
	 *
	 * @param g2 The graphics context
	 */
	@Override
	public void draw(Graphics2D g2)
	{
		for (SingleTargetEdgeVisual edgeVisual : _edgeVisuals)
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
		_edgeVisuals.forEach(ev -> ev.updateBounds(graphics));
		super.updateBounds(graphics);
	}

	@Override
	public void toSVG(SVGWriter sw, Graphics2D g2)
	{
		_edgeVisuals.forEach(ev -> ev.toSVG(sw, g2));
	}

	@Override
	public void dispose()
	{
		_edgeVisuals.forEach(ev -> ev.dispose());
		_edgeVisuals.clear();
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
		return _edgeVisuals.stream().anyMatch(ev -> ev.containsPoint(x, y));
	}

	@Override
	public List<DrawPrimitive> getPrimitives()
	{
		return Collections.emptyList();
	}

	@Override
	public ConnectorVisual getSourceConnector()
	{
		return _edgeVisuals.isEmpty() ? null : _edgeVisuals.get(0)._sourceConnector;
	}

	/**
	 * Gets the connectors at target side.
	 *
	 * @return The connector or null.
	 */
	public List<ConnectorVisual> getTargetConnectors()
	{
		List<ConnectorVisual> targets = new ArrayList<>(_edgeVisuals.size());
		for (SingleTargetEdgeVisual edgeVisual : _edgeVisuals)
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
		return _edgeVisuals.isEmpty() ? null : _edgeVisuals.get(0).getSourceVisual();
	}

	/**
	 * Gets the target visuals.
	 *
	 * @return The target visual or null.
	 */
	public List<Visual> getTargetVisuals()
	{
		List<Visual> targets = new ArrayList<>(_edgeVisuals.size());
		for (SingleTargetEdgeVisual edgeVisual : _edgeVisuals)
		{
			Visual v = edgeVisual._targetConnector.getParentVisual();
			if (v != null)
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
		if (v != null && !_edgeVisuals.isEmpty())
		{
			return _edgeVisuals.get(0).getSourceVisual() == v || _edgeVisuals.stream().anyMatch(s -> s.getTargetVisuals().contains(v));
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
		return _edgeVisuals.stream()
						   .filter(ev -> ev.getTargetVisuals().contains(v))
						   .map(ev -> ev._targetConnector)
						   .findAny().orElse(null);
	}

	/**
	 * Gets the control points of the edge.
	 *
	 * @return The list of control points, possibly empty, but never null.
	 */
	public List<PathControlVisual> getControlPoints()
	{
		final List<PathControlVisual> l = new ArrayList<>();
		_edgeVisuals.forEach(v -> l.addAll(v.getControlPoints()));
		return l;
	}

	/**
	 * Gets the edge visuals.
	 *
	 * @return The collection of associated visuals.
	 */
	@Override
	public List<Visual> getVisuals()
	{
		if (!_edgeVisuals.isEmpty())
		{
			List<Visual> v = new ArrayList<>();
			v.add(_edgeVisuals.get(0)._sourceConnector);
			_edgeVisuals.forEach(ev -> {
				if (ev._targetConnector != null)
					v.add(ev._targetConnector);
				v.addAll(ev._controlVisual);
			});
			return v;
		}
		else
			return Collections.emptyList();
	}


	/**
	 * Sets Bit Flags.<br>
	 * Specific handling for "SELECTED".
	 *
	 * @param flags Flag bits to add.
	 */
	@Override
	public void setFlags(int flags)
	{
		super.setFlags(flags);
		if ((flags & VisualFlags.SELECTED) != 0)
		{
			_edgeVisuals.forEach(v -> v.setFlags(flags));
		}
	}

	/**
	 * Clears Bit Flags.<br>
	 * Specific handling for "SELECTED".
	 *
	 * @param flags Flag bits to clear.
	 */
	@Override
	public void clearFlags(int flags)
	{
		super.clearFlags(flags);
		if ((flags & VisualFlags.SELECTED) != 0)
		{
			_edgeVisuals.forEach(v -> v.clearFlags(flags));
		}
	}

}
