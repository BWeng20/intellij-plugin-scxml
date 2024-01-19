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
	public float connectorSize = 5;

	/**
	 * Size of state corners.
	 */
	public float stateCornerArcSize = 20;

	/**
	 * Maximum distance to select an edge.
	 */
	public float selectEdgeMaxDistance = 10;

	/**
	 * Minimal width of states in pixel.
	 */
	public float stateMinimalWidth = 50;

	/**
	 * The minimal inner model box dimension inside the parent visual.
	 */
	public Dimension2DFloat innerModelBoxMinDimension = new Dimension2DFloat(200, 200);

	/**
	 * Insets of the inner model box inside the parent visual.
	 */
	public InsetsFloat innerModelBoxInsets = new InsetsFloat(20, 10, 10, 10);

	/**
	 * Graph use buffering for complex figures.
	 */
	public boolean buffered = false;

	/**
	 * Graph uses buffers to render elements.
	 */
	public boolean antialiasing = false;

	/**
	 * Installs a wheel-listener that zooms by mouse-wheel if Meta/Ctrl-Key is hold.
	 */
	public boolean zoomByMetaMouseWheelEnabled = true;

	/**
	 * The current scale of the graph.
	 */
	public float scale = 1f;

	/**
	 * Background for the graph.
	 */
	public Paint graphBackground;

}
