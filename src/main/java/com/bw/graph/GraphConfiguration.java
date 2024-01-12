package com.bw.graph;

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
	 * Maximum distance to select an edge.
	 */
	public float selectEdgeMaxDistance = 10;

	/**
	 * Graph is shown with antialiasing if true.
	 */
	public boolean doubleBuffered = false;

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
