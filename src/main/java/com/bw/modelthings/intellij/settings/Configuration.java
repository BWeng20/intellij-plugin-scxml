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
	public boolean _antialiasing;

	/**
	 * Graph uses buffers to render elements.
	 */
	public boolean _buffered;

	/**
	 * Enables zooms by mouse-wheel if Meta/Ctrl-Key is hold.
	 */
	public boolean _zoomByMetaMouseWheelEnabled;

	/**
	 * Editor Layout mode.
	 */
	public EditorLayout _editorLayout = EditorLayout.Tabs;

}
