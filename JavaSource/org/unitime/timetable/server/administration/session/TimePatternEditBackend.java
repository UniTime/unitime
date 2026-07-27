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
package org.unitime.timetable.server.administration.session;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.Transaction;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.client.admin.TimePatternsPage.TimePatternEditRequest;
import org.unitime.timetable.gwt.client.admin.TimePatternsPage.TimePatternEditResponse;
import org.unitime.timetable.gwt.client.admin.TimePatternsPage.TimePatternInterface;
import org.unitime.timetable.gwt.client.admin.TimePatternsPage.TimePatternInterface.Type;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePattern.TimePatternType;
import org.unitime.timetable.model.TimePatternDays;
import org.unitime.timetable.model.TimePatternTime;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.TimePatternDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

@GwtRpcImplements(TimePatternEditRequest.class)
public class TimePatternEditBackend implements GwtRpcImplementation<TimePatternEditRequest, TimePatternEditResponse> {
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);

	@Override
	public TimePatternEditResponse execute(TimePatternEditRequest request, SessionContext context) {
		context.checkPermission(Right.TimePatterns);
		switch (request.getOperation()) {
		case ADD:
			TimePatternEditResponse addResponse = new TimePatternEditResponse();
			addResponse.setSessionId(context.getUser().getCurrentAcademicSessionId());
			addResponse.setSessionName(context.getUser().getCurrentAuthority().getQualifiers("Session").get(0).getQualifierLabel());
			addResponse.setPattern(new TimePatternInterface());
			addResponse.getPattern().setFirstDayOfWeek(ApplicationProperty.TimePatternFirstDayOfWeek.intValue());
			
			setupLookups(context, addResponse, null);
			return addResponse;
		case EDIT:
			TimePatternEditResponse response = new TimePatternEditResponse();
			response.setSessionId(context.getUser().getCurrentAcademicSessionId());
			response.setSessionName(context.getUser().getCurrentAuthority().getQualifiers("Session").get(0).getQualifierLabel());
			
			TimePattern pattern = TimePatternDAO.getInstance().get(request.getPatternId());
			if (pattern == null)
				throw new GwtRpcException(MSG.errorDoesNotExists(MSG.columnDatePattern()));
			TimePatternInterface tp = new TimePatternInterface();
			tp.setFirstDayOfWeek(ApplicationProperty.TimePatternFirstDayOfWeek.intValue());
			tp.setPatternId(pattern.getUniqueId());
			tp.setName(pattern.getName());
			tp.setType(Type.values()[pattern.getType()]);
			tp.setNbrMtgs(pattern.getNrMeetings());
			tp.setMinPerMtg(pattern.getMinPerMtg());
			tp.setSlotsPerMtg(pattern.getSlotsPerMtg());
			tp.setBreakTime(pattern.getBreakTime());
			tp.setVisible(pattern.isVisible());;
			for (TimePatternDays days: pattern.getDays())
				tp.addDay(days.getDayCode());
			for (TimePatternTime time: pattern.getTimes())
				tp.addStart(time.getStartSlot());
			for (Department dept: pattern.getDepartments())
				tp.addDepartmentId(dept.getUniqueId());
			tp.setCanEdit(pattern.isEditable());
			response.setCanDelete(!pattern.isUsed());
		
			response.setPattern(tp);
			
			setupLookups(context, response, pattern);
			return response;
		case DELETE:
			deletePattern(context, request.getPatternId());
			return null;
		case SAVE:
			TimePattern other = TimePattern.findByName(context.getUser().getCurrentAcademicSessionId(), request.getPattern().getName());
			if (other != null && !other.getUniqueId().equals(request.getPatternId()))
				throw new GwtRpcException(MSG.errorAlreadyExists(request.getPattern().getName()));
			
			TimePatternEditResponse saveResponse = new TimePatternEditResponse();
			saveResponse.setSessionId(context.getUser().getCurrentAcademicSessionId());
			saveResponse.setSessionName(context.getUser().getCurrentAuthority().getQualifiers("Session").get(0).getQualifierLabel());
			saveResponse.setPattern(new TimePatternInterface());
			saveResponse.getPattern().setPatternId(savePattern(context, request.getPattern()));
			
			return saveResponse;
		case ASSIGN_DEPTS:
			TimePatternEditResponse assgnDeptResponse = new TimePatternEditResponse();
			assgnDeptResponse.setLog(assignDepts(context));
			return assgnDeptResponse;
		}
		return null;
	}
	
	protected void setupLookups(SessionContext context, TimePatternEditResponse response, TimePattern pattern) {
		for (Department dept: DepartmentDAO.getInstance().getSession()
    			.createQuery("from Department where session.uniqueId = :sessionId order by deptCode", Department.class)
    			.setParameter("sessionId", context.getUser().getCurrentAcademicSessionId())
    			.list()) {
			if (dept.isExternalManager() || !dept.getSubjectAreas().isEmpty())
				response.addDepartment(dept.getUniqueId(), dept.getLabel());
		}
		if (pattern != null)
			for (Department dept: pattern.getDepartments())
				if (response.getDepartment(dept.getUniqueId()) == null)
					response.addDepartment(dept.getUniqueId(), dept.getLabel());
		if (response.hasDepartments())
			Collections.sort(response.getDepartments());
	}
	
	protected void deletePattern(SessionContext context, Long patternId) {
        org.hibernate.Session hibSession = TimetableManagerDAO.getInstance().getSession();
        Transaction tx = null;
        
        try {
        	tx = hibSession.beginTransaction();
        	
        	TimePattern tp = (TimePatternDAO.getInstance()).get(patternId, hibSession);
        	
        	if (tp.isUsed())
        		throw new GwtRpcException(MSG.hintTimePatternUsed());
        	
            ChangeLog.addChange(
                    hibSession, 
                    context, 
                    tp, 
                    ChangeLog.Source.TIME_PATTERN_EDIT, 
                    ChangeLog.Operation.DELETE, 
                    null, 
                    null);
        	
        	for (Department d: tp.getDepartments()) {
        		d.getTimePatterns().remove(tp);
        		hibSession.merge(d);
        	}

            hibSession.merge(tp);
    		hibSession.remove(tp);

           	tx.commit();
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw e;
        }
	}
	
	protected Long savePattern(SessionContext context, TimePatternInterface pattern) {
        Transaction tx = null;
        org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
        Long sessionId = context.getUser().getCurrentAcademicSessionId();
        Long ret = null;
        
        try {
            tx = hibSession.beginTransaction();
            
            TimePattern tp = null;
            if (pattern.getPatternId() == null) {
            	tp = new TimePattern();
            	tp.setSession(SessionDAO.getInstance().get(sessionId, hibSession));
            	tp.setTimes(new HashSet<TimePatternTime>());
            	tp.setDays(new HashSet<TimePatternDays>());
            	tp.setDepartments(new HashSet<Department>());
            } else {
            	tp = TimePatternDAO.getInstance().get(pattern.getPatternId(), hibSession);
            }
            
            tp.setName(pattern.getName());
    		tp.setVisible(pattern.isVisible());
    		tp.setType(pattern.getType().ordinal());
    		tp.setBreakTime(pattern.getBreakTime());
            if (pattern.getPatternId() == null || tp.isEditable()) {
            	tp.setMinPerMtg(pattern.getMinPerMtg());
    			tp.setNrMeetings(pattern.getNbrMtgs());
    			tp.setSlotsPerMtg(pattern.getSlotsPerMtg());
    			
    			Map<Integer, TimePatternTime> remainingTimes = new HashMap<Integer, TimePatternTime>();
    			for (TimePatternTime time: tp.getTimes())
    				remainingTimes.put(time.getStartSlot(), time);
    			if (pattern.hasStarts())
    				for (Integer start: pattern.getStarts()) {
    					if (remainingTimes.remove(start) == null) {
    						TimePatternTime time = new TimePatternTime();
    						time.setStartSlot(start);
    						tp.addToTimes(time);
    					}
    				}
    			for (TimePatternTime time: remainingTimes.values())
    				tp.getTimes().remove(time);
    			
    			Map<Integer, TimePatternDays> remainingDays = new HashMap<Integer, TimePatternDays>();
    			for (TimePatternDays days: tp.getDays())
    				remainingDays.put(days.getDayCode(), days);
    			if (pattern.hasDays())
    				for (Integer dayCode: pattern.getDays()) {
    					if (remainingDays.remove(dayCode) == null) {
    						TimePatternDays days = new TimePatternDays();
    						days.setDayCode(dayCode);
    						tp.addToDays(days);
    					}
    				}
    			for (TimePatternDays days: remainingDays.values())
    				tp.getDays().remove(days);
            }
            
        	if (pattern.getPatternId() == null)
        		hibSession.persist(tp);
        	ret = tp.getUniqueId();
        	
        	Set<Department> remainingDepartments = new HashSet<Department>(tp.getDepartments());
        	if (pattern.hasDepartmentIds() && (tp.getTimePatternType() == TimePatternType.Extended || tp.getTimePatternType() == TimePatternType.ExactTime))
        		for (Long deptId: pattern.getDepartmentIds()) {
        			Department dept = DepartmentDAO.getInstance().get(deptId, hibSession);
        			if (!remainingDepartments.remove(dept)) {
        				tp.addToDepartments(dept);
        				dept.addToTimePatterns(tp);
        				hibSession.merge(dept);
        			}
        		}
        	for (Department dept: remainingDepartments) {
        		tp.getDepartments().remove(dept);
        		dept.getTimePatterns().remove(tp);
        		hibSession.merge(dept);
        	}
        	hibSession.merge(tp);
        	
            ChangeLog.addChange(
                    hibSession, 
                    context, 
                    tp, 
                    ChangeLog.Source.TIME_PATTERN_EDIT, 
                    (pattern.getPatternId() == null ? ChangeLog.Operation.CREATE : ChangeLog.Operation.UPDATE), 
                    null, 
                    null);
            
            tx.commit() ;
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw e;
        }
        return ret;
	}
	
	protected byte[] assignDepts(SessionContext context) {
        Transaction tx = null;
        PrintWriter out = null;
        org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
        Long sessionId = context.getUser().getCurrentAcademicSessionId();
        StringWriter log = new StringWriter();
        try {
            tx = hibSession.beginTransaction();
            
        	out = new PrintWriter(log);
            
        	TreeSet<TimePattern> allTimePatterns = new TreeSet<TimePattern>(TimePattern.findAll(context.getUser().getCurrentAcademicSessionId(), null));
        	for (TimePattern tp: allTimePatterns) {
        		
        		if (!tp.isExtended()) continue;
        		
        		out.println("Checking "+tp.getName()+" ...");
        		
        		List<TimePref> timePrefs =
   					hibSession.
   					createQuery("select distinct p from TimePref as p inner join p.timePattern as tp where tp.uniqueId=:uniqueId", TimePref.class).
   					setParameter("uniqueId", tp.getUniqueId()).list();
        		
        		HashSet<Department> depts = new HashSet<Department>();
        		
        		for (TimePref timePref: timePrefs) {
        			if (timePref.getOwner() instanceof Class_) {
        				Class_ c = (Class_)timePref.getOwner();
        				if (!c.getSession().getUniqueId().equals(sessionId)) continue;
        				depts.add(c.getManagingDept());
        			} else if (timePref.getOwner() instanceof SchedulingSubpart) {
        				SchedulingSubpart s = (SchedulingSubpart)timePref.getOwner();
        				if (!s.getSession().getUniqueId().equals(sessionId)) continue;
        				depts.add(s.getManagingDept());
        			}
        		}
        		
        		out.println("  -- departments: "+depts);

        		boolean added = false;
        		for (Department d: depts) {
        			if (d.isExternalManager()) {
        				/*
        				if (dp.getDepartments().contains(d)) {
        					dp.getDepartments().remove(d);
        					d.getTimePatterns().remove(dp);
        					hibSession.saveOrUpdate(d);
            				out.println("    -- department "+d+" removed from "+dp.getName());
            				added=true;
        				}*/
        				continue;
        			}
        			if (!tp.getDepartments().contains(d)) {
        				tp.getDepartments().add(d);
        				d.getTimePatterns().add(tp);
        				hibSession.merge(d);
        				out.println("    -- department "+d+" added to "+tp.getName());
        				added = true;
        			}
        		}
        		if (added) {
        			hibSession.merge(tp);
                    ChangeLog.addChange(
                            hibSession, 
                            context, 
                            tp, 
                            ChangeLog.Source.TIME_PATTERN_EDIT, 
                            ChangeLog.Operation.UPDATE, 
                            null, 
                            null);
        		}
        	}

        	out.flush(); out.close(); out = null;
            
            tx.commit();
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw new GwtRpcException(e.getMessage(), e);
        }
        return log.getBuffer().toString().getBytes();
	}

}
