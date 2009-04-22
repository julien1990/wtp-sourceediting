/*******************************************************************************
 * Copyright (c) 2001, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jens Lukowski/Innoopract - initial renaming/restructuring
 *     
 *******************************************************************************/
package org.eclipse.wst.xml.ui.internal.preferences;

import java.util.Vector;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferenceLinkArea;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.eclipse.wst.sse.ui.internal.preferences.ui.AbstractPreferencePage;
import org.eclipse.wst.xml.core.internal.XMLCorePlugin;
import org.eclipse.wst.xml.core.internal.preferences.XMLCorePreferenceNames;
import org.eclipse.wst.xml.ui.internal.XMLUIMessages;
import org.eclipse.wst.xml.ui.internal.XMLUIPlugin;
import org.eclipse.wst.xml.ui.internal.editor.IHelpContextIds;

public class XMLSourcePreferencePage extends AbstractPreferencePage implements ModifyListener, SelectionListener, IWorkbenchPreferencePage {
	private final int MIN_INDENTATION_SIZE = 0;
	private final int MAX_INDENTATION_SIZE = 16;

	// Content Assist
	protected Button fAutoPropose;
	protected Label fAutoProposeLabel;
	protected Text fAutoProposeText;
	private Combo fSuggestionStrategyCombo;
	private Vector fSuggestionStrategies = null;
	protected Button fClearAllBlankLines;

	// Formatting
	protected Label fLineWidthLabel;
	protected Text fLineWidthText;
	protected Button fSplitMultiAttrs;
	private Button fIndentUsingTabs;
	private Button fIndentUsingSpaces;
	private Spinner fIndentationSize;
	private Button fPreservePCDATAContent;
	private Button fAlignEndBracket;
	// BUG195264 - Support for removing/adding a space before empty close tags
	private Button fSpaceBeforeEmptyCloseTag;

	// grammar constraints
	protected Button fUseInferredGrammar;

	protected Control createContents(Composite parent) {
		final Composite composite = super.createComposite(parent, 1);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IHelpContextIds.XML_PREFWEBX_SOURCE_HELPID);

		new PreferenceLinkArea(composite, SWT.WRAP | SWT.MULTI, "org.eclipse.wst.sse.ui.preferences.editor", XMLUIMessages._UI_STRUCTURED_TEXT_EDITOR_PREFS_LINK,//$NON-NLS-1$
					(IWorkbenchPreferenceContainer) getContainer(), null).getControl().setLayoutData(GridDataFactory.fillDefaults().hint(150, SWT.DEFAULT).create());
		new Label(composite, SWT.NONE).setLayoutData(GridDataFactory.swtDefaults().create());

		createContentsForFormattingGroup(composite);
		createContentsForContentAssistGroup(composite);
		createContentsForGrammarConstraintsGroup(composite);
		setSize(composite);
		loadPreferences();

		return composite;
	}

	protected void createContentsForContentAssistGroup(Composite parent) {
		Group contentAssistGroup = createGroup(parent, 2);
		contentAssistGroup.setText(XMLUIMessages.Content_assist_UI_);

		fAutoPropose = createCheckBox(contentAssistGroup, XMLUIMessages.Automatically_make_suggest_UI_);
		((GridData) fAutoPropose.getLayoutData()).horizontalSpan = 2;
		fAutoPropose.addSelectionListener(this);

		fAutoProposeLabel = createLabel(contentAssistGroup, XMLUIMessages.Prompt_when_these_characte_UI_);
		fAutoProposeText = createTextField(contentAssistGroup);

		createLabel(contentAssistGroup, XMLUIMessages.Suggestion_Strategy);
		fSuggestionStrategyCombo = new Combo(contentAssistGroup, SWT.READ_ONLY);
		fSuggestionStrategies = new Vector();
		fSuggestionStrategyCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fSuggestionStrategyCombo.add(XMLUIMessages.Suggestion_Strategy_Lax);
		fSuggestionStrategies.add(XMLUIPreferenceNames.SUGGESTION_STRATEGY_VALUE_LAX);
		fSuggestionStrategyCombo.add(XMLUIMessages.Suggestion_Strategy_Strict);
		fSuggestionStrategies.add(XMLUIPreferenceNames.SUGGESTION_STRATEGY_VALUE_STRICT);
	}

	protected void createContentsForFormattingGroup(Composite parent) {
		Group formattingGroup = createGroup(parent, 2);
		formattingGroup.setText(XMLUIMessages.Formatting_UI_);

		fLineWidthLabel = createLabel(formattingGroup, XMLUIMessages.Line_width__UI_);
		fLineWidthText = new Text(formattingGroup, SWT.SINGLE | SWT.BORDER);
		GridData gData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.BEGINNING);
		gData.widthHint = 25;
		fLineWidthText.setLayoutData(gData);
		fLineWidthText.addModifyListener(this);

		fSplitMultiAttrs = createCheckBox(formattingGroup, XMLUIMessages.Split_multiple_attributes);
		((GridData) fSplitMultiAttrs.getLayoutData()).horizontalSpan = 2;
		fAlignEndBracket = createCheckBox(formattingGroup, XMLUIMessages.Align_final_bracket);
		((GridData) fAlignEndBracket.getLayoutData()).horizontalSpan = 2;
		fPreservePCDATAContent = createCheckBox(formattingGroup, XMLUIMessages.Preserve_PCDATA_Content);
		((GridData) fPreservePCDATAContent.getLayoutData()).horizontalSpan = 2;
		fClearAllBlankLines = createCheckBox(formattingGroup, XMLUIMessages.Clear_all_blank_lines_UI_);
		((GridData) fClearAllBlankLines.getLayoutData()).horizontalSpan = 2;
		fSpaceBeforeEmptyCloseTag = createCheckBox(formattingGroup, XMLUIMessages.Space_before_empty_close_tag);
		((GridData) fSpaceBeforeEmptyCloseTag.getLayoutData()).horizontalSpan = 2;

		// [269224] - Place the indent controls in their own composite for proper tab ordering
		Composite indentComposite = createComposite(formattingGroup, 1);
		((GridData) indentComposite.getLayoutData()).horizontalSpan = 2;
		((GridLayout) indentComposite.getLayout()).marginWidth = 0;
		((GridLayout) indentComposite.getLayout()).marginHeight = 0;

		fIndentUsingTabs = createRadioButton(indentComposite, XMLUIMessages.Indent_using_tabs);
		((GridData) fIndentUsingTabs.getLayoutData()).horizontalSpan = 1;

		fIndentUsingSpaces = createRadioButton(indentComposite, XMLUIMessages.Indent_using_spaces);
		((GridData) fIndentUsingSpaces.getLayoutData()).horizontalSpan = 1;

		createLabel(formattingGroup, XMLUIMessages.Indentation_size);
		fIndentationSize = new Spinner(formattingGroup, SWT.READ_ONLY | SWT.BORDER);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		fIndentationSize.setLayoutData(gd);
		fIndentationSize.setToolTipText(XMLUIMessages.Indentation_size_tip);
		fIndentationSize.setMinimum(MIN_INDENTATION_SIZE);
		fIndentationSize.setMaximum(MAX_INDENTATION_SIZE);
		fIndentationSize.setIncrement(1);
		fIndentationSize.setPageIncrement(4);
		fIndentationSize.addModifyListener(this);
	}

	protected void createContentsForGrammarConstraintsGroup(Composite parent) {
		Group grammarConstraintsGroup = createGroup(parent, 1);
		grammarConstraintsGroup.setText(XMLUIMessages.Grammar_Constraints);
		grammarConstraintsGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

		fUseInferredGrammar = createCheckBox(grammarConstraintsGroup, XMLUIMessages.Use_inferred_grammar_in_absence_of);
	}

	protected IPreferenceStore doGetPreferenceStore() {
		return XMLUIPlugin.getDefault().getPreferenceStore();
	}

	protected void doSavePreferenceStore() {
		XMLUIPlugin.getDefault().savePluginPreferences(); // editor
		XMLCorePlugin.getDefault().savePluginPreferences(); // model
	}

	protected void enableValues() {
		if (fAutoPropose != null) {
			if (fAutoPropose.getSelection()) {
				fAutoProposeLabel.setEnabled(true);
				fAutoProposeText.setEnabled(true);
			}
			else {
				fAutoProposeLabel.setEnabled(false);
				fAutoProposeText.setEnabled(false);
			}
		}
	}

	protected Preferences getModelPreferences() {
		return XMLCorePlugin.getDefault().getPluginPreferences();
	}

	/**
	 * Return the currently selected suggestion strategy preference
	 * 
	 * @return a suggestion strategy constant from XMLUIPreferenceNames
	 */
	private String getCurrentSuggestionStrategy() {
		int i = fSuggestionStrategyCombo.getSelectionIndex();
		if (i >= 0) {
			return (String) (fSuggestionStrategies.elementAt(i));
		}
		return ""; //$NON-NLS-1$
	}

	protected void initializeValues() {
		initializeValuesForFormattingGroup();
		initializeValuesForContentAssistGroup();
		initializeValuesForGrammarConstraintsGroup();
	}

	protected void initializeValuesForContentAssistGroup() {
		// Content Assist
		fAutoPropose.setSelection(getPreferenceStore().getBoolean(XMLUIPreferenceNames.AUTO_PROPOSE));
		fAutoProposeText.setText(getPreferenceStore().getString(XMLUIPreferenceNames.AUTO_PROPOSE_CODE));
		String suggestionStrategy = getPreferenceStore().getString(XMLUIPreferenceNames.SUGGESTION_STRATEGY);
		if (suggestionStrategy.length() > 0) {
			setCurrentSuggestionStrategy(suggestionStrategy);
		}
		else {
			setCurrentSuggestionStrategy(XMLUIPreferenceNames.SUGGESTION_STRATEGY_VALUE_LAX);
		}
	}

	protected void initializeValuesForFormattingGroup() {
		// Formatting
		fLineWidthText.setText(getModelPreferences().getString(XMLCorePreferenceNames.LINE_WIDTH));
		fSplitMultiAttrs.setSelection(getModelPreferences().getBoolean(XMLCorePreferenceNames.SPLIT_MULTI_ATTRS));
		fAlignEndBracket.setSelection(getModelPreferences().getBoolean(XMLCorePreferenceNames.ALIGN_END_BRACKET));
		fClearAllBlankLines.setSelection(getModelPreferences().getBoolean(XMLCorePreferenceNames.CLEAR_ALL_BLANK_LINES));
		fPreservePCDATAContent.setSelection(getModelPreferences().getBoolean(XMLCorePreferenceNames.PRESERVE_CDATACONTENT));
		fSpaceBeforeEmptyCloseTag.setSelection(getModelPreferences().getBoolean(XMLCorePreferenceNames.SPACE_BEFORE_EMPTY_CLOSE_TAG));
		
		if (XMLCorePreferenceNames.TAB.equals(getModelPreferences().getString(XMLCorePreferenceNames.INDENTATION_CHAR))) {
			fIndentUsingTabs.setSelection(true);
			fIndentUsingSpaces.setSelection(false);
		}
		else {
			fIndentUsingSpaces.setSelection(true);
			fIndentUsingTabs.setSelection(false);
		}

		fIndentationSize.setSelection(getModelPreferences().getInt(XMLCorePreferenceNames.INDENTATION_SIZE));
	}

	protected void initializeValuesForGrammarConstraintsGroup() {
		fUseInferredGrammar.setSelection(getPreferenceStore().getBoolean(XMLUIPreferenceNames.USE_INFERRED_GRAMMAR));
	}

	protected void performDefaults() {
		performDefaultsForFormattingGroup();
		performDefaultsForContentAssistGroup();
		performDefaultsForGrammarConstraintsGroup();

		validateValues();
		enableValues();

		super.performDefaults();
	}

	protected void performDefaultsForContentAssistGroup() {
		// Content Assist
		fAutoPropose.setSelection(getPreferenceStore().getDefaultBoolean(XMLUIPreferenceNames.AUTO_PROPOSE));
		fAutoProposeText.setText(getPreferenceStore().getDefaultString(XMLUIPreferenceNames.AUTO_PROPOSE_CODE));
		String suggestionStrategy = getPreferenceStore().getDefaultString(XMLUIPreferenceNames.SUGGESTION_STRATEGY);
		if (suggestionStrategy.length() > 0) {
			setCurrentSuggestionStrategy(suggestionStrategy);
		}
		else {
			setCurrentSuggestionStrategy(XMLUIPreferenceNames.SUGGESTION_STRATEGY_VALUE_LAX);
		}
	}

	protected void performDefaultsForFormattingGroup() {
		// Formatting
		fLineWidthText.setText(getModelPreferences().getDefaultString(XMLCorePreferenceNames.LINE_WIDTH));
		fSplitMultiAttrs.setSelection(getModelPreferences().getDefaultBoolean(XMLCorePreferenceNames.SPLIT_MULTI_ATTRS));
		fAlignEndBracket.setSelection(getModelPreferences().getDefaultBoolean(XMLCorePreferenceNames.ALIGN_END_BRACKET));
		fClearAllBlankLines.setSelection(getModelPreferences().getDefaultBoolean(XMLCorePreferenceNames.CLEAR_ALL_BLANK_LINES));
		fPreservePCDATAContent.setSelection(getModelPreferences().getDefaultBoolean(XMLCorePreferenceNames.PRESERVE_CDATACONTENT));
		// BUG195264 - Support for removing/adding a space before empty close tags
		fSpaceBeforeEmptyCloseTag.setSelection(getModelPreferences().getDefaultBoolean(XMLCorePreferenceNames.SPACE_BEFORE_EMPTY_CLOSE_TAG));
		
		if (XMLCorePreferenceNames.TAB.equals(getModelPreferences().getDefaultString(XMLCorePreferenceNames.INDENTATION_CHAR))) {
			fIndentUsingTabs.setSelection(true);
			fIndentUsingSpaces.setSelection(false);
		}
		else {
			fIndentUsingSpaces.setSelection(true);
			fIndentUsingTabs.setSelection(false);
		}
		fIndentationSize.setSelection(getModelPreferences().getDefaultInt(XMLCorePreferenceNames.INDENTATION_SIZE));
	}

	protected void performDefaultsForGrammarConstraintsGroup() {
		fUseInferredGrammar.setSelection(getPreferenceStore().getDefaultBoolean(XMLUIPreferenceNames.USE_INFERRED_GRAMMAR));
	}

	public boolean performOk() {
		boolean result = super.performOk();

		doSavePreferenceStore();

		return result;
	}

	/**
	 * Set a suggestion strategy in suggestion strategy combo box
	 * 
	 * @param strategy
	 */
	private void setCurrentSuggestionStrategy(String strategy) {
		// Clear the current selection.
		fSuggestionStrategyCombo.clearSelection();
		fSuggestionStrategyCombo.deselectAll();

		int i = fSuggestionStrategies.indexOf(strategy);
		if (i >= 0) {
			fSuggestionStrategyCombo.select(i);
		}
	}

	protected void storeValues() {
		storeValuesForFormattingGroup();
		storeValuesForContentAssistGroup();
		storeValuesForGrammarConstraintsGroup();
	}

	protected void storeValuesForContentAssistGroup() {
		// Content Assist
		getPreferenceStore().setValue(XMLUIPreferenceNames.AUTO_PROPOSE, fAutoPropose.getSelection());
		getPreferenceStore().setValue(XMLUIPreferenceNames.AUTO_PROPOSE_CODE, fAutoProposeText.getText());
		getPreferenceStore().setValue(XMLUIPreferenceNames.SUGGESTION_STRATEGY, getCurrentSuggestionStrategy());
	}

	protected void storeValuesForFormattingGroup() {
		// Formatting
		getModelPreferences().setValue(XMLCorePreferenceNames.LINE_WIDTH, fLineWidthText.getText());
		getModelPreferences().setValue(XMLCorePreferenceNames.SPLIT_MULTI_ATTRS, fSplitMultiAttrs.getSelection());
		getModelPreferences().setValue(XMLCorePreferenceNames.ALIGN_END_BRACKET, fAlignEndBracket.getSelection());
		getModelPreferences().setValue(XMLCorePreferenceNames.CLEAR_ALL_BLANK_LINES, fClearAllBlankLines.getSelection());
		getModelPreferences().setValue(XMLCorePreferenceNames.PRESERVE_CDATACONTENT, fPreservePCDATAContent.getSelection());
		// BUG195264 - Support for removing/adding a space before empty close tags
		getModelPreferences().setValue(XMLCorePreferenceNames.SPACE_BEFORE_EMPTY_CLOSE_TAG, fSpaceBeforeEmptyCloseTag.getSelection());
		
		if (fIndentUsingTabs.getSelection()) {
			getModelPreferences().setValue(XMLCorePreferenceNames.INDENTATION_CHAR, XMLCorePreferenceNames.TAB);
		}
		else {
			getModelPreferences().setValue(XMLCorePreferenceNames.INDENTATION_CHAR, XMLCorePreferenceNames.SPACE);
		}
		getModelPreferences().setValue(XMLCorePreferenceNames.INDENTATION_SIZE, fIndentationSize.getSelection());
	}

	protected void storeValuesForGrammarConstraintsGroup() {
		getPreferenceStore().setValue(XMLUIPreferenceNames.USE_INFERRED_GRAMMAR, fUseInferredGrammar.getSelection());
	}

	protected void validateValues() {
		boolean isError = false;
		String widthText = null;

		if (fLineWidthText != null) {
			try {
				widthText = fLineWidthText.getText();
				int formattingLineWidth = Integer.parseInt(widthText);
				if ((formattingLineWidth < WIDTH_VALIDATION_LOWER_LIMIT) || (formattingLineWidth > WIDTH_VALIDATION_UPPER_LIMIT)) {
					throw new NumberFormatException();
				}
			}
			catch (NumberFormatException nfexc) {
				setInvalidInputMessage(widthText);
				setValid(false);
				isError = true;
			}
		}

		int indentSize = 0;
		if (fIndentationSize != null) {
			try {
				indentSize = fIndentationSize.getSelection();
				if ((indentSize < MIN_INDENTATION_SIZE) || (indentSize > MAX_INDENTATION_SIZE)) {
					throw new NumberFormatException();
				}
			}
			catch (NumberFormatException nfexc) {
				setInvalidInputMessage(Integer.toString(indentSize));
				setValid(false);
				isError = true;
			}
		}

		if (!isError) {
			setErrorMessage(null);
			setValid(true);
		}
	}
}
