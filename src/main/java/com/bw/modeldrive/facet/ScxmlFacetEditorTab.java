package com.bw.modeldrive.facet;

import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;

/**
 * UI for {@link ScxmlFacet}.
 * Manages validation and modification of the state.
 */
public class ScxmlFacetEditorTab extends FacetEditorTab
{

	private static final String FACET_PANEL_PROMPT = "Path To SDK: ";

	private final ScxmlFacetState mySettings;
	private final JTextField myPath;

	/**
	 * Only org.intellij.sdk.facet.DemoFacetState is captured so it can be updated per user changes in the EditorTab.
	 *
	 * @param state     {@link ScxmlFacetState} object persisting {@link ScxmlFacet} state.
	 * @param context   Facet editor context, can be used to get e.g. the current project module.
	 * @param validator Facet validator manager, can be used to get and apply a custom validator for this facet.
	 */
	public ScxmlFacetEditorTab(@NotNull ScxmlFacetState state, @NotNull FacetEditorContext context,
							   @NotNull FacetValidatorsManager validator)
	{
		mySettings = state;
		myPath = new JTextField(state.getScxmlSdkPath());
	}

	/**
	 * Provides the {@link JPanel} displayed in the Project Structure | Facet UI
	 *
	 * @return {@link JPanel} to be displayed in the {@link ScxmlFacetEditorTab}.
	 */
	@NotNull
	@Override
	public JComponent createComponent()
	{
		JPanel top = new JPanel(new BorderLayout());
		top.add(new JLabel(FACET_PANEL_PROMPT), BorderLayout.WEST);
		top.add(myPath);
		JPanel facetPanel = new JPanel(new BorderLayout());
		facetPanel.add(top, BorderLayout.NORTH);
		return facetPanel;
	}

	/**
	 * @return the name of this facet for display in this editor tab.
	 */
	@Override
	@Nls(capitalization = Nls.Capitalization.Title)
	public String getDisplayName()
	{
		return ScxmlFacetType.FACET_NAME;
	}

	/**
	 * Determines if the facet state entered in the UI differs from the currently stored state.
	 * Called when user changes text in {@link #myPath}.
	 *
	 * @return {@code true} if the state returned from the panel's UI differs from the stored facet state.
	 */
	@Override
	public boolean isModified()
	{
		return !StringUtil.equals(mySettings.getScxmlSdkPath(), myPath.getText()
																	  .trim());
	}

	/**
	 * Stores new facet state (text) entered by the user.
	 * Called when {@link #isModified()} returns true.
	 *
	 * @throws ConfigurationException if anything generates an exception.
	 */
	@Override
	public void apply() throws ConfigurationException
	{
		// Not much to go wrong here, but fulfill the contract
		try
		{
			String newTextContent = myPath.getText();
			mySettings.setScxmlSdkPath(newTextContent);
		}
		catch (Exception e)
		{
			throw new ConfigurationException(e.toString());
		}
	}

	/**
	 * Copies current {@link ScxmlFacetState} into the {@link #myPath} UI element.
	 * This method is called each time this editor tab is needed for display.
	 */
	@Override
	public void reset()
	{
		myPath.setText(mySettings.getScxmlSdkPath());
	}

}