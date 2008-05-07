/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.xsl.ui.tests;


import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.wst.xsl.ui.internal.validation.TestDelegatingSourceValidatorForXSL;
import org.eclipse.wst.xsl.ui.tests.editor.XSLCodeCompletionTest;


public class XSLUITestSuite extends TestSuite {
	public static Test suite() {
		return new XSLUITestSuite();
	}

	public XSLUITestSuite() {
		super("XML UI Test Suite");
	//	addTestSuite(TestDelegatingSourceValidatorForXSL.class);
		addTestSuite(XSLCodeCompletionTest.class);
	}
}
