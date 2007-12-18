/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jst.jsp.core.tests.contentmodels;

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jst.jsp.core.tests.taglibindex.BundleResourceUtil;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.INodeNotifier;
import org.eclipse.wst.xml.core.internal.contentmodel.CMElementDeclaration;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.eclipse.wst.xml.core.internal.ssemodelquery.ModelQueryAdapter;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class TestTaglibCMTests extends TestCase {
	private static final String TESTFILES_PATHSTRING = "/testfiles/";
	private static final String PROJECT_NAME = "testLoadTaglibs";
	private static final String TAG_NAME = "logic:empty";

	public TestTaglibCMTests(String name) {
		super(name);
	}

	public TestTaglibCMTests() {
		super();
	}

	public static Test suite() {
		return new TestTaglibCMTests();
	}

	protected void setUp() throws Exception {
		super.setUp();
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
		if (!project.exists()) {
			// Create new project
			project = BundleResourceUtil.createSimpleProject(PROJECT_NAME, null, null);
			BundleResourceUtil.copyBundleEntriesIntoWorkspace(TESTFILES_PATHSTRING + PROJECT_NAME, Path.ROOT.append(PROJECT_NAME).toString());
			BundleResourceUtil.copyBundleEntryIntoWorkspace("/testfiles/struts.jar", Path.ROOT.append(PROJECT_NAME).append("web stuff/WEB-INF/lib/struts.jar").toString());
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
			project.build(IncrementalProjectBuilder.FULL_BUILD, null);
		}
		assertTrue(project.isAccessible());
	}

	public void testLoadCustomTagsThroughJSPSyntax() throws IOException, CoreException {
		IFile jspFile = ResourcesPlugin.getWorkspace().getRoot().getFile(Path.ROOT.append(PROJECT_NAME).append("web stuff/test1.jsp"));

		IDOMModel model = null;
		try {
			model = (IDOMModel) StructuredModelManager.getModelManager().getModelForEdit(jspFile);
			NodeList presents = model.getDocument().getElementsByTagName(TAG_NAME);
			assertNotNull(TAG_NAME + " was missing from document", presents.item(0));
			ModelQueryAdapter modelQueryAdapter = (ModelQueryAdapter) ((INodeNotifier) presents.item(0)).getAdapterFor(ModelQueryAdapter.class);
			CMElementDeclaration declaration = modelQueryAdapter.getModelQuery().getCMElementDeclaration((Element) presents.item(0));
			assertNotNull("no CMElementDelcaration for " + TAG_NAME, declaration);
			assertEquals("qualified name from element declaration was different", TAG_NAME, declaration.getNodeName());
		}
		finally {
			if (model != null) {
				model.releaseFromEdit();
			}
		}
	}

	public void testLoadCustomTagsThroughXMLSyntax() throws IOException, CoreException {
		IFile jspFile = ResourcesPlugin.getWorkspace().getRoot().getFile(Path.ROOT.append(PROJECT_NAME).append("web stuff/testX.jsp"));

		IDOMModel model = null;
		try {
			model = (IDOMModel) StructuredModelManager.getModelManager().getModelForEdit(jspFile);
			NodeList presents = model.getDocument().getElementsByTagName(TAG_NAME);
			assertNotNull(TAG_NAME + " was missing from document", presents.item(0));
			ModelQueryAdapter modelQueryAdapter = (ModelQueryAdapter) ((INodeNotifier) presents.item(0)).getAdapterFor(ModelQueryAdapter.class);
			CMElementDeclaration declaration = modelQueryAdapter.getModelQuery().getCMElementDeclaration((Element) presents.item(0));
			assertNotNull("no CMElementDeclaration for " + TAG_NAME, declaration);
			assertEquals("qualified name from element declaration was different", TAG_NAME, declaration.getNodeName());
		}
		finally {
			if (model != null) {
				model.releaseFromEdit();
			}
		}
	}
	
	public void testTagFileReferencedInTLD() throws IOException, CoreException {
		String DPROJECT_NAME = "DynamicWebProject";
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(DPROJECT_NAME);
		if (!project.exists()) {
			// Create new project
			project = BundleResourceUtil.createSimpleProject(DPROJECT_NAME, null, null);
			BundleResourceUtil.copyBundleZippedEntriesIntoWorkspace("/testfiles/jspErrorProject.zip", Path.ROOT);
		}
		project.refreshLocal(IResource.DEPTH_INFINITE, null);

		IFile jspFile = ResourcesPlugin.getWorkspace().getRoot().getFile(Path.ROOT.append("DynamicWebProject/WebContent/index.jsp"));

		IDOMModel model = null;
		try {
			model = (IDOMModel) StructuredModelManager.getModelManager().getModelForEdit(jspFile);
			String DTAGNAME = "date:returndate";
			NodeList returnDates = model.getDocument().getElementsByTagName(DTAGNAME);
			assertNotNull("date:returndate was missing from document", returnDates.item(0));
			ModelQueryAdapter modelQueryAdapter = (ModelQueryAdapter) ((INodeNotifier) returnDates.item(0)).getAdapterFor(ModelQueryAdapter.class);
			CMElementDeclaration declaration = modelQueryAdapter.getModelQuery().getCMElementDeclaration((Element) returnDates.item(0));
			assertNotNull("no CMElementDeclaration for date:returndate", declaration);
			assertEquals("qualified name from element declaration was different", DTAGNAME, declaration.getNodeName());
		}
		finally {
			if (model != null) {
				model.releaseFromEdit();
			}
		}
	}
}
