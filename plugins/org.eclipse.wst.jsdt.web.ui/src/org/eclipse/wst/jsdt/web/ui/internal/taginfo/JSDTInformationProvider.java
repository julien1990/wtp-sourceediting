/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     
 *******************************************************************************/
package org.eclipse.wst.jsdt.web.ui.internal.taginfo;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.information.IInformationProviderExtension;
import org.eclipse.wst.sse.ui.internal.SSEUIPlugin;

/**
 * Provides javadoc context information for java code inside JSPs (Shows tooltip
 * description)
 */
public class JSDTInformationProvider implements IInformationProvider, IInformationProviderExtension {
	private ITextHover fTextHover = null;
	
	public JSDTInformationProvider() {
		fTextHover = SSEUIPlugin.getDefault().getTextHoverManager().createBestMatchHover(new JSDTHoverProcessor());
	}
	
	public String getInformation(ITextViewer textViewer, IRegion subject) {
		return (String) getInformation2(textViewer, subject);
	}
	
	public Object getInformation2(ITextViewer textViewer, IRegion subject) {
		return fTextHover.getHoverInfo(textViewer, subject);
	}
	
	public IRegion getSubject(ITextViewer textViewer, int offset) {
		return fTextHover.getHoverRegion(textViewer, offset);
	}
}
