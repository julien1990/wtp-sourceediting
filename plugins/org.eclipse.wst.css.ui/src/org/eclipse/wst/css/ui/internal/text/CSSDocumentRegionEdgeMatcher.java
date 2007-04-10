/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.css.ui.internal.text;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.wst.css.core.internal.parserz.CSSRegionContexts;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;

public class CSSDocumentRegionEdgeMatcher implements ICharacterPairMatcher {

	private int fAnchor = ICharacterPairMatcher.LEFT;

	/**
	 * @param validContexts
	 * @param nextMatcher
	 */
	public CSSDocumentRegionEdgeMatcher() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.source.ICharacterPairMatcher#clear()
	 */
	public void clear() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.source.ICharacterPairMatcher#dispose()
	 */
	public void dispose() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.source.ICharacterPairMatcher#getAnchor()
	 */
	public int getAnchor() {
		return fAnchor;
	}

	public IRegion match(IDocument document, int offset) {
		if (document instanceof IStructuredDocument) {
			IStructuredDocumentRegion r = ((IStructuredDocument) document).getRegionAtCharacterOffset(offset);
			if (r != null) {
				if (r.getPrevious() != null && r.getStartOffset() == offset && r.getPrevious().getType().equals(CSSRegionContexts.CSS_RBRACE)) {
					r = r.getPrevious();
				}
				if (r.getType().equals(CSSRegionContexts.CSS_RBRACE)) {
					while (r != null && !r.getType().equals(CSSRegionContexts.CSS_LBRACE)) {
						r = r.getPrevious();
					}
					if (r != null) {
						return new Region(r.getStartOffset(), 1);
					}
				} else if (r.getType().equals(CSSRegionContexts.CSS_LBRACE)) {
					while (r != null && !r.getType().equals(CSSRegionContexts.CSS_RBRACE)) {
						r = r.getNext();
					}
					if (r != null) {
						return new Region(r.getEndOffset() - 1, 1);
					}
				}
			}
		}
		return null;
	}
}
