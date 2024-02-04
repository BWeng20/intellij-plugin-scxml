package com.bw.modelthings.fsm.ui;

import com.bw.graph.editor.action.EditAction;
import com.bw.graph.editor.action.MoveAction;
import com.bw.modelthings.fsm.ui.actions.RenameStateAction;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds a current changes in editor.
 */
public class EditorChanges
{
	/**
	 * Creates a new empty instance.
	 *
	 * @param action The action of the change
	 */
	public EditorChanges(EditAction action)
	{
		if (action instanceof RenameStateAction renameStateAction)
		{
			_statesRenamed.put(renameStateAction._oldName, renameStateAction._newName);
			_commandName = "Rename";
		}
		else if (action instanceof MoveAction moveAction)
		{
			if (moveAction._what instanceof StateVisual stateVisual)
			{
				_commandName = "Move";
				_bounds.put(stateVisual._state._name, new PosAndBounds(stateVisual.getAbsolutePosition(), stateVisual.getAbsoluteBounds2D(null)));
			}
			else if (moveAction._what instanceof StartVisual startVisual)
			{

				_commandName = "Move";
				String name = (startVisual._parent == null) ? "xxx" : startVisual._parent.getCurrentName();
				_startBounds.put(name, new PosAndBounds(startVisual.getAbsolutePosition(), startVisual.getAbsoluteBounds2D(null)));
			}
		}
	}

	/**
	 * The command name of the change
	 */
	public String _commandName;

	/**
	 * Map of renamed states.
	 * Key = Original Name, Value = new name.
	 */
	public Map<String, String> _statesRenamed = new HashMap<>();

	/**
	 * Collected bounds. Key = State Name, Value = the bounds.
	 */
	public Map<String, PosAndBounds> _bounds = new HashMap<>();

	/**
	 * Collected start-node bounds. Key = Parent State Name, Value = the bounds.
	 */
	public Map<String, PosAndBounds> _startBounds = new HashMap<>();

}
