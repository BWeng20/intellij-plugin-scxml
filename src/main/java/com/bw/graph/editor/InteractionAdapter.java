package com.bw.graph.editor;

import com.bw.graph.visual.Visual;

/**
 * Listener to monitor user interactions with graph components
 */
public class InteractionAdapter implements InteractionListener
{

	/**
	 * Creates a new instance.
	 */
	public InteractionAdapter()
	{

	}

	/**
	 * Some visual was selected.
	 *
	 * @param visual The selected visual.
	 */
	@Override
	public void selected(Visual visual)
	{
	}

	@Override
	public void deselected(Visual visual)
	{
	}


	@Override
	public void hierarchyChanged()
	{
	}

	@Override
	public void mouseDragging(Visual visual)
	{
	}

	@Override
	public void mouseOver(Visual visual)
	{
	}

}
