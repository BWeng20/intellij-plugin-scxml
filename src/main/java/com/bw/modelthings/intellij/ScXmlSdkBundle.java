package com.bw.modelthings.intellij;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;


/**
 * The resource bundle wrapper used in this plugin.
 */
public class ScXmlSdkBundle extends DynamicBundle
{
	private static final ScXmlSdkBundle INSTANCE = new ScXmlSdkBundle();
	private static final String PATH_TO_BUNDLE = "messages.ScxmlSdkBundle";

	/**
	 * Creates the bundle singleton.
	 */
	private ScXmlSdkBundle()
	{
		super(PATH_TO_BUNDLE);
	}

	/**
	 * gets a localized message.
	 *
	 * @param key    The text id
	 * @param params Additional parameters.
	 * @return The composed message.
	 */
	public static String message(@NotNull @PropertyKey(resourceBundle = PATH_TO_BUNDLE) String key, @NotNull Object... params)
	{
		return INSTANCE.getMessage(key, params);
	}
}
