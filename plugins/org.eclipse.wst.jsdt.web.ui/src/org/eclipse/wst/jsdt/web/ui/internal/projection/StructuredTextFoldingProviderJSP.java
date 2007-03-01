package org.eclipse.wst.jsdt.web.ui.internal.projection;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.source.projection.IProjectionListener;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.PropagatingAdapter;
import org.eclipse.wst.sse.core.internal.model.FactoryRegistry;
import org.eclipse.wst.sse.core.internal.provisional.INodeAdapter;
import org.eclipse.wst.sse.core.internal.provisional.INodeNotifier;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.IndexedRegion;
import org.eclipse.wst.sse.ui.internal.projection.IStructuredTextFoldingProvider;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Updates the projection model of a structured model for JSP.
 */
public class StructuredTextFoldingProviderJSP implements
		IStructuredTextFoldingProvider, IProjectionListener, ITextInputListener {
	private final static boolean debugProjectionPerf = "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.wst.jsdt.web.ui/projectionperf")); //$NON-NLS-1$ //$NON-NLS-2$

	private IDocument fDocument;
	private ProjectionViewer fViewer;
	private boolean fProjectionNeedsToBeEnabled = false;
	/**
	 * Maximum number of child nodes to add adapters to (limit for performance
	 * sake)
	 */
	private final int MAX_CHILDREN = 10;
	/**
	 * Maximum number of sibling nodes to add adapters to (limit for performance
	 * sake)
	 */
	private final int MAX_SIBLINGS = 1000;

	/**
	 * Adds an adapter to node and its children
	 * 
	 * @param node
	 * @param childLevel
	 */
	private void addAdapterToNodeAndChildren(Node node, int childLevel) {
		// stop adding initial adapters MAX_CHILDREN levels deep for
		// performance sake
		if (node instanceof INodeNotifier && childLevel < MAX_CHILDREN) {
			INodeNotifier notifier = (INodeNotifier) node;

			// try and get the adapter for the current node and update the
			// adapter with projection information
			ProjectionModelNodeAdapterJSP adapter = (ProjectionModelNodeAdapterJSP) notifier
					.getExistingAdapter(ProjectionModelNodeAdapterJSP.class);
			if (adapter != null) {
				adapter.updateAdapter(node, fViewer);
			} else {
				// just call getadapter so the adapter is created and
				// automatically initialized
				notifier.getAdapterFor(ProjectionModelNodeAdapterJSP.class);
			}
			ProjectionModelNodeAdapterHTML adapter2 = (ProjectionModelNodeAdapterHTML) notifier
					.getExistingAdapter(ProjectionModelNodeAdapterHTML.class);
			if (adapter2 != null) {
				adapter2.updateAdapter(node);
			} else {
				// just call getadapter so the adapter is created and
				// automatically initialized
				notifier.getAdapterFor(ProjectionModelNodeAdapterHTML.class);
			}
			int siblingLevel = 0;
			Node nextChild = node.getFirstChild();
			while (nextChild != null && siblingLevel < MAX_SIBLINGS) {
				Node childNode = nextChild;
				nextChild = childNode.getNextSibling();

				addAdapterToNodeAndChildren(childNode, childLevel + 1);
				++siblingLevel;
			}
		}
	}

	/**
	 * Goes through every node and adds an adapter onto each for tracking
	 * purposes
	 */
	private void addAllAdapters() {
		long start = System.currentTimeMillis();

		if (fDocument != null) {
			IStructuredModel sModel = null;
			try {
				sModel = StructuredModelManager.getModelManager()
						.getExistingModelForRead(fDocument);
				if (sModel != null) {
					int startOffset = 0;
					IndexedRegion startNode = sModel
							.getIndexedRegion(startOffset);
					if (startNode instanceof Node) {
						int siblingLevel = 0;
						Node nextSibling = (Node) startNode;
						while (nextSibling != null
								&& siblingLevel < MAX_SIBLINGS) {
							Node currentNode = nextSibling;
							nextSibling = currentNode.getNextSibling();

							addAdapterToNodeAndChildren(currentNode, 0);
							++siblingLevel;
						}
					}
				}
			} finally {
				if (sModel != null) {
					sModel.releaseFromRead();
				}
			}
		}
		if (debugProjectionPerf) {
			long end = System.currentTimeMillis();
			System.out
					.println("StructuredTextFoldingProviderJSP.addAllAdapters: " + (end - start)); //$NON-NLS-1$
		}
	}

	/**
	 * Get the ProjectionModelNodeAdapterFactoryHTML to use with this provider.
	 * 
	 * @return ProjectionModelNodeAdapterFactoryHTML
	 */
	private ProjectionModelNodeAdapterFactoryHTML getAdapterFactoryHTML(
			boolean createIfNeeded) {
		long start = System.currentTimeMillis();

		ProjectionModelNodeAdapterFactoryHTML factory = null;
		if (fDocument != null) {
			IStructuredModel sModel = null;
			try {
				sModel = StructuredModelManager.getModelManager()
						.getExistingModelForRead(fDocument);
				if (sModel != null) {
					FactoryRegistry factoryRegistry = sModel
							.getFactoryRegistry();

					// getting the projectionmodelnodeadapter for the first
					// time
					// so do some initializing
					if (!factoryRegistry
							.contains(ProjectionModelNodeAdapterHTML.class)
							&& createIfNeeded) {
						ProjectionModelNodeAdapterFactoryHTML newFactory = new ProjectionModelNodeAdapterFactoryHTML();

						// add factory to factory registry
						factoryRegistry.addFactory(newFactory);

						// add factory to propogating adapter
						IDOMModel domModel = (IDOMModel) sModel;
						Document document = domModel.getDocument();
						PropagatingAdapter propagatingAdapter = (PropagatingAdapter) ((INodeNotifier) document)
								.getAdapterFor(PropagatingAdapter.class);
						if (propagatingAdapter != null) {
							propagatingAdapter
									.addAdaptOnCreateFactory(newFactory);
						}
					}

					// try and get the factory
					factory = (ProjectionModelNodeAdapterFactoryHTML) factoryRegistry
							.getFactoryFor(ProjectionModelNodeAdapterHTML.class);
				}
			} finally {
				if (sModel != null) {
					sModel.releaseFromRead();
				}
			}
		}

		if (debugProjectionPerf) {
			long end = System.currentTimeMillis();
			System.out
					.println("StructuredTextFoldingProviderJSP.getAdapterFactoryHTML: " + (end - start)); //$NON-NLS-1$
		}
		return factory;
	}

	/**
	 * Get the ProjectionModelNodeAdapterFactoryJSP to use with this provider.
	 * 
	 * @return ProjectionModelNodeAdapterFactoryJSP
	 */
	private ProjectionModelNodeAdapterFactoryJSP getAdapterFactoryJSP(
			boolean createIfNeeded) {
		long start = System.currentTimeMillis();

		ProjectionModelNodeAdapterFactoryJSP factory = null;
		if (fDocument != null) {
			IStructuredModel sModel = null;
			try {
				sModel = StructuredModelManager.getModelManager()
						.getExistingModelForRead(fDocument);
				if (sModel != null) {
					FactoryRegistry factoryRegistry = sModel
							.getFactoryRegistry();

					// getting the projectionmodelnodeadapter for the first
					// time
					// so do some initializing
					if (!factoryRegistry
							.contains(ProjectionModelNodeAdapterJSP.class)
							&& createIfNeeded) {
						ProjectionModelNodeAdapterFactoryJSP newFactory = new ProjectionModelNodeAdapterFactoryJSP();

						// add factory to factory registry
						factoryRegistry.addFactory(newFactory);

						// add factory to propogating adapter
						IDOMModel domModel = (IDOMModel) sModel;
						Document document = domModel.getDocument();
						PropagatingAdapter propagatingAdapter = (PropagatingAdapter) ((INodeNotifier) document)
								.getAdapterFor(PropagatingAdapter.class);
						if (propagatingAdapter != null) {
							propagatingAdapter
									.addAdaptOnCreateFactory(newFactory);
						}
					}

					// try and get the factory
					factory = (ProjectionModelNodeAdapterFactoryJSP) factoryRegistry
							.getFactoryFor(ProjectionModelNodeAdapterJSP.class);
				}
			} finally {
				if (sModel != null) {
					sModel.releaseFromRead();
				}
			}
		}

		if (debugProjectionPerf) {
			long end = System.currentTimeMillis();
			System.out
					.println("StructuredTextFoldingProviderJSP.getAdapterFactoryJSP: " + (end - start)); //$NON-NLS-1$
		}
		return factory;
	}

	/**
	 * Initialize this provider with the correct document. Assumes projection is
	 * enabled. (otherwise, only install would have been called)
	 */
	public void initialize() {
		if (!isInstalled()) {
			return;
		}

		// clear out old info
		projectionDisabled();

		fDocument = fViewer.getDocument();

		// set projection viewer on new document's adapter factory
		if (fViewer.getProjectionAnnotationModel() != null) {
			ProjectionModelNodeAdapterFactoryJSP factory = getAdapterFactoryJSP(true);
			if (factory != null) {
				factory.addProjectionViewer(fViewer);
			}
			ProjectionModelNodeAdapterFactoryHTML factory2 = getAdapterFactoryHTML(true);
			if (factory2 != null) {
				factory2.addProjectionViewer(fViewer);
			}

			addAllAdapters();
		}
		fProjectionNeedsToBeEnabled = false;
	}

	/**
	 * Associate a ProjectionViewer with this IStructuredTextFoldingProvider
	 * 
	 * @param viewer -
	 *            assumes not null
	 */
	public void install(ProjectionViewer viewer) {
		// uninstall before trying to install new viewer
		if (isInstalled()) {
			uninstall();
		}
		fViewer = viewer;
		fViewer.addProjectionListener(this);
		fViewer.addTextInputListener(this);
	}

	private boolean isInstalled() {
		return fViewer != null;
	}

	public void projectionDisabled() {
		ProjectionModelNodeAdapterFactoryJSP factory = getAdapterFactoryJSP(false);
		if (factory != null) {
			factory.removeProjectionViewer(fViewer);
		}
		ProjectionModelNodeAdapterFactoryHTML factory2 = getAdapterFactoryHTML(false);
		if (factory2 != null) {
			factory2.removeProjectionViewer(fViewer);
		}

		// clear out all annotations
		if (fViewer.getProjectionAnnotationModel() != null) {
			fViewer.getProjectionAnnotationModel().removeAllAnnotations();
		}

		removeAllAdapters();

		fDocument = null;
		fProjectionNeedsToBeEnabled = false;
	}

	public void projectionEnabled() {
		initialize();
	}

	/**
	 * Removes an adapter from node and its children
	 * 
	 * @param node
	 * @param level
	 */
	private void removeAdapterFromNodeAndChildren(Node node, int level) {
		if (node instanceof INodeNotifier) {
			INodeNotifier notifier = (INodeNotifier) node;

			// try and get the adapter for the current node and remove it
			INodeAdapter adapter = notifier
					.getExistingAdapter(ProjectionModelNodeAdapterJSP.class);
			if (adapter != null) {
				notifier.removeAdapter(adapter);
			}

			INodeAdapter adapter2 = notifier
					.getExistingAdapter(ProjectionModelNodeAdapterHTML.class);
			if (adapter2 != null) {
				notifier.removeAdapter(adapter2);
			}

			Node nextChild = node.getFirstChild();
			while (nextChild != null) {
				Node childNode = nextChild;
				nextChild = childNode.getNextSibling();

				removeAdapterFromNodeAndChildren(childNode, level + 1);
			}
		}
	}

	/**
	 * Goes through every node and removes adapter from each for cleanup
	 * purposes
	 */
	private void removeAllAdapters() {
		long start = System.currentTimeMillis();

		if (fDocument != null) {
			IStructuredModel sModel = null;
			try {
				sModel = StructuredModelManager.getModelManager()
						.getExistingModelForRead(fDocument);
				if (sModel != null) {
					int startOffset = 0;
					IndexedRegion startNode = sModel
							.getIndexedRegion(startOffset);
					if (startNode instanceof Node) {
						Node nextSibling = (Node) startNode;
						while (nextSibling != null) {
							Node currentNode = nextSibling;
							nextSibling = currentNode.getNextSibling();

							removeAdapterFromNodeAndChildren(currentNode, 0);
						}
					}
				}
			} finally {
				if (sModel != null) {
					sModel.releaseFromRead();
				}
			}
		}

		if (debugProjectionPerf) {
			long end = System.currentTimeMillis();
			System.out
					.println("StructuredTextFoldingProviderJSP.addAllAdapters: " + (end - start)); //$NON-NLS-1$
		}
	}

	public void inputDocumentAboutToBeChanged(IDocument oldInput,
			IDocument newInput) {
		// if folding is enabled and new document is going to be a totally
		// different document, disable projection
		if (fDocument != null && fDocument != newInput) {
			// disable projection and disconnect everything
			projectionDisabled();
			fProjectionNeedsToBeEnabled = true;
		}
	}

	public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
		// if projection was previously enabled before input document changed
		// and new document is different than old document
		if (fProjectionNeedsToBeEnabled && fDocument == null
				&& newInput != null) {
			projectionEnabled();
			fProjectionNeedsToBeEnabled = false;
		}
	}

	/**
	 * Disconnect this IStructuredTextFoldingProvider from projection viewer
	 */
	public void uninstall() {
		if (isInstalled()) {
			projectionDisabled();

			fViewer.removeProjectionListener(this);
			fViewer.removeTextInputListener(this);
			fViewer = null;
		}
	}
}
