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
package org.eclipse.jst.jsp.core.internal.contentmodel.tld;



import java.util.ArrayList;
import java.util.List;

import org.eclipse.jst.jsp.core.contentmodel.tld.JSP11TLDNames;
import org.eclipse.jst.jsp.core.contentmodel.tld.JSP12TLDNames;
import org.eclipse.jst.jsp.core.contentmodel.tld.TLDDocument;
import org.eclipse.jst.jsp.core.contentmodel.tld.TLDElementDeclaration;
import org.eclipse.wst.common.contentmodel.CMContent;
import org.eclipse.wst.common.contentmodel.CMDataType;
import org.eclipse.wst.common.contentmodel.CMDocument;
import org.eclipse.wst.common.contentmodel.CMNamedNodeMap;
import org.eclipse.wst.common.contentmodel.CMNode;
import org.eclipse.wst.common.contentmodel.annotation.AnnotationMap;
import org.eclipse.wst.sse.core.util.StringUtils;

public class CMElementDeclarationImpl implements TLDElementDeclaration {

	// optional attributes
	public CMNamedNodeMapImpl attributes = new CMNamedNodeMapImpl();
	// (empty|JSP|tagdependant|scriptless) - optional, defaults to JSP
	private String bodycontent = JSP11TLDNames.CONTENT_JSP;

	/** 
	 * since JSP 1.2
	 * 
	 * Usage information
	 */
	private String description;
	
	/** 
	 * since JSP 1.2
	 */
	private String displayName;

	/** 
	 * since JSP 2.0
	 */
	private String fExample;

	private CMDocument fOwnerDocument;
	
	private String fPath = null;

	private List fTagExtensions = new ArrayList(0);
	/** 
	 * since JSP 1.2
	 */
	private String largeIcon;
	
	
	private int maxOccur = -1;
	private int minOccur = 0;

	// required tag name
	private String nodeName = null;
	
	/** 
	 * since JSP 1.2
	 */
	private String smallIcon;
	
	// tag handler class - required
	private String tagclass = null;
	
	// tag extra info class (subclass of javax.servlet.jsp.TagExtraInfo) - optional
	private String teiclass = null;

	/** 
	 * since JSP 1.2
	 */
	private List variables;

	/**
	 * CMElementDeclarationImpl constructor comment.
	 */
	public CMElementDeclarationImpl(CMDocument owner) {
		super();
		fOwnerDocument = owner;
	}

	/**
	 * getAttributes method
	 * @return CMNamedNodeMap
	 *
	 * Returns CMNamedNodeMap of AttributeDeclaration
	 */
	public CMNamedNodeMap getAttributes() {
		return attributes;
	}

	/**
	 * 
	 * @return java.lang.String
	 */
	public String getBodycontent() {
		return bodycontent;
	}

	/**
	 * getCMContent method
	 * @return CMContent
	 *
	 * Returns the root node of this element's content model.
	 * This can be an CMElementDeclaration or a CMGroup
	 */
	public CMContent getContent() {
		return null;
	}

	/**
	 * getContentType method
	 * @return int
	 *
	 * Returns one of :
	 * ANY, EMPTY, ELEMENT, MIXED, PCDATA, CDATA.
	 */
	public int getContentType() {
		if (bodycontent.equals(JSP11TLDNames.CONTENT_EMPTY))
			return EMPTY;
		if (bodycontent.equals(JSP11TLDNames.CONTENT_TAGDEPENDENT))
			return PCDATA;
		else
			// JSP
			return ANY;
	}

	/**
	 * getDataType method
	 * @return java.lang.String
	 */
	public CMDataType getDataType() {
		return new CMDataTypeImpl(CMDataType.CDATA);
	}

	/**
	 * Gets the description.
	 * @return Returns a String
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Gets the displayName.
	 * @return Returns a String
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * getElementName method
	 * @return java.lang.String
	 */
	public String getElementName() {
		return getNodeName();
	}
	/**
	 * @return Returns the example.
	 */
	public String getExample() {
		return fExample;
	}
	/**
	 * @return Returns the extensions.
	 */
	public List getExtensions() {
		return fTagExtensions;
	}

	/**
	 * 
	 * @return java.lang.String
	 */
	public String getInfo() {
		return getDescription();
	}

	/**
	 * Gets the largeIcon.
	 * @return Returns a String
	 */
	public String getLargeIcon() {
		return largeIcon;
	}

	/**
	 * getLocalElements method
	 * @return CMNamedNodeMap
	 *
	 * Returns a list of locally defined elements.
	 */
	public CMNamedNodeMap getLocalElements() {
		return null;
	}

	/**
	 * getMaxOccur method
	 * @return int
	 *
	 * If -1, it's UNBOUNDED.
	 */
	public int getMaxOccur() {
		return maxOccur;
	}

	/**
	 * getMinOccur method
	 * @return int
	 *
	 * If 0, it's OPTIONAL.
	 * If 1, it's REQUIRED.
	 */
	public int getMinOccur() {
		return minOccur;
	}

	/**
	 * getNodeName method
	 * @return java.lang.String
	 */
	public String getNodeName() {
		return nodeName;
	}

	/**
	 * getNodeType method
	 * @return int
	 *
	 * Returns one of :
	 *
	 */
	public int getNodeType() {
		return CMNode.ELEMENT_DECLARATION;
	}

	/**
	 * @return
	 */
	public CMDocument getOwnerDocument() {
		return fOwnerDocument;
	}
	/**
	 * @return Returns the path.
	 */
	public String getPath() {
		return fPath;
	}

	/**
	 * getProperty method
	 * @return java.lang.Object
	 *
	 * Returns the object property described by the propertyName
	 *
	 */
	public Object getProperty(String propertyName) {
		if (propertyName != null && propertyName.equals("tagInfo")) { //$NON-NLS-1$
			return StringUtils.restoreMarkers(getTagInfo());	// return tag info
		}
		else if (propertyName != null && propertyName.equals("description")) {	//$NON-NLS-1$
			return StringUtils.restoreMarkers(getDescription()); // return tag description
		}
		else if (propertyName.equals(TLDDocument.CM_KIND)) {
			return TLDDocument.JSP_TLD;
		}
		else if (propertyName.equals(JSP12TLDNames.SMALL_ICON) || propertyName.equals(JSP12TLDNames.LARGE_ICON)) {
			return getOwnerDocument().getProperty(propertyName);
		}
		return null;
	}

	/**
	 * Gets the smallIcon.
	 * @return Returns a String
	 */
	public String getSmallIcon() {
		return smallIcon;
	}
	
	/**
	 * Returns the XPath of this element
	 * (currently just returns the node name)
	 * @return
	 */
	private String getSpec() {
		return getNodeName();
	}

	/**
	 * 
	 * @return java.lang.String
	 */
	public String getTagclass() {
		return tagclass;
	}
	
	/**
	 * Get the taginfo for this current element
	 * @return String taginfo if it exists, null otherwise
	 */
	private String getTagInfo() {
		if (getOwnerDocument().supports("annotationMap")) { //$NON-NLS-1$
			AnnotationMap map = (AnnotationMap)getOwnerDocument().getProperty("annotationMap"); //$NON-NLS-1$
			String spec = getSpec();
			String result = map.getProperty(spec, "tagInfo"); //$NON-NLS-1$
			return StringUtils.restoreMarkers(result); // return tag info
		}
		return null;
	}

	/**
	 * 
	 * @return java.lang.String
	 */
	public String getTeiclass() {
		return teiclass;
	}

	/**
	 * Gets the variables.
	 * @return Returns a List
	 */
	public List getVariables() {
		if (variables == null)
			variables = new ArrayList();
		return variables;
	}

	/**
	 * 
	 * @param newBodycontent java.lang.String
	 */
	public void setBodycontent(String newBodycontent) {
		bodycontent = newBodycontent;
	}

	/**
	 * Sets the description.
	 * @param description The description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Sets the displayName.
	 * @param displayName The displayName to set
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	/**
	 * @param example The example to set.
	 */
	public void setExample(String example) {
		fExample = example;
	}

	/**
	 * Sets the largeIcon.
	 * @param largeIcon The largeIcon to set
	 */
	public void setLargeIcon(String largeIcon) {
		this.largeIcon = largeIcon;
	}

	public void setNodeName(String string) {
		nodeName = string;
	}
	/**
	 * @param path The path to set.
	 */
	public void setPath(String path) {
		fPath = path;
	}

	/**
	 * Sets the smallIcon.
	 * @param smallIcon The smallIcon to set
	 */
	public void setSmallIcon(String smallIcon) {
		this.smallIcon = smallIcon;
	}

	/**
	 * 
	 * @param newTagclass java.lang.String
	 */
	public void setTagclass(String newTagclass) {
		tagclass = newTagclass;
	}

	/**
	 * 
	 * @param newTeiclass java.lang.String
	 */
	public void setTeiclass(String newTeiclass) {
		teiclass = newTeiclass;
	}

	/**
	 * Sets the variables.
	 * @param variables The variables to set
	 */
	public void setVariables(List variables) {
		this.variables = variables;
	}

	/**
	 * supports method
	 * @return boolean
	 *
	 * Returns true if the CMNode supports a specified property
	 *
	 */
	public boolean supports(String propertyName) {
		return false;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("\n\t " + super.toString()); //$NON-NLS-1$
		buffer.append("\n\t name:" + StringUtils.escape(getNodeName())); //$NON-NLS-1$
		buffer.append("\n\t tag class:" + StringUtils.escape(getTagclass())); //$NON-NLS-1$
		buffer.append("\n\t tei class:" + StringUtils.escape(getTeiclass())); //$NON-NLS-1$
		buffer.append("\n\t body content:" + StringUtils.escape(getBodycontent())); //$NON-NLS-1$
		buffer.append("\n\t description (info):" + StringUtils.escape(getDescription())); //$NON-NLS-1$
		buffer.append("\n\t attributes:"); //$NON-NLS-1$
		CMNamedNodeMap attributes = getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			buffer.append("\n\t\t" + StringUtils.replace(attributes.item(i).toString(), "\n", "\n\t\t")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		buffer.append("\n\t variables:"); //$NON-NLS-1$
		for (int i = 0; i < getVariables().size(); i++) {
			buffer.append("\n\t\t" + StringUtils.replace(getVariables().get(i).toString(), "\n", "\n\t\t")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		return buffer.toString();
	}
}