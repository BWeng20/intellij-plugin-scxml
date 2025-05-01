package com.bw.modelthings.fsm;

import com.bw.modelthings.fsm.model.FiniteStateMachine;
import com.bw.modelthings.fsm.parser.LogExtensionParser;
import com.bw.modelthings.fsm.parser.XmlParser;
import com.bw.modelthings.fsm.ui.FsmGraphPanel;
import com.bw.modelthings.fsm.ui.ScxmlGraphExtension;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

/**
 * Standalone version (just for testing)
 */
public class Editor extends JFrame
{
	private static JFileChooser scxmlFileChooser;

	private FsmGraphPanel graph_;

	public Editor(String title)
	{
		super(title);
		setLayout(new BorderLayout());

		graph_ = new FsmGraphPanel();
		graph_.setPreferredSize(new Dimension(600, 400));

		add(graph_, BorderLayout.CENTER);

		graph_.setStyle(Collections.emptyMap());


	}


	public static synchronized JFileChooser getSCXMLFileChooser()
	{
		if (scxmlFileChooser == null)
		{
			scxmlFileChooser = new JFileChooser();
			scxmlFileChooser.setFileFilter(new FileNameExtensionFilter("State Chart XML (SCXML) files", "scxml", "xml"));
			scxmlFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		}
		return scxmlFileChooser;
	}


	public static void main(String[] args)
	{
		Editor editor = new Editor("FSM Editor");
		editor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JMenuBar menu = new JMenuBar();
		JMenuItem loadMenuItem = new JMenuItem("Open...", KeyEvent.VK_L);
		loadMenuItem.setToolTipText("<html>Opens a SCXML file.</html>");
		loadMenuItem.addActionListener(e ->
		{
			JFileChooser fs = getSCXMLFileChooser();
			int returnVal = fs.showOpenDialog(editor);
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				editor.openFsm(fs.getSelectedFile()
								 .toPath());
			}
		});
		JMenu file = new JMenu("File", false);
		file.setMnemonic('F');
		file.add(loadMenuItem);

		menu.add(file);

		editor.setJMenuBar(menu);
		editor.pack();
		editor.setLocationByPlatform(true);
		editor.setVisible(true);

		if (args.length > 0)
		{
			editor.openFsm(Paths.get(args[0]));
		}
	}

	public void openFsm(Path file)
	{

		try
		{
			XmlParser parser = new XmlParser();
			parser.addExtensionParser("*", new LogExtensionParser());

			ScxmlGraphExtension ge = new ScxmlGraphExtension();
			parser.addExtensionParser(ScxmlGraphExtension.NS_GRAPH_EXTENSION, ge);

			final FiniteStateMachine fsm = parser.parse(file, new String(Files.readAllBytes(file), StandardCharsets.UTF_8));

			graph_.setStateMachine(fsm, ge);


		}
		catch (Exception e)
		{
			// @TODO
			e.printStackTrace();
		}
	}
}
