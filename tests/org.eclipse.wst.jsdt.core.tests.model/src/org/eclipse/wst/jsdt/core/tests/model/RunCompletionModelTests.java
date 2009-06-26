/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.jsdt.core.tests.model;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.wst.jsdt.core.tests.junit.extension.TestCase;

import junit.framework.Test;
import junit.framework.TestSuite;

public class RunCompletionModelTests extends junit.framework.TestCase {

	protected static final boolean ONLY_JAVADOC = "true".equals(System.getProperty("onlyJavadoc", "false"));

	public final static List COMPLETION_SUITES = new ArrayList();
	static {
		if (!ONLY_JAVADOC) {
			if(false) COMPLETION_SUITES.add(CompletionTests.class);
			COMPLETION_SUITES.add(CompletionTests2.class);
			if(false) COMPLETION_SUITES.add(CompletionContextTests.class);
			if(false) COMPLETION_SUITES.add(CompletionWithMissingTypesTests.class);
			if(false) COMPLETION_SUITES.add(CompletionWithMissingTypesTests2.class);
			if(false) COMPLETION_SUITES.add(SnippetCompletionContextTests.class);
		}
		if(false) COMPLETION_SUITES.add(JavadocTypeCompletionModelTest.class);
		if(false) COMPLETION_SUITES.add(JavadocFieldCompletionModelTest.class);
		if(false) COMPLETION_SUITES.add(JavadocMethodCompletionModelTest.class);
		if(false) COMPLETION_SUITES.add(JavadocPackageCompletionModelTest.class);
		if(false) COMPLETION_SUITES.add(JavadocTextCompletionModelTest.class);
		if(false) COMPLETION_SUITES.add(JavadocBugsCompletionModelTest.class);
		if(false) COMPLETION_SUITES.add(JavadocCompletionContextTests.class);
	}

	public static Class[] getTestClasses() {
//		int size = COMPLETION_SUITES.size();
//		if (!ONLY_JAVADOC) {
//			Class[] testClasses = new Class[size+1];
//			COMPLETION_SUITES.toArray(testClasses);
//			testClasses[size] = CompletionTests2.class;
//			testClasses[size+1] = CompletionWithMissingTypesTests2.class;
//			if(false) testClasses[size+2] = SnippetCompletionTests.class;
//			if(false) testClasses[size+3] = SnippetCompletionTests_1_5.class;
//			return testClasses;
//		}
//		Class[] testClasses = new Class[size];
		return  (Class[])COMPLETION_SUITES.toArray(new Class[COMPLETION_SUITES.size()]);
		//return testClasses;
	}

	public RunCompletionModelTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite ts = new TestSuite(RunCompletionModelTests.class.getName());

		// Store test classes with same "Completion"project
		AbstractJavaModelCompletionTests.COMPLETION_SUITES = new ArrayList(COMPLETION_SUITES);

		// Get all classes
		Class[] allClasses = getTestClasses();

		// Reset forgotten subsets of tests
		TestCase.TESTS_PREFIX = null;
		TestCase.TESTS_NAMES = null;
		TestCase.TESTS_NUMBERS = null;
		TestCase.TESTS_RANGE = null;
		TestCase.RUN_ONLY_ID = null;

		// Add all tests suite of tests
		for (int i = 0, length = allClasses.length; i < length; i++) {
			Class testClass = allClasses[i];

			// call the suite() method and add the resulting suite to the suite
			try {
				Method suiteMethod = testClass.getDeclaredMethod("suite", new Class[0]); //$NON-NLS-1$
				Test suite = (Test) suiteMethod.invoke(null, new Object[0]);
				ts.addTest(suite);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.getTargetException().printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
		return ts;
	}
}
