package org.eclipse.jst.jsp.core.internal.taglib;


import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import javax.servlet.jsp.tagext.VariableInfo;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jst.jsp.core.internal.Logger;
import org.eclipse.jst.jsp.core.internal.contentmodel.TaglibController;
import org.eclipse.jst.jsp.core.internal.contentmodel.tld.TLDCMDocumentManager;
import org.eclipse.jst.jsp.core.internal.contentmodel.tld.TaglibTracker;
import org.eclipse.jst.jsp.core.internal.contentmodel.tld.provisional.TLDAttributeDeclaration;
import org.eclipse.jst.jsp.core.internal.contentmodel.tld.provisional.TLDElementDeclaration;
import org.eclipse.jst.jsp.core.internal.contentmodel.tld.provisional.TLDVariable;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionList;
import org.eclipse.wst.sse.core.utils.StringUtils;
import org.eclipse.wst.xml.core.internal.contentmodel.CMDocument;
import org.eclipse.wst.xml.core.internal.contentmodel.CMNamedNodeMap;
import org.eclipse.wst.xml.core.internal.contentmodel.CMNode;
import org.eclipse.wst.xml.core.internal.contentmodel.modelquery.ModelQuery;
import org.eclipse.wst.xml.core.internal.modelquery.ModelQueryUtil;
import org.eclipse.wst.xml.core.internal.provisional.contentmodel.CMNodeWrapper;
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;

/**
 * This class helps find TaglibVariables in a JSP file.
 * 
 * @author pavery
 */
public class TaglibHelper {

	// for debugging
	private static final boolean DEBUG;
	static {
		String value = Platform.getDebugOption("org.eclipse.jst.jsp.core/debug/taglibvars"); //$NON-NLS-1$
		DEBUG = value != null && value.equalsIgnoreCase("true"); //$NON-NLS-1$
	}

	private IProject fProject = null;
	private TaglibClassLoader fLoader = null;

	private Set fProjectEntries = null;
	private Set fContainerEntries = null;

	public TaglibHelper(IProject project) {
		setProject(project);
		fProjectEntries = new HashSet();
		fContainerEntries = new HashSet();
	}

	/**
	 * @param tagToAdd
	 *            is the name of the tag whose variables we want
	 * @param structuredDoc
	 *            is the IStructuredDocument where the tag is found
	 * @param customTag
	 *            is the IStructuredDocumentRegion opening tag for the custom
	 *            tag
	 */
	public TaglibVariable[] getTaglibVariables(String tagToAdd, IStructuredDocument structuredDoc, IStructuredDocumentRegion customTag) {

		List results = new ArrayList();
		ModelQuery mq = getModelQuery(structuredDoc);
		if (mq != null) {
			TLDCMDocumentManager mgr = TaglibController.getTLDCMDocumentManager(structuredDoc);

			// TaglibSupport support = ((TaglibModelQuery)
			// mq).getTaglibSupport();
			if (mgr == null)
				return new TaglibVariable[0];

			List trackers = mgr.getCMDocumentTrackers(customTag.getEndOffset());
			Iterator taglibs = trackers.iterator();

			// TaglibSupport support = ((TaglibModelQuery)
			// mq).getTaglibSupport();
			// if (support == null)
			// return new TaglibVariable[0];
			//
			// Iterator taglibs =
			// support.getCMDocuments(customTag.getStartOffset()).iterator();
			CMDocument doc = null;
			CMNamedNodeMap elements = null;
			while (taglibs.hasNext()) {
				doc = (CMDocument) taglibs.next();
				CMNode node = null;
				if ((elements = doc.getElements()) != null && (node = elements.getNamedItem(tagToAdd)) != null && node.getNodeType() == CMNode.ELEMENT_DECLARATION) {

					if (node instanceof CMNodeWrapper) {
						node = ((CMNodeWrapper) node).getOriginNode();
					}

					// 1.2+ taglib style
					addVariables(results, node, customTag);

					// for 1.1 need more info from taglib tracker
					if (doc instanceof TaglibTracker) {
						String uri = ((TaglibTracker) doc).getURI();
						String prefix = ((TaglibTracker) doc).getPrefix();
						// only for 1.1 taglibs
						addTEIVariables(customTag, results, (TLDElementDeclaration) node, prefix, uri);
					}
				}
			}
		}

		return (TaglibVariable[]) results.toArray(new TaglibVariable[results.size()]);
	}

	/**
	 * Adds 1.2 style TaglibVariables to the results list.
	 * 
	 * @param results
	 *            list where the <code>TaglibVariable</code> s are added
	 * @param node
	 */
	private void addVariables(List results, CMNode node, IStructuredDocumentRegion customTag) {

		List list = ((TLDElementDeclaration) node).getVariables();
		Iterator it = list.iterator();
		while (it.hasNext()) {
			TLDVariable var = (TLDVariable) it.next();
			String varName = var.getNameGiven();
			if (varName == null) {
				String attrName = var.getNameFromAttribute();
				/*
				 * Iterate through the document region to find the
				 * corresponding attribute name, and then use its value
				 */
				ITextRegionList regions = customTag.getRegions();
				boolean attrNameFound = false;
				for (int i = 2; i < regions.size(); i++) {
					ITextRegion region = regions.get(i);
					if (DOMRegionContext.XML_TAG_ATTRIBUTE_NAME.equals(region.getType())) {
						attrNameFound = attrName.equals(customTag.getText(region));
					}
					if (attrNameFound && DOMRegionContext.XML_TAG_ATTRIBUTE_VALUE.equals(region.getType())) {
						varName = StringUtils.strip(customTag.getText(region));
					}
				}
			}
			if (varName != null) {
				String varClass = "java.lang.String"; //$NON-NLS-1$ // the default class...
				if (var.getVariableClass() != null) {
					varClass = var.getVariableClass();
				}
				results.add(new TaglibVariable(varClass, varName, var.getScope()));
			}
		}
	}

	/**
	 * Adds 1.1 style TaglibVariables (defined in a TagExtraInfo class) to the
	 * results list.
	 * 
	 * @param customTag
	 * @param results
	 *            list where the <code>TaglibVariable</code> s are added
	 * @param decl
	 *            TLDElementDelcaration for the custom tag
	 * @param prefix
	 *            custom tag prefix
	 * @param uri
	 *            URI where the tld can be found
	 */
	private void addTEIVariables(IStructuredDocumentRegion customTag, List results, TLDElementDeclaration decl, String prefix, String uri) {

		String teiClassname = decl.getTeiclass();
		if (teiClassname == null || teiClassname.length() == 0)
			return;

		TaglibClassLoader loader = getClassloader();

		Class teiClass = null;
		try {
			teiClass = Class.forName(teiClassname, true, loader);
			if (teiClass != null) {
				Object teiObject = teiClass.newInstance();
				if (TagExtraInfo.class.isInstance(teiObject)) {
					TagExtraInfo tei = (TagExtraInfo) teiObject;
					Hashtable tagDataTable = extractTagData(customTag);
					TagInfo info = getTagInfo(decl, tei, prefix, uri);
					if (info != null) {
						tei.setTagInfo(info);

						// add to results
						TagData td = new TagData(tagDataTable);
						if (tei.isValid(td)) {
							VariableInfo[] vInfos = tei.getVariableInfo(td);
							if (vInfos != null) {
								for (int i = 0; i < vInfos.length; i++) {
									results.add(new TaglibVariable(vInfos[i].getClassName(), vInfos[i].getVarName(), vInfos[i].getScope()));
								}
							}
						}
					}
				}
			}
		}
		catch (ClassNotFoundException e) {
			// TEI class wasn't on classpath
			if (DEBUG)
				logException(teiClassname, e);
		}
		catch (InstantiationException e) {
			if (DEBUG)
				logException(teiClassname, e);
		}
		catch (IllegalAccessException e) {
			if (DEBUG)
				logException(teiClassname, e);
		}
		catch (ClassCastException e) {
			// TEI class wasn't really a subclass of TagExtraInfo
			if (DEBUG)
				logException(teiClassname, e);
		}
		catch (Exception e) {
			// this is 3rd party code, need to catch all exceptions
			if (DEBUG)
				logException(teiClassname, e);
		}
		catch (Error e) {
			// this is 3rd party code, need to catch all errors
			if (DEBUG)
				logException(teiClassname, e);
		}
		finally {
			// Thread.currentThread().setContextClassLoader(oldLoader);
		}
	}

	/**
	 * @param decl
	 * @return the TagInfo for the TLDELementDeclaration if the declaration is
	 *         valid, otherwise null
	 */
	private TagInfo getTagInfo(TLDElementDeclaration decl, TagExtraInfo tei, String prefix, String uri) {

		TagLibraryInfo libInfo = new TagLibraryInfo(prefix, uri) { /*
																	 * dummy
																	 * impl
																	 */
		};

		CMNamedNodeMap attrs = decl.getAttributes();
		TagAttributeInfo[] attrInfos = new TagAttributeInfo[attrs.getLength()];
		TLDAttributeDeclaration attr = null;
		String type = ""; //$NON-NLS-1$ 

		// get tag attribute infos
		for (int i = 0; i < attrs.getLength(); i++) {
			attr = (TLDAttributeDeclaration) attrs.item(i);
			type = attr.getType();
			// default value for type is String
			if (attr.getType() == null || attr.getType().equals("")) //$NON-NLS-1$ 
				type = "java.lang.String"; //$NON-NLS-1$ 
			attrInfos[i] = new TagAttributeInfo(attr.getAttrName(), attr.isRequired(), type, false);
		}

		String tagName = decl.getNodeName();
		String tagClass = decl.getTagclass();
		String bodyContent = decl.getBodycontent();
		if (tagName != null && tagClass != null && bodyContent != null)
			return new TagInfo(tagName, tagClass, bodyContent, decl.getInfo(), libInfo, tei, attrInfos);
		return null;

	}

	/**
	 * @param e
	 */
	private void logException(String teiClassname, Throwable e) {

		String message = "teiClassname: ["; //$NON-NLS-1$ 
		if (teiClassname != null)
			message += teiClassname;
		message += "]"; //$NON-NLS-1$
		Logger.logException(message, e);
	}

	/**
	 * Returns all attribute -> value pairs for the tag in a Hashtable.
	 * 
	 * @param customTag
	 * @return
	 */
	private Hashtable extractTagData(IStructuredDocumentRegion customTag) {

		Hashtable tagDataTable = new Hashtable();
		ITextRegionList regions = customTag.getRegions();
		ITextRegion r = null;
		String attrName = ""; //$NON-NLS-1$
		String attrValue = ""; //$NON-NLS-1$
		for (int i = 0; i < regions.size(); i++) {
			r = regions.get(i);
			// check if attr name
			if (r.getType() == DOMRegionContext.XML_TAG_ATTRIBUTE_NAME) {
				attrName = customTag.getText(r);
				// check equals is next region
				if (regions.size() > ++i) {
					r = regions.get(i);
					if (r.getType() == DOMRegionContext.XML_TAG_ATTRIBUTE_EQUALS && regions.size() > ++i) {
						// get attr value
						r = regions.get(i);
						if (r.getType() == DOMRegionContext.XML_TAG_ATTRIBUTE_VALUE) {
							r = regions.get(i);
							// attributes in our document have quotes, so we
							// need to strip them
							attrValue = StringUtils.stripQuotes(customTag.getText(r));
							tagDataTable.put(attrName, attrValue);
						}
					}
				}
			}
		}
		return tagDataTable;
	}

	private TaglibClassLoader getClassloader() {

		if (fLoader == null) {
			fLoader = new TaglibClassLoader(this.getClass().getClassLoader());
			fProjectEntries.clear();
			fContainerEntries.clear();
			addClasspathEntriesForProject(getProject(), fLoader);
		}
		return fLoader;
	}

	/**
	 * @param loader
	 */
	private void addClasspathEntriesForProject(IProject p, TaglibClassLoader loader) {

		// avoid infinite recursion and closed project
		if (!p.isAccessible() || fProjectEntries.contains(p.getFullPath().toString()))
			return;
		fProjectEntries.add(p.getFullPath().toString());

		// add things on classpath that we are interested in
		if (p != null) {
			try {
				if (p.hasNature(JavaCore.NATURE_ID)) {

					IJavaProject project = JavaCore.create(p);
					IPath wkspaceRoot = ResourcesPlugin.getWorkspace().getRoot().getLocation();

					try {
						IClasspathEntry[] entries = project.getRawClasspath();
						addDefaultDirEntry(loader, project, wkspaceRoot);
						addClasspathEntries(loader, project, wkspaceRoot, entries);
					}
					catch (JavaModelException e) {
						Logger.logException(e);
					}
				}
			}
			catch (CoreException e) {
				Logger.logException(e);
			}
		}
	}

	private void addClasspathEntries(TaglibClassLoader loader, IJavaProject project, IPath wkspaceRoot, IClasspathEntry[] entries) throws JavaModelException {
		IClasspathEntry entry;
		for (int i = 0; i < entries.length; i++) {

			entry = entries[i];
			if (DEBUG)
				System.out.println("current entry is: " + entry); //$NON-NLS-1$

			switch (entry.getEntryKind()) {
				case IClasspathEntry.CPE_SOURCE :
					addSourceEntry(loader, wkspaceRoot, entry);
					break;
				case IClasspathEntry.CPE_LIBRARY :
					addLibraryEntry(loader, wkspaceRoot, entry.getPath().toString());
					break;
				case IClasspathEntry.CPE_PROJECT :
					addProjectEntry(loader, entry);
					break;
				case IClasspathEntry.CPE_VARIABLE :
					addVariableEntry(loader, wkspaceRoot, entry);
					break;
				case IClasspathEntry.CPE_CONTAINER :
					addContainerEntry(loader, project, wkspaceRoot, entry);
					break;
			}
		}
	}

	/**
	 * @param loader
	 * @param entry
	 */
	private void addVariableEntry(TaglibClassLoader loader, IPath wkspaceRoot, IClasspathEntry entry) {

		if (DEBUG)
			System.out.println(" -> adding variable entry: [" + entry + "]"); //$NON-NLS-1$ //$NON-NLS-2$

		// variable should either be a project or a library entry

		String variableName = entry.getPath().toString();
		IPath variablePath = JavaCore.getClasspathVariable(variableName);

		// RATLC01076854
		// variable paths may not exist
		// in that case null will be returned
		if (variablePath != null) {
			if (variablePath.segments().length == 1) {
				IProject varProj = ResourcesPlugin.getWorkspace().getRoot().getProject(variablePath.toString());
				if (varProj != null && varProj.exists()) {
					addClasspathEntriesForProject(varProj, loader);
					return;
				}
			}
			addLibraryEntry(loader, wkspaceRoot, variablePath.toString());
		}
	}

	/**
	 * @param loader
	 * @param project
	 * @param wkspaceRoot
	 * @param entry
	 * @throws JavaModelException
	 */
	private void addContainerEntry(TaglibClassLoader loader, IJavaProject project, IPath wkspaceRoot, IClasspathEntry entry) throws JavaModelException {

		IClasspathContainer container = JavaCore.getClasspathContainer(entry.getPath(), project);
		if (container != null) {
			// avoid infinite recursion
			if (!fContainerEntries.contains(container.getPath().toString())) {
				fContainerEntries.add(container.getPath().toString());

				IClasspathEntry[] cpes = container.getClasspathEntries();
				// recursive call here
				addClasspathEntries(loader, project, wkspaceRoot, cpes);
			}
		}
	}

	/**
	 * @param loader
	 * @param entry
	 */
	private void addProjectEntry(TaglibClassLoader loader, IClasspathEntry entry) {

		if (DEBUG)
			System.out.println(" -> project entry: [" + entry + "]"); //$NON-NLS-1$ //$NON-NLS-2$

		IPath path = entry.getPath();
		IProject refereceProj = ResourcesPlugin.getWorkspace().getRoot().getProject(path.toString());
		if (refereceProj != null && refereceProj.exists())
			addClasspathEntriesForProject(refereceProj, loader);
	}

	/**
	 * @param loader
	 * @param project
	 * @param wkspaceRoot
	 * @throws JavaModelException
	 */
	private void addDefaultDirEntry(TaglibClassLoader loader, IJavaProject project, IPath wkspaceRoot) throws JavaModelException {

		// add default bin directory for the project
		IPath outputLocation = project.getOutputLocation();
		if (!outputLocation.toFile().exists()) {
			outputLocation = wkspaceRoot.append(outputLocation);
		}
		loader.addDirectory(outputLocation.toString());
	}

	/**
	 * @param loader
	 * @param wkspaceRoot
	 * @param entry
	 */
	private void addLibraryEntry(TaglibClassLoader loader, IPath wkspaceRoot, String libPath) {

		String jarPath = libPath;
		File file = new File(jarPath);

		// if not absolute path, it's workspace relative
		if (!file.exists()) {
			jarPath = wkspaceRoot.append(jarPath).toString();
		}

		if (jarPath.endsWith(".jar")) { //$NON-NLS-1$ 
			loader.addJar(jarPath);
		}
		else if (file.isDirectory()) {
			// it's actually a folder containing binaries
			loader.addDirectory(jarPath);
		}
	}

	/**
	 * @param loader
	 * @param wkspaceRoot
	 * @param entry
	 */
	private void addSourceEntry(TaglibClassLoader loader, IPath wkspaceRoot, IClasspathEntry entry) {

		// add bin directory for specific entry if it has
		// one
		if (entry.getOutputLocation() != null) {
			String outputPath = entry.getOutputLocation().toString();
			File file = entry.getOutputLocation().toFile();
			// if not absolute path, it's workspace relative
			if (!file.exists()) {
				outputPath = wkspaceRoot.append(entry.getOutputLocation()).toString();
			}
			loader.addDirectory(outputPath);
		}
	}

	/**
	 * @return Returns the fModelQuery.
	 */
	public ModelQuery getModelQuery(IDocument doc) {
		IStructuredModel model = null;
		ModelQuery mq = null;
		try {
			model = StructuredModelManager.getModelManager().getExistingModelForRead(doc);
			mq = ModelQueryUtil.getModelQuery(model);
		}
		finally {
			if (model != null)
				model.releaseFromRead();
		}
		return mq;
	}


	/**
	 * @return Returns the fFile.
	 */
	public IProject getProject() {

		return fProject;
	}

	/**
	 * @param file
	 *            The fFile to set.
	 */
	public void setProject(IProject p) {
		fProject = p;
	}
}
