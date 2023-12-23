package com.bw.modeldrive;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;


public class ModelDriveBundle extends DynamicBundle
{
	private static final ModelDriveBundle INSTANCE = new ModelDriveBundle();
	private static final String PATH_TO_BUNDLE = "messages.ModelDriveBundle";

	private ModelDriveBundle()
	{
		super(PATH_TO_BUNDLE);
	}

	public static String message(@NotNull @PropertyKey(resourceBundle = PATH_TO_BUNDLE) String key, @NotNull Object... params)
	{
		return INSTANCE.getMessage(key, params);
	}
}
