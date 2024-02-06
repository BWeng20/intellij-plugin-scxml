package com.bw.modelthings.fsm.ui;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores layout information about transitions or initials.
 */
public class TransitionDescription
{
	public String _nameList;

	/**
	 * Relative location of target connector.
	 */
	public Point2D.Float _relativeTargetConnector;

	/**
	 * Relative location of source connector.
	 */
	public Point2D.Float _relativeSourceConnector;

	/**
	 * Absolute locations of path control.
	 */
	public List<Point2D.Float> _pathControlPoints = new ArrayList<>();
}
