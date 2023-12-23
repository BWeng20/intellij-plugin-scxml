package com.bw.modeldrive.model;

/**
 * Holds all data of an Event.
 */
public class Event
{
	/** The name of the event. */
	public String name;

	/** The type of the event. */
	public EventType etype;

	/** The send-id of the event. */
	public Integer sendid;

	/** The origin of the event. */
	public String origin;

	/** The type of origin of the event. */
	public String origintype;

	/** The invoke-id of the event. */
	public InvokeId invokeid;

	/** The DoneData of the final-state that triggered the event. */
	public DoneData data;
}
