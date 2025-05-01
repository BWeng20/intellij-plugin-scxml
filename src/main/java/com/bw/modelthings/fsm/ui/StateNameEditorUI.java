package com.bw.modelthings.fsm.ui;

/**
 * Interface for Platform dependent UI implementations for state-name-editing.
 * @param <C> The common base class of the UI framework used.
 */
public interface StateNameEditorUI<C> extends EditorUI<C>
{
	/**
	 * Sets the data.
	 *
	 * @param state The state visual.
	 */
	void setStateVisual(StateVisual state);

	String getStateName();
}
