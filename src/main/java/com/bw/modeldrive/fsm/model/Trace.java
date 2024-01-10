package com.bw.modeldrive.fsm.model;

/**
 * Trace modes
 */
public enum Trace
{
	/**
	 * Trace methode calls.
	 */
	METHODS,

	/**
	 * Trace state transitions.
	 */
	STATES,

	/**
	 * Trace events.
	 */
	EVENTS,

	/**
	 * Trace also arguments for methods.
	 */
	ARGUMENTS,

	/**
	 * Trace also results for methods.
	 */
	RESULTS,

	/**
	 * Trace all above.
	 */
	ALL;
}
