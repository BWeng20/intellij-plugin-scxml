package com.bw.modeldrive;

import com.intellij.ide.actions.CreateFileFromTemplateAction;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsActions;
import com.intellij.psi.PsiDirectory;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

public class ScxmlNewFileAction extends CreateFileFromTemplateAction
{

	public ScxmlNewFileAction()
	{
		// Uses text from i18n resources.
		this(null, null, null);
	}

	public ScxmlNewFileAction(@NlsActions.ActionText String text, @NlsActions.ActionDescription String description, Icon icon)
	{
		super(text, description, icon);
	}

	@Override
	protected void buildDialog(Project project, PsiDirectory directory,
							   CreateFileFromTemplateDialog.Builder builder)
	{
		builder
				.setTitle("New SCXML File")
				.addKind("XML", Icons.SCXML, "SCXML.xml");
	}

	@Override
	protected String getActionName(PsiDirectory directory,
								   @NotNull String newName, String templateName)
	{
		return "Create SCXML: " + newName;
	}
}
