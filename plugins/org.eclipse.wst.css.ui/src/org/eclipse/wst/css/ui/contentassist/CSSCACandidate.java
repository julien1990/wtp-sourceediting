/*****************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and
 * is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ****************************************************************************/
package org.eclipse.wst.css.ui.contentassist;



import org.eclipse.wst.css.ui.image.CSSImageType;

class CSSCACandidate {

	private String fReplacementString = null;
	private int fCursorPosition = 0;
	private String fDisplayString = null;
	private CSSImageType fImageType = null;

	/**
	 * CSSCACandidate constructor comment.
	 */
	CSSCACandidate() {
		super();
	}

	/**
	 *  
	 */
	int getCursorPosition() {
		return fCursorPosition;
	}

	/**
	 *  
	 */
	String getDisplayString() {
		return fDisplayString;
	}

	/**
	 *  
	 */
	CSSImageType getImageType() {
		return fImageType;
	}

	/**
	 *  
	 */
	String getReplacementString() {
		return fReplacementString;
	}

	/**
	 *  
	 */
	void setCursorPosition(int cursorPosition) {
		fCursorPosition = cursorPosition;
	}

	/**
	 *  
	 */
	void setDisplayString(String displayString) {
		fDisplayString = displayString;
	}

	/**
	 *  
	 */
	void setImageType(CSSImageType imageType) {
		fImageType = imageType;
	}

	/**
	 *  
	 */
	void setReplacementString(String replacementString) {
		fReplacementString = replacementString;
	}

	/**
	 * Returns a String that represents the value of this object.
	 * 
	 * @return a string representation of the receiver
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append(getReplacementString());
		buf.append("\"");//$NON-NLS-1$
		buf.append(getDisplayString());
		buf.append("\"");//$NON-NLS-1$
		buf.append("(");//$NON-NLS-1$
		buf.append(getCursorPosition());
		buf.append(")");//$NON-NLS-1$

		return buf.toString();
	}
}