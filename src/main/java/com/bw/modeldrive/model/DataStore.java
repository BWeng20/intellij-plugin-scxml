package com.bw.modeldrive.model;

import java.util.HashMap;

/**
 * Hold data. Simple wrapper around a hash-map.
 */
public class DataStore
{
	/**
	 * Create a new empty data-store.
	 */
	public DataStore()
	{

	}

	/**
	 * The map that holds the data.
	 */
	public final HashMap<String, Data> values = new HashMap<>();
}
