package org.eclipse.wst.jsdt.web.ui.internal.contentassist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.jsdt.core.CompletionProposal;
import org.eclipse.wst.jsdt.core.Signature;
import org.eclipse.wst.jsdt.ui.text.java.CompletionProposalCollector;
import org.eclipse.wst.jsdt.ui.text.java.CompletionProposalComparator;
import org.eclipse.wst.jsdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.wst.jsdt.web.core.internal.java.JSPTranslation;

/**
 * Passed into ICodeComplete#codeComplete(int offset, CompletionRequestor
 * requestor). Adapts IJavaCompletionProposals to JSPCompletion proposals. This
 * includes: - translating offsets - "fixing" up display strings - filtering
 * some unwanted proposals
 * 
 * @plannedfor 1.0
 */
public class JSDTProposalCollector extends CompletionProposalCollector {

	private JSPTranslation fTranslation;
	private Comparator fComparator;
    private IJavaCompletionProposal[] fJavaProposals;
    private List fGeneratedFunctionNames;
    
	public JSDTProposalCollector( JSPTranslation translation) {
	  super(translation.getCompilationUnit());
	  if (translation == null) {
			throw new IllegalArgumentException("JSPTranslation cannot be null"); //$NON-NLS-1$
	  }
		fTranslation = translation;
	}
	
//	public List getGeneratedFunctionNames(){
//		if(fGeneratedFunctionNames==null){
//			fGeneratedFunctionNames = fTranslation.getGeneratedFunctionNames();
//		}
//		return fGeneratedFunctionNames;
//	}
    
	/**
	 * Ensures that we only return JSPCompletionProposals.
	 * 
	 * @return an array of JSPCompletionProposals
	 */
	public JSDTCompletionProposal[] getJSPCompletionProposals() {
		List results = new ArrayList();
		IJavaCompletionProposal[] javaProposals = getJavaCompletionProposals();
		// need to filter out non JSPCompletionProposals
		// because their offsets haven't been translated
		for (int i = 0; i < javaProposals.length; i++) {
			if (javaProposals[i] instanceof JSDTCompletionProposal) {
				
				results.add(javaProposals[i]);
			}
		}
		Collections.sort(results, getComparator());
		return (JSDTCompletionProposal[]) results
				.toArray(new JSDTCompletionProposal[results.size()]);
	}

	private Comparator getComparator() {
		if (fComparator == null) {
			fComparator = new CompletionProposalComparator();
		}
		return fComparator;
	}

	/**
	 * Overridden to: - translate Java -> JSP offsets - fix
	 * cursor-position-after - fix mangled servlet name in display string -
	 * remove unwanted proposals (servlet constructor)
	 */
	@Override
	protected IJavaCompletionProposal createJavaCompletionProposal(
			CompletionProposal proposal) {

		JSDTCompletionProposal jspProposal = null;

		// ignore constructor proposals (they're not relevant for our JSP
		// proposal list)
		if (!proposal.isConstructor()) {

//			if (proposal.getKind() == CompletionProposal.TYPE_REF) {
//				String signature = String.valueOf(proposal
//						.getDeclarationSignature());
//				String completion = String.valueOf(proposal.getCompletion());
//				if (completion.indexOf(signature) != -1) {
//					jspProposal = createAutoImportProposal(proposal);
//				}
//			}

			// default behavior
//			if (jspProposal == null) {
//			for(int i = 0;i<getGeneratedFunctionNames().size();i++){
//				if((new String(proposal.getName())).equalsIgnoreCase((String)getGeneratedFunctionNames().get(i))) return jspProposal;
//			}
			jspProposal = createJspProposal(proposal);
			//}
		}
		return jspProposal;
	}

//	private JSPCompletionProposal createAutoImportProposal(
//			CompletionProposal proposal) {
//
//		JSPCompletionProposal jspProposal = null;
//
//		String signature = new String(proposal.getDeclarationSignature());
//		String completion = new String(proposal.getCompletion());
//
//		// it's fully qualified so we should
//		// add an import statement
//		// create an autoimport proposal
//		String newCompletion = completion.replaceAll(signature + ".", ""); //$NON-NLS-1$ //$NON-NLS-2$
//
//		// java offset
//		int offset = proposal.getReplaceStart();
//		// replacement length
//		int length = proposal.getReplaceEnd() - offset;
//		// translate offset from Java > JSP
//		offset = fTranslation.getJspOffset(offset);
//		// cursor position after must be calculated
//		int positionAfter = calculatePositionAfter(proposal, newCompletion,
//				offset);
//
//		// from java proposal
//		IJavaCompletionProposal javaProposal = super
//				.createJavaCompletionProposal(proposal);
//		proposal.getDeclarationSignature();
//		Image image = javaProposal.getImage();
//		String displayString = javaProposal.getDisplayString();
//		displayString = getTranslation().fixupMangledName(displayString);
//		IContextInformation contextInformation = javaProposal
//				.getContextInformation();
//		// don't do this, it's slow
//		// String additionalInfo = javaProposal.getAdditionalProposalInfo();
//		int relevance = javaProposal.getRelevance();
//
//		boolean updateLengthOnValidate = true;
//
//		jspProposal = new AutoImportProposal(completion, newCompletion, offset,
//				length, positionAfter, image, displayString,
//				contextInformation, null, relevance, updateLengthOnValidate);
//
//		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=124483
//		// set wrapped java proposal so additional info can be calculated on
//		// demand
//		jspProposal.setJavaCompletionProposal(javaProposal);
//
//		return jspProposal;
//	}

	private JSDTCompletionProposal createJspProposal(CompletionProposal proposal) {

		JSDTCompletionProposal jspProposal;
		String completion = String.valueOf(proposal.getCompletion());
		// java offset
		int offset = proposal.getReplaceStart();
		// replacement length
		int length = proposal.getReplaceEnd() - offset;
		// translate offset from Java > JSP
	
		// cursor position after must be calculated
		int positionAfter = calculatePositionAfter(proposal, completion, offset);

		// from java proposal
		IJavaCompletionProposal javaProposal = super
				.createJavaCompletionProposal(proposal);
		proposal.getDeclarationSignature();
		Image image = javaProposal.getImage();
		String displayString = javaProposal.getDisplayString();
		displayString = getTranslation().fixupMangledName(displayString);
//		for(int i = 0;i<getGeneratedFunctionNames().size();i++){
//			displayString.replace((String)getGeneratedFunctionNames().get(i), "");
//		}
		
		
		IContextInformation contextInformation = javaProposal
				.getContextInformation();
		//String additionalInfo = javaProposal.getAdditionalProposalInfo();
		int relevance = javaProposal.getRelevance();

		boolean updateLengthOnValidate = true;

		jspProposal = new JSDTCompletionProposal(completion, offset, length,
				positionAfter, image, displayString, contextInformation, null,
				relevance, updateLengthOnValidate);

		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=124483
		// set wrapped java proposal so additional info can be calculated on
		// demand
		jspProposal.setJavaCompletionProposal(javaProposal);

		return jspProposal;
	}

	/**
	 * Cacluates the where the cursor should be after applying this proposal.
	 * eg. method(|) if the method proposal chosen had params.
	 * 
	 * @param proposal
	 * @param completion
	 * @param currentCursorOffset
	 * @return
	 */
	private int calculatePositionAfter(CompletionProposal proposal,
			String completion, int currentCursorOffset) {
		// calculate cursor position after
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=118398
		// int positionAfter = currentCursorOffset+completion.length();
		int positionAfter = completion.length();

		int kind = proposal.getKind();

		// may need better logic here...
		// put cursor inside parenthesis if there's params
		// only checking for any kind of declaration
		if (kind == CompletionProposal.ANONYMOUS_CLASS_DECLARATION
				|| kind == CompletionProposal.METHOD_DECLARATION
				|| kind == CompletionProposal.POTENTIAL_METHOD_DECLARATION
				|| kind == CompletionProposal.METHOD_REF) {
			String[] params = Signature.getParameterTypes(String
					.valueOf(proposal.getSignature()));
			if (completion.length() > 0 && params.length > 0) {
				positionAfter--;
			}
		}
		return positionAfter;
	}

	static char[] getTypeTriggers() {
		return TYPE_TRIGGERS;
	}

	public JSPTranslation getTranslation() {
		return fTranslation;
	}

}