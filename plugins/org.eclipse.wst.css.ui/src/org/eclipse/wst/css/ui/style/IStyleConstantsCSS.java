/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.css.ui.style;

/**
 * Contains the symbolic name of styles used by LineStyleProvider,
 * ColorManager, and any others who may be interested
 */
public interface IStyleConstantsCSS {
	public static final String NORMAL = "NORMAL"; //$NON-NLS-1$
	public static final String ATMARK_RULE = "ATMARK_RULE"; //$NON-NLS-1$
	public static final String SELECTOR = "SELECTOR"; //$NON-NLS-1$
	public static final String MEDIA = "MEDIA"; //$NON-NLS-1$
	public static final String COMMENT = "COMMENT"; //$NON-NLS-1$
	public static final String PROPERTY_NAME = "PROPERTY_NAME"; //$NON-NLS-1$
	public static final String PROPERTY_VALUE = "PROPERTY_VALUE"; //$NON-NLS-1$
	public static final String URI = "URI"; //$NON-NLS-1$
	public static final String STRING = "STRING"; //$NON-NLS-1$
	public static final String COLON = "COLON"; //$NON-NLS-1$
	public static final String SEMI_COLON = "SEMI_COLON"; //$NON-NLS-1$
	public static final String CURLY_BRACE = "CURLY_BRACE"; //$NON-NLS-1$
	public static final String ERROR = "ERROR"; //$NON-NLS-1$
}