package com.bw.graph.editor.action;

import java.util.ArrayList;
import java.util.List;

/**
 * List of edit actions, used in case an editor changed multiple options.
 */
public class EditActionList implements EditAction
{

	/**
	 * Creates a new action list.
	 */
	public EditActionList()
	{
	}

	/**
	 * The list of actions.
	 */
	public final List<EditAction> _actions = new ArrayList<>();
}
