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
package org.eclipse.wst.css.core.internal.format;

import java.util.List;

import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.wst.css.core.internal.formatter.CSSFormatUtil;
import org.eclipse.wst.css.core.internal.formatter.CSSSourceFormatter;
import org.eclipse.wst.css.core.internal.formatter.CSSSourceFormatterFactory;
import org.eclipse.wst.css.core.internal.provisional.document.ICSSDocument;
import org.eclipse.wst.css.core.internal.provisional.document.ICSSModel;
import org.eclipse.wst.css.core.internal.provisional.document.ICSSNode;
import org.eclipse.wst.sse.core.internal.format.AbstractStructuredFormatProcessor;
import org.eclipse.wst.sse.core.internal.format.IStructuredFormatPreferences;
import org.eclipse.wst.sse.core.internal.format.IStructuredFormatter;
import org.eclipse.wst.sse.core.internal.provisional.INodeNotifier;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.IndexedRegion;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.w3c.dom.Node;


public class FormatProcessorCSS extends AbstractStructuredFormatProcessor {


	protected String getFileExtension() {
		return "css"; //$NON-NLS-1$
	}

	public void formatModel(IStructuredModel structuredModel) {
		int start = 0;
		int length = structuredModel.getStructuredDocument().getLength();

		formatModel(structuredModel, start, length);
	}

	public void formatModel(IStructuredModel structuredModel, int start, int length) {
		CSSFormatUtil formatUtil = CSSFormatUtil.getInstance();
		if (structuredModel instanceof ICSSModel) {
			//BUG102822 take advantage of IDocumentExtension4
			IDocumentExtension4 docExt4 = null;
			if (structuredModel.getStructuredDocument() instanceof IDocumentExtension4) {
				docExt4 = (IDocumentExtension4)structuredModel.getStructuredDocument();
			}
			DocumentRewriteSession rewriteSession = (docExt4 == null) ? null :
				docExt4.startRewriteSession(DocumentRewriteSessionType.UNRESTRICTED);
			
			ICSSDocument doc = ((ICSSModel) structuredModel).getDocument();
			CSSSourceFormatter formatter = CSSSourceFormatterFactory.getInstance().getSourceFormatter((INodeNotifier) doc);
			StringBuffer buf = formatter.format(doc);
			if (buf != null) {
				int startOffset = ((IndexedRegion) doc).getStartOffset();
				int endOffset = ((IndexedRegion) doc).getEndOffset();
				formatUtil.replaceSource(doc.getModel(), startOffset, endOffset - startOffset, buf.toString());
			}
			
			//BUG102822 take advantage of IDocumentExtension4
			if (docExt4 != null && rewriteSession != null)
				docExt4.stopRewriteSession(rewriteSession);
		}
		else if (structuredModel instanceof IDOMModel) {
			List cssnodes = formatUtil.collectCSSNodes(structuredModel, start, length);
			if (cssnodes != null && !cssnodes.isEmpty()) {
				ICSSModel model = null;
				
				//BUG102822 take advantage of IDocumentExtension4
				IDocumentExtension4 docExt4 = null;
				if (structuredModel.getStructuredDocument() instanceof IDocumentExtension4) {
					docExt4 = (IDocumentExtension4)structuredModel.getStructuredDocument();
				}
				DocumentRewriteSession rewriteSession = (docExt4 == null) ? null :
					docExt4.startRewriteSession(DocumentRewriteSessionType.UNRESTRICTED);
				
				for (int i = 0; i < cssnodes.size(); i++) {
					ICSSNode node = (ICSSNode) cssnodes.get(i);
					CSSSourceFormatter formatter = CSSSourceFormatterFactory.getInstance().getSourceFormatter((INodeNotifier) node);
					StringBuffer buf = formatter.format(node);
					if (buf != null) {
						int startOffset = ((IndexedRegion) node).getStartOffset();
						int endOffset = ((IndexedRegion) node).getEndOffset();
						if (model == null) {
							model = node.getOwnerDocument().getModel();
						}
						formatUtil.replaceSource(model, startOffset, endOffset - startOffset, buf.toString());
					}
				}
				
				//BUG102822 take advantage of IDocumentExtension4
				if (docExt4 != null && rewriteSession != null)
					docExt4.stopRewriteSession(rewriteSession);
			}
		}
	}

	public IStructuredFormatPreferences getFormatPreferences() {
		return null;
	}

	protected IStructuredFormatter getFormatter(Node node) {
		return null;
	}

	protected void refreshFormatPreferences() {
	}
}