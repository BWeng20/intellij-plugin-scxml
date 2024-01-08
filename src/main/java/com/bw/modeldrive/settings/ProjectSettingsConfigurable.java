package com.bw.modeldrive.settings;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.components.JBCheckBox;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.GridLayout;

/**
 * Configurable implementation for EP com.intellij.projectConfigurable
 */
public class ProjectSettingsConfigurable implements com.intellij.openapi.options.Configurable
{
	private com.intellij.openapi.project.Project theProject;

	/**
	 * Creates a new instance. Called by the framework.
	 *
	 * @param project The project.
	 */
	public ProjectSettingsConfigurable(com.intellij.openapi.project.Project project)
	{
		theProject = project;
	}


	@Override
	public String getDisplayName()
	{
		return "SCXML SDK";
	}

	private JPanel editorComponent;
	private JBCheckBox antialiasing;
	private JBCheckBox doubleBuffered;

	@Override
	public @Nullable JComponent createComponent()
	{
		if (editorComponent == null)
		{
			editorComponent = new JPanel(new GridLayout(2, 1));
			editorComponent.add(antialiasing = new JBCheckBox("Antialiasing"));
			editorComponent.add(doubleBuffered = new JBCheckBox("Double Buffered"));
		}
		return editorComponent;
	}

	@Override
	public boolean isModified()
	{
		PersistenceService service = theProject.getService(PersistenceService.class);
		if (service != null)
		{
			Configuration configuration = service.getState();
			return configuration.doublebuffered != doubleBuffered.isSelected() ||
					configuration.antialiasing != antialiasing.isSelected();
		}
		return false;
	}

	@Override
	public void apply() throws ConfigurationException
	{
		PersistenceService service = theProject.getService(PersistenceService.class);
		if (service == null)
			throw new ConfigurationException("Persistence Service not available");

		if (isModified())
		{
			Configuration configuration = service.getState();
			configuration.doublebuffered = doubleBuffered.isSelected();
			configuration.antialiasing = antialiasing.isSelected();

			ChangeConfigurationNotifier publisher = theProject.getMessageBus()
															  .syncPublisher(ChangeConfigurationNotifier.CHANGE_CONFIG_TOPIC);
			publisher.onChange(configuration);
		}
	}

	@Override
	public void reset()
	{
		PersistenceService service = theProject.getService(PersistenceService.class);
		if (service != null)
		{
			Configuration configuration = service.getState();
			doubleBuffered.setSelected(configuration.doublebuffered);
			antialiasing.setSelected(configuration.antialiasing);
		}
	}

	@Override
	public void cancel()
	{
	}
}
