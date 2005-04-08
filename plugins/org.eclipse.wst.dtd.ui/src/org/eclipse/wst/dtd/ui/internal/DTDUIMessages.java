/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.wst.dtd.ui.internal;

import org.eclipse.osgi.util.NLS;

/**
 * Strings used by DTD UI
 * 
 * @since 1.0
 */
public class DTDUIMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.wst.dtd.ui.internal.DTDUIPluginResources";//$NON-NLS-1$

	public static String _UI_ACTION_ADD_ATTRIBUTELIST;
	public static String _UI_ACTION_ADD_DTD_NOTATION;
	public static String _UI_ACTION_ADD_DTD_ENTITY;
	public static String _UI_ACTION_ADD_DTD_ELEMENT;
	public static String _UI_ACTION_ADD_DTD_COMMENT;
	public static String _UI_ACTION_DTD_DELETE;
	public static String _UI_ACTION_ADD_ATTRIBUTE;
	public static String _UI_ACTION_GROUP_ADD_GROUP;
	public static String _UI_ACTION_ADD_ELEMENT;
	public static String _UI_BUTTON_GROUP_ITEMS_LOGICALLY;
	public static String _UI_BUTTON_SORT_ITEMS;
	public static String _UI_ACTION_ADD_PARAM_ENTITY_REF;
	public static String _UI_MOVE_ATTRIBUTE;
	public static String _UI_MOVE_CONTENT;
	public static String _UI_MOVE_NODE;
	public static String _UI_MOVE_NODES;
	public static String _ERROR_FILENAME_MUST_END_DTD;
	public static String DTDColorPage_0;
	public static String DTDColorPage_1;
	public static String DTDColorPage_2;
	public static String DTDColorPage_3;
	public static String DTDColorPage_4;
	public static String DTDColorPage_5;
	public static String DTDColorPage_6;
	public static String DTDColorPage_7;
	public static String DTDColorPage_8;
	public static String DTDPropertySourceAdapter_0;
	public static String DTDPropertySourceAdapter_1;
	public static String DTDPropertySourceAdapter_2;
	public static String _UI_CREATE_NEW_DTD_FILE;
	public static String _UI_WIZARD_NEW_DTD_TITLE;
	public static String _UI_WIZARD_NEW_DTD_EXPL;
	public static String _UI_LABEL_DTD_FILE_DELETE;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, DTDUIMessages.class);
	}
}
