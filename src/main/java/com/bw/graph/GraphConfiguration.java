package com.bw.graph;

import com.bw.graph.util.Dimension2DFloat;
import com.bw.graph.util.InsetsFloat;

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
	 * The inner model box dimension inside the parent visual.
	 */
	public Dimension2DFloat innerModelBoxDimension = new Dimension2DFloat(200, 200);

	/**
	 * Insets of the inner model box inside the parent visual.
	 */
	public InsetsFloat innerModelBoxInsets = new InsetsFloat(20, 10, 10, 10);

	/**
	 * Insets of the inner model inside its box.
	 */
	public InsetsFloat innerModelInsets = new InsetsFloat(5, 5, 5, 5);

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

}
