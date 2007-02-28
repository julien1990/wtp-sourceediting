/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Alex Blewitt - https://bugs.eclipse.org/bugs/show_bug.cgi?id=168954
 *******************************************************************************/
package org.eclipse.wst.jsdt.internal.ui.preferences.cleanup;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import org.eclipse.wst.jsdt.internal.corext.fix.CleanUpConstants;

import org.eclipse.wst.jsdt.internal.ui.fix.CodeFormatCleanUp;
import org.eclipse.wst.jsdt.internal.ui.fix.CommentFormatCleanUp;
import org.eclipse.wst.jsdt.internal.ui.fix.ICleanUp;
import org.eclipse.wst.jsdt.internal.ui.fix.ImportsCleanUp;
import org.eclipse.wst.jsdt.internal.ui.fix.SortMembersCleanUp;
import org.eclipse.wst.jsdt.internal.ui.preferences.formatter.JavaPreview;
import org.eclipse.wst.jsdt.internal.ui.preferences.formatter.ModifyDialog;
import org.eclipse.wst.jsdt.internal.ui.preferences.formatter.ModifyDialogTabPage;
import org.eclipse.wst.jsdt.internal.ui.util.PixelConverter;

public final class CodeFormatingTabPage extends ModifyDialogTabPage {
	
	private final Map fValues;
	private final boolean fIsSaveParticipantConfiguration;
	private CleanUpPreview fCleanUpPreview;
	
	public CodeFormatingTabPage(ModifyDialog dialog, Map values) {
		this(dialog, values, false);
	}
	
	public CodeFormatingTabPage(IModificationListener listener, Map values, boolean isSaveParticipantConfiguration) {
		super(listener, values);
		fValues= values;
		fIsSaveParticipantConfiguration= isSaveParticipantConfiguration;
	}

	protected JavaPreview doCreateJavaPreview(Composite parent) {
		fCleanUpPreview= new CleanUpPreview(parent, new ICleanUp[] {new ImportsCleanUp(fValues), new CodeFormatCleanUp(fValues), new CommentFormatCleanUp(fValues), new SortMembersCleanUp(fValues)}, false);
		return fCleanUpPreview;
	}
	
	protected void doCreatePreferences(Composite composite, int numColumns) {
		
		Group group= createGroup(numColumns, composite, CleanUpMessages.CodeFormatingTabPage_GroupName_Formatter);
		
		if (!fIsSaveParticipantConfiguration) {
			createCheckboxPref(group, numColumns, CleanUpMessages.CodeFormatingTabPage_CheckboxName_FormatSourceCode, CleanUpConstants.FORMAT_SOURCE_CODE, CleanUpModifyDialog.FALSE_TRUE);
		}
		
		final CheckboxPreference whiteSpace= createCheckboxPref(group, numColumns, CleanUpMessages.CodeFormatingTabPage_RemoveTrailingWhitespace_checkbox_text, CleanUpConstants.FORMAT_REMOVE_TRAILING_WHITESPACES, CleanUpModifyDialog.FALSE_TRUE);
		
		Composite sub= new Composite(group, SWT.NONE);
		GridData gridData= new GridData(SWT.FILL, SWT.TOP, true, false);
		gridData.horizontalSpan= numColumns;
		sub.setLayoutData(gridData);
		GridLayout layout= new GridLayout(3, false);
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		sub.setLayout(layout);
		
		indent(sub);
		final RadioPreference allPref= createRadioPref(sub, 1, CleanUpMessages.CodeFormatingTabPage_RemoveTrailingWhitespace_all_radio, CleanUpConstants.FORMAT_REMOVE_TRAILING_WHITESPACES_ALL, CleanUpModifyDialog.FALSE_TRUE);
		final RadioPreference ignoreEmptyPref= createRadioPref(sub, 1, CleanUpMessages.CodeFormatingTabPage_RemoveTrailingWhitespace_ignoreEmpty_radio, CleanUpConstants.FORMAT_REMOVE_TRAILING_WHITESPACES_IGNORE_EMPTY, CleanUpModifyDialog.FALSE_TRUE);
		
		whiteSpace.addObserver(new Observer() {
			public void update(Observable o, Object arg) {
				allPref.setEnabled(whiteSpace.getChecked());
				ignoreEmptyPref.setEnabled(whiteSpace.getChecked());
			}
			
		});
		
		allPref.setEnabled(whiteSpace.getChecked());
		ignoreEmptyPref.setEnabled(whiteSpace.getChecked());
		
    	PixelConverter pixelConverter= new PixelConverter(composite);
	    	
		if (!fIsSaveParticipantConfiguration) {
			createLabel(CleanUpMessages.CodeFormatingTabPage_FormatterSettings_Description, group, numColumns, pixelConverter).setFont(composite.getFont());
		
			Group importsGroup= createGroup(numColumns, composite, CleanUpMessages.CodeFormatingTabPage_Imports_GroupName);
			createCheckboxPref(importsGroup, numColumns, CleanUpMessages.CodeFormatingTabPage_OrganizeImports_CheckBoxLable, CleanUpConstants.ORGANIZE_IMPORTS, CleanUpModifyDialog.FALSE_TRUE);
			
			createLabel(CleanUpMessages.CodeFormatingTabPage_OrganizeImportsSettings_Description, importsGroup, numColumns, pixelConverter).setFont(composite.getFont());
		}
		
		Group sortMembersGroup = createGroup(numColumns, composite, CleanUpMessages.CodeFormatingTabPage_SortMembers_GroupName);
		final CheckboxPreference sortMembersPref = createCheckboxPref(sortMembersGroup, numColumns, CleanUpMessages.CodeFormatingTabPage_SortMembers_CheckBoxLabel, CleanUpConstants.SORT_MEMBERS, CleanUpModifyDialog.FALSE_TRUE);
		
		indent(sortMembersGroup);
		final CheckboxPreference sortMembersFieldsPref = createCheckboxPref(sortMembersGroup, numColumns - 1, CleanUpMessages.CodeFormatingTabPage_SortMembersFields_CheckBoxLabel, CleanUpConstants.SORT_MEMBERS_ALL, CleanUpModifyDialog.FALSE_TRUE);
		
		sortMembersPref.addObserver( new Observer() {
			public void update(Observable o, Object arg) {
				sortMembersFieldsPref.setEnabled(sortMembersPref.getChecked());
			}	    		
		});
		sortMembersFieldsPref.setEnabled(sortMembersPref.getChecked());
		
		createLabel(CleanUpMessages.CodeFormatingTabPage_SortMembers_Description, sortMembersGroup, numColumns, pixelConverter).setFont(composite.getFont());
    }
	
	private Label createLabel(String text, Group group, int numColumns, PixelConverter pixelConverter) {
		Label label= new Label(group, SWT.WRAP);
		label.setText(text);
		GridData gridData= new GridData(GridData.FILL, GridData.CENTER, true, false, numColumns, 0);
		gridData.widthHint= pixelConverter.convertHorizontalDLUsToPixels(150);
		label.setLayoutData(gridData);
		return label;
	}
	
	private void indent(Composite composite) {
		Label l= new Label(composite, SWT.NONE);
		GridData gd= new GridData();
		gd.widthHint= fPixelConverter.convertWidthInCharsToPixels(4);
		l.setLayoutData(gd);
	}
	
	protected void doUpdatePreview() {
		fCleanUpPreview.setWorkingValues(fValues);
		fCleanUpPreview.update();
	}
	
	protected void initializePage() {
		fCleanUpPreview.update();
	}
}