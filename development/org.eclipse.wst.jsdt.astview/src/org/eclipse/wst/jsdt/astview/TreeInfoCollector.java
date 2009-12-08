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
package org.eclipse.wst.jsdt.astview;

import org.eclipse.wst.jsdt.core.dom.ASTNode;
import org.eclipse.wst.jsdt.core.dom.JavaScriptUnit;

/**
 *
 */
public class TreeInfoCollector {
	
	public static class NodeCounter extends GenericVisitor {

		public int numberOfNodes= 0;
		
		protected boolean visitNode(ASTNode node) {
			numberOfNodes++;
			return true;
		}
	}
 	
	
	private final JavaScriptUnit fRoot;

	public TreeInfoCollector(JavaScriptUnit root) {
		fRoot= root;
	}

	public int getSize() {
		return fRoot.subtreeBytes();
	}
	
	public int getNumberOfNodes() {
		NodeCounter counter= new NodeCounter();
		fRoot.accept(counter);
		return counter.numberOfNodes;
	}
	

}
