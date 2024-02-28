package com.bw.graph.editor;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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
	private boolean _handled = false;

	@Override
	public void openEditor(GraphPane graph, JComponent editor, Runnable endEdit, Runnable cancelEdit)
	{
		if (_dialog != null)
			closeEditor();

		_dialog = new JDialog(SwingUtilities.getWindowAncestor(graph));

		_dialog.setLayout(new BorderLayout());

		JButton ok = new JButton("Ok");
		JButton cancel = new JButton("Cancel");

		_handled = false;

		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttons.add(ok);
		buttons.add(cancel);

		ok.addActionListener(e -> {
			_handled = true;
			System.err.println("OK selected");
			endEdit.run();
		});

		cancel.addActionListener(e -> {
			_handled = true;
			System.err.println("CANCEL selected");
			cancelEdit.run();
		});

		_dialog.add(buttons, BorderLayout.SOUTH);
		_dialog.setModal(false);
		_dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		_dialog.add(editor, BorderLayout.CENTER);
		_dialog.pack();
		_dialog.setLocationRelativeTo(graph);
		_dialog.setVisible(true);

		_dialog.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowDeactivated(WindowEvent e)
			{
				if (!_handled)
				{
					System.err.println("Editor Deactivated");
					cancelEdit.run();
					_handled = true;
				}
			}
		});
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
