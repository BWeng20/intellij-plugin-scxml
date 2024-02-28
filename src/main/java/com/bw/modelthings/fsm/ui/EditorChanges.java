package com.bw.modelthings.fsm.ui;

import com.bw.graph.editor.action.EditAction;
import com.bw.graph.editor.action.EditActionList;
import com.bw.graph.editor.action.MoveAction;
import com.bw.graph.visual.ConnectorVisual;
import com.bw.graph.visual.EdgeVisual;
import com.bw.graph.visual.PathControlVisual;
import com.bw.graph.visual.SingleTargetEdgeVisual;
import com.bw.modelthings.fsm.ui.actions.EventChangeAction;
import com.bw.modelthings.fsm.ui.actions.RenameStateAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds a current changes in editor.
 */
public class EditorChanges
{
	/**
	 * Possible editor commands
	 */
	public enum Command
	{
		/**
		 * Some visual was moved.
		 */
		Move,

		/**
		 * Some visual was renamed.
		 */
		Rename,

		/**
		 * Events of a transition changed.
		 */
		Events
	}

	private final Map<String, TransitionDescription> _transitionDescriptionMap = new HashMap<>();

	/**
	 * Get the descriptor for a transition by the edge-visual.
	 * The EdgeVisual should have a String-Id (docId or xml:id).
	 *
	 * @param edgeVisual The edge visual.
	 * @return The descriptor, never null.
	 */
	public TransitionDescription getTransitionDescriptor(EdgeVisual edgeVisual)
	{
		String id = (String) edgeVisual.getId();
		TransitionDescription transitionDescription = _transitionDescriptionMap.get(id);
		if (transitionDescription == null)
		{
			_transitionDescriptionMap.put(id, transitionDescription);
			transitionDescription = new TransitionDescription();
			transitionDescription._relativeTargetConnectorPosition = new ArrayList<>(Collections.nCopies(edgeVisual.getTargetConnectors().size(), null));

			_transitionDescriptionMap.put(id, transitionDescription);
		}
		return transitionDescription;
	}

	/**
	 * Get the descriptor for a transition by the Edge id.
	 *
	 * @param id The id.
	 * @return The descriptor or null.
	 */
	public TransitionDescription getTransitionDescriptor(String id)
	{
		return _transitionDescriptionMap.get(id);
	}


	/**
	 * Creates a new empty instance.
	 *
	 * @param action The action of the change
	 */
	public EditorChanges(EditAction action)
	{
		addAction(action);
	}

	/**
	 * Adds an action to the changes.
	 *
	 * @param action The action, can be null which will be ignored.
	 */
	public void addAction(EditAction action)
	{
		if (action instanceof RenameStateAction renameStateAction)
		{
			_statesRenamed.put(renameStateAction._oldName, renameStateAction._newName);
			_command = Command.Rename;
		}
		else if (action instanceof MoveAction moveAction)
		{
			if (moveAction._what instanceof StateVisual stateVisual)
			{
				_command = Command.Move;
				_bounds.put(stateVisual._state._name, new PosAndBounds(stateVisual.getAbsolutePosition(), stateVisual.getAbsoluteBounds2D(null)));
			}
			else if (moveAction._what instanceof StartVisual startVisual)
			{
				_command = Command.Move;
				String name = (startVisual._parent == null) ? "xxx" : startVisual._parent.getCurrentName();
				_startBounds.put(name, new PosAndBounds(startVisual.getAbsolutePosition(), startVisual.getAbsoluteBounds2D(null)));
			}
			else if (moveAction._what instanceof ConnectorVisual connectorVisual)
			{
				if (connectorVisual.getParentVisual() instanceof StateVisual)
				{
					_command = Command.Move;
					EdgeVisual edgeVisual = connectorVisual.getEdgeVisual();
					while (edgeVisual.getParentEdge() != null)
						edgeVisual = edgeVisual.getParentEdge();

					TransitionDescription td = getTransitionDescriptor(edgeVisual);
					int idx = edgeVisual.getTargetConnectors().indexOf(connectorVisual);
					if (idx >= 0)
					{
						td._relativeTargetConnectorPosition.set(idx, moveAction._to);
					}
					else
					{
						td._relativeSourceConnectorPosition = moveAction._to;
					}
				}
			}
			else if (moveAction._what instanceof PathControlVisual pathControlVisual)
			{
				SingleTargetEdgeVisual edgeVisual = pathControlVisual.getEdgeVisual();
				TransitionDescription td = getTransitionDescriptor(edgeVisual);
				_command = Command.Move;
				td._pathControlPoints.clear();
				for (var cp : edgeVisual.getControlPoints())
				{
					td._pathControlPoints.add(cp.getAbsolutePosition());
				}
			}
		}
		else if (action instanceof EventChangeAction eventAction)
		{

			TransitionDescription td = getTransitionDescriptor((String) eventAction._what.getId());
			if (td != null)
			{
				td._events = eventAction._events;
				_command = Command.Events;
			}
		}
		else if (action instanceof EditActionList actionList)
		{
			actionList._actions.forEach(this::addAction);
		}
	}

	/**
	 * The command name of the change
	 */
	public Command _command;

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
