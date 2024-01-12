package com.bw.modeldrive.fsm.ui;

import com.bw.graph.Alignment;
import com.bw.graph.DrawContext;
import com.bw.graph.VisualModel;
import com.bw.graph.primitive.Circle;
import com.bw.graph.primitive.Line;
import com.bw.graph.primitive.Rectangle;
import com.bw.graph.primitive.Text;
import com.bw.graph.util.Dimension2DFloat;
import com.bw.graph.util.InsetsFloat;
import com.bw.graph.visual.ConnectorVisual;
import com.bw.graph.visual.EdgeVisual;
import com.bw.graph.visual.GenericVisual;
import com.bw.graph.visual.Visual;
import com.bw.modeldrive.fsm.model.FiniteStateMachine;
import com.bw.modeldrive.fsm.model.State;
import com.bw.modeldrive.fsm.model.Transition;

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
	 * Creates a new factory.
	 */
	public GraphFactory()
	{
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
		GenericVisual startNode = new GenericVisual(start.name, style);
		Circle circle = new Circle(radius, radius, radius, style.configuration, null);
		circle.setFill(true);
		startNode.setPosition(x, y);
		startNode.addDrawingPrimitive(circle);
		return startNode;
	}

	/**
	 * Creates an edge between teo visuals.
	 *
	 * @param id     The Identification, can be null.
	 * @param source The source-visual
	 * @param target The target-visual
	 * @param g2     Graphics context for calculations.
	 * @param style  The style to use.
	 * @return The edge visual created.
	 */
	public EdgeVisual createEdge(Integer id, Visual source, Visual target, Graphics2D g2, DrawContext style)
	{
		if (source != null && target != null)
		{
			Rectangle2D.Float startBounds = source.getBounds2D(g2);

			ConnectorVisual sourceConnector = new ConnectorVisual(source, style);
			Rectangle2D.Float sourceConnectorBounds = sourceConnector.getBounds2D(g2);
			sourceConnector.setPosition(startBounds.width - (sourceConnectorBounds.width / 2f), (startBounds.height - sourceConnectorBounds.height) / 2f);

			Rectangle2D.Float targetBounds = target.getBounds2D(g2);
			ConnectorVisual targetConnector = new ConnectorVisual(target, style);
			Rectangle2D.Float targetConnectorBounds = targetConnector.getBounds2D(g2);
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
		GenericVisual v = (GenericVisual) visual;
		v.setPosition(x, y);

		Rectangle2D stringBounds = stateInnerContext.normal.fontMetrics.getStringBounds(state.name, g2);
		float fh = stateInnerContext.normal.fontMetrics.getHeight();

		float height = 5 * fh;
		float width = (float) (stringBounds.getWidth() + 10);

		if (visual.getInnerModel() != null)
		{
			Dimension2DFloat dim = visual.getInnerModelDimension();
			InsetsFloat insets = visual.getInnerModelInsets();
			insets.top = fh * 2f;

			float w = dim.width + insets.right + insets.left;
			float h = dim.height + insets.top + insets.bottom;

			if (width < w) width = w;
			if (height < h) height = h;
		}

		Rectangle frame = new Rectangle(
				0, 0, width, height, stateOuterContext.configuration,
				stateOuterContext.normal);
		frame.setFill(true);
		v.addDrawingPrimitive(frame);

		Line separator = new Line(0, fh * 1.5f, width, fh * 1.5f
				, stateInnerContext.configuration, stateInnerContext.normal);
		v.addDrawingPrimitive(separator);

		Text label = new Text(0, 0, state.name, stateInnerContext.configuration, stateInnerContext.normal);
		label.setAlignment(Alignment.Center);
		label.setInsets(fh * 0.25f, 0, 0, 0);

		v.addDrawingPrimitive(label);
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
										 DrawContext edgeStyles
	)
	{
		VisualModel rootModel = new VisualModel();
		if (fsm != null && fsm.pseudoRoot != null)
		{
			java.util.Queue<State> states = new LinkedList<>();

			states.add(fsm.pseudoRoot);

			final float gapY = 5;
			float fh = stateOutlineStyles.normal.fontMetrics.getHeight();

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
						stateVisuals.get(state.parent.name).getInnerModel().addVisual(stateVisual);
					else
						rootModel.addVisual(stateVisual);
					stateVisuals.put(state.name, stateVisual);

					if (!state.states.isEmpty())
					{
						VisualModel subModel = new VisualModel();
						stateVisual.setInnerModel(subModel);
						statePosition = new Rectangle2D.Float(fh, fh, 0, 0);
						statePosition.x += 2 * fh;
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

			for (var visualEntry : stateVisuals.entrySet())
			{
				Visual visual = visualEntry.getValue();

				State state = statesByName.get(visualEntry.getKey());
				if (state.parent != null)
				{
					statePosition = statePositions.get(state.parent.name);
					createStatePrimitives(visual, statePosition.x, statePosition.y, state, g2, stateOutlineStyles, stateInnerStyles);

					Rectangle2D.Float bounds = visual.getBounds2D(g2);
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
			for (Transition t : transitions.values())
			{
				Visual sourceVisual = stateVisuals.get(t.source.name);
				for (State target : t.target)
				{
					stateVisuals.get(t.source.parent.name).getInnerModel().addEdge(
							createEdge(t.docId, sourceVisual, stateVisuals.get(target.name), g2, edgeStyles));
				}
			}

			for (State state : statesByName.values())
			{
				if (!state.states.isEmpty())
				{
					Visual startVisual = createStartVisual(state, fh / 2, fh, fh / 2, startStyles);

					Visual stateVisual = stateVisuals.get(state.name);
					VisualModel innerModel = stateVisual.getInnerModel();
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
						initialStates.add(state.getInnerStatesInDocumentOrder().get(0));
						id = null;
					}
					for (State initialState : initialStates)
					{
						Visual targetVisual = stateVisuals.get(initialState.name);
						if (targetVisual == null)
							log.warning(String.format("Target state %s of initial transition not found", initialState.name));
						else
						{
							innerModel.addEdge(createEdge(id, startVisual, targetVisual, g2, edgeStyles));
						}
					}
				}
			}
		}

		return rootModel;
	}

}
