package org.eclipse.jst.jsp.ui.internal.hyperlink;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.URLHyperlink;
import org.eclipse.jst.jsp.core.internal.provisional.JSP11Namespace;
import org.eclipse.jst.jsp.core.taglib.ITLDRecord;
import org.eclipse.jst.jsp.core.taglib.ITaglibRecord;
import org.eclipse.jst.jsp.core.taglib.TaglibIndex;
import org.eclipse.jst.jsp.core.text.IJSPPartitions;
import org.eclipse.jst.jsp.ui.internal.Logger;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.IndexedRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredPartitioning;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.core.utils.StringUtils;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMAttr;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Detects hyperlinks for taglibs.
 */
public class TaglibHyperlinkDetector implements IHyperlinkDetector {
	private final String HTTP_PROTOCOL = "http://";//$NON-NLS-1$

	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
		IHyperlink hyperlink = null;

		if (textViewer != null && region != null) {
			IDocument doc = textViewer.getDocument();
			if (doc != null) {
				try {
					// check if jsp tag/directive first
					ITypedRegion partition = TextUtilities.getPartition(doc, IStructuredPartitioning.DEFAULT_STRUCTURED_PARTITIONING, region.getOffset(), false);
					if (partition != null && partition.getType() == IJSPPartitions.JSP_DIRECTIVE) {
						// check if jsp taglib directive
						Node currentNode = getCurrentNode(doc, region.getOffset());
						if (currentNode != null && currentNode.getNodeType() == Node.ELEMENT_NODE && JSP11Namespace.ElementName.DIRECTIVE_TAGLIB.equalsIgnoreCase(currentNode.getNodeName())) {
							// get the uri attribute
							Attr taglibNode = ((Element) currentNode).getAttributeNode(JSP11Namespace.ATTR_NAME_URI);
							ITaglibRecord reference = TaglibIndex.resolve(getBaseLocationForTaglib(doc), taglibNode.getValue(), false);
							if (reference != null) {
								// handle taglibs
								switch (reference.getRecordType()) {
									case (ITaglibRecord.TLD) : {
										ITLDRecord record = (ITLDRecord) reference;
										String uriString = record.getPath().toString();
										IRegion hyperlinkRegion = getHyperlinkRegion(taglibNode);
										hyperlink = createHyperlink(uriString, hyperlinkRegion, doc, taglibNode);
									}
										break;
									case (ITaglibRecord.JAR) :
									case (ITaglibRecord.URL) : {
										IRegion hyperlinkRegion = getHyperlinkRegion(taglibNode);
										hyperlink = new TaglibJarUriHyperlink(hyperlinkRegion, reference);
									}
								}
							}
						}
					}
				}
				catch (BadLocationException e) {
					Logger.log(Logger.WARNING_DEBUG, e.getMessage(), e);
				}
			}
		}
		if (hyperlink != null)
			return new IHyperlink[]{hyperlink};
		return null;
	}

	/**
	 * Get the base location from the current model (if within workspace,
	 * location is relative to workspace, otherwise, file system path)
	 */
	private String getBaseLocationForTaglib(IDocument document) {
		String baseLoc = null;

		// get the base location from the current model
		IStructuredModel sModel = null;
		try {
			sModel = StructuredModelManager.getModelManager().getExistingModelForRead(document);
			if (sModel != null) {
				baseLoc = sModel.getBaseLocation();
			}
		}
		finally {
			if (sModel != null) {
				sModel.releaseFromRead();
			}
		}
		return baseLoc;
	}

	// the below methods were copied from URIHyperlinkDetector

	private IRegion getHyperlinkRegion(Node node) {
		IRegion hyperRegion = null;

		if (node != null) {
			short nodeType = node.getNodeType();
			if (nodeType == Node.DOCUMENT_TYPE_NODE) {
				// handle doc type node
				IDOMNode docNode = (IDOMNode) node;
				hyperRegion = new Region(docNode.getStartOffset(), docNode.getEndOffset() - docNode.getStartOffset());
			}
			else if (nodeType == Node.ATTRIBUTE_NODE) {
				// handle attribute nodes
				IDOMAttr att = (IDOMAttr) node;
				// do not include quotes in attribute value region
				int regOffset = att.getValueRegionStartOffset();
				ITextRegion valueRegion = att.getValueRegion();
				if (valueRegion != null) {
					int regLength = valueRegion.getTextLength();
					String attValue = att.getValueRegionText();
					if (StringUtils.isQuoted(attValue)) {
						++regOffset;
						regLength = regLength - 2;
					}
					hyperRegion = new Region(regOffset, regLength);
				}
			}
		}
		return hyperRegion;
	}

	/**
	 * Returns an IFile from the given uri if possible, null if cannot find
	 * file from uri.
	 * 
	 * @param fileString
	 *            file system or workspace-relative path
	 * @return returns IFile if fileString exists in the workspace
	 */
	private IFile getFile(String fileString) {
		IFile file = null;

		if (fileString != null) {
			IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(new Path(fileString));
			for (int i = 0; i < files.length && file == null; i++)
				if (files[i].exists())
					file = files[i];
		}
		if (file == null) {
			file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(fileString));
		}

		return file;
	}

	/**
	 * Create the appropriate hyperlink
	 * 
	 * @param uriString
	 * @param hyperlinkRegion
	 * @return IHyperlink
	 */
	private IHyperlink createHyperlink(String uriString, IRegion hyperlinkRegion, IDocument document, Node node) {
		IHyperlink link = null;

		if (uriString != null) {
			String temp = uriString.toLowerCase();
			if (temp.startsWith(HTTP_PROTOCOL)) {
				// this is a URLHyperlink since this is a web address
				link = new URLHyperlink(hyperlinkRegion, uriString);
			}

			// try to locate the file in the workspace
			IFile file = getFile(uriString);
			if (file != null && file.exists()) {
				// this is a WorkspaceFileHyperlink since file exists in
				// workspace
				link = new WorkspaceFileHyperlink(hyperlinkRegion, file);
			}
			else {
				// this is an ExternalFileHyperlink since file does not exist
				// in workspace
				File externalFile = new File(uriString);
				link = new ExternalFileHyperlink(hyperlinkRegion, externalFile);
			}
		}

		return link;
	}

	/**
	 * Returns the node the cursor is currently on in the document. null if no
	 * node is selected
	 * 
	 * @param offset
	 * @return Node either element, doctype, text, or null
	 */
	private Node getCurrentNode(IDocument document, int offset) {
		// get the current node at the offset (returns either: element,
		// doctype, text)
		IndexedRegion inode = null;
		IStructuredModel sModel = null;
		try {
			sModel = StructuredModelManager.getModelManager().getExistingModelForRead(document);
			inode = sModel.getIndexedRegion(offset);
			if (inode == null)
				inode = sModel.getIndexedRegion(offset - 1);
		}
		finally {
			if (sModel != null)
				sModel.releaseFromRead();
		}

		if (inode instanceof Node) {
			return (Node) inode;
		}
		return null;
	}
}
