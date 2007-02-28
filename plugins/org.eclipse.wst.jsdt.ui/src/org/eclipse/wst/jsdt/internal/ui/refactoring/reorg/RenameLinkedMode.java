package org.eclipse.wst.jsdt.internal.ui.refactoring.reorg;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.operation.IRunnableWithProgress;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IEditingSupport;
import org.eclipse.jface.text.IEditingSupportRegistry;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.link.ILinkedModeListener;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.jface.text.link.LinkedModeUI.ExitFlags;
import org.eclipse.jface.text.link.LinkedModeUI.IExitPolicy;
import org.eclipse.jface.text.source.ISourceViewer;

import org.eclipse.ui.texteditor.link.EditorLinkedModeUI;

import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardPage;

import org.eclipse.wst.jsdt.core.ICompilationUnit;
import org.eclipse.wst.jsdt.core.IJavaElement;
import org.eclipse.wst.jsdt.core.IJavaProject;
import org.eclipse.wst.jsdt.core.IMethod;
import org.eclipse.wst.jsdt.core.JavaConventions;
import org.eclipse.wst.jsdt.core.JavaCore;
import org.eclipse.wst.jsdt.core.JavaModelException;
import org.eclipse.wst.jsdt.core.dom.ASTNode;
import org.eclipse.wst.jsdt.core.dom.CompilationUnit;
import org.eclipse.wst.jsdt.core.dom.SimpleName;
import org.eclipse.wst.jsdt.core.refactoring.IJavaRefactorings;
import org.eclipse.wst.jsdt.core.refactoring.descriptors.RenameJavaElementDescriptor;

import org.eclipse.wst.jsdt.internal.corext.dom.LinkedNodeFinder;
import org.eclipse.wst.jsdt.internal.corext.dom.NodeFinder;
import org.eclipse.wst.jsdt.internal.corext.refactoring.rename.RenamingNameSuggestor;
import org.eclipse.wst.jsdt.internal.corext.util.JavaModelUtil;

import org.eclipse.wst.jsdt.ui.refactoring.RenameSupport;

import org.eclipse.wst.jsdt.internal.ui.JavaPlugin;
import org.eclipse.wst.jsdt.internal.ui.javaeditor.ASTProvider;
import org.eclipse.wst.jsdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.wst.jsdt.internal.ui.javaeditor.EditorHighlightingSynchronizer;
import org.eclipse.wst.jsdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.wst.jsdt.internal.ui.refactoring.DelegateUIHelper;

public class RenameLinkedMode {

	private class FocusEditingSupport implements IEditingSupport {
		public boolean ownsFocusShell() {
			if (fInfoPopup == null)
				return false;
			Shell popup= fInfoPopup.getShell();
			if (popup == null || popup.isDisposed())
				return false;
			Shell activeShell= popup.getDisplay().getActiveShell();
			return popup == activeShell;
		}

		public boolean isOriginator(DocumentEvent event, IRegion subjectRegion) {
			return false; //leave on external modification outside positions
		}
	}
	
	private class EditorSynchronizer implements ILinkedModeListener {
		public void left(LinkedModeModel model, int flags) {
			linkedModeLeft();
			if ( (flags & ILinkedModeListener.UPDATE_CARET) != 0) {
				doRename(fShowPreview);
			}
		}

		public void resume(LinkedModeModel model, int flags) {
		}

		public void suspend(LinkedModeModel model) {
		}
	}
	
	private class ExitPolicy implements IExitPolicy {
		public ExitFlags doExit(LinkedModeModel model, VerifyEvent event, int offset, int length) {
			fShowPreview= (event.stateMask & SWT.CTRL) != 0;
			return null; // don't change behavior; do actions in EditorSynchronizer
		}
	}
	
	
	private static RenameLinkedMode fgActiveLinkedMode;
	
	private CompilationUnitEditor fEditor;
	private IJavaElement fJavaElement;

	private RenameInformationPopup fInfoPopup;
	
	private Point fOriginalSelection;
	private String fOriginalName;

	private LinkedPosition fNamePosition;
	private LinkedModeModel fLinkedModeModel;
	private LinkedPositionGroup fLinkedPositionGroup;
	private final FocusEditingSupport fFocusEditingSupport;
	private boolean fShowPreview;

	public RenameLinkedMode(IJavaElement element, CompilationUnitEditor editor) {
		fEditor= editor;
		fJavaElement= element;
		fFocusEditingSupport= new FocusEditingSupport();
	}
	
	public static RenameLinkedMode getActiveLinkedMode() {
		return fgActiveLinkedMode;
	}
	
	public void start() {
		if (fgActiveLinkedMode != null) {
			// for safety; should already be handled in RenameJavaElementAction
			fgActiveLinkedMode.startFullDialog();
			return;
		}
		
		ISourceViewer viewer= fEditor.getViewer();
		IDocument document= viewer.getDocument();
		fOriginalSelection= viewer.getSelectedRange();
		int offset= fOriginalSelection.x;
		
		try {
			CompilationUnit root= JavaPlugin.getDefault().getASTProvider().getAST(getCompilationUnit(), ASTProvider.WAIT_YES, null);
			
			fLinkedPositionGroup= new LinkedPositionGroup();
			ASTNode selectedNode= NodeFinder.perform(root, fOriginalSelection.x, fOriginalSelection.y);
			if (! (selectedNode instanceof SimpleName)) {
				return; // TODO: show dialog
			}
			SimpleName nameNode= (SimpleName) selectedNode;
			
			fOriginalName= nameNode.getIdentifier();
			final int pos= nameNode.getStartPosition();
			ASTNode[] sameNodes= LinkedNodeFinder.findByNode(root, nameNode);
			
			//TODO: copied from LinkedNamesAssistProposal#apply(..):
			// sort for iteration order, starting with the node @ offset
			Arrays.sort(sameNodes, new Comparator() {
				public int compare(Object o1, Object o2) {
					return rank((ASTNode) o1) - rank((ASTNode) o2);
				}
				/**
				 * Returns the absolute rank of an <code>ASTNode</code>. Nodes
				 * preceding <code>pos</code> are ranked last.
				 *
				 * @param node the node to compute the rank for
				 * @return the rank of the node with respect to the invocation offset
				 */
				private int rank(ASTNode node) {
					int relativeRank= node.getStartPosition() + node.getLength() - pos;
					if (relativeRank < 0)
						return Integer.MAX_VALUE + relativeRank;
					else
						return relativeRank;
				}
			});
			for (int i= 0; i < sameNodes.length; i++) {
				ASTNode elem= sameNodes[i];
				LinkedPosition linkedPosition= new LinkedPosition(document, elem.getStartPosition(), elem.getLength(), i);
				if (i == 0)
					fNamePosition= linkedPosition;
				fLinkedPositionGroup.addPosition(linkedPosition);
			}
				
			fLinkedModeModel= new LinkedModeModel();
			fLinkedModeModel.addGroup(fLinkedPositionGroup);
			fLinkedModeModel.forceInstall();
			fLinkedModeModel.addLinkingListener(new EditorHighlightingSynchronizer(fEditor));
			fLinkedModeModel.addLinkingListener(new EditorSynchronizer());
            
			LinkedModeUI ui= new EditorLinkedModeUI(fLinkedModeModel, viewer);
			ui.setExitPosition(viewer, offset, 0, Integer.MAX_VALUE);
			ui.setExitPolicy(new ExitPolicy());
			ui.enter();
			
			viewer.setSelectedRange(fOriginalSelection.x, fOriginalSelection.y); // by default, full word is selected; restore original selection
			
		} catch (BadLocationException e) {
			JavaPlugin.log(e);
		}
		
		if (viewer instanceof IEditingSupportRegistry) {
			IEditingSupportRegistry registry= (IEditingSupportRegistry) viewer;
			registry.register(fFocusEditingSupport);
		}

		openSecondaryPopup();
//		startAnimation();
		fgActiveLinkedMode= this;
	}
	
//	private void startAnimation() {
//		//TODO:
//		// - switch off if animations disabled 
//		// - show rectangle around target for 500ms after animation
//		Shell shell= fEditor.getSite().getShell();
//		StyledText textWidget= fEditor.getViewer().getTextWidget();
//		
//		// from popup:
//		Rectangle startRect= fPopup.getBounds();
//		
//		// from editor:
////		Point startLoc= textWidget.getParent().toDisplay(textWidget.getLocation());
////		Point startSize= textWidget.getSize();
////		Rectangle startRect= new Rectangle(startLoc.x, startLoc.y, startSize.x, startSize.y);
//		
//		// from hell:
////		Rectangle startRect= shell.getClientArea();
//		
//		Point caretLocation= textWidget.getLocationAtOffset(textWidget.getCaretOffset());
//		Point displayLocation= textWidget.toDisplay(caretLocation);
//		Rectangle targetRect= new Rectangle(displayLocation.x, displayLocation.y, 0, 0);
//		
//		RectangleAnimation anim= new RectangleAnimation(shell, startRect, targetRect);
//		anim.schedule();
//	}

	void doRename(boolean showPreview) {
		cancel();
		
		try {
			fEditor.getViewer().getTextWidget().setRedraw(false);
			String newName= fNamePosition.getContent();
			if (fOriginalName.equals(newName))
				return;
			RenameSupport renameSupport= undoAndCreateRenameSupport(newName);
			if (renameSupport == null)
				return;
			
			Shell shell= fEditor.getSite().getShell();
			boolean executed;
			if (showPreview) {
				executed= renameSupport.openDialog(shell, true);
			} else {
				renameSupport.perform(shell, fEditor.getSite().getWorkbenchWindow());
				executed= true;
			}
			if (executed) {
				restoreFullSelection();
			}
			JavaModelUtil.reconcile(getCompilationUnit());
		} catch (CoreException ex) {
			JavaPlugin.log(ex);
		} catch (InterruptedException ex) {
			// canceling is OK -> redo text changes in that case?
		} catch (InvocationTargetException ex) {
			JavaPlugin.log(ex);
		} catch (BadLocationException e) {
			JavaPlugin.log(e);
		} finally {
			fEditor.getViewer().getTextWidget().setRedraw(true);
		}
	}

	public void cancel() {
		fLinkedModeModel.exit(ILinkedModeListener.NONE);
		linkedModeLeft();
	}
	
	private void restoreFullSelection() {
		if (fOriginalSelection.y != 0) {
			int originalOffset= fOriginalSelection.x;
			LinkedPosition[] positions= fLinkedPositionGroup.getPositions();
			for (int i= 0; i < positions.length; i++) {
				LinkedPosition position= positions[i];
				if (! position.isDeleted() && position.includes(originalOffset)) {
					fEditor.getViewer().setSelectedRange(position.offset, position.length);
					return;
				}
			}
		}
	}
	
	private RenameSupport undoAndCreateRenameSupport(String newName) throws CoreException {
		// Assumption: the linked mode model should be shut down by now.
		
		ISourceViewer viewer= fEditor.getViewer();
		final IDocument document= viewer.getDocument();
		
		try {
			if (! fOriginalName.equals(newName)) {
				fEditor.getSite().getWorkbenchWindow().run(false, true, new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						LinkedPosition[] positions= fLinkedPositionGroup.getPositions();
						Arrays.sort(positions, new Comparator() {
							public int compare(Object o1, Object o2) {
								return ((LinkedPosition) o1).offset - ((LinkedPosition) o2).offset;
							}
						});
						int correction= 0;
						int originalLength= fOriginalName.length();
						for (int i= 0; i < positions.length; i++) {
							LinkedPosition position= positions[i];
							try {
								int length= position.getLength();
								document.replace(position.getOffset() + correction, length, fOriginalName);
								correction= correction - length + originalLength;
							} catch (BadLocationException e) {
								throw new InvocationTargetException(e);
							}
						}
					}
				});
			}
		} catch (InvocationTargetException e) {
			throw new CoreException(new Status(IStatus.ERROR, JavaPlugin.getPluginId(), ReorgMessages.RenameLinkedMode_error_saving_editor, e));
		} catch (InterruptedException e) {
			// cancelling is OK
			return null;
		} finally {
			JavaModelUtil.reconcile(getCompilationUnit());
		}
		
		viewer.setSelectedRange(fOriginalSelection.x, fOriginalSelection.y);
		
		RenameJavaElementDescriptor descriptor= createRenameDescriptor(fJavaElement, newName);
		RenameSupport renameSupport= RenameSupport.create(descriptor);
		return renameSupport;
	}

	private ICompilationUnit getCompilationUnit() {
		return (ICompilationUnit) EditorUtility.getEditorInputJavaElement(fEditor, false);
	}
	
	public void startFullDialog() {
		cancel();
		
		try {
			String newName= fNamePosition.getContent();
			RenameSupport renameSupport= undoAndCreateRenameSupport(newName);
			if (renameSupport != null)
				renameSupport.openDialog(fEditor.getSite().getShell());
		} catch (CoreException e) {
			JavaPlugin.log(e);
		} catch (BadLocationException e) {
			JavaPlugin.log(e);
		}
	}
	
	/**
	 * @param javaElement
	 * @param newName
	 * @return a rename descriptor with current settings as used in the refactoring dialogs 
	 * @throws JavaModelException
	 */
	private RenameJavaElementDescriptor createRenameDescriptor(IJavaElement javaElement, String newName) throws JavaModelException {
		String contributionId;
		// see RefactoringExecutionStarter#createRenameSupport(..):
		int elementType= javaElement.getElementType();
		switch (elementType) {
			case IJavaElement.JAVA_PROJECT:
				contributionId= IJavaRefactorings.RENAME_JAVA_PROJECT;
				break;
			case IJavaElement.PACKAGE_FRAGMENT_ROOT:
				contributionId= IJavaRefactorings.RENAME_SOURCE_FOLDER;
				break;
			case IJavaElement.PACKAGE_FRAGMENT:
				contributionId= IJavaRefactorings.RENAME_PACKAGE;
				break;
			case IJavaElement.COMPILATION_UNIT:
				contributionId= IJavaRefactorings.RENAME_COMPILATION_UNIT;
				break;
			case IJavaElement.TYPE:
				contributionId= IJavaRefactorings.RENAME_TYPE;
				break;
			case IJavaElement.METHOD:
				final IMethod method= (IMethod) javaElement;
				if (method.isConstructor())
					return createRenameDescriptor(method.getDeclaringType(), newName);
				else
					contributionId= IJavaRefactorings.RENAME_METHOD;
				break;
			case IJavaElement.FIELD:
				contributionId= IJavaRefactorings.RENAME_FIELD;
				break;
			case IJavaElement.TYPE_PARAMETER:
				contributionId= IJavaRefactorings.RENAME_TYPE_PARAMETER;
				break;
			case IJavaElement.LOCAL_VARIABLE:
				contributionId= IJavaRefactorings.RENAME_LOCAL_VARIABLE;
				break;
			default:
				return null;
		}
		
		RenameJavaElementDescriptor descriptor= (RenameJavaElementDescriptor) RefactoringCore.getRefactoringContribution(contributionId).createDescriptor();
		descriptor.setJavaElement(javaElement);
		descriptor.setNewName(newName);
		if (elementType != IJavaElement.PACKAGE_FRAGMENT_ROOT)
			descriptor.setUpdateReferences(true);
		
		IDialogSettings javaSettings= JavaPlugin.getDefault().getDialogSettings();
		IDialogSettings refactoringSettings= javaSettings.getSection(RefactoringWizardPage.REFACTORING_SETTINGS); //TODO: undocumented API
		if (refactoringSettings == null) {
			refactoringSettings= javaSettings.addNewSection(RefactoringWizardPage.REFACTORING_SETTINGS); 
		}
		
		switch (elementType) {
			case IJavaElement.METHOD:
			case IJavaElement.FIELD:
				descriptor.setDeprecateDelegate(refactoringSettings.getBoolean(DelegateUIHelper.DELEGATE_DEPRECATION));
				descriptor.setKeepOriginal(refactoringSettings.getBoolean(DelegateUIHelper.DELEGATE_UPDATING));
		}
		switch (elementType) {
			case IJavaElement.TYPE:
//			case IJavaElement.COMPILATION_UNIT: // TODO
				descriptor.setUpdateSimilarDeclarations(refactoringSettings.getBoolean(RenameRefactoringWizard.TYPE_UPDATE_SIMILAR_ELEMENTS));
				int strategy;
				try {
					strategy= refactoringSettings.getInt(RenameRefactoringWizard.TYPE_SIMILAR_MATCH_STRATEGY);
				} catch (NumberFormatException e) {
					strategy= RenamingNameSuggestor.STRATEGY_EXACT;
				}
				descriptor.setMatchStrategy(strategy);
		}
		switch (elementType) {
			case IJavaElement.PACKAGE_FRAGMENT:
				descriptor.setUpdateHierarchy(refactoringSettings.getBoolean(RenameRefactoringWizard.PACKAGE_RENAME_SUBPACKAGES));
		}
		switch (elementType) {
			case IJavaElement.PACKAGE_FRAGMENT:
			case IJavaElement.TYPE:
				String fileNamePatterns= refactoringSettings.get(RenameRefactoringWizard.QUALIFIED_NAMES_PATTERNS);
				if (fileNamePatterns != null && fileNamePatterns.length() != 0) {
					descriptor.setFileNamePatterns(fileNamePatterns);
					descriptor.setUpdateQualifiedNames(refactoringSettings.getBoolean(RenameRefactoringWizard.UPDATE_QUALIFIED_NAMES));
				}
		}
		switch (elementType) {
			case IJavaElement.PACKAGE_FRAGMENT:
			case IJavaElement.TYPE:
			case IJavaElement.FIELD:
				descriptor.setUpdateTextualOccurrences(refactoringSettings.getBoolean(RenameRefactoringWizard.UPDATE_TEXTUAL_MATCHES));
		}
		switch (elementType) {
			case IJavaElement.FIELD:
				descriptor.setRenameGetters(refactoringSettings.getBoolean(RenameRefactoringWizard.FIELD_RENAME_GETTER));
				descriptor.setRenameSetters(refactoringSettings.getBoolean(RenameRefactoringWizard.FIELD_RENAME_SETTER));
		}
		return descriptor;
	}

	private void linkedModeLeft() {
		fgActiveLinkedMode= null;
		fInfoPopup.close();
		
		ISourceViewer viewer= fEditor.getViewer();
		if (viewer instanceof IEditingSupportRegistry) {
			IEditingSupportRegistry registry= (IEditingSupportRegistry) viewer;
			registry.unregister(fFocusEditingSupport);
		}
	}

	private void openSecondaryPopup() {
		fInfoPopup= new RenameInformationPopup(fEditor, this);
		fInfoPopup.open();
	}

	public boolean isCaretInLinkedPosition() {
		return getCurrentLinkedPosition() != null;
	}

	public LinkedPosition getCurrentLinkedPosition() {
		Point selection= fEditor.getViewer().getSelectedRange();
		int start= selection.x;
		int end= start + selection.y;
		LinkedPosition[] positions= fLinkedPositionGroup.getPositions();
		for (int i= 0; i < positions.length; i++) {
			LinkedPosition position= positions[i];
			if (position.includes(start) || position.includes(end))
				return position;
		}
		return null;
	}

	public boolean isEnabled() {
		try {
			String newName= fNamePosition.getContent();
			if (fOriginalName.equals(newName))
				return false;
			/* 
			 * TODO: use JavaRenameProcessor#checkNewElementName(String)
			 * but make sure implementations don't access outdated Java Model
			 * (cache all necessary information before starting linked mode).
			 */
			IJavaProject project= fJavaElement.getJavaProject();
			String sourceLevel= project.getOption(JavaCore.COMPILER_SOURCE, true);
			String complianceLevel= project.getOption(JavaCore.COMPILER_COMPLIANCE, true);
			return JavaConventions.validateIdentifier(newName, sourceLevel, complianceLevel).isOK();
		} catch (BadLocationException e) {
			return false;
		}
		
	}

	public boolean isOriginalName() {
		try {
			String newName= fNamePosition.getContent();
			return fOriginalName.equals(newName);
		} catch (BadLocationException e) {
			return false;
		}
	}

}
