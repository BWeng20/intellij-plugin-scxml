package com.bw.modeldrive.parser;

import com.bw.modeldrive.ModelDriveBundle;
import com.bw.modeldrive.model.BindingType;
import com.bw.modeldrive.model.ExecutableContent;
import com.bw.modeldrive.model.FiniteStateMachine;
import com.bw.modeldrive.model.State;
import com.bw.modeldrive.model.Transition;
import com.bw.modeldrive.model.TransitionType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;

import java.util.Arrays;
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
			throw new ParserException(ModelDriveBundle.message("parser.error.root_tag_is_not_scxml", root.getLocalName()), null);
		}
		return fsm;
	}

	/**
	 * Parse a state specification and all sub-elements.
	 *
	 * @param node     The State node.
	 * @param parallel True if this is a parallel state.
	 * @param parent   The parent state.
	 * @return The corresponding state.
	 */
	protected State parseState(XmlTag node, boolean parallel, State parent)
	{
		State state = getOrCreateStateWithAttributes(node, parallel, parent);

		PsiElement child = node.getFirstChild();
		while (child != null)
		{

			if (child instanceof XmlTag xmlChild)
			{
				if (NS_SCXML.equals(xmlChild.getNamespace()))
				{
					switch (xmlChild.getLocalName())
					{
						case TAG_ON_ENTRY -> state.onentry = parseExecutableContent(xmlChild);
						case TAG_ON_EXIT -> parseToDo(xmlChild, state);
						case TAG_TRANSITION -> parseToDo(xmlChild, state);
						case TAG_INITIAL -> parseToDo(xmlChild, state);
						case TAG_STATE -> parseState(xmlChild, false, state);
						case TAG_PARALLEL -> parseState(xmlChild, true, state);
						case TAG_FINAL -> parseState(xmlChild, false, state);
						case TAG_HISTORY -> parseToDo(xmlChild, state);
						case TAG_DATAMODEL -> parseToDo(xmlChild, state);
						case TAG_INVOKE -> parseToDo(xmlChild, state);
						default -> debug("Unsupported tag %s", xmlChild.getLocalName());
					}
				}
			}
			child = child.getNextSibling();
		}

		return state;
	}

	/**
	 * Parse executable content as in &lt;onentry&gt; or &lt;onexit&gt;.
	 *
	 * @param node The parent node of the content.
	 * @return The content
	 */
	protected ExecutableContent parseExecutableContent(XmlTag node)
	{
		ExecutableContent c = null;

		PsiElement child = node.getFirstChild();
		while (child != null)
		{
			if (child instanceof XmlTag xmlChild)
			{
				if (NS_SCXML.equals(xmlChild.getNamespace()))
				{
					switch (xmlChild.getLocalName())
					{
						case TAG_RAISE -> parseToDo(xmlChild, null);
						default -> debug("Unsupported tag %s", xmlChild.getLocalName());
					}
				}
			}
			child = child.getNextSibling();
		}

		return c;
	}


	protected void parseToDo(XmlTag tag, State state)
	{

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

		if (initial != null)
		{
			// Create initial-transition with the initial states
			Transition t = new Transition();
			t.docId = ++docIdCounter;
			t.transitionType = TransitionType.Internal;
			t.source = parent;
			state.initial = t;
			parseStateSpecification(initial, t.target);
			fsm.transitions.put(t.id, t);
		}

		state.docId = ++docIdCounter;

		if (parent != null)
		{
			state.parent = parent;
			debug("state %s %sparent %s", state.name, state.isParallel ? "(parallel) " : "", parent.name);
			if (!parent.states.contains(state))
			{
				parent.states.add(state);
			}
		}
		else
		{
			debug("state %s %sno parent", state.name, parallel ? "(parallel) " : "");
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

}
