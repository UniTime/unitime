/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.action;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.LookupDispatchAction;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.SessionEditForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.RoomType;
import org.unitime.timetable.model.RoomTypeOption;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.DatePatternDAO;
import org.unitime.timetable.model.dao.SessionDAO;
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
 */
public class SessionEditAction extends LookupDispatchAction {

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
		
        // Check access
        if(!Web.hasRole( request.getSession(),
		 			 new String[] {Roles.ADMIN_ROLE} )) {
		  throw new Exception ("Access Denied.");
		}
		
		SessionEditForm sessionEditForm = (SessionEditForm) form;		
		Long id =  new Long(Long.parseLong(request.getParameter("sessionId")));
		Session acadSession = Session.getSessionById(id);
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
		sessionEditForm.setExamStart(sdf.format(acadSession.getExamBeginDate()));
		
		for (RoomType t : RoomType.findAll()) {
		    RoomTypeOption o = t.getOption(acadSession);
		    sessionEditForm.setRoomOptionScheduleEvents(t.getReference(), o.canScheduleEvents());
		    sessionEditForm.setRoomOptionMessage(t.getReference(), o.getMessage());
		}
		
        Session sessn = Session.getSessionById(id);
		LookupTables.setupDatePatterns(request, sessn, false, Constants.BLANK_OPTION_LABEL, null, null, null);
		request.setAttribute("Sessions.holidays", sessionEditForm.getSession().getHolidaysHtml());		
		return mapping.findForward("showEdit");
	}
	
	public ActionForward deleteSession(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {
				
        // Check access
        if(!Web.hasRole( request.getSession(),
		 			 new String[] {Roles.ADMIN_ROLE} )) {
		  throw new Exception ("Access Denied.");
		}
        
        if (Session.getAllSessions().size()==1) {
            ActionMessages errors = new ActionMessages();
            errors.add("sessionId", new ActionMessage("errors.generic", "Last academic session cannot be deleted -- there needs to be at least one academic session present."));
            saveErrors(request, errors);
            return mapping.findForward("showEdit");
            
        }

		SessionEditForm sessionEditForm = (SessionEditForm) form;		
		Long id =  new Long(Long.parseLong(request.getParameter("sessionId")));
        Session sessn = Session.getSessionById(id);
		Session.deleteSessionById(id);
		return mapping.findForward("showSessionList");
	}
	
	public ActionForward addSession(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
        // Check access
        if(!Web.hasRole( request.getSession(),
		 			 new String[] {Roles.ADMIN_ROLE} )) {
		  throw new Exception ("Access Denied.");
		}

		SessionEditForm sessionEditForm = (SessionEditForm) form;
		return mapping.findForward("showAdd");
	}
	
	public ActionForward saveSession(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {
				
        // Check access
        if(!Web.hasRole( request.getSession(),
		 			 new String[] {Roles.ADMIN_ROLE} )) {
		  throw new Exception ("Access Denied.");
		}
        
        Transaction tx = null;
        org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
        
        try {
            tx = hibSession.beginTransaction();

            SessionEditForm sessionEditForm = (SessionEditForm) form;
            Session sessn = sessionEditForm.getSession();
            
            if (sessionEditForm.getSessionId()!=null && sessn.getSessionId().intValue()!=0) 
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

            for (RoomType t : RoomType.findAll()) {
                RoomTypeOption o = t.getOption(sessn);
                o.setScheduleEvents(sessionEditForm.getRoomOptionScheduleEvents(t.getReference()));
                o.setMessage(sessionEditForm.getRoomOptionMessage(t.getReference()));
                hibSession.saveOrUpdate(o);
            }

            ChangeLog.addChange(
                    hibSession, 
                    request, 
                    sessn, 
                    ChangeLog.Source.SESSION_EDIT, 
                    ChangeLog.Operation.UPDATE, 
                    null, 
                    null);
            
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
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
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
		sessn.setHolidays(request);
	}


	public ActionForward cancelSessionEdit(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) {
		return mapping.findForward("showSessionList");
	}
	
}