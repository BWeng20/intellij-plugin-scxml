package com.bw.modelthings.fsm.parser;

import com.bw.modelthings.fsm.model.BindingType;
import com.bw.modelthings.fsm.model.ExecutableContent;
import com.bw.modelthings.fsm.model.FiniteStateMachine;
import com.bw.modelthings.fsm.model.FsmElement;
import com.bw.modelthings.fsm.model.Invoke;
import com.bw.modelthings.fsm.model.PseudoRoot;
import com.bw.modelthings.fsm.model.State;
import com.bw.modelthings.fsm.model.Transition;
import com.bw.modelthings.fsm.model.TransitionType;
import com.bw.modelthings.fsm.model.executablecontent.Block;
import com.bw.modelthings.fsm.model.executablecontent.If;
import com.bw.modelthings.fsm.model.executablecontent.Log;
import com.bw.modelthings.intellij.ScXmlSdkBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SCXML parser. Not thread-safe. Please use one instance for each file.
 */
public class XmlParser implements ScxmlTags
{
	/**
	 * The fsm the parser is working on.
	 */
	protected FiniteStateMachine _fsm;

	/**
	 * List of extensions.
	 */
	protected Map<String, List<ExtensionParser>> _extensionParsers = new HashMap<>();


	/**
	 * Extension to use for namespace for which no matching extension was found.
	 */
	protected ExtensionParser _fallbackExtensionParser;

	/**
	 * Creates a new XMLParser.
	 */
	public XmlParser()
	{
	}

	/**
	 * Logger for this class.
	 */
	protected final static Logger LOG = Logger.getLogger(XmlParser.class.getName());

	/**
	 * Resolver that checks that all includes are located inside the same directory subtree as the main file.
	 */
	public static class IncludeProtectionResolver implements EntityResolver
	{

		private String basePathUri;

		/**
		 * Creates a new resolver that allows only files below the same directory-tree as the main file.
		 *
		 * @param mainFile The main xml file that is parsed.
		 */
		IncludeProtectionResolver(Path mainFile)
		{
			basePathUri = mainFile.getParent()
								  .normalize()
								  .toUri()
								  .toString();
		}

		@Override
		public InputSource resolveEntity(String publicId, String systemId) throws SAXException
		{
			if (publicId != null)
				throw new SAXParseException("Try to access file with publicId '" + publicId + "'.", null);
			if (!isLocalUri(systemId))
				throw new SAXParseException("Try to access file outside directory tree  '" + systemId + "'.", null);

			// use the default behaviour
			return null;
		}

		private boolean isLocalUri(String systemId)
		{
			try
			{
				return systemId == null ||
						new URI(systemId).normalize()
										 .toString()
										 .startsWith(basePathUri);
			}
			catch (URISyntaxException e)
			{
				return false;
			}
		}
	}

	/**
	 * Parses the SCXML file.<br>
	 * The XML source shall be the current content
	 * of the document and may yet not be stored in the	file.
	 *
	 * @param file The file of the content. Used to retrieve the location.
	 * @param xml  The XML content.
	 * @return The created model.
	 * @throws ParserException in case something was wrong with the file.
	 */
	public FiniteStateMachine parse(Path file, String xml) throws ParserException
	{
		javax.xml.parsers.DocumentBuilderFactory factory = org.apache.xerces.jaxp.DocumentBuilderFactoryImpl.newInstance();
		factory.setNamespaceAware(true);
		factory.setXIncludeAware(true);
		factory.setIgnoringElementContentWhitespace(true);

		Document doc;
		try
		{
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

			DocumentBuilder builder = factory.newDocumentBuilder();

			// Protect us against XXE or SSRF, restrict any includes.
			builder.setEntityResolver(new IncludeProtectionResolver(file));

			InputSource is = new InputSource();
			is.setByteStream(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
			is.setSystemId(file.normalize()
							   .toString());

			doc = builder.parse(is);
		}
		catch (Exception e)
		{
			LOG.log(Level.WARNING, e.getMessage(), e);
			doc = null;
		}

		_fsm = null;

		if (doc != null)
		{
			NodeList scxmlElements = doc.getElementsByTagNameNS(NS_SCXML, TAG_SCXML);
			if (scxmlElements.getLength() != 1)
			{
				throw new ParserException("Exactly on <scxml> element expected");
			}
			Element root = (Element) scxmlElements.item(0);
			if (ScxmlTags.TAG_SCXML.equals(root.getLocalName()))
			{
				_fsm = new FiniteStateMachine();

				_fsm._name = getOptionalAttribute(root, ATTR_NAME);
				_fsm._dataModel = getAttributeOrDefault(root, ATTR_DATAMODEL, "Null");
				_fsm._binding = mapBindingType(getAttributeOrDefault(root, ATTR_BINDING, BindingType.Early.name()));
				_fsm._pseudoRoot = parseState(root, false, null);
			}
			else
			{
				throw new ParserException(ScXmlSdkBundle.message("parser.error.root_tag_is_not_scxml", root.getLocalName()));
			}
		}
		return _fsm;
	}

	class ScxmlElementIterator implements Iterator<Element>
	{
		Element child;

		List<Element> notHandled = new ArrayList<>();
		FsmElement currentFsmElement;


		ScxmlElementIterator(Element parent, FsmElement fsmElement)
		{
			this.currentFsmElement = fsmElement;
			processToNextElement(parent.getFirstChild());
		}

		private void processToNextElement(Node child)
		{
			while (child != null)
			{
				if (child instanceof Element xmlTagChild)
				{
					final String ns = child.getNamespaceURI();
					if (ns == null || NS_SCXML.equals(ns))
						break;
					else
						notHandled.add(((Element) child));
				}
				child = child.getNextSibling();
			}
			this.child = (Element) child;
		}

		@Override
		public boolean hasNext()
		{
			if (child == null)
			{
				if (currentFsmElement != null)
					processNotHandledElements(currentFsmElement);
			}
			return child != null;
		}

		@Override
		public Element next()
		{
			Element r = child;
			if (child != null)
				processToNextElement(child.getNextSibling());
			return r;
		}

		void processNotHandledElements(FsmElement fsmElement)
		{
			for (Element e : notHandled)
			{
				List<ExtensionParser> pl = _extensionParsers.get(e.getNamespaceURI());
				if (pl == null)
				{
					if (_fallbackExtensionParser != null)
						_fallbackExtensionParser.processChild(fsmElement, e);
				}
				else
					for (ExtensionParser p : pl)
						p.processChild(fsmElement, e);
			}
			notHandled.clear();
		}
	}

	/**
	 * Process unhandled attributes (with unknown namespaces).
	 *
	 * @param e          The parent-element of the attributes.
	 * @param fsmElement The current element.
	 */
	protected void processUnhandledAttributes(Element e, FsmElement fsmElement)
	{

		NamedNodeMap attributes = e.getAttributes();
		int n = attributes.getLength();
		for (int i = 0; i < n; ++i)
		{
			Node attrNode = attributes.item(i);
			String nsUri = attrNode.getNamespaceURI();
			if (nsUri != null && !NS_SCXML.equals(nsUri))
			{
				List<ExtensionParser> pl = _extensionParsers.get(nsUri);
				if (pl == null)
				{
					if (_fallbackExtensionParser != null)
						_fallbackExtensionParser.processAttribute(fsmElement, attrNode);

				}
				else
					for (ExtensionParser ep : pl)
						ep.processAttribute(fsmElement, attrNode);
			}
		}

	}


	/**
	 * Parse a state specification and all sub-elements.
	 *
	 * @param node     The State node.
	 * @param parallel True if this is a parallel state.
	 * @param parent   The parent state.
	 * @return The corresponding state.
	 * @throws ParserException in case something was wrong with the file.
	 */
	protected State parseState(Element node, boolean parallel, State parent) throws ParserException
	{
		State state = getOrCreateStateWithAttributes(node, parallel, parent);

		for (ScxmlElementIterator it = new ScxmlElementIterator(node, state); it.hasNext(); )
		{
			Element xmlChild = it.next();
			switch (xmlChild.getLocalName())
			{
				case TAG_ON_ENTRY -> state._onEntry = parseExecutableContentBlock(xmlChild);
				case TAG_ON_EXIT -> state._onExit = parseExecutableContentBlock(xmlChild);
				case TAG_TRANSITION -> parseTransition(xmlChild, state);
				case TAG_INITIAL -> parseInitialTransition(xmlChild, state);
				case TAG_STATE -> parseState(xmlChild, false, state);
				case TAG_PARALLEL -> parseState(xmlChild, true, state);
				case TAG_FINAL ->
				{
					State s = parseState(xmlChild, false, state);
					s._isFinal = true;
				}
				case TAG_HISTORY -> parseToDo(xmlChild, state);
				case TAG_DATAMODEL -> parseToDo(xmlChild, state);
				case TAG_INVOKE -> parseInvoke(xmlChild, state);
				default -> debug("Unsupported tag %s", xmlChild.getLocalName());
			}
		}
		return state;
	}

	/**
	 * Parse an invoke node.
	 *
	 * @param node        The node.
	 * @param sourceState The parent-state
	 */
	protected void parseInvoke(Element node, State sourceState)
	{
		Invoke invoke = new Invoke();

		invoke._typeName = getOptionalAttribute(node, ATTR_TYPE);
		invoke._typeExpr = getOptionalAttribute(node, ATTR_TYPEEXPR);
		invoke._src = getOptionalAttribute(node, ATTR_SRC);
		invoke._srcExpr = getOptionalAttribute(node, ATTR_SRCEXPR);
		invoke._id = getOptionalAttribute(node, ATTR_ID);
		invoke._idLocation = getOptionalAttribute(node, ATTR_IDLOCATION);
		parseSymbolList(getOptionalAttribute(node, ATTR_NAMELIST), invoke._nameList);
		invoke._autoforward = parseBoolean(getOptionalAttribute(node, ATTR_AUTOFORWARD), false);

		for (ScxmlElementIterator it = new ScxmlElementIterator(node, invoke); it.hasNext(); )
		{
			Element xmlChild = it.next();
			switch (xmlChild.getLocalName())
			{
				case TAG_PARAM -> parseToDo(node, sourceState);
				case TAG_FINALIZE -> parseToDo(node, sourceState);
				case TAG_CONTENT -> parseToDo(node, sourceState);
				default -> debug("Unsupported tag %s", xmlChild.getLocalName());
			}
		}
		sourceState._invoke.add(invoke);
	}

	/**
	 * Parse an initial transition node.
	 *
	 * @param node        The node.
	 * @param sourceState The parent-state
	 * @throws ParserException in case something was wrong with the file.
	 */
	protected void parseInitialTransition(Element node, State sourceState) throws ParserException
	{
		Transition t = parseTransitionWithAttributes(node);

		if (sourceState._initial != null)
		{
			throw new ParserException("<initial> must not be specified if initial-attribute was given");
		}

		t._source = sourceState;
		sourceState._initial = t;

		debug(String.format("Initial %s", t));
	}

	/**
	 * Parse a transition node.
	 *
	 * @param node        The node.
	 * @param sourceState The parent state.
	 * @throws ParserException in case something was wrong with the file.
	 */
	protected void parseTransition(Element node, State sourceState) throws ParserException
	{
		Transition t = parseTransitionWithAttributes(node);
		t._source = sourceState;
		sourceState._transitions.add(t);
		debug(String.format("Transition %s", t));
	}

	/**
	 * Common part of parsing some transition node (initial or transition), including attributes and executable content.
	 *
	 * @param node The transition node
	 * @return The transition (not added to state)
	 * @throws ParserException in case something was wrong with the file.
	 */
	protected Transition parseTransitionWithAttributes(Element node) throws ParserException
	{
		Transition t = new Transition();

		t._docId = ++_docIdCounter;
		t._xmlId = node.getAttributeNS(NS_XML, ATTR_ID);
		if (t._xmlId == null)
			t._xmlId = String.valueOf(t._docId);

		parseSymbolList(getOptionalAttribute(node, TAG_EVENT), t._events);
		t._cond = getOptionalAttribute(node, ATTR_COND);
		parseStateSpecification(getOptionalAttribute(node, ATTR_TARGET), t._target);

		t._transitionType = mapTransitionType(getOptionalAttribute(node, ATTR_TYPE));
		t._content = parseExecutableContentBlock(node);

		return t;
	}

	/**
	 * Mapping from binding mode name to enum.
	 */
	protected static final Map<String, BindingType> BINDING_TYPE_MAP =
			Map.of(BINDING_TYPE_LATE, BindingType.Late,
					BINDING_TYPE_EARLY, BindingType.Early);


	/**
	 * Translates a binding mode name.
	 *
	 * @param type The name of the type. Can be null.
	 * @return The Binding mode or null if mode is null or empty.
	 * @throws ParserException If the mode is not empty or null but value is unknown.
	 */
	protected BindingType mapBindingType(String type) throws ParserException
	{
		BindingType typeValue = null;
		if (type != null && !type.isEmpty())
		{
			typeValue = BINDING_TYPE_MAP.get(type);
			if (typeValue == null)
			{
				throw new ParserException(String.format("Unknown binding type value '%s'", type));
			}
		}
		return typeValue;
	}


	/**
	 * Mapping from transition type name to enum.
	 */
	protected static final Map<String, TransitionType> TRANSITION_TYPE_MAP =
			Map.of(TRANSITION_TYPE_INTERNAL, TransitionType.Internal,
					TRANSITION_TYPE_EXTERNAL, TransitionType.External);

	/**
	 * Translates a transition type name.
	 *
	 * @param type The name of the type. Can be null.
	 * @return The Transition type or null if type is null or empty.
	 * @throws ParserException If type is not empty or null but value is unknown.
	 */
	protected TransitionType mapTransitionType(String type) throws ParserException
	{
		TransitionType typeValue = null;
		if (type != null && !type.isEmpty())
		{
			typeValue = TRANSITION_TYPE_MAP.get(type);
			if (typeValue == null)
			{
				throw new ParserException(String.format("Unknown transition type value '%s'", type));
			}
		}
		return typeValue;
	}


	/**
	 * Parse executable content as in &lt;onentry&gt; or &lt;onexit&gt;.
	 *
	 * @param node The parent node of the content.
	 * @return The content
	 * @throws ParserException in case something was wrong with the file.
	 */
	protected ExecutableContent parseExecutableContentBlock(Element node) throws ParserException
	{
		ExecutableContent c = null;

		ScxmlElementIterator it = new ScxmlElementIterator(node, null);
		while (it.hasNext())
		{
			c = parseExecutableContentElement(it.next(), c);
		}
		it.processNotHandledElements(c);
		return c;
	}

	/**
	 * Parse a single Executable Content element.
	 *
	 * @param xmlChild The element to parse.
	 * @param previous The previous content in chain.
	 * @return The parsed content.
	 * @throws ParserException in case something was wrong with the file.
	 */
	protected ExecutableContent parseExecutableContentElement(Element xmlChild, ExecutableContent previous) throws ParserException
	{
		return switch (xmlChild.getLocalName())
		{
			case TAG_RAISE -> parseRaise(xmlChild, previous);
			case TAG_IF -> parseIf(xmlChild, previous);
			case TAG_FOR_EACH -> parseForEach(xmlChild, previous);
			case TAG_LOG -> parseLog(xmlChild, previous);
			case TAG_ASSIGN -> parseAssign(xmlChild, previous);
			case TAG_SCRIPT -> parseScript(xmlChild, previous);
			case TAG_SEND -> parseSend(xmlChild, previous);
			case TAG_CANCEL -> parseCancel(xmlChild, previous);
			default ->
			{
				debug("Unsupported tag %s", xmlChild.getLocalName());
				yield null;
			}
		};
	}


	/**
	 * Parse a &lt;raise&gt; element.
	 *
	 * @param node The XML node.
	 * @param prev The previous content in the current chain.
	 * @return The executable content.
	 */
	protected ExecutableContent parseRaise(Element node, ExecutableContent prev)
	{
		// @TODO
		return chainExecutableContent(prev, null);
	}

	/**
	 * Parse a &lt;if&gt; element.
	 *
	 * @param node The XML node.
	 * @param prev The previous content in the current chain.
	 * @return The executable content.
	 * @throws ParserException in case something was wrong with the file.
	 */
	protected ExecutableContent parseIf(Element node, ExecutableContent prev) throws ParserException
	{
		If ifC = new If(getRequiredAttribute(node, ATTR_COND));

		Block currentBlock = new Block();
		ifC.content = currentBlock;
		If currentIf = ifC;

		boolean elseSeen = false;

		for (ScxmlElementIterator it = new ScxmlElementIterator(node, ifC); it.hasNext(); )
		{
			Element xmlChild = it.next();
			switch (xmlChild.getLocalName())
			{
				case TAG_ELSE ->
				{
					if (elseSeen)
					{
						throw new ParserException("Wrong sequence of <elseif> and <else> tags");
					}
					elseSeen = true;

					currentBlock = new Block();
					currentIf.elseContent = currentBlock;
				}
				case TAG_ELSEIF ->
				{
					if (elseSeen)
					{
						throw new ParserException("Wrong sequence of <elseif> and <else> tags");
					}
					If nextIf = new If(getRequiredAttribute(xmlChild, ATTR_COND));
					currentIf.elseContent = nextIf;
					currentIf = nextIf;
					currentBlock = new Block();
					currentIf.content = currentBlock;
				}
				default -> parseExecutableContentElement(xmlChild, currentBlock);
			}
		}
		processUnhandledAttributes(node, ifC);
		return chainExecutableContent(prev, ifC);
	}

	/**
	 * Parse a &lt;foreach&gt; element.
	 *
	 * @param node The XML node.
	 * @param prev The previous content in the current chain.
	 * @return The executable content.
	 */
	protected ExecutableContent parseForEach(Element node, ExecutableContent prev)
	{
		// @TODO
		return chainExecutableContent(prev, null);
	}

	/**
	 * Parse a &lt;log&gt; element.
	 *
	 * @param node The XML node.
	 * @param prev The previous content in the current chain.
	 * @return The executable content.
	 * @throws ParserException in case something was wrong with the file.
	 */
	protected ExecutableContent parseLog(Element node, ExecutableContent prev) throws ParserException
	{
		final Log log = new Log(getAttributeOrDefault(node, ATTR_LABEL, ""), getOptionalAttribute(node, ATTR_EXPR));
		return chainExecutableContent(prev, log);
	}

	/**
	 * Parse a &lt;assign&gt; element.
	 *
	 * @param node The XML node.
	 * @param prev The previous content in the current chain.
	 * @return The executable content.
	 */
	protected ExecutableContent parseAssign(Element node, ExecutableContent prev)
	{
		// @TODO
		return chainExecutableContent(prev, null);
	}

	/**
	 * Parse a &lt;script&gt; element.
	 *
	 * @param node The XML node.
	 * @param prev The previous content in the current chain.
	 * @return The executable content.
	 */
	protected ExecutableContent parseScript(Element node, ExecutableContent prev)
	{
		// @TODO
		return chainExecutableContent(prev, null);
	}

	/**
	 * Parse a &lt;send&gt; element.
	 *
	 * @param node The XML node.
	 * @param prev The previous content in the current chain.
	 * @return The executable content.
	 */
	protected ExecutableContent parseSend(Element node, ExecutableContent prev)
	{
		// @TODO
		return chainExecutableContent(prev, null);
	}

	/**
	 * Parse a &lt;cancel&gt; element.
	 *
	 * @param node The XML node.
	 * @param prev The previous content in the current chain.
	 * @return The executable content.
	 */
	protected ExecutableContent parseCancel(Element node, ExecutableContent prev)
	{
		// @TODO
		return chainExecutableContent(prev, null);
	}

	/**
	 * Placeholder for not-yet-implemented stuff.
	 *
	 * @param tag   The tag that was not handled.
	 * @param state The state in which this happened.
	 */
	protected void parseToDo(Element tag, State state)
	{
		LOG.warning(String.format("Not yet handled: %s [state %s]", tag.getLocalName(), state._name));
	}

	/**
	 * Generates an internal id. Used in case an id is missing.
	 *
	 * @return The generated Id.
	 */
	public String generateId()
	{
		_idCount += 1;
		return String.format("__id%d", _idCount);
	}

	/**
	 * Get or creates a state for a state-node. Sets all attributes.
	 *
	 * @param node     The XML node of the state.
	 * @param parallel True if this is a parallel state.
	 * @param parent   The parent state.
	 * @return The corresponding state.
	 */
	protected State getOrCreateStateWithAttributes(Element node, boolean parallel, State parent)
	{
		String sname = getAttributeOrCompute(node, ATTR_ID, this::generateId);
		State state;
		if (TAG_SCXML.equals(node.getLocalName()))
		{
			PseudoRoot pseudoRoot = new PseudoRoot(_fsm);
			pseudoRoot._name = sname;
			_fsm._states.put(sname, pseudoRoot);
			state = pseudoRoot;
		}
		else
		{
			state = getOrCreateState(sname, parallel);
		}
		String initial = getSCXMLAttribute(node, ATTR_INITIAL);
		state._docId = ++_docIdCounter;

		if (initial != null)
		{
			// Create initial-transition with the initial states
			Transition t = new Transition();
			t._docId = ++_docIdCounter;
			t._transitionType = TransitionType.Internal;
			t._source = state;
			parseStateSpecification(initial, t._target);
			if (!t._target.isEmpty())
				state._initial = t;
		}

		if (parent != null)
		{
			state._parent = parent;
			if (!parent._states.contains(state))
			{
				parent._states.add(state);
			}
		}

		processUnhandledAttributes(node, state);

		return state;
	}

	/**
	 * Get or create a state for the given name.
	 *
	 * @param sname    The unique name of the state.
	 * @param parallel True if the state is a &lt;parallel&gt;
	 * @return The state
	 */
	public State getOrCreateState(String sname, boolean parallel)
	{
		State state = _fsm._states.get(sname);
		if (state == null)
		{
			state = new State();
			state._name = sname;
			_fsm._states.put(sname, state);
		}
		if (parallel)
			state._isParallel = parallel;
		return state;
	}

	/**
	 * Gets an attribute.
	 *
	 * @param tag       The Element to get the attribute from.
	 * @param attribute The case-sensitive name of the attribute.
	 * @return The found attribute value or null
	 */
	public String getSCXMLAttribute(Element tag, String attribute)
	{
		return tag.getAttributeNS(null, attribute);
	}

	/**
	 * Gets an optional attribute.
	 *
	 * @param tag       The Element to get the attribute from.
	 * @param attribute The case-sensitive name of the attribute.
	 * @return The found attribute value or null
	 */
	public String getOptionalAttribute(Element tag, String attribute)
	{
		return getAttributeOrDefault(tag, attribute, null);
	}


	/**
	 * Gets an attribute.
	 *
	 * @param tag          The Element to get the attribute from.
	 * @param attribute    The case-sensitive name of the attribute.
	 * @param defaultValue The default value in case the attribute is missing. Can be null.
	 * @return The found attribute value or null
	 */
	public String getAttributeOrDefault(Element tag, String attribute, String defaultValue)
	{
		String value = getSCXMLAttribute(tag, attribute);
		return value == null ? defaultValue : value;
	}

	/**
	 * Gets an attribute.
	 *
	 * @param tag       The Element to get the attribute from.
	 * @param attribute The case-sensitive name of the attribute.
	 * @param supplier  The supplier for the fallback value in case the attribute is missing. Must be not null.
	 * @return The found attribute value or null
	 */
	public String getAttributeOrCompute(Element tag, String attribute, Supplier<String> supplier)
	{
		String value = getSCXMLAttribute(tag, attribute);
		return (value == null || value.isEmpty()) ? supplier.get() : value;
	}

	/**
	 * Gets a required attribute.
	 *
	 * @param tag       The Element to get the attribute from.
	 * @param attribute The case-sensitive name of the attribute.
	 * @return The found attribute value.
	 * @throws ParserException If attribute is missing
	 */
	public String getRequiredAttribute(Element tag, String attribute) throws ParserException
	{
		final String value = getSCXMLAttribute(tag, attribute);
		if (value == null)
			throw new ParserException(ScXmlSdkBundle.message("parser.error.missing_attribute", attribute, tag.getLocalName()));
		return value;
	}

	/**
	 * Id generator if ids are missing
	 */
	private int _idCount = 0;

	/**
	 * Document-order-Id generator.
	 */
	private int _docIdCounter = 0;

	/**
	 * Parse a state-specification, a white-space separated list of stare references.
	 *
	 * @param targetName The list of stare references
	 * @param targets    A list to add the states to.
	 */
	protected void parseStateSpecification(String targetName, java.util.List<State> targets)
	{
		Arrays.stream(ScxmlTags.splitNameList(targetName))
			  .forEach(t ->
					  {
						  if (!t.isEmpty())
							  targets.add(getOrCreateState(t, false));
					  }
			  );
	}

	/**
	 * Parse a boolean value.
	 *
	 * @param value        The boolean value.
	 * @param defaultValue The default in case the value is null or empty.
	 * @return true if value equals case-insensitive to 'true'
	 */
	protected boolean parseBoolean(String value, boolean defaultValue)
	{
		return (value == null || value.isEmpty()) ? defaultValue : "true".equalsIgnoreCase(value);
	}

	/**
	 * Parse a symbol list, a white-space separated list of symbols.
	 *
	 * @param eventNames The list of symbols
	 * @param events     A list to add the symbols to.
	 */
	protected void parseSymbolList(String eventNames, java.util.List<String> events)
	{
		events.addAll(Arrays.asList(ScxmlTags.splitNameList(eventNames)));
	}

	/**
	 * Log a debug message.
	 *
	 * @param format Format string. @See {@link String#format(String, Object...)}
	 * @param args   THe arguments for the format specification.
	 */
	protected void debug(String format, Object... args)
	{
		/// @TODO: Set this correctly to "debug" if we know how to dump it to host console.
		LOG.warning(String.format(format, args));
	}

	/**
	 * Chains two ExecutableContent.
	 *
	 * @param c1 The first one
	 * @param c2 The second one
	 * @return The chained content.
	 */
	protected ExecutableContent chainExecutableContent(ExecutableContent c1, ExecutableContent c2)
	{
		if (c1 == null)
			return c2;
		if (c2 == null)
			return c1;
		Block result;
		if (c1 instanceof Block)
		{
			result = (Block) c1;
		}
		else
		{
			result = new Block();
			result.content.add(c1);
		}
		result.content.add(c2);
		return result;
	}

	/**
	 * Adds an extension.
	 *
	 * @param namespace The namespace-uri for this extension (the complete uri, not the alias) or "*" to match all.
	 * @param parser    The extension instance.
	 */
	public void addExtensionParser(String namespace, ExtensionParser parser)
	{
		if ("*".equals(namespace))
			_fallbackExtensionParser = parser;
		else
			_extensionParsers.computeIfAbsent(namespace.intern(), s -> new ArrayList<>())
							 .add(parser);
	}

	/**
	 * Removes an extension.
	 *
	 * @param namespace The namespace-uri for this extension (the complete uri, not the alias) or "*" for the fallback handler.
	 */
	public void removeExtensionParser(String namespace)
	{
		if ("*".equals(namespace))
			_fallbackExtensionParser = null;
		else
			_extensionParsers.remove(namespace);
	}
}
