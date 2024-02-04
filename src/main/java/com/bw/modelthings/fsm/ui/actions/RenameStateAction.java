package com.bw.modelthings.fsm.ui.actions;

import com.bw.graph.editor.action.EditAction;

/**
 * Action to rename a state.
 */
public class RenameStateAction implements EditAction
{
	/**
	 * The original name.
	 */
	public final String _oldName;

	/**
	 * The new name.
	 */
	public final String _newName;

	/**
	 * Creates a new rename action
	 *
	 * @param oldName Original name.
	 * @param newName New name.
	 */
	public RenameStateAction(String oldName, String newName)
	{
		_oldName = oldName;
		_newName = newName;
	}
}
