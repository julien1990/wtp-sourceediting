package org.eclipse.wst.jsdt.web.ui.internal.autoedit;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.wst.html.core.text.IHTMLPartitions;
import org.eclipse.wst.jsdt.core.IJavaProject;
import org.eclipse.wst.jsdt.core.JavaCore;
import org.eclipse.wst.jsdt.internal.ui.text.java.JavaAutoIndentStrategy;
import org.eclipse.wst.jsdt.internal.ui.text.java.SmartSemicolonAutoEditStrategy;
import org.eclipse.wst.jsdt.internal.ui.text.javadoc.JavaDocAutoIndentStrategy;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;

public class AutoEditStrategyForJs implements IAutoEditStrategy {
	private IAutoEditStrategy[] fStrategies;
	
	public AutoEditStrategyForJs() {
		super();
	}
	
	public void customizeDocumentCommand(IDocument document, DocumentCommand command) {
		IAutoEditStrategy[] strats = getAutoEditStrategies(document);
		for (int i = 0; i < strats.length; i++) {
			strats[i].customizeDocumentCommand(document, command);
		}
	}
	
	public IAutoEditStrategy[] getAutoEditStrategies(IDocument document) {
		if (fStrategies != null) {
			return fStrategies;
		}
		String partitioning = IHTMLPartitions.SCRIPT;
		fStrategies = new IAutoEditStrategy[] { new JavaDocAutoIndentStrategy(partitioning), new SmartSemicolonAutoEditStrategy(partitioning),
				new JavaAutoIndentStrategy(partitioning, getJavaProject(document)) };
		/* new AutoEditStrategyForTabs() */
		return fStrategies;
	}
	
	private IJavaProject getJavaProject(IDocument document) {
		IDOMModel model = null;
		IJavaProject javaProject = null;
		try {
			model = (IDOMModel) StructuredModelManager.getModelManager().getExistingModelForRead(document);
			String baseLocation = model.getBaseLocation();
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IPath filePath = new Path(baseLocation);
			IProject project = null;
			if (filePath.segmentCount() > 0) {
				project = root.getProject(filePath.segment(0));
			}
			if (project != null) {
				javaProject = JavaCore.create(project);
			}
		} finally {
			if (model != null) {
				model.releaseFromRead();
			}
		}
		return javaProject;
	}
}
