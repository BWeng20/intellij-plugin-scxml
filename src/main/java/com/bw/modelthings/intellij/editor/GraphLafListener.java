package com.bw.modelthings.intellij.editor;

/**
 * App specific listener interface to be used by editors to update visual settings if LAF was changed.
 *
 * @see GraphLafManagerListener
 */
public interface GraphLafListener
{
	/**
	 * Called by manager if laf was changed.
	 */
	void lafChanged();
}
