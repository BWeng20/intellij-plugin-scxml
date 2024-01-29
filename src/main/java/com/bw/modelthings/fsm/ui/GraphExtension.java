package com.bw.modelthings.fsm.ui;

import com.bw.modelthings.fsm.model.FsmElement;
import com.bw.modelthings.fsm.model.State;
import com.bw.modelthings.fsm.parser.ExtensionParser;
import com.bw.svg.SVGWriter;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

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
	public static final String ATTR_POS = "pos";

	/**
	 * Name of attribute used for bounds of the start-node
	 */
	public static final String ATTR_START_POS = "start-pos";

	/**
	 * Holds a position and the bounding-rectangle.
	 */
	public static class PosAndBounds
	{
		/**
		 * The bounds.
		 */
		public final Rectangle2D.Float bounds;

		/**
		 * The position.
		 */
		public final Point2D.Float position;

		/**
		 * Creates a new instance.
		 *
		 * @param pos    The position.
		 * @param bounds The bounds.
		 */
		public PosAndBounds(Point2D.Float pos, Rectangle2D.Float bounds)
		{
			this.bounds = bounds;
			this.position = pos;
		}

		/**
		 * Gets the XML representation.
		 * A sequence of six float values, separated by white-space.
		 *
		 * @param precisionFactor The precision factor to use.
		 * @return The string.
		 */
		public String toXML(float precisionFactor)
		{
			return SVGWriter.toPoint(position, precisionFactor) + " " +
					SVGWriter.toBox(bounds, precisionFactor);
		}

		@Override
		public String toString()
		{
			return toXML(1000);
		}

		private final static Pattern SPLIT_REG_EXP = Pattern.compile("(?U)\\s");


		/**
		 * Parse a XML position and bound string.
		 *
		 * @param bounds The bound string.
		 * @return The bounds or null if the string was not correct.
		 */
		public static PosAndBounds parse(String bounds)
		{
			if (bounds != null)
			{
				String[] coordinate = SPLIT_REG_EXP.split(bounds, 0);
				if (coordinate.length == 6)
				{
					try
					{
						return new PosAndBounds(
								new Point2D.Float(
										Float.parseFloat(coordinate[0]),
										Float.parseFloat(coordinate[1])
								),
								new Rectangle2D.Float(
										Float.parseFloat(coordinate[2]),
										Float.parseFloat(coordinate[3]),
										Float.parseFloat(coordinate[4]),
										Float.parseFloat(coordinate[5]))
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
	}

	/**
	 * Collected bounds. Key = docId, Value = the bounds.
	 */
	protected Map<Integer, PosAndBounds> _bounds = new HashMap<>();

	/**
	 * Collected start-node bounds. Key = docId (of parent state/scxml element), Value = the bounds.
	 */
	protected Map<Integer, PosAndBounds> _startBounds = new HashMap<>();

	public Map<String,String> _statesRenamed;

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
