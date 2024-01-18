package com.bw.modelthings.fsm.parser;

import com.bw.modelthings.fsm.model.FsmElement;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Enables client to collect additional data during parsing.
 */
public interface ExtensionParser
{
	/**
	 * Called for unhandled child element.
	 *
	 * @param item    The parent model item, can be null for case where the model has no explicit item.
	 * @param element The unhandled element.
	 */
	void processChild(FsmElement item, Element element);

	/**
	 * Called for unhandled attributes of known elements.
	 *
	 * @param item          The model item.
	 * @param attributeNode The unhandled attribute.
	 */
	void processAttribute(FsmElement item, Node attributeNode);

}
