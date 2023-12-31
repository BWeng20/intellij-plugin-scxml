package com.bw.modeldrive.editor;

import com.bw.modeldrive.model.FiniteStateMachine;
import com.bw.modeldrive.parser.ParserException;
import com.bw.modeldrive.parser.XmlParser;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import java.beans.PropertyChangeListener;

/**
 * SCXML Editor.
 */
public class ScxmlGraphEditor extends UserDataHolderBase implements FileEditor
{
	/**
	 * The Graphical Editor component.
	 */
	final ScxmlGraphPanel component;

	/**
	 * The file that is shown.
	 */
	final VirtualFile file;

	/**
	 * The XML file that is shown (Psi file for {@link #file}).
	 */
	XmlFile xmlFile;

	/**
	 * The document of the xml file.
	 */
	Document xmlDocument;

	/**
	 * Counts reported document changes.
	 */
	int documentChanges = 0;


	/**
	 * Listener for document changes.
	 */
	com.intellij.openapi.editor.event.DocumentListener documentListener = new DocumentListener()
	{
		@Override
		public void documentChanged(@NotNull DocumentEvent event)
		{
			log.warn("Document Event " + event);
			triggerUpdate();
		}
	};

	/**
	 * Triggers a background update of the graph.
	 */
	protected void triggerUpdate()
	{
		++documentChanges;
		// Executes in worker thread with read-lock.
		ApplicationManager.getApplication()
						  .executeOnPooledThread(() ->
								  ApplicationManager.getApplication()
													.runReadAction(() ->
													{
														if (documentChanges > 0 && xmlFile != null)
														{
															if (documentChanges > 1)
															{
																log.warn("Accumulated document changes " + documentChanges);
															}
															documentChanges = 0;
															try
															{
																final FiniteStateMachine fsm = new XmlParser().parse(xmlFile);
																ApplicationManager.getApplication()
																				  .invokeLater(() -> component.setStateMachine(fsm));
															}
															catch (ProcessCanceledException pce)
															{
																throw pce;
															}
															catch (ParserException pe)
															{
																component.setError(pe);
															}
														}
													})
						  );
	}

	private static final Logger log = Logger.getInstance(ScxmlGraphEditor.class);


	/**
	 * Creates a new editor.
	 *
	 * @param file    The original file
	 * @param psiFile The matching psi file.
	 */
	public ScxmlGraphEditor(@NotNull VirtualFile file, @Nullable PsiFile psiFile)
	{
		component = new ScxmlGraphPanel();
		this.file = file;
		setXmlFile((psiFile instanceof XmlFile) ? ((XmlFile) psiFile) : null);
	}

	/**
	 * Sets the XML Psi file.
	 *
	 * @param xmlFile The file, can be null.
	 */
	public void setXmlFile(XmlFile xmlFile)
	{
		if (this.xmlDocument != null)
		{
			this.xmlDocument.removeDocumentListener(documentListener);
		}
		this.xmlFile = xmlFile;
		if (xmlFile == null)
		{
			component.setError(null);
		}
		else
		{
			xmlDocument = PsiDocumentManager.getInstance(xmlFile.getProject())
											.getDocument(xmlFile);
			if (xmlDocument != null)
			{
				log.warn("Document " + xmlDocument + " " + xmlDocument.getClass()
																	  .getName());
				xmlDocument.addDocumentListener(documentListener);
			}
			triggerUpdate();
		}
	}

	@Override
	public @NotNull JComponent getComponent()
	{
		return component;
	}

	@Override
	public @Nullable JComponent getPreferredFocusedComponent()
	{
		return component;
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
		component.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(@NotNull PropertyChangeListener listener)
	{
		component.removePropertyChangeListener(listener);
	}

	@Override
	public VirtualFile getFile()
	{
		return file;
	}

	@Override
	public void dispose()
	{
	}
}
