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
package org.unitime.timetable.action;

import java.text.ParseException;
import java.util.Date;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesDefinitions;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.hibernate.Transaction;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.form.SessionEditForm;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ClassEvent;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.EventContact;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamEvent;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.StudentSectioningQueue;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.ClassDurationTypeDAO;
import org.unitime.timetable.model.dao.DatePatternDAO;
import org.unitime.timetable.model.dao.InstructionalMethodDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.StudentSectioningStatusDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.LookupTables;


/** 
 * @author Tomas Muller
 */
@Action(value="sessionEdit", results = {
		@Result(name = "showAdd", type = "tiles", location = "sessionAdd.tiles"),
		@Result(name = "showEdit", type = "tiles", location = "sessionEdit.tiles"),
		@Result(name = "showSessionList", type = "redirect", location = "/sessionList.action")
	})
@TilesDefinitions({
	@TilesDefinition(name = "sessionAdd.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Add Academic Session"),
		@TilesPutAttribute(name = "body", value = "/admin/sessionEdit.jsp")
	}),
	@TilesDefinition(name = "sessionEdit.tiles", extend = "baseLayout", putAttributes =  {
			@TilesPutAttribute(name = "title", value = "Edit Academic Session"),
			@TilesPutAttribute(name = "body", value = "/admin/sessionEdit.jsp")
	})
})
public class SessionEditAction extends UniTimeAction<SessionEditForm> {
	private static final long serialVersionUID = 8818504076997048439L;
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);

	private Long sessionId;
	private Boolean refresh;
	
	public Long getSessionId() { return sessionId; }
	public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
	public Boolean getRefresh() { return refresh; }
	public void setRefresh(Boolean refresh) { this.refresh = refresh; }
	
	@Override
	public String execute() throws Exception {
		if (form == null)
			form = new SessionEditForm();
		if (MSG.actionAddAcademicSession().equals(op)) {
			return addSession();
		} else if (MSG.actionSaveAcademicSession().equals(op)) {
			return saveSession();
		} else if (MSG.actionUpdateAcademicSession().equals(op)) {
			return saveSession();
		} else if ("editSession".equals(op)) {
			return editSession();
		} else if (MSG.actionDeleteAcademicSession().equals(op)) {
			return deleteSession();
		} else if (MSG.actionBackToAcademicSessions().equals(op)) {
			return cancelSessionEdit();
		} else {
			return "showSessionList";
		}
	}
	
	public String editSession() throws Exception {
		Session acadSession = Session.getSessionById(sessionId);
		
		sessionContext.checkPermission(acadSession, Right.AcademicSessionEdit);
		form.setIncludeTestSession(sessionContext.hasPermission(Right.AllowTestSessions));
		
		form.setSession(acadSession);
		DatePattern d = acadSession.getDefaultDatePattern();
		
		if (d!=null) {
		    form.setDefaultDatePatternId(d.getUniqueId());
		    form.setDefaultDatePatternLabel(d.getName());
		}
		else {
		    form.setDefaultDatePatternId(-1l);
		    form.setDefaultDatePatternLabel(MSG.infoNoDefaultDatePattern());
		}
		form.setAcademicInitiative(acadSession.getAcademicInitiative());
		form.setAcademicYear(acadSession.getAcademicYear());
		form.setAcademicTerm(acadSession.getAcademicTerm());
		
		Formats.Format<Date> sdf = Formats.getDateFormat(Formats.Pattern.DATE_ENTRY_FORMAT);
		form.setSessionStart(sdf.format(acadSession.getSessionBeginDateTime()));
		form.setSessionEnd(sdf.format(acadSession.getSessionEndDateTime()));
		form.setClassesEnd(sdf.format(acadSession.getClassesEndDateTime()));
		form.setExamStart(acadSession.getExamBeginDate()==null?"":sdf.format(acadSession.getExamBeginDate()));
		form.setEventStart(acadSession.getEventBeginDate()==null?"":sdf.format(acadSession.getEventBeginDate()));
		form.setEventEnd(acadSession.getEventEndDate()==null?"":sdf.format(acadSession.getEventEndDate()));
		
		LookupTables.setupDatePatterns(request, sessionId, false, Constants.BLANK_OPTION_LABEL, null, null, null);
		request.setAttribute("holidays", form.getSession().getHolidaysHtml());
		
		form.setWkEnroll(acadSession.getLastWeekToEnroll());
		form.setWkChange(acadSession.getLastWeekToChange());
		form.setWkDrop(acadSession.getLastWeekToDrop());
		form.setSectStatus(acadSession.getDefaultSectioningStatus() == null ? -1 : acadSession.getDefaultSectioningStatus().getUniqueId());
		form.setDurationType(acadSession.getDefaultClassDurationType() == null ? -1 : acadSession.getDefaultClassDurationType().getUniqueId());
		form.setInstructionalMethod(acadSession.getDefaultInstructionalMethod() == null ? -1 : acadSession.getDefaultInstructionalMethod().getUniqueId());
		
		return "showEdit";
	}
	
	public String deleteSession() throws Exception {
		Long id = form.getSessionId();
		
        if (id.equals(sessionContext.getUser().getCurrentAcademicSessionId())) {
        	addFieldError("form.sessionId", MSG.errorCannotDeleteCurrentAcademicSession());
            return "showEdit";
        }
        
		sessionContext.checkPermission(id, "Session", Right.AcademicSessionDelete);
		
		Session.deleteSessionById(id);
		
		return "showSessionList";
	}
	
	public String addSession() throws Exception {
		sessionContext.checkPermission(Right.AcademicSessionAdd);
		((SessionEditForm)form).setIncludeTestSession(sessionContext.hasPermission(Right.AllowTestSessions));

		return "showAdd";
	}
	
	public String saveSession() throws Exception {
        if (form.getSessionId() == null || form.getSessionId().equals(0l)) {
        	sessionContext.checkPermission(Right.AcademicSessionAdd);
        } else {
        	sessionContext.checkPermission(form.getSessionId(), "Session", Right.AcademicSessionEdit);
        }

        if (Boolean.TRUE.equals(refresh)) {
            Session sessn = form.getSession();
            setHolidays(sessn);
            if (form.getSessionId() != null){
                LookupTables.setupDatePatterns(request, form.getSessionId(), false, Constants.BLANK_OPTION_LABEL, null, null, null);
                if (!hasFieldErrors())
                	request.setAttribute("holidays", sessn.getHolidaysHtml());
                return "showEdit";
            } else
                return "showAdd";
        }

        Transaction tx = null;
        org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
        
        try {
            tx = hibSession.beginTransaction();

            Session sessn = form.getSession();
            
            if (form.getSessionId()!=null && !sessn.getSessionId().equals(0l)) 
                sessn = (SessionDAO.getInstance()).get(form.getSessionId(),hibSession);
            else 
                sessn.setSessionId(null);
            
            form.validate(this);
            if (hasFieldErrors()) {
                if (sessn.getSessionId()!=null) {
                    LookupTables.setupDatePatterns(request, sessn.getSessionId(), false, Constants.BLANK_OPTION_LABEL, null, null, null);
                    request.setAttribute("holidays", sessn.getHolidaysHtml());     
                    return "showEdit";
                } else {
                    setHolidays(sessn);
                    return "showAdd";
                }
            }

            if (form.getDefaultDatePatternId() != null && form.getDefaultDatePatternId() >= 0) {
                DatePattern d = DatePatternDAO.getInstance().get(form.getDefaultDatePatternId());
                sessn.setDefaultDatePattern(d);
            } else {
            	sessn.setDefaultDatePattern(null);;
            }
            
            sessn.setAcademicInitiative(form.getAcademicInitiative());
            sessn.setStatusType(form.getStatusType());
            setSessionData(sessn);

            if (sessn.getSessionId() == null)
            	hibSession.persist(sessn);
            else
            	hibSession.merge(sessn);

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
            	for (Assignment assignment: hibSession.createQuery(
            			"select a from Class_ c inner join c.assignments a inner join a.solution s where s.commited = true and s.owner.session.uniqueId = :sessionId " +
            			"and c.uniqueId not in (select e.clazz.uniqueId from ClassEvent e where e.clazz.controllingDept.session.uniqueId = :sessionId)",
            			Assignment.class)
            			.setParameter("sessionId", sessn.getUniqueId()).list()) {
            		ClassEvent event = assignment.generateCommittedEvent(null,true);
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
            			"from Exam x where x.session.uniqueId = :sessionId and x.assignedPeriod != null " +
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
                
		return "showSessionList";
	}

	private void setHolidays(Session sessn) throws ParseException {
		if (form.validateDates(this)) {			
			setSessionData(sessn);
			request.setAttribute("holidays", sessn.getHolidaysHtml());		
		}
	}


	private void setSessionData(Session sessn) throws ParseException {
		Formats.Format<Date> sdf = Formats.getDateFormat(Formats.Pattern.DATE_ENTRY_FORMAT);
		sessn.setAcademicYear(form.getAcademicYear());
		sessn.setAcademicTerm(form.getAcademicTerm());
		sessn.setSessionBeginDateTime(sdf.parse(form.getSessionStart()));
		sessn.setSessionEndDateTime(sdf.parse(form.getSessionEnd()));
		sessn.setClassesEndDateTime(sdf.parse(form.getClassesEnd()));
		sessn.setExamBeginDate(sdf.parse(form.getExamStart()));
		sessn.setEventBeginDate(sdf.parse(form.getEventStart()));
		sessn.setEventEndDate(sdf.parse(form.getEventEnd()));
		sessn.setHolidays(request);
		sessn.setLastWeekToEnroll(form.getWkEnroll());
		sessn.setLastWeekToChange(form.getWkChange());
		sessn.setLastWeekToDrop(form.getWkDrop());
		sessn.setDefaultSectioningStatus(form.getSectStatus() == null || form.getSectStatus() < 0 ? null : StudentSectioningStatusDAO.getInstance().get(form.getSectStatus()));
		sessn.setDefaultClassDurationType(form.getDurationType() == null || form.getDurationType() < 0 ? null : ClassDurationTypeDAO.getInstance().get(form.getDurationType()));
		sessn.setDefaultInstructionalMethod(form.getInstructionalMethod() == null || form.getInstructionalMethod() < 0 ? null : InstructionalMethodDAO.getInstance().get(form.getInstructionalMethod())); 
	}


	public String cancelSessionEdit() {
		return "showSessionList";
	}
	
}
