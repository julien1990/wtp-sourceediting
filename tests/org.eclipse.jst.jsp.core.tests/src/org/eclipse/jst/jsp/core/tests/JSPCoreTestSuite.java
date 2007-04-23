/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jst.jsp.core.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jst.jsp.core.tests.cleanupformat.FormatTester;
import org.eclipse.jst.jsp.core.tests.contenttypeidentifier.contentspecific.TestContentTypeHandlers;
import org.eclipse.jst.jsp.core.tests.contenttypeidentifier.contentspecific.TestModelHandlers;
import org.eclipse.jst.jsp.core.tests.dom.TestImportedNodes;
import org.eclipse.jst.jsp.core.tests.model.TestModelIncludes;
import org.eclipse.jst.jsp.core.tests.model.TestModelRelease;
import org.eclipse.jst.jsp.core.tests.model.TestModelWithNoFile;
import org.eclipse.jst.jsp.core.tests.source.JSPTokenizerTest;
import org.eclipse.jst.jsp.core.tests.taglibindex.TestIndex;
import org.eclipse.jst.jsp.core.tests.translation.JSPJavaTranslatorCoreTest;
import org.eclipse.jst.jsp.core.tests.validation.JSPActionValidatorTest;
import org.eclipse.jst.jsp.core.tests.validation.JSPJavaValidatorTest;
import org.eclipse.jst.jsp.css.core.tests.source.JSPedCSSSourceParserTest;



public class JSPCoreTestSuite extends TestSuite {
	public static Test suite() {
		return new JSPCoreTestSuite();
	}

	public JSPCoreTestSuite() {
		super("SSE JSP Core Test Suite");

		String noninteractive = System.getProperty("wtp.autotest.noninteractive");
		String wtp_autotest_noninteractive = null;
		if (noninteractive != null)
			wtp_autotest_noninteractive = noninteractive;
		System.setProperty("wtp.autotest.noninteractive", "true");

		addTest(TestCeanupFormat.suite());
		addTest(ModelCloneSuite.suite());
		addTest(new TestSuite(TestModelHandlers.class, "TestModelHandlers"));
		addTest(new TestSuite(TestContentTypeHandlers.class, "TestContentTypeHandlers"));
		addTest(new TestSuite(TestModelManager.class, "TestModelManager"));
		addTest(new TestSuite(FormatTester.class, "FormatTester"));
		addTest(new TestSuite(TestModelRelease.class, "JSP Model Tests"));
		addTest(new TestSuite(TestModelWithNoFile.class, "JSP Model Tests"));
		addTest(new TestSuite(TestIndex.class, "TaglibIndex Tests"));
		addTest(new TestSuite(JSPTokenizerTest.class, "Special Parsing Tests"));
		addTest(new TestSuite(JSPJavaTranslatorCoreTest.class));
		addTest(new TestSuite(TestModelIncludes.class));
		addTest(new TestSuite(JSPCorePreferencesTest.class));
		addTest(new TestSuite(JSPedCSSSourceParserTest.class, "Special Parsing Tests for JSP-CSS content"));
		addTest(new TestSuite(JSPJavaValidatorTest.class, "JSP Java Validator Tests"));
		addTest(new TestSuite(TestImportedNodes.class, "Imported Nodes Tests"));
		addTest(new TestSuite(TestFixedCMDocuments.class, "Fixed CMDocument Creation Tests"));
		addTest(new TestSuite(JSPActionValidatorTest.class, "JSP Action Validator Tests"));

		if (wtp_autotest_noninteractive != null)
			System.setProperty("wtp.autotest.noninteractive", wtp_autotest_noninteractive);
	}
}
