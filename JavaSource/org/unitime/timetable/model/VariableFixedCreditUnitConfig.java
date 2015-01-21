/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.timetable.model;

import org.unitime.timetable.model.base.BaseVariableFixedCreditUnitConfig;



/**
 * @author Tomas Muller
 */
public class VariableFixedCreditUnitConfig extends BaseVariableFixedCreditUnitConfig {
	private static final long serialVersionUID = 1L;
	public static String CREDIT_FORMAT = "variableMinMax";

/*[CONSTRUCTOR MARKER BEGIN]*/
	public VariableFixedCreditUnitConfig () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public VariableFixedCreditUnitConfig (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

	public String creditText() {
		StringBuffer sb = new StringBuffer();
		sb.append(sCreditFormat.format(this.getMinUnits()));
		sb.append(" or ");
		sb.append(sCreditFormat.format(this.getMaxUnits()));
		sb.append(" ");
		sb.append(this.getCreditUnitType().getLabel());
		sb.append(" of ");
		sb.append(this.getCreditType().getLabel());
		return(sb.toString());
	}
	
	public String creditAbbv() {
		return (getCreditFormatAbbv()+" "+sCreditFormat.format(getMinUnits())+","+sCreditFormat.format(getMaxUnits())+" "+getCreditUnitType().getAbbv()+" "+getCreditType().getAbbv()).trim();
	}
	
	public Object clone() {
		VariableFixedCreditUnitConfig newCreditConfig = new VariableFixedCreditUnitConfig();
		baseClone(newCreditConfig);
		newCreditConfig.setMaxUnits(getMaxUnits());
		newCreditConfig.setMinUnits(getMinUnits());
		return(newCreditConfig);
	}

}
