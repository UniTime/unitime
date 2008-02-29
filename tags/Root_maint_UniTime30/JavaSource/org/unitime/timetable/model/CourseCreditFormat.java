/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.model;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.hibernate.criterion.Order;
import org.unitime.timetable.model.base.BaseCourseCreditFormat;
import org.unitime.timetable.model.dao.CourseCreditFormatDAO;




public class CourseCreditFormat extends BaseCourseCreditFormat {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CourseCreditFormat () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CourseCreditFormat (Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public CourseCreditFormat (
		Long uniqueId,
		java.lang.String reference) {

		super (
			uniqueId,
			reference);
	}

/*[CONSTRUCTOR MARKER END]*/

	public static String COURSE_CREDIT_FORMAT_ATTR_NAME = "courseCreditFormatList";
	
	public static Vector courseCreditFormatList = null;
	
	public synchronized static Vector getCourseCreditFormatList(boolean refresh) {
		if (courseCreditFormatList != null && !refresh){
			return(courseCreditFormatList);
		}
		
		CourseCreditFormatDAO ccfDao = new CourseCreditFormatDAO();
		Vector orderList = new Vector();
        orderList.addElement(Order.asc("label"));
        
        List l = ccfDao.findAll(orderList);
		courseCreditFormatList = new Vector(l);
        return(courseCreditFormatList);
	}
	
	public static CourseCreditFormat getCourseCreditForReference(String referenceString){
		if (referenceString == null || referenceString.length() == 0){
			return(null);
		}
		CourseCreditFormat ccf = null;
		for(Iterator it = getCourseCreditFormatList(false).iterator(); it.hasNext(); ){
			ccf = (CourseCreditFormat) it.next();
			if (referenceString.equals(ccf.getReference())){
				return(ccf);
			}
		}
		return(null);
	}
	
	public static CourseCreditFormat getCourseCreditForUniqueId(Integer uniqueId){
		if (uniqueId == null){
			return(null);
		}
		CourseCreditFormat ccf = null;
		for(Iterator it = getCourseCreditFormatList(false).iterator(); it.hasNext(); ){
			ccf = (CourseCreditFormat) it.next();
			if (uniqueId.equals(ccf.getUniqueId())){
				return(ccf);
			}
		}
		return(null);
	}
	
	public String getAbbv() {
		if (getAbbreviation()==null) return "";
		return getAbbreviation();
	}
}