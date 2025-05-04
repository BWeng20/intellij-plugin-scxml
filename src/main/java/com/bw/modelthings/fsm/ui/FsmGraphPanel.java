package com.bw.modelthings.fsm.ui;

import com.bw.graph.DrawStyle;
import com.bw.graph.SimpleDrawStyle;
import com.bw.graph.VisualModel;
import com.bw.graph.editor.GraphPane;
import com.bw.graph.editor.action.EditAction;
import com.bw.graph.primitive.ModelPrimitive;
import com.bw.graph.visual.Visual;
import com.bw.modelthings.fsm.model.FiniteStateMachine;
import com.bw.modelthings.fsm.model.State;
import com.bw.modelthings.fsm.ui.swing.EditorUISwingManager;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

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
	protected SimpleDrawStyle _stateOutlineStyle = new SimpleDrawStyle();

	/**
	 * Style for inner drawings in the states.
	 * Needs to be configured by platform-specific inheritance.
	 */
	protected SimpleDrawStyle _stateInnerStyle = new SimpleDrawStyle();

	protected SimpleDrawStyle _stateFocusStyle = new SimpleDrawStyle();

	/**
	 * Style for start nodes.
	 * Needs to be configured by platform-specific inheritance.
	 */
	protected SimpleDrawStyle _startStyle = new SimpleDrawStyle();

	/**
	 * Context for state outline.
	 */
	protected DrawStyle _stateOutlineContext = _stateOutlineStyle;

	/**
	 * The context for inner drawing for states.
	 */
	protected DrawStyle _stateInnerContext = _stateInnerStyle;

	protected DrawStyle _stateFocusContext = _stateFocusStyle;

	/**
	 * Context for edges.
	 */
	protected DrawStyle _edgeContext = _stateInnerStyle;

	protected FsmGraphBuilder _factory;

	public static final String STYLE_START_LINE = "START_LINE";
	public static final String STYLE_START_FILL = "START_FILL";
	public static final String STYLE_STATE_OUTLINE_LINE = "STATE_OUTLINE_LINE";
	public static final String STYLE_STATE_OUTLINE_FILL = "STATE_OUTLINE_FILL";
	public static final String STYLE_STATE_OUTLINE_TEXT = "STATE_OUTLINE_TEXT";
	public static final String STYLE_STATE_OUTLINE_FONT = "STATE_OUTLINE_FONT";
	public static final String STYLE_STATE_OUTLINE_FONT_METRICS = "STATE_OUTLINE_FONT_METRICS";

	public void setStyle(Map<String, Object> style)
	{
		final Paint foreground = getForeground();
		final Paint background = getBackground();

		_startStyle._linePaint = (Paint) style.getOrDefault(STYLE_START_LINE, foreground);
		_startStyle._fillPaint = (Paint) style.getOrDefault(STYLE_START_FILL, background);

		_stateOutlineStyle._linePaint = (Paint) style.getOrDefault(STYLE_STATE_OUTLINE_LINE, foreground);
		_stateOutlineStyle._fillPaint = (Paint) style.getOrDefault(STYLE_STATE_OUTLINE_FILL, background);

		_stateOutlineStyle._lineStroke = new BasicStroke(2);
		_stateOutlineStyle._textPaint = (Paint) style.getOrDefault(STYLE_STATE_OUTLINE_TEXT, foreground);


		Font font = (Font) style.get(STYLE_STATE_OUTLINE_FONT);
		if (font == null)
			font = getFont();
		FontMetrics fontMetrics = (FontMetrics) style.get(STYLE_STATE_OUTLINE_FONT_METRICS);
		if (fontMetrics == null)
			fontMetrics = getFontMetrics(font);

		_stateOutlineStyle._font = font;
		_stateOutlineStyle._fontMetrics = fontMetrics;

		_stateInnerStyle._linePaint = _stateOutlineStyle.getLinePaint();
		_stateInnerStyle._fillPaint = _stateOutlineStyle.getFillPaint();
		_stateInnerStyle._lineStroke = new BasicStroke(1);
		_stateInnerStyle._textPaint = _stateOutlineStyle.getTextPaint();

		_stateInnerStyle._background = background;
		_stateInnerStyle._font = font;
		_stateInnerStyle._fontMetrics = fontMetrics;

		// TODO: configuration!
		_stateFocusStyle._linePaint = Color.GRAY;
		_stateFocusStyle._fillPaint = _stateOutlineStyle.getFillPaint();
		_stateFocusStyle._lineStroke = new BasicStroke(2.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[]{5}, 0.0f);
		_stateFocusStyle._textPaint = _stateOutlineStyle.getTextPaint();
	}


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
	 * Adds a state.
	 *
	 * @param stateName The name of the state.
	 */
	public State addState(State parent, String stateName, Point2D.Float p)
	{
		if (_fsm != null)
		{
			if (_fsm._states.containsKey(stateName))
			{
				System.out.println("Name already exists");
				return null;
			}

			State state = new State();
			state._name = stateName;
			state._docId = _fsm.createDocId();

			if (parent == null)
			{
				parent = _fsm._pseudoRoot;
			}
			parent.addState(state);

			_fsm._states.put(stateName, state);
			_factory.createVisuals(null, state, (Graphics2D) _pane.getGraphics(),
					_graphConfiguration, _startStyle, _stateOutlineStyle, _stateInnerStyle, _stateFocusStyle, _edgeContext);

			StateVisual visual = _pane.getStateVisual(state);
			if (visual != null)
			{
				visual.setAbsolutePosition(p, null);
			}
			SwingUtilities.invokeLater(() ->
			{
				repaint();
			});
			return state;
		}
		return null;
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
		_factory = new FsmGraphBuilder(graphExtension, new EditorUISwingManager());

		_pane.setModel(null);
		if (_root != null)
		{
			_root.dispose();
			_root = null;
		}
		this._fsm = fsm;
		this._graphExtension = graphExtension;
		VisualModel rootModel =
				_factory.createVisualModel(fsm, (Graphics2D) _pane.getGraphics(),
						_graphConfiguration, _startStyle, _stateOutlineContext, _stateInnerContext, _stateFocusContext, _edgeContext);

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
