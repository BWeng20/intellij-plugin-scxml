package com.bw.modelthings.fsm.ui;

import com.bw.graph.editor.action.EditAction;
import com.bw.graph.editor.action.MoveAction;
import com.bw.graph.visual.ConnectorVisual;
import com.bw.graph.visual.EdgeVisual;
import com.bw.graph.visual.PathControlVisual;
import com.bw.modelthings.fsm.ui.actions.RenameStateAction;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds a current changes in editor.
 */
public class EditorChanges
{
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
			else if (moveAction._what instanceof ConnectorVisual connectorVisual)
			{
				if (connectorVisual.getParent() instanceof StateVisual)
				{
					_commandName = "Move";
					EdgeVisual edgeVisual = connectorVisual.getEdgeVisual();
					TransitionDescription td = getTransitionDescriptor(edgeVisual);
					if (edgeVisual.getTargetConnector() == connectorVisual)
					{
						td._relativeTargetConnector = moveAction._to;
					}
					else
					{
						td._relativeSourceConnector = moveAction._to;
					}
				}
			}
			else if (moveAction._what instanceof PathControlVisual pathControlVisual)
			{
				EdgeVisual edgeVisual = pathControlVisual.getEdgeVisual();
				TransitionDescription td = getTransitionDescriptor(edgeVisual);
				_commandName = "Move";
				td._pathControlPoints.clear();
				for (var cp : edgeVisual.getControlPoints())
				{
					td._pathControlPoints.add(cp.getAbsolutePosition());
				}
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
