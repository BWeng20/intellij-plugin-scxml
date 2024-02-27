package com.bw.modelthings.intellij.editor;

import com.bw.graph.editor.GraphPane;
import com.intellij.openapi.ui.DialogWrapper;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;

/**
 * Dialog handler using intellij classes.
 */
public class DialogHandler implements com.bw.graph.editor.DialogHandler
{
	/**
	 * Creates a new handler.
	 */
	public DialogHandler()
	{
	}

	static class EditorDialogWrapper extends DialogWrapper
	{
		public EditorDialogWrapper()
		{
			super(true); // use current window as parent
			setTitle("Editor");

			setOKButtonText("Ok");
			setCancelButtonText("Cancel");

			init();
		}

		JPanel dialogPanel = new JPanel(new BorderLayout());

		@Override
		protected JComponent createCenterPanel()
		{
			return dialogPanel;
		}
	}

	EditorDialogWrapper _wrapper;
	JComponent _editorComponent;

	@Override
	public void openEditor(GraphPane graph, JComponent editor, Runnable endEdit, Runnable cancelEdit)
	{
		_wrapper = new EditorDialogWrapper();

		_wrapper.setCrossClosesWindow(true);
		_wrapper.setResizable(true);
		_wrapper.setModal(false);
		_wrapper.dialogPanel.add(editor, BorderLayout.CENTER);

		_wrapper.setOnDeactivationAction(() -> {
			// Will possibly be called multiple times during processing.
			if (_wrapper != null)
			{
				if (_wrapper.isOK())
					endEdit.run();
				else
					cancelEdit.run();
			}
		});

		_editorComponent = editor;

		_wrapper.show();

	}

	@Override
	public void closeEditor()
	{
		if (_wrapper.isShowing())
			_wrapper.close(0);

		_wrapper.dialogPanel.remove(_editorComponent);
		_wrapper.disposeIfNeeded();
		_wrapper = null;

	}
}
