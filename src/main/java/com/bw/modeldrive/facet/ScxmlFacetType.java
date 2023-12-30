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

public class ScxmlFacetType extends FacetType<ScxmlFacet, ScxmlFacetConfiguration>
{
	public static final String FACET_ID = "SCXML_FACET";
	public static final String FACET_NAME = "Scxml Facet";
	public static final FacetTypeId<ScxmlFacet> FACET_TYPE_ID = new FacetTypeId<>(FACET_ID);

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
	public Icon getIcon() {
		return Icons.SCXML;
	}
}