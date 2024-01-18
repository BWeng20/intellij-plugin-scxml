package com.bw.modelthings.intellij.editor;

import com.bw.XmlWriter;
import com.bw.graph.VisualModel;
import com.bw.graph.visual.GenericVisual;
import com.bw.graph.visual.Visual;
import com.bw.modelthings.fsm.model.FiniteStateMachine;
import com.bw.modelthings.fsm.parser.LogExtensionParser;
import com.bw.modelthings.fsm.parser.ParserException;
import com.bw.modelthings.fsm.parser.ScxmlTags;
import com.bw.modelthings.fsm.parser.XmlParser;
import com.bw.modelthings.fsm.ui.GraphExtension;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
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
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.Timer;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Graphical SCXML {@link FileEditor}.
 */
public class ScxmlGraphEditor extends UserDataHolderBase implements FileEditor
{
	private static final Logger log = Logger.getInstance(ScxmlGraphEditor.class);

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
	PsiFile xmlFile;

	/**
	 * The document of the xml file.
	 */
	Document xmlDocument;

	/**
	 * True if an update of the graph was triggered.
	 */
	boolean updateGraphTriggered = false;

	/**
	 * True if an update of the XML file was triggered.
	 */
	boolean updateXmlTriggered = false;

	/**
	 * Sync to document is in progress, if this flag is true.
	 */
	boolean inDocumentSync = false;

	/**
	 * Timer to trigger graph to document synchronization.
	 */
	Timer updateXmlTimer;

	/**
	 * Listener for document changes.
	 */
	com.intellij.openapi.editor.event.DocumentListener documentListener = new DocumentListener()
	{
		@Override
		public void documentChanged(@NotNull DocumentEvent event)
		{
			if (inDocumentSync)
			{
				log.warn("(Sync) Ignored Document Event " + event);
			}
			else
			{
				log.warn("Document Event " + event);
				triggerGraphUpdate();
			}
		}
	};

	/**
	 * Triggers a background update of the graph.
	 */
	protected void triggerGraphUpdate()
	{
		if (!updateGraphTriggered)
		{
			updateGraphTriggered = true;
			// Executes in worker thread with read-lock.
			ApplicationManager.getApplication()
							  .executeOnPooledThread(() -> ApplicationManager.getApplication()
																			 .runReadAction(this::runGraphUpdate));
		}
	}

	/**
	 * Triggers a background update of the XML file.
	 */
	protected void triggerXmlUpdate()
	{
		if (!updateXmlTriggered)
		{
			updateXmlTriggered = true;
			ApplicationManager.getApplication().invokeLater(() -> WriteCommandAction.runWriteCommandAction(xmlFile.getProject(), this::runXmlUpdate));
		}
	}


	/**
	 * This method is synchronized because it is possible that this method is called from different worker threads in parallel.
	 */
	private synchronized void runXmlUpdate()
	{
		// @TODO: move calculation of graphextensions to worker thread, then use the result here in write action.
		if (updateXmlTriggered)
		{
			updateXmlTriggered = false;
			try
			{
				inDocumentSync = true;
				PsiDocumentManager.getInstance(xmlFile.getProject()).commitDocument(xmlDocument);

				VisualModel model = (component.root == null) ? null : component.root.getSubModel();
				if (xmlFile != null && model != null)
				{
					HashMap<String, Rectangle2D.Float> bounds = new HashMap<>();

					List<Visual> visuals = new ArrayList<>(model.getVisuals());

					while (!visuals.isEmpty())
					{
						Visual v = visuals.remove(visuals.size() - 1);
						if (v instanceof GenericVisual)
						{
							String id = (String) v.getId();
							if (id != null)
							{
								bounds.put(id, v.getBounds2D(null));
							}
						}
						VisualModel m = v.getSubModel();
						if (m != null)
						{
							visuals.addAll(m.getVisuals());
						}
					}
					try
					{
						float precisionFactor = 1000;
						XmlDocument doc = ((XmlFile) xmlFile).getDocument();
						XmlTag root = doc.getRootTag();
						if (root != null)
						{
							List<XmlTag> allStates = new ArrayList<>(100);
							allStates.add(root);

							while (!allStates.isEmpty())
							{
								XmlTag s = allStates.remove(allStates.size() - 1);
								String id = s.getAttributeValue(ScxmlTags.ATTR_ID);

								allStates.addAll(Arrays.asList(s.findSubTags(ScxmlTags.TAG_STATE, ScxmlTags.NS_SCXML)));
								allStates.addAll(Arrays.asList(s.findSubTags(ScxmlTags.TAG_PARALLEL, ScxmlTags.NS_SCXML)));

								if (id != null)
								{
									Rectangle2D.Float r = bounds.get(id);
									if (r != null)
									{
										String bs = XmlWriter.floatToStringRestrictedPrecision(r.x, precisionFactor) + " " +
												XmlWriter.floatToStringRestrictedPrecision(r.y, precisionFactor) + " " +
												XmlWriter.floatToStringRestrictedPrecision(r.width, precisionFactor) + " " +
												XmlWriter.floatToStringRestrictedPrecision(r.height, precisionFactor);
										System.err.println("State " + id + " " + bs);

										XmlAttribute attr = s.getAttribute(GraphExtension.ATTR_BOUNDS, GraphExtension.NS_GRAPH_EXTENSION);
										if (attr == null)
										{
											s.setAttribute(GraphExtension.ATTR_BOUNDS, GraphExtension.NS_GRAPH_EXTENSION, bs);
										}
										else
										{
											if (!bs.equals(attr.getValue()))
											{
												System.err.println(attr.getValue() + " -> " + bs);
												attr.setValue(bs);
											}
										}
									}
									else
									{
										System.err.println("No bounds for " + id);
									}
								}
							}
						}

						PsiDocumentManager.getInstance(xmlFile.getProject()).commitDocument(xmlDocument);

					}
					catch (ProcessCanceledException pce)
					{
						// Shall never be caught
						throw pce;
					}
				}
			}
			finally
			{
				inDocumentSync = false;
				// @TODO some unlock?
			}
		}
	}

	/**
	 * This method is synchronized because it is possible that this method is called from different worker threads in parallel.
	 */
	private synchronized void runGraphUpdate()
	{
		if (updateGraphTriggered)
		{
			updateGraphTriggered = false;
			if (file != null)
			{
				try
				{
					XmlParser parser = new XmlParser();
					parser.addExtensionParser("*", new LogExtensionParser());

					GraphExtension ge = new GraphExtension();
					parser.addExtensionParser(GraphExtension.NS_GRAPH_EXTENSION, ge);

					final FiniteStateMachine fsm = parser.parse(file.toNioPath(), xmlDocument.getText());
					ApplicationManager.getApplication()
									  .invokeLater(() -> component.setStateMachine(fsm, ge));
				}
				catch (ProcessCanceledException pce)
				{
					// Shall never be caught
					throw pce;
				}
				catch (ParserException pe)
				{
					component.setError(pe);
				}
			}
		}
	}

	/**
	 * Creates a new editor.
	 *
	 * @param file    The original file
	 * @param psiFile The matching psi file.
	 */
	public ScxmlGraphEditor(@NotNull VirtualFile file, @Nullable PsiFile psiFile)
	{
		component = new ScxmlGraphPanel(psiFile == null ? null : psiFile.getProject());
		this.file = file;
		setXmlFile(psiFile);

		updateXmlTimer = new Timer(500, e -> {
			if (component.root != null)
			{
				VisualModel model = component.root.getSubModel();
				if (model != null && model.isModified())
				{
					log.warn("Model modified");
					model.setModified(false);
					triggerXmlUpdate();
				}
			}
		});
		updateXmlTimer.start();
	}

	/**
	 * Sets the XML Psi file.
	 *
	 * @param xmlFile The file, can be null.
	 */
	public void setXmlFile(PsiFile xmlFile)
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
			triggerGraphUpdate();
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
		if (xmlDocument != null)
		{
			xmlDocument.removeDocumentListener(documentListener);
			xmlDocument = null;
		}
	}
}
