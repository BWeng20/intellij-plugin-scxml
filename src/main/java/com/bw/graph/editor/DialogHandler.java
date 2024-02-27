package com.bw.graph.editor;

import javax.swing.JComponent;

/**
 * Handles editor dialogs.
 */
public interface DialogHandler
{
	/**
	 * Opens the editor dialog.
	 *
	 * @param graph      The graph panel.
	 * @param editor     The editor component to show.
	 * @param endEdit    The "ok" callback.
	 * @param cancelEdit The "cancel" callback.
	 */
	void openEditor(GraphPane graph, JComponent editor, Runnable endEdit, Runnable cancelEdit);

	/**
	 * Close the editor dialog.
	 */
	void closeEditor();
}
