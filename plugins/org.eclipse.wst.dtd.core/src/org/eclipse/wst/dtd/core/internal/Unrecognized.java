/*******************************************************************************
 * Copyright (c) 2001, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jens Lukowski/Innoopract - initial renaming/restructuring
 *     
 *******************************************************************************/
package org.eclipse.wst.dtd.core.internal;

import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.sse.core.text.IStructuredDocumentRegion;


public class Unrecognized extends TopLevelNode {

	public Unrecognized(DTDFile file, IStructuredDocumentRegion flatNode) {
		super(file, flatNode);
	}

	public Image getImage() {
		return DTDCorePlugin.getInstance().getImage(DTDResource.UNRECOGNIZEDICON);
	}

	public String getName() {
		String text = getStructuredDTDDocumentRegion().getText();
		if (text.length() <= 30) {
			return text;
		}
		else {
			return text.substring(0, 29) + "..."; //$NON-NLS-1$
		}
	}

}
