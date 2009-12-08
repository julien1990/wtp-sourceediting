/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.wst.jsdt.astview.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.wst.jsdt.core.dom.ASTNode;
import org.eclipse.wst.jsdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.wst.jsdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.wst.jsdt.core.dom.ClassInstanceCreation;
import org.eclipse.wst.jsdt.core.dom.ConstructorInvocation;
import org.eclipse.wst.jsdt.core.dom.Expression;
import org.eclipse.wst.jsdt.core.dom.FieldAccess;
import org.eclipse.wst.jsdt.core.dom.FunctionDeclaration;
import org.eclipse.wst.jsdt.core.dom.FunctionInvocation;
import org.eclipse.wst.jsdt.core.dom.FunctionRef;
import org.eclipse.wst.jsdt.core.dom.IBinding;
import org.eclipse.wst.jsdt.core.dom.IFunctionBinding;
import org.eclipse.wst.jsdt.core.dom.ITypeBinding;
import org.eclipse.wst.jsdt.core.dom.IVariableBinding;
import org.eclipse.wst.jsdt.core.dom.ImportDeclaration;
import org.eclipse.wst.jsdt.core.dom.JavaScriptUnit;
import org.eclipse.wst.jsdt.core.dom.MemberRef;
import org.eclipse.wst.jsdt.core.dom.Name;
import org.eclipse.wst.jsdt.core.dom.PackageDeclaration;
import org.eclipse.wst.jsdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.wst.jsdt.core.dom.SuperConstructorInvocation;
import org.eclipse.wst.jsdt.core.dom.SuperFieldAccess;
import org.eclipse.wst.jsdt.core.dom.SuperMethodInvocation;
import org.eclipse.wst.jsdt.core.dom.Type;
import org.eclipse.wst.jsdt.core.dom.VariableDeclaration;

public class ASTViewContentProvider implements ITreeContentProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
	}
	
	/*(non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object parent) {
		return getChildren(parent);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object child) {
		if (child instanceof ASTNode) {
			ASTNode node= (ASTNode) child;
			ASTNode parent= node.getParent();
			if (parent != null) {
				StructuralPropertyDescriptor prop= node.getLocationInParent();
				return new NodeProperty(parent, prop);
			}
		} else if (child instanceof ASTAttribute) {
			return ((ASTAttribute) child).getParent();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parent) {
		if (parent instanceof ASTAttribute) {
			return ((ASTAttribute) parent).getChildren();
		} else if (parent instanceof ASTNode) {
			return getNodeChildren((ASTNode) parent);
		}
		return new Object[0];
	}
	
	private Object[] getNodeChildren(ASTNode node) {
		ArrayList res= new ArrayList();

		if (node instanceof Expression) {
			Expression expression= (Expression) node;
			ITypeBinding expressionTypeBinding= expression.resolveTypeBinding();
			res.add(createExpressionTypeBinding(node, expressionTypeBinding));
			
			// expressions:
			if (expression instanceof Name) {
				IBinding binding= ((Name) expression).resolveBinding();
				if (binding != expressionTypeBinding)
					res.add(createBinding(expression, binding));
			} else if (expression instanceof FunctionInvocation) {
				FunctionInvocation methodInvocation= (FunctionInvocation) expression;
				IFunctionBinding binding= methodInvocation.resolveMethodBinding();
				res.add(createBinding(expression, binding));
				String inferred= String.valueOf(methodInvocation.isResolvedTypeInferredFromExpectedType());
				res.add(new GeneralAttribute(expression, "ResolvedTypeInferredFromExpectedType", inferred)); //$NON-NLS-1$
			} else if (expression instanceof SuperMethodInvocation) {
				SuperMethodInvocation superMethodInvocation= (SuperMethodInvocation) expression;
				IFunctionBinding binding= superMethodInvocation.resolveMethodBinding();
				res.add(createBinding(expression, binding));
				String inferred= String.valueOf(superMethodInvocation.isResolvedTypeInferredFromExpectedType());
				res.add(new GeneralAttribute(expression, "ResolvedTypeInferredFromExpectedType", inferred)); //$NON-NLS-1$
			} else if (expression instanceof ClassInstanceCreation) {
				IFunctionBinding binding= ((ClassInstanceCreation) expression).resolveConstructorBinding();
				res.add(createBinding(expression, binding));
			} else if (expression instanceof FieldAccess) {
				IVariableBinding binding= ((FieldAccess) expression).resolveFieldBinding();
				res.add(createBinding(expression, binding));
			} else if (expression instanceof SuperFieldAccess) {
				IVariableBinding binding= ((SuperFieldAccess) expression).resolveFieldBinding();
				res.add(createBinding(expression, binding));
//			// Expression attributes:
//			res.add(new GeneralAttribute(expression, "Boxing: " + expression.resolveBoxing() + "; Unboxing: " + expression.resolveUnboxing())); //$NON-NLS-1$ //$NON-NLS-2$
//			res.add(new GeneralAttribute(expression, "ConstantExpressionValue", expression.resolveConstantExpressionValue())); //$NON-NLS-1$
			}
		// references:
		} else if (node instanceof ConstructorInvocation) {
			IFunctionBinding binding= ((ConstructorInvocation) node).resolveConstructorBinding();
			res.add(createBinding(node, binding));
		} else if (node instanceof SuperConstructorInvocation) {
			IFunctionBinding binding= ((SuperConstructorInvocation) node).resolveConstructorBinding();
			res.add(createBinding(node, binding));
		} else if (node instanceof FunctionRef) {
			IBinding binding= ((FunctionRef) node).resolveBinding();
			res.add(createBinding(node, binding));
		} else if (node instanceof MemberRef) {
			IBinding binding= ((MemberRef) node).resolveBinding();
			res.add(createBinding(node, binding));
		} else if (node instanceof Type) {
			IBinding binding= ((Type) node).resolveBinding();
			res.add(createBinding(node, binding));
			
		// declarations:
		} else if (node instanceof AbstractTypeDeclaration) {
			IBinding binding= ((AbstractTypeDeclaration) node).resolveBinding();
			res.add(createBinding(node, binding));
		} else if (node instanceof FunctionDeclaration) {
			IBinding binding= ((FunctionDeclaration) node).resolveBinding();
			res.add(createBinding(node, binding));
		} else if (node instanceof VariableDeclaration) {
			IBinding binding= ((VariableDeclaration) node).resolveBinding();
			res.add(createBinding(node, binding));
		} else if (node instanceof AnonymousClassDeclaration) {
			IBinding binding= ((AnonymousClassDeclaration) node).resolveBinding();
			res.add(createBinding(node, binding));
		} else if (node instanceof ImportDeclaration) {
			IBinding binding= ((ImportDeclaration) node).resolveBinding();
			res.add(createBinding(node, binding));
		} else if (node instanceof PackageDeclaration) {
			IBinding binding= ((PackageDeclaration) node).resolveBinding();
			res.add(createBinding(node, binding));
		}
 		
		
		
		List list= node.structuralPropertiesForType();
		for (int i= 0; i < list.size(); i++) {
			StructuralPropertyDescriptor curr= (StructuralPropertyDescriptor) list.get(i);
			res.add(new NodeProperty(node, curr));
		}
		
		if (node instanceof JavaScriptUnit) {
			JavaScriptUnit root= (JavaScriptUnit) node;
			res.add(new JavaElement(root, root.getJavaElement()));
			res.add(new CommentsProperty(root));
			res.add(new ProblemsProperty(root));
			res.add(new SettingsProperty(root));
			res.add(new WellKnownTypesProperty(root));
		}
		
		return res.toArray();
	}
	
	private Binding createBinding(ASTNode parent, IBinding binding) {
		String label= Binding.getBindingLabel(binding);
		return new Binding(parent, label, binding, true);
	}

	private Object createExpressionTypeBinding(ASTNode parent, ITypeBinding binding) {
		String label= "> (Expression) type binding"; //$NON-NLS-1$
		return new Binding(parent, label, binding, true);
	}
	
	public boolean hasChildren(Object parent) {
		return getChildren(parent).length > 0;
	}
}
