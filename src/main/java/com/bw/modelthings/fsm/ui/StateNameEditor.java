package com.bw.modelthings.fsm.ui;

import com.bw.graph.VisualModel;
import com.bw.graph.editor.Editor;
import com.bw.graph.editor.action.EditAction;
import com.bw.graph.visual.VisualFlags;
import com.bw.modelthings.fsm.ui.actions.RenameStateAction;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.Objects;

/**
 * Proxy to provide state name editor.
 */
class StateNameEditor<C> implements Editor
{
	private final StateVisual stateVisual;

	/**
	 * The editor component.
	 */
	public StateNameEditorUI<C> _ui;

	/**
	 * Create a new state proxy.
	 *
	 * @param ui The editor component.
	 */
	public StateNameEditor(StateVisual stateVisual, StateNameEditorUI<C> ui)
	{
		this.stateVisual = stateVisual;
		this._ui = ui;
	}

	@Override
	public String toString()
	{
		return stateVisual._state._name;
	}

	@Override
	public C getEditor()
	{
		_ui.setStateVisual(stateVisual);
		return _ui.getComponent();
	}

	/**
	 * Commits the edited text, updates the text-primitive and the Layout of the StateVisual.
	 *
	 * @param model The model.
	 * @param g2    Graphics context for calculations.
	 */
	@Override
	public EditAction endEdit(VisualModel model, Graphics2D g2)
	{
		EditAction action;

		String newName = _ui.getStateName().trim();
		if (!Objects.equals(newName, stateVisual._state._name))
		{
			action = new RenameStateAction(stateVisual._state._name, newName);

			stateVisual._state._name = newName;
			stateVisual.setFlags(VisualFlags.MODIFIED);
			Point2D.Float pt = stateVisual.getAbsolutePosition();
			stateVisual.createStatePrimitives(pt.x, pt.y, g2, null);
			stateVisual.setPreferredDimension(null);
			stateVisual.placeConnectors(model.getEdgesAt(stateVisual), g2);

		}
		else
			action = null;
		return action;
	}

	@Override
	public void cancelEdit()
	{
	}

	@Override
	public boolean isInPlace()
	{
		return true;
	}
}
