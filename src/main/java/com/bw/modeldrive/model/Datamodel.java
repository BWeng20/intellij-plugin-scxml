package com.bw.modeldrive.model;

/**
 * <strong>W3C says:</strong><br>
 * The Data Model offers the capability of storing, reading, and modifying a set of data that is internal to the state machine.
 * This specification does not mandate any specific data model, but instead defines a set of abstract capabilities that can
 * be realized by various languages, such as ECMAScript or XML/XPath. Implementations may choose the set of data models that
 * they support. In addition to the underlying data structure, the data model defines a set of expressions as described in
 * 5.9 Expressions. These expressions are used to refer to specific locations in the data model, to compute values to
 * assign to those locations, and to evaluate boolean conditions.<br>
 * Finally, the data model includes a set of system variables, as defined in 5.10 System Variables, which are automatically maintained
 * by the SCXML processor.
 */
public interface Datamodel
{
	/**
	 * Returns the global data.<br>
	 * As the datamodel needs access to other global variables and rust doesn't like
	 * accessing data of parents (Fsm in this case) from inside a member (the actual Datmodel), most global data is
	 * store in the "GlobalData" struct that is owned by the datamodel.
	 */
	GlobalData global();

	/**
	 * Get the name of this data model.
	 *
	 * @return The name. Never null.
	 */
	String getName();

	/**
	 * Initialize the datamodel for one data-store.
	 * This method is called for the global data and for the data of each state.
	 */
	void initializeDataModel(FiniteStateMachine fsm, State state);

	/**
	 * Sets a global variable.
	 */
	void set(String name, Data data);

	/**
	 * Gets a global variable.
	 */
	Data get(String name);

	/**
	 * Clear all.
	 */
	void clear();

	/**
	 * "log" function, use for &lt;log&gt; content.
	 */
	void log(String msg);

	/**
	 * Execute a script.
	 */
	void execute(FiniteStateMachine fsm, String script);

	/**
	 * Executges a for-each.
	 *
	 * @param fsm             The State Machine.
	 * @param arrayExpression The array expressions
	 * @param item            The item variable name
	 * @param index           The index variable name.
	 * @param executeBody     The code to execute.
	 */
	void executeForEach(FiniteStateMachine fsm, String arrayExpression, String item, String index, Executor executeBody);

	/**
	 * <strong>W3C says:</strong><br>
	 * The set of operators in conditional expressions varies depending on the data model,
	 * but all data models must support the 'In()' predicate, which takes a state ID as its
	 * argument and returns true if the state machine is in that state.<br>
	 * Conditional expressions in conformant SCXML documents should not have side effects.
	 * <strong>Actual Implementation:</strong><br>
	 * As no side-effects shall occur, this method should be some "const". But we assume that most script-engines have
	 * no read-only "eval" function and such method may be hard to implement.
	 */
	boolean executeCondition(FiniteStateMachine fsm, String script);

	void executeContent(FiniteStateMachine fsm, ExecutableContent content);

}
