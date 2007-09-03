/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     
 *******************************************************************************/
package org.eclipse.jst.jsp.core.internal.contenttype;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jst.jsp.core.internal.Logger;
import org.eclipse.jst.jsp.core.internal.util.CommonXML;
import org.eclipse.jst.jsp.core.internal.util.FileContentCache;
import org.eclipse.jst.jsp.core.taglib.TaglibIndex;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * A cache fo property group information stored in web.xml files. Information
 * is not persisted.
 */
public class DeploymentDescriptorPropertyCache {
	private static final PropertyGroup[] NO_PROPERTY_GROUPS = new PropertyGroup[0];

	private static class DeploymentDescriptor {
		PropertyGroup[] groups;
		long modificationStamp;
		Float version = new Float(defaultWebAppVersion);
	}

	private static class FacetCore {
		long modificationStamp;
		float version = defaultWebAppVersion;
	}

	/**
	 * Represntation of the JSP 2.0 property-group definitions from a servlet
	 * deployment descriptor.
	 */
	public static final class PropertyGroup {
		static PropertyGroup createFrom(IPath path, Node propertyGroupNode, int groupNumber) {
			PropertyGroup group = new PropertyGroup(path, groupNumber);
			Node propertyGroupID = propertyGroupNode.getAttributes().getNamedItem(ID);
			if (propertyGroupID != null) {
				group.setId(propertyGroupID.getNodeValue());
			}
			Node node = propertyGroupNode.getFirstChild();
			while (node != null) {
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					String name = node.getLocalName();
					if (name == null) {
						name = node.getNodeName();
					}
					if (IS_XML.equals(name)) {
						group.setIsXML(getContainedText(node));
					}
					else if (EL_IGNORED.equals(name)) {
						group.setElignored(getContainedText(node));
					}
					else if (INCLUDE_CODA.equals(name)) {
						group.addCoda(getContainedText(node));
					}
					else if (INCLUDE_PRELUDE.equals(name)) {
						group.addPrelude(getContainedText(node));
					}
					else if (SCRIPTING_INVALID.equals(name)) {
						group.setScriptingInvalid(getContainedText(node));
					}
					else if (PAGE_ENCODING.equals(name)) {
						group.setPageEncoding(getContainedText(node));
					}
					else if (URL_PATTERN.equals(name)) {
						group.setUrlPattern(getContainedText(node));
					}
				}

				node = node.getNextSibling();
			}

			return group;
		}

		private boolean el_ignored;

		private String id;

		private IPath[] include_coda = new IPath[0];

		private IPath[] include_prelude = new IPath[0];
		private boolean is_xml;
		private StringMatcher matcher;
		private String page_encoding;
		private boolean scripting_invalid;
		String url_pattern;
		private IPath webxmlPath;
		
		int number;

		private PropertyGroup(IPath path, int number) {
			super();
			this.webxmlPath = path;
			this.number = number;
		}

		private void addCoda(String containedText) {
			if (containedText.length() > 0) {
				IPath[] codas = new IPath[include_coda.length + 1];
				System.arraycopy(include_coda, 0, codas, 0, include_coda.length);
				codas[include_coda.length] = webxmlPath.removeLastSegments(2).append(containedText);
				include_coda = codas;
			}
		}

		private void addPrelude(String containedText) {
			if (containedText.length() > 0) {
				IPath[] preludes = new IPath[include_prelude.length + 1];
				System.arraycopy(include_prelude, 0, preludes, 0, include_prelude.length);
				preludes[include_prelude.length] = webxmlPath.removeLastSegments(2).append(containedText);
				include_prelude = preludes;
			}
		}

		public String getId() {
			return id;
		}

		public IPath[] getIncludeCoda() {
			return include_coda;
		}

		public IPath[] getIncludePrelude() {
			return include_prelude;
		}

		public String getPageEncoding() {
			return page_encoding;
		}

		public String getUrlPattern() {
			return url_pattern;
		}

		public boolean isELignored() {
			return el_ignored;
		}

		public boolean isIsXML() {
			return is_xml;
		}

		public boolean isScriptingInvalid() {
			return scripting_invalid;
		}

		boolean matches(String pattern, boolean optimistic) {
			if (matcher == null)
				return optimistic;
			return matcher.match(pattern);
		}

		private void setElignored(String el_ignored) {
			this.el_ignored = Boolean.valueOf(el_ignored).booleanValue();
		}

		private void setId(String id) {
			this.id = id;
		}

		private void setIsXML(String is_xml) {
			this.is_xml = Boolean.valueOf(is_xml).booleanValue();
		}

		private void setPageEncoding(String page_encoding) {
			this.page_encoding = page_encoding;
		}

		private void setScriptingInvalid(String scripting_invalid) {
			this.scripting_invalid = Boolean.valueOf(scripting_invalid).booleanValue();
		}

		private void setUrlPattern(String url_pattern) {
			this.url_pattern = url_pattern;
			if (url_pattern != null && url_pattern.length() > 0) {
				this.matcher = new StringMatcher(url_pattern);
			}
		}
		
		public String toString() {
			return number + ":" + url_pattern;
		}
	}

	private static class ResourceChangeListener implements IResourceChangeListener {
		public void resourceChanged(IResourceChangeEvent event) {
			IResourceDelta delta = event.getDelta();
			if (event.getType() != IResourceChangeEvent.POST_CHANGE)
				return;
			if (delta.getKind() == IResourceDelta.CHANGED && (delta.getFlags() == IResourceDelta.ENCODING || delta.getFlags() == IResourceDelta.MARKERS))
				return;
			IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {
				public boolean visit(IResourceDelta delta) {
					IResource resource = delta.getResource();
					if (resource.getType() == IResource.FILE) {
						IPath path = resource.getFullPath();
						int segmentCount = path.segmentCount();
						if (segmentCount > 1 && path.lastSegment().equals(WEB_XML) && path.segment(segmentCount - 2).equals(WEB_INF)) {
							getInstance().deploymentDescriptorChanged(path);
						}
					}
					return true;
				}
			};
			try {
				delta.accept(visitor);
			}
			catch (CoreException e) {
				Logger.logException(e);
			}
		}
	}

	private static class ResourceErrorHandler implements ErrorHandler {
		private boolean fDoLogExceptions = false;
		private IPath fPath;

		ResourceErrorHandler(boolean logExceptions) {
			super();
			fDoLogExceptions = logExceptions;
		}

		public void error(SAXParseException exception) throws SAXException {
			if (fDoLogExceptions)
				Logger.log(Logger.WARNING, "SAXParseException with " + fPath + " (error) while reading descriptor: " + exception.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		public void fatalError(SAXParseException exception) throws SAXException {
			if (fDoLogExceptions)
				Logger.log(Logger.WARNING, "SAXParseException with " + fPath + " (fatalError) while reading descriptor: " + exception.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		public void setPath(IPath path) {
			fPath = path;
		}

		public void warning(SAXParseException exception) throws SAXException {
			if (fDoLogExceptions)
				Logger.log(Logger.WARNING, "SAXParseException with " + fPath + " (warning) while reading descriptor: " + exception.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}

	/**
	 * Copied from org.eclipse.core.internal.propertytester.StringMatcher, but
	 * should be replaced with a more accurate implementation of the rules in
	 * Servlet spec SRV.11.2
	 */
	private static class StringMatcher {
		private static final char SINGLE_WILD_CARD = '\u0000';

		/**
		 * Boundary value beyond which we don't need to search in the text
		 */
		private int bound = 0;

		private boolean hasLeadingStar;

		private boolean hasTrailingStar;

		private final String pattern;

		private final int patternLength;

		/**
		 * The pattern split into segments separated by *
		 */
		private String segments[];

		/**
		 * StringMatcher constructor takes in a String object that is a simple
		 * pattern which may contain '*' for 0 and many characters and '?' for
		 * exactly one character.
		 * 
		 * Literal '*' and '?' characters must be escaped in the pattern e.g.,
		 * "\*" means literal "*", etc.
		 * 
		 * Escaping any other character (including the escape character
		 * itself), just results in that character in the pattern. e.g., "\a"
		 * means "a" and "\\" means "\"
		 * 
		 * If invoking the StringMatcher with string literals in Java, don't
		 * forget escape characters are represented by "\\".
		 * 
		 * @param pattern
		 *            the pattern to match text against
		 */
		StringMatcher(String pattern) {
			if (pattern == null)
				throw new IllegalArgumentException();
			this.pattern = pattern;
			patternLength = pattern.length();
			parseWildCards();
		}

		/**
		 * @param text
		 *            a simple regular expression that may only contain '?'(s)
		 * @param start
		 *            the starting index in the text for search, inclusive
		 * @param end
		 *            the stopping point of search, exclusive
		 * @param p
		 *            a simple regular expression that may contain '?'
		 * @return the starting index in the text of the pattern , or -1 if
		 *         not found
		 */
		private int findPosition(String text, int start, int end, String p) {
			boolean hasWildCard = p.indexOf(SINGLE_WILD_CARD) >= 0;
			int plen = p.length();
			for (int i = start, max = end - plen; i <= max; ++i) {
				if (hasWildCard) {
					if (regExpRegionMatches(text, i, p, 0, plen))
						return i;
				}
				else {
					if (text.regionMatches(true, i, p, 0, plen))
						return i;
				}
			}
			return -1;
		}

		/**
		 * Given the starting (inclusive) and the ending (exclusive) positions
		 * in the <code>text</code>, determine if the given substring
		 * matches with aPattern
		 * 
		 * @return true if the specified portion of the text matches the
		 *         pattern
		 * @param text
		 *            a String object that contains the substring to match
		 */
		public boolean match(String text) {
			if (text == null)
				return false;
			final int end = text.length();
			final int segmentCount = segments.length;
			if (segmentCount == 0 && (hasLeadingStar || hasTrailingStar)) // pattern
				// contains
				// only
				// '*'(s)
				return true;
			if (end == 0)
				return patternLength == 0;
			if (patternLength == 0)
				return false;
			int currentTextPosition = 0;
			if ((end - bound) < 0)
				return false;
			int segmentIndex = 0;
			String current = segments[segmentIndex];

			/* process first segment */
			if (!hasLeadingStar) {
				int currentLength = current.length();
				if (!regExpRegionMatches(text, 0, current, 0, currentLength))
					return false;
				segmentIndex++;
				currentTextPosition = currentTextPosition + currentLength;
			}
			if ((segmentCount == 1) && (!hasLeadingStar) && (!hasTrailingStar)) {
				// only one segment to match, no wild cards specified
				return currentTextPosition == end;
			}
			/* process middle segments */
			while (segmentIndex < segmentCount) {
				current = segments[segmentIndex];
				int currentMatch = findPosition(text, currentTextPosition, end, current);
				if (currentMatch < 0)
					return false;
				currentTextPosition = currentMatch + current.length();
				segmentIndex++;
			}

			/* process final segment */
			if (!hasTrailingStar && currentTextPosition != end) {
				int currentLength = current.length();
				return regExpRegionMatches(text, end - currentLength, current, 0, currentLength);
			}
			return segmentIndex == segmentCount;
		}

		/**
		 * Parses the pattern into segments separated by wildcard '*'
		 * characters.
		 */
		private void parseWildCards() {
			if (pattern.startsWith("*"))//$NON-NLS-1$
				hasLeadingStar = true;
			if (pattern.endsWith("*")) {//$NON-NLS-1$
				/* make sure it's not an escaped wildcard */
				if (patternLength > 1 && pattern.charAt(patternLength - 2) != '\\') {
					hasTrailingStar = true;
				}
			}

			ArrayList temp = new ArrayList();

			int pos = 0;
			StringBuffer buf = new StringBuffer();
			while (pos < patternLength) {
				char c = pattern.charAt(pos++);
				switch (c) {
					case '\\' :
						if (pos >= patternLength) {
							buf.append(c);
						}
						else {
							char next = pattern.charAt(pos++);
							/* if it's an escape sequence */
							if (next == '*' || next == '?' || next == '\\') {
								buf.append(next);
							}
							else {
								/*
								 * not an escape sequence, just insert
								 * literally
								 */
								buf.append(c);
								buf.append(next);
							}
						}
						break;
					case '*' :
						if (buf.length() > 0) {
							/* new segment */
							temp.add(buf.toString());
							bound += buf.length();
							buf.setLength(0);
						}
						break;
					case '?' :
						/*
						 * append special character representing single match
						 * wildcard
						 */
						buf.append(SINGLE_WILD_CARD);
						break;
					default :
						buf.append(c);
				}
			}

			/* add last buffer to segment list */
			if (buf.length() > 0) {
				temp.add(buf.toString());
				bound += buf.length();
			}
			segments = (String[]) temp.toArray(new String[temp.size()]);
		}

		/**
		 * 
		 * @return boolean
		 * @param text
		 *            a String to match
		 * @param tStart
		 *            the starting index of match, inclusive
		 * @param p
		 *            a simple regular expression that may contain '?'
		 * @param pStart
		 *            The start position in the pattern
		 * @param plen
		 *            The length of the pattern
		 */
		private boolean regExpRegionMatches(String text, int tStart, String p, int pStart, int plen) {
			while (plen-- > 0) {
				char tchar = text.charAt(tStart++);
				char pchar = p.charAt(pStart++);

				// process wild cards, skipping single wild cards
				if (pchar == SINGLE_WILD_CARD)
					continue;
				if (pchar == tchar)
					continue;
				if (Character.toUpperCase(tchar) == Character.toUpperCase(pchar))
					continue;
				// comparing after converting to upper case doesn't handle all
				// cases;
				// also compare after converting to lower case
				if (Character.toLowerCase(tchar) == Character.toLowerCase(pchar))
					continue;
				return false;
			}
			return true;
		}
	}

	private static DeploymentDescriptorPropertyCache _instance = new DeploymentDescriptorPropertyCache();

	private static final float defaultWebAppVersion = 2.4f;
	private static String EL_IGNORED = "el-ignored";
	private static final String FACET_FACET = "facet";
	private static final String FACET_INSTALLED = "installed";
	private static final String FACET_VERSION = "version";
	private static final String FACET_WEB_APP = "jst.web";
	private static String ID = "id";
	private static String INCLUDE_CODA = "include-coda";
	private static String INCLUDE_PRELUDE = "include-prelude";

	private static String IS_XML = "is-xml";
	private static String JSP_PROPERTY_GROUP = "jsp-property-group";
	private static String PAGE_ENCODING = "page-encoding";

	private static String SCRIPTING_INVALID = "scripting-invalid";
	private static String URL_PATTERN = "url-pattern";
	private static final String WEB_APP_ELEMENT_LOCAL_NAME = ":web-app";
	private static final String WEB_APP_ELEMENT_NAME = "web-app";

	private static final String WEB_APP_VERSION_NAME = "version";
	private static final String WEB_INF = "WEB-INF";
	private static final String WEB_XML = "web.xml";
	private static final String WEB_INF_WEB_XML = WEB_INF + IPath.SEPARATOR + WEB_XML;

	static String getContainedText(Node parent) {
		NodeList children = parent.getChildNodes();
		if (children.getLength() == 1) {
			return children.item(0).getNodeValue().trim();
		}
		StringBuffer s = new StringBuffer();
		Node child = parent.getFirstChild();
		while (child != null) {
			if (child.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
				String reference = ((EntityReference) child).getNodeValue();
				if (reference == null && child.getNodeName() != null) {
					reference = "&" + child.getNodeName() + ";"; //$NON-NLS-1$ //$NON-NLS-2$
				}
				if (reference != null) {
					s.append(reference.trim());
				}
			}
			else {
				s.append(child.getNodeValue().trim());
			}
			child = child.getNextSibling();
		}
		return s.toString().trim();
	}

	public static DeploymentDescriptorPropertyCache getInstance() {
		return _instance;
	}

	public static void start() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(getInstance().fResourceChangeListener, IResourceChangeEvent.POST_CHANGE);
	}

	public static void stop() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(getInstance().fResourceChangeListener);
	}

	private ResourceErrorHandler errorHandler;

	private Map fDeploymentDescriptors = new Hashtable();
	private Map fFacetCores = new Hashtable();

	private IResourceChangeListener fResourceChangeListener = new ResourceChangeListener();

	private EntityResolver resolver;


	private DeploymentDescriptorPropertyCache() {
		super();
	}

	private void _parseDocument(IFile file, Float[] version, List groupList, SubProgressMonitor subMonitor, Document document) {
		Element webapp = document.getDocumentElement();
		if (webapp != null) {
			if (webapp.getTagName().equals(WEB_APP_ELEMENT_NAME) || webapp.getNodeName().endsWith(WEB_APP_ELEMENT_LOCAL_NAME)) {
				String versionValue = webapp.getAttribute(WEB_APP_VERSION_NAME);
				if (versionValue != null) {
					try {
						version[0] = Float.valueOf(versionValue);
					}
					catch (NumberFormatException e) {
						// doesn't matter
					}
				}
			}
		}
		NodeList propertyGroupElements = document.getElementsByTagName(JSP_PROPERTY_GROUP);
		int length = propertyGroupElements.getLength();
		subMonitor.beginTask("Reading Property Groups", length);
		for (int i = 0; i < length; i++) {
			PropertyGroup group = PropertyGroup.createFrom(file.getFullPath(), propertyGroupElements.item(i), i);
			subMonitor.worked(1);
			if (group != null) {
				groupList.add(group);
			}
		}
	}

	/**
	 * Convert the SRV spec version to the JSP spec version
	 */
	private float convertSpecVersions(float version) {
		if (version > 0) {
			if (version == 2.5f)
				return 2.1f;
			else if (version == 2.4f)
				return 2.0f;
			else if (version == 2.3f)
				return 1.2f;
			else if (version == 2.2f)
				return 1.1f;
			else if (version == 2.1f)
				return 1.0f;
		}
		return convertSpecVersions(defaultWebAppVersion);
	}

	void deploymentDescriptorChanged(final IPath fullPath) {
		if (fDeploymentDescriptors.containsKey(fullPath.makeAbsolute())) {
			updateCacheEntry(fullPath);
		}
	}

	/**
	 * parse the specified resource using Xerces, and if that fails, use the
	 * SSE XML parser to find the property groups.
	 */
	private DeploymentDescriptor fetchDescriptor(IFile file, IProgressMonitor monitor) {
		monitor.beginTask("Reading Deployment Descriptor", 3);
		PropertyGroup groups[] = null;

		IStructuredModel model = null;
		List groupList = new ArrayList();
		Float[] version = new Float[1];
		SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 2);
		DocumentBuilder builder = CommonXML.getDocumentBuilder(false);
		builder.setEntityResolver(getEntityResolver());
		builder.setErrorHandler(getErrorHandler(file.getFullPath()));
		InputStream is = null;
		try {
			InputSource inputSource = new InputSource();
			is = file.getContents();
			inputSource.setByteStream(is);
			inputSource.setSystemId(file.getFullPath().toString());
			Document document = builder.parse(inputSource);
			_parseDocument(file, version, groupList, subMonitor, document);
		}
		catch (SAXException e1) {
			/* encountered a fatal parsing error, try our own parser */
			try {
				/**
				 * Chiefly because the web.xml file itself is editable, use
				 * SSE to get the DOM Document because it is more fault
				 * tolerant.
				 */
				model = StructuredModelManager.getModelManager().getModelForRead(file);
				monitor.worked(1);
				if (model instanceof IDOMModel) {
					IDOMDocument document = ((IDOMModel) model).getDocument();
					_parseDocument(file, version, groupList, subMonitor, document);
				}
			}
			catch (Exception e) {
				Logger.logException(e);
			}
			finally {
				if (model != null) {
					model.releaseFromRead();
				}
			}
		}
		catch (IOException e1) {
			/* file is unreadable, create no property groups */
		}
		catch (CoreException e) {
			/*
			 * file is unreadable, create no property groups but log the
			 * exception
			 */
			Logger.logException(e);
		}
		finally {
			if (is != null)
				try {
					is.close();
				}
				catch (IOException e) {
					// nothing to do
				}
			groups = (PropertyGroup[]) groupList.toArray(new PropertyGroup[groupList.size()]);
			subMonitor.done();
		}

		if (groups == null) {
			groups = NO_PROPERTY_GROUPS;
		}

		DeploymentDescriptor deploymentDescriptor = new DeploymentDescriptor();
		deploymentDescriptor.modificationStamp = file.getModificationStamp();
		deploymentDescriptor.groups = groups;
		deploymentDescriptor.version = version[0];
		monitor.done();
		fDeploymentDescriptors.put(file.getFullPath(), new SoftReference(deploymentDescriptor));
		return deploymentDescriptor;
	}

	private FacetCore fetchFacetCore(IFile facetConfigFile) {
		FacetCore facetCore = new FacetCore();
		facetCore.modificationStamp = facetConfigFile.getModificationStamp();
		facetCore.version = defaultWebAppVersion;

		DocumentBuilder builder = CommonXML.getDocumentBuilder(false);
		builder.setEntityResolver(getEntityResolver());
		builder.setErrorHandler(getErrorHandler(facetConfigFile.getFullPath()));
		String input = null;
		try {
			InputSource inputSource = new InputSource();
			input = FileContentCache.getInstance().getContents(facetConfigFile.getFullPath());
			inputSource.setCharacterStream(new StringReader(input));
			inputSource.setSystemId(facetConfigFile.getFullPath().toString());
			Document document = builder.parse(inputSource);
			NodeList installedList = document.getElementsByTagName(FACET_INSTALLED);
			for (int i = 0; i < installedList.getLength(); i++) {
				Element installed = (Element) installedList.item(i);
				String facetName = installed.getAttribute(FACET_FACET);
				if (FACET_WEB_APP.equals(facetName)) {
					try {
						facetCore.version = Float.valueOf(installed.getAttribute(FACET_VERSION)).floatValue();
					}
					catch (NumberFormatException e) {
						// badly written file
					}
				}
			}
		}
		catch (SAXException e1) {
			Logger.logException(e1);
		}
		catch (IOException e1) {
			// unlikely
		}
		fFacetCores.put(facetConfigFile.getFullPath(), new SoftReference(facetCore));
		return facetCore;
	}

	private EntityResolver getEntityResolver() {
		if (resolver == null) {
			resolver = new EntityResolver() {
				public InputSource resolveEntity(String publicID, String systemID) throws SAXException, IOException {
					InputSource result = new InputSource(new ByteArrayInputStream(new byte[0]));
					result.setPublicId(publicID);
					result.setSystemId(systemID != null ? systemID : "/_" + getClass().getName()); //$NON-NLS-1$
					return result;
				}
			};
		}
		return resolver;
	}

	/**
	 * Returns an ErrorHandler that will not stop the parser on reported
	 * errors
	 */
	private ErrorHandler getErrorHandler(IPath path) {
		if (errorHandler == null) {
			errorHandler = new ResourceErrorHandler(false);
		}
		errorHandler.setPath(path);
		return errorHandler;
	}

	/**
	 * @param fullPath
	 * @return the JSP version supported by the web application containing the
	 *         path. A value stated within a web.xml file takes priority.
	 */
	public float getJSPVersion(IPath fullPath) {
		float version = defaultWebAppVersion;
		IPath contextRoot = TaglibIndex.getContextRoot(fullPath);
		if (contextRoot != null) {
			IPath webxmlPath = contextRoot.append(WEB_INF_WEB_XML);
			IFile webxmlFile = ResourcesPlugin.getWorkspace().getRoot().getFile(webxmlPath);
			if (webxmlFile.isAccessible()) {
				Reference descriptorHolder = (Reference) fDeploymentDescriptors.get(webxmlPath);
				DeploymentDescriptor descriptor = null;

				if (descriptorHolder == null || ((descriptor = (DeploymentDescriptor) descriptorHolder.get()) == null) || (descriptor.modificationStamp == IResource.NULL_STAMP) || (descriptor.modificationStamp != webxmlFile.getModificationStamp())) {
					descriptor = fetchDescriptor(webxmlFile, new NullProgressMonitor());
				}

				if (descriptor.version != null) {
					version = descriptor.version.floatValue();
					return convertSpecVersions(version);
				}
			}
		}
		// Parse the .settings/org.eclipse.wst.common.project.facet.core.xml
		IPath facetConfigPath = new Path(fullPath.makeAbsolute().segment(0)).append(".settings/org.eclipse.wst.common.project.facet.core.xml");
		IFile facetConfigFile = ResourcesPlugin.getWorkspace().getRoot().getFile(facetConfigPath);
		if (facetConfigFile.isAccessible()) {
			FacetCore facetCore = null;
			Reference facetCoreHolder = (Reference) fFacetCores.get(facetConfigPath);
			if (facetCoreHolder == null || ((facetCore = (FacetCore) facetCoreHolder.get()) == null) || (facetCore.modificationStamp == IResource.NULL_STAMP) || (facetCore.modificationStamp != facetConfigFile.getModificationStamp())) {
				facetCore = fetchFacetCore(facetConfigFile);
				version = facetCore.version;
			}

		}
		return convertSpecVersions(version);
	}

	/**
	 * @param jspFilePath
	 * @return PropertyGroups matching the file at the given path or an empty
	 *         array if no web.xml file exists or no matching property group
	 *         was defined. A returned PropertyGroup object should be
	 *         considered short-lived and not saved for later use.
	 */
	public PropertyGroup[] getPropertyGroups(IPath jspFilePath) {
		List matchingGroups = new ArrayList(1);
		IPath contextRoot = TaglibIndex.getContextRoot(jspFilePath);
		if (contextRoot == null)
			return NO_PROPERTY_GROUPS;

		IPath webxmlPath = contextRoot.append(WEB_INF_WEB_XML);
		IFile webxmlFile = ResourcesPlugin.getWorkspace().getRoot().getFile(webxmlPath);
		if (!webxmlFile.isAccessible())
			return NO_PROPERTY_GROUPS;

		Reference descriptorHolder = (Reference) fDeploymentDescriptors.get(webxmlPath);
		DeploymentDescriptor descriptor = null;

		if (descriptorHolder == null || ((descriptor = (DeploymentDescriptor) descriptorHolder.get()) == null) || (descriptor.modificationStamp == IResource.NULL_STAMP) || (descriptor.modificationStamp != webxmlFile.getModificationStamp())) {
			descriptor = fetchDescriptor(webxmlFile, new NullProgressMonitor());
		}

		for (int i = 0; i < descriptor.groups.length; i++) {
			if (descriptor.groups[i].matches(jspFilePath.removeFirstSegments(contextRoot.segmentCount()).makeAbsolute().toString(), false)) {
				matchingGroups.add(descriptor.groups[i]);
			}
		}
		if (matchingGroups.isEmpty()) {
			for (int i = 0; i < descriptor.groups.length; i++) {
				if (descriptor.groups[i].matches(jspFilePath.removeFirstSegments(contextRoot.segmentCount()).toString(), true)) {
					matchingGroups.add(descriptor.groups[i]);
				}
			}
		}
		return (PropertyGroup[]) matchingGroups.toArray(new PropertyGroup[matchingGroups.size()]);
	}

	private void updateCacheEntry(IPath fullPath) {
		/* don't update right now; remove and wait for another query to update */
		fDeploymentDescriptors.remove(fullPath);
	}
}
