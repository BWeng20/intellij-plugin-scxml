package com.bw.modeldrive.parser;

import com.bw.modeldrive.ModelDriveBundle;
import com.bw.modeldrive.model.BindingType;
import com.bw.modeldrive.model.ExecutableContent;
import com.bw.modeldrive.model.FiniteStateMachine;
import com.bw.modeldrive.model.Invoke;
import com.bw.modeldrive.model.State;
import com.bw.modeldrive.model.Transition;
import com.bw.modeldrive.model.TransitionType;
import com.bw.modeldrive.model.executablecontent.Block;
import com.bw.modeldrive.model.executablecontent.If;
import com.bw.modeldrive.model.executablecontent.Log;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;

/**
 * SCXML parser. Not thread-safe. Please use one instance for each file.
 */
public class XmlParser implements ScxmlTags
{
	/**
	 * The fsm the parser is working on.
	 */
	protected FiniteStateMachine fsm;

	/**
	 * Creates a new XMLParser.
	 */
	public XmlParser()
	{

	}

	private static final Logger log = Logger.getInstance(XmlParser.class);

	/**
	 * Parses the SCXML file.
	 *
	 * @param xml The SCXML file.
	 * @return The created model.
	 * @throws ParserException in case something was wrong with the file.
	 */
	public FiniteStateMachine parse(XmlFile xml) throws ParserException
	{
		fsm = null;

		XmlTag root = xml.getRootTag();
		if (ScxmlTags.TAG_SCXML.equals(root.getLocalName()))
		{

			fsm = new FiniteStateMachine();

			fsm.name = root.getAttributeValue(ATTR_NAME);
			fsm.datamodel = root.getAttributeValue(ATTR_DATAMODEL);
			fsm.binding = BindingType.valueOf(getAttributeOrDefault(root, ATTR_BINDING, BindingType.Early.name()));
			fsm.pseudoRoot = parseState(root, false, null);
		}
		else
		{
			throw new ParserException(ModelDriveBundle.message("parser.error.root_tag_is_not_scxml", root.getLocalName()));
		}
		return fsm;
	}

	static class ScxmlChildIterator implements Iterator<XmlTag>
	{
		XmlTag child;

		ScxmlChildIterator(XmlTag parent)
		{
			processToScxmlTag(parent.getFirstChild());
		}

		private void processToScxmlTag(PsiElement child)
		{
			while (child != null && !(child instanceof XmlTag xmlTagChild && NS_SCXML.equals(xmlTagChild.getNamespace())))
			{
				child = child.getNextSibling();
			}
			this.child = (XmlTag) child;
		}

		@Override
		public boolean hasNext()
		{
			return child != null;
		}

		@Override
		public XmlTag next()
		{
			XmlTag r = child;
			if (child != null)
				processToScxmlTag(child.getNextSibling());
			return r;
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
	protected State parseState(XmlTag node, boolean parallel, State parent) throws ParserException
	{
		State state = getOrCreateStateWithAttributes(node, parallel, parent);

		for (ScxmlChildIterator it = new ScxmlChildIterator(node); it.hasNext(); )
		{
			XmlTag xmlChild = it.next();
			switch (xmlChild.getLocalName())
			{
				case TAG_ON_ENTRY -> state.onentry = parseExecutableContentBlock(xmlChild);
				case TAG_ON_EXIT -> state.onexit = parseExecutableContentBlock(xmlChild);
				case TAG_TRANSITION -> parseTransition(xmlChild, state);
				case TAG_INITIAL -> parseInitialTransition(xmlChild, state);
				case TAG_STATE -> parseState(xmlChild, false, state);
				case TAG_PARALLEL -> parseState(xmlChild, true, state);
				case TAG_FINAL ->
				{
					State s = parseState(xmlChild, false, state);
					s.isFinal = true;
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
	 * @throws ParserException in case something was wrong with the file.
	 */
	protected void parseInvoke(XmlTag node, State sourceState) throws ParserException
	{
		Invoke invoke = new Invoke();

		invoke.typeName = getOptionalAttribute(node, ATTR_TYPE);
		invoke.typeExpr = getOptionalAttribute(node, ATTR_TYPEEXPR);
		invoke.src = getOptionalAttribute(node, ATTR_SRC);
		invoke.srcexpr = getOptionalAttribute(node, ATTR_SRCEXPR);
		invoke.id = getOptionalAttribute(node, ATTR_ID);
		invoke.idLocation = getOptionalAttribute(node, ATTR_IDLOCATION);
		parseSymbolList(getOptionalAttribute(node, ATTR_NAMELIST), invoke.namelist);
		invoke.autoforward = parseBoolean(getOptionalAttribute(node, ATTR_AUTOFORWARD), false);

		for (ScxmlChildIterator it = new ScxmlChildIterator(node); it.hasNext(); )
		{
			XmlTag xmlChild = it.next();
			switch (xmlChild.getLocalName())
			{
				case TAG_PARAM -> parseToDo(node, sourceState);
				case TAG_FINALIZE -> parseToDo(node, sourceState);
				case TAG_CONTENT -> parseToDo(node, sourceState);
				default -> debug("Unsupported tag %s", xmlChild.getLocalName());
			}
		}

		sourceState.invoke.add(invoke);
	}

	/**
	 * Parse an initial transition node.
	 *
	 * @param node        The node.
	 * @param sourceState The parent-state
	 * @throws ParserException in case something was wrong with the file.
	 */
	protected void parseInitialTransition(XmlTag node, State sourceState) throws ParserException
	{
		Transition t = parseTransitionWithAttributes(node);

		if (sourceState.initial != null)
		{
			throw new ParserException("<initial> must not be specified if initial-attribute was given");
		}

		t.source = sourceState;
		sourceState.initial = t;

		debug(String.format("Initial %s", t));
	}

	/**
	 * Parse a transition node.
	 *
	 * @param node        The node.
	 * @param sourceState The parent state.
	 * @throws ParserException in case something was wrong with the file.
	 */
	protected void parseTransition(XmlTag node, State sourceState) throws ParserException
	{
		Transition t = parseTransitionWithAttributes(node);
		t.source = sourceState;
		sourceState.transitions.add(t);
		debug(String.format("Transition %s", t));
	}

	/**
	 * Common part of parsing some transition node (initial or transition), including attributes and executable content.
	 *
	 * @param node The transition node
	 * @return The transition (not added to state)
	 * @throws ParserException in case something was wrong with the file.
	 */
	protected Transition parseTransitionWithAttributes(XmlTag node) throws ParserException
	{
		Transition t = new Transition();

		t.docId = ++docIdCounter;

		parseSymbolList(getOptionalAttribute(node, TAG_EVENT), t.events);
		t.cond = getOptionalAttribute(node, ATTR_COND);
		parseStateSpecification(getOptionalAttribute(node, ATTR_TARGET), t.target);

		t.transitionType = mapTransitionType(getOptionalAttribute(node, ATTR_TYPE));
		t.content = parseExecutableContentBlock(node);

		return t;
	}

	/**
	 * Mapping from transition type name to enum.
	 */
	protected static final Map<String, TransitionType> transitionTypeName =
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
			typeValue = transitionTypeName.get(type);
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
	protected ExecutableContent parseExecutableContentBlock(XmlTag node) throws ParserException
	{
		ExecutableContent c = null;

		for (ScxmlChildIterator it = new ScxmlChildIterator(node); it.hasNext(); )
		{
			c = parseExecutableContentElement(it.next(), c);
		}
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
	protected ExecutableContent parseExecutableContentElement(XmlTag xmlChild, ExecutableContent previous) throws ParserException
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
	protected ExecutableContent parseRaise(XmlTag node, ExecutableContent prev)
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
	protected ExecutableContent parseIf(XmlTag node, ExecutableContent prev) throws ParserException
	{
		If ifC = new If(getRequiredAttribute(node, ATTR_COND));

		Block currentBlock = new Block();
		ifC.content = currentBlock;
		If currentIf = ifC;

		boolean elseSeen = false;

		for (ScxmlChildIterator it = new ScxmlChildIterator(node); it.hasNext(); )
		{
			XmlTag xmlChild = it.next();
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
		return chainExecutableContent(prev, ifC);
	}

	/**
	 * Parse a &lt;foreach&gt; element.
	 *
	 * @param node The XML node.
	 * @param prev The previous content in the current chain.
	 * @return The executable content.
	 */
	protected ExecutableContent parseForEach(XmlTag node, ExecutableContent prev)
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
	protected ExecutableContent parseLog(XmlTag node, ExecutableContent prev) throws ParserException
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
	protected ExecutableContent parseAssign(XmlTag node, ExecutableContent prev)
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
	protected ExecutableContent parseScript(XmlTag node, ExecutableContent prev)
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
	protected ExecutableContent parseSend(XmlTag node, ExecutableContent prev)
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
	protected ExecutableContent parseCancel(XmlTag node, ExecutableContent prev)
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
	protected void parseToDo(XmlTag tag, State state)
	{
		log.warn(String.format("Not yet handled: %s [state %s]", tag.getLocalName(), state.name));
	}

	/**
	 * Generates an internal id. Used in case an id is missing.
	 *
	 * @return The generated Id.
	 */
	public String generateId()
	{
		idCount += 1;
		return String.format("__id%d", idCount);
	}

	/**
	 * Get or creates a state for a state-node. Sets all attributes.
	 *
	 * @param node     The XML node of the state.
	 * @param parallel True if this is a parallel state.
	 * @param parent   The parent state.
	 * @return The corresponding state.
	 */
	protected State getOrCreateStateWithAttributes(XmlTag node, boolean parallel, State parent)
	{
		String sname = getAttributeOrCompute(node, ATTR_ID, this::generateId);
		State state = getOrCreateState(sname, parallel);
		String initial = node.getAttributeValue(ATTR_INITIAL, NS_SCXML);

		state.docId = ++docIdCounter;

		if (initial != null)
		{
			// Create initial-transition with the initial states
			Transition t = new Transition();
			t.docId = ++docIdCounter;
			t.transitionType = TransitionType.Internal;
			t.source = state;
			state.initial = t;
			parseStateSpecification(initial, t.target);
		}

		if (parent != null)
		{
			state.parent = parent;
			debug("state %s parent %s", state.toString(), parent.name);
			if (!parent.states.contains(state))
			{
				parent.states.add(state);
			}
		}
		else
		{
			debug("state %s no parent", state.toString());
		}
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
		State state = fsm.states.get(sname);
		if (state == null)
		{
			state = new State();
			state.name = sname;
			fsm.states.put(sname, state);
		}
		if (parallel)
			state.isParallel = parallel;
		return state;
	}

	/**
	 * Gets an optional attribute.
	 *
	 * @param tag       The Element to get the attribute from.
	 * @param attribute The case-sensitive name of the attribute.
	 * @return The found attribute value or null
	 */
	public String getOptionalAttribute(XmlTag tag, String attribute)
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
	public String getAttributeOrDefault(XmlTag tag, String attribute, String defaultValue)
	{
		String value = tag.getAttributeValue(attribute, NS_SCXML);
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
	public String getAttributeOrCompute(XmlTag tag, String attribute, Supplier<String> supplier)
	{
		String value = tag.getAttributeValue(attribute, NS_SCXML);
		return value == null ? supplier.get() : value;
	}

	/**
	 * Gets a required attribute.
	 *
	 * @param tag       The Element to get the attribute from.
	 * @param attribute The case-sensitive name of the attribute.
	 * @return The found attribute value.
	 * @throws ParserException If attribute is missing
	 */
	public String getRequiredAttribute(XmlTag tag, String attribute) throws ParserException
	{
		final String value = tag.getAttributeValue(attribute, NS_SCXML);
		if (value == null)
			throw new ParserException(ModelDriveBundle.message("parser.error.missing_attribute", attribute, tag.getLocalName()));
		return value;
	}

	/**
	 * Id generator if ids are missing
	 */
	int idCount = 0;

	/**
	 * Document-order-Id generator.
	 */
	int docIdCounter = 0;

	/**
	 * Parse a state-specification, a white-space separated list of stare references.
	 *
	 * @param targetName The list of stare references
	 * @param targets    A list to add the states to.
	 */
	protected void parseStateSpecification(String targetName, java.util.List<State> targets)
	{
		Arrays.stream(targetName.split("(?U)\s"))
			  .forEach(t -> targets.add(getOrCreateState(t, false)));
	}

	/**
	 * Parse a boolean value.
	 *
	 * @param value The boolean value.
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
		if (eventNames != null)
		{
			Arrays.stream(eventNames.split("(?U)\s"))
				  .forEach(events::add);
		}
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
		log.warn(String.format(format, args));
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

}
