package com.bw.graph;

import com.bw.graph.visual.ConnectorVisual;
import com.bw.graph.visual.EdgeVisual;
import com.bw.graph.visual.SingleTargetEdgeVisual;
import com.bw.graph.visual.MultiTargetEdgeVisual;
import com.bw.graph.visual.Visual;
import com.bw.graph.visual.VisualContainer;
import com.bw.graph.visual.VisualFlags;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
	 * Flags.
	 */
	private int _flags = 0;

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
			setModified();
			visual.resetBounds();
			_visuals.add(visual);
			fireModelChange();
		}
	}

	/**
	 * Moved the visual to top of z-order.
	 *
	 * @param visual The visual to move.
	 * @return True if z-order was changed.
	 */
	public boolean moveVisualToTop(Visual visual)
	{
		boolean zOrderChanged = false;
		if (visual instanceof VisualContainer visualContainer)
		{
			List<Visual> subVisuals = visualContainer.getVisuals();
			subVisuals.add(0, visual);

			int index = subVisuals.size();
			int mainIndex = _visuals.size();
			while ((--mainIndex) >= 0 && (--index) >= 0)
			{
				if (subVisuals.get(index) != _visuals.get(mainIndex))
				{
					zOrderChanged = true;
					break;
				}
			}
			if (zOrderChanged)
			{
				for (Visual v : subVisuals)
				{
					if (_visuals.remove(v))
					{
						_visuals.add(v);
					}
				}
				fireModelChange();
			}

		}
		else if (_visuals.get(_visuals.size() - 1) != visual)
		{
			if (_visuals.remove(visual))
			{
				_visuals.add(visual);
				zOrderChanged = true;
				fireModelChange();
			}
		}
		return zOrderChanged;
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
		return isFlagSetDeep(VisualFlags.MODIFIED);
	}

	/**
	 * Sets modified status.
	 */
	public void setModified()
	{
		setFlags(VisualFlags.MODIFIED);
	}

	@Override
	public String toString()
	{
		return _name == null ? "none" : _name;
	}

	/**
	 * Checks if flag is set on the model.
	 *
	 * @param flags bit-wise combinations of flags.
	 * @return True if the flag is set.
	 */
	public boolean isFlagSet(int flags)
	{
		return (_flags & flags) == flags;
	}

	/**
	 * Checks if the model itself or any visual has the flag set.
	 *
	 * @param flags The bit-wise cobination of flags to check.
	 * @return True if all bits are set in the model or any contained visual.
	 */
	public boolean isFlagSetDeep(int flags)
	{
		return (_flags & flags) == flags ||
				_visuals.stream()
						.anyMatch(visual -> visual.isFlagSet(flags));
	}

	/**
	 * Sets the flags in the model and all visuals.
	 *
	 * @param flags The bit-wise combination of flags to set.
	 */
	public void setFlags(int flags)
	{
		_flags |= flags;
		for (Visual v : _visuals)
			v.setFlags(flags);
	}

	/**
	 * Clears the flags in the model and all visuals.
	 *
	 * @param flags The bit-wise combination of flags to clear.
	 */
	public void clearFlags(int flags)
	{
		_flags &= ~flags;

		for (Visual v : _visuals)
			v.clearFlags(flags);
	}

	/**
	 * Gets all connectors that are attached at the visual.
	 *
	 * @param visual The visual to get connectors for.
	 * @return The list of connectors. May be empty but never null.
	 */
	public List<ConnectorVisual> getConnectorsAt(Visual visual)
	{
		return _visuals.stream()
					   .filter(v -> v instanceof EdgeVisual)
					   .map(v -> ((EdgeVisual) v).getConnector(visual))
					   .filter(Objects::nonNull)
					   .collect(Collectors.toList());
	}


	/**
	 * Gets all edges that are attached at the visual.
	 *
	 * @param visual The visual to get connectors for.
	 * @return The list of connectors. May be empty but never null.
	 */
	public List<EdgeVisual> getEdgesAt(Visual visual)
	{
		return _visuals.stream()
					   .filter(v -> v instanceof EdgeVisual edgeVisual && edgeVisual.isConnectedTo(visual))
					   .map(v -> (MultiTargetEdgeVisual) v)
					   .collect(Collectors.toList());
	}
}
