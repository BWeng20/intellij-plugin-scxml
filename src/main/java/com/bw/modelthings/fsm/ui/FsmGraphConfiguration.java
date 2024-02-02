package com.bw.modelthings.fsm.ui;

import com.bw.graph.GraphConfiguration;

/**
 * Finite Statemachine specific graph-configuration.
 */
public class FsmGraphConfiguration extends GraphConfiguration
{
	/**
	 * Creates a new instance with default values.
	 */
	public FsmGraphConfiguration()
	{
	}

	/**
	 * Minimal width of states in pixel.
	 */
	public float _stateMinimalWidth = 50;

}
