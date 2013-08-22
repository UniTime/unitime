/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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

import java.util.Iterator;
import java.util.List;

import org.unitime.timetable.model.base.BaseCourseCatalog;
import org.unitime.timetable.model.dao.SubjectAreaDAO;




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
