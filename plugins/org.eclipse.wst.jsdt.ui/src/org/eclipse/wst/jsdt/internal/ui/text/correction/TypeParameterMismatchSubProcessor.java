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
package org.eclipse.wst.jsdt.internal.ui.text.correction;

import java.util.Collection;

import org.eclipse.wst.jsdt.core.dom.ASTNode;
import org.eclipse.wst.jsdt.core.dom.CompilationUnit;
import org.eclipse.wst.jsdt.core.dom.ParameterizedType;
import org.eclipse.wst.jsdt.core.dom.SimpleName;

import org.eclipse.wst.jsdt.internal.corext.dom.ASTNodes;

import org.eclipse.wst.jsdt.ui.text.java.IInvocationContext;
import org.eclipse.wst.jsdt.ui.text.java.IProblemLocation;


public class TypeParameterMismatchSubProcessor {

	public static void getTypeParameterMismatchProposals(IInvocationContext context, IProblemLocation problem, Collection proposals) {
		CompilationUnit astRoot= context.getASTRoot();
		ASTNode selectedNode= problem.getCoveredNode(astRoot);
		if (!(selectedNode instanceof SimpleName)) {
			return;
		}

		ASTNode normalizedNode= ASTNodes.getNormalizedNode(selectedNode);
		if (!(normalizedNode instanceof ParameterizedType)) {
			return;
		}
		// waiting for result of https://bugs.eclipse.org/bugs/show_bug.cgi?id=81544


	}
}
