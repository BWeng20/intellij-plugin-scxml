package com.bw.modelthings.fsm.ui;

import com.bw.modelthings.fsm.model.FsmElement;
import com.bw.modelthings.fsm.model.State;
import com.bw.modelthings.fsm.parser.ExtensionParser;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.HashMap;
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
	 * Collected bounds. Key = docId, Value = the bounds.
	 */
	protected Map<Integer, PosAndBounds> _bounds = new HashMap<>();

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
			}
		}
	}
}