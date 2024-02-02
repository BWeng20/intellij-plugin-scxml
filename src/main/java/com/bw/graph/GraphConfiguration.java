package com.bw.graph;

import com.bw.graph.util.Dimension2DFloat;
import com.bw.graph.util.InsetsFloat;

import java.awt.Paint;

/**
 * Graph configuration.
 */
public class GraphConfiguration
{

	/**
	 * Creates a new graph configuration.
	 */
	public GraphConfiguration()
	{
	}

	/**
	 * Size of connector visuals.
	 */
	public float _connectorSize = 5;

	/**
	 * Size of state corners.
	 */
	public float _stateCornerArcSize = 20;

	/**
	 * Maximum distance to a visual.
	 */
	public float _selectMaxDistance = 5;

	/**
	 * Maximal quadratic distance that a visual "snaps" into position during dragging.
	 */
	public float _snapMaxSquaredDistance = 10 * 10;

	/**
	 * The minimal dimension of s sub-model-box inside the parent visual.
	 */
	public Dimension2DFloat _innerModelBoxMinDimension = new Dimension2DFloat(200, 200);

	/**
	 * Insets of the sub-model-box inside the parent visual.
	 */
	public InsetsFloat _innerModelBoxInsets = new InsetsFloat(20, 10, 10, 10);

	/**
	 * Graph use buffering for complex figures.
	 */
	public boolean _buffered = false;

	/**
	 * Graph uses buffers to render elements.
	 */
	public boolean _antialiasing = false;

	/**
	 * Installs a wheel-listener that zooms by mouse-wheel if Meta/Ctrl-Key is hold.
	 */
	public boolean _zoomByMetaMouseWheelEnabled = true;

	/**
	 * The current scale of the graph.
	 */
	public float _scale = 1f;

	/**
	 * Background for the graph.
	 */
	public Paint _graphBackground;

	/**
	 * Factor for rounding floats.
	 */
	public float _precisionFactor = 10 * 10 * 10;
}
