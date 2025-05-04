package com.bw.modelthings.fsm.ui.swing;

import com.bw.modelthings.fsm.ui.EditorUIManager;
import com.bw.modelthings.fsm.ui.StateNameEditorUI;
import com.bw.modelthings.fsm.ui.TransitionEditorUI;

import javax.swing.JComponent;

public class EditorUISwingManager implements EditorUIManager<JComponent>
{
	private TransitionEditorUI<JComponent> _transitionEditor;
	private StateNameEditorUI<JComponent> _stateNameEditor;

	public EditorUISwingManager()
	{
		this(new StateNameEditor(),
				new TransitionEditor());
	}

	public EditorUISwingManager(
			StateNameEditorUI<? extends JComponent> stateNameUI,
			TransitionEditorUI<? extends JComponent> transitionEditorUI
	)
	{
		_transitionEditor = (TransitionEditorUI<JComponent>) transitionEditorUI;
		_stateNameEditor = (StateNameEditorUI<JComponent>) stateNameUI;
	}

	@Override
	public TransitionEditorUI<JComponent> getTransitionEditorUI()
	{
		return _transitionEditor;
	}

	@Override
	public StateNameEditorUI<JComponent> getStateNameEditorUI()
	{
		return _stateNameEditor;
	}
}
