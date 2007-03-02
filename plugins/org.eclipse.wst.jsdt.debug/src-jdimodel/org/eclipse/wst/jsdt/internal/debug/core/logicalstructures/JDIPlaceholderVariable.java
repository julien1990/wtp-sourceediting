/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.jsdt.internal.debug.core.logicalstructures;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.wst.jsdt.debug.core.IJavaModifiers;
import org.eclipse.wst.jsdt.debug.core.IJavaType;
import org.eclipse.wst.jsdt.debug.core.IJavaValue;
import org.eclipse.wst.jsdt.debug.core.IJavaVariable;

/**
 * Encapsulates a name and a value.  Used when a variable is required (such as for
 * the VariablesView), but we only have a value available (such as the result of 
 * an evaluation for an object browser).
 * 
 * @since 3.0
 */
public class JDIPlaceholderVariable extends PlatformObject implements IJavaVariable  {

	private String fName;
	private IJavaValue fValue;

	public JDIPlaceholderVariable(String name, IJavaValue value) {
		fName = name;
		fValue = value;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.debug.core.IJavaVariable#getSignature()
	 */
	public String getSignature() throws DebugException {
		return ((IJavaValue)getValue()).getSignature();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.debug.core.IJavaVariable#getGenericSignature()
	 */
	public String getGenericSignature() throws DebugException {
		return ((IJavaValue)getValue()).getGenericSignature();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.debug.core.IJavaVariable#getJavaType()
	 */
	public IJavaType getJavaType() throws DebugException {
		return ((IJavaValue)getValue()).getJavaType();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.debug.core.IJavaVariable#isLocal()
	 */
	public boolean isLocal() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IVariable#getValue()
	 */
	public IValue getValue() {
		return fValue;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IVariable#getName()
	 */
	public String getName() {
		return fName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IVariable#getReferenceTypeName()
	 */
	public String getReferenceTypeName() throws DebugException {
		return ((IJavaValue)getValue()).getReferenceTypeName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IVariable#hasValueChanged()
	 */
	public boolean hasValueChanged() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.debug.core.IJavaModifiers#isPublic()
	 */
	public boolean isPublic() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.debug.core.IJavaModifiers#isPrivate()
	 */
	public boolean isPrivate() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.debug.core.IJavaModifiers#isProtected()
	 */
	public boolean isProtected() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.debug.core.IJavaModifiers#isPackagePrivate()
	 */
	public boolean isPackagePrivate() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.debug.core.IJavaModifiers#isFinal()
	 */
	public boolean isFinal() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.debug.core.IJavaModifiers#isStatic()
	 */
	public boolean isStatic() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.debug.core.IJavaModifiers#isSynthetic()
	 */
	public boolean isSynthetic() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugElement#getModelIdentifier()
	 */
	public String getModelIdentifier() {
		return getValue().getModelIdentifier();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugElement#getDebugTarget()
	 */
	public IDebugTarget getDebugTarget() {
		return ((IJavaValue)getValue()).getDebugTarget();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugElement#getLaunch()
	 */
	public ILaunch getLaunch() {
		return getValue().getLaunch();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#setValue(java.lang.String)
	 */
	public void setValue(String expression) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#setValue(org.eclipse.debug.core.model.IValue)
	 */
	public void setValue(IValue value) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#supportsValueModification()
	 */
	public boolean supportsValueModification() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#verifyValue(java.lang.String)
	 */
	public boolean verifyValue(String expression) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#verifyValue(org.eclipse.debug.core.model.IValue)
	 */
	public boolean verifyValue(IValue value) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (IJavaVariable.class.equals(adapter) ||
			IJavaModifiers.class.equals(adapter)) {
			return this;
		}
		return super.getAdapter(adapter);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof JDIPlaceholderVariable) {
			JDIPlaceholderVariable var = (JDIPlaceholderVariable)obj;
			return var.getName().equals(getName()) && var.getValue().equals(getValue());
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return fName.hashCode() + fValue.hashCode();
	}

}
