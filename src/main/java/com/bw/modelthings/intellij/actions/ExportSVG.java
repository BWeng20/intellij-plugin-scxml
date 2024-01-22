package com.bw.modelthings.intellij.actions;

import com.bw.modelthings.intellij.editor.ScxmlGraphPanel;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

import java.awt.Component;

/**
 * Action to export the current model as SVG.
 */
public class ExportSVG extends AnAction implements DumbAware
{

	/**
	 * Creates the action. Called by platform.
	 */
	public ExportSVG()
	{
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e)
	{
		Component c = e.getData(PlatformCoreDataKeys.CONTEXT_COMPONENT);
		if (c instanceof ScxmlGraphPanel)
		{
			System.err.println(((ScxmlGraphPanel) c).getSVG());
		}
	}

	@Override
	public void update(@NotNull final AnActionEvent e)
	{
		e.getPresentation()
		 .setEnabledAndVisible(true);
	}
}
