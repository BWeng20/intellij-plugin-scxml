package com.bw.modeldrive;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;


/**
 * The resource bundle wrapper used in this plugin.
 */
public class ModelDriveBundle extends DynamicBundle
{
	private static final ModelDriveBundle INSTANCE = new ModelDriveBundle();
	private static final String PATH_TO_BUNDLE = "messages.ModelDriveBundle";

	/**
	 * Creates the bundle singleton.
	 */
	private ModelDriveBundle()
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
