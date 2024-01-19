package com.bw.modelthings.fsm.ui;

import com.bw.graph.DrawContext;
import com.bw.graph.VisualModel;
import com.bw.graph.primitive.Circle;
import com.bw.graph.util.InsetsFloat;
import com.bw.graph.visual.ConnectorVisual;
import com.bw.graph.visual.EdgeVisual;
import com.bw.graph.visual.GenericVisual;
import com.bw.graph.visual.Visual;
import com.bw.modelthings.fsm.model.FiniteStateMachine;
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
public class GraphFactory
{
	/**
	 * Logger of this class.
	 */
	static final Logger log = Logger.getLogger(GraphFactory.class.getName());

	/**
	 * State visuals by state name.
	 */
	protected java.util.Map<String, Visual> stateVisuals = new HashMap<>();


	/**
	 * Extensions with layout information
	 */
	protected GraphExtension graphExtension;

	/**
	 * The text field to use as state name editor.
	 */
	protected JTextComponent stateNameTextField;

	/**
	 * Creates a new factory.
	 *
	 * @param graphExtension graph-extension handler or null.
	 */
	public GraphFactory(GraphExtension graphExtension)
	{
		this.graphExtension = graphExtension;
	}

	/**
	 * Sets the text editor component for state names.
	 *
	 * @param textEditor The editor component to use.
	 */
	public void setStateNameEditorComponent(JTextComponent textEditor)
	{
		stateNameTextField = textEditor;
	}

	/**
	 * Creates a visual for a start-node
	 *
	 * @param start  The parent state.
	 * @param x      Base X Position
	 * @param y      Base Y Position
	 * @param radius Radius of circle.
	 * @param style  The style to use.
	 * @return The visual
	 */
	public Visual createStartVisual(State start, float x, float y, float radius, DrawContext style)
	{
		GenericVisual startNode = new GenericVisual(null, style);
		Circle circle = new Circle(radius, radius, radius, style.configuration, null);
		circle.setFill(true);
		startNode.setPosition(x, y);
		startNode.addDrawingPrimitive(circle);
		startNode.setModified(false);
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
			Visual sourceVisual = stateVisuals.get(source.name);

			boolean toInnerModel = false;
			while (target != null && !source.parent.states.contains(target))
			{
				target = target.parent;
				toInnerModel = true;
			}
			if (target != null)
			{
				return createEdge(id, sourceVisual, stateVisuals.get(target.name), g2, style, toInnerModel);
			}
		}
		return null;
	}

	/**
	 * Creates an edge between two visuals.
	 * Only the edge to the state or parent in the same model is created.
	 *
	 * @param id           The Identification, can be null.
	 * @param source       The source-visual
	 * @param target       The target-visual
	 * @param g2           Graphics context for calculations.
	 * @param style        The style to use.
	 * @param toInnerModel True if the edge target the inner model of the state.
	 * @return The edge visual created.
	 */
	public EdgeVisual createEdge(Integer id, Visual source, Visual target, Graphics2D g2, DrawContext style, boolean toInnerModel)
	{
		if (source != null && target != null)
		{
			Rectangle2D.Float startBounds = source.getBounds2D(g2);

			ConnectorVisual sourceConnector = new ConnectorVisual(source, style);
			Rectangle2D.Float sourceConnectorBounds = sourceConnector.getBounds2D(g2);
			sourceConnector.setPosition(startBounds.width - (sourceConnectorBounds.width / 2f), (startBounds.height - sourceConnectorBounds.height) / 2f);
			sourceConnector.setModified(false);

			Rectangle2D.Float targetBounds = target.getBounds2D(g2);
			ConnectorVisual targetConnector = new ConnectorVisual(target, style);
			Rectangle2D.Float targetConnectorBounds = targetConnector.getBounds2D(g2);
			if (toInnerModel)
				targetConnector.setPosition(style.configuration.innerModelBoxInsets.left - targetConnectorBounds.width + (targetConnectorBounds.width / 2f), (targetBounds.height - targetConnectorBounds.height) / 2f);
			else
				targetConnector.setPosition(-targetConnectorBounds.width + (targetConnectorBounds.width / 2f), (targetBounds.height - targetConnectorBounds.height) / 2f);

			return new EdgeVisual(id, sourceConnector, targetConnector, style);
		}
		return null;
	}

	/**
	 * Creates the primitives for a state visual
	 *
	 * @param visual            The visual of the state.
	 * @param x                 Base X Position
	 * @param y                 Base Y Position
	 * @param state             State to create the visual for.
	 * @param g2                Graphics to use for dimension calculations.
	 * @param stateOuterContext The draw context for the outline.
	 * @param stateInnerContext The draw context for the inner drawings.
	 */
	public void createStatePrimitives(Visual visual, float x, float y, State state, Graphics2D g2, DrawContext stateOuterContext, DrawContext stateInnerContext)
	{
		new StateNameProxy(state, stateNameTextField, stateOuterContext, stateInnerContext)
				.createStatePrimitives(visual, x, y, g2, graphExtension.bounds.get(state.docId));
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
		VisualModel rootModel = new VisualModel();
		if (fsm != null && fsm.pseudoRoot != null)
		{
			java.util.Queue<State> states = new LinkedList<>();

			states.add(fsm.pseudoRoot);

			final float gapY = 5;
			float fh = stateOutlineStyles.normal.fontMetrics.getHeight();

			InsetsFloat insets = stateInnerStyles.configuration.innerModelBoxInsets;
			insets.top = fh * 2.5f;
			insets.bottom = fh;
			insets.left = fh;
			insets.right = fh;

			Rectangle2D.Float statePosition = new Rectangle2D.Float(fh, fh, 0, 0);
			statePosition.x += 2 * fh;

			final Map<String, Rectangle2D.Float> statePositions = new HashMap<>();
			statePositions.put(fsm.pseudoRoot.name, statePosition);

			final Map<String, State> statesByName = new HashMap<>();
			java.util.Map<Integer, Transition> transitions = new HashMap<>();
			while (!states.isEmpty())
			{
				State state = states.poll();
				if (!stateVisuals.containsKey(state.name))
				{
					statesByName.put(state.name, state);

					Visual stateVisual = new GenericVisual(state.name, stateInnerStyles);

					if (state.parent != null)
					{
						stateVisuals.get(state.parent.name).getSubModel().addVisual(stateVisual);
					}
					else
						rootModel.addVisual(stateVisual);

					stateVisuals.put(state.name, stateVisual);

					if (!state.states.isEmpty())
					{
						VisualModel subModel = new VisualModel();
						stateVisual.setSubModel(subModel);
						statePosition = new Rectangle2D.Float(3 * fh, fh, 0, 0);
						statePositions.put(state.name, statePosition);
						states.addAll(state.states);

					}
					for (Transition t : state.transitions.asList())
					{
						states.addAll(t.target);
						transitions.put(t.docId, t);
					}
				}
			}

			// We have now all information to create draw-primitives in the states-visuals.
			for (var visualEntry : stateVisuals.entrySet())
			{
				Visual visual = visualEntry.getValue();

				State state = statesByName.get(visualEntry.getKey());
				if (state.parent != null)
				{
					statePosition = statePositions.get(state.parent.name);
					createStatePrimitives(visual, statePosition.x, statePosition.y, state, g2, stateOutlineStyles, stateInnerStyles);

					Rectangle2D.Float bounds = visual.getBounds2D(g2);
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
				for (State target : t.target)
				{
					stateVisuals.get(t.source.parent.name)
								.getSubModel()
								.addVisual(
										createEdge(t.docId, t.source, target, g2, edgeStyles));
				}
			}

			// Create start-visuals for all sub-models.
			for (State state : statesByName.values())
			{
				if (!state.states.isEmpty())
				{
					Visual startVisual = createStartVisual(state, fh / 2, fh, fh / 2, startStyles);

					Visual stateVisual = stateVisuals.get(state.name);
					VisualModel innerModel = stateVisual.getSubModel();
					innerModel.addVisual(startVisual);

					List<State> initialStates = new ArrayList<>();

					Integer id;
					// Add initial transitions.
					if (state.initial != null)
					{
						initialStates.addAll(state.initial.target);
						id = state.initial.docId;
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
						while (initialState != null && !state.states.contains(initialState))
						{
							initialState = initialState.parent;
							toInnerModel = true;
						}
						Visual targetVisual = null;
						if (initialState != null)
						{
							targetVisual = stateVisuals.get(initialState.name);
						}
						if (targetVisual == null)
							log.warning(String.format("Target state %s of initial transition not found", initialState.name));
						else
						{
							innerModel.addVisual(createEdge(id, startVisual, targetVisual, g2, edgeStyles, toInnerModel));
						}
					}
				}
			}
		}
		rootModel.setModified(false);
		return rootModel;
	}

}
