/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     
 *******************************************************************************/
package org.eclipse.wst.javascript.core.tests;

import junit.framework.Test;
import junit.framework.TestSuite;


public class JSCoreTestSuite extends TestSuite {

	public static Test suite() {
		return new JSCoreTestSuite();
	}

	public JSCoreTestSuite() {
		super();
		addTest(new TestSuite(VerifyCorePluginTests.class));
	}
}
