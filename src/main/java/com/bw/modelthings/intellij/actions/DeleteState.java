package com.bw.modelthings.intellij.actions;

import com.bw.modelthings.fsm.model.State;
import com.bw.modelthings.intellij.editor.ScxmlGraphPanel;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

import java.awt.Component;

/**
 * Action to delete the current selected State from the FSM.
 */
public class DeleteState extends AnAction implements DumbAware
{

	/**
	 * Creates a new instance. Called by platform.
	 */
	public DeleteState()
	{

	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent anActionEvent)
	{
		Component c = anActionEvent.getData(PlatformCoreDataKeys.CONTEXT_COMPONENT);
		if (c instanceof ScxmlGraphPanel panel)
		{
			State state = panel.getSelectedState();
			if (state != null)
			{
				panel.removeState(state, true);
			}
		}
	}

	@Override
	public void update(@NotNull final AnActionEvent anActionEvent)
	{
		boolean enable = false;
		Component c = anActionEvent.getData(PlatformCoreDataKeys.CONTEXT_COMPONENT);
		if (c instanceof ScxmlGraphPanel panel)
		{
			enable = null != panel.getSelectedState();
		}
		anActionEvent.getPresentation()
					 .setEnabledAndVisible(enable);
	}

	@Override
	public @NotNull ActionUpdateThread getActionUpdateThread()
	{
		return ActionUpdateThread.EDT;
	}

}
