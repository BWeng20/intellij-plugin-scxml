package com.bw.modelthings.fsm.ui.swing;

import com.bw.modelthings.fsm.ui.StateNameEditorUI;
import com.bw.modelthings.fsm.ui.StateVisual;

import javax.swing.JComponent;
import javax.swing.JTextField;

public class StateNameEditor extends JTextField implements StateNameEditorUI<JComponent>
{
	@Override
	public void setStateVisual(StateVisual state)
	{
		setText(state.getState()._name);
	}

	@Override
	public JComponent getComponent()
	{
		return this;
	}

	@Override
	public String getStateName()
	{
		return getText();
	}
}
