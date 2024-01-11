package com.bw.graph;

import com.bw.graph.visual.EdgeVisual;
import com.bw.graph.visual.Visual;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Container for a graph model.
 */
public class VisualModel
{
	/**
	 * Creates a new empty model.
	 */
	public VisualModel()
	{
	}

	/**
	 * Listeners
	 */
	private final LinkedList<VisualModelListener> listeners = new LinkedList<>();


	/**
	 * List of visuals.
	 */
	private final LinkedList<Visual> visuals = new LinkedList<>();

	/**
	 * List of Edges.
	 */
	private final LinkedList<EdgeVisual> edges = new LinkedList<>();

	/**
	 * Gets the edges. Modification of the returned list will lead to undefined behaviour.
	 *
	 * @return The edges.
	 */
	public List<EdgeVisual> getEdges()
	{
		return edges;
	}

	/**
	 * Adds a new edge.
	 *
	 * @param edgeVisual The new edge.
	 */
	public void addEdge(EdgeVisual edgeVisual)
	{
		edges.add(edgeVisual);
		fireModelChange();
	}

	/**
	 * Gets the visuals. Modification of the returned list will lead to undefined behaviour.
	 *
	 * @return The visuals.
	 */
	public List<Visual> getVisuals()
	{
		return visuals;
	}

	/**
	 * Adds a new visual.
	 *
	 * @param visual The new visual.
	 */
	public void addVisual(Visual visual)
	{
		visual.resetBounds();
		visuals.add(visual);
		fireModelChange();
	}

	/**
	 * Moved the visual to top of z-order.
	 *
	 * @param visual The visual to move.
	 */
	public void moveVisualToTop(Visual visual)
	{
		visuals.remove(visual);
		visuals.add(visual);
		fireModelChange();
	}

	/**
	 * Dispose all. Clears edges and visuals and removed all listeners.
	 */
	public void dispose()
	{
		visuals.clear();
		edges.clear();
		listeners.clear();
	}

	/**
	 * Adds a new listener. It's not possible to add the same listener twice.
	 *
	 * @param listener The listener.
	 */
	public void addListener(VisualModelListener listener)
	{
		listeners.remove(listener);
		listeners.add(listener);
	}

	/**
	 * Removed a listener.
	 *
	 * @param listener The listener, can be null.
	 */
	public void removeListener(VisualModelListener listener)
	{
		listeners.remove(listener);
	}

	/**
	 * Informs all listeners about a model change.
	 */
	protected void fireModelChange()
	{
		List<VisualModelListener> ll = new ArrayList<>(listeners);
		for (var l : ll)
			l.modelChanged();
	}

}
