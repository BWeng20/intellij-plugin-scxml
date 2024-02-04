package com.bw.modelthings.fsm.ui;

import com.bw.graph.DrawContext;
import com.bw.graph.DrawStyle;
import com.bw.graph.VisualModel;
import com.bw.graph.editor.GraphPane;
import com.bw.graph.editor.action.EditAction;
import com.bw.graph.primitive.ModelPrimitive;
import com.bw.graph.visual.Visual;
import com.bw.modelthings.fsm.model.FiniteStateMachine;
import com.bw.modelthings.fsm.model.State;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;
import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Panel to show the FSM as Graphical State Machine.
 */
public class FsmGraphPanel extends JPanel
{
	/**
	 * The in-place editor for state-names.
	 */
	protected JTextComponent _stateNameEditorComponent = new JTextField();

	/**
	 * Root model.
	 */
	protected Visual _root;

	/**
	 * The fsm specific graph configuration.
	 */
	protected FsmGraphConfiguration _graphConfiguration = new FsmGraphConfiguration();

	/**
	 * The generic graph pane.
	 */
	protected GraphPane _pane = new GraphPane(_graphConfiguration);

	/**
	 * The FSM.
	 */
	protected FiniteStateMachine _fsm;

	/**
	 * The extension from parser that was used for the current FSM.
	 */
	protected ScxmlGraphExtension _graphExtension;

	/**
	 * Style for state outline.
	 * Needs to be configured by platform-specific inheritance.
	 */
	protected DrawStyle _stateOutlineStyle = new DrawStyle();

	/**
	 * Style for inner drawings in the states.
	 * Needs to be configured by platform-specific inheritance.
	 */
	protected DrawStyle _stateInnerStyle = new DrawStyle();

	/**
	 * Style for start nodes.
	 * Needs to be configured by platform-specific inheritance.
	 */
	protected DrawStyle _startStyle = new DrawStyle();

	/**
	 * Context for state outline.
	 */
	protected DrawContext _stateOutlineContext = new DrawContext(_pane.getGraphConfiguration(), _stateOutlineStyle);

	/**
	 * The context for inner drawing for states.
	 */
	protected DrawContext _stateInnerContext = new DrawContext(_pane.getGraphConfiguration(), _stateInnerStyle);

	/**
	 * Context for edges.
	 */
	protected DrawContext _edgeContext = new DrawContext(_pane.getGraphConfiguration(), _stateInnerStyle);

	/**
	 * Context for start node.
	 */
	protected DrawContext _startContext = new DrawContext(_pane.getGraphConfiguration(), _startStyle);

	/**
	 * Clean up resources.
	 */
	public void dispose()
	{
		if (_fsm != null)
		{
			_fsm.dispose();
			_fsm = null;
		}
		_graphExtension = null;
		_pane.dispose();
	}

	/**
	 * Get the SVG for the current model.
	 *
	 * @return The SVG source code.
	 */
	public String getSVG()
	{
		return _pane.toSVG();
	}

	/**
	 * Get the state of the currently selected visual.
	 *
	 * @return The state or null.
	 */
	public State getSelectedState()
	{
		return getState(_pane.getSelectedVisual());
	}

	/**
	 * Get the state of a state-visual.
	 *
	 * @param v The visual.
	 * @return The state or null if the visual contains no state.
	 */
	public State getState(Visual v)
	{
		if (v instanceof StateVisual stateVisual)
		{
			return stateVisual.getState();
		}
		return null;
	}

	/**
	 * Remove a state.
	 *
	 * @param state           The state.
	 * @param keepChildStates If true child-states and internal transitions are moved to parent-state.
	 */
	public void removeState(State state, boolean keepChildStates)
	{
		if (_fsm != null)
		{
			_fsm.remove(state, keepChildStates);
			setStateMachine(_fsm, _graphExtension);
		}
	}


	/**
	 * Extracts the name of the state behind the visual.
	 *
	 * @param stateVisual The state visual.
	 * @return The name or null - if the visual is no state or if the name is not set.
	 */
	public String getNameOfState(Visual stateVisual)
	{
		State state = getState(stateVisual);
		return state == null ? null : state._name;
	}

	/**
	 * Get the graph configuration.
	 *
	 * @return The graph configuration.
	 */
	public FsmGraphConfiguration getGraphConfiguration()
	{
		return _graphConfiguration;
	}

	/**
	 * Gets the root node visual.
	 *
	 * @return The visual or null.
	 */
	public Visual getRootVisual()
	{
		return _root;
	}

	/**
	 * Create a new editor panel.
	 */
	public FsmGraphPanel()
	{
		super(new BorderLayout());
		add(new JScrollPane(_pane), BorderLayout.CENTER);
	}

	/**
	 * Gets the current state machine
	 *
	 * @return The state machine or null
	 */
	public FiniteStateMachine getStateMachine()
	{
		return _fsm;
	}

	/**
	 * Gets the graph-extension used to create the FSM.
	 *
	 * @return The extension or null.
	 */
	public ScxmlGraphExtension getGraphExtension()
	{
		return _graphExtension;
	}

	/**
	 * Get the editor updates and commits all changes in the model.
	 *
	 * @return The updates. Never null.
	 */
	public List<EditorChanges> getEditorUpdate()
	{
		Deque<EditAction> editActions = _pane.getEditActions();
		List<EditorChanges> updates = new ArrayList<>();

		for (EditAction e : editActions)
		{
			updates.add(new EditorChanges(e));
		}
		_pane.commitActions();
		return updates;
	}

	/**
	 * Gets the visual for the start-node of the model.
	 *
	 * @param model The model.
	 * @return The start node or null.
	 */
	public static Visual getStartVisual(VisualModel model)
	{
		return model.getVisuals()
					.stream()
					.filter(v -> v.isFlagSet(FsmVisualFlags.START_NODE_FLAG))
					.findFirst()
					.orElse(null);
	}


	/**
	 * Sets the FSM to show.
	 *
	 * @param fsm            The FSM to show.
	 * @param graphExtension The graph-extension or null
	 */
	public void setStateMachine(FiniteStateMachine fsm, ScxmlGraphExtension graphExtension)
	{
		FsmGraphFactory factory = new FsmGraphFactory(graphExtension);
		factory.setStateNameEditorComponent(_stateNameEditorComponent);

		_pane.setModel(null);
		if (_root != null)
		{
			_root.dispose();
			_root = null;
		}
		this._fsm = fsm;
		this._graphExtension = graphExtension;
		VisualModel rootModel =
				factory.createVisualModel(fsm, (Graphics2D) _pane.getGraphics(),
						_startContext, _stateOutlineContext, _stateInnerContext, _edgeContext);

		if (!rootModel.getVisuals()
					  .isEmpty())
		{
			_root = rootModel.getVisuals()
							 .get(0);
			_pane.setModel(ModelPrimitive.getChildModel(_root));
		}
		else
		{
			_root = null;
			_pane.setModel(null);
		}
	}

	/**
	 * Gets the graph pane.
	 *
	 * @return The pane.
	 */
	public GraphPane getGraphPane()
	{
		return _pane;
	}

}
