/*******************************************************************************
 * Copyright (c) 2005, 2009 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0
 *     Jesper Moller- bug 275610 - Avoid big time and memory overhead for externals
 *     David Carver  - bug 281186 - implementation of fn:id and fn:idref
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.types;

import org.w3c.dom.*;
import org.eclipse.wst.xml.xpath2.processor.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.ResultSequenceFactory;
import org.eclipse.wst.xml.xpath2.processor.internal.*;

/**
 * A representation of the ProcessingInstruction datatype
 */
public class PIType extends NodeType {
	private ProcessingInstruction _value;

	/**
	 * Initialises according to the supplied parameters
	 * 
	 * @param v
	 *            The processing instruction this node represents
	 * @param doc_order
	 *            The document order
	 */
	public PIType(ProcessingInstruction v) {
		super(v);
		_value = v;
	}

	/**
	 * Retrieves the actual processing instruction this node represents
	 * 
	 * @return Actual processing instruction this node represents
	 */
	public ProcessingInstruction value() {
		return _value;
	}

	/**
	 * Retrieves the datatype's full pathname
	 * 
	 * @return "processing-instruction" which is the datatype's full pathname
	 */
	@Override
	public String string_type() {
		return "processing instruction";
	}

	/**
	 * Retrieves a String representation of the actual processing instruction
	 * stored
	 * 
	 * @return String representation of the actual processing instruction stored
	 */
	@Override
	public String string_value() {
		return _value.getData();
	}

	/**
	 * Creates a new ResultSequence consisting of the processing instruction
	 * stored
	 * 
	 * @return New ResultSequence consisting of the processing instruction
	 *         stored
	 */
	@Override
	public ResultSequence typed_value() {
		ResultSequence rs = ResultSequenceFactory.create_new();

		rs.add(new XSString(string_value()));

		return rs;
	}

	/**
	 * Constructs the node's name
	 * 
	 * @return A QName representation of the node's name
	 */
	@Override
	public QName node_name() {
		QName name = new QName(null, _value.getTarget());

		name.set_namespace(null);

		return name;
	}

	/**
	 * @since 1.1
	 */
	@Override
	public boolean isID() {
		return false;
	}

	/**
	 * @since 1.1
	 */
	@Override
	public boolean isIDREF() {
		return false;
	}
}
