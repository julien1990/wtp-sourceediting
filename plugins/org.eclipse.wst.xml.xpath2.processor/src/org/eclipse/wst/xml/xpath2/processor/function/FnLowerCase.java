/*******************************************************************************
 * Copyright (c) 2005, 2009 Andrea Bittau and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.function;

import org.eclipse.wst.xml.xpath2.processor.*;
import org.eclipse.wst.xml.xpath2.processor.types.*;

import java.util.*;

/**
 * <p>
 * Conversion to lower-case function.
 * </p>
 * 
 * <p>
 * Usage: fn:lower-case($arg as xs:string?) as xs:string
 * </p>
 * 
 * <p>
 * This class returns the value of $arg after translating every character to its
 * lower-case correspondent. Every character that does not have an lower-case
 * correspondent is included in the returned value in its original form.
 * </p>
 * 
 * <p>
 * If the value of $arg is the empty sequence, the zero-length string is
 * returned.
 * </p>
 */
public class FnLowerCase extends Function {
	private static Collection _expected_args = null;

	/**
	 * Constructor for FnLowerCase.
	 */
	public FnLowerCase() {
		super(new QName("lower-case"), 1);
	}

	/**
	 * Evaluate the arguments.
	 * 
	 * @param args
	 *            are evaluated.
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return The evaluation of the arguments being converted to lower case.
	 */
	@Override
	public ResultSequence evaluate(Collection args) throws DynamicError {
		return lower_case(args);
	}

	/**
	 * Convert arguments to lower case.
	 * 
	 * @param args
	 *            are converted to lower case.
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return The result of converting the arguments to lower case.
	 */
	public static ResultSequence lower_case(Collection args)
			throws DynamicError {
		Collection cargs = Function.convert_arguments(args, expected_args());

		ResultSequence arg1 = (ResultSequence) cargs.iterator().next();

		ResultSequence rs = ResultSequenceFactory.create_new();

		if (arg1.empty()) {
			rs.add(new XSString(""));
			return rs;
		}

		String str = ((XSString) arg1.first()).value();

		rs.add(new XSString(str.toLowerCase()));

		return rs;
	}

	/**
	 * Calculate the expected arguments.
	 * 
	 * @return The expected arguments.
	 */
	public static Collection expected_args() {
		if (_expected_args == null) {
			_expected_args = new ArrayList();
			_expected_args.add(new SeqType(new XSString(), SeqType.OCC_QMARK));
		}

		return _expected_args;
	}
}
