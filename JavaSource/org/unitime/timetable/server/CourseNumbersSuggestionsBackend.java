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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.client.widgets.CourseNumbersSuggestBox.SuggestionInterface;
import org.unitime.timetable.gwt.client.widgets.CourseNumbersSuggestBox.SuggestionRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.InstructionalOffering.OfferingWaitListMode;
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
        
        String wlFilter = "";
        if (params.containsKey("waitlist")) {
        	if ("W".equals(params.get("waitlist"))) {
    			if (InstructionalOffering.getDefaultWaitListMode() == OfferingWaitListMode.WaitList)
    				wlFilter = " and (co.instructionalOffering.waitlistMode is null or co.instructionalOffering.waitlistMode = 1) and co.instructionalOffering.notOffered = false ";
    			else
    				wlFilter = " and co.instructionalOffering.waitlistMode = 1 and co.instructionalOffering.notOffered = false ";
    		} else if ("N".equals(params.get("waitlist"))) {
    			if (InstructionalOffering.getDefaultWaitListMode() != OfferingWaitListMode.WaitList)
    				wlFilter = " and (co.instructionalOffering.waitlistMode is null or co.instructionalOffering.waitlistMode != 1) and co.instructionalOffering.notOffered = false ";
    			else
    				wlFilter = " and co.instructionalOffering.waitlistMode != 1 and co.instructionalOffering.notOffered = false ";
    		} else if ("R".equals(params.get("waitlist"))) {
    			if (InstructionalOffering.getDefaultWaitListMode() != OfferingWaitListMode.Disabled)
    				wlFilter = " and (co.instructionalOffering.waitlistMode is null or co.instructionalOffering.waitlistMode > 0) and co.instructionalOffering.notOffered = false ";
    			else
    				wlFilter = " and co.instructionalOffering.waitlistMode > 0 and co.instructionalOffering.notOffered = false ";
    		} else if ("X".equals(params.get("waitlist"))) {
    			if (InstructionalOffering.getDefaultWaitListMode() == OfferingWaitListMode.Disabled)
    				wlFilter = " and (co.instructionalOffering.waitlistMode is null or co.instructionalOffering.waitlistMode = 0) and co.instructionalOffering.notOffered = false ";
    			else
    				wlFilter = " and co.instructionalOffering.waitlistMode = 0 and co.instructionalOffering.notOffered = false ";
    		} else if ("Z".equals(params.get("waitlist"))) {
    			if (InstructionalOffering.getDefaultWaitListMode() == OfferingWaitListMode.ReSchedule)
    				wlFilter = " and (co.instructionalOffering.waitlistMode is null or co.instructionalOffering.waitlistMode = 2) and co.instructionalOffering.notOffered = false ";
    			else
    				wlFilter = " and co.instructionalOffering.waitlistMode = 2 and co.instructionalOffering.notOffered = false ";
    		}
        }
        
        if (params.containsKey("subjectAbbv")) {
        	Long sessionId = null;
        	try {
        		if (params.containsKey("sessionId"))
        			sessionId = Long.parseLong(params.get("sessionId")); 
        	} catch (NumberFormatException e) {}
        	if (sessionId == null)
        		sessionId = (context.isAuthenticated() ? context.getUser().getCurrentAcademicSessionId() : null);
        	if (sessionId != null)
            	for (CourseOffering co: hibSession.createQuery(
            			"select co from CourseOffering co " + 
            			"where co.subjectArea.session.uniqueId = :sessionId and co.subjectArea.subjectAreaAbbreviation = :subjectAbbv " +
            			("include".equals(params.get("notOffered")) ? "" : "and co.instructionalOffering.notOffered = false ") +
            			("exclude".equals(params.get("crossListed")) || "0".equals(params.get("crossListed")) ? "and co.isControl = true " : "") +
            			wlFilter + 
            			(ApplicationProperty.CourseOfferingTitleSearch.isTrue() && request.getQuery().length() > 2
            			? "and (co.courseNbr like :q or lower(co.title) like lower('%' || :q))"
            			: "and co.courseNbr like :q") + " order by co.courseNbr", CourseOffering.class)
            			.setParameter("sessionId", sessionId)
            			.setParameter("subjectAbbv", params.get("subjectAbbv"))
            			.setParameter("q", request.getQuery() + "%")
            			.setCacheable(true).setMaxResults(request.getLimit()).list()) {
            		result.add(new SuggestionInterface(co.getCourseNumberWithTitle(), co.getCourseNbr()));
            	}
        }
        
        if (params.containsKey("subjectId")) {
        	List<Long> subjectIds = new ArrayList<Long>();
        	for (String subjectId: params.get("subjectId").split(",")) {
        		try {
        			subjectIds.add(Long.parseLong(subjectId));
            	} catch (NumberFormatException e) {}
        	}
        	if (!subjectIds.isEmpty()) {
            	for (CourseOffering co: hibSession.createQuery(
            			"select co from CourseOffering co " + 
            			"where co.subjectArea.uniqueId in :subjectIds " +
            			("include".equals(params.get("notOffered")) ? "" : "and co.instructionalOffering.notOffered = false ") +
            			("exclude".equals(params.get("crossListed")) || "0".equals(params.get("crossListed")) ? "and co.isControl = true " : "") +
            			wlFilter + 
            			(ApplicationProperty.CourseOfferingTitleSearch.isTrue() && request.getQuery().length() > 2
            			? "and (co.courseNbr like :q or lower(co.title) like lower('%' || :q))"
            			: "and co.courseNbr like :q") + " order by co.courseNbr", CourseOffering.class)
            			.setParameter("subjectIds", subjectIds)
            			.setParameter("q", request.getQuery() + "%")
            			.setCacheable(true).setMaxResults(request.getLimit()).list()) {
            		result.add(new SuggestionInterface(co.getCourseNumberWithTitle(), co.getCourseNbr()));
            	}
        	}
        }
        
        return result;
	}

}
