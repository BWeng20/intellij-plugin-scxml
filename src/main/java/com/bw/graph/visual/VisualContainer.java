package com.bw.graph.visual;

import java.util.List;

/**
 * Interface for visual containers.
 */
public interface VisualContainer
{
	/**
	 * Gets the associated sub-visuals of this container.
	 * Remind that visuals can be associated to multiple other visuals.
	 *
	 * @return Collection of visuals.
	 */
	List<Visual> getVisuals();
}
