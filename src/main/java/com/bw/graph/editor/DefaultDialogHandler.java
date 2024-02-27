package com.bw.graph.editor;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

/**
 * Default Dialog Handler, using a JDialog.
 */
public class DefaultDialogHandler implements DialogHandler
{
	/**
	 * Creates a new Handler.
	 */
	public DefaultDialogHandler()
	{

	}


	private JDialog _dialog;

	@Override
	public void openEditor(GraphPane graph, JComponent editor, Runnable endEdit, Runnable cancelEdit)
	{
		if (_dialog != null)
			closeEditor();

		_dialog = new JDialog(SwingUtilities.getWindowAncestor(graph));

		_dialog.setLayout(new BorderLayout());

		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
		_dialog.add(buttons, BorderLayout.SOUTH);
		_dialog.setModal(false);
		_dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		_dialog.add(editor, BorderLayout.CENTER);
		_dialog.pack();
		_dialog.setLocationRelativeTo(graph);
		_dialog.setVisible(true);
	}

	@Override
	public void closeEditor()
	{
		if (_dialog != null)
		{
			if (_dialog.isShowing())
				_dialog.setVisible(false);
			_dialog = null;
		}
	}
}
