package com.bw.modelthings.fsm.ui;

import com.bw.graph.Alignment;
import com.bw.graph.DrawContext;
import com.bw.graph.VisualModel;
import com.bw.graph.primitive.ModelPrimitive;
import com.bw.graph.util.InsetsFloat;
import com.bw.graph.visual.ConnectorVisual;
import com.bw.graph.visual.MultiTargetEdgeVisual;
import com.bw.graph.visual.Visual;
import com.bw.graph.visual.VisualFlags;
import com.bw.modelthings.fsm.model.FiniteStateMachine;
import com.bw.modelthings.fsm.model.PseudoRoot;
import com.bw.modelthings.fsm.model.State;
import com.bw.modelthings.fsm.model.Transition;

import javax.swing.text.JTextComponent;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Factory to create Graph visuals from an FSM model.<br>
 * The class is not thread safe, use different instances in different threads.
 */
public class FsmGraphFactory
{

	/**
	 * Logger of this class.
	 */
	static final Logger log = Logger.getLogger(FsmGraphFactory.class.getName());

	/**
	 * State visuals by state name.
	 */
	protected java.util.Map<String, StateVisual> _stateVisuals = new HashMap<>();


	/**
	 * Extensions with layout information
	 */
	protected ScxmlGraphExtension _graphExtension;

	/**
	 * The text field to use as state name editor.
	 */
	protected JTextComponent _stateNameTextField;

	/**
	 * Creates a new factory.
	 *
	 * @param graphExtension graph-extension handler or null.
	 */
	public FsmGraphFactory(ScxmlGraphExtension graphExtension)
	{
		this._graphExtension = graphExtension;
	}

	/**
	 * Sets the text editor component for state names.
	 *
	 * @param textEditor The editor component to use.
	 */
	public void setStateNameEditorComponent(JTextComponent textEditor)
	{
		_stateNameTextField = textEditor;
	}

	/**
	 * Creates a visual for a start-node
	 *
	 * @param parent The parent state.
	 * @param x      Base X Position
	 * @param y      Base Y Position
	 * @param radius Radius of circle.
	 * @param style  The style to use.
	 * @return The visual
	 */
	public Visual createStartVisual(StateVisual parent, float x, float y, float radius, DrawContext style)
	{
		StartVisual startNode = new StartVisual(parent, style);
		startNode.createPrimitives(x, y, radius, _graphExtension._startBounds.get(parent._state._docId), style);
		return startNode;
	}

	/**
	 * Creates an edge for a Transition.
	 * Only the edge to the state or parent in the same model is created.
	 *
	 * @param id         The Identification, can be null.
	 * @param transition The transition represented by this edge.
	 * @param g2         Graphics context for calculations.
	 * @param style      The style to use.
	 * @return The edge visual created.
	 */
	public MultiTargetEdgeVisual createEdge(String id, Transition transition, Graphics2D g2, DrawContext style)
	{
		if (transition._source != null && transition._target != null && !transition._target.isEmpty())
		{
			StateVisual sourceVisual = _stateVisuals.get(transition._source._name);

			List<AbstractMap.SimpleEntry<StateVisual, StateVisual>> targets = new ArrayList<>(transition._target.size());

			for (State targetState : transition._target)
			{
				StateVisual targetedVisual = _stateVisuals.get(targetState._name);

				boolean toInnerModel = false;
				while (targetState != null && !transition._source._parent._states.contains(targetState))
				{
					targetState = targetState._parent;
					toInnerModel = true;
				}
				if (targetState != null)
				{
					targets.add(new AbstractMap.SimpleEntry<>(_stateVisuals.get(targetState._name),
							toInnerModel ? targetedVisual : null));
				}
			}
			if (!targets.isEmpty())
			{
				return createEdge(id, sourceVisual, transition, targets, g2, style);
			}
		}
		return null;
	}

	/**
	 * Creates an edge between two visuals.
	 * Only the edge to the state or parent in the same model is created.
	 *
	 * @param id         The Identification, can be null.
	 * @param source     The source-visual
	 * @param transition The transition represented by this edge.
	 * @param targets    Pairs of target-visual and (optional) the inner child of the target visual that is the real target.
	 * @param g2         Graphics context for calculations.
	 * @param style      The style to use.
	 * @return The edge visual created.
	 */
	public MultiTargetEdgeVisual createEdge(String id, Visual source, Transition transition, List<AbstractMap.SimpleEntry<StateVisual, StateVisual>> targets, Graphics2D g2, DrawContext style)
	{
		MultiTargetEdgeVisual edgeVisual;
		if (source != null && targets != null && !targets.isEmpty())
		{
			ConnectorVisual sourceConnector = new TransitionSourceControlVisual(source, transition, style, VisualFlags.ALWAYS);
			List<ConnectorVisual> targetConnectors = targets.stream().map(targetPair -> {
				ConnectorVisual cv = new ConnectorVisual(targetPair.getKey(), style, VisualFlags.ALWAYS);
				cv.setTargetedParentChild(targetPair.getValue());
				return cv;
			}).collect(Collectors.toList());

			edgeVisual = new MultiTargetEdgeVisual(id, sourceConnector, targetConnectors, style);
		}
		else
			edgeVisual = null;
		return edgeVisual;
	}

	/**
	 * Creates a visual model for a state machine.
	 *
	 * @param fsm                The source state machine
	 * @param g2                 Graphics to use for calculations.
	 * @param startStyles        The styles for start-states.
	 * @param stateOutlineStyles The styles for states outlines.
	 * @param stateInnerStyles   The styles for states inner drawings.
	 * @param edgeStyles         The styles for edges.
	 * @return The model.
	 */
	public VisualModel createVisualModel(FiniteStateMachine fsm, Graphics2D g2,
										 DrawContext startStyles,
										 DrawContext stateOutlineStyles,
										 DrawContext stateInnerStyles,
										 DrawContext edgeStyles)
	{
		VisualModel rootModel = new VisualModel(fsm == null ? "none" : fsm._name);
		if (fsm != null && fsm._pseudoRoot != null)
		{
			java.util.Queue<State> states = new LinkedList<>();

			states.add(fsm._pseudoRoot);

			final float gapY = 5;
			float fh = stateOutlineStyles._style.getFontMetrics().getHeight();

			InsetsFloat insets = stateInnerStyles._configuration._innerModelBoxInsets;
			insets._top = fh * 2.5f;
			insets._bottom = fh;
			insets._left = fh;
			insets._right = fh;

			Rectangle2D.Float statePosition = new Rectangle2D.Float(fh, fh, 0, 0);
			statePosition.x += 2 * fh;

			final Map<String, Rectangle2D.Float> statePositions = new HashMap<>();
			statePositions.put(fsm._pseudoRoot._name, statePosition);

			final Map<String, State> statesByName = new HashMap<>();
			java.util.Map<Integer, Transition> transitions = new HashMap<>();
			while (!states.isEmpty())
			{
				State state = states.poll();
				if (!_stateVisuals.containsKey(state._name))
				{
					statesByName.put(state._name, state);

					StateVisual stateVisual = new StateVisual(state, _stateNameTextField, stateOutlineStyles, stateInnerStyles);
					if (state instanceof PseudoRoot pseudoRoot)
					{
						stateVisual.setDisplayName(pseudoRoot._fsmName);
					}

					if (state._parent != null)
					{
						VisualModel model = _stateVisuals.get(state._parent._name)
														 .getChildModel();
						model.addVisual(stateVisual);
					}
					else
						rootModel.addVisual(stateVisual);

					_stateVisuals.put(state._name, stateVisual);

					if (!state._states.isEmpty())
					{
						String modelName = state._name;
						if (state instanceof PseudoRoot pseudoRoot && pseudoRoot._fsmName != null)
						{
							modelName = pseudoRoot._fsmName;
						}
						VisualModel subModel = new VisualModel(modelName);
						ModelPrimitive modelPrimitive = new ModelPrimitive(0, 0, stateInnerStyles._configuration, stateInnerStyles._style, VisualFlags.ALWAYS);
						modelPrimitive.setAlignment(Alignment.Center);
						modelPrimitive.setInsets(stateInnerStyles._configuration._innerModelBoxInsets);
						modelPrimitive.setChildModel(subModel);
						_stateVisuals.get(state._name)
									 .addDrawingPrimitive(modelPrimitive);
						statePosition = new Rectangle2D.Float(3 * fh, fh, 0, 0);
						statePositions.put(state._name, statePosition);
						states.addAll(state._states);

					}
					for (Transition t : state._transitions)
					{
						states.addAll(t._target);
						transitions.put(t._docId, t);
					}
				}
			}

			// We have now all information to create draw-primitives in the states-visuals.
			for (var visualEntry : _stateVisuals.entrySet())
			{
				StateVisual visual = visualEntry.getValue();

				State state = statesByName.get(visualEntry.getKey());
				if (state._parent != null)
				{
					statePosition = statePositions.get(state._parent._name);
					visual.createStatePrimitives(statePosition.x, statePosition.y, g2, _graphExtension._bounds.get(state._docId),
							null);

					Rectangle2D.Float bounds = visual.getAbsoluteBounds2D(g2);
					if (bounds != null)
					{
						statePosition.y += bounds.height + gapY;
						if (bounds.width > statePosition.width)
							statePosition.width = bounds.width;
						if (statePosition.y > 600)
						{
							statePosition.x += statePosition.width + fh;
							statePosition.y = fh;
							statePosition.width = 0;
						}
					}
				}
			}

			List<AbstractMap.SimpleEntry<StateVisual, StateVisual>> targetVisuals = new ArrayList<>();

			// Create edges
			for (Transition t : transitions.values())
			{
				MultiTargetEdgeVisual edgeVisual = createEdge(t._xmlId, t, g2, edgeStyles);

				if (edgeVisual != null)
				{
					TransitionDescription td = _graphExtension.getTransitionDescriptor(t._docId);

					getModelForState(t._source).addVisual(edgeVisual);
					ConnectorVisual sourceConnector = edgeVisual.getSourceConnector();

					if (td._relativeSourceConnectorPosition != null)
					{
						sourceConnector.setRelativePosition(td._relativeSourceConnectorPosition.x, td._relativeSourceConnectorPosition.y);
					}

					getModelForVisual((StateVisual) sourceConnector.getParent()).getVisuals()
																				.add(sourceConnector);

					final List<ConnectorVisual> targetConnectorVisuals = edgeVisual.getTargetConnectors();

					if (td._relativeTargetConnectorPosition != null)
					{
						int tCN = td._relativeTargetConnectorPosition.size();
						if (targetConnectorVisuals.size() == tCN)
						{
							for (int i = 0; i < tCN; ++i)
							{
								Point2D.Float pt = td._relativeTargetConnectorPosition.get(i);
								targetConnectorVisuals.get(i).setRelativePosition(pt.x, pt.y);
							}
						}
					}
					targetConnectorVisuals.forEach(targetConnector ->
							getModelForVisual((StateVisual) targetConnector.getParent()).getVisuals()
																						.add(targetConnector));
				}
			}

			// Create start-visuals for all sub-models.
			for (State state : statesByName.values())
			{
				if (!state._states.isEmpty())
				{
					StateVisual stateVisual = _stateVisuals.get(state._name);
					Visual startVisual = createStartVisual(stateVisual, fh / 2, fh, fh / 2, startStyles);

					VisualModel innerModel = ModelPrimitive.getChildModel(stateVisual);
					innerModel.addVisual(startVisual);

					List<State> initialStates = new ArrayList<>();

					String id;
					// Add initial transitions.
					if (state._initial != null)
					{
						initialStates.addAll(state._initial._target);
						id = state._initial._xmlId;
					}
					else
					{
						initialStates.add(state.getInnerStatesInDocumentOrder()
											   .get(0));
						id = null;
					}
					targetVisuals.clear();
					for (State initialState : initialStates)
					{
						boolean toInnerModel = false;
						StateVisual targetedVisual = _stateVisuals.get(initialState._name);
						while (initialState != null && !state._states.contains(initialState))
						{
							initialState = initialState._parent;
							toInnerModel = true;
						}
						StateVisual targetVisual = null;
						if (initialState != null)
						{
							targetVisual = _stateVisuals.get(initialState._name);
						}
						if (targetVisual == null)
							log.warning(String.format("Target state %s of initial transition not found", initialState == null ? "null" : initialState._name));
						else
						{
							targetVisuals.add(new AbstractMap.SimpleEntry<>(targetVisual, toInnerModel ? targetedVisual : null));
						}
					}
					innerModel.addVisual(createEdge(id, startVisual, null, targetVisuals, g2, edgeStyles));
				}
			}

			// Place all connectors
			// "Start" connectors are placed at default relative position and doesn't need
			// to be updated.
			for (StateVisual stateVisual : _stateVisuals.values())
			{
				VisualModel model;
				if (stateVisual._state._parent == null)
					model = rootModel;
				else
					model = _stateVisuals.get(stateVisual._state._parent._name)
										 .getChildModel();
				stateVisual.placeConnectors(model.getEdgesAt(stateVisual), g2);
			}
		}
		rootModel.clearFlags(VisualFlags.MODIFIED);
		return rootModel;
	}

	private VisualModel getModelForVisual(StateVisual visual)
	{
		return ModelPrimitive.getChildModel(_stateVisuals.get(visual._state._parent._name));
	}

	private VisualModel getModelForState(State state)
	{
		return ModelPrimitive.getChildModel(_stateVisuals.get(state._parent._name));
	}

}
