package com.bw.modeldrive.parser;

import com.bw.modeldrive.ModelDriveBundle;
import com.bw.modeldrive.model.BindingType;
import com.bw.modeldrive.model.FinitStateMachine;
import com.bw.modeldrive.model.State;
import com.bw.modeldrive.model.Transition;
import com.bw.modeldrive.model.TransitionType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.openapi.diagnostic.Logger;

import java.util.Arrays;
import java.util.function.Supplier;

public class XmlParser implements ScxmlTags
{
	protected FinitStateMachine fsm;

	private static final Logger log = Logger.getInstance(XmlParser.class);

	public FinitStateMachine parse(XmlFile xml) throws ParserException
	{
		XmlTag root = xml.getRootTag();
		if ( ScxmlTags.TAG_SCXML.equals(root.getLocalName()) ){

			fsm = new FinitStateMachine();

			fsm.name = root.getAttributeValue(ATTR_NAME);
			fsm.datamodel = root.getAttributeValue(ATTR_DATAMODEL);
			fsm.binding = BindingType.valueOf(getAttributeOrDefault(root, ATTR_BINDING, BindingType.Early.name()));
			fsm.pseudoRoot = parseState(root, false, null);
		} else {
			throw new ParserException(ModelDriveBundle.message("parser.error.root_tag_is_not_scxml", root.getLocalName()), null);
		}
		return null;
	}

	protected State parseState(XmlTag node, boolean parallel, State parent ) {
		State state = getOrCreateStateWithAttributes(node, parallel, parent);

		PsiElement child = node.getFirstChild();
		while ( child != null ) {

			if ( child instanceof XmlTag xmlChild)
			{
				if ( NS_SCXML.equals(xmlChild.getNamespace()) )
				{
					switch (xmlChild.getLocalName())
					{
						case TAG_STATE -> parseState(xmlChild, false, state);
						case TAG_PARALLEL -> parseState(xmlChild, true, state);
						default -> debug("Unsupported tag %s", xmlChild.getLocalName() );
					}
				}
			}


			child = child.getNextSibling();
		}

		return state;
	}

	public String generateId() {
		idCount += 1;
		return String.format("__id%d", idCount);
	}

	protected State getOrCreateStateWithAttributes(XmlTag node, boolean parallel, State parent)
	{
		String sname = getAttributeOrCompute( node, ATTR_ID, this::generateId );
		State state = getOrCreateState(sname, parallel);
		String initial = node.getAttributeValue( ATTR_INITIAL, NS_SCXML );

		if (initial != null) {
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

		if (parent != null) {
			state.parent = parent;
			debug("state %s %sparent %s", state.name, state.isParallel? "(parallel) ": "", parent.name);
			if (!parent.states.contains(state)) {
				parent.states.add(state);
			}
		} else {
			debug("state %s %sno parent", state.name,  parallel?"(parallel) ":"");
		}
		return state;
	}

	/**
	 * Get or create a state for the given name.
	 * @param sname The unique name of the state.
	 * @param parallel True if the state is a &lt;parallel&gt;
	 * @return The state
	 */
	public State getOrCreateState(String sname, boolean parallel)
	{
		State state = fsm.states.get(sname);
		if ( state == null ) {
			state = new State();
			state.name = sname;
			fsm.states.put(sname, state);
		}
		if ( parallel )
			state.isParallel = parallel;
		return state;
	}

	public String getAttributeOrDefault( XmlTag tag, String attribute, String defaultValue) {
		String value = tag.getAttributeValue(attribute, NS_SCXML);
		return value == null ? defaultValue : value;
	}

	public String getAttributeOrCompute( XmlTag tag, String attribute, Supplier<String> supplier) {
		String value = tag.getAttributeValue(attribute, NS_SCXML);
		return value == null ? supplier.get() : value;
	}

	int idCount = 0;
	int docIdCounter = 0;

	protected void parseStateSpecification(String targetName, java.util.List<State> targets) {
		Arrays.stream(targetName.split("(?U)\s")).forEach( t -> targets.add(getOrCreateState(t, false)));
	}

	protected void debug( String format, Object... args) {
		log.warn( String.format(format, args) );
	}

}
