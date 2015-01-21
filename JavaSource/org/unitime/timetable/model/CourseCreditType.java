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
import org.unitime.timetable.model.base.BaseCourseCreditType;
import org.unitime.timetable.model.dao.CourseCreditTypeDAO;
import org.unitime.timetable.model.dao.CourseCreditUnitTypeDAO;




/**
 * @author Tomas Muller
 */
public class CourseCreditType extends BaseCourseCreditType {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CourseCreditType () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CourseCreditType (Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

	public static String COURSE_CREDIT_TYPE_ATTR_NAME = "courseCreditTypeList";
	
	public static synchronized List<CourseCreditType> getCourseCreditTypeList() {
		return CourseCreditTypeDAO.getInstance().findAll(Order.asc("label"));
	}
	
	public static CourseCreditType getCourseCreditTypeForReference(String referenceString){
		if (referenceString == null || referenceString.isEmpty()) return null;
		return (CourseCreditType)CourseCreditUnitTypeDAO.getInstance().getSession().createQuery(
				"from CourseCreditType where reference = :reference")
				.setString("reference", referenceString).setMaxResults(1).setCacheable(true).uniqueResult();
	}

	public static CourseCreditType getCourseCreditTypeForUniqueId(Long uniqueId){
		return (uniqueId == null ? null : CourseCreditTypeDAO.getInstance().get(uniqueId));
	}
	
	public String getAbbv() {
		if (getAbbreviation()==null) return "";
		return getAbbreviation();
	}	
}
