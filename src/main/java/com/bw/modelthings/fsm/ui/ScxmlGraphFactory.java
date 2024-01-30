package com.bw.modelthings.fsm.ui;

import com.bw.graph.Alignment;
import com.bw.graph.DrawContext;
import com.bw.graph.VisualModel;
import com.bw.graph.primitive.Circle;
import com.bw.graph.primitive.ModelPrimitive;
import com.bw.graph.util.InsetsFloat;
import com.bw.graph.visual.ConnectorVisual;
import com.bw.graph.visual.EdgeVisual;
import com.bw.graph.visual.GenericPrimitiveVisual;
import com.bw.graph.visual.Visual;
import com.bw.graph.visual.VisualFlags;
import com.bw.modelthings.fsm.model.FiniteStateMachine;
import com.bw.modelthings.fsm.model.PseudoRoot;
import com.bw.modelthings.fsm.model.State;
import com.bw.modelthings.fsm.model.Transition;

import javax.swing.text.JTextComponent;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Factory to create Graph visuals from an FSM model.<br>
 * The class is not thread safe, use different instances in different threads.
 */
public class ScxmlGraphFactory
{
	/**
	 * Visual flag for a start-node.
	 */
	public final static int START_NODE_FLAG = 128;

	/**
	 * Logger of this class.
	 */
	static final Logger log = Logger.getLogger(ScxmlGraphFactory.class.getName());

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
	public ScxmlGraphFactory(ScxmlGraphExtension graphExtension)
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
	public Visual createStartVisual(State parent, float x, float y, float radius, DrawContext style)
	{
		GenericPrimitiveVisual startNode = new GenericPrimitiveVisual(parent._docId, style);
		Circle circle = new Circle(0, 0, radius, style._configuration, style._style, VisualFlags.ALWAYS);
		circle.setFill(true);
		startNode.addDrawingPrimitive(circle);
		Circle circleActive = new Circle(0, 0, radius + 5, style._configuration, style._style, VisualFlags.SELECTED);
		startNode.addDrawingPrimitive(circleActive);
		startNode.clearFlags(VisualFlags.MODIFIED);
		startNode.setFlags(START_NODE_FLAG);

		PosAndBounds startBounds = _graphExtension._startBounds.get(parent._docId);
		if (startBounds == null)
		{
			startNode.setAbsolutePosition(x, y, null);
		}
		else
		{
			startNode.setAbsolutePosition(startBounds.position, startBounds.bounds);
		}
		return startNode;
	}

	/**
	 * Creates an edge between two states.
	 * Only the edge to the state or parent in the same model is created.
	 *
	 * @param id     The Identification, can be null.
	 * @param source The source-state
	 * @param target The target-stare
	 * @param g2     Graphics context for calculations.
	 * @param style  The style to use.
	 * @return The edge visual created.
	 */
	public EdgeVisual createEdge(Integer id, State source, State target, Graphics2D g2, DrawContext style)
	{
		if (source != null && target != null)
		{
			StateVisual sourceVisual = _stateVisuals.get(source._name);
			StateVisual targetedVisual = _stateVisuals.get(target._name);

			boolean toInnerModel = false;
			while (target != null && !source._parent._states.contains(target))
			{
				target = target._parent;
				toInnerModel = true;
			}
			if (target != null)
			{
				return createEdge(id, sourceVisual, _stateVisuals.get(target._name), g2, style, toInnerModel ? targetedVisual : null);
			}
		}
		return null;
	}

	/**
	 * Creates an edge between two visuals.
	 * Only the edge to the state or parent in the same model is created.
	 *
	 * @param id            The Identification, can be null.
	 * @param source        The source-visual
	 * @param target        The target-visual
	 * @param g2            Graphics context for calculations.
	 * @param style         The style to use.
	 * @param targetedChild If not null the inner child of the target visual that is the real target.
	 * @return The edge visual created.
	 */
	public EdgeVisual createEdge(Integer id, Visual source, StateVisual target, Graphics2D g2, DrawContext style, Visual targetedChild)
	{
		EdgeVisual edgeVisual;
		if (source != null && target != null)
		{
			ConnectorVisual sourceConnector = new ConnectorVisual(source, style, VisualFlags.ALWAYS);
			ConnectorVisual targetConnector = new ConnectorVisual(target, style, VisualFlags.ALWAYS);
			targetConnector.setTargetedParentChild(targetedChild);

			edgeVisual = new EdgeVisual(id, sourceConnector, targetConnector, style);

			if (source instanceof StateVisual stateVisual)
				stateVisual.placeConnector(edgeVisual, g2);
			// Otherwise a "start" source with relative 0,0 position.

			target.placeConnector(edgeVisual, g2);
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
			float fh = stateOutlineStyles._style._fontMetrics.getHeight();

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
						VisualModel model = _stateVisuals.get(state._parent._name).getChildModel();
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
					visual.createStatePrimitives(statePosition.x, statePosition.y, g2, _graphExtension._bounds.get(state._docId), null);

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

			// Create edges
			for (Transition t : transitions.values())
			{
				for (State target : t._target)
				{
					ModelPrimitive.getChildModel(_stateVisuals.get(t._source._parent._name))
								  .addVisual(
										  createEdge(t._docId, t._source, target, g2, edgeStyles));
				}
			}

			// Create start-visuals for all sub-models.
			for (State state : statesByName.values())
			{
				if (!state._states.isEmpty())
				{
					Visual startVisual = createStartVisual(state, fh / 2, fh, fh / 2, startStyles);

					StateVisual stateVisual = _stateVisuals.get(state._name);
					VisualModel innerModel = ModelPrimitive.getChildModel(stateVisual);
					innerModel.addVisual(startVisual);

					List<State> initialStates = new ArrayList<>();

					Integer id;
					// Add initial transitions.
					if (state._initial != null)
					{
						initialStates.addAll(state._initial._target);
						id = state._initial._docId;
					}
					else
					{
						initialStates.add(state.getInnerStatesInDocumentOrder()
											   .get(0));
						id = null;
					}
					for (State initialState : initialStates)
					{
						boolean toInnerModel = false;
						Visual targetedVisual = _stateVisuals.get(initialState._name);
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
							log.warning(String.format("Target state %s of initial transition not found", initialState._name));
						else
						{
							innerModel.addVisual(createEdge(id, startVisual, targetVisual, g2, edgeStyles,
									toInnerModel ? targetedVisual : null));
						}
					}
				}
			}
		}
		rootModel.clearFlags(VisualFlags.MODIFIED);
		return rootModel;
	}

}
