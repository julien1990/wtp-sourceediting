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
package org.eclipse.wst.css.core.metamodel.util;

import org.eclipse.wst.css.core.document.ICSSNode;
import org.eclipse.wst.css.core.metamodel.CSSMetaModel;
import org.eclipse.wst.sse.core.IStructuredModel;


public class CSSMetaModelFinder {
	/**
	 * Constructor for CSSMetaModelFinder.
	 */
	private CSSMetaModelFinder() {
		super();
	}

	static synchronized public CSSMetaModelFinder getInstance() {
		if (fInstance == null) {
			fInstance = new CSSMetaModelFinder();
		}
		return fInstance;
	}

	public CSSMetaModel findMetaModelFor(ICSSNode node) {
		return CSSProfileFinder.getInstance().findProfileFor(node).getMetaModel();
	}

	public CSSMetaModel findMetaModelFor(IStructuredModel model) {
		return CSSProfileFinder.getInstance().findProfileFor(model).getMetaModel();

	}

	// findMetaModelFor(Node) is not used.
	// Even if it is used, it can be replaced with
	// findMetaModelFor(IStructuredModel) easily

	public CSSMetaModel findMetaModelFor(String baseLocation) {
		return CSSProfileFinder.getInstance().findProfileFor(baseLocation).getMetaModel();
	}

	static private CSSMetaModelFinder fInstance = null;
}