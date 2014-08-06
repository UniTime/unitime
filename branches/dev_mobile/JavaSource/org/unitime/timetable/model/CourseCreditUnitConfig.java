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

import java.util.Locale;

import org.unitime.timetable.model.base.BaseCourseCreditUnitConfig;



/**
 * @author Stephanie Schluttenhofer, Tomas Muller
 */
public abstract class CourseCreditUnitConfig extends BaseCourseCreditUnitConfig {
	private static final long serialVersionUID = 1L;
	protected static java.text.DecimalFormat sCreditFormat = new java.text.DecimalFormat("0.###",new java.text.DecimalFormatSymbols(Locale.US));
    

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CourseCreditUnitConfig () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CourseCreditUnitConfig (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

	public static CourseCreditUnitConfig createCreditUnitConfigOfFormat(String creditFormat, String creditType, String creditUnitType, Float units, Float maxUnits, Boolean fractionalIncrementsAllowed, Boolean creditAtCourseLevel){
		if (creditFormat == null || creditFormat.length() == 0 | creditType == null | creditUnitType == null || creditType.length() == 0 | creditUnitType.length() == 0){
			return(null);
		}
		CourseCreditType cct = CourseCreditType.getCourseCreditTypeForReference(creditType);
		CourseCreditUnitType ccut = CourseCreditUnitType.getCourseCreditUnitTypeForReference(creditUnitType);
		return(createCreditUnitConfigOfFormat(creditFormat, cct, ccut, units, maxUnits, fractionalIncrementsAllowed, creditAtCourseLevel));
	}
	
	public static CourseCreditUnitConfig createCreditUnitConfigOfFormat(String creditFormat, Long creditType, Long creditUnitType, Float units, Float maxUnits, Boolean fractionalIncrementsAllowed, Boolean creditAtCourseLevel){
		if (creditFormat == null || creditFormat.length() == 0){
			return(null);
		}

		CourseCreditType cct = CourseCreditType.getCourseCreditTypeForUniqueId(creditType);
		CourseCreditUnitType ccut = CourseCreditUnitType.getCourseCreditUnitTypeForUniqueId(creditUnitType);
		
		return(createCreditUnitConfigOfFormat(creditFormat, cct, ccut, units, maxUnits, fractionalIncrementsAllowed, creditAtCourseLevel));
	}
	
	public static CourseCreditUnitConfig createCreditUnitConfigOfFormat(String creditFormat, CourseCreditType creditType, CourseCreditUnitType creditUnitType, Float units, Float maxUnits, Boolean fractionalIncrementsAllowed, Boolean creditAtCourseLevel){
		if (creditFormat == null || creditFormat.length() == 0){
			return(null);
		}
		CourseCreditUnitConfig ccuc = null;
		if (creditFormat.equals(FixedCreditUnitConfig.CREDIT_FORMAT)){
			FixedCreditUnitConfig fcuc = new FixedCreditUnitConfig();
			fcuc.setFixedUnits(units);
			ccuc = fcuc;
		} else if (creditFormat.equals(VariableFixedCreditUnitConfig.CREDIT_FORMAT)){
			VariableFixedCreditUnitConfig vfcuc = new VariableFixedCreditUnitConfig();
			vfcuc.setMinUnits(units);
			vfcuc.setMaxUnits(maxUnits);
			ccuc = vfcuc;
		} else if (creditFormat.equals(VariableRangeCreditUnitConfig.CREDIT_FORMAT)){
			VariableRangeCreditUnitConfig vrcuc = new VariableRangeCreditUnitConfig();
			vrcuc.setMinUnits(units);
			vrcuc.setMaxUnits(maxUnits);
			vrcuc.setFractionalIncrementsAllowed(fractionalIncrementsAllowed);
			ccuc = vrcuc;
		} else if (creditFormat.endsWith(ArrangeCreditUnitConfig.CREDIT_FORMAT)){
			ccuc = new ArrangeCreditUnitConfig();
		} else {
			return(null);
		}
		ccuc.setCourseCreditFormat(CourseCreditFormat.getCourseCreditForReference(creditFormat));
		ccuc.setDefinesCreditAtCourseLevel(creditAtCourseLevel);
		ccuc.setCreditType(creditType);
		ccuc.setCreditUnitType(creditUnitType);
		
		return(ccuc);
	}

	public abstract String creditText();
	public abstract String creditAbbv();
	
	public void setOwner(SchedulingSubpart schedulingSubpart){
		if(!isDefinesCreditAtCourseLevel().booleanValue()){
			setSubpartOwner(schedulingSubpart);
		}
	}
	public void setOwner(CourseOffering courseOffering){
		if(isDefinesCreditAtCourseLevel().booleanValue()){
			setCourseOwner(courseOffering);
		}
	}
	
	public String getCreditFormat() {
		CourseCreditFormat ccf = getCourseCreditFormat();
		return (ccf == null ? null : ccf.getReference());
	}
	
	public String getCreditFormatAbbv() {
		CourseCreditFormat ccf = getCourseCreditFormat();
		return (ccf == null || ccf.getAbbreviation() == null ? "" : ccf.getAbbreviation());
	}
	
	protected void baseClone(CourseCreditUnitConfig newCreditConfig){
		newCreditConfig.setCourseCreditFormat(getCourseCreditFormat());
		newCreditConfig.setCreditType(getCreditType());
		newCreditConfig.setCreditUnitType(getCreditUnitType());
		newCreditConfig.setDefinesCreditAtCourseLevel(isDefinesCreditAtCourseLevel());
	}
	
	public abstract Object clone();
	
	public String toString(){
		return(creditText());
	}
}
