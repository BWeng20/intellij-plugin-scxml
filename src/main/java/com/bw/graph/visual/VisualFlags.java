package com.bw.graph.visual;

/**
 * Static interface with possible flags bits.
 */
public interface VisualFlags
{
	/**
	 * Flag to draw always.
	 */
	int ALWAYS = 1;

	/**
	 * Flag to draw if selected.
	 */
	int SELECTED = 2;

	/**
	 * Flag to mark the element as modified.
	 */
	int MODIFIED = 4;

	/**
	 * Flag to declare a primitive editable.
	 */
	int EDITABLE = 8;
}
