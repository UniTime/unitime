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
package org.unitime.timetable.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unitime.timetable.gwt.client.widgets.CourseNumbersSuggestBox.SuggestionInterface;
import org.unitime.timetable.gwt.client.widgets.CourseNumbersSuggestBox.SuggestionRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.security.SessionContext;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(SuggestionRpcRequest.class)
public class CourseNumbersSuggestionsBackend implements GwtRpcImplementation<SuggestionRpcRequest, GwtRpcResponseList<SuggestionInterface>> {

	@Override
	public GwtRpcResponseList<SuggestionInterface> execute(SuggestionRpcRequest request, SessionContext context) {
        Map<String, String> params = new HashMap<String, String>();
        for (String p: request.getConfiguration().split(";")) {
        	if (p.trim().isEmpty()) continue;
			String name = p.substring(0, p.indexOf('=')).trim();
			String value = p.substring(p.indexOf('=') + 1).trim();
			params.put(name, value);
		}
        
        org.hibernate.Session hibSession = CourseOfferingDAO.getInstance().getSession();
        
        GwtRpcResponseList<SuggestionInterface> result = new GwtRpcResponseList<SuggestionInterface>();
        
        if (params.containsKey("subjectAbbv")) {
        	Long sessionId = null;
        	try {
        		if (params.containsKey("sessionId"))
        			sessionId = Long.parseLong(params.get("sessionId")); 
        	} catch (NumberFormatException e) {}
        	if (sessionId == null)
        		sessionId = (context.isAuthenticated() ? context.getUser().getCurrentAcademicSessionId() : null);
        	if (sessionId != null)
            	for (CourseOffering co: (List<CourseOffering>)hibSession.createQuery(
            			"select co from CourseOffering co " + 
            			"where co.subjectArea.session.uniqueId = :sessionId and co.subjectArea.subjectAreaAbbreviation = :subjectAbbv " +
            			("include".equals(params.get("notOffered")) ? "" : "and co.instructionalOffering.notOffered = false ") +
            			("exclude".equals(params.get("crossListed")) ? "and co.isControl = true " : "") +
            			"and co.courseNbr like :q order by co.courseNbr")
            			.setLong("sessionId", sessionId)
            			.setString("subjectAbbv", params.get("subjectAbbv"))
            			.setString("q", request.getQuery() + "%")
            			.setCacheable(true).setMaxResults(request.getLimit()).list()) {
            		result.add(new SuggestionInterface(co.getCourseNumberWithTitle(), co.getCourseNbr()));
            	}
        }
        
        if (params.containsKey("subjectId")) {
        	Long subjectId = null;
        	try {
        		subjectId = Long.parseLong(params.get("subjectId"));
        	} catch (NumberFormatException e) {}
        	if (subjectId != null) {
            	for (CourseOffering co: (List<CourseOffering>)hibSession.createQuery(
            			"select co from CourseOffering co " + 
            			"where co.subjectArea.uniqueId = :subjectId " +
            			("include".equals(params.get("notOffered")) ? "" : "and co.instructionalOffering.notOffered = false ") +
            			("exclude".equals(params.get("crossListed")) ? "and co.isControl = true " : "") +
            			"and co.courseNbr like :q order by co.courseNbr")
            			.setLong("subjectId", subjectId)
            			.setString("q", request.getQuery() + "%")
            			.setCacheable(true).setMaxResults(request.getLimit()).list()) {
            		result.add(new SuggestionInterface(co.getCourseNumberWithTitle(), co.getCourseNbr()));
            	}
        	}
        }
        
        return result;
        
        /*
        
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
        */
	}

}
