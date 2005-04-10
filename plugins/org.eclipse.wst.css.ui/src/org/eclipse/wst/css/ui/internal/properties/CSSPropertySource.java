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
package org.eclipse.wst.css.ui.internal.properties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.wst.css.core.internal.metamodel.CSSMMCategory;
import org.eclipse.wst.css.core.internal.metamodel.CSSMMNode;
import org.eclipse.wst.css.core.internal.metamodel.CSSMetaModel;
import org.eclipse.wst.css.core.internal.metamodel.util.CSSMetaModelFinder;
import org.eclipse.wst.css.core.internal.metamodel.util.CSSMetaModelUtil;
import org.eclipse.wst.css.core.internal.provisional.document.ICSSNode;
import org.eclipse.wst.css.core.internal.provisional.document.ICSSNodeList;
import org.eclipse.wst.css.core.internal.provisional.document.ICSSStyleDeclItem;
import org.eclipse.wst.css.ui.internal.CSSUIMessages;
import org.eclipse.wst.sse.core.INodeAdapter;
import org.eclipse.wst.sse.core.INodeNotifier;
import org.w3c.dom.css.CSSStyleDeclaration;

/**
 * A IPropertySource implementation for a JFace viewer used to display
 * propreties of DOM nodes. This takes an adapter factory to create JFace
 * adapters for the nodes in the tree.
 */
public class CSSPropertySource implements INodeAdapter, IPropertySource {
	protected ICSSNode fNode = null;
	// for performance...
	final static Class ADAPTER_KEY = IPropertySource.class;

	/**
	 * DOMPropertySource constructor comment.
	 */
	public CSSPropertySource(INodeNotifier target) {
		super();
		fNode = (ICSSNode) target;
	}

	protected IPropertyDescriptor createDefaultPropertyDescriptor(String attributeName) {
		// the displayName MUST be set
		IPropertyDescriptor descriptor = new CSSTextPropertyDescriptor(attributeName, attributeName, fNode);
		//	IPropertyDescriptor descriptor = new
		// TextPropertyDescriptor(attributeName, attributeName);
		return descriptor;
	}

	protected IPropertyDescriptor createPropertyDescriptor(CSSMMNode node, String category) {
		return createPropertyDescriptor(node.getName(), category);
	}

	protected IPropertyDescriptor createPropertyDescriptor(String name, String category) {
		IPropertyDescriptor descriptor = null;
		if (name != null && 0 < name.length()) {
			name = name.toLowerCase();
			if (category == null) {
				category = CSSUIMessages.INFO_Not_Categorized_1; //$NON-NLS-1$
			}
			descriptor = new CSSTextPropertyDescriptor(name, name, fNode, category);
			//			if (category == null) {
			//				descriptor = new CSSTextPropertyDescriptor(name, name, fNode);
			//			} else {
			//				descriptor = new CSSTextPropertyDescriptor(name, name, fNode,
			// category);
			//			}
		}
		return descriptor;
	}

	/**
	 * Returns a value for this object that can be editted in a property
	 * sheet.
	 * 
	 * @return a value that can be editted
	 */
	public Object getEditableValue() {
		return null;
	}

	/**
	 * Returns the current collection of property descriptors.
	 * 
	 * @return a vector containing all descriptors.
	 */
	public IPropertyDescriptor[] getPropertyDescriptors() {
		CSSMetaModel metamodel = CSSMetaModelFinder.getInstance().findMetaModelFor(fNode);
		Iterator iProperties = Collections.EMPTY_LIST.iterator();
		switch (fNode.getNodeType()) {
			case ICSSNode.STYLERULE_NODE :
			case ICSSNode.FONTFACERULE_NODE :
			case ICSSNode.PAGERULE_NODE :
			case ICSSNode.STYLEDECLARATION_NODE :
				CSSMMNode mmParent = new CSSMetaModelUtil(metamodel).getMetaModelNodeFor(fNode);
				if (mmParent != null) {
					iProperties = mmParent.getChildNodes();
				}
				break;
			case ICSSNode.STYLEDECLITEM_NODE :
				CSSMMNode mmNode = new CSSMetaModelUtil(metamodel).getMetaModelNodeFor(fNode);
				if (mmNode != null) {
					iProperties = Collections.singletonList(mmNode).iterator();
				}
				break;
			default :
				break;
		}

		// setup categories
		Map categories = new HashMap();
		Iterator iCategories = metamodel.getCategories();
		while (iCategories.hasNext()) {
			CSSMMCategory category = (CSSMMCategory) iCategories.next();
			categories.put(category.getName(), category.getCaption());
		}

		// collect property names
		Set declaredProperties = new HashSet();
		if (iProperties.hasNext()) {
			CSSStyleDeclaration declaration = getDeclarationNode();
			if (declaration != null) {
				ICSSNodeList nodeList = ((ICSSNode) declaration).getChildNodes();
				int nProps = (nodeList != null) ? nodeList.getLength() : 0;
				for (int i = 0; i < nProps; i++) {
					ICSSNode node = nodeList.item(i);
					if (node instanceof ICSSStyleDeclItem) {
						String name = ((ICSSStyleDeclItem) node).getPropertyName();
						if (name != null && 0 < name.length()) {
							declaredProperties.add(name.toLowerCase());
						}
					}
				}
			}
		}

		List descriptors = new ArrayList();

		// first: properties from content model
		while (iProperties.hasNext()) {
			CSSMMNode node = (CSSMMNode) iProperties.next();
			if (node.getType() == CSSMMNode.TYPE_PROPERTY || node.getType() == CSSMMNode.TYPE_DESCRIPTOR) {
				String category = (String) categories.get(node.getAttribute("category")); //$NON-NLS-1$
				String name = node.getName().toLowerCase();
				if (declaredProperties.contains(name)) {
					declaredProperties.remove(name);
				}
				IPropertyDescriptor descriptor = createPropertyDescriptor(name, category);
				if (descriptor != null) {
					descriptors.add(descriptor);
				}
			}
		}

		// second: existing properties but not in content model
		Iterator iRemains = declaredProperties.iterator();
		while (iRemains.hasNext()) {
			IPropertyDescriptor descriptor = createPropertyDescriptor((String) iRemains.next(), null);
			if (descriptor != null) {
				descriptors.add(descriptor);
			}
		}

		IPropertyDescriptor[] resultArray = new IPropertyDescriptor[descriptors.size()];
		return (IPropertyDescriptor[]) descriptors.toArray(resultArray);
	}

	/**
	 * Returns the current value for the named property.
	 * 
	 * @param name
	 *            the name of the property as named by its property descriptor
	 * @return the current value of the property
	 */
	public Object getPropertyValue(Object name) {
		if (name == null) {
			return ""; //$NON-NLS-1$
		}

		String valueString = null;
		String nameString = name.toString();

		CSSStyleDeclaration declaration = null;

		switch (fNode.getNodeType()) {
			case ICSSNode.STYLEDECLITEM_NODE :
				valueString = ((ICSSStyleDeclItem) fNode).getCSSValueText();
				break;
			case ICSSNode.STYLERULE_NODE :
			case ICSSNode.FONTFACERULE_NODE :
			case ICSSNode.PAGERULE_NODE :
				declaration = (CSSStyleDeclaration) fNode.getFirstChild();
				if (declaration != null) {
					valueString = declaration.getPropertyValue(nameString);
				}
				break;
			case ICSSNode.STYLEDECLARATION_NODE :
				valueString = ((CSSStyleDeclaration) fNode).getPropertyValue(nameString);
				break;
			case ICSSNode.PRIMITIVEVALUE_NODE :
				ICSSNode parent = fNode;
				while (parent != null && !(parent instanceof ICSSStyleDeclItem)) {
					parent = parent.getParentNode();
				}
				if (parent != null) {
					valueString = ((ICSSStyleDeclItem) parent).getCSSValueText();
				}
				break;
			default :
				break;
		}

		if (valueString == null) {
			valueString = ""; //$NON-NLS-1$
		}

		return valueString;
	}

	/**
	 * Allowing the INodeAdapter to compare itself against the type allows it
	 * to return true in more than one case.
	 */
	public boolean isAdapterForType(java.lang.Object type) {
		return type.equals(ADAPTER_KEY);
	}

	/**
	 * Returns whether the property value has changed from the default.
	 * 
	 * @return <code>true</code> if the value of the specified property has
	 *         changed from its original default value; <code>false</code>
	 *         otherwise.
	 */
	public boolean isPropertySet(Object property) {
		if (property == null) {
			return false;
		}
		CSSStyleDeclaration declaration = getDeclarationNode();
		if (declaration != null) {
			String value = declaration.getPropertyValue(property.toString());
			if (value != null && 0 < value.length()) {
				return true;
			}
		}

		return false;
	}

	/**
	 */
	public void notifyChanged(INodeNotifier notifier, int eventType, java.lang.Object changedFeature, java.lang.Object oldValue, java.lang.Object newValue, int pos) {
	}

	/**
	 * Resets the specified property's value to its default value.
	 * 
	 * @param property
	 *            the property to reset
	 */
	public void resetPropertyValue(Object str) {
		if (str == null) {
			return;
		}
		CSSStyleDeclaration declaration = getDeclarationNode();
		if (declaration != null) {
			declaration.removeProperty(str.toString());
		}
	}

	/**
	 * Sets the named property to the given value.
	 * 
	 * @param name
	 *            the name of the property being set
	 * @param value
	 *            the new value for the property
	 */
	public void setPropertyValue(Object name, Object value) {
		if (name == null) {
			return;
		}
		String valueString = (value != null) ? value.toString() : null;
		String nameString = name.toString();
		CSSStyleDeclaration declaration = getDeclarationNode();
		if (declaration != null) {
			try {
				if (valueString == null || valueString.length() <= 0) {
					declaration.removeProperty(nameString);
				} else {
					declaration.setProperty(nameString, valueString, ""); //$NON-NLS-1$
				}
			} catch (Exception e) {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				String title = CSSUIMessages.Title_InvalidValue; //$NON-NLS-1$
				String message = CSSUIMessages.Message_InvalidValue; //$NON-NLS-1$
				MessageDialog.openWarning(window.getShell(), title, message);
			}
		}
	}

	private CSSStyleDeclaration getDeclarationNode() {
		CSSStyleDeclaration declaration = null;

		switch (fNode.getNodeType()) {
			case ICSSNode.STYLEDECLITEM_NODE :
				declaration = (CSSStyleDeclaration) fNode.getParentNode();
				break;
			case ICSSNode.STYLERULE_NODE :
			case ICSSNode.FONTFACERULE_NODE :
			case ICSSNode.PAGERULE_NODE :
				declaration = (CSSStyleDeclaration) fNode.getFirstChild();
				break;
			case ICSSNode.STYLEDECLARATION_NODE :
				declaration = (CSSStyleDeclaration) fNode;
				break;
			case ICSSNode.PRIMITIVEVALUE_NODE :
				ICSSNode parent = fNode;
				while (parent != null && !(parent instanceof CSSStyleDeclaration)) {
					parent = parent.getParentNode();
				}
				if (parent != null) {
					declaration = (CSSStyleDeclaration) parent;
				}
				break;
			default :
				break;
		}

		return declaration;
	}
}