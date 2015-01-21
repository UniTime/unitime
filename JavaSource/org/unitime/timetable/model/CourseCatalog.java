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

import java.util.Iterator;
import java.util.List;

import org.unitime.timetable.model.base.BaseCourseCatalog;
import org.unitime.timetable.model.dao.SubjectAreaDAO;




/**
 * @author Stephanie Schluttenhofer, Tomas Muller
 */
public class CourseCatalog extends BaseCourseCatalog {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CourseCatalog () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CourseCatalog (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

	public static CourseCatalog findCourseFromPreviousSessionInCatalogForSession(CourseOffering courseOffering, Session session){
		if (courseOffering == null || session == null){
			return(null);
		}
		String query = "select distinct cc from CourseCatalog cc";
		query += " where cc.session.uniqueId=:sessionId";
		query += "  and ((cc.subject=:subjectAbbv";
		query += "    and cc.courseNumber=:courseNbr";
		query += "    and (cc.previousSubject is null or cc.previousSubject = cc.subject)";
		query += "    and (cc.previousCourseNumber is null or cc.previousCourseNumber = cc.courseNumber))";
		query += "   or (cc.previousSubject=:subjectAbbv";
		query += "    and cc.previousCourseNumber=:courseNbr))";
		if (courseOffering.getPermId() != null && Integer.parseInt(courseOffering.getPermId()) > 0){
			query += "  and cc.permanentId = '" + courseOffering.getPermId() + "'";
		}
		List l = SubjectAreaDAO.getInstance().getQuery(query)
					.setLong("sessionId", session.getUniqueId())
					.setString("subjectAbbv", courseOffering.getSubjectAreaAbbv())
					.setString("courseNbr", courseOffering.getCourseNbr())
					.list();
		if (l != null && l.size() == 1){
			return((CourseCatalog) l.get(0));
		}
		if (l != null && l.size() > 1){
			CourseCatalog cc = null;
			boolean found = false;
			for (Iterator ccIt = l.iterator(); (ccIt.hasNext() && !found);){
				cc = (CourseCatalog) ccIt.next();
				if (cc.getPreviousSubject().equals(courseOffering.getSubjectAreaAbbv())
						&& cc.getPreviousCourseNumber().equals(courseOffering.getCourseNbr())){
					found = true;
				} else if (cc.getPermanentId() != null && courseOffering.getPermId() != null && cc.getPermanentId().equals(courseOffering.getPermId())){
					found = true;
				}
			}
			if (found){
				return(cc);
			}
		}
		return(null);
	}
	public static CourseCatalog findCourseInCatalogForSession(CourseOffering courseOffering, Session session){
		if (courseOffering == null || session == null){
			return(null);
		}
		String query = "select distinct cc.* from CourseCatalog cc";
		query += " where cc.session.uniqueId=:sessionId";
		query += "  and cc.subject=:subjectAbbv";
		query += "  and cc.courseNumber=:courseNbr";
		List l = SubjectAreaDAO.getInstance().getQuery(query)
					.setLong("sessionId", session.getUniqueId())
					.setString("subjectAbbv", courseOffering.getSubjectAreaAbbv())
					.setString("courseNbr", courseOffering.getCourseNbr())
					.list();
		if (l != null && l.size() == 1){
			return((CourseCatalog) l.get(0));
		}
		if (l != null && l.size() > 1){
			CourseCatalog cc = null;
			boolean found = false;
			for (Iterator ccIt = l.iterator(); (ccIt.hasNext() && !found);){
				cc = (CourseCatalog) ccIt.next();
				if (cc.getSubject().equals(courseOffering.getSubjectAreaAbbv())
						&& cc.getCourseNumber().equals(courseOffering.getCourseNbr())){
					found = true;
				}
			}
			if (found){
				return(cc);
			}
		}
		return(null);
	}

}
