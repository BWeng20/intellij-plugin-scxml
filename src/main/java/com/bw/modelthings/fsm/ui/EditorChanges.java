package com.bw.modelthings.fsm.ui;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds a current changes in editor.
 */
public class EditorChanges
{
	/**
	 * Creates a new empty instance.
	 */
	public EditorChanges()
	{

	}

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
