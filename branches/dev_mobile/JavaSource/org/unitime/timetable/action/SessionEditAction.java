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
package org.unitime.timetable.action;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
import org.unitime.timetable.model.dao.DatePatternDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.StudentSectioningStatusDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.spring.struts.SpringAwareLookupDispatchAction;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.LookupTables;


/** 
 * MyEclipse Struts
 * Creation date: 02-18-2005
 * 
 * XDoclet definition:
 * @struts:action path="/sessionEdit" name="sessionEditForm" parameter="do" scope="request" validate="true"
 * @struts:action-forward name="showEdit" path="/admin/sessionEdit.jsp"
 * @struts:action-forward name="showAdd" path="/admin/sessionAdd.jsp"
 * @struts:action-forward name="showSessionList" path="/sessionList.do" redirect="true"
 *
 * @author Tomas Muller
 */
@Service("/sessionEdit")
public class SessionEditAction extends SpringAwareLookupDispatchAction {
	
	@Autowired SessionContext sessionContext;

	// --------------------------------------------------------- Instance Variables

	// --------------------------------------------------------- Methods

	/** 
	 * Method execute
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 * @throws HibernateException
	 */
	
	protected Map getKeyMethodMap() {
	      Map map = new HashMap();
	      map.put("editSession", "editSession");
	      map.put("button.addSession", "addSession");
	      map.put("button.saveSession", "saveSession");
	      map.put("button.updateSession", "saveSession");
	      map.put("button.deleteSession", "deleteSession");
	      map.put("button.cancelSessionEdit", "cancelSessionEdit");
	      map.put("button.loadCrsOffrDemand", "loadCrsOffrDemand");
	      map.put("button.viewCrsOffrDemand", "viewCrsOffrDemand");
	      return map;
	  }

	
	public ActionForward editSession(
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response) throws Exception {
		
		SessionEditForm sessionEditForm = (SessionEditForm) form;		
		Long id =  new Long(Long.parseLong(request.getParameter("sessionId")));
		Session acadSession = Session.getSessionById(id);
		
		sessionContext.checkPermission(acadSession, Right.AcademicSessionEdit);
		sessionEditForm.setIncludeTestSession(sessionContext.hasPermission(Right.AllowTestSessions));
		
		sessionEditForm.setSession(acadSession);
		DatePattern d = acadSession.getDefaultDatePattern();
		
		if (d!=null) {
		    sessionEditForm.setDefaultDatePatternId(d.getUniqueId().toString());
		    sessionEditForm.setDefaultDatePatternLabel(d.getName());
		}
		else {
		    sessionEditForm.setDefaultDatePatternId("");
		    sessionEditForm.setDefaultDatePatternLabel("Default date pattern not set");
		}
		sessionEditForm.setAcademicInitiative(acadSession.getAcademicInitiative());
		sessionEditForm.setAcademicYear(acadSession.getAcademicYear());
		sessionEditForm.setAcademicTerm(acadSession.getAcademicTerm());
		
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		sessionEditForm.setSessionStart(sdf.format(acadSession.getSessionBeginDateTime()));
		sessionEditForm.setSessionEnd(sdf.format(acadSession.getSessionEndDateTime()));
		sessionEditForm.setClassesEnd(sdf.format(acadSession.getClassesEndDateTime()));
		sessionEditForm.setExamStart(acadSession.getExamBeginDate()==null?"":sdf.format(acadSession.getExamBeginDate()));
		sessionEditForm.setEventStart(acadSession.getEventBeginDate()==null?"":sdf.format(acadSession.getEventBeginDate()));
		sessionEditForm.setEventEnd(acadSession.getEventEndDate()==null?"":sdf.format(acadSession.getEventEndDate()));
		
        Session sessn = Session.getSessionById(id);
		LookupTables.setupDatePatterns(request, sessn, false, Constants.BLANK_OPTION_LABEL, null, null, null);
		request.setAttribute("Sessions.holidays", sessionEditForm.getSession().getHolidaysHtml());
		
		sessionEditForm.setWkEnroll(acadSession.getLastWeekToEnroll());
		sessionEditForm.setWkChange(acadSession.getLastWeekToChange());
		sessionEditForm.setWkDrop(acadSession.getLastWeekToDrop());
		sessionEditForm.setSectStatus(acadSession.getDefaultSectioningStatus() == null ? -1 : acadSession.getDefaultSectioningStatus().getUniqueId());
		
		return mapping.findForward("showEdit");
	}
	
	public ActionForward deleteSession(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		Long id =  new Long(Long.parseLong(request.getParameter("sessionId")));
		
        if (id.equals(sessionContext.getUser().getCurrentAcademicSessionId())) {
            ActionMessages errors = new ActionMessages();
            errors.add("sessionId", new ActionMessage("errors.generic", "Current academic session cannot be deleted -- please change your session first."));
            saveErrors(request, errors);
            return mapping.findForward("showEdit");
        }
        
		sessionContext.checkPermission(id, "Session", Right.AcademicSessionDelete);
		
		Session.deleteSessionById(id);
		
		return mapping.findForward("showSessionList");
	}
	
	public ActionForward addSession(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		sessionContext.checkPermission(Right.AcademicSessionAdd);
		((SessionEditForm)form).setIncludeTestSession(sessionContext.hasPermission(Right.AllowTestSessions));

		return mapping.findForward("showAdd");
	}
	
	public ActionForward saveSession(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {
				
        SessionEditForm sessionEditForm = (SessionEditForm) form;
        
        if (sessionEditForm.getSessionId() == null || sessionEditForm.getSessionId().equals(0l)) {
        	sessionContext.checkPermission(Right.AcademicSessionAdd);
        } else {
        	sessionContext.checkPermission(sessionEditForm.getSessionId(), "Session", Right.AcademicSessionEdit);
        }

        Transaction tx = null;
        org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
        
        try {
            tx = hibSession.beginTransaction();

            Session sessn = sessionEditForm.getSession();
            
            if (sessionEditForm.getSessionId()!=null && !sessn.getSessionId().equals(0l)) 
                sessn = (new SessionDAO()).get(sessionEditForm.getSessionId(),hibSession);
            else 
                sessn.setSessionId(null);
            
            String refresh = request.getParameter("refresh");
            
            if (refresh!=null && refresh.equals("1")) {
                
                ActionErrors errors = new ActionErrors(); 
                setHolidays(request, sessionEditForm, errors, sessn);
                if (sessn.getSessionId()!=null){
                    LookupTables.setupDatePatterns(request, sessn, false, Constants.BLANK_OPTION_LABEL, null, null, null);
                    request.setAttribute("Sessions.holidays", sessn.getHolidaysHtml());     
                    return mapping.findForward("showEdit");
                }
                else
                    return mapping.findForward("showAdd");
            }
            
            ActionMessages errors = sessionEditForm.validate(mapping, request);
            if (errors.size()>0) {
                saveErrors(request, errors);
                if (sessn.getSessionId()!=null) {
                    LookupTables.setupDatePatterns(request, sessn, false, Constants.BLANK_OPTION_LABEL, null, null, null);
                    request.setAttribute("Sessions.holidays", sessn.getHolidaysHtml());     
                    return mapping.findForward("showEdit");
                }
                else {
                    ActionErrors errors2 = new ActionErrors(); 
                    setHolidays(request, sessionEditForm, errors2, sessn);
                    return mapping.findForward("showAdd");
                }
            }

            if (sessionEditForm.getDefaultDatePatternId()!=null && 
                    sessionEditForm.getDefaultDatePatternId().trim().length()>0) {
                DatePattern d = new DatePatternDAO().get(new Long(sessionEditForm.getDefaultDatePatternId()));
                sessn.setDefaultDatePattern(d);
            }
            
            sessn.setAcademicInitiative(sessionEditForm.getAcademicInitiative());
            sessn.setStatusType(sessionEditForm.getStatusType());
            setSessionData(request, sessionEditForm, sessn);

            hibSession.saveOrUpdate(sessn);

            ChangeLog.addChange(
                    hibSession, 
                    sessionContext, 
                    sessn, 
                    ChangeLog.Source.SESSION_EDIT, 
                    ChangeLog.Operation.UPDATE, 
                    null, 
                    null);
            
            if (sessionEditForm.getSessionId() != null)
            	StudentSectioningQueue.sessionStatusChanged(hibSession, sessionContext.getUser(), sessionEditForm.getSessionId(), false);
            
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
                hibSession.save(contact);
            }

            if (sessn.getStatusType().isTestSession()) {
            	hibSession.createQuery(
            			"delete ExamEvent where exam in (from Exam x where x.session.uniqueId = :sessionId)")
            			.setLong("sessionId", sessn.getUniqueId()).executeUpdate();
            	hibSession.createQuery(
            			"delete ClassEvent where clazz in (from Class_ c where c.committedAssignment.solution.owner.session.uniqueId = :sessionId)")
            			.setLong("sessionId", sessn.getUniqueId()).executeUpdate();
            } else {
            	for (Assignment assignment: (List<Assignment>)hibSession.createQuery(
            			"select a from Class_ c inner join c.committedAssignment a where c.committedAssignment.solution.owner.session.uniqueId = :sessionId" +
            			" and (select count(e) from ClassEvent e where e.clazz = c) = 0")
            			.setLong("sessionId", sessn.getUniqueId()).list()) {
            		ClassEvent event = assignment.generateCommittedEvent(null,true);
            		if (event != null && !event.getMeetings().isEmpty()) {
                    	event.setMainContact(contact);
                        hibSession.saveOrUpdate(event);
                    }
        		    if (event != null && event.getMeetings().isEmpty() && event.getUniqueId() != null)
        		    	hibSession.delete(event);
            	}
            	for (Exam exam: (List<Exam>)hibSession.createQuery(
            			"from Exam x where x.session.uniqueId = :sessionId and x.assignedPeriod != null and" +
            			" (select count(e) from ExamEvent e where e.exam = x) = 0")
            			.setLong("sessionId", sessn.getUniqueId()).list()) {
            		ExamEvent event = exam.generateEvent(null, true);
                    if (event!=null) {
                        event.setEventName(exam.getLabel());
                        event.setMinCapacity(exam.getSize());
                        event.setMaxCapacity(exam.getSize());
                        event.setMainContact(contact);
                        hibSession.saveOrUpdate(event);
                    }
            	}
            }
            
            tx.commit() ;
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw e;
        }
                
		return mapping.findForward("showSessionList");
	}

	/**
	 * 
	 */
	private void setHolidays(
			HttpServletRequest request,
			SessionEditForm sessionEditForm,
			ActionErrors errors,
			Session sessn) throws ParseException {
		
		sessionEditForm.validateDates(errors);
		
		if (errors.size()==0) {			
			setSessionData(request, sessionEditForm, sessn);
			request.setAttribute("Sessions.holidays", sessn.getHolidaysHtml());		
		}
		else
			saveErrors(request, new ActionMessages(errors));
	}


	/**
	 * 
	 */
	private void setSessionData(
			HttpServletRequest request,
			SessionEditForm sessionEditForm,
			Session sessn ) throws ParseException {
		
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		sessn.setAcademicYear(sessionEditForm.getAcademicYear());
		sessn.setAcademicTerm(sessionEditForm.getAcademicTerm());
		sessn.setSessionBeginDateTime(sdf.parse(sessionEditForm.getSessionStart()));
		sessn.setSessionEndDateTime(sdf.parse(sessionEditForm.getSessionEnd()));
		sessn.setClassesEndDateTime(sdf.parse(sessionEditForm.getClassesEnd()));
		sessn.setExamBeginDate(sdf.parse(sessionEditForm.getExamStart()));
		sessn.setEventBeginDate(sdf.parse(sessionEditForm.getEventStart()));
		sessn.setEventEndDate(sdf.parse(sessionEditForm.getEventEnd()));
		sessn.setHolidays(request);
		sessn.setLastWeekToEnroll(sessionEditForm.getWkEnroll());
		sessn.setLastWeekToChange(sessionEditForm.getWkChange());
		sessn.setLastWeekToDrop(sessionEditForm.getWkDrop());
		sessn.setDefaultSectioningStatus(sessionEditForm.getSectStatus() == null || sessionEditForm.getSectStatus() < 0 ? null : StudentSectioningStatusDAO.getInstance().get(sessionEditForm.getSectStatus())); 
	}


	public ActionForward cancelSessionEdit(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) {
		return mapping.findForward("showSessionList");
	}
	
}
