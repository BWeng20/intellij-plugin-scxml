package com.bw.modeldrive.intellij;

import com.intellij.openapi.util.IconLoader;

import javax.swing.Icon;

/**
 * Icons used by Plugin.
 */
public interface Icons
{
	/**
	 * Small icon used for decorators.
	 */
	Icon SCXML = IconLoader.getIcon("/icons/scxml.svg", Icons.class);

	/**
	 * Small State Machine icon.
	 */
	Icon STATE_MACHINE = IconLoader.getIcon("/icons/statemachine.svg", Icons.class);

}
