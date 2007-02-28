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
package org.eclipse.wst.jsdt.internal.ui.text.template.contentassist;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.RangeMarker;
import org.eclipse.text.edits.TextEdit;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.IRewriteTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateException;

import org.eclipse.wst.jsdt.core.ICompilationUnit;
import org.eclipse.wst.jsdt.core.IJavaProject;
import org.eclipse.wst.jsdt.core.dom.AST;
import org.eclipse.wst.jsdt.core.dom.ASTNode;
import org.eclipse.wst.jsdt.core.dom.ASTParser;
import org.eclipse.wst.jsdt.core.dom.Block;
import org.eclipse.wst.jsdt.core.dom.MethodDeclaration;
import org.eclipse.wst.jsdt.core.dom.Statement;
import org.eclipse.wst.jsdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.wst.jsdt.core.dom.rewrite.ITrackedNodePosition;

import org.eclipse.wst.jsdt.internal.corext.dom.ASTNodes;
import org.eclipse.wst.jsdt.internal.corext.dom.GenericVisitor;
import org.eclipse.wst.jsdt.internal.corext.template.java.CompilationUnitContext;
import org.eclipse.wst.jsdt.internal.corext.template.java.CompilationUnitContextType;
import org.eclipse.wst.jsdt.internal.corext.template.java.JavaContextType;

import org.eclipse.wst.jsdt.ui.text.java.IInvocationContext;

import org.eclipse.wst.jsdt.internal.ui.JavaPlugin;
import org.eclipse.wst.jsdt.internal.ui.text.correction.AssistContext;
import org.eclipse.wst.jsdt.internal.ui.text.correction.SurroundWith;

public class SurroundWithTemplateProposal extends TemplateProposal {
	
	private static class SurroundWithTemplate extends SurroundWith {
		
		private static final String $_LINE_SELECTION= "${" + GlobalTemplateVariables.LineSelection.NAME + "}"; //$NON-NLS-1$ //$NON-NLS-2$
		
		private final Template fTemplate;
		private final IJavaProject fCurrentProject;
		private ITrackedNodePosition fNewBodyPosition;
		private ASTNode fTemplateNode;
		
		public SurroundWithTemplate(IInvocationContext context, Statement[] selectedNodes, Template template) {
			super(context.getASTRoot(), selectedNodes);
			fTemplate= template;
			fCurrentProject= context.getCompilationUnit().getJavaProject();
		}
		
		public ITrackedNodePosition getNewBodyPosition() {
			return fNewBodyPosition;
		}
		
		/**
		 * Generate a new code skeleton.
		 * @param newBody The new body which will be filled with code.
		 * @param rewrite The rewrite to use to change the ast.
		 * @return The root of the new code.
		 */
		protected Statement generateCodeSkeleton(Block newBody, ASTRewrite rewrite) {
			fNewBodyPosition= rewrite.track(newBody);
			return newBody;
		}
		
		protected List getVariableDeclarationReadsInside(Statement[] selectedNodes, int maxVariableId) {
			if (isNewContext())
				return super.getVariableDeclarationReadsInside(selectedNodes, maxVariableId);
			return new ArrayList();
		}
		
		protected boolean isNewContext() {

			final String templateVariableRegEx= "\\$\\{[^\\}]*\\}"; //$NON-NLS-1$
			
			String template= fTemplate.getPattern();
			int currentPosition= template.indexOf($_LINE_SELECTION);
			int insertionPosition= -1;
			while (currentPosition != -1) {
				insertionPosition= currentPosition;
				template= template.replaceFirst(templateVariableRegEx, ""); //$NON-NLS-1$
				currentPosition= template.indexOf($_LINE_SELECTION);
			}
			template= template.replaceAll(templateVariableRegEx, ""); //$NON-NLS-1$

			AST ast= getAst();
			ASTParser parser= ASTParser.newParser(ast.apiLevel());
			parser.setSource(template.toCharArray());
			parser.setProject(fCurrentProject);
			parser.setKind(ASTParser.K_STATEMENTS);
			ASTNode root= parser.createAST(null);
			if (((Block)root).statements().isEmpty()) {
				parser= ASTParser.newParser(ast.apiLevel());
				parser.setSource(template.toCharArray());
				parser.setProject(fCurrentProject);
				parser.setKind(ASTParser.K_EXPRESSION);
				root= parser.createAST(null);
			}
			
			final int lineSelectionPosition= insertionPosition;
			root.accept(new GenericVisitor() {
				public void endVisit(Block node) {
					super.endVisit(node);
					if (fTemplateNode == null && node.getStartPosition() <= lineSelectionPosition && node.getLength() + node.getStartPosition() >= lineSelectionPosition) {
						fTemplateNode= node;
					}
				}
			});
			
			if (fTemplateNode != null && ASTNodes.getParent(fTemplateNode, MethodDeclaration.class) != null) {
				return true;
			}
			
			return false;
		}
		
	}


	private final IRegion fRegion;
	private final ICompilationUnit fCompilationUnit;
	private final CompilationUnitContext fContext;
	private final Template fTemplate;
	private final Statement[] fSelectedStatements;
	private TemplateProposal fProposal;
	private IRegion fSelectedRegion;

	public SurroundWithTemplateProposal(ICompilationUnit compilationUnit, Template template, CompilationUnitContext context, IRegion region, Image image, Statement[] selectedStatements) {
		super(template, context, region, image);
		fCompilationUnit= compilationUnit;
		fTemplate= template;
		fContext= context;
		fRegion= region;
		fSelectedStatements= selectedStatements;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.internal.ui.text.template.contentassist.TemplateProposal#getAdditionalProposalInfo()
	 */
	public String getPreviewContent() {
		try {
			IDocument document= new Document(fCompilationUnit.getBuffer().getContents());
			CompilationUnitContext context= createNewContext(document);
			
			int offset= context.getCompletionOffset();
			int start= context.getStart();
			int end= context.getEnd();
			IRegion region= new Region(start, end - start);
			
			context.setReadOnly(false);
			TemplateBuffer templateBuffer;
			try {
				templateBuffer= context.evaluate(fTemplate);
			} catch (TemplateException e1) {
				JavaPlugin.log(e1);
				return null;
			}
			
			start= region.getOffset();
			end= region.getOffset() + region.getLength();
			end= Math.max(end, offset);

			String templateString= templateBuffer.getString();
			document.replace(start, end - start, templateString);
			
			return document.get();
			
		} catch (MalformedTreeException e) {
			JavaPlugin.log(e);
		} catch (IllegalArgumentException e) {
			JavaPlugin.log(e);
		} catch (BadLocationException e) {
			JavaPlugin.log(e);
		} catch (CoreException e) {
			JavaPlugin.log(e);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.internal.ui.text.template.contentassist.TemplateProposal#apply(org.eclipse.jface.text.ITextViewer, char, int, int)
	 */
	public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {
		try {
			setRedraw(viewer, false);
			IDocument document= viewer.getDocument();
			CompilationUnitContext context= createNewContext(document);
			
			int start= context.getStart();
			int end= context.getEnd();
			IRegion region= new Region(start, end - start);

			//Evaluate the template within the new context
			fProposal= new TemplateProposal(fTemplate, context, region, null);
			fProposal.apply(viewer, trigger, stateMask, context.getCompletionOffset());
		} catch (MalformedTreeException e) {
			handleException(viewer, e, fRegion);
		} catch (IllegalArgumentException e) {
			handleException(viewer, e, fRegion);
		} catch (BadLocationException e) {
			handleException(viewer, e, fRegion);
		} catch (CoreException e) {
			handleException(viewer, e, fRegion);
		} finally {
			setRedraw(viewer, true);
		}
	}
	
	private void setRedraw(ITextViewer viewer, boolean redraw) {
		if (viewer instanceof ITextViewerExtension) {
			ITextViewerExtension extension= (ITextViewerExtension) viewer;
			IRewriteTarget target= extension.getRewriteTarget();
			target.setRedraw(redraw);
		}
    }

	public Point getSelection(IDocument document) {
		if (fSelectedRegion != null) {
			return new Point(fSelectedRegion.getOffset(), fSelectedRegion.getLength());
		} else if (fProposal != null) {
			return fProposal.getSelection(document);
		} else {
			return null;
		}
	}
	
	private CompilationUnitContext createNewContext(IDocument document) throws CoreException, BadLocationException {
		AssistContext invocationContext= new AssistContext(fCompilationUnit, fContext.getStart(), fContext.getEnd() - fContext.getStart());
		
		SurroundWithTemplate surroundWith= new SurroundWithTemplate(invocationContext, fSelectedStatements, fTemplate);
		Map options= fCompilationUnit.getJavaProject().getOptions(true);
		MultiTextEdit allEdits= (MultiTextEdit)surroundWith.getRewrite().rewriteAST(document, options);
		removeBlockStartAndEnd(allEdits);
		MultiTextEdit allEditsCopy= (MultiTextEdit)allEdits.copy();
		
		String newSelection= calculateNewSelection(document, surroundWith.getNewBodyPosition(), allEdits);
		
		//Change the document such that it contains the new variable declarations (i.e. with final keyword)
		allEditsCopy.apply(document);
		
		//The document may have changed and we need a new context
		
		//Find position for template insertion in new document
		int offset= surroundWith.getNewBodyPosition().getStartPosition();
		
		//Create the new context
		CompilationUnitContextType contextType= (CompilationUnitContextType) JavaPlugin.getDefault().getTemplateContextRegistry().getContextType(JavaContextType.NAME);
		CompilationUnitContext context= contextType.createContext(document, offset, newSelection.length(), fCompilationUnit);
		context.setVariable("selection", newSelection); //$NON-NLS-1$
		context.setForceEvaluation(true);
		return context;
	}

	private void removeBlockStartAndEnd(MultiTextEdit edits) {
		TextEdit[] children= edits.getChildren();
		int i= 0;
		while (!(children[i] instanceof RangeMarker)) {
			i++;
		}
		i++;
		edits.removeChild(i);
		while (!(children[i] instanceof RangeMarker)) {
			i++;
		}
		children= edits.getChildren();
		TextEdit edit= children[i - 2];
		if (edit instanceof InsertEdit) {
			final InsertEdit insert= (InsertEdit) edit;
			final String text= insert.getText();
			if (text != null && text.startsWith(";")) { //$NON-NLS-1$
				children[i - 2]= new InsertEdit(edit.getOffset(), ";"); //$NON-NLS-1$
				edits.removeChildren();
				edits.addChildren(children);
				return;
			}
		}
		edits.removeChild(i - 2);
	}
	
	private String calculateNewSelection(IDocument document, ITrackedNodePosition position, MultiTextEdit edits) throws BadLocationException {
		IDocument tmpDocument= new Document(String.copyValueOf(document.get().toCharArray()));
		edits.apply(tmpDocument);
		return tmpDocument.get(position.getStartPosition(), position.getLength());
	}
	
	private void handleException(ITextViewer viewer, Exception e, IRegion region) {
		JavaPlugin.log(e);
		openErrorDialog(viewer.getTextWidget().getShell(), e);
		fSelectedRegion= region;
	}

	private void openErrorDialog(Shell shell, Exception e) {
		MessageDialog.openError(shell, TemplateContentAssistMessages.TemplateEvaluator_error_title, e.getMessage());
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean validate(IDocument document, int offset, DocumentEvent event) {
		return false;
	}
}
