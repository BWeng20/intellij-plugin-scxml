package com.bw.modeldrive.model;

/**
 * The type of history.<br>
 * Determines whether the active atomic substate(s) of the current state or
 * only its immediate active substate(s) are recorded.<br>
 */
public enum HistoryType
{
	/**
	 * If the 'type' of a &lt;history&gt; element is "shallow", the SCXML processor must
	 * record the immediately active children of its parent before taking any transition that exits the parent.
	 */
	Shallow,

	/**
	 * If the 'type' of a &lt;history&gt; element is "deep", the SCXML processor must record the active atomic descendants
	 * of the parent before taking any transition that exits the parent.
	 */
	Deep,

	/** History is not set. */
	None;
}
