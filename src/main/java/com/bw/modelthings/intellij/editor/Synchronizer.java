package com.bw.modelthings.intellij.editor;

import com.bw.graph.VisualModel;
import com.bw.graph.editor.InteractionAdapter;
import com.bw.graph.primitive.ModelPrimitive;
import com.bw.graph.visual.Visual;
import com.bw.modelthings.fsm.model.FiniteStateMachine;
import com.bw.modelthings.fsm.parser.LogExtensionParser;
import com.bw.modelthings.fsm.parser.ParserException;
import com.bw.modelthings.fsm.parser.ScxmlTags;
import com.bw.modelthings.fsm.parser.XmlParser;
import com.bw.modelthings.fsm.ui.EditorChanges;
import com.bw.modelthings.fsm.ui.PosAndBounds;
import com.bw.modelthings.fsm.ui.ScxmlGraphExtension;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;

import javax.swing.Timer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Synchronize the two sub-editors.
 */
public class Synchronizer implements Disposable
{

	private static final Logger LOG = Logger.getInstance(Synchronizer.class);

	/**
	 * Graphical editor.
	 */
	private ScxmlGraphEditor _scxmlEditor;

	/**
	 * Textual editor.
	 */
	private TextEditor _xmlTextEditor;

	/**
	 * The file the editor is showing.
	 */
	final VirtualFile _file;

	/**
	 * The project the current file comes from.
	 */
	final Project _theProject;

	/**
	 * Sync to document is in progress, if this flag is true.
	 */
	boolean _inDocumentSync = false;

	/**
	 * True if an update of the graph was triggered.
	 */
	boolean _updateGraphTriggered = false;

	/**
	 * True if an update of the XML file was triggered.
	 */
	boolean _updateXmlTriggered = false;

	/**
	 * Enables file/graph synchronization.
	 */
	boolean _enableSync = true;

	/**
	 * Timer to trigger graph to document synchronization.
	 */
	Timer _updateXmlTimer;

	/**
	 * Temporary used buffer. Usage is not thread safe, shall be used only from synchronized methods.
	 */
	private final StringBuilder _tempName = new StringBuilder();

	/**
	 * Creates a new editor.
	 *
	 * @param file        The file to show.
	 * @param theProject  The project.
	 * @param scxmlEditor The graphical editor.
	 * @param textEditor  The generic Xml Text Editor.
	 */
	public Synchronizer(@NotNull VirtualFile file, @NotNull Project theProject,
						ScxmlGraphEditor scxmlEditor, TextEditor textEditor)
	{
		this._file = file;
		this._theProject = theProject;
		this._scxmlEditor = scxmlEditor;
		this._xmlTextEditor = textEditor;

		_updateXmlTimer = new Timer(2000, e ->
		{
			Visual root = _scxmlEditor._component.getRootVisual();
			if (root != null)
			{
				VisualModel model = ModelPrimitive.getChildModel(root);
				if (model != null && model.isModified())
				{
					if (_enableSync && !_updateXmlTriggered)
					{
						LOG.warn("Model modified, sync with file");
						_updateXmlTriggered = true;
						ApplicationManager.getApplication()
										  .invokeLater(() -> WriteCommandAction.runWriteCommandAction(_theProject, this::runXmlUpdate));
					}
					else
					{
						LOG.warn("Model modified, but sync disabled");
					}
				}
			}
		});
		_updateXmlTimer.start();

		_scxmlEditor._component.getGraphPane()
							   .addInteractionListener(new InteractionAdapter()
							   {
								   @Override
								   public void deselected(Visual visual)
								   {
									   _enableSync = true;
								   }

								   @Override
								   public void mouseDragging(Visual visual)
								   {
									   // Disable sync to xml file during dragging.
									   _enableSync = (visual == null);
								   }
							   });

		Document doc = _xmlTextEditor.getEditor().getDocument();
		if (doc != null)
			doc.addDocumentListener(_documentListener);
		triggerGraphUpdate();
	}

	@Override
	public void dispose()
	{
		if (_xmlTextEditor != null && _xmlTextEditor.getEditor() != null)
		{
			Document doc = _xmlTextEditor.getEditor().getDocument();
			if (doc != null)
				doc.removeDocumentListener(_documentListener);
		}
	}

	/**
	 * Listener for document changes.
	 */
	DocumentListener _documentListener = new DocumentListener()
	{
		@Override
		public void documentChanged(@NotNull DocumentEvent event)
		{
			if (_inDocumentSync)
			{
				LOG.warn("(Sync) Ignored Document Event " + event);
			}
			else
			{
				LOG.warn("Document Event " + event);
				triggerGraphUpdate();
			}
		}
	};

	/**
	 * Triggers a background update of the graph.
	 */
	protected void triggerGraphUpdate()
	{
		if (!_updateGraphTriggered)
		{
			_updateGraphTriggered = true;
			// Executes in worker thread with read-lock.
			ApplicationManager.getApplication()
							  .executeOnPooledThread(() -> ApplicationManager.getApplication()
																			 .runReadAction(this::runGraphUpdate));
		}
	}


	/**
	 * This method is synchronized because it is possible that this method is called from different worker threads in parallel.
	 */
	private synchronized void runGraphUpdate()
	{
		if (_updateGraphTriggered)
		{
			_updateGraphTriggered = false;
			if (_file != null)
			{
				try
				{
					XmlParser parser = new XmlParser();
					parser.addExtensionParser("*", new LogExtensionParser());

					ScxmlGraphExtension ge = new ScxmlGraphExtension();
					parser.addExtensionParser(ScxmlGraphExtension.NS_GRAPH_EXTENSION, ge);

					final FiniteStateMachine fsm = parser.parse(_file.toNioPath(), _xmlTextEditor.getEditor().getDocument().getText());
					ApplicationManager.getApplication()
									  .invokeLater(() -> _scxmlEditor._component.setStateMachine(fsm, ge));
				}
				catch (ProcessCanceledException pce)
				{
					// Shall never be caught
					throw pce;
				}
				catch (ParserException pe)
				{
					_scxmlEditor._component.setError(pe);
				}
			}
		}
	}


	/**
	 * This method is synchronized because it is possible that this method is called from different worker threads in parallel.
	 */
	private synchronized void runXmlUpdate()
	{
		// @TODO: move calculation of graphextensions to worker thread, then use the result here in write action.
		if (_updateXmlTriggered)
		{
			try
			{
				_inDocumentSync = true;

				FiniteStateMachine fsm = _scxmlEditor._component.getStateMachine();
				EditorChanges update = _scxmlEditor._component.getEditorUpdate();

				if (fsm != null && update != null)
				{
					// Update psi file
					try
					{
						PsiDocumentManager.getInstance(_theProject).doPostponedOperationsAndUnblockDocument(_xmlTextEditor.getEditor().getDocument());

						final float precisionFactor = _scxmlEditor._component.getGraphConfiguration()._precisionFactor;
						PsiFile psi = PsiManager.getInstance(_theProject).findFile(_file);
						XmlDocument doc = psi == null ? null : ((XmlFile) psi).getDocument();
						if (doc != null)
						{
							XmlTag root = doc.getRootTag();
							if (root != null)
							{
								List<XmlTag> allStates = new ArrayList<>(100);
								allStates.add(root);

								while (!allStates.isEmpty())
								{
									XmlTag s = allStates.remove(allStates.size() - 1);

									if (!update._statesRenamed.isEmpty())
									{
										// Renaming
										renameInNameList(s, ScxmlTags.ATTR_ID, null, update._statesRenamed);
										renameInNameList(s, ScxmlTags.ATTR_INITIAL, null, update._statesRenamed);
										renameInNameList(s.findSubTags(ScxmlTags.TAG_TRANSITION, ScxmlTags.NS_SCXML), ScxmlTags.ATTR_TARGET, null, update._statesRenamed);

										XmlTag initial = s.findFirstSubTag(ScxmlTags.TAG_INITIAL);
										if (initial != null)
											renameInNameList(initial.findSubTags(ScxmlTags.TAG_TRANSITION, ScxmlTags.NS_SCXML), ScxmlTags.ATTR_TARGET, null, update._statesRenamed);
									}

									String id = s.getAttributeValue(ScxmlTags.ATTR_ID);

									List<XmlTag> states = Arrays.asList(s.findSubTags(ScxmlTags.TAG_STATE, ScxmlTags.NS_SCXML));
									List<XmlTag> parallels = Arrays.asList(s.findSubTags(ScxmlTags.TAG_PARALLEL, ScxmlTags.NS_SCXML));

									allStates.addAll(states);
									allStates.addAll(parallels);

									if (id != null)
									{
										PosAndBounds r = update._bounds.get(id);
										if (r != null)
										{
											String bs = r.toXML(precisionFactor);
											XmlAttribute attr = s.getAttribute(ScxmlGraphExtension.ATTR_POS, ScxmlGraphExtension.NS_GRAPH_EXTENSION);
											if (attr == null)
											{
												s.setAttribute(ScxmlGraphExtension.ATTR_POS, ScxmlGraphExtension.NS_GRAPH_EXTENSION, bs);
											}
											else
											{
												if (!bs.equals(attr.getValue()))
												{
													attr.setValue(bs);
												}
											}
										}
									}

									PosAndBounds startNodeBounds = update._startBounds.get(id);

									if (startNodeBounds != null || !(states.isEmpty() && parallels.isEmpty()))
									{

										// A submachine. We need to set the start-state bounds
										if (startNodeBounds == null)
										{
											startNodeBounds = update._startBounds.get(s.getAttributeValue(ScxmlTags.ATTR_NAME, ScxmlTags.NS_SCXML));
										}
										if (startNodeBounds != null)
										{
											String bs = startNodeBounds.toXML(precisionFactor);
											XmlAttribute attrStart = s.getAttribute(ScxmlGraphExtension.ATTR_START_POS, ScxmlGraphExtension.NS_GRAPH_EXTENSION);
											if (attrStart == null)
											{
												s.setAttribute(ScxmlGraphExtension.ATTR_START_POS, ScxmlGraphExtension.NS_GRAPH_EXTENSION, bs);
											}
											else
											{
												if (!bs.equals(attrStart.getValue()))
												{
													attrStart.setValue(bs);
												}
											}
										}
									}
								}
							}
						}
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
				_updateXmlTriggered = false;
				_inDocumentSync = false;
				LOG.warn("XML updated");
				// @TODO some unlock?
			}
		}
	}

	/**
	 * Method is not thread-safe, must be called only from synchronized methods.
	 *
	 * @param tags         The Tags to process.
	 * @param attribute    The attribute to change.
	 * @param namespace    The namespace of the attribute.
	 * @param replacements The replacements.
	 */
	private void renameInNameList(XmlTag[] tags, String attribute, String namespace, Map<String, String> replacements)
	{
		for (XmlTag tag : tags)
		{
			renameInNameList(tag, attribute, namespace, replacements);
		}
	}

	/**
	 * Method is not thread-safe, must be called only from synchronized methods.
	 *
	 * @param tag          The Tag to process.
	 * @param attribute    The attribute to change.
	 * @param namespace    The namespace of the attribute.
	 * @param replacements The replacements.
	 */
	private void renameInNameList(XmlTag tag, String attribute, String namespace, Map<String, String> replacements)
	{
		XmlAttribute attr = tag.getAttribute(attribute, namespace);
		if (attr != null)
		{
			_tempName.setLength(0);
			boolean changed = false;
			for (String name : ScxmlTags.splitNameList(attr.getValue()))
			{
				if (!_tempName.isEmpty())
					_tempName.append(' ');
				String newName = replacements.get(name);
				if (newName == null)
				{
					_tempName.append(name);
				}
				else
				{
					changed = true;
					_tempName.append(newName);
				}
			}
			if (changed)
				attr.setValue(_tempName.toString());
		}
	}
}
