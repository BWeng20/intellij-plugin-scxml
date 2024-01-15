package com.bw.modeldrive.fsm.parser;

import com.bw.modeldrive.fsm.model.FsmElement;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.logging.Logger;

/**
 * Logs unhandled elements and attributes.
 */
public class LogExtensionParser implements ExtensionParser
{

	/**
	 * Creates a new logging extension parser with default logger.
	 */
	public LogExtensionParser()
	{
		this.logger = log;
	}

	/**
	 * Creates a new logging extension parser on top of some logger.
	 *
	 * @param log The logger to use.
	 */
	public LogExtensionParser(Logger log)
	{
		this.logger = log;
	}

	/**
	 * Default Logger.
	 */
	protected static Logger log = Logger.getLogger(XmlParser.class.getName() + ".unhandled");

	/**
	 * Logger to use
	 */
	protected final Logger logger;


	@Override
	public void processChild(FsmElement item, Element element)
	{
		logger.warning("Unhandled: " + element.getNodeName() + " @ " + item);
	}

	@Override
	public void processAttribute(FsmElement item, Node attributeNode)
	{
		logger.warning("Unhandled: " + attributeNode.getNamespaceURI() + ":" + attributeNode.getLocalName() + "=" + attributeNode.getNodeValue() + " @ " + item);
	}

}
