package com.bw.modelthings.intellij.editor;

import com.intellij.ide.ui.customization.CustomActionsSchema;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;

/**
 * Graphical SCXML {@link FileEditor}.
 */
public class ScxmlGraphEditor extends UserDataHolderBase implements FileEditor
{
	private static final Logger LOG = Logger.getInstance(ScxmlGraphEditor.class);

	/**
	 * The Graphical Editor component.
	 */
	final ScxmlGraphPanel _component;

	/**
	 * The file that is shown.
	 */
	final VirtualFile _file;

	/**
	 * Creates a new editor.
	 *
	 * @param file    The original file
	 * @param psiFile The matching psi file.
	 */
	public ScxmlGraphEditor(@NotNull VirtualFile file, @Nullable PsiFile psiFile)
	{
		_component = new ScxmlGraphPanel(psiFile == null ? null : psiFile.getProject());
		this._file = file;

		_component.getGraphPane()
				  .addMouseListener(new MouseAdapter()
				  {
					  @Override
					  public void mouseReleased(MouseEvent event)
					  {
						  if (event.isPopupTrigger() && !event.isConsumed())
						  {
							  ActionGroup ag = (ActionGroup) CustomActionsSchema.getInstance()
																				.getCorrectedAction("ScXMLPopupMenu");
							  JPopupMenu popupMenu = ActionManager.getInstance()
																  .createActionPopupMenu(ActionPlaces.EDITOR_POPUP, ag)
																  .getComponent();
							  popupMenu.show(_component, event.getX(), event.getY());
						  }

					  }
				  });
	}

	@Override
	public @NotNull JComponent getComponent()
	{
		return _component;
	}

	@Override
	public @Nullable JComponent getPreferredFocusedComponent()
	{
		return _component;
	}

	@Override
	public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getName()
	{
		return "Scxml Graph";
	}

	@Override
	public void setState(@NotNull FileEditorState state)
	{
	}

	@Override
	public boolean isModified()
	{
		return false;
	}

	@Override
	public boolean isValid()
	{
		return true;
	}

	@Override
	public void addPropertyChangeListener(@NotNull PropertyChangeListener listener)
	{
		_component.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(@NotNull PropertyChangeListener listener)
	{
		_component.removePropertyChangeListener(listener);
	}

	@Override
	public VirtualFile getFile()
	{
		return _file;
	}

	@Override
	public void dispose()
	{
		if (_component != null)
			_component.dispose();
	}
}
