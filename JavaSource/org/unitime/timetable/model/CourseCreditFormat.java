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
