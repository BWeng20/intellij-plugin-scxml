package com.bw.modelthings.fsm.ui;

/**
 * Editor pane for FSM Transitions.
 */
public interface TransitionEditorUI<C> extends EditorUI<C>
{

	/**
	 * Sets the data for the transition.
	 *
	 * @param transition The transition visual.
	 */
	void setTransitionVisual(TransitionVisual transition);

}
