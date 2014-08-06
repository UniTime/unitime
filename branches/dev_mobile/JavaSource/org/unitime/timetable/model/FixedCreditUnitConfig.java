/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.model;

import org.unitime.timetable.model.base.BaseFixedCreditUnitConfig;



/**
 * @author Tomas Muller
 */
public class FixedCreditUnitConfig extends BaseFixedCreditUnitConfig {
	private static final long serialVersionUID = 1L;
	public static String CREDIT_FORMAT = "fixedUnit";

/*[CONSTRUCTOR MARKER BEGIN]*/
	public FixedCreditUnitConfig () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public FixedCreditUnitConfig (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

	public String creditText() {
		StringBuffer sb = new StringBuffer();
		sb.append(sCreditFormat.format(this.getFixedUnits()));
		sb.append(" ");
		sb.append(this.getCreditUnitType().getLabel());
		sb.append(" of ");
		sb.append(this.getCreditType().getLabel());
		return(sb.toString());
	}
	
	public String creditAbbv() {
		return (getCreditFormatAbbv()+" "+sCreditFormat.format(getFixedUnits())+" "+getCreditUnitType().getAbbv()+" "+getCreditType().getAbbv()).trim();
	}

	public Object clone() {
		FixedCreditUnitConfig newCreditConfig = new FixedCreditUnitConfig();
		baseClone(newCreditConfig);
		newCreditConfig.setFixedUnits(getFixedUnits());
		return (newCreditConfig);
	}
	
}
