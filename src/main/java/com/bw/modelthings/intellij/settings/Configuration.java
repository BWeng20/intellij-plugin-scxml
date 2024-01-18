package com.bw.modelthings.intellij.settings;

/**
 * ScXML configuration.
 */
public class Configuration
{

	/**
	 * Create a new Configuration.
	 */
	public Configuration()
	{

	}

	/**
	 * Graph is shown with antialiasing if true.
	 */
	public boolean antialiasing;

	/**
	 * Graph uses buffers to render elements.
	 */
	public boolean buffered;

	/**
	 * Enables zooms by mouse-wheel if Meta/Ctrl-Key is hold.
	 */
	public boolean zoomByMetaMouseWheelEnabled;

	/**
	 * Editor Layout mode.
	 */
	public EditorLayout editorLayout = EditorLayout.Tabs;

}
