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
package org.eclipse.wst.jsdt.internal.corext.refactoring.rename;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.wst.jsdt.core.ICompilationUnit;
import org.eclipse.wst.jsdt.core.formatter.IndentManipulation;
import org.eclipse.wst.jsdt.core.search.MethodReferenceMatch;
import org.eclipse.wst.jsdt.core.search.SearchMatch;

import org.eclipse.wst.jsdt.internal.corext.refactoring.CollectingSearchRequestor;
import org.eclipse.wst.jsdt.internal.corext.util.SearchUtils;

class MethodOccurenceCollector extends CollectingSearchRequestor {

		private final int fNameLength;

		public MethodOccurenceCollector(String methodName) {
			fNameLength= methodName.length();
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.wst.jsdt.internal.corext.refactoring.CollectingSearchRequestor#acceptSearchMatch(org.eclipse.wst.jsdt.core.search.SearchMatch)
		 */
		public void acceptSearchMatch(SearchMatch match) throws CoreException {
			ICompilationUnit unit= SearchUtils.getCompilationUnit(match);
			if (unit == null)
				return;
			
			if (match instanceof MethodReferenceMatch
					&& ((MethodReferenceMatch) match).isSuperInvocation()
					&& match.getAccuracy() == SearchMatch.A_INACCURATE) {
				return; // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=156491
			}
			
			int start= match.getOffset();
			int length= match.getLength();
			if (match.isImplicit()) { // see bug 94062
				super.acceptSearchMatch(match);
				return;
			}
			
			String matchText= unit.getBuffer().getText(start, length);
			//TODO: use Scanner
			int leftBracketIndex= matchText.indexOf("("); //$NON-NLS-1$
			if (leftBracketIndex != -1) {
				// reference in code includes arguments; reference in javadoc doesn't; constructors ?
				matchText= matchText.substring(0, leftBracketIndex);
			}
		
			int theDotIndex= matchText.lastIndexOf("."); //$NON-NLS-1$
			if (theDotIndex == -1) {
				match.setLength(fNameLength);
				super.acceptSearchMatch(match);
			} else {
				start= start + theDotIndex + 1;
				for (int i= theDotIndex + 1; i < matchText.length() && IndentManipulation.isIndentChar(matchText.charAt(i)); i++) {
					start++;
				}
				match.setOffset(start);
				match.setLength(fNameLength);
				super.acceptSearchMatch(match);
			}
		}	
	}
