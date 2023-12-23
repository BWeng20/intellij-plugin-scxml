package com.bw.modeldrive.model;

public class Id
{
	public final int _id;

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
