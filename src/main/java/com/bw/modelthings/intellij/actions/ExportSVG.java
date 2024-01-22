package com.bw.modelthings.intellij.actions;

import com.bw.modelthings.intellij.ScXmlSdkBundle;
import com.bw.modelthings.intellij.editor.ScxmlGraphPanel;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import org.jetbrains.annotations.NotNull;

import java.awt.Component;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

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

	VirtualFile lastVF;

	@Override
	public void actionPerformed(@NotNull AnActionEvent e)
	{
		Component c = e.getData(PlatformCoreDataKeys.CONTEXT_COMPONENT);
		if (c instanceof ScxmlGraphPanel)
		{
			FileSaverDescriptor d = new FileSaverDescriptor(
					ScXmlSdkBundle.message("action.ScXmlEditor.SvgExport.chooser.title"),
					ScXmlSdkBundle.message("action.ScXmlEditor.SvgExport.chooser.description"), "svg");
			VirtualFileWrapper fileWrapper =
					FileChooserFactory.getInstance().createSaveFileDialog(d, e.getProject())
									  .save(lastVF == null ? null : lastVF.getParent(), lastVF == null ? null : lastVF.getName());

			if (fileWrapper != null)
			{
				String svg = ((ScxmlGraphPanel) c).getSVG();
				try (FileOutputStream fs = new FileOutputStream(fileWrapper.getFile()))
				{
					fs.write(svg.getBytes(StandardCharsets.UTF_8));
					fs.flush();
					lastVF = fileWrapper.getVirtualFile();
				}
				catch (IOException ex)
				{
				}
			}
		}
	}

	@Override
	public void update(@NotNull final AnActionEvent e)
	{
		e.getPresentation()
		 .setEnabledAndVisible(true);
	}

	@Override
	public @NotNull ActionUpdateThread getActionUpdateThread()
	{
		return ActionUpdateThread.EDT;
	}
}
