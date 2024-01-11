package com.bw.graph;

import com.bw.graph.visual.Visual;

/**
 * Listener to monitor user interactions with graph components
 */
public interface InteractionListener
{
	/**
	 * A visual was selected.
	 *
	 * @param visual The selected visual.
	 */
	void selected(Visual visual);

	/**
	 * A visual was de-selected.
	 *
	 * @param visual The de-selected visual.
	 */
	void deselected(Visual visual);

}
