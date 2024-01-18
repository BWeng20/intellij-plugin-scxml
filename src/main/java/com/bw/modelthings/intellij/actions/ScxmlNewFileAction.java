package com.bw.modelthings.intellij.actions;

import com.bw.modelthings.intellij.Icons;
import com.intellij.ide.actions.CreateFileFromTemplateAction;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsActions;
import com.intellij.psi.PsiDirectory;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

/**
 * Action to create a new empty SCXML file.
 */
public class ScxmlNewFileAction extends CreateFileFromTemplateAction
{

	/**
	 * Creates the action with values from i18n resources.
	 */
	public ScxmlNewFileAction()
	{
		this(null, null, null);
	}

	/**
	 * Creates the action.
	 *
	 * @param description The description
	 * @param text        The text
	 * @param icon        The icon
	 */
	public ScxmlNewFileAction(@NlsActions.ActionText String text, @NlsActions.ActionDescription String description, Icon icon)
	{
		super(text, description, icon);
	}

	@Override
	protected void buildDialog(Project project, PsiDirectory directory,
							   CreateFileFromTemplateDialog.Builder builder)
	{
		builder
				.setTitle("SCXML File")
				.addKind("XML", Icons.SCXML, "SCXML.xml");
	}

	@Override
	protected String getActionName(PsiDirectory directory,
								   @NotNull String newName, String templateName)
	{
		return "Create SCXML: " + newName;
	}
}
