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

import org.hibernate.Transaction;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.client.admin.AcademicSessionsPage.AcademicSessionEditRequest;
import org.unitime.timetable.gwt.client.admin.AcademicSessionsPage.AcademicSessionEditResponse;
import org.unitime.timetable.gwt.client.admin.AcademicSessionsPage.AcademicSessionInterface;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ClassDurationType;
import org.unitime.timetable.model.ClassEvent;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.EventContact;
import org.unitime.timetable.model.EventDateMapping;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamEvent;
import org.unitime.timetable.model.InstructionalMethod;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.StudentSectioningQueue;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.ClassDurationTypeDAO;
import org.unitime.timetable.model.dao.DatePatternDAO;
import org.unitime.timetable.model.dao.DepartmentStatusTypeDAO;
import org.unitime.timetable.model.dao.InstructionalMethodDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.StudentSectioningStatusDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

@GwtRpcImplements(AcademicSessionEditRequest.class)
public class AcademicSessionEditBackend implements GwtRpcImplementation<AcademicSessionEditRequest, AcademicSessionEditResponse>{
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);

	@Override
	public AcademicSessionEditResponse execute(AcademicSessionEditRequest request, SessionContext context) {
		switch(request.getOperation()) {
		case ADD:
			context.checkPermission(Right.AcademicSessionAdd);
			AcademicSessionEditResponse addResponse = new AcademicSessionEditResponse();
			
			for (DepartmentStatusType status: DepartmentStatusType.findAllForSession(context.hasPermission(Right.AllowTestSessions)))
				addResponse.addSessionStatus(status.getUniqueId(), status.getLabel());
			
	    	for (ClassDurationType type: ClassDurationType.findAll())
	    		if (type.isVisible())
	    			addResponse.addClassDuration(type.getUniqueId(), type.getLabel());
	    	
	    	for (StudentSectioningStatus status: StudentSectioningStatus.findAll(-1l))
	    		addResponse.addStudentStatus(status.getUniqueId(), status.getLabel());

	    	for (InstructionalMethod im: InstructionalMethod.findAll())
	    		addResponse.addInstructionalMethod(im.getUniqueId(), im.getLabel());
	    	
	    	addResponse.setNrExcessDays(ApplicationProperty.SessionNrExcessDays.intValue());
			
			return addResponse;
		case EDIT:
			context.checkPermission(request.getSessionId(), Right.AcademicSessionEdit);
			Session acadSession = Session.getSessionById(request.getSessionId());
			
			AcademicSessionEditResponse response = new AcademicSessionEditResponse();
			
			for (DepartmentStatusType status: DepartmentStatusType.findAllForSession(context.hasPermission(Right.AllowTestSessions)))
				response.addSessionStatus(status.getUniqueId(), status.getLabel());
			if (acadSession.getStatusType() != null && !response.hasSessionStatus(acadSession.getStatusType().getUniqueId()))
				response.addSessionStatus(acadSession.getStatusType().getUniqueId(), acadSession.getStatusType().getLabel());
			
	    	for (ClassDurationType type: ClassDurationType.findAll())
	    		if (type.isVisible() || type.equals(acadSession.getDefaultClassDurationType()))
	    			response.addClassDuration(type.getUniqueId(), type.getLabel());
	    	
	    	for (StudentSectioningStatus status: StudentSectioningStatus.findAll(request.getSessionId()))
	    		response.addStudentStatus(status.getUniqueId(), status.getLabel());

	    	for (InstructionalMethod im: InstructionalMethod.findAll())
	    		response.addInstructionalMethod(im.getUniqueId(), im.getLabel());
	    	
	    	for (DatePattern dp: DatePattern.findAll(request.getSessionId(), false, null, acadSession.getDefaultDatePattern()))
	    		response.addDatePattern(dp.getUniqueId(), dp.getName());
	    	
	    	response.setCanDelete(context.hasPermission(acadSession, Right.AcademicSessionDelete));
	    	response.setNrExcessDays(ApplicationProperty.SessionNrExcessDays.intValue());
	    	response.setSession(loadSession(acadSession));
			
			return response;
		case DELETE:
			context.checkPermission(request.getSessionId(), Right.AcademicSessionDelete);
			if (request.getSessionId().equals(context.getUser().getCurrentAcademicSessionId()))
				throw new GwtRpcException(MSG.errorCannotDeleteCurrentAcademicSession());
			Session.deleteSessionById(request.getSessionId());
			return null;
		case SAVE:
			if (request.getSessionId() == null)
				context.checkPermission(Right.AcademicSessionAdd);
			else
				context.checkPermission(request.getSessionId(), Right.AcademicSessionEdit);
			
			Session sessn = Session.getSessionUsingInitiativeYearTerm(request.getSession().getInitiative(), request.getSession().getYear(), request.getSession().getTerm());
			if (request.getSessionId() == null && sessn != null)
				throw new GwtRpcException(MSG.errorAcademicSessionAlreadyExists());
			if (request.getSessionId() != null && sessn != null && !request.getSessionId().equals(sessn.getSessionId()))
				throw new GwtRpcException(MSG.errorAcademicSessionSameAlreadyExists());
			
			AcademicSessionEditResponse saveResponse = new AcademicSessionEditResponse();
			saveResponse.setSession(new AcademicSessionInterface());
			saveResponse.getSession().setSessionId(saveSession(context, request.getSession()));
			
			return saveResponse;
		}
		return null;
	}
	
	protected AcademicSessionInterface loadSession(Session acadSession) {
		AcademicSessionInterface form = new AcademicSessionInterface();
		form.setSessionId(acadSession.getUniqueId());
		form.setInitiative(acadSession.getAcademicInitiative());
		form.setYear(acadSession.getAcademicYear());
		form.setTerm(acadSession.getAcademicTerm());
		
		form.setSessionStart(acadSession.getSessionBeginDateTime());
		form.setSessionEnd(acadSession.getSessionEndDateTime());
		form.setClassEnd(acadSession.getClassesEndDateTime());
		form.setExamStart(acadSession.getExamBeginDate());
		form.setEventStart(acadSession.getEventBeginDate());
		form.setEventEnd(acadSession.getEventEndDate());
		form.setNotificationStart(acadSession.getNotificationsBeginDate());
		form.setNotificationEnd(acadSession.getNotificationsEndDate());
		
		form.setHolidays(acadSession.getHolidays());
		
		form.setNewEnrollmentDeadline(acadSession.getLastWeekToEnroll());
		form.setClassChangesDeadline(acadSession.getLastWeekToChange());
		form.setCourseDropDeadline(acadSession.getLastWeekToDrop());
		
		form.setDefaultDatePatternId(acadSession.getDefaultDatePattern() == null ? null : acadSession.getDefaultDatePattern().getUniqueId());
		form.setStudentStatusId(acadSession.getDefaultSectioningStatus() == null ? null : acadSession.getDefaultSectioningStatus().getUniqueId());
		form.setDefaultClassDurationId(acadSession.getDefaultClassDurationType() == null ? null : acadSession.getDefaultClassDurationType().getUniqueId());
		form.setInstructionalMethodId(acadSession.getDefaultInstructionalMethod() == null ? null : acadSession.getDefaultInstructionalMethod().getUniqueId());
		form.setSessionStatusId(acadSession.getStatusType() == null ? null : acadSession.getStatusType().getUniqueId());
		
		return form;
	}
	
	protected Long saveSession(SessionContext sessionContext, AcademicSessionInterface form) {
        Transaction tx = null;
        org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
        Long ret = null;
        
        try {
            tx = hibSession.beginTransaction();

            Session sessn = null;
            if (form.getSessionId() == null)
            	sessn = new Session();
            else
            	sessn = SessionDAO.getInstance().get(form.getSessionId(), hibSession);
            
            sessn.setDefaultDatePattern(form.getDefaultDatePatternId() == null ? null : DatePatternDAO.getInstance().get(form.getDefaultDatePatternId(), hibSession));
            
            sessn.setAcademicInitiative(form.getInitiative());
            sessn.setStatusType(form.getSessionStatusId() == null ? null : DepartmentStatusTypeDAO.getInstance().get(form.getSessionStatusId(), hibSession));
            sessn.setAcademicYear(form.getYear());
    		sessn.setAcademicTerm(form.getTerm());
    		sessn.setSessionBeginDateTime(form.getSessionStart());
    		sessn.setSessionEndDateTime(form.getSessionEnd());
    		sessn.setClassesEndDateTime(form.getClassEnd());
    		sessn.setExamBeginDate(form.getExamStart());
    		sessn.setEventBeginDate(form.getEventStart());
    		sessn.setEventEndDate(form.getEventEnd());
    		sessn.setHolidays(form.getHolidays());
    		sessn.setLastWeekToEnroll(form.getNewEnrollmentDeadline());
    		sessn.setLastWeekToChange(form.getClassChangesDeadline());
    		sessn.setLastWeekToDrop(form.getCourseDropDeadline());
    		sessn.setDefaultSectioningStatus(form.getStudentStatusId() == null ? null : StudentSectioningStatusDAO.getInstance().get(form.getStudentStatusId(), hibSession));
    		sessn.setDefaultClassDurationType(form.getDefaultClassDurationId() == null ? null : ClassDurationTypeDAO.getInstance().get(form.getDefaultClassDurationId(), hibSession));
    		sessn.setDefaultInstructionalMethod(form.getInstructionalMethodId() == null ? null : InstructionalMethodDAO.getInstance().get(form.getInstructionalMethodId(), hibSession));
    		sessn.setNotificationsBeginDate(form.getNotificationStart());
    		sessn.setNotificationsEndDate(form.getNotificationEnd());

            if (sessn.getSessionId() == null)
            	hibSession.persist(sessn);
            else
            	hibSession.merge(sessn);
            ret = sessn.getUniqueId();

            ChangeLog.addChange(
                    hibSession, 
                    sessionContext, 
                    sessn, 
                    ChangeLog.Source.SESSION_EDIT, 
                    ChangeLog.Operation.UPDATE, 
                    null, 
                    null);
            
            if (form.getSessionId() != null)
            	StudentSectioningQueue.sessionStatusChanged(hibSession, sessionContext.getUser(), form.getSessionId(), false);
            
            EventContact contact = EventContact.findByExternalUniqueId(sessionContext.getUser().getExternalUserId());
            if (contact==null) {
                contact = new EventContact();
                TimetableManager manager = TimetableManager.findByExternalId(sessionContext.getUser().getExternalUserId());
                if (manager != null) {
                	contact.setFirstName(manager.getFirstName());
                	contact.setMiddleName(manager.getMiddleName());
                	contact.setLastName(manager.getLastName());
                	contact.setExternalUniqueId(manager.getExternalUniqueId());
                	contact.setEmailAddress(manager.getEmailAddress());
                } else {
                	String[] name = sessionContext.getUser().getName().split(" ");
                	String fname = name.length >= 2 ? name[0] : "";
                	String lname = name.length >= 1 ? name[name.length - 1] : sessionContext.getUser().getName();
                	String mname = null;
                	if (name.length >= 3) {
                		for (int i = 1; i < name.length - 1; i++) {
                			if (fname == null)
                				fname = "";
                			else
                				fname += " ";
                			fname += name[i];
                		}
                	}
                	contact.setFirstName(fname);
                	contact.setMiddleName(mname);
                	contact.setLastName(lname);
                	contact.setExternalUniqueId(sessionContext.getUser().getExternalUserId());
                	contact.setEmailAddress(sessionContext.getUser().getEmail());
                	
                }
                hibSession.persist(contact);
            }

            if (sessn.getStatusType().isTestSession()) {
            	hibSession.createMutationQuery(
            			"delete ExamEvent where exam in (from Exam x where x.session.uniqueId = :sessionId)")
            			.setParameter("sessionId", sessn.getUniqueId()).executeUpdate();
            	hibSession.createMutationQuery(
            			"delete ClassEvent where clazz in (from Class_ c where c.committedAssignment.solution.owner.session.uniqueId = :sessionId)")
            			.setParameter("sessionId", sessn.getUniqueId()).executeUpdate();
            } else {
            	EventDateMapping.Class2EventDateMap class2eventDates = EventDateMapping.getMapping(sessn.getUniqueId());
            	for (Assignment assignment: hibSession.createQuery(
            			"select a from Class_ c inner join c.assignments a inner join a.solution s where s.commited = true and s.owner.session.uniqueId = :sessionId " +
            			"and c.uniqueId not in (select e.clazz.uniqueId from ClassEvent e where e.clazz.controllingDept.session.uniqueId = :sessionId)",
            			Assignment.class)
            			.setParameter("sessionId", sessn.getUniqueId()).list()) {
            		ClassEvent event = assignment.generateCommittedEvent(null, true, class2eventDates);
            		if (event != null && !event.getMeetings().isEmpty()) {
                    	event.setMainContact(contact);
                    	if (event.getUniqueId() == null)
                    		hibSession.persist(event);
                    	else
                    		hibSession.merge(event);
                    }
        		    if (event != null && event.getMeetings().isEmpty() && event.getUniqueId() != null)
        		    	hibSession.remove(event);
            	}
            	for (Exam exam: hibSession.createQuery(
            			"from Exam x where x.session.uniqueId = :sessionId and x.assignedPeriod is not null " +
            			"and x.uniqueId not in (select e.exam.uniqueId from ExamEvent e where e.exam.session.uniqueId = :sessionId)",
            			Exam.class)
            			.setParameter("sessionId", sessn.getUniqueId()).list()) {
            		ExamEvent event = exam.generateEvent(null, true);
                    if (event!=null) {
                        event.setEventName(exam.getLabel());
                        event.setMinCapacity(exam.getSize());
                        event.setMaxCapacity(exam.getSize());
                        event.setMainContact(contact);
                    	if (event.getUniqueId() == null)
                    		hibSession.persist(event);
                    	else
                    		hibSession.merge(event);
                    }
            	}
            }
            
            tx.commit() ;
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw e;
        }
        return ret;
	}

}
