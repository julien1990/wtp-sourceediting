/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.wst.jsdt.internal.ui.refactoring.sef;

import org.eclipse.wst.jsdt.core.Flags;
import org.eclipse.wst.jsdt.core.IField;
import org.eclipse.wst.jsdt.core.IMethod;
import org.eclipse.wst.jsdt.core.JavaModelException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;

import org.eclipse.ui.PlatformUI;

import org.eclipse.wst.jsdt.internal.corext.refactoring.sef.SelfEncapsulateFieldRefactoring;
import org.eclipse.wst.jsdt.internal.corext.util.JdtFlags;

import org.eclipse.wst.jsdt.ui.JavaElementLabels;

import org.eclipse.wst.jsdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.wst.jsdt.internal.ui.JavaPluginImages;
import org.eclipse.wst.jsdt.internal.ui.dialogs.TextFieldNavigationHandler;
import org.eclipse.wst.jsdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.wst.jsdt.internal.ui.refactoring.RefactoringMessages;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;

public class SelfEncapsulateFieldInputPage extends UserInputWizardPage {

	private SelfEncapsulateFieldRefactoring fRefactoring;
	private IDialogSettings fSettings;
	
	private static final String GENERATE_JAVADOC= "GenerateJavadoc";  //$NON-NLS-1$
	

	public SelfEncapsulateFieldInputPage() {
		super("InputPage"); //$NON-NLS-1$
		setDescription(RefactoringMessages.SelfEncapsulateFieldInputPage_description); 
		setImageDescriptor(JavaPluginImages.DESC_WIZBAN_REFACTOR_CU);
	}
	
	public void createControl(Composite parent) {
		fRefactoring= (SelfEncapsulateFieldRefactoring)getRefactoring();
		loadSettings();
		
		Composite result= new Composite(parent, SWT.NONE);
		setControl(result);
		initializeDialogUnits(result);
		
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		layout.verticalSpacing= 8;
		result.setLayout(layout);
		GridData gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint= convertWidthInCharsToPixels(25);
		
		Label label= new Label(result, SWT.LEFT);
		label.setText(RefactoringMessages.SelfEncapsulateFieldInputPage_getter_name); 
		Text getter= new Text(result, SWT.BORDER);
		getter.setText(fRefactoring.getGetterName());
		getter.setLayoutData(gd);
		getter.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				fRefactoring.setGetterName(((Text)e.widget).getText());
				processValidation();
			}
		});
		TextFieldNavigationHandler.install(getter);
		
		if (needsSetter()) {
			label= new Label(result, SWT.LEFT);
			label.setText(RefactoringMessages.SelfEncapsulateFieldInputPage_setter_name); 
			Text setter= new Text(result, SWT.BORDER);
			setter.setText(fRefactoring.getSetterName());
			setter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			setter.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					fRefactoring.setSetterName(((Text)e.widget).getText());
					processValidation();
				}
			});
			TextFieldNavigationHandler.install(setter);
		}			
		
		// createSeparator(result, layouter);
		
		label= new Label(result, SWT.LEFT);
		label.setText(RefactoringMessages.SelfEncapsulateFieldInputPage_insert_after); 
		final Combo combo= new Combo(result, SWT.READ_ONLY);
		fillWithPossibleInsertPositions(combo, fRefactoring.getField());
		combo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				fRefactoring.setInsertionIndex(combo.getSelectionIndex() - 1);
			}
		});
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		createAccessModifier(result);
		
		createFieldAccessBlock(result);
			
		Button checkBox= new Button(result, SWT.CHECK);
		checkBox.setText(RefactoringMessages.SelfEncapsulateFieldInputPage_generateJavadocComment); 
		checkBox.setSelection(fRefactoring.getGenerateJavadoc());
		checkBox.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setGenerateJavadoc(((Button)e.widget).getSelection());
			}
		});
		checkBox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		
		processValidation();
		
		getter.setFocus();
		
		Dialog.applyDialogFont(result);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IJavaHelpContextIds.SEF_WIZARD_PAGE);		
	}
	
	private void loadSettings() {
		fSettings= getDialogSettings().getSection(SelfEncapsulateFieldWizard.DIALOG_SETTING_SECTION);
		if (fSettings == null) {
			fSettings= getDialogSettings().addNewSection(SelfEncapsulateFieldWizard.DIALOG_SETTING_SECTION);
			fSettings.put(GENERATE_JAVADOC, JavaPreferencesSettings.getCodeGenerationSettings(fRefactoring.getField().getJavaProject()).createComments);
		}
		fRefactoring.setGenerateJavadoc(fSettings.getBoolean(GENERATE_JAVADOC));
	}	

	private void createAccessModifier(Composite result) {
		int visibility= fRefactoring.getVisibility();
		if (Flags.isPublic(visibility))
			return;
		GridLayout layout;
		Label label;
		label= new Label(result, SWT.NONE);
		label.setText(RefactoringMessages.SelfEncapsulateFieldInputPage_access_Modifiers); 
		
		Composite group= new Composite(result, SWT.NONE);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		layout= new GridLayout();
		layout.numColumns= 4; layout.marginWidth= 0; layout.marginHeight= 0;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Object[] info= createData(visibility);
		String[] labels= (String[])info[0];
		Integer[] data= (Integer[])info[1];
		for (int i= 0; i < labels.length; i++) {
			Button radio= new Button(group, SWT.RADIO);
			radio.setText(labels[i]);
			radio.setData(data[i]);
			int iData= data[i].intValue();
			if (iData == visibility)
				radio.setSelection(true);
			radio.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					fRefactoring.setVisibility(((Integer)event.widget.getData()).intValue());
				}
			});
		}
	}
	
	private void createFieldAccessBlock(Composite result) {
		Label label= new Label(result, SWT.LEFT);
		label.setText(RefactoringMessages.SelfEncapsulateField_field_access); 
		
		Composite group= new Composite(result, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.marginWidth= 0; layout.marginHeight= 0; layout.numColumns= 2;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Button radio= new Button(group, SWT.RADIO);
		radio.setText(RefactoringMessages.SelfEncapsulateField_use_setter_getter); 
		radio.setSelection(true);
		radio.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fRefactoring.setEncapsulateDeclaringClass(true);
			}
		});
		radio.setLayoutData(new GridData());
		
		radio= new Button(group, SWT.RADIO);
		radio.setText(RefactoringMessages.SelfEncapsulateField_keep_references); 
		radio.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fRefactoring.setEncapsulateDeclaringClass(false);
			}
		});
		radio.setLayoutData(new GridData());
	}

	private Object[] createData(int visibility) {
		String pub= RefactoringMessages.SelfEncapsulateFieldInputPage_public; 
		String pro= RefactoringMessages.SelfEncapsulateFieldInputPage_protected; 
		String def= RefactoringMessages.SelfEncapsulateFieldInputPage_default; 
		String priv= RefactoringMessages.SelfEncapsulateFieldInputPage_private; 
		
		String[] labels= null;
		Integer[] data= null;
		if (Flags.isPrivate(visibility)) {
			labels= new String[] { pub, pro, def, priv };
			data= new Integer[] {new Integer(Flags.AccPublic), new Integer(Flags.AccProtected), new Integer(0), new Integer(Flags.AccPrivate) };
		} else if (Flags.isProtected(visibility)) {
			labels= new String[] { pub, pro };
			data= new Integer[] {new Integer(Flags.AccPublic), new Integer(Flags.AccProtected)};
		} else {
			labels= new String[] { pub, def };
			data= new Integer[] {new Integer(Flags.AccPublic), new Integer(0)};
		}
		return new Object[] {labels, data};
	}
	
	private void fillWithPossibleInsertPositions(Combo combo, IField field) {
		int select= 0;
		combo.add(RefactoringMessages.SelfEncapsulateFieldInputPage_first_method); 
		try {
			IMethod[] methods= field.getDeclaringType().getMethods();
			for (int i= 0; i < methods.length; i++) {
				combo.add(JavaElementLabels.getElementLabel(methods[i], JavaElementLabels.M_PARAMETER_TYPES));
			}
			if (methods.length > 0)
				select= methods.length;
		} catch (JavaModelException e) {
			// Fall through
		}
		combo.select(select);
		fRefactoring.setInsertionIndex(select - 1);
	}
	
	private void setGenerateJavadoc(boolean value) {
		fSettings.put(GENERATE_JAVADOC, value);
		fRefactoring.setGenerateJavadoc(value);
	}
	
	private void processValidation() {
		RefactoringStatus status= fRefactoring.checkMethodNames();
		String message= null;
		boolean valid= true;
		if (status.hasFatalError()) {
			message= status.getMessageMatchingSeverity(RefactoringStatus.FATAL);
			valid= false;
		}
		setErrorMessage(message);
		setPageComplete(valid);
	}
	
	private boolean needsSetter() {
		try {
			return !JdtFlags.isFinal(fRefactoring.getField());
		} catch(JavaModelException e) {
			return true;
		}
	}	
}
