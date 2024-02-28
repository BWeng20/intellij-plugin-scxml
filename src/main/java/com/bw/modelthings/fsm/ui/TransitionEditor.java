package com.bw.modelthings.fsm.ui;

import com.bw.graph.VisualModel;
import com.bw.graph.editor.Editor;
import com.bw.graph.editor.action.EditAction;

import javax.swing.JComponent;
import java.awt.Graphics2D;

/**
 * Editor for Transitions.
 */
public class TransitionEditor implements Editor
{
	/**
	 * Initializes a new transition editor.
	 *
	 * @param pane   The editor pane to use.
	 * @param visual The visual to edit.
	 */
	public TransitionEditor(TransitionEditorPane pane, TransitionVisual visual)
	{
		_pane = pane;
		_transitionVisual = visual;
	}

	private final TransitionEditorPane _pane;
	private final TransitionVisual _transitionVisual;

	@Override
	public boolean isInPlace()
	{
		return false;
	}

	@Override
	public JComponent getEditor()
	{
		_pane.setTransitionVisual(_transitionVisual);
		return _pane;
	}

	@Override
	public EditAction endEdit(VisualModel model, Graphics2D g2)
	{
		// TODO
		return null;
	}

	@Override
	public void cancelEdit()
	{

	}
}
