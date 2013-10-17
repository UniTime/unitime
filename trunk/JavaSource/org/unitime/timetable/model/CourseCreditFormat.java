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

import java.util.List;

import org.hibernate.criterion.Order;
import org.unitime.timetable.model.base.BaseCourseCreditFormat;
import org.unitime.timetable.model.dao.CourseCreditFormatDAO;
import org.unitime.timetable.model.dao.CourseCreditUnitTypeDAO;




/**
 * @author Tomas Muller
 */
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

/*[CONSTRUCTOR MARKER END]*/

	public static String COURSE_CREDIT_FORMAT_ATTR_NAME = "courseCreditFormatList";
	
	public synchronized static List<CourseCreditFormat> getCourseCreditFormatList() {
		return CourseCreditFormatDAO.getInstance().findAll(Order.asc("label"));
	}
	
	public static CourseCreditFormat getCourseCreditForReference(String referenceString){
		if (referenceString == null || referenceString.isEmpty()) return null;
		return (CourseCreditFormat)CourseCreditUnitTypeDAO.getInstance().getSession().createQuery(
				"from CourseCreditFormat where reference = :reference")
				.setString("reference", referenceString).setMaxResults(1).setCacheable(true).uniqueResult();
	}
	
	public static CourseCreditFormat getCourseCreditForUniqueId(Long uniqueId) {
		return (uniqueId == null ? null : CourseCreditFormatDAO.getInstance().get(uniqueId));
	}
	
	public String getAbbv() {
		if (getAbbreviation()==null) return "";
		return getAbbreviation();
	}
}
