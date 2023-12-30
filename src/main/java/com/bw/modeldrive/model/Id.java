package com.bw.modeldrive.model;

/**
 * Base class for typesafe identification members.
 */
public class Id
{
	/**
	 * The id.
	 */
	public final int _id;

	/**
	 * Creates a new id.
	 *
	 * @param id Id value.
	 */
	protected Id(int id)
	{
		_id = id;
	}

	@Override
	public int hashCode()
	{
		return _id;
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof Id otherId)
			return _id == otherId._id;
		else
			return false;
	}

	@Override
	public String toString()
	{
		return Integer.toString(_id);
	}
}
