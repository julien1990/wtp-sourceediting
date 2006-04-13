/*******************************************************************************
 * Copyright (c) 2001, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jens Lukowski/Innoopract - initial renaming/restructuring
 *     
 *******************************************************************************/
package org.eclipse.wst.sse.ui.internal.reconcile.validator;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcileResult;
import org.eclipse.jface.text.reconciler.IReconcileStep;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.ui.internal.IReleasable;
import org.eclipse.wst.sse.ui.internal.Logger;
import org.eclipse.wst.sse.ui.internal.reconcile.DocumentAdapter;
import org.eclipse.wst.sse.ui.internal.reconcile.ReconcileAnnotationKey;
import org.eclipse.wst.sse.ui.internal.reconcile.StructuredTextReconcilingStrategy;
import org.eclipse.wst.sse.ui.internal.reconcile.TemporaryAnnotation;
import org.eclipse.wst.validation.internal.ConfigurationManager;
import org.eclipse.wst.validation.internal.ProjectConfiguration;
import org.eclipse.wst.validation.internal.ValidationRegistryReader;
import org.eclipse.wst.validation.internal.provisional.core.IValidator;


/**
 * Special validator strategy. Runs validator steps contributed via the
 * <code>org.eclipse.wst.sse.ui.extensions.sourcevalidation</code> extension
 * point
 * 
 * @author pavery
 */
public class ValidatorStrategy extends StructuredTextReconcilingStrategy {

	private String[] fContentTypeIds = null;
	private List fMetaData = null;
	/** validator id (as declared in ext point) -> ReconcileStepForValidator * */
	private HashMap fVidToVStepMap = null;
	private ProjectConfiguration fProjectConfiguration = null;

	/*
	 * List of ValidatorMetaDatas of total scope validators that have been run
	 * since beginProcessing() was called.
	 */
	private List fTotalScopeValidatorsAlreadyRun;

	public ValidatorStrategy(ISourceViewer sourceViewer, String contentType) {
		super(sourceViewer);
		fMetaData = new ArrayList();
		fContentTypeIds = calculateParentContentTypeIds(contentType);
		fVidToVStepMap = new HashMap();
	}

	public void addValidatorMetaData(ValidatorMetaData vmd) {
		fMetaData.add(vmd);
	}

	public void beginProcessing() {
		if (fTotalScopeValidatorsAlreadyRun == null)
			fTotalScopeValidatorsAlreadyRun = new ArrayList();
	}

	/**
	 * The content type passed in should be the most specific one. TODO: This
	 * exact method is also in ValidatorMetaData. Should be in a common place.
	 * 
	 * @param contentType
	 * @return
	 */
	private String[] calculateParentContentTypeIds(String contentTypeId) {

		Set parentTypes = new HashSet();

		IContentTypeManager ctManager = Platform.getContentTypeManager();
		IContentType ct = ctManager.getContentType(contentTypeId);
		String id = contentTypeId;

		while (ct != null && id != null) {

			parentTypes.add(id);
			ct = ctManager.getContentType(id);
			if (ct != null) {
				IContentType baseType = ct.getBaseType();
				id = (baseType != null) ? baseType.getId() : null;
			}
		}
		return (String[]) parentTypes.toArray(new String[parentTypes.size()]);
	}

	protected boolean canHandlePartition(String partitionType) {
		ValidatorMetaData vmd = null;
		for (int i = 0; i < fMetaData.size(); i++) {
			vmd = (ValidatorMetaData) fMetaData.get(i);
			if (vmd.canHandlePartitionType(getContentTypeIds(), partitionType))
				return true;
		}
		return false;
	}

	protected boolean containsStep(IReconcileStep step) {
		return fVidToVStepMap.containsValue(step);
	}

	/**
	 * @see org.eclipse.wst.sse.ui.internal.provisional.reconcile.AbstractStructuredTextReconcilingStrategy#createReconcileSteps()
	 */
	public void createReconcileSteps() {
		// do nothing, steps are created
	}

	public void endProcessing() {
		fTotalScopeValidatorsAlreadyRun.clear();
	}

	/**
	 * All content types on which this ValidatorStrategy can run
	 * 
	 * @return
	 */
	public String[] getContentTypeIds() {
		return fContentTypeIds;
	}

	/**
	 * @param tr
	 *            Partition of the region to reconcile.
	 * @param dr
	 *            Dirty region representation of the typed region
	 */
	public void reconcile(ITypedRegion tr, DirtyRegion dr) {

		if (isCanceled())
			return;

		IDocument doc = getDocument();
		// for external files, this can be null
		if (doc == null)
			return;

		String partitionType = tr.getType();

		ValidatorMetaData vmd = null;
		List annotationsToAdd = new ArrayList();
		/*
		 * Loop through all of the relevant validator meta data to find
		 * supporting validators for this partition type. Don't check
		 * this.canHandlePartition() before-hand since it just loops through
		 * and calls vmd.canHandlePartitionType()...which we're already doing
		 * here anyway to find the right vmd.
		 */
		for (int i = 0; i < fMetaData.size() && !isCanceled(); i++) {
			vmd = (ValidatorMetaData) fMetaData.get(i);
			if (vmd.canHandlePartitionType(getContentTypeIds(), partitionType)) {
				/*
				 * Check if validator is enabled according to validation
				 * preferences before attempting to create/use it
				 */
				if (isValidatorEnabled(vmd)) {
					int validatorScope = vmd.getValidatorScope();
					ReconcileStepForValidator validatorStep = null;
					// get step for partition type
					Object o = fVidToVStepMap.get(vmd.getValidatorId());
					if (o != null) {
						validatorStep = (ReconcileStepForValidator) o;
					}
					else {
						// if doesn't exist, create one
						IValidator validator = vmd.createValidator();

						validatorStep = new ReconcileStepForValidator(validator, validatorScope);
						validatorStep.setInputModel(new DocumentAdapter(doc));

						fVidToVStepMap.put(vmd.getValidatorId(), validatorStep);
					}

					if (!fTotalScopeValidatorsAlreadyRun.contains(vmd)) {
						annotationsToAdd.addAll(Arrays.asList(validatorStep.reconcile(dr, dr)));

						if (validatorScope == ReconcileAnnotationKey.TOTAL) {
							// mark this validator as "run"
							fTotalScopeValidatorsAlreadyRun.add(vmd);
						}
					}
				}
			}
		}

		TemporaryAnnotation[] annotationsToRemove = getAnnotationsToRemove(dr);
		if (annotationsToRemove.length + annotationsToAdd.size() > 0)
			smartProcess(annotationsToRemove, (IReconcileResult[]) annotationsToAdd.toArray(new IReconcileResult[annotationsToAdd.size()]));
	}

	public void release() {
		super.release();
		Iterator it = fVidToVStepMap.values().iterator();
		IReconcileStep step = null;
		while (it.hasNext()) {
			step = (IReconcileStep) it.next();
			if (step instanceof IReleasable)
				((IReleasable) step).release();
		}
	}

	/**
	 * @see org.eclipse.wst.sse.ui.internal.reconcile.AbstractStructuredTextReconcilingStrategy#setDocument(org.eclipse.jface.text.IDocument)
	 */
	public void setDocument(IDocument document) {

		super.setDocument(document);

		// validator steps are in "fVIdToVStepMap" (as opposed to fFirstStep >
		// next step etc...)
		Iterator it = fVidToVStepMap.values().iterator();
		IReconcileStep step = null;
		while (it.hasNext()) {
			step = (IReconcileStep) it.next();
			step.setInputModel(new DocumentAdapter(document));
		}
	}

	/**
	 * Checks if validator is enabled according to Validation preferences
	 * 
	 * @param vmd
	 * @return
	 */
	private boolean isValidatorEnabled(ValidatorMetaData vmd) {
		boolean enabled = false;
		ProjectConfiguration configuration = getProjectConfiguration();
		org.eclipse.wst.validation.internal.ValidatorMetaData metadata = ValidationRegistryReader.getReader().getValidatorMetaData(vmd.getValidatorClass());
		if (configuration != null && metadata != null) {
			if (configuration.isBuildEnabled(metadata) || configuration.isManualEnabled(metadata))
				enabled = true;
		}
		return enabled;
	}

	/**
	 * Gets current validation project configuration based on current project
	 * (which is based on current document)
	 * 
	 * @return ProjectConfiguration
	 */
	private ProjectConfiguration getProjectConfiguration() {
		if (fProjectConfiguration == null) {
			IFile file = getFile();
			if (file != null) {
				IProject project = file.getProject();
				if (project != null) {
					try {
						fProjectConfiguration = ConfigurationManager.getManager().getProjectConfiguration(project);
					}
					catch (InvocationTargetException e) {
						Logger.log(Logger.WARNING_DEBUG, e.getMessage(), e);
					}
				}
			}
		}

		return fProjectConfiguration;
	}

	/**
	 * Gets IFile from current document
	 * 
	 * @return IFile
	 */
	private IFile getFile() {
		IStructuredModel model = null;
		IFile file = null;
		try {
			model = StructuredModelManager.getModelManager().getExistingModelForRead(getDocument());
			if (model != null) {
				String baseLocation = model.getBaseLocation();
				// The baseLocation may be a path on disk or relative to the
				// workspace root. Don't translate on-disk paths to
				// in-workspace resources.
				IPath basePath = new Path(baseLocation);
				if (basePath.segmentCount() > 1 && !basePath.toFile().exists()) {
					file = ResourcesPlugin.getWorkspace().getRoot().getFile(basePath);
				}
			}
		}
		finally {
			if (model != null) {
				model.releaseFromRead();
			}
		}
		return file;
	}
}
