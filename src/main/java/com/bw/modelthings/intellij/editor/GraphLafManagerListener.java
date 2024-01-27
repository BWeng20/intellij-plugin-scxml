package com.bw.modelthings.intellij.editor;

import com.intellij.ide.ui.LafManager;
import com.intellij.ide.ui.LafManagerListener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * LafManagerListener to update Visual settings if LAF was changed.<br>
 * See plugin.xml.
 */
class GraphLafManagerListener implements LafManagerListener
{
	private final static List<GraphLafListener> LISTENERS = Collections.synchronizedList(new ArrayList<>());

	/**
	 * Creates the listener. Called by platform.
	 */
	GraphLafManagerListener()
	{
	}

	@Override
	public void lookAndFeelChanged(@NotNull LafManager lafManager)
	{
		List<GraphLafListener> l = new ArrayList<>(LISTENERS);
		for (GraphLafListener gl : l)
			gl.lafChanged();

	}

	/**
	 * Adds an app specific listener, used by editors to update visual settings.
	 *
	 * @param listener The listener.
	 */
	public static void addGraphLafListener(GraphLafListener listener)
	{
		GraphLafManagerListener.LISTENERS.remove(listener);
		GraphLafManagerListener.LISTENERS.add(listener);
	}

	/**
	 * Removes the app specific listener, called by editors on dispose.
	 *
	 * @param listener The listener to remove.
	 */
	public static void removeGraphLafListener(GraphLafListener listener)
	{
		GraphLafManagerListener.LISTENERS.remove(listener);
	}
}
