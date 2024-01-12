package com.bw.graph.editor;

import com.bw.graph.visual.Visual;

/**
 * Listener to monitor user interactions with graph components
 */
public interface InteractionListener
{
	/**
	 * Some visual was selected.
	 *
	 * @param visual The selected visual.
	 */
	void selected(Visual visual);

	/**
	 * A visual was explicitly de-selected without replacement.
	 *
	 * @param visual The de-selected visual.
	 */
	void deselected(Visual visual);


	/**
	 * Some sub-model was entered or left.
	 */
	void hierarchyChanged();

}
