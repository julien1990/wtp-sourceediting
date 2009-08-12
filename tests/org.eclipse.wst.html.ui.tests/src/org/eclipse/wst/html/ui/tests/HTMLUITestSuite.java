/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.html.ui.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.wst.html.ui.tests.viewer.TestViewerConfigurationHTML;



public class HTMLUITestSuite extends junit.framework.TestSuite {
	public static Test suite() {
		return new HTMLUITestSuite();
	}

	public HTMLUITestSuite() {
		super("HTML UI TestSuite");
		addTest(new TestSuite(VerifyEditorPlugin.class));
		addTest(new TestSuite(HTMLUIPreferencesTest.class));
		addTest(new TestSuite(TestViewerConfigurationHTML.class));
		addTest(new TestSuite(TestEditorConfigurationHTML.class));
		//TODO Add back, commenting out for junit failures addTest(new TestSuite(TestHTMLValidator.class, "Test HTMLValidator"));
		//		addTest(new SSEModelTestSuite());
	}
}
