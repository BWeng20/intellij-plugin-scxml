package com.bw.modeldrive.model;

/**
 * Type of events.
 */
public enum EventType
{
	/**
	 * For events raised by the platform itself, such as error events
	 */
	platform,

	/**
	 * For events raised by &lt;raise&gt; and &lt;send&gt; with target '_internal'
	 */
	internal,

	/**
	 * For all other events
	 */
	external
}
