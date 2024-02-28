package com.bw.graph.visual;

import com.bw.graph.DrawContext;

import java.util.List;

/**
 * Base fo visuals that encapsulate other visual.
 */
public abstract class VisualContainer extends Visual
{
	/**
	 * Create a new empty visual.
	 *
	 * @param id      The identification. Can be null.
	 * @param context The Drawing context to use.
	 */
	protected VisualContainer(Object id, DrawContext context)
	{
		super(id, context);
	}

	/**
	 * Gets the associated sub-visuals of this container.
	 * Remind that visuals can be associated to multiple other visuals.
	 *
	 * @return Collection of visuals.
	 */
	public abstract List<Visual> getVisuals();
}
