package com.bw.modeldrive.model;

/**
 * Holds all data of an Event.
 */
public class Event
{
	/**
	 * Creates a new event with minimal values.
	 *
	 * @param name The name
	 * @param type The type.
	 */
	public Event(String name, EventType type)
	{
		this.name = name;
		this.etype = type;
	}

	/**
	 * The name of the event.
	 */
	public final String name;

	/**
	 * The type of the event.
	 */
	public final EventType etype;

	/**
	 * The send-id of the event.
	 */
	public Integer sendid;

	/**
	 * The origin of the event.
	 */
	public String origin;

	/**
	 * The type of origin of the event.
	 */
	public String origintype;

	/**
	 * The invoke-id of the event.
	 */
	public String invokeid;

	/**
	 * The DoneData of the final-state that triggered the event.
	 */
	public DoneData data;
}
