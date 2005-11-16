/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     
 *******************************************************************************/

package org.eclipse.jst.jsp.core.tests.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jst.jsp.core.internal.provisional.contenttype.ContentTypeIdForJSP;
import org.eclipse.jst.jsp.core.tests.JSPCoreTestsPlugin;
import org.eclipse.wst.sse.core.internal.provisional.IModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.StructuredModelManager;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;

public class TestModelWithNoFile extends TestCase {

	public void testJSPModel() {
		IDOMModel structuredModel = (IDOMModel) StructuredModelManager.getModelManager().createUnManagedStructuredModelFor(ContentTypeIdForJSP.ContentTypeID_JSP);
		boolean test = structuredModel.getId().equals(IModelManager.UNMANAGED_MODEL);
		assertTrue(test);
		structuredModel.releaseFromEdit();
		assertTrue(true);
	}

	public void testBug116066_1() {
		IModelManager modelManager = StructuredModelManager.getModelManager();
		IStructuredModel model = null;

		// Create new project
		IProject project = createSimpleProject("bug116066_1", null, null);

		IFile testFile = project.getFile("nonExistant.jsp");
		assertFalse(testFile.exists());

		// Get the model and set a reference to that tag library into it
		try {
			model = modelManager.getNewModelForEdit(testFile, false);
			assertNotNull("couldn't get new model for " + testFile.getFullPath(), model);
			model.getStructuredDocument().set("<%@taglib prefix=\"tagdependent\" uri=\"tagdependent\">\n<tagdependent:code> <<< </tagdependent:code>");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (model != null) {
				model.releaseFromEdit();
			}
		}
	}

	public void testBug116066_2() {
		IModelManager modelManager = StructuredModelManager.getModelManager();
		IStructuredModel model = null;

		// Create new project
		IProject project = createSimpleProject("bug116066_2", null, null);
		// Copy a TLD into the project
		IFile tld = copyBundleEntryIntoWorkspace("/testfiles/116066/tagdep.tld", "/bug116066_2/tagdep.tld");
		assertTrue(tld.exists());

		IFile testFile = project.getFile("nonExistant.jsp");
		assertFalse(testFile.exists());

		// Get the model and set a reference to that tag library into it
		try {
			model = modelManager.getNewModelForEdit(testFile, false);
			assertNotNull("couldn't get new model for " + testFile.getFullPath(), model);
			model.getStructuredDocument().set("<%@taglib prefix=\"tagdependent\" uri=\"tagdependent\">\n<tagdependent:code> <<< </tagdependent:code>");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (model != null) {
				model.releaseFromEdit();
			}
		}
	}

	private IFile copyBundleEntryIntoWorkspace(String entryname, String fullPath) {
		IFile file = null;
		URL tld = JSPCoreTestsPlugin.getDefault().getBundle().getEntry(entryname);
		if (tld != null) {
			try {
				byte[] b = new byte[2048];
				InputStream input = tld.openStream();
				ByteArrayOutputStream output = new ByteArrayOutputStream();
				int i = -1;
				while ((i = input.read(b)) > -1) {
					output.write(b, 0, i);
				}
				file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(fullPath));
				if (file != null) {
					file.create(new ByteArrayInputStream(output.toByteArray()), true, new NullProgressMonitor());
				}
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return file;
	}

	private IProject createSimpleProject(String name, IPath location, String[] natureIds) {
		IProjectDescription description = ResourcesPlugin.getWorkspace().newProjectDescription(name);
		if (location != null) {
			description.setLocation(location);
		}
		if (natureIds != null) {
			description.setNatureIds(natureIds);
		}
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
		try {
			project.create(description, new NullProgressMonitor());
			assertTrue(project.exists());
			project.open(new NullProgressMonitor());
		}
		catch (CoreException e) {
			e.printStackTrace();
		}
		return project;
	}
}