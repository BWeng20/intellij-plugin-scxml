package com.bw.modelthings.intellij.settings;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JPanel;

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
	private JBCheckBox buffered;
	private JBCheckBox zoomByMouseWheel;

	@Override
	public @Nullable JComponent createComponent()
	{
		if (editorComponent == null)
		{
			antialiasing = new JBCheckBox("Antialiasing");
			buffered = new JBCheckBox("Draw States Buffered");
			zoomByMouseWheel = new JBCheckBox("Zoom by Meta/Ctrl-Key + MouseWheel");


			editorComponent = FormBuilder.createFormBuilder()
										 .addComponent(antialiasing, 1)
										 .addComponent(buffered, 1)
										 .addComponent(zoomByMouseWheel, 1)
										 .addComponentFillVertically(new JPanel(), 0)
										 .getPanel();
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
			return configuration.buffered != buffered.isSelected() ||
					configuration.antialiasing != antialiasing.isSelected() ||
					configuration.zoomByMetaMouseWheelEnabled != zoomByMouseWheel.isSelected();
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
			configuration.buffered = buffered.isSelected();
			configuration.antialiasing = antialiasing.isSelected();
			configuration.zoomByMetaMouseWheelEnabled = zoomByMouseWheel.isSelected();

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
			buffered.setSelected(configuration.buffered);
			antialiasing.setSelected(configuration.antialiasing);
			zoomByMouseWheel.setSelected(configuration.zoomByMetaMouseWheelEnabled);
		}
	}

	@Override
	public void cancel()
	{
	}
}
