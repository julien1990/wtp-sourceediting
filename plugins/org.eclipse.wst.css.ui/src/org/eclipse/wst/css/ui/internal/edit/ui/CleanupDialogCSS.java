/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.css.ui.internal.edit.ui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.css.core.internal.cleanup.CSSCleanupStrategy;
import org.eclipse.wst.css.core.internal.cleanup.CSSCleanupStrategyImpl;
import org.eclipse.wst.css.ui.internal.CSSUIMessages;
import org.eclipse.wst.css.ui.internal.editor.IHelpContextIds;

public class CleanupDialogCSS extends Dialog {

	private boolean embeddedCSS;
	protected Button fRadioButtonIdentCaseAsis;
	protected Button fRadioButtonIdentCaseLower;
	protected Button fRadioButtonIdentCaseUpper;
	protected Button fRadioButtonPropNameCaseAsis;
	protected Button fRadioButtonPropNameCaseLower;
	protected Button fRadioButtonPropNameCaseUpper;
	protected Button fRadioButtonPropValueCaseAsis;
	protected Button fRadioButtonPropValueCaseLower;
	protected Button fRadioButtonPropValueCaseUpper;
	protected Button fRadioButtonSelectorTagCaseAsis;
	protected Button fRadioButtonSelectorTagCaseLower;
	protected Button fRadioButtonSelectorTagCaseUpper;
	protected Button fCheckBoxQuoteValues;
	protected Button fCheckBoxFormatSource;

	/**
	 * CSSCleanupDialog constructor comment.
	 * 
	 * @param parentShell
	 *            org.eclipse.swt.widgets.Shell
	 */
	public CleanupDialogCSS(Shell parentShell) {
		super(parentShell);
	}

	/**
	 * 
	 * @return org.eclipse.swt.widgets.Control
	 * @param parent
	 *            org.eclipse.swt.widgets.Composite
	 */
	public Control createDialogArea(Composite parent) {
		if (isEmbeddedCSS())
			getShell().setText(CSSUIMessages.CSS_Cleanup_UI_);
		else
			getShell().setText(CSSUIMessages.Cleanup_UI_);

		Composite panel = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.makeColumnsEqualWidth = true;
		panel.setLayout(layout);

		PlatformUI.getWorkbench().getHelpSystem().setHelp(panel, IHelpContextIds.CSS_CLEANUP_HELPID);

		// Convert ident case
		// ACC: Group radio buttons together so associated label is read
		//		Label identCaseLabel = new Label(panel, SWT.NULL);
		//		identCaseLabel.setText(ResourceHandler.getString("Identifier_case__UI_"));
		// //$NON-NLS-1$ = "Identifier case:"
		//		Canvas identCase = new Canvas(panel, SWT.NULL);
		Group identCase = new Group(panel, SWT.NULL);
		identCase.setText(CSSUIMessages.Identifier_case__UI_);
		GridLayout hLayout = new GridLayout();
		hLayout.numColumns = 3;
		identCase.setLayout(hLayout);
		fRadioButtonIdentCaseAsis = new Button(identCase, SWT.RADIO);
		fRadioButtonIdentCaseAsis.setText(CSSUIMessages.As_is_UI_);
		fRadioButtonIdentCaseLower = new Button(identCase, SWT.RADIO);
		fRadioButtonIdentCaseLower.setText(CSSUIMessages.Lower_UI_);
		fRadioButtonIdentCaseUpper = new Button(identCase, SWT.RADIO);
		fRadioButtonIdentCaseUpper.setText(CSSUIMessages.Upper_UI_);

		// Convert property name case
		// ACC: Group radio buttons together so associated label is read
		//		Label propNameCaseLabel = new Label(panel, SWT.NULL);
		//		propNameCaseLabel.setText(ResourceHandler.getString("Property_name_case__UI_"));
		// //$NON-NLS-1$ = "Property name case:"
		//		Canvas propNameCase = new Canvas(panel, SWT.NULL);
		Group propNameCase = new Group(panel, SWT.NULL);
		propNameCase.setText(CSSUIMessages.Property_name_case__UI_);
		hLayout = new GridLayout();
		hLayout.numColumns = 3;
		propNameCase.setLayout(hLayout);
		fRadioButtonPropNameCaseAsis = new Button(propNameCase, SWT.RADIO);
		fRadioButtonPropNameCaseAsis.setText(CSSUIMessages.As_is_UI_);
		fRadioButtonPropNameCaseLower = new Button(propNameCase, SWT.RADIO);
		fRadioButtonPropNameCaseLower.setText(CSSUIMessages.Lower_UI_);
		fRadioButtonPropNameCaseUpper = new Button(propNameCase, SWT.RADIO);
		fRadioButtonPropNameCaseUpper.setText(CSSUIMessages.Upper_UI_);

		// Convert property Value case
		// ACC: Group radio buttons together so associated label is read
		//		Label propValueCaseLabel = new Label(panel, SWT.NULL);
		//		propValueCaseLabel.setText(ResourceHandler.getString("Property_value_case__UI_"));
		// //$NON-NLS-1$ = "Property value case:"
		//		Canvas propValueCase = new Canvas(panel, SWT.NULL);
		Group propValueCase = new Group(panel, SWT.NULL);
		propValueCase.setText(CSSUIMessages.Property_value_case__UI_);
		hLayout = new GridLayout();
		hLayout.numColumns = 3;
		propValueCase.setLayout(hLayout);
		fRadioButtonPropValueCaseAsis = new Button(propValueCase, SWT.RADIO);
		fRadioButtonPropValueCaseAsis.setText(CSSUIMessages.As_is_UI_);
		fRadioButtonPropValueCaseLower = new Button(propValueCase, SWT.RADIO);
		fRadioButtonPropValueCaseLower.setText(CSSUIMessages.Lower_UI_);
		fRadioButtonPropValueCaseUpper = new Button(propValueCase, SWT.RADIO);
		fRadioButtonPropValueCaseUpper.setText(CSSUIMessages.Upper_UI_);

		if (!isEmbeddedCSS()) {
			// Convert selector tag case
			// ACC: Group radio buttons together so associated label is read
			//			Label selectorTagCaseLabel = new Label(panel, SWT.NULL);
			//			selectorTagCaseLabel.setText(ResourceHandler.getString("Selector_tag_name_case__UI_"));
			// //$NON-NLS-1$ = "Selector tag name case:"
			//			Canvas selectorTagCase = new Canvas(panel, SWT.NULL);
			Group selectorTagCase = new Group(panel, SWT.NULL);
			selectorTagCase.setText(CSSUIMessages.Selector_tag_name_case__UI_);
			hLayout = new GridLayout();
			hLayout.numColumns = 3;
			selectorTagCase.setLayout(hLayout);
			fRadioButtonSelectorTagCaseAsis = new Button(selectorTagCase, SWT.RADIO);
			fRadioButtonSelectorTagCaseAsis.setText(CSSUIMessages.As_is_UI_);
			fRadioButtonSelectorTagCaseLower = new Button(selectorTagCase, SWT.RADIO);
			fRadioButtonSelectorTagCaseLower.setText(CSSUIMessages.Lower_UI_);
			fRadioButtonSelectorTagCaseUpper = new Button(selectorTagCase, SWT.RADIO);
			fRadioButtonSelectorTagCaseUpper.setText(CSSUIMessages.Upper_UI_);
		}

		// Quote attribute values
		fCheckBoxQuoteValues = new Button(panel, SWT.CHECK);
		fCheckBoxQuoteValues.setText(CSSUIMessages.Quote_values_UI_);

		if (!isEmbeddedCSS()) {
			// Format source
			fCheckBoxFormatSource = new Button(panel, SWT.CHECK);
			fCheckBoxFormatSource.setText(CSSUIMessages.Format_source_UI_);
		}

		setCleanupOptions();

		return panel;
	}

	/**
	 * Insert the method's description here.
	 * 
	 * @return boolean
	 */
	public boolean isEmbeddedCSS() {
		return embeddedCSS;
	}

	/**
	 *  
	 */
	protected void okPressed() {
		updateCleanupOptions();
		super.okPressed();
	}

	/**
	 *  
	 */
	protected void setCleanupOptions() {
		CSSCleanupStrategy stgy = CSSCleanupStrategyImpl.getInstance();

		if (fRadioButtonIdentCaseAsis != null) {
			if (stgy.getIdentCase() == CSSCleanupStrategy.UPPER)
				fRadioButtonIdentCaseUpper.setSelection(true);
			else if (stgy.getIdentCase() == CSSCleanupStrategy.LOWER)
				fRadioButtonIdentCaseLower.setSelection(true);
			else
				fRadioButtonIdentCaseAsis.setSelection(true);
		}

		if (fRadioButtonPropNameCaseAsis != null) {
			if (stgy.getPropNameCase() == CSSCleanupStrategy.UPPER)
				fRadioButtonPropNameCaseUpper.setSelection(true);
			else if (stgy.getPropNameCase() == CSSCleanupStrategy.LOWER)
				fRadioButtonPropNameCaseLower.setSelection(true);
			else
				fRadioButtonPropNameCaseAsis.setSelection(true);
		}

		if (fRadioButtonPropValueCaseAsis != null) {
			if (stgy.getPropValueCase() == CSSCleanupStrategy.UPPER)
				fRadioButtonPropValueCaseUpper.setSelection(true);
			else if (stgy.getPropValueCase() == CSSCleanupStrategy.LOWER)
				fRadioButtonPropValueCaseLower.setSelection(true);
			else
				fRadioButtonPropValueCaseAsis.setSelection(true);
		}

		if (fRadioButtonSelectorTagCaseAsis != null) {
			if (stgy.getSelectorTagCase() == CSSCleanupStrategy.UPPER)
				fRadioButtonSelectorTagCaseUpper.setSelection(true);
			else if (stgy.getSelectorTagCase() == CSSCleanupStrategy.LOWER)
				fRadioButtonSelectorTagCaseLower.setSelection(true);
			else
				fRadioButtonSelectorTagCaseAsis.setSelection(true);
		}

		if (fCheckBoxQuoteValues != null)
			fCheckBoxQuoteValues.setSelection(stgy.isQuoteValues());

		if (fCheckBoxFormatSource != null)
			fCheckBoxFormatSource.setSelection(stgy.isFormatSource());

	}

	/**
	 * Insert the method's description here.
	 * 
	 * @param newEmbeddedCSS
	 *            boolean
	 */
	public void setEmbeddedCSS(boolean newEmbeddedCSS) {
		embeddedCSS = newEmbeddedCSS;
	}

	/**
	 *  
	 */
	protected void updateCleanupOptions() {
		CSSCleanupStrategy stgy = CSSCleanupStrategyImpl.getInstance();

		if (fRadioButtonIdentCaseAsis != null) {
			if (fRadioButtonIdentCaseUpper.getSelection())
				stgy.setIdentCase(CSSCleanupStrategy.UPPER);
			else if (fRadioButtonIdentCaseLower.getSelection())
				stgy.setIdentCase(CSSCleanupStrategy.LOWER);
			else
				stgy.setIdentCase(CSSCleanupStrategy.ASIS);
		}

		if (fRadioButtonPropNameCaseAsis != null) {
			if (fRadioButtonPropNameCaseUpper.getSelection())
				stgy.setPropNameCase(CSSCleanupStrategy.UPPER);
			else if (fRadioButtonPropNameCaseLower.getSelection())
				stgy.setPropNameCase(CSSCleanupStrategy.LOWER);
			else
				stgy.setPropNameCase(CSSCleanupStrategy.ASIS);
		}

		if (fRadioButtonPropValueCaseAsis != null) {
			if (fRadioButtonPropValueCaseUpper.getSelection())
				stgy.setPropValueCase(CSSCleanupStrategy.UPPER);
			else if (fRadioButtonPropValueCaseLower.getSelection())
				stgy.setPropValueCase(CSSCleanupStrategy.LOWER);
			else
				stgy.setPropValueCase(CSSCleanupStrategy.ASIS);
		}

		if (fRadioButtonSelectorTagCaseAsis != null) {
			if (fRadioButtonSelectorTagCaseUpper.getSelection())
				stgy.setSelectorTagCase(CSSCleanupStrategy.UPPER);
			else if (fRadioButtonSelectorTagCaseLower.getSelection())
				stgy.setSelectorTagCase(CSSCleanupStrategy.LOWER);
			else
				stgy.setSelectorTagCase(CSSCleanupStrategy.ASIS);
		}

		if (fCheckBoxQuoteValues != null)
			stgy.setQuoteValues(fCheckBoxQuoteValues.getSelection());

		if (fCheckBoxFormatSource != null)
			stgy.setFormatSource(fCheckBoxFormatSource.getSelection());

		// save these values to preferences
		((CSSCleanupStrategyImpl) stgy).saveOptions();
	}
}
