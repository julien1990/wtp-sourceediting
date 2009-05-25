/*******************************************************************************
 * Copyright (c) 2005, 2009 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0
 *     Mukul Gandhi - bug 273760 - wrong namespace for functions and data types
 *     Mukul Gandhi - bug 274952 - implementation of xs:long data type
 *     Mukul Gandhi - bug 275105 - implementation of xs:int data type
 *     Mukul Gandhi - bug 277599 - implementation of xs:nonPositiveInteger data type
 *     Mukul Gandhi - bug 277602 - implementation of xs:negativeInteger data type
 *     Mukul Gandhi - bug 277608 - implementation of xs:short data type
 *     Mukul Gandhi - bug 277609 - implementation of xs:nonNegativeInteger data type
 *     Mukul Gandhi - bug 277629 - implementation of xs:unsignedLong data type
 *     Mukul Gandhi - bug 277632 - implementation of xs:positiveInteger data type
 *     Mukul Gandhi - bug 277639 - implementation of xs:byte data type
 *     Mukul Gandhi - bug 277642 - implementation of xs:unsignedInt data type  
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.function;

import org.eclipse.wst.xml.xpath2.processor.internal.function.ConstructorFL;
import org.eclipse.wst.xml.xpath2.processor.internal.types.*;

/**
 * XML Schema control library support.
 */
public class XSCtrLibrary extends ConstructorFL {
	/**
	 * Path to W3.org XML Schema specification.
	 */
	public static final String XML_SCHEMA_NS = "http://www.w3.org/2001/XMLSchema";

	/**
	 * Constructor for XSCtrLibrary.
	 */
	public XSCtrLibrary() {
		super(XML_SCHEMA_NS);

		// add types here
		add_type(new XSString());
		add_type(new XSBoolean());

		// numeric
		add_type(new XSDecimal());
		add_type(new XSFloat());
		add_type(new XSDouble());
		add_type(new XSInteger());
		add_type(new XSLong());
		add_type(new XSInt());
		add_type(new XSNonPositiveInteger());
		add_type(new XSNegativeInteger());
		add_type(new XSShort());
		add_type(new XSNonNegativeInteger());
		add_type(new XSUnsignedLong());
		add_type(new XSPositiveInteger());
		add_type(new XSByte());
		add_type(new XSUnsignedInt());
		add_type(new XSUnsignedShort());

		// date
		add_type(new XSDateTime());
		add_type(new XSDate());
		add_type(new XSTime());
		add_type(new XSGYearMonth());
		add_type(new XSGYear());
		add_type(new XSGMonthDay());
		add_type(new XSGMonth());
		add_type(new XSGDay());

		add_type(new QName());
		add_type(new XSNCName());
		add_type(new XSAnyURI());
		add_type(new XSYearMonthDuration());
		add_type(new XSDayTimeDuration());
	}
}
