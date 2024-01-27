package com.bw.modelthings.fsm.parser;

import com.bw.modelthings.fsm.model.FsmElement;
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
		this._logger = LOG;
	}

	/**
	 * Creates a new logging extension parser on top of some logger.
	 *
	 * @param log The logger to use.
	 */
	public LogExtensionParser(Logger log)
	{
		this._logger = log;
	}

	/**
	 * Default Logger.
	 */
	protected final static Logger LOG = Logger.getLogger(XmlParser.class.getName() + ".unhandled");

	/**
	 * Logger to use
	 */
	protected final Logger _logger;


	@Override
	public void processChild(FsmElement item, Element element)
	{
		_logger.warning("Unhandled: " + element.getNodeName() + " @ " + item);
	}

	@Override
	public void processAttribute(FsmElement item, Node attributeNode)
	{
		_logger.warning("Unhandled: " + attributeNode.getNamespaceURI() + ":" + attributeNode.getLocalName() + "=" + attributeNode.getNodeValue() + " @ " + item);
	}

}
