/*
* Copyright (c) 2002 IBM Corporation and others.
* All rights reserved.   This program and the accompanying materials
* are made available under the terms of the Common Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/cpl-v10.html
* 
* Contributors:
*   IBM - Initial API and implementation
*   Jens Lukowski/Innoopract - initial renaming/restructuring
* 
*/
package org.eclipse.wst.sse.ui.preferences.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.util.Assert;
import org.eclipse.ui.editors.text.EditorsUI;

/**
 * A settable IStatus. 
 * Can be an error, warning, info or ok. For error, info and warning states,
 * a message describes the problem.
 * 
 * This class was copied from other StatusInfo classes that are located in internal packages
 */
class StatusInfo implements IStatus {

	/** The message of this status.  */	
	private String fStatusMessage;
	/** The severity of this status. */
	private int fSeverity;
	
	/**
	 * Creates a status set to OK (no message).
	 */
	public StatusInfo() {
		this(OK, null);
	}

	/**
	 * Creates a status with the given severity and message.
	 * 
	 * @param severity the severity of this status: ERROR, WARNING, INFO and OK.
	 * @param message the message of this status. Applies only for ERROR,
	 * WARNING and INFO.
	 */	
	public StatusInfo(int severity, String message) {
		fStatusMessage= message;
		fSeverity= severity;
	}		
	
	/*
	 * @see org.eclipse.core.runtime.IStatus#isOK()
	 */
	public boolean isOK() {
		return fSeverity == IStatus.OK;
	}

	/**
	 * Returns whether this status indicates a warning.
	 * 
	 * @return <code>true</code> if this status has severity
	 *    {@link IStatus#WARNING} and <code>false</code> otherwise
	 */
	public boolean isWarning() {
		return fSeverity == IStatus.WARNING;
	}

	/**
	 * Returns whether this status indicates an info.
	 * 
	 * @return <code>true</code> if this status has severity
	 *    {@link IStatus#INFO} and <code>false</code> otherwise
	 */
	public boolean isInfo() {
		return fSeverity == IStatus.INFO;
	}	

	/**
	 * Returns whether this status indicates an error.
	 * 
	 * @return <code>true</code> if this status has severity
	 *    {@link IStatus#ERROR} and <code>false</code> otherwise
	 */
	public boolean isError() {
		return fSeverity == IStatus.ERROR;
	}
	
	/*
	 * @see IStatus#getMessage()
	 */
	public String getMessage() {
		return fStatusMessage;
	}
	
	/**
	 * Sets the status to ERROR.
	 * 
	 * @param errorMessage the error message which can be an empty string, but not <code>null</code>
	 */	
	public void setError(String errorMessage) {
		Assert.isNotNull(errorMessage);
		fStatusMessage= errorMessage;
		fSeverity= IStatus.ERROR;
	}

	/**
	 * Sets the status to WARNING.
	 * 
	 * @param warningMessage the warning message which can be an empty string, but not <code>null</code>
	 */		
	public void setWarning(String warningMessage) {
		Assert.isNotNull(warningMessage);
		fStatusMessage= warningMessage;
		fSeverity= IStatus.WARNING;
	}

	/**
	 * Sets the status to INFO.
	 * 
	 * @param infoMessage the info message which can be an empty string, but not <code>null</code>
	 */		
	public void setInfo(String infoMessage) {
		Assert.isNotNull(infoMessage);
		fStatusMessage= infoMessage;
		fSeverity= IStatus.INFO;
	}	

	/**
	 * Sets the status to OK.
	 */		
	public void setOK() {
		fStatusMessage= null;
		fSeverity= IStatus.OK;
	}
	
	/*
	 * @see IStatus#matches(int)
	 */
	public boolean matches(int severityMask) {
		return (fSeverity & severityMask) != 0;
	}

	/**
	 * Returns always <code>false</code>.
	 * 
	 * @see IStatus#isMultiStatus()
	 */
	public boolean isMultiStatus() {
		return false;
	}

	/*
	 * @see IStatus#getSeverity()
	 */
	public int getSeverity() {
		return fSeverity;
	}

	/*
	 * @see IStatus#getPlugin()
	 */
	public String getPlugin() {
		return EditorsUI.PLUGIN_ID;
	}

	/**
	 * Returns always <code>null</code>.
	 * 
	 * @see IStatus#getException()
	 */
	public Throwable getException() {
		return null;
	}

	/**
	 * Returns always the error severity.
	 * 
	 * @see IStatus#getCode()
	 */
	public int getCode() {
		return fSeverity;
	}

	/**
	 * Returns always <code>null</code>.
	 * 
	 * @see IStatus#getChildren()
	 */
	public IStatus[] getChildren() {
		return new IStatus[0];
	}	

}
