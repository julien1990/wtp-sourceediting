/*
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *   IBM - Initial API and implementation
 * 
 */
package org.eclipse.wst.sse.ui.internal.contentassist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.wst.sse.ui.internal.ExtendedConfigurationBuilder;

public class StructuredContentAssistant extends ContentAssistant {
	private static final String CONTENT_ASSIST_PROCESSOR_EXTENDED_ID = "contentassistprocessor"; //$NON-NLS-1$

	/**
	 * personal list of content assist processors
	 */
	private Map fProcessors;
	/**
	 * list of partition types where extended processors have been installed
	 */
	private List fInstalledExtendedContentTypes;

	/**
	 * Each set content assist processor is placed inside a
	 * CompoundContentAssistProcessor which allows multiple processors per
	 * partition type
	 * 
	 * @param processor
	 *            the content assist processor to register, or
	 *            <code>null</code> to remove an existing one
	 * @param contentType
	 *            the content type under which to register
	 */
	public void setContentAssistProcessor(IContentAssistProcessor processor, String partitionType) {
		if (fProcessors == null)
			fProcessors = new HashMap();

		CompoundContentAssistProcessor compoundProcessor = (CompoundContentAssistProcessor) getContentAssistProcessor(partitionType);

		// if processor is null, you want to remove all processors of
		// contentType
		if (processor == null && compoundProcessor != null) {
			compoundProcessor.dispose();
			fProcessors.remove(partitionType);
			compoundProcessor = null;
		}
		if (processor != null) {
			// create a new compoundprocess if there already isnt one
			if (compoundProcessor == null) {
				compoundProcessor = new CompoundContentAssistProcessor();
			}
			// add processor to compound processor
			compoundProcessor.add(processor);
			// add compound procesor to processors list (will replace old one,
			// even if same instance)
			fProcessors.put(partitionType, compoundProcessor);
		}
		super.setContentAssistProcessor(compoundProcessor, partitionType);
	}

	/**
	 * Returns the content assist processor to be used for the given content
	 * type. Also installs any content assist processors that were added by
	 * extension point.
	 * 
	 * @param contentType
	 *            the type of the content for which this content assistant is
	 *            to be requested
	 * @return an instance content assist processor or <code>null</code> if
	 *         none exists for the specified content type
	 */
	public IContentAssistProcessor getContentAssistProcessor(String partitionType) {
		if (fInstalledExtendedContentTypes == null || !fInstalledExtendedContentTypes.contains(partitionType)) {
			// get extended content assist processors that have not already
			// been set
			List processors = ExtendedConfigurationBuilder.getInstance().getConfigurations(CONTENT_ASSIST_PROCESSOR_EXTENDED_ID, partitionType);
			if (processors != null && !processors.isEmpty()) {
				Iterator iter = processors.iterator();
				while (iter.hasNext()) {
					IContentAssistProcessor processor = (IContentAssistProcessor) iter.next();
					setContentAssistProcessor(processor, partitionType);
				}
				// add partition type to list of extended partition types
				// installed
				if (fInstalledExtendedContentTypes == null)
					fInstalledExtendedContentTypes = new ArrayList();
				fInstalledExtendedContentTypes.add(partitionType);
			}
		}

		IContentAssistProcessor processor = super.getContentAssistProcessor(partitionType);
		return processor;
	}

	public void uninstall() {
		// dispose of all content assist processors
		if (fProcessors != null && !fProcessors.isEmpty()) {
			Collection collection = fProcessors.values();
			Iterator iter = collection.iterator();
			while (iter.hasNext()) {
				((CompoundContentAssistProcessor) iter.next()).dispose();
			}
			fProcessors.clear();
		}
		super.uninstall();
	}
}
