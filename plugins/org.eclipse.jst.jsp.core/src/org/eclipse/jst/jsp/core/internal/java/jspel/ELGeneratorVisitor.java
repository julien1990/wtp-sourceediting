/*******************************************************************************
 * Copyright (c) 2005, 2006 BEA Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BEA Systems - initial implementation
 *     
 *     Bug 154474 EL: 'and', 'or', ... operator
 *     https://bugs.eclipse.org/bugs/show_bug.cgi?id=154474
 *     Bernhard Huemer <bernhard.huemer@gmail.com>
 *     
 *******************************************************************************/
package org.eclipse.jst.jsp.core.internal.java.jspel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.Position;
import org.eclipse.jst.jsp.core.internal.contentmodel.TaglibController;
import org.eclipse.jst.jsp.core.internal.contentmodel.tld.CMDocumentImpl;
import org.eclipse.jst.jsp.core.internal.contentmodel.tld.TLDCMDocumentManager;
import org.eclipse.jst.jsp.core.internal.contentmodel.tld.TaglibTracker;
import org.eclipse.jst.jsp.core.internal.contentmodel.tld.provisional.TLDFunction;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionCollection;

public class ELGeneratorVisitor implements JSPELParserVisitor {
	
	private static final String ENDL = "\n"; //$NON-NLS-1$
	
	private static final String fExpressionHeader1 = "public String _elExpression"; //$NON-NLS-1$
	private static final String fExpressionHeader2 = "()" + ENDL + //$NON-NLS-1$
	"\t\tthrows java.io.IOException, javax.servlet.ServletException {" + ENDL + //$NON-NLS-1$
	"javax.servlet.jsp.PageContext pageContext = null;" + ENDL + //$NON-NLS-1$
	"java.util.Map param = null;" + ENDL + //$NON-NLS-1$
	"java.util.Map paramValues = null;" + ENDL + //$NON-NLS-1$
	"java.util.Map header = null;" + ENDL + //$NON-NLS-1$ 
	"java.util.Map headerValues = null;" + ENDL + //$NON-NLS-1$
	"java.util.Map cookie = null;" + ENDL + //$NON-NLS-1$ 
	"java.util.Map initParam = null;" + ENDL + //$NON-NLS-1$
	"java.util.Map pageScope = null;" + ENDL + //$NON-NLS-1$
	"java.util.Map requestScope = null;" + ENDL + //$NON-NLS-1$
	"java.util.Map sessionScope = null;" + ENDL + //$NON-NLS-1$
	"java.util.Map applicationScope = null;" + ENDL + //$NON-NLS-1$
	"return \"\"+"; //$NON-NLS-1$

	private static final String fExpressionHeader2_param = "()" + ENDL + //$NON-NLS-1$
	"\t\tthrows java.io.IOException, javax.servlet.ServletException {" + ENDL + //$NON-NLS-1$
	"javax.servlet.jsp.PageContext pageContext = null;" + ENDL + //$NON-NLS-1$
	"java.util.Map<java.lang.String, java.lang.String> param = null;" + ENDL + //$NON-NLS-1$
	"java.util.Map<java.lang.String, java.lang.String[]> paramValues = null;" + ENDL + //$NON-NLS-1$
	"java.util.Map<java.lang.String, java.lang.String> header = null;" + ENDL + //$NON-NLS-1$ 
	"java.util.Map<java.lang.String, java.lang.String[]> headerValues = null;" + ENDL + //$NON-NLS-1$
	"java.util.Map<java.lang.String, javax.servlet.http.Cookie> cookie = null;" + ENDL + //$NON-NLS-1$ 
	"java.util.Map<java.lang.String, java.lang.String> initParam = null;" + ENDL + //$NON-NLS-1$
	"java.util.Map<java.lang.String, java.lang.Object> pageScope = null;" + ENDL + //$NON-NLS-1$
	"java.util.Map<java.lang.String, java.lang.Object> requestScope = null;" + ENDL + //$NON-NLS-1$
	"java.util.Map<java.lang.String, java.lang.Object> sessionScope = null;" + ENDL + //$NON-NLS-1$
	"java.util.Map<java.lang.String, java.lang.Object> applicationScope = null;" + ENDL + //$NON-NLS-1$
	"return \"\"+"; //$NON-NLS-1$
	
	private static final String fJspImplicitObjects[] = { "pageContext" }; //$NON-NLS-1$
	
	private static final String fJspImplicitMaps[] = { 	"param", "paramValues", "header", "headerValues", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
														"cookie", "initParam", "pageScope", "requestScope", "sessionScope",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
														"applicationScope" }; //$NON-NLS-1$
	
	private static final HashMap fJSPImplicitObjectMap = new HashMap(fJspImplicitObjects.length);
	static {
		for(int i = 0; i < fJspImplicitObjects.length; i++) {
			fJSPImplicitObjectMap.put(fJspImplicitObjects[i], new Boolean(true));
		}
		
		for(int i = 0; i < fJspImplicitMaps.length; i++) {
			fJSPImplicitObjectMap.put(fJspImplicitMaps[i], new Boolean(false));
		}
	}
	
	private static final String fFooter = " ;" + ENDL + "}" + ENDL; //$NON-NLS-1$ //$NON-NLS-2$

	private StringBuffer fResult;
	private Map fCodeMap;
	private int fOffsetInUserCode;
	private static int methodCounter = 0;
	private IStructuredDocument fDocument = null;
	private int fContentStart;
	private static Map fOperatorMap;
	
	// this flag lets us know if we were unable to generate for some reason.  One possible reason is that the expression 
	// contains a reference to a variable for which information is only available at runtime.
	private boolean fCanGenerate = true;

	private IStructuredDocumentRegion fCurrentNode;

	private boolean fUseParameterizedTypes;

	/**
	 * Tranlsation of XML-style operators to java
	 */
	static {
		fOperatorMap = new HashMap();
		fOperatorMap.put("gt", ">"); //$NON-NLS-1$ //$NON-NLS-2$
		fOperatorMap.put("lt", "<"); //$NON-NLS-1$ //$NON-NLS-2$
		fOperatorMap.put("ge", ">="); //$NON-NLS-1$ //$NON-NLS-2$
		fOperatorMap.put("le", "<="); //$NON-NLS-1$ //$NON-NLS-2$
		fOperatorMap.put("mod", "%"); //$NON-NLS-1$ //$NON-NLS-2$
		fOperatorMap.put("eq", "=="); //$NON-NLS-1$ //$NON-NLS-2$
		fOperatorMap.put("and", "&&"); //$NON-NLS-1$ //$NON-NLS-2$
		fOperatorMap.put("or", "||"); //$NON-NLS-1$ //$NON-NLS-2$
		fOperatorMap.put("not", "!"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * The constructor squirrels away a few things we'll need later
	 * 
	 * @param result
	 * @param codeMap
	 * @param translator
	 * @param jspReferenceRegion
	 * @param contentStart
	 */
	public ELGeneratorVisitor(StringBuffer result, IStructuredDocumentRegion currentNode, Map codeMap, IStructuredDocument document, ITextRegionCollection jspReferenceRegion, int contentStart)
	{
		fResult = result;
		fCodeMap = codeMap;
		fOffsetInUserCode = result.length();
		fContentStart = contentStart;
		fDocument = document;
		fCurrentNode = currentNode;
		
		fUseParameterizedTypes = compilerSupportsParameterizedTypes();
	}

	/**
	 * Append a token to the output stream.  Automatically calculating mapping.
	 * 
	 * @param token
	 */
	private void append(Token token)
	{
		append(token.image, token.beginColumn - 1, token.endColumn);
	}
	
	/**
	 * Append a translation for the corresponding input token.
	 * 
	 * @param translated
	 * @param token
	 */
	private void append(String translated, Token token)
	{
		append(translated, token.beginColumn - 1, token.endColumn);
	}

	/**
	 * Append a string explicitly giving the input mapping.
	 * 
	 * @param newText
	 * @param jspPositionStart
	 * @param jspPositionEnd
	 */
	private void append(String newText, int jspPositionStart, int jspPositionEnd)
	{
		fResult.append(newText);
		Position javaRange = new Position(fOffsetInUserCode, newText.length());
		Position jspRange = new Position(fContentStart + jspPositionStart, jspPositionEnd - jspPositionStart);

		fCodeMap.put(javaRange, jspRange);
		fOffsetInUserCode += newText.length();
	}
	
	/**
	 * Append text that will be unmapped and therefore will not be available for completion.
	 * 
	 * @param newText
	 */
	private void append(String newText)
	{
		fResult.append(newText);
		fOffsetInUserCode += newText.length();
	}
	
	/**
	 * Generate a function invocation.
	 * 
	 * @param fullFunctionName
	 * @return
	 */
	protected String genFunction(String fullFunctionName) {
		TLDCMDocumentManager docMgr = TaglibController.getTLDCMDocumentManager(fDocument);
		int colonIndex = fullFunctionName.indexOf(':');
		String prefix = fullFunctionName.substring(0, colonIndex);
		String functionName = fullFunctionName.substring(colonIndex + 1);
		if (docMgr == null)
			return null;
		
		Iterator taglibs = docMgr.getCMDocumentTrackers(fCurrentNode.getStartOffset()).iterator();
		while (taglibs.hasNext()) {
			TaglibTracker tracker = (TaglibTracker)taglibs.next();
			if(tracker.getPrefix().equals(prefix)) {
				CMDocumentImpl doc = (CMDocumentImpl)tracker.getDocument();
				
				List functions = doc.getFunctions();
				for(Iterator it = functions.iterator(); it.hasNext(); ) {
					TLDFunction function = (TLDFunction)it.next();
					if(function.getName().equals(functionName)) {
						return function.getClassName() + "." + function.getName(); //$NON-NLS-1$
					}
				}
			}
		}
		return null;
	}

	/**
	 * Handle a simple node -- fallback
	 */
	public Object visit(SimpleNode node, Object data) {
		return(node.childrenAccept(this, data));
	}

	static synchronized int getMethodCounter() {
		return methodCounter++;
	}
	
	/**
	 * Handle top-level expression
	 */
	public Object visit(ASTExpression node, Object data) {
		int start = node.getFirstToken().beginColumn - 1;
		int end = node.lastToken.endColumn - 1;
		append(fExpressionHeader1, start, start);
		append(Integer.toString(getMethodCounter()), start, start);
		if (fUseParameterizedTypes)
			append(fExpressionHeader2_param, start, start);
		else
			append(fExpressionHeader2, start, start);
		
		Object retval = node.childrenAccept(this, data);

		append(fFooter, end, end);
		
		// something is preventing good code generation so empty out the result and the map.
		if(!fCanGenerate) {
			fResult.delete(0, fResult.length());
			fCodeMap.clear();			
		}
		return retval;
	}

	private boolean compilerSupportsParameterizedTypes() {
		if (fDocument != null) {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IPath location = TaglibController.getLocation(fDocument);
			if (location != null && location.segmentCount() > 0) {
				IJavaProject project = JavaCore.create(root.getProject(location.segment(0)));
				String compliance = project.getOption(JavaCore.COMPILER_SOURCE, true);
				try {
					return Float.parseFloat(compliance) >= 1.5;
				}
				catch (NumberFormatException e) {
					return false;
				}
			}
		}
		return false;
	}
	
	/**
	 * Generically generate an operator node.
	 * 
	 * @param node
	 * @param data
	 */
	private void generateOperatorNode(ASTOperatorExpression node, Object data) {
		for(int i = 0; i < node.children.length; i++) {
			node.children[i].jjtAccept(this, data);
			if( node.children.length - i > 1) {
				appendOperator((Token)node.getOperatorTokens().get(i));
			}
		}
	}
	
	/**
	 * Append an operator to the output stream after translation (if any)
	 * 
	 * @param token
	 * @return
	 */
	private String appendOperator(Token token) {
		String tokenImage = token.image.trim();
		String translated = (String)fOperatorMap.get(tokenImage);
		if(null != translated) {
			append(translated, token);
		} else {
			append(token);
		}
		return(translated);
	}

	/**
	 * Handle or Expression
	 */
	public Object visit(ASTOrExpression node, Object data) {
		generateOperatorNode(node, data);
		return(null);
	}


	/**
	 * Handle and expression 
	 */
	public Object visit(ASTAndExpression node, Object data) {
		generateOperatorNode(node, data);
		return(null);
	}


	/**
	 * Handle equality
	 */
	public Object visit(ASTEqualityExpression node, Object data) {
		generateOperatorNode(node, data);
		return(null);
	}


	/**
	 * Handle Relational
	 */
	public Object visit(ASTRelationalExpression node, Object data) {
		generateOperatorNode(node, data);
		return(null);
	}


	/**
	 * Handle addition
	 */
	public Object visit(ASTAddExpression node, Object data) {
		generateOperatorNode(node, data);
		return(null);
	}


	/**
	 * Handle multiply
	 */
	public Object visit(ASTMultiplyExpression node, Object data) {
		generateOperatorNode(node, data);
		return(null);
	}


	/** 
	 * Choice Expression (ternary operator)
	 */
	public Object visit(ASTChoiceExpression node, Object data) {
		node.children[0].jjtAccept(this, data);
		append("?"); //$NON-NLS-1$
		node.children[1].jjtAccept(this, data);
		append(":"); //$NON-NLS-1$
		node.children[2].jjtAccept(this,data);
		return null;
	}


	/**
	 * Handle unary
	 */
	public Object visit(ASTUnaryExpression node, Object data) {
		if(JSPELParserConstants.EMPTY == node.firstToken.kind) {
			append("((null == "); //$NON-NLS-1$
			node.childrenAccept(this, data);
			append(") || ("); //$NON-NLS-1$
			node.childrenAccept(this, data);
			append(").isEmpty())"); //$NON-NLS-1$
		} else if(JSPELParserConstants.NOT1 == node.firstToken.kind || JSPELParserConstants.NOT2 == node.firstToken.kind) {
			append("(!"); //$NON-NLS-1$
			node.childrenAccept(this, data);
			append(")"); //$NON-NLS-1$
		} else if(JSPELParserConstants.MINUS == node.firstToken.kind) {
			append("(-"); //$NON-NLS-1$
			node.childrenAccept(this, data);
			append(")"); //$NON-NLS-1$
		} else {
			node.childrenAccept(this, data);
		}
		return null;
	}


	/**
	 * Value node
	 */
	public Object visit(ASTValue node, Object data) {
		if(node.jjtGetNumChildren() >= 2) {
			if(node.jjtGetChild(0) instanceof ASTValuePrefix && node.jjtGetChild(1) instanceof ASTValueSuffix) {
				ASTValuePrefix prefix = (ASTValuePrefix) node.jjtGetChild(0);
				ASTValueSuffix suffix = (ASTValueSuffix) node.jjtGetChild(1);
				if(prefix.firstToken.image.equals("pageContext") && suffix.getPropertyNameToken().image.equals("request")) {
					append("((HTTPServletRequest)");
				}
			}
		}
		return node.childrenAccept(this, data);	
	}


	/**
	 * Value Prefix
	 */
	public Object visit(ASTValuePrefix node, Object data) {
		// this is a raw identifier.  May sure it's an implicit object.
		// This is the primary plae where modification is needed to 
		// support JSF backing beans.
		if(null == node.children) {
			if(isCompletingObject(node.firstToken.image)) {
				append(node.firstToken);
			} else {
				fCanGenerate = false;
			}
			return(null);
		}
		return node.childrenAccept(this, data);
	}


	/**
	 * Function for testing implicit objects.
	 * 
	 * @param image
	 * @return
	 */
	private boolean isCompletingObject(String image) {
		Boolean value = (Boolean)fJSPImplicitObjectMap.get(image);
		return null == value ? false : value.booleanValue();
	}

	/**
	 * Value suffix
	 */
	public Object visit(ASTValueSuffix node, Object data) {
		if(JSPELParserConstants.LBRACKET == node.firstToken.kind) {
			fCanGenerate = false;
		} else if(null != node.getPropertyNameToken()) {
			Token suffix = node.getPropertyNameToken();
			String ucaseName = suffix.image.substring(0, 1).toUpperCase() + suffix.image.substring(1, suffix.image.length()); 

			// This is a special case.  Note that the type system, no matter how much type information
			// we would have wouldn't give us the correct result.  We're looking for "pageContext.request" 
			// here and will add a downcast to (HTTPServletRequest)
			
			append(node.firstToken);
			append("get" + ucaseName + "()", suffix); //$NON-NLS-1$ //$NON-NLS-2$
			
			SimpleNode parent = (SimpleNode) node.jjtGetParent();
			if(suffix.image.equals("request") && parent instanceof ASTValue && //$NON-NLS-1$
					parent.jjtGetParent() instanceof ASTUnaryExpression && parent.firstToken.image.equals("pageContext")) { //$NON-NLS-1$
				append(")");
			} 


		} else {
			append(node.firstToken);
		}
		return null;
	}


	/**
	 * Function invocation
	 */
	public Object visit(ASTFunctionInvocation node, Object data) {
		String functionTranslation = genFunction(node.getFullFunctionName());
		if(null != functionTranslation)
		{
			append(functionTranslation + "(", node.getFirstToken()); //$NON-NLS-1$
			for(int i = 0; i < node.children.length; i++) {
				node.children[i].jjtAccept(this, data);
				if( node.children.length - i > 1){
					append(","); //$NON-NLS-1$
				}
			}
			append(")"); //$NON-NLS-1$
		}
		return null;
	}

	/**
	 * Literal
	 */
	public Object visit(ASTLiteral node, Object data) {
		// TODO any further translation needed?
		append(node.firstToken);
		return null;
	}
}
