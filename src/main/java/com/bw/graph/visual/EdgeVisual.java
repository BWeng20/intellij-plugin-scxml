package com.bw.graph.visual;

import com.bw.graph.DrawContext;

import java.awt.Graphics2D;
import java.util.List;

/**
 * Base of Edge Visuals.
 */
public abstract class EdgeVisual extends VisualContainer
{
	/**
	 * Initializes the new Edge Visual.
	 *
	 * @param id      The id or null.
	 * @param context The Draw Context to use.
	 */
	public EdgeVisual(Object id, DrawContext context)
	{
		super(id, context);
	}

	@Override
	protected void drawRelative(Graphics2D g2)
	{
		// Nothing to do, edge is drawn in "draw" above as not relative.
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
	public void dragBy(float x, float y)
	{
	}

	/**
	 * Gets the source visual.
	 *
	 * @return The source visual or null.
	 */
	public abstract Visual getSourceVisual();

	/**
	 * Gets the target visual. Same as {@link #getTargetConnectors()} and {@link ConnectorVisual#getParentVisual()}
	 *
	 * @return The list of target visuals. Possibly empty, but never null.
	 */
	public abstract List<Visual> getTargetVisuals();

	/**
	 * Gets the connectors for target sides.
	 *
	 * @return The connectors. Possibly empty, but never null.
	 */
	public abstract List<ConnectorVisual> getTargetConnectors();

	/**
	 * Checks if {@link #getSourceVisual()} or {@link #getTargetVisuals()} is/contains v.
	 *
	 * @param v The visual to check for.
	 * @return true if the visual is the target- or source-visual.
	 */
	public abstract boolean isConnectedTo(Visual v);

	/**
	 * Gets connector for visual.
	 *
	 * @param v The visual to check for.
	 * @return The connector or null.
	 */
	public abstract ConnectorVisual getConnector(Visual v);

	/**
	 * Gets the connector at source side.
	 *
	 * @return The connector or null.
	 */
	public abstract ConnectorVisual getSourceConnector();

	/**
	 * Gets the parent edge if this edge is part of a multi-edge.
	 *
	 * @return The parent edge or null
	 */
	public EdgeVisual getParentEdge()
	{
		return (EdgeVisual) _parent;
	}

	/**
	 * Sets the parent edge if this edge is part of a multi-edge.
	 *
	 * @param edge The parent edge or null
	 */
	public void setParentEdge(EdgeVisual edge)
	{
		_parent = edge;
	}
}
