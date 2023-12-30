package com.bw.modeldrive.editor;

import com.bw.modeldrive.model.FiniteStateMachine;
import com.intellij.openapi.Disposable;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;

/**
 * Panel to show the FSM as Graphical State Machine.
 */
public class ScxmlGraphPanel extends JPanel implements Disposable
{
	/**
	 * Dummy to show something
	 */
	JLabel info;

	@Override
	public void dispose()
	{
	}

	/**
	 * Create a new editor panel.
	 */
	public ScxmlGraphPanel()
	{
		super(new BorderLayout());
		info = new JLabel("SCXML");
		add(info, BorderLayout.CENTER);
	}

	/**
	 * Sets the FSM to show.
	 *
	 * @param fsm The FSM to show.
	 */
	public void setStateMachine(FiniteStateMachine fsm)
	{
	}

	/**
	 * In case the FSM could not be loaded, show the cause.
	 *
	 * @param cause The error cause, can be null.
	 */
	public void setError(Throwable cause)
	{
		info.setText(cause == null ? "---" : cause.getLocalizedMessage());
	}
}
