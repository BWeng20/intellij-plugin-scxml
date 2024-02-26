package com.bw.modelthings.fsm.ui;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores layout information about transitions or initials.
 */
public class TransitionDescription
{

	/**
	 * Create a new TransitionDescription.
	 */
	public TransitionDescription()
	{

	}

	/**
	 * Relative location of target connector.
	 */
	public List<Point2D.Float> _relativeTargetConnectorPosition;

	/**
	 * Relative location of source connector.
	 */
	public Point2D.Float _relativeSourceConnectorPosition;

	/**
	 * Absolute locations of path control.
	 */
	public List<Point2D.Float> _pathControlPoints = new ArrayList<>();
}
