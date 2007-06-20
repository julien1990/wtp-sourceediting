package org.eclipse.wst.jsdt.web.ui.internal.contentassist;

import java.util.Arrays;
import java.util.Vector;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.wst.jsdt.web.core.internal.java.IJsTranslation;
import org.eclipse.wst.jsdt.web.core.internal.java.JsTranslation;
import org.eclipse.wst.jsdt.web.core.internal.java.JsTranslationAdapter;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.eclipse.wst.xml.ui.internal.contentassist.AbstractContentAssistProcessor;

public class JSDTContentAssistant extends AbstractContentAssistProcessor {
	private JSDTContentAssistantProcessor fContentAssistProcessor;
	private JSDTTemplateAssistProcessor fTemplateAssistProcessor;
	private JsTranslationAdapter fTranslationAdapter;
	
	
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentPosition) {
		Vector proposals = new Vector();
		ICompletionProposal[] completionProposals;
		JSDTProposalCollector theCollector = getProposalCollector(viewer, documentPosition);
		/* --------- Content Assistant --------- */
		getContentAssistProcessor().setProposalCollector(theCollector);
		completionProposals = getContentAssistProcessor().computeCompletionProposals(viewer, documentPosition);
		proposals.addAll(Arrays.asList(completionProposals));
		/* --------- template completions --------- */
		getTemplateCompletionProcessor().setProposalCollector(theCollector);
		completionProposals = getTemplateCompletionProcessor().computeCompletionProposals(viewer, documentPosition);
		proposals.addAll(Arrays.asList(completionProposals));
		return (ICompletionProposal[]) proposals.toArray(new ICompletionProposal[0]);
	}
	
	private JSDTContentAssistantProcessor getContentAssistProcessor() {
		if (fContentAssistProcessor == null) {
			fContentAssistProcessor = new JSDTContentAssistantProcessor();
		}
		return fContentAssistProcessor;
	}
	
	private JsTranslation getJSPTranslation(ITextViewer viewer, int offset) {
		IDOMModel xmlModel = null;
		try {
			xmlModel = (IDOMModel) StructuredModelManager.getModelManager().getExistingModelForRead(viewer.getDocument());
			IDOMDocument xmlDoc = xmlModel.getDocument();
			if (fTranslationAdapter == null) {
				fTranslationAdapter = (JsTranslationAdapter) xmlDoc.getAdapterFor(IJsTranslation.class);
			}
			if (fTranslationAdapter != null) {
				return fTranslationAdapter.getJSPTranslation();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (xmlModel != null) {
				xmlModel.releaseFromRead();
			}
		}
		return null;
	}
	
	protected JSDTProposalCollector getProposalCollector(ITextViewer viewer, int offset) {
		JsTranslation tran = getJSPTranslation(viewer, offset);
		return new JSDTProposalCollector(tran);
	}
	
	private JSDTTemplateAssistProcessor getTemplateCompletionProcessor() {
		if (fTemplateAssistProcessor == null) {
			fTemplateAssistProcessor = new JSDTTemplateAssistProcessor();
		}
		return fTemplateAssistProcessor;
	}
}
