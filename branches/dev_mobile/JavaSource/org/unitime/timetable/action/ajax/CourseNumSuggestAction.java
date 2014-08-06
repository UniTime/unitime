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
package org.unitime.timetable.action.ajax;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.hibernate.FlushMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.util.Constants;


import fr.improve.struts.taglib.layout.suggest.MultipleSuggestAction;

/**
 * MyEclipse Struts Creation date: 01-11-2006
 * 
 * XDoclet definition:
 * 
 * @struts:action scope="request"
 *
 * @author Tomas Muller
 */
@Service("/getCourseNumbers")
public class CourseNumSuggestAction extends MultipleSuggestAction {
	
	@Autowired SessionContext sessionContext;

    // --------------------------------------------------------- Methods

    /**
     * Retrieve Suggestion List of Course Numbers for a given Subject Area
     */
    public Collection getMultipleSuggestionList(HttpServletRequest request, Map map) {
        
        List result = null;
        
        // Read form variables -- Classes Schedule Screen
        if (map.get("session")!=null && map.get("session") instanceof String &&
            map.get("subjectArea")!=null && map.get("subjectArea") instanceof String &&
            map.get("courseNumber")!=null ) {
            
            StringBuffer query = new StringBuffer();
            query.append("select distinct co.courseNbr ");
            query.append("  from CourseOffering co ");
            query.append(" where co.subjectArea.session.uniqueId = :acadSessionId ");
            query.append("       and co.subjectArea.subjectAreaAbbreviation = :subjectArea");
            query.append("       and co.courseNbr like :courseNbr ");
            query.append(" order by co.courseNbr ");
    
            CourseOfferingDAO cdao = new CourseOfferingDAO();
            Session hibSession = cdao.getSession();
    
            Query q = hibSession.createQuery(query.toString());
            q.setFetchSize(5000);
            q.setCacheable(true);
            q.setFlushMode(FlushMode.MANUAL);
            q.setLong("acadSessionId", Long.parseLong(map.get("session").toString()));
            q.setString("subjectArea", map.get("subjectArea").toString());
            q.setString("courseNbr", map.get("courseNumber").toString() + "%");
    
            result = q.list();
            
            if (result == null)
                result = new ArrayList();

            return result;
        }
        
        // Get Academic Session
        Long acadSessionId = (sessionContext.isAuthenticated() ? sessionContext.getUser().getCurrentAcademicSessionId() : null);
        
        if (acadSessionId == null)
        	return new ArrayList();

        // Read form variables -- Instructional Offerings Screen, Reservations Screen
        if(map.get("subjectAreaId")!=null && map.get("courseNbr")!=null 
        		&& map.get("subjectAreaId").toString().length()>0 && 
        		!Constants.ALL_OPTION_VALUE.equals(map.get("subjectAreaId"))) {
            
	        StringBuffer query = new StringBuffer();
	        query.append("select distinct co.courseNbr ");
	        query.append("  from CourseOffering co ");
	        query.append(" where co.subjectArea.session.uniqueId = :acadSessionId ");
	        query.append(" 		 and co.subjectArea.uniqueId = :subjectAreaId ");
	        query.append(" 		 and co.courseNbr like :courseNbr ");
	        //query.append(" 		 and co.isControl = true ");
	        query.append(" order by co.courseNbr ");
	
	        CourseOfferingDAO cdao = new CourseOfferingDAO();
	        Session hibSession = cdao.getSession();
	
	        Query q = hibSession.createQuery(query.toString());
	        q.setFetchSize(5000);
	        q.setCacheable(true);
	        q.setFlushMode(FlushMode.MANUAL);
	        q.setLong("acadSessionId", acadSessionId);
	        q.setLong("subjectAreaId", Long.valueOf(map.get("subjectAreaId").toString()));
	        q.setString("courseNbr", map.get("courseNbr").toString() + "%");
	
	        result = q.list();
        }
        
        // Read form variables -- Distribution Preferences Screen
        if(map.get("filterSubjectAreaId")!=null && !Constants.BLANK_OPTION_VALUE.equals(map.get("filterSubjectAreaId")) && !Constants.ALL_OPTION_VALUE.equals(map.get("filterSubjectAreaId")) && map.get("filterCourseNbr")!=null ) {
            
	        StringBuffer query = new StringBuffer();
	        query.append("select distinct co.courseNbr ");
	        query.append("  from CourseOffering co ");
	        query.append(" where co.subjectArea.session.uniqueId = :acadSessionId ");
	        query.append(" 		 and co.subjectArea.uniqueId = :subjectAreaId ");
	        query.append(" 		 and co.courseNbr like :courseNbr ");
	        query.append(" 		 and co.isControl = true ");
	        query.append(" 		 and co.instructionalOffering.notOffered = false ");
	        query.append(" order by co.courseNbr ");
	
	        CourseOfferingDAO cdao = new CourseOfferingDAO();
	        Session hibSession = cdao.getSession();
	
	        Query q = hibSession.createQuery(query.toString());
	        q.setFetchSize(5000);
	        q.setCacheable(true);
	        q.setFlushMode(FlushMode.MANUAL);
	        q.setLong("acadSessionId", acadSessionId);
	        q.setLong("subjectAreaId", Long.valueOf(map.get("filterSubjectAreaId").toString()));
	        q.setString("courseNbr", map.get("filterCourseNbr").toString() + "%");
	
	        result = q.list();
        }
        
        // Read form variables -- Classes Screen
        if(map.get("subjectAreaIds")!=null && map.get("subjectAreaIds") instanceof String && map.get("courseNbr")!=null ) {
            
	        StringBuffer query = new StringBuffer();
	        query.append("select distinct co.courseNbr ");
	        query.append("  from CourseOffering co ");
	        query.append(" where co.subjectArea.session.uniqueId = :acadSessionId ");
	        query.append(" 		 and co.subjectArea.uniqueId = :subjectAreaId");
	        query.append(" 		 and co.courseNbr like :courseNbr ");
	        if (!"include".equals(map.get("notOffered")))
	        	query.append(" 		 and co.instructionalOffering.notOffered = false ");
	        //query.append(" 		 and co.isControl = true ");
	        query.append(" order by co.courseNbr ");
	
	        CourseOfferingDAO cdao = new CourseOfferingDAO();
	        Session hibSession = cdao.getSession();
	
	        Query q = hibSession.createQuery(query.toString());
	        q.setFetchSize(5000);
	        q.setCacheable(true);
	        q.setFlushMode(FlushMode.MANUAL);
	        q.setLong("acadSessionId", acadSessionId);
	        q.setLong("subjectAreaId", Long.valueOf(map.get("subjectAreaIds").toString()));
	        q.setString("courseNbr", map.get("courseNbr").toString() + "%");
	
	        result = q.list();
        }
        
        if (result == null)
            result = new ArrayList();

        return result;
    }
}
