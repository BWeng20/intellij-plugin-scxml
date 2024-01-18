package com.bw.modelthings.fsm.ui;

import com.bw.graph.editor.EditorProxy;
import com.bw.graph.primitive.DrawPrimitive;
import com.bw.graph.primitive.Text;
import com.bw.modelthings.fsm.model.State;

import javax.swing.JTextField;
import javax.swing.text.JTextComponent;
import java.awt.Component;
import java.util.Objects;

public class StateNameProxy implements EditorProxy
{
	public State state;
	public JTextComponent textComponent;

	private String originalName;

	public StateNameProxy(State state, JTextComponent textComponent ) {
		this.state = state;
		this.textComponent = textComponent;
	}

	@Override
	public String toString() {
		return state.name;
	}

	@Override
	public Component getEditor(DrawPrimitive text)
	{
		originalName = state.name;
		textComponent.setText(state.name);
		return textComponent;
	}

	@Override
	public void endEdit(DrawPrimitive text)
	{
		String newName = textComponent.getText().trim();
		if (!Objects.equals(newName, state.name))
		{
			state.name = textComponent.getText();
			text.getVisual().setModified(true);
		}
	}

	@Override
	public void cancelEdit (DrawPrimitive text)
	{
		if (originalName != null && !Objects.equals(originalName, state.name))
		{
			state.name = originalName;
			text.getVisual().setModified(true);
		}
	}
}
