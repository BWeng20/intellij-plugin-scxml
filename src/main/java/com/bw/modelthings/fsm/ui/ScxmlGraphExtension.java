package com.bw.modelthings.fsm.ui;

import com.bw.modelthings.fsm.model.FsmElement;
import com.bw.modelthings.fsm.model.State;
import com.bw.modelthings.fsm.model.Transition;
import com.bw.modelthings.fsm.parser.ExtensionParser;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * SCXML extension handler for "GraphExtension" that supports layout information within the scxml.
 */
public class ScxmlGraphExtension implements ExtensionParser
{
	/**
	 * Creates a new instance.
	 */
	public ScxmlGraphExtension()
	{

	}

	/**
	 * Logger for this class.
	 */
	protected static Logger log = Logger.getLogger(ScxmlGraphExtension.class.getName());


	/**
	 * Name space of graph-extension schema that is used to enrich the scxml with layout information.
	 */
	public static final String NS_GRAPH_EXTENSION = "http://berndwengenroth.de/GraphExtension/1.0";

	/**
	 * Name of attribute used for bounds
	 */
	public static final String ATTR_POS = "pos";

	/**
	 * Name of attribute used for bounds of the start-node
	 */
	public static final String ATTR_START_POS = "start-pos";

	/**
	 * Name of attribute used for position of edge-source-connector.
	 */
	public static final String ATTR_SOURCE_POS = "source-pos";

	/**
	 * Name of attribute used for position of edge-target-connector.
	 */
	public static final String ATTR_TARGET_POS = "target-pos";

	/**
	 * Attribute for edge-control-points.
	 */
	public static final String ATTR_PC_POS = "path-control-pos";

	/**
	 * Collected bounds. Key = docId, Value = the bounds.
	 */
	protected final Map<Integer, PosAndBounds> _bounds = new HashMap<>();


	private final Map<Integer, TransitionDescription> transitionDescriptionMap = new HashMap<>();

	/**
	 * Get the descriptor for a transition by the edge-visual.
	 *
	 * @param transitionDocId The transition doc-id.
	 * @return The descriptor, never null.
	 */
	public TransitionDescription getTransitionDescriptor(Integer transitionDocId)
	{
		TransitionDescription transitionDescription = transitionDescriptionMap.get(transitionDocId);
		if (transitionDescription == null)
		{
			transitionDescription = new TransitionDescription();
			transitionDescriptionMap.put(transitionDocId, transitionDescription);
		}
		return transitionDescription;
	}

	/**
	 * Collected start-node bounds. Key = docId (of parent state/scxml element), Value = the bounds.
	 */
	protected Map<Integer, PosAndBounds> _startBounds = new HashMap<>();

	@Override
	public void processChild(FsmElement item, Element element)
	{
		if (item instanceof State)
		{
		}
	}

	/**
	 * Parse a bounds values, a white-space separated list of six floats.
	 *
	 * @param bounds The value.
	 * @return The parsed bounds or null.
	 */
	protected PosAndBounds parsePosAndBounds(String bounds)
	{
		return PosAndBounds.parse(bounds);
	}

	/**
	 * Parse a position values, a white-space separated list of two floats.
	 *
	 * @param pos The value.
	 * @return The parsed position or null.
	 */
	protected Point2D.Float parsePosition(String pos)
	{
		return PosAndBounds.parsePosition(pos);
	}

	/**
	 * Parse a list of position values.
	 *
	 * @param posList The value.
	 * @return The parsed position list.
	 */
	protected List<Point2D.Float> parsePositionList(String posList)
	{
		return PosAndBounds.parsePositionList(posList);
	}

	@Override
	public void processAttribute(FsmElement item, Node attributeNode)
	{
		if (item instanceof State)
		{
			switch (attributeNode.getLocalName())
			{
				case ATTR_START_POS ->
				{
					PosAndBounds r = parsePosAndBounds(attributeNode.getNodeValue());
					if (r != null)
						_startBounds.put(((State) item)._docId, r);
				}
				case ATTR_POS ->
				{
					PosAndBounds r = parsePosAndBounds(attributeNode.getNodeValue());
					if (r != null)
						_bounds.put(((State) item)._docId, r);
				}
				case ATTR_SOURCE_POS ->
				{
					Point2D.Float position = parsePosition(attributeNode.getNodeValue());
					if (position != null)
						getTransitionDescriptor(((Transition) item)._docId)._relativeSourceConnector = position;
				}
				case ATTR_TARGET_POS ->
				{
					Point2D.Float position = parsePosition(attributeNode.getNodeValue());
					if (position != null)
						getTransitionDescriptor(((Transition) item)._docId)._relativeTargetConnector = position;
				}
				case ATTR_PC_POS ->
				{
					List<Point2D.Float> positions = parsePositionList(attributeNode.getNodeValue());
					if (positions != null)
						getTransitionDescriptor(((Transition) item)._docId)._pathControlPoints.addAll(positions);
				}
			}
		}
	}
}
