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
package org.eclipse.wst.html.core.modelhandler;

import org.eclipse.wst.html.core.encoding.HTMLDocumentCharsetDetector;
import org.eclipse.wst.html.core.encoding.HTMLDocumentLoader;
import org.eclipse.wst.html.core.encoding.HTMLModelLoader;
import org.eclipse.wst.sse.core.ModelLoader;
import org.eclipse.wst.sse.core.document.IDocumentCharsetDetector;
import org.eclipse.wst.sse.core.document.IDocumentLoader;
import org.eclipse.wst.sse.core.modelhandler.AbstractModelHandler;
import org.eclipse.wst.sse.core.modelhandler.IModelHandler;

public class ModelHandlerForHTML extends AbstractModelHandler implements IModelHandler {
	/** 
	 * Needs to match what's in plugin registry. 
	 * In fact, can be overwritten at run time with 
	 * what's in registry! (so should never be 'final')
	 */
	static String AssociatedContentTypeID = "org.eclipse.wst.html.core.htmlsource"; //$NON-NLS-1$
	/**
	 * Needs to match what's in plugin registry. 
	 * In fact, can be overwritten at run time with 
	 * what's in registry! (so should never be 'final')
	 */
	private static String ModelHandlerID_HTML = "org.eclipse.wst.html.core.modelhandler"; //$NON-NLS-1$


	public ModelHandlerForHTML() {
		super();
		setId(ModelHandlerID_HTML);
		setAssociatedContentTypeId(AssociatedContentTypeID);
	}

	public ModelLoader getModelLoader() {
		return new HTMLModelLoader();
	}

	public IDocumentCharsetDetector getEncodingDetector() {
		return new HTMLDocumentCharsetDetector();
	}

	public IDocumentLoader getDocumentLoader() {
		return new HTMLDocumentLoader();
	}

}