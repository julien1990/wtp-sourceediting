/*******************************************************************************
 * Copyright (c) 2005, 2009 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0
 *     David Carver (STAR) - bug 262765 - Fixed parsing of gMonth values 
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.types;

import org.eclipse.wst.xml.xpath2.processor.DynamicContext;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.ResultSequenceFactory;
import org.eclipse.wst.xml.xpath2.processor.internal.*;
import org.eclipse.wst.xml.xpath2.processor.internal.function.*;

import java.util.*;

/**
 * A representation of the gMonth datatype
 */
public class XSGMonth extends CalendarType implements CmpEq {

	private Calendar _calendar;
	private boolean _timezoned;
	private XSDuration _tz;

	/**
	 * Initializes a representation of the supplied month
	 * 
	 * @param cal
	 *            Calendar representation of the month to be stored
	 * @param tz
	 *            Timezone associated with this month
	 */
	public XSGMonth(Calendar cal, XSDuration tz) {
		_calendar = cal;
		if (tz != null) {
			_timezoned = true;
			_tz = tz;
		}
	}

	/**
	 * Initialises a representation of the current month
	 */
	public XSGMonth() {
		this(new GregorianCalendar(TimeZone.getTimeZone("GMT")), null);
	}

	/**
	 * Retrieves the datatype's name
	 * 
	 * @return "gMonth" which is the datatype's name
	 */
	@Override
	public String type_name() {
		return "gMonth";
	}

	/**
	 * Parses a String representation of a month and constructs a new XSGMonth
	 * representation of it.
	 * 
	 * @param str
	 *            The String representation of the month (and optional timezone)
	 * @return The XSGMonth representation of the supplied date
	 */
	public static XSGMonth parse_gMonth(String str) {

		String startdate = "1972-";
		String starttime = "T00:00:00";
		boolean tz = false;

		int index = str.lastIndexOf('+', str.length());
		
		if (index == -1)
			index = str.lastIndexOf('-');
		if (index == -1)
			index = str.lastIndexOf('Z', str.length());
		if (index != -1) {
			int zIndex = str.lastIndexOf('Z', str.length());
			if (zIndex == -1) {
				if (index > 3) {
					zIndex = index;
				}
			}
			
			String[] split = str.split("-");
			startdate += split[2].replace("Z", "") + "-01";
			
			if (str.indexOf('T') != -1) { 
				if (split.length > 3) {
					String[] timesplit = split[3].split(":");
					if (timesplit.length < 3) {
						starttime = "T";
						for (int cnt = 0; cnt < timesplit.length; cnt++) {
							starttime += timesplit[cnt] + ":";
						}
						starttime += "00";
					} else {
						starttime += timesplit[0] + ":" + timesplit[1] + ":" + timesplit[2];
					}
				}
			}
			startdate = startdate.trim();
			startdate += starttime;

			if (zIndex != -1) {
				startdate += str.substring(zIndex);
				tz = true;
			}
		} else {
			startdate += str + starttime;
		}

		XSDateTime dt = XSDateTime.parseDateTime(startdate);
		if (dt == null)
			return null;

		return new XSGMonth(dt.calendar(), dt.tz());
	}

	/**
	 * Creates a new ResultSequence consisting of the extractable gMonth in the
	 * supplied ResultSequence
	 * 
	 * @param arg
	 *            The ResultSequence from which the gMonth is to be extracted
	 * @return New ResultSequence consisting of the supplied month
	 * @throws DynamicError
	 */
	@Override
	public ResultSequence constructor(ResultSequence arg) throws DynamicError {
		ResultSequence rs = ResultSequenceFactory.create_new();

		if (arg.empty())
			return rs;

		AnyAtomicType aat = (AnyAtomicType) arg.first();
		
		if (!isCastable(aat)) {
			throw DynamicError.cant_cast(null);
		}

		XSGMonth val = castGMonth(aat);

		if (val == null)
			throw DynamicError.cant_cast(null);

		rs.add(val);

		return rs;
	}

	
	private boolean isCastable(AnyAtomicType aat) {
		if (aat instanceof XSString || aat instanceof XSUntypedAtomic) {
			return true;
		}
		
		if (aat instanceof XSTime) {
			return false;
		}
		
		if (aat instanceof XSDate || aat instanceof XSDateTime || 
			aat instanceof XSGMonth) {
			return true;
		}
		
		return false;
	}
	
	private XSGMonth castGMonth(AnyAtomicType aat) {
		if (aat instanceof XSGMonth) {
			XSGMonth gm = (XSGMonth) aat;
			return new XSGMonth(gm.calendar(), gm.tz());
		}
		
		if (aat instanceof XSDate) {
			XSDate date = (XSDate) aat;
			return new XSGMonth(date.calendar(), date.tz());
		}
		
		if (aat instanceof XSDateTime) {
			XSDateTime dateTime = (XSDateTime) aat;
			return new XSGMonth(dateTime.calendar(), dateTime.tz());
		}
		
		return parse_gMonth(aat.string_value());
	}
	
	/**
	 * Retrieves the actual month as an integer
	 * 
	 * @return The actual month as an integer
	 */
	public int month() {
		return _calendar.get(Calendar.MONTH) + 1;
	}

	/**
	 * Check for whether a timezone was specified at creation
	 * 
	 * @return True if a timezone was specified. False otherwise
	 */
	public boolean timezoned() {
		return _timezoned;
	}

	/**
	 * Retrieves a String representation of the stored month
	 * 
	 * @return String representation of the stored month
	 */
	@Override
	public String string_value() {
		String ret = "--";

		ret += XSDateTime.pad_int(month(), 2);

		if (timezoned()) {
			
			int hrs = tz().hours();
			int min = tz().minutes();
			double secs = tz().seconds();
			if (hrs == 0 && min == 0 && secs == 0) {
			  ret += "Z";
			}
			else {
			  String tZoneStr = "";
			  if (tz().negative()) {
				tZoneStr += "-";  
			  }
			  else {
				tZoneStr += "+"; 
			  }
			  tZoneStr += XSDateTime.pad_int(hrs, 2);  
			  tZoneStr += ":";
			  tZoneStr += XSDateTime.pad_int(min, 2);
			  
			  ret += tZoneStr;
			}
		}

		return ret;
	}

	/**
	 * Retrieves the datatype's full pathname
	 * 
	 * @return "xs:gMonth" which is the datatype's full pathname
	 */
	@Override
	public String string_type() {
		return "xs:gMonth";
	}

	/**
	 * Retrieves the Calendar representation of the month stored
	 * 
	 * @return Calendar representation of the month stored
	 */
	public Calendar calendar() {
		return _calendar;
	}

	/**
	 * Equality comparison between this and the supplied representation. This
	 * representation must be of type XSGMonth
	 * 
	 * @param arg
	 *            The XSGMonth to compare with
	 * @return True if the two representations are of the same month. False
	 *         otherwise
	 * @throws DynamicError
	 */
	public boolean eq(AnyType arg, DynamicContext context) throws DynamicError {
		XSGMonth val = (XSGMonth) NumericType.get_single_type(arg,
				XSGMonth.class);

		return calendar().equals(val.calendar());
	}
	
	/**
	 * Retrieves the timezone associated with the date stored
	 * 
	 * @return the timezone associated with the date stored
	 * @since 1.1
	 */
	public XSDuration tz() {
		return _tz;
	}	
	
}
