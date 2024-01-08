package com.bw.modeldrive.settings;

import com.intellij.util.messages.Topic;

/**
 * MessageBus Notifier interface for Configuration changes.
 */
public interface ChangeConfigurationNotifier
{
	/**
	 * Project topic for the notifier.
	 */
	@Topic.ProjectLevel
	Topic<ChangeConfigurationNotifier> CHANGE_CONFIG_TOPIC =
			Topic.create("ScXML Config", ChangeConfigurationNotifier.class);

	/**
	 * Called if configuration has changed.
	 *
	 * @param config The changed configuration.
	 */
	void onChange(Configuration config);

}
