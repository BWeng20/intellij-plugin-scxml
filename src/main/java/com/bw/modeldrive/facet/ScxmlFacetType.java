package com.bw.modeldrive.facet;

import com.bw.modeldrive.Icons;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeId;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.util.NlsSafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

/**
 * Type description of the SCXML facet.
 */
public class ScxmlFacetType extends FacetType<ScxmlFacet, ScxmlFacetConfiguration>
{
	/**
	 * Id of this facet
	 */
	public static final String FACET_ID = "SCXML_FACET";

	/**
	 * Name of this facet
	 */
	public static final String FACET_NAME = "Scxml Facet";

	/**
	 * Id of this facet
	 */
	public static final FacetTypeId<ScxmlFacet> FACET_TYPE_ID = new FacetTypeId<>(FACET_ID);

	/**
	 * Creates the new Type instance.
	 */
	public ScxmlFacetType()
	{
		super(FACET_TYPE_ID, FACET_ID, FACET_NAME);
	}

	@Override
	public ScxmlFacetConfiguration createDefaultConfiguration()
	{
		return new ScxmlFacetConfiguration();
	}

	@Override
	public ScxmlFacet createFacet(@NotNull Module module, @NlsSafe String name, @NotNull ScxmlFacetConfiguration configuration, @Nullable Facet underlyingFacet)
	{
		return new ScxmlFacet(this, module, name, configuration, underlyingFacet);
	}

	@Override
	public boolean isSuitableModuleType(ModuleType moduleType)
	{
		return true;
	}

	@Nullable
	@Override
	public Icon getIcon()
	{
		return Icons.SCXML;
	}
}
