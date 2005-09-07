/*
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *   IBM - Initial API and implementation
 *   Jens Lukowski/Innoopract - initial renaming/restructuring
 * 
 */
package org.eclipse.wst.sse.ui.internal.contentassist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.contentassist.IContentAssistSubjectControl;
import org.eclipse.jface.contentassist.ISubjectControlContentAssistProcessor;
import org.eclipse.jface.contentassist.ISubjectControlContextInformationValidator;
import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.sse.ui.internal.IReleasable;

/**
 * A processor that aggregates the proposals of multiple other processors.
 * When proposals are requested, the contained processors are queried in the
 * order they were added to the compound object. Copied from
 * org.eclipse.jdt.internal.ui.text.CompoundContentAssistProcessor.
 * Modification was made to add a dispose() method.
 */
class CompoundContentAssistProcessor implements IContentAssistProcessor, ISubjectControlContentAssistProcessor {

	private static class WrappedContextInformation implements IContextInformation {
		private IContextInformation fInfo;
		private IContentAssistProcessor fProcessor;

		WrappedContextInformation(IContextInformation info, IContentAssistProcessor processor) {
			fInfo = info;
			fProcessor = processor;
		}

		/*
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object obj) {
			return fInfo.equals(obj);
		}

		/*
		 * @see org.eclipse.jface.text.contentassist.IContextInformation#getContextDisplayString()
		 */
		public String getContextDisplayString() {
			return fInfo.getContextDisplayString();
		}

		/*
		 * @see org.eclipse.jface.text.contentassist.IContextInformation#getImage()
		 */
		public Image getImage() {
			return fInfo.getImage();
		}

		/*
		 * @see org.eclipse.jface.text.contentassist.IContextInformation#getInformationDisplayString()
		 */
		public String getInformationDisplayString() {
			return fInfo.getInformationDisplayString();
		}

		/*
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			return fInfo.hashCode();
		}

		/*
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return fInfo.toString();
		}

		IContentAssistProcessor getProcessor() {
			return fProcessor;
		}
	}

	private static class CompoundContentAssistValidator implements IContextInformationValidator {
		private List fValidators = new ArrayList();

		void add(IContextInformationValidator validator) {
			fValidators.add(validator);
		}

		/*
		 * @see org.eclipse.jface.text.contentassist.IContextInformationValidator#install(org.eclipse.jface.text.contentassist.IContextInformation,
		 *      org.eclipse.jface.text.ITextViewer, int)
		 */
		public void install(IContextInformation info, ITextViewer viewer, int documentPosition) {
			IContextInformationValidator validator = getValidator(info);
			if (validator != null)
				validator.install(info, viewer, documentPosition);
		}

		IContextInformationValidator getValidator(IContextInformation info) {
			if (info instanceof WrappedContextInformation) {
				WrappedContextInformation wrap = (WrappedContextInformation) info;
				return wrap.getProcessor().getContextInformationValidator();
			}

			return null;
		}

		/*
		 * @see org.eclipse.jface.text.contentassist.IContextInformationValidator#isContextInformationValid(int)
		 */
		public boolean isContextInformationValid(int documentPosition) {
			boolean isValid = false;
			for (Iterator it = fValidators.iterator(); it.hasNext();) {
				IContextInformationValidator v = (IContextInformationValidator) it.next();
				isValid |= v.isContextInformationValid(documentPosition);
			}
			return isValid;
		}

	}

	private static class CompoundContentAssistValidatorEx extends CompoundContentAssistValidator implements ISubjectControlContextInformationValidator {

		/*
		 * @see ISubjectControlContextInformationValidator#install(IContextInformation,
		 *      IContentAssistSubjectControl, int)
		 */
		public void install(IContextInformation info, IContentAssistSubjectControl contentAssistSubjectControl, int documentPosition) {
			IContextInformationValidator validator = getValidator(info);
			if (validator instanceof ISubjectControlContextInformationValidator)
				((ISubjectControlContextInformationValidator) validator).install(info, contentAssistSubjectControl, documentPosition);
		}

	}

	private final Set fProcessors = new LinkedHashSet();

	/**
	 * Creates a new instance.
	 */
	public CompoundContentAssistProcessor() {
	}

	/**
	 * Creates a new instance with one child processor.
	 * 
	 * @param processor
	 *            the processor to add
	 */
	public CompoundContentAssistProcessor(IContentAssistProcessor processor) {
		add(processor);
	}

	/**
	 * Adds a processor to this compound processor.
	 * 
	 * @param processor
	 *            the processor to add
	 */
	public void add(IContentAssistProcessor processor) {
		Assert.isNotNull(processor);
		fProcessors.add(processor);
	}

	/**
	 * Removes a processor from this compound processor.
	 * 
	 * @param processor
	 *            the processor to remove
	 */
	public void remove(IContentAssistProcessor processor) {
		fProcessors.remove(processor);
	}

	/**
	 * Creates a new instance and adds all specified processors.
	 * 
	 * @param processors
	 */
	public CompoundContentAssistProcessor(IContentAssistProcessor[] processors) {
		for (int i = 0; i < processors.length; i++) {
			add(processors[i]);
		}
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeCompletionProposals(org.eclipse.jface.text.ITextViewer,
	 *      int)
	 */
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
		List ret = new LinkedList();
		for (Iterator it = fProcessors.iterator(); it.hasNext();) {
			IContentAssistProcessor p = (IContentAssistProcessor) it.next();
			ICompletionProposal[] proposals = p.computeCompletionProposals(viewer, documentOffset);
			if (proposals != null)
				ret.addAll(Arrays.asList(proposals));
		}
		return (ICompletionProposal[]) ret.toArray(new ICompletionProposal[ret.size()]);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The returned objects are wrapper objects around the real information
	 * containers.
	 * </p>
	 * 
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeContextInformation(org.eclipse.jface.text.ITextViewer,
	 *      int)
	 */
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int documentOffset) {
		List ret = new LinkedList();
		for (Iterator it = fProcessors.iterator(); it.hasNext();) {
			IContentAssistProcessor p = (IContentAssistProcessor) it.next();
			IContextInformation[] informations = p.computeContextInformation(viewer, documentOffset);
			if (informations != null)
				for (int i = 0; i < informations.length; i++)
					ret.add(new WrappedContextInformation(informations[i], p));
		}
		return (IContextInformation[]) ret.toArray(new IContextInformation[ret.size()]);
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
	 */
	public char[] getCompletionProposalAutoActivationCharacters() {
		Set ret = new LinkedHashSet();
		for (Iterator it = fProcessors.iterator(); it.hasNext();) {
			IContentAssistProcessor p = (IContentAssistProcessor) it.next();
			char[] chars = p.getCompletionProposalAutoActivationCharacters();
			if (chars != null)
				for (int i = 0; i < chars.length; i++)
					ret.add(new Character(chars[i]));
		}

		char[] chars = new char[ret.size()];
		int i = 0;
		for (Iterator it = ret.iterator(); it.hasNext(); i++) {
			Character ch = (Character) it.next();
			chars[i] = ch.charValue();
		}
		return chars;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationAutoActivationCharacters()
	 */
	public char[] getContextInformationAutoActivationCharacters() {
		Set ret = new LinkedHashSet();
		for (Iterator it = fProcessors.iterator(); it.hasNext();) {
			IContentAssistProcessor p = (IContentAssistProcessor) it.next();
			char[] chars = p.getContextInformationAutoActivationCharacters();
			if (chars != null)
				for (int i = 0; i < chars.length; i++)
					ret.add(new Character(chars[i]));
		}

		char[] chars = new char[ret.size()];
		int i = 0;
		for (Iterator it = ret.iterator(); it.hasNext(); i++) {
			Character ch = (Character) it.next();
			chars[i] = ch.charValue();
		}
		return chars;
	}

	/**
	 * Returns the first non- <code>null</code> error message of any
	 * contained processor, or <code>null</code> if no processor has an
	 * error message.
	 * 
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getErrorMessage()
	 * @return {@inheritDoc}
	 */
	public String getErrorMessage() {
		for (Iterator it = fProcessors.iterator(); it.hasNext();) {
			IContentAssistProcessor p = (IContentAssistProcessor) it.next();
			String err = p.getErrorMessage();
			if (err != null)
				return err;
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The returned validator is a wrapper around the validators provided by
	 * the child processors.
	 * </p>
	 * 
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationValidator()
	 */
	public IContextInformationValidator getContextInformationValidator() {
		boolean hasValidator = false;
		boolean hasExtension = false;
		for (Iterator it = fProcessors.iterator(); it.hasNext();) {
			IContentAssistProcessor p = (IContentAssistProcessor) it.next();
			IContextInformationValidator v = p.getContextInformationValidator();
			if (v instanceof ISubjectControlContextInformationValidator) {
				hasExtension = true;
				break;
			}
			else if (v != null) {
				hasValidator = true;
			}
		}

		CompoundContentAssistValidator validator = null;
		if (hasExtension)
			validator = new CompoundContentAssistValidatorEx();
		else if (hasValidator)
			validator = new CompoundContentAssistValidator();

		if (validator != null)
			for (Iterator it = fProcessors.iterator(); it.hasNext();) {
				IContentAssistProcessor p = (IContentAssistProcessor) it.next();
				IContextInformationValidator v = p.getContextInformationValidator();
				if (v != null)
					validator.add(v);
			}

		return validator;
	}

	/*
	 * @see ISubjectControlContentAssistProcessor#computeCompletionProposals(IContentAssistSubjectControl,
	 *      int)
	 */
	public ICompletionProposal[] computeCompletionProposals(IContentAssistSubjectControl contentAssistSubjectControl, int documentOffset) {
		List ret = new LinkedList();
		for (Iterator it = fProcessors.iterator(); it.hasNext();) {
			Object o = it.next();
			if (o instanceof ISubjectControlContentAssistProcessor) {
				ISubjectControlContentAssistProcessor p = (ISubjectControlContentAssistProcessor) o;
				ICompletionProposal[] proposals = p.computeCompletionProposals(contentAssistSubjectControl, documentOffset);
				if (proposals != null)
					ret.addAll(Arrays.asList(proposals));
			}
		}

		return (ICompletionProposal[]) ret.toArray(new ICompletionProposal[ret.size()]);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The returned objects are wrapper objects around the real information
	 * containers.
	 * </p>
	 * 
	 * @see org.eclipse.jface.contentassist.ISubjectControlContentAssistProcessor#computeContextInformation(org.eclipse.jface.text.contentassist.IContentAssistSubject,
	 *      int)
	 */
	public IContextInformation[] computeContextInformation(IContentAssistSubjectControl contentAssistSubjectControl, int documentOffset) {
		List ret = new LinkedList();
		for (Iterator it = fProcessors.iterator(); it.hasNext();) {
			Object o = it.next();
			if (o instanceof ISubjectControlContentAssistProcessor) {
				ISubjectControlContentAssistProcessor p = (ISubjectControlContentAssistProcessor) o;
				IContextInformation[] informations = p.computeContextInformation(contentAssistSubjectControl, documentOffset);
				if (informations != null)
					for (int i = 0; i < informations.length; i++)
						ret.add(new WrappedContextInformation(informations[i], p));
			}
		}
		return (IContextInformation[]) ret.toArray(new IContextInformation[ret.size()]);
	}

	/**
	 * Dispose of any content assist processors that need disposing
	 */
	public void dispose() {
		// go through list of content assist processors and dispose
		for (Iterator it = fProcessors.iterator(); it.hasNext();) {
			IContentAssistProcessor p = (IContentAssistProcessor) it.next();
			if (p instanceof IReleasable) {
				((IReleasable)p).release();
			}
		}
		fProcessors.clear();
	}
}
