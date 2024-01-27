package com.bw.modelthings.fsm.model;

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
		this._name = name;
		this._eType = type;
	}

	/**
	 * The name of the event.
	 */
	public final String _name;

	/**
	 * The type of the event.
	 */
	public final EventType _eType;

	/**
	 * The send-id of the event.
	 */
	public Integer _sendId;

	/**
	 * The origin of the event.
	 */
	public String _origin;

	/**
	 * The type of origin of the event.
	 */
	public String _originType;

	/**
	 * The invoke-id of the event.
	 */
	public String _invokeId;

	/**
	 * The DoneData of the final-state that triggered the event.
	 */
	public DoneData _data;
}
