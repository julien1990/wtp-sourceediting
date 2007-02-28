/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.jsdt.internal.ui.wizards.buildpaths;

import org.eclipse.core.runtime.Assert;

import org.eclipse.wst.jsdt.core.IClasspathAttribute;
import org.eclipse.wst.jsdt.core.IClasspathEntry;
import org.eclipse.wst.jsdt.core.IJavaProject;
import org.eclipse.wst.jsdt.core.JavaCore;

import org.eclipse.wst.jsdt.ui.wizards.ClasspathAttributeConfiguration.ClasspathAttributeAccess;


/**
  */
public class CPListElementAttribute {

	private CPListElement fParent;
	private String fKey;
	private Object fValue;
	private final boolean fBuiltIn;
	
	private ClasspathAttributeAccess fCachedAccess;
	
	public CPListElementAttribute(CPListElement parent, String key, Object value, boolean builtIn) {
		fKey= key;
		fValue= value;
		fParent= parent;
		fBuiltIn= builtIn;
		if (!builtIn) {
			Assert.isTrue(value instanceof String || value == null);
		}	
	}
	
    private CPListElementAttribute(boolean buildIn) {
    	fBuiltIn= buildIn;
    }

	public IClasspathAttribute getClasspathAttribute() {
		Assert.isTrue(!fBuiltIn);
		return JavaCore.newClasspathAttribute(fKey, (String) fValue);
	}
	
	public CPListElement getParent() {
		return fParent;
	}
	
	/**
	 * @return Returns <code>true</code> if the attribute is a built in attribute.
	 */
	public boolean isBuiltIn() {
		return fBuiltIn;
	}
	
	/**
	 * @return Returns <code>true</code> if the attribute is in a non-modifiable classpath container
	 */
	public boolean isInNonModifiableContainer() {
		return fParent.isInNonModifiableContainer();
	}

	/**
	 * Returns the key.
	 * @return String
	 */
	public String getKey() {
		return fKey;
	}

	/**
	 * Returns the value.
	 * @return Object
	 */
	public Object getValue() {
		return fValue;
	}
	
	/**
	 * Returns the value.
	 */
	public void setValue(Object value) {
		fValue= value;
		fCachedAccess= null;
		getParent().attributeChanged(fKey);
	}
	
    public boolean equals(Object obj) {
        if (!(obj instanceof CPListElementAttribute))
            return false;
        CPListElementAttribute attrib= (CPListElementAttribute)obj;
        return attrib.fKey== this.fKey && attrib.getParent().getPath().equals(fParent.getPath());
    }

    public CPListElementAttribute copy() {
    	CPListElementAttribute result= new CPListElementAttribute(fBuiltIn);
    	result.fParent= fParent;
    	result.fKey= fKey;
    	result.fValue= fValue;
	    return result;
    }
    
    public ClasspathAttributeAccess getClasspathAttributeAccess() {
    	if (fCachedAccess == null) {
	    	fCachedAccess= new ClasspathAttributeAccess() {
	    		public IClasspathAttribute getClasspathAttribute() {
	 				return CPListElementAttribute.this.getClasspathAttribute();
				}
				public IJavaProject getJavaProject() {
					return getParent().getJavaProject();
				}
				public IClasspathEntry getParentClasspassEntry() {
					return getParent().getClasspathEntry();
				}
	    	};
    	}
    	return fCachedAccess;
    }
    
    
}
