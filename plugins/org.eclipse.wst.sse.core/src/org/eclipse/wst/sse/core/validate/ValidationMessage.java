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
package org.eclipse.wst.sse.core.validate;



/**
 */
public class ValidationMessage {

	private String message;
	private int offset;
	private int length;
	private int severity;
	public static final int ERROR = 1;
	public static final int WARNING = 2;
	public static final int INFORMATION = 3;

	/**
	 */
	public ValidationMessage(String message, int offset, int severity) {
		this(message, offset, 0, severity);
	}

	/**
	 */
	public ValidationMessage(String message, int offset, int length, int severity) {
		super();

		this.message = message;
		this.offset = offset;
		this.length = length;
		this.severity = severity;
	}

	/**
	 */
	public int getLength() {
		return this.length;
	}

	/**
	 */
	public String getMessage() {
		return this.message;
	}

	/**
	 */
	public int getOffset() {
		return this.offset;
	}

	/**
	 */
	public int getSeverity() {
		return this.severity;
	}
}
