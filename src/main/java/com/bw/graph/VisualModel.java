package com.bw.graph;

import com.bw.graph.visual.Visual;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
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
	 *
	 * @param name The name, can be null
	 */
	public VisualModel(String name)
	{
		this._name = name;
	}

	/**
	 * The name of the model.
	 */
	public final String _name;

	/**
	 * Listeners
	 */
	private final LinkedList<VisualModelListener> _listeners = new LinkedList<>();


	/**
	 * List of visuals.
	 */
	private final LinkedList<Visual> _visuals = new LinkedList<>();


	/**
	 * Marks the model as modified.
	 */
	private boolean _dirty = false;

	/**
	 * Gets the visuals. Modification of the returned list will lead to undefined behaviour.
	 *
	 * @return The visuals.
	 */
	public List<Visual> getVisuals()
	{
		return _visuals;
	}

	/**
	 * Adds a new visual.
	 *
	 * @param visual The new visual.
	 */
	public void addVisual(Visual visual)
	{
		if (visual != null)
		{
			_dirty = true;
			visual.resetBounds();
			_visuals.add(visual);
			fireModelChange();
		}
	}

	/**
	 * Moved the visual to top of z-order.
	 *
	 * @param visual The visual to move.
	 */
	public void moveVisualToTop(Visual visual)
	{
		if (_visuals.get(_visuals.size() - 1) != visual)
		{
			if (_visuals.remove(visual))
			{
				_visuals.add(visual);
				fireModelChange();
			}
		}
	}

	/**
	 * Dispose all. Dispose edges and visuals and removes all listeners.
	 */
	public void dispose()
	{
		_visuals.forEach(Visual::dispose);
		_visuals.clear();
		_listeners.clear();
	}

	/**
	 * Adds a new listener. It's not possible to add the same listener twice.
	 *
	 * @param listener The listener.
	 */
	public void addListener(VisualModelListener listener)
	{
		_listeners.remove(listener);
		_listeners.add(listener);
	}

	/**
	 * Removed a listener.
	 *
	 * @param listener The listener, can be null.
	 */
	public void removeListener(VisualModelListener listener)
	{
		_listeners.remove(listener);
	}

	/**
	 * Informs all listeners about a model change.
	 */
	protected void fireModelChange()
	{
		List<VisualModelListener> ll = new ArrayList<>(_listeners);
		for (var l : ll)
			l.modelChanged();
	}

	/**
	 * Draws the model
	 *
	 * @param g2 The Graphics to use.
	 */
	public void draw(Graphics2D g2)
	{
		for (Visual v : _visuals)
			v.draw(g2);
	}

	/**
	 * Calculate the bounds of the model.
	 *
	 * @param g2 The Graphics to use. for calculations.
	 * @return The bounds, containing all elements.
	 */
	public Rectangle2D.Float getBounds2D(Graphics2D g2)
	{
		Rectangle2D.Float bounds = new Rectangle2D.Float(0, 0, 0, 0);
		float x2 = 0;
		float y2 = 0;
		float t2;
		for (Visual visual : _visuals)
		{
			Rectangle2D.Float visualBounds = visual.getAbsoluteBounds2D(g2);
			if (bounds.x > visualBounds.x)
				bounds.x = visualBounds.x;
			if (bounds.y > visualBounds.y)
				bounds.y = visualBounds.y;
			t2 = visualBounds.x + visualBounds.width;
			if (x2 < t2)
				x2 = t2;
			t2 = visualBounds.y + visualBounds.height;
			if (y2 < t2)
				y2 = t2;
		}
		bounds.width = x2 - bounds.x;
		bounds.height = y2 - bounds.y;
		return bounds;
	}

	/**
	 * Force a repaint of the model.
	 */
	public void repaint()
	{
		getVisuals().forEach(Visual::invalidateBuffers);
	}

	/**
	 * Checks if the model was modified
	 *
	 * @return true if modified.
	 */
	public boolean isModified()
	{
		return _dirty || _visuals.stream()
								 .anyMatch(Visual::isModified);
	}

	/**
	 * Sets modified status.
	 *
	 * @param modified The new modified.
	 */
	public void setModified(boolean modified)
	{
		_dirty = modified;
		if (!modified)
		{
			_visuals.forEach(v -> v.setModified(false));
		}
	}

	@Override
	public String toString()
	{
		return _name == null ? "none" : _name;
	}

}
