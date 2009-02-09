/*******************************************************************************
 * Copyright (c) 2005, 2009 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0 
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.types;

import org.w3c.dom.*;
import org.eclipse.wst.xml.xpath2.processor.*;

/**
 * A representation of the CommentType datatype
 */
public class CommentType extends NodeType {
	private Comment _value;

	/**
	 * Initialise according to the supplied parameters
	 * 
	 * @param v
	 *            The comment being represented
	 * @param doc_order
	 *            The document order
	 */
	public CommentType(Comment v, int doc_order) {
		super(v, doc_order);
		_value = v;
	}

	/**
	 * Retrieves the datatype's full pathname
	 * 
	 * @return "comment" which is the datatype's full pathname
	 */
	@Override
	public String string_type() {
		return "comment";
	}

	/**
	 * Retrieves a String representation of the comment being stored
	 * 
	 * @return String representation of the comment being stored
	 */
	@Override
	public String string_value() {
		return _value.getNodeValue();
	}

	/**
	 * Creates a new ResultSequence consisting of the comment stored
	 * 
	 * @return New ResultSequence consisting of the comment stored
	 */
	@Override
	public ResultSequence typed_value() {
		ResultSequence rs = ResultSequenceFactory.create_new();

		rs.add(new XSString(_value.getData()));

		return rs;
	}

	/**
	 * Unsupported method for this node.
	 * 
	 * @return null
	 */
	@Override
	public QName node_name() {
		return null;
	}
}
