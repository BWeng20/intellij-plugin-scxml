package com.bw.modelthings.fsm.ui;

/**
 * Manager to maintain platform dependent UI components.<br>
 * The implementation may return the same instance for different calls of the same method.
 *
 * @param <C> The common base class of the UI framework used.
 */
public interface EditorUIManager<C>
{
	TransitionEditorUI<C> getTransitionEditorUI();

	StateNameEditorUI<C> getStateNameEditorUI();
}
