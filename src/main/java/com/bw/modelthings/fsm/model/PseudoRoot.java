package com.bw.modelthings.fsm.model;

/**
 * Holds the representation of the pseudo root state.
 */
public class PseudoRoot extends State
{

	/**
	 * Creates a new state.
	 */
	public PseudoRoot()
	{
	}

	/**
	 * The state property {@link #name} of the pseudo root has a synthetic value,
	 * as attribute "name" from &lt;scxml&gt; is not guaranteed to be unique.<br>
	 * Instaed "fsmName" is set to "name".
	 */
	public String fsmName;


}
