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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.Transaction;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.admin.DatePatternsPage.DatePatternEditRequest;
import org.unitime.timetable.gwt.client.admin.DatePatternsPage.DatePatternEditResponse;
import org.unitime.timetable.gwt.client.admin.DatePatternsPage.DatePatternInterface;
import org.unitime.timetable.gwt.client.admin.DatePatternsPage.DatePatternInterface.Type;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.EventInterface.SessionMonth;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.DatePattern.DatePatternType;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.DatePatternDAO;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.instructor.PatternDatesBackend;
import org.unitime.timetable.util.DateUtils;

@GwtRpcImplements(DatePatternEditRequest.class)
public class DatePatternEditBackend implements GwtRpcImplementation<DatePatternEditRequest, DatePatternEditResponse>{
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);

	@Override
	public DatePatternEditResponse execute(DatePatternEditRequest request, SessionContext context) {
		context.checkPermission(Right.DatePatterns);
		switch (request.getOperation()) {
		case ADD:
			DatePatternEditResponse addResponse = new DatePatternEditResponse();
			addResponse.setSessionId(context.getUser().getCurrentAcademicSessionId());
			addResponse.setSessionName(context.getUser().getCurrentAuthority().getQualifiers("Session").get(0).getQualifierLabel());
			addResponse.setPattern(new DatePatternInterface());
			
			setupLookups(context, addResponse, null);
			return addResponse;
		case EDIT:
			DatePatternEditResponse response = new DatePatternEditResponse();
			response.setSessionId(context.getUser().getCurrentAcademicSessionId());
			response.setSessionName(context.getUser().getCurrentAuthority().getQualifiers("Session").get(0).getQualifierLabel());
			
			DatePattern pattern = DatePatternDAO.getInstance().get(request.getPatternId());
			DatePatternInterface dp = new DatePatternInterface();
			dp.setPatternId(pattern.getUniqueId());
			dp.setName(pattern.getName());
			dp.setType(Type.values()[pattern.getType()]);
			dp.setNbrWeeks(pattern.getNumberOfWeeks() == null ? null : Math.round(pattern.getNumberOfWeeks()));
			dp.setVisible(pattern.isVisible());;
			dp.setDefault(pattern.isDefault());
			for (DatePattern parent: pattern.getChildren())
				dp.addChildrenId(parent.getUniqueId());
			for (DatePattern child: pattern.getParents())
				dp.addParentId(child.getUniqueId());
			for (Department dept: pattern.getDepartments())
				dp.addDepartmentId(dept.getUniqueId());
			dp.setPattern(pattern.getPattern());
			dp.setOffset(pattern.getOffset());
			response.setCanDelete(!pattern.isUsed() && !pattern.isDefault());
			response.setPattern(dp);
			
			setupLookups(context, response, pattern);
			return response;
		case DELETE:
			deletePattern(context, request.getPatternId());
			return null;
		case SAVE:
			DatePattern other = DatePattern.findByName(context.getUser().getCurrentAcademicSessionId(), request.getPattern().getName());
			if (other != null && !other.getUniqueId().equals(request.getPatternId()))
				throw new GwtRpcException(MSG.errorAlreadyExists(request.getPattern().getName()));
			
			DatePatternEditResponse saveResponse = new DatePatternEditResponse();
			saveResponse.setSessionId(context.getUser().getCurrentAcademicSessionId());
			saveResponse.setSessionName(context.getUser().getCurrentAuthority().getQualifiers("Session").get(0).getQualifierLabel());
			saveResponse.setPattern(new DatePatternInterface());
			saveResponse.getPattern().setPatternId(savePattern(context, request.getPattern()));
			
			return saveResponse;
		case PUSH_UP:
			DatePatternEditResponse pushResponse = new DatePatternEditResponse();
			pushResponse.setLog(pushUp(context));
			return pushResponse;
		case ASSIGN_DEPTS:
			DatePatternEditResponse assgnDeptResponse = new DatePatternEditResponse();
			assgnDeptResponse.setLog(assignDepts(context));
			return assgnDeptResponse;
		}
		return null;
	}
	
	protected void setupLookups(SessionContext context, DatePatternEditResponse response, DatePattern pattern) {
		
		for (DatePattern dp: DatePattern.findAllParents(context.getUser().getCurrentAcademicSessionId())) 
			response.addParentPattern(dp.getUniqueId(), dp.getName());
		for (DatePattern dp: DatePattern.findAllChildren(context.getUser().getCurrentAcademicSessionId())) 
			response.addChildPattern(dp.getUniqueId(), dp.getName());
		
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
		
		Session session = SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId());
		for (SessionMonth m: PatternDatesBackend.listMonths(session))
			response.addMonth(m);
		response.setBaseOffset(DateUtils.daysBetween(DateUtils.getDate(1, session.getPatternStartMonth(), session.getSessionStartYear()), session.getSessionBeginDateTime()));
	}
	
	protected void deletePattern(SessionContext context, Long patternId) {
        org.hibernate.Session hibSession = TimetableManagerDAO.getInstance().getSession();
        Transaction tx = null;
        
        try {
        	tx = hibSession.beginTransaction();
        	
        	DatePattern dp = (DatePatternDAO.getInstance()).get(patternId, hibSession);
        	
        	if (dp.isUsed() || dp.isDefault())
        		throw new GwtRpcException(MSG.infoDatePatternUsed());
        	
            ChangeLog.addChange(
                    hibSession, 
                    context, 
                    dp, 
                    ChangeLog.Source.DATE_PATTERN_EDIT, 
                    ChangeLog.Operation.DELETE, 
                    null, 
                    null);
        	
        	for (Department d: dp.getDepartments()) {
        		d.getDatePatterns().remove(dp);
        		hibSession.merge(d);
        	}
        	for (DatePattern d: dp.getChildren()) {
    			d.getParents().remove(dp);
    			hibSession.merge(d);
    		}
    		dp.getChildren().clear();
    		for (DatePattern d: dp.getParents()) {
    			d.getChildren().remove(dp);
    			hibSession.merge(d);
    		}
    		dp.getParents().clear();

            hibSession.merge(dp);
    		hibSession.remove(dp);

           	tx.commit();
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw e;
        }
	}
	
	protected Long savePattern(SessionContext context, DatePatternInterface pattern) {
        Transaction tx = null;
        org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
        Long sessionId = context.getUser().getCurrentAcademicSessionId();
        Long ret = null;
        
        try {
            tx = hibSession.beginTransaction();
            
            DatePattern dp = null;
            if (pattern.getPatternId() == null) {
            	dp = new DatePattern();
            	dp.setSession(SessionDAO.getInstance().get(sessionId, hibSession));
            	dp.setDepartments(new HashSet<Department>());
            	dp.setChildren(new HashSet<DatePattern>());
            	dp.setParents(new HashSet<DatePattern>());
            } else {
            	dp = DatePatternDAO.getInstance().get(pattern.getPatternId(), hibSession);
            }

        	dp.setName(pattern.getName());
        	dp.setNumberOfWeeks(pattern.getNbrWeeks() == null ? 0 : Float.valueOf(pattern.getNbrWeeks()));
        	dp.setType(pattern.getType().ordinal());
        	dp.setVisible(pattern.isVisible());
        	if (dp.getDatePatternType() == DatePatternType.PatternSet || !pattern.hasPattern()) {
        		dp.setPattern("0");
        		dp.setOffset(0);
        	} else {
        		dp.setPattern(pattern.getPattern());
        		dp.setOffset(pattern.getOffset());
        	}
        	
        	if (pattern.getPatternId() == null)
        		hibSession.persist(dp);
        	ret = dp.getUniqueId();
        	
        	Set<Department> remainingDepartments = new HashSet<Department>(dp.getDepartments());
        	if (pattern.hasDepartmentIds() && (dp.getDatePatternType() == DatePatternType.PatternSet || dp.getDatePatternType() == DatePatternType.Extended))
        		for (Long deptId: pattern.getDepartmentIds()) {
        			Department dept = DepartmentDAO.getInstance().get(deptId, hibSession);
        			if (!remainingDepartments.remove(dept)) {
        				dp.addToDepartments(dept);
        				dept.addToDatePatterns(dp);
        				hibSession.merge(dept);
        			}
        		}
        	for (Department dept: remainingDepartments) {
        		dp.getDepartments().remove(dept);
        		dept.getDatePatterns().remove(dp);
        		hibSession.merge(dept);
        	}
        	
        	if (dp.getDatePatternType() == DatePatternType.PatternSet) {
        		HashSet<DatePattern> oldParents = new HashSet<DatePattern>(dp.getParents());
        		for (DatePattern d: oldParents) {
        			dp.getParents().remove(d);			
        			d.getChildren().remove(dp);
        			hibSession.merge(d);
        		}
        		HashSet<DatePattern> oldChildren = new HashSet<DatePattern>(dp.getChildren());
        		if (pattern.hasChildrenIds())
        			for (Long childId: pattern.getChildrenIds()) {
        				DatePattern d = (DatePatternDAO.getInstance()).get(childId,hibSession);
        				if (d==null) continue;
        				if (!oldChildren.remove(d)) {
        					d.getParents().add(dp);
        					dp.getChildren().add(d);
        					hibSession.merge(d);
        				}
        			}
    			for (DatePattern d: oldChildren) {
    				d.getParents().remove(dp);
    				dp.getChildren().remove(d);
    				hibSession.merge(d);
    			}
        	} else {
        		HashSet<DatePattern> oldParents = new HashSet<DatePattern>(dp.getParents());
        		if (pattern.hasParentIds())
        			for (Long parentId: pattern.getParentIds()) {
            			DatePattern d = (DatePatternDAO.getInstance()).get(parentId,hibSession);
            			if (d==null) continue;
            			if (!oldParents.remove(d)) {
            				dp.getParents().add(d);		
            				d.getChildren().add(dp);
            				hibSession.merge(dp);				
            			}
            		}
        		for (DatePattern d: oldParents) {
        			dp.getParents().remove(d);			
        			d.getChildren().remove(dp);
        			hibSession.merge(d);
        		}
        		HashSet<DatePattern> oldChildren = new HashSet<DatePattern>(dp.getChildren());
    			for (DatePattern d: oldChildren) {
    				d.getParents().remove(dp);
    				dp.getChildren().remove(d);
    				hibSession.merge(d);
    			}
        	}
        	
        	hibSession.merge(dp);
        	
        	if (pattern.isDefault()) {
        		dp.getSession().setDefaultDatePattern(dp);
        		hibSession.merge(dp.getSession());
        	}
        	
            ChangeLog.addChange(
                    hibSession, 
                    context, 
                    dp, 
                    ChangeLog.Source.DATE_PATTERN_EDIT, 
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
	
	protected byte[] pushUp(SessionContext context) {
        Transaction tx = null;
        PrintWriter out = null;
        org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
        Long sessionId = context.getUser().getCurrentAcademicSessionId();
        StringWriter log = new StringWriter();
        try {
            tx = hibSession.beginTransaction();
            
        	out = new PrintWriter(log);
            
        	List<SchedulingSubpart> subparts =
					hibSession.
					createQuery("select distinct c.schedulingSubpart from Class_ as c inner join c.datePattern as dp where dp.session.uniqueId=:sessionId", SchedulingSubpart.class).
					setParameter("sessionId", sessionId).list();

        	for (SchedulingSubpart subpart: subparts) {
        		
        		out.println("Checking "+subpart.getSchedulingSubpartLabel()+" ...");
        		
        		boolean sameDatePattern = true;
        		DatePattern dp = null;
        		
        		for (Class_ clazz: subpart.getClasses()) {
        			if (clazz.getDatePattern()==null) {
        				sameDatePattern=false; break;
        			}
        			if (dp==null)
        				dp = clazz.getDatePattern();
        			else if (!dp.equals(clazz.getDatePattern())) {
        				sameDatePattern=false; break;
        			}
        		}
        		
        		if (!sameDatePattern) continue;
        		
        		out.println("  -- all classes share same date pattern "+dp.getName()+" --> pushing it to subpart");
        		
        		for (Class_ clazz: subpart.getClasses()) {
        			clazz.setDatePattern(null);
        			hibSession.merge(clazz);
        		}
        		subpart.setDatePattern(dp.isDefault()?null:dp);
        		hibSession.merge(subpart);
            }

        	out.flush(); out.close(); out = null;
            
            tx.commit() ;
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw new GwtRpcException(e.getMessage(), e);
        }
        return log.getBuffer().toString().getBytes();
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
            
        	TreeSet<DatePattern> allDatePatterns = new TreeSet<DatePattern>(DatePattern.findAll(sessionId, true, null, null));
        	for (DatePattern dp: allDatePatterns) {
        		
        		if (!dp.isExtended()) continue;
        		
        		out.println("Checking "+dp.getName()+" ...");
        		
        		List<Class_> classes =
   					hibSession.
   					createQuery("select distinct c from Class_ as c inner join c.datePattern as dp where dp.uniqueId=:uniqueId", Class_.class).
   					setParameter("uniqueId", dp.getUniqueId()).list();
        		
        		List<SchedulingSubpart> subparts = 
					hibSession.
					createQuery("select distinct s from SchedulingSubpart as s inner join s.datePattern as dp where dp.uniqueId=:uniqueId", SchedulingSubpart.class).
					setParameter("uniqueId", dp.getUniqueId()).list();
        		
        		HashSet<Department> depts = new HashSet<Department>();
        		
        		for (Class_ c: classes) {
        			depts.add(c.getManagingDept());
        		}
        		
        		for (SchedulingSubpart s: subparts) {
        			depts.add(s.getManagingDept());
        		}
        		
        		out.println("  -- departments: "+depts);

        		boolean added = false;
        		for (Department d: depts) {
        			if (d.isExternalManager()) {
        				/*
        				if (dp.getDepartments().contains(d)) {
        					dp.getDepartments().remove(d);
        					d.getDatePatterns().remove(dp);
        					hibSession.saveOrUpdate(d);
            				out.println("    -- department "+d+" removed from "+dp.getName());
            				added=true;
        				}*/
        				continue;
        			}
        			if (!dp.getDepartments().contains(d)) {
        				dp.getDepartments().add(d);
        				d.getDatePatterns().add(dp);
        				hibSession.merge(d);
        				out.println("    -- department "+d+" added to "+dp.getName());
        				added = true;
        			}
        		}
        		if (added) {
        			hibSession.merge(dp);
                    ChangeLog.addChange(
                            hibSession, 
                            context, 
                            dp, 
                            ChangeLog.Source.DATE_PATTERN_EDIT, 
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
