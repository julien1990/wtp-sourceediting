/*******************************************************************************
 * Copyright (c) 2005 BEA Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BEA Systems - initial implementation
 *     
 *******************************************************************************/

package org.eclipse.jst.jsp.core.internal.java.jspel;

import java.util.Map;
import org.eclipse.jst.jsp.core.internal.java.JSPTranslator;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionCollection;

/**
 * The code checker for the jsf/jsp EL.
 */
public final class ELGenerator {
	/**
     * Constructor.
     *
     * @param locator A valid ELLocator instance, may not be null.
     */
    public ELGenerator() {
    } // constructor
	

    /**
     * Check the netuiel AST and set diagnostics as necessary.
     * @param translator TODO
     * @param jspReferenceRegion TODO
     * @param contentStart 
     * @param contentLength 
     */
    public void generate(ASTExpression root, StringBuffer result, Map codeMap, JSPTranslator translator, ITextRegionCollection jspReferenceRegion, int contentStart, int contentLength) {
		ELGeneratorVisitor visitor = new ELGeneratorVisitor(result, codeMap, translator, jspReferenceRegion, contentStart);
		root.jjtAccept(visitor, null);
    }
}
