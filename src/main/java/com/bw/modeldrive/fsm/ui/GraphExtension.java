package com.bw.modeldrive.fsm.ui;

import com.bw.modeldrive.fsm.model.FsmElement;
import com.bw.modeldrive.fsm.model.State;
import com.bw.modeldrive.fsm.parser.ExtensionParser;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SCXML extension handler for "GraphExtension" that supports layout information within the scxml.
 */
public class GraphExtension implements ExtensionParser
{

	/**
	 * Creates a new instance.
	 */
	public GraphExtension()
	{

	}

	/**
	 * Logger for this class.
	 */
	protected static Logger log = Logger.getLogger(GraphExtension.class.getName());


	/**
	 * Name space of graph-extension schema that is used to enrich the scxml with layout information.
	 */
	public static final String NS_GRAPH_EXTENSION = "http://berndwengenroth.de/GraphExtension/1.0";

	/**
	 * Name of attribute used for bounds
	 */
	public static final String ATTR_BOUNDS = "bounds";

	/**
	 * Collected bounds. Key = docId, Value = the bounds.
	 */
	protected Map<Integer, Rectangle2D.Float> bounds = new HashMap<>();

	@Override
	public void processChild(FsmElement item, Element element)
	{
		if (item instanceof State)
		{
		}
	}

	/**
	 * Parse a bounds values, a white-space separated list of four floats.
	 *
	 * @param bounds The value.
	 * @return The parsed rectangle or null.
	 */
	protected Rectangle2D.Float parseBounds(String bounds)
	{
		if (bounds != null)
		{
			String[] coordinate = bounds.split("(?U)\\s");
			if (coordinate.length == 4)
			{
				try
				{
					return new Rectangle2D.Float(
							Float.parseFloat(coordinate[0]),
							Float.parseFloat(coordinate[1]),
							Float.parseFloat(coordinate[2]),
							Float.parseFloat(coordinate[3])
					);
				}
				catch (NumberFormatException e)
				{
					log.log(Level.WARNING, "bounds could not be parsed", e);
				}
			}
		}
		return null;
	}

	@Override
	public void processAttribute(FsmElement item, Node attributeNode)
	{
		if (item instanceof State)
		{
			switch (attributeNode.getLocalName())
			{
				case ATTR_BOUNDS ->
				{
					Rectangle2D.Float r = parseBounds(attributeNode.getNodeValue());
					if (r != null)
						bounds.put(((State) item).docId, r);
				}
			}
		}
	}
}
