package com.bw.modeldrive.fsm.ui;

import com.bw.graph.Alignment;
import com.bw.graph.DrawContext;
import com.bw.graph.VisualModel;
import com.bw.graph.primitive.Circle;
import com.bw.graph.primitive.Line;
import com.bw.graph.primitive.Rectangle;
import com.bw.graph.primitive.Text;
import com.bw.graph.visual.ConnectorVisual;
import com.bw.graph.visual.EdgeVisual;
import com.bw.graph.visual.GenericVisual;
import com.bw.graph.visual.Visual;
import com.bw.modeldrive.fsm.model.FiniteStateMachine;
import com.bw.modeldrive.fsm.model.State;
import com.bw.modeldrive.fsm.model.Transition;
import org.apache.commons.collections.map.HashedMap;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.logging.Logger;

/**
 * Factory to create Graph visuals from an FSM model.
 */
public class GraphFactory
{
	/**
	 * Logger of this class.
	 */
	static final Logger log = Logger.getLogger(GraphFactory.class.getName());


	/**
	 * Creates a new factory.
	 */
	public GraphFactory()
	{
	}

	/**
	 * Creates a visual for a start-node
	 *
	 * @param x      Base X Position
	 * @param y      Base Y Position
	 * @param radius Radius of circle.
	 * @param style  The style to use.
	 * @return The visual
	 */
	public Visual createStartVisual(float x, float y, float radius, DrawContext style)
	{
		GenericVisual startNode = new GenericVisual(style);
		Circle circle = new Circle(radius, radius, radius, style.configuration, null);
		circle.setFill(true);
		startNode.setPosition(x, y);
		startNode.addDrawingPrimitive(circle);
		return startNode;
	}

	/**
	 * Creates an edge between teo visuals.
	 *
	 * @param source The source-visual
	 * @param target The target-visual
	 * @param g2     Graphics context for calculations.
	 * @param style  The style to use.
	 * @return The edge visual created.
	 */
	public EdgeVisual createEdge(Visual source, Visual target, Graphics2D g2, DrawContext style)
	{
		if (source != null && target != null)
		{
			Rectangle2D.Float startBounds = source.getBounds2D(g2);

			ConnectorVisual sourceConnector = new ConnectorVisual(source, style);
			Rectangle2D.Float sourceConnectorBounds = sourceConnector.getBounds2D(g2);
			sourceConnector.setPosition(startBounds.width, (startBounds.height - sourceConnectorBounds.height) / 2f);

			Rectangle2D.Float targetBounds = target.getBounds2D(g2);
			ConnectorVisual targetConnector = new ConnectorVisual(target, style);
			Rectangle2D.Float targetConnectorBounds = targetConnector.getBounds2D(g2);
			targetConnector.setPosition(-targetConnectorBounds.width, (targetBounds.height - targetConnectorBounds.height) / 2f);

			return new EdgeVisual(sourceConnector, targetConnector, style);
		}
		return null;
	}

	/**
	 * Creates a visual for a state
	 *
	 * @param x                 Base X Position
	 * @param y                 Base Y Position
	 * @param state             State to create the visual for.
	 * @param g2                Graphics to use for dimension calculations.
	 * @param stateOuterContext The draw context for the outline.
	 * @param stateInnerContext The draw context for the inner drawings.
	 * @return The created visual.
	 */
	public Visual createStateVisual(float x, float y, State state, Graphics2D g2, DrawContext stateOuterContext, DrawContext stateInnerContext)
	{
		GenericVisual v = new GenericVisual(stateInnerContext);
		v.setPosition(x, y);

		Rectangle2D stringBounds = stateInnerContext.normal.fontMetrics.getStringBounds(state.name, g2);
		float fh = stateInnerContext.normal.fontMetrics.getHeight();
		float height = 5 * fh;

		Rectangle frame = new Rectangle(
				0, 0, (float) (stringBounds.getWidth() + 10), height, stateOuterContext.configuration,
				stateOuterContext.normal);
		frame.setFill(true);
		v.addDrawingPrimitive(frame);

		Line separator = new Line(0, fh * 1.5f, (float) (stringBounds.getWidth() + 10), fh * 1.5f
				, stateInnerContext.configuration, stateInnerContext.normal);
		v.addDrawingPrimitive(separator);

		Text label = new Text(0, 0, state.name, stateInnerContext.configuration, stateInnerContext.normal);
		label.setAlignment(Alignment.Center);
		label.setInsets(fh * 0.25f, 0, 0, 0);

		v.addDrawingPrimitive(label);

		return v;
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
		VisualModel model = new VisualModel();
		if (fsm != null && fsm.pseudoRoot != null)
		{
			java.util.Map<String, Visual> stateVisuals = new HashedMap();
			java.util.Queue<State> states = new LinkedList<>(fsm.pseudoRoot.states);

			final float gapY = 5;
			float fh = stateOutlineStyles.normal.fontMetrics.getHeight();
			float x = fh;
			float y = fh;
			Visual startNode = createStartVisual(x + fh / 2, y + fh, fh / 2, startStyles);
			model.addVisual(startNode);

			x += 2 * fh;
			float maxWidth = 0;
			java.util.Map<Integer, Transition> transitions = new HashedMap();
			while (!states.isEmpty())
			{
				State state = states.poll();
				if (!stateVisuals.containsKey(state.name))
				{
					Visual stateVisual = createStateVisual(x, y, state, g2, stateOutlineStyles, stateInnerStyles);
					Rectangle2D.Float bounds = stateVisual.getBounds2D(g2);
					y += bounds.height + gapY;
					if (bounds.width > maxWidth)
						maxWidth = bounds.width;
					if (y > 600)
					{
						x += maxWidth + fh;
						y = fh;
						maxWidth = 0;
					}
					model.addVisual(stateVisual);
					stateVisuals.put(state.name, stateVisual);

					states.addAll(state.states);
					for (Transition t : state.transitions.asList())
					{
						states.addAll(t.target);
						transitions.put(t.docId, t);
					}
				}
			}
			if (fsm.pseudoRoot.initial != null)
			{
				for (State s : fsm.pseudoRoot.initial.target)
				{
					Visual targetVisual = stateVisuals.get(s.name);
					if (targetVisual == null)
						log.warning(String.format("Target state %s of initial transition not found", s.name));
					else
					{
						model.addEdge(createEdge(startNode, targetVisual, g2, edgeStyles));
					}

				}
			}
			for (Transition t : transitions.values())
			{
				Visual sourceVisual = stateVisuals.get(t.source.name);
				for (State target : t.target)
				{
					model.addEdge(createEdge(sourceVisual, stateVisuals.get(target.name), g2, edgeStyles));
				}
			}
		}
		return model;
	}

}
