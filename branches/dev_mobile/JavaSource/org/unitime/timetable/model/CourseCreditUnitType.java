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
import org.unitime.timetable.model.base.BaseCourseCreditUnitType;
import org.unitime.timetable.model.dao.CourseCreditUnitTypeDAO;




/**
 * @author Tomas Muller
 */
public class CourseCreditUnitType extends BaseCourseCreditUnitType {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CourseCreditUnitType () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CourseCreditUnitType (Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

	public static String COURSE_CREDIT_UNIT_TYPE_ATTR_NAME = "courseCreditUnitTypeList";
	
	public static synchronized List<CourseCreditUnitType> getCourseCreditUnitTypeList() {
		return CourseCreditUnitTypeDAO.getInstance().findAll(Order.asc("label"));
	}
	
	public static CourseCreditUnitType getCourseCreditUnitTypeForReference(String referenceString) {
		if (referenceString == null || referenceString.isEmpty()) return null;
		return (CourseCreditUnitType)CourseCreditUnitTypeDAO.getInstance().getSession().createQuery(
				"from CourseCreditUnitType where reference = :reference")
				.setString("reference", referenceString).setMaxResults(1).setCacheable(true).uniqueResult();
	}

	public static CourseCreditUnitType getCourseCreditUnitTypeForUniqueId(Long uniqueId){
		return (uniqueId == null ? null : CourseCreditUnitTypeDAO.getInstance().get(uniqueId));
	}
	
	public String getAbbv() {
		if (getAbbreviation()==null) return "";
		return getAbbreviation();
	}

}
