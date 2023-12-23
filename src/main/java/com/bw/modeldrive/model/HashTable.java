package com.bw.modeldrive.model;

import java.util.HashMap;

/**
 * <p>Implementation of the data-structures and algorithms described in the W3C scxml proposal.<br>
 * As reference each type and method has the w3c description as documentation.<br>
 * See <a href="https://www.w3.org/TR/scxml/#AlgorithmforSCXMLInterpretation">AlgorithmforSCXMLInterpretation</a>
 * </p>
 * <p>Structs and methods are designed to match the signatures in the W3c-Pseudo-code.</p><br>
 * <p><b>W3C HashTable declares operators:</b></p>
 * <ul><li>table[foo] returns the value associated with foo.</li>
 * <li>table[foo] = bar sets the value associated with foo to be bar.</li></ul>
 * As this is not possible with java, we are using methods.
 */
public final class HashTable<K, V>
{
	HashMap<K, V> _hashMap = new HashMap<>();

	public void put(K key, V value)
	{
		_hashMap.put(key, value);
	}

	public V get(K key)
	{
		return _hashMap.get(key);
	}
}
