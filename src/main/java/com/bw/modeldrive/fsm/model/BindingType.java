package com.bw.modeldrive.fsm.model;

/**
 * Data model binding type,
 */
public enum BindingType
{
	/**
	 * <h4>W3C says:</h4>
	 * The SCXML Processor must create all data elements and assign their initial values at document initialization time.
	 */
	Early,

	/**
	 * <h4>W3C says:</h4>
	 * The SCXML Processor must create the data elements at document initialization time,
	 * but must assign the specified initial value to a given data element only when the state that contains it is
	 * entered for the first time, before any &lt;onentry&gt; markup.<br>
	 * (The value of the data element between the time it is created and the time its parent state is first entered
	 * will depend on the data language chosen. The initial value specified by 'expr', 'src' or in-line content will
	 * be assigned to the data element even if the element already has a non-null value when the parent state is
	 * first entered.)
	 */
	Late
}
