/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.commons.web.WebTable.WebTableLine;
import org.unitime.timetable.form.RollForwardSessionForm;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.util.SessionRollForward;
import org.unitime.timetable.util.queue.QueueItem;
import org.unitime.timetable.util.queue.QueueProcessor;


/** 
 * MyEclipse Struts
 * Creation date: 02-27-2007
 * 
 * XDoclet definition:
 * @struts.action path="/exportSessionToMsf" name="exportSessionToMsfForm" input="/form/exportSessionToMsf.jsp" scope="request" validate="true"
 */
public class RollForwardSessionAction extends Action {
	/*
	 * Generated Methods
	 */

	/** 
	 * Method execute
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 * @throws Exception 
	 */
	public ActionForward execute(ActionMapping mapping, ActionForm form,
		HttpServletRequest request, HttpServletResponse response) throws Exception {
	    HttpSession webSession = request.getSession();
        if(!Web.isLoggedIn( webSession )) {
            throw new Exception ("Access Denied.");
        }
        MessageResources rsc = getResources(request);
        
        RollForwardSessionForm rollForwardSessionForm = (RollForwardSessionForm) form;
		HttpSession httpSession = request.getSession();
		User user = Web.getUser(request.getSession());
        // Get operation
        String op = request.getParameter("op");		  
           
        if (op != null && op.equals(rsc.getMessage("button.rollForward"))) {
            ActionMessages errors = rollForwardSessionForm.validate(mapping, request);
            if (errors.isEmpty()) {
            	QueueProcessor.getInstance().add(new RollForwardQueueItem(
            			Session.getCurrentAcadSession(user), TimetableManager.getManager(user), (RollForwardSessionForm)rollForwardSessionForm.clone()));
            } else {
                saveErrors(request, errors);
            }
        }

		if (request.getParameter("remove") != null) {
			QueueProcessor.getInstance().remove(Long.valueOf(request.getParameter("remove")));
	    }
		WebTable table = getQueueTable(request, TimetableManager.getManager(user).getUniqueId(), rollForwardSessionForm);
	    if (table != null) {
	    	request.setAttribute("table", table.printTable(WebTable.getOrder(request.getSession(),"rollForwardSession.ord")));
	    }
	    
		rollForwardSessionForm.setAdmin(user.isAdmin());
		
		setToFromSessionsInForm(rollForwardSessionForm);
		rollForwardSessionForm.setSubjectAreas(getSubjectAreas(rollForwardSessionForm.getSessionToRollForwardTo()));
		if (rollForwardSessionForm.getSubpartLocationPrefsAction() == null){
			rollForwardSessionForm.setSubpartLocationPrefsAction(SessionRollForward.ROLL_PREFS_ACTION);
		}
		if (rollForwardSessionForm.getSubpartTimePrefsAction() == null){
			rollForwardSessionForm.setSubpartTimePrefsAction(SessionRollForward.ROLL_PREFS_ACTION);			
		}
		if (rollForwardSessionForm.getClassPrefsAction() == null){
			rollForwardSessionForm.setClassPrefsAction(SessionRollForward.DO_NOT_ROLL_ACTION);
		}

  		return mapping.findForward("displayRollForwardSessionForm");
	}
	
	private WebTable getQueueTable(HttpServletRequest request, Long managerId, RollForwardSessionForm form) {
        WebTable.setOrder(request.getSession(),"rollForwardSession.ord",request.getParameter("ord"),1);
		String log = request.getParameter("log");
		DateFormat df = new SimpleDateFormat("h:mma");
		List<QueueItem> queue = QueueProcessor.getInstance().getItems(null, null, "Roll Forward");
		if (queue.isEmpty()) return null;
		WebTable table = new WebTable(9, "Roll forward in progress", "rollForwardSession.do?ord=%%",
				new String[] { "Name", "Status", "Progress", "Owner", "Session", "Created", "Started", "Finished", "Output"},
				new String[] { "left", "left", "right", "left", "left", "left", "left", "left", "center"},
				new boolean[] { true, true, true, true, true, true, true, true, true});
		Date now = new Date();
		long timeToShow = 1000 * 60 * 60;
		for (QueueItem item: queue) {
			if (item.finished() != null && now.getTime() - item.finished().getTime() > timeToShow) continue;
			String name = item.name();
			if (name.length() > 60) name = name.substring(0, 57) + "...";
			String delete = null;
			if (managerId.equals(item.getOwnerId()) && (item.started() == null || item.finished() != null)) {
				delete = "<img src='images/Delete16.gif' border='0' onClick=\"if (confirm('Do you really want to remove this data exchange?')) document.location='rollForwardSession.do?remove="+item.getId()+"'; event.cancelBubble=true;\">";
			}
			WebTableLine line = table.addLine("onClick=\"document.location='rollForwardSession.do?log=" + item.getId() + "';\"",
					new String[] {
						name + (delete == null ? "": " " + delete),
						item.status(),
						(item.progress() <= 0.0 || item.progress() >= 1.0 ? "" : String.valueOf(Math.round(100 * item.progress())) + "%"),
						item.getOwner().getName(),
						item.getSession().getLabel(),
						df.format(item.created()),
						item.started() == null ? "" : df.format(item.started()),
						item.finished() == null ? "" : df.format(item.finished()),
						item.output() == null ? "" : "<A href='temp/"+item.output().getName()+"'>"+item.output().getName().substring(item.output().getName().lastIndexOf('.') + 1).toUpperCase()+"</A>"
					},
					new Comparable[] {
						item.getId(),
						item.status(),
						item.progress(),
						item.getOwner().getName(),
						item.getSession(),
						item.created().getTime(),
						item.started() == null ? Long.MAX_VALUE : item.started().getTime(),
						item.finished() == null ? Long.MAX_VALUE : item.finished().getTime(),
						null
					});
			if (log != null && log.equals(item.getId().toString())) {
				request.setAttribute("logname", name);
				request.setAttribute("logid", item.getId().toString());
				request.setAttribute("log", item.log());
				((RollForwardQueueItem)item).getForm().copyTo(form);
				saveErrors(request, ((RollForwardQueueItem)item).getErrors());
				line.setBgColor("rgb(168,187,225)");
			}
			if (log == null && item.started() != null && item.finished() == null && managerId.equals(item.getOwnerId())) {
				request.setAttribute("logname", name);
				request.setAttribute("logid", item.getId().toString());
				request.setAttribute("log", item.log());
				((RollForwardQueueItem)item).getForm().copyTo(form);
				saveErrors(request, ((RollForwardQueueItem)item).getErrors());
				line.setBgColor("rgb(168,187,225)");
			}
		}
		return table;
	}
	
	private class RollForwardQueueItem extends QueueItem {
		private RollForwardSessionForm iForm;
		private int iProgress = 0;
		private ActionMessages iErrors = new ActionMessages();
		
		public RollForwardQueueItem(Session session, TimetableManager owner, RollForwardSessionForm form) {
			super(session, owner);
			iForm = form;
		}
		
		public ActionMessages getErrors() {
			return iErrors;
		}
		
		public RollForwardSessionForm getForm() {
			return iForm;
		}
		
		@Override
		protected void execute() throws Exception {
	        SessionRollForward sessionRollForward = new SessionRollForward();
	        if (iErrors.isEmpty() && iForm.getRollForwardDatePatterns()) {
				setStatus("Date patterns ...");
	        	sessionRollForward.rollDatePatternsForward(iErrors, iForm);
	        }
	        iProgress++;
            if (iErrors.isEmpty() && iForm.getRollForwardTimePatterns()) {
				setStatus("Time patterns ...");
	        	sessionRollForward.rollTimePatternsForward(iErrors, iForm);
	        }
	        iProgress++;
        	if (iErrors.isEmpty() && iForm.getRollForwardDepartments()) {
				setStatus("Departments ...");
	        	sessionRollForward.rollDepartmentsForward(iErrors, iForm);	
	        }
	        iProgress++;
        	if (iErrors.isEmpty() && iForm.getRollForwardManagers()) {
				setStatus("Managers ...");
        		sessionRollForward.rollManagersForward(iErrors, iForm);
        	}
	        iProgress++;
        	if (iErrors.isEmpty() && iForm.getRollForwardRoomData()) {
				setStatus("Rooms ...");
        		sessionRollForward.rollBuildingAndRoomDataForward(iErrors, iForm);
        	}
	        iProgress++;
        	if (iErrors.isEmpty() && iForm.getRollForwardSubjectAreas()) {
				setStatus("Subjects ...");
        		sessionRollForward.rollSubjectAreasForward(iErrors, iForm);
        	}
	        iProgress++;
        	if (iErrors.isEmpty() && iForm.getRollForwardInstructorData()) {
				setStatus("Instructors ...");
        		sessionRollForward.rollInstructorDataForward(iErrors, iForm);
        	}
	        iProgress++;
        	if (iErrors.isEmpty() && iForm.getRollForwardCourseOfferings()) {
				setStatus("Courses ...");
        		sessionRollForward.rollCourseOfferingsForward(iErrors, iForm);
        	}
	        iProgress++;
        	if (iErrors.isEmpty() && iForm.getRollForwardClassInstructors()) {
				setStatus("Class instructors ...");
        		sessionRollForward.rollClassInstructorsForward(iErrors, iForm);
        	}
	        iProgress++;
        	if (iErrors.isEmpty() && iForm.getAddNewCourseOfferings()) {
				setStatus("New courses ...");
        		sessionRollForward.addNewCourseOfferings(iErrors, iForm);
        	}
	        iProgress++;
        	if (iErrors.isEmpty() && iForm.getRollForwardExamConfiguration()) {
				setStatus("Exam config ...");
        		sessionRollForward.rollExamConfigurationDataForward(iErrors, iForm);
        	}
	        iProgress++;
        	if (iErrors.isEmpty() && iForm.getRollForwardMidtermExams()) {
				setStatus("Midterm exams ...");
        		sessionRollForward.rollMidtermExamsForward(iErrors, iForm);
        	}
	        iProgress++;
        	if (iErrors.isEmpty() && iForm.getRollForwardFinalExams()) {
				setStatus("Final exams ...");
        		sessionRollForward.rollFinalExamsForward(iErrors, iForm);
        	}
	        iProgress++;
        	if (iErrors.isEmpty() && iForm.getRollForwardStudents()) {
				setStatus("Students ...");
        	    sessionRollForward.rollStudentsForward(iErrors, iForm);
        	}
	        iProgress++;
	        if (!iErrors.isEmpty()) {
	        	setError(new Exception(((ActionMessage)iErrors.get().next()).getValues()[0].toString()));
	        }
		}

		@Override
		public String name() {
			List<String> names = new ArrayList<String>();
			if (iForm.getRollForwardDatePatterns()) names.add("date patterns");
            if (iForm.getRollForwardTimePatterns()) names.add("time patterns");
        	if (iForm.getRollForwardDepartments()) names.add("departments");
        	if (iForm.getRollForwardManagers()) names.add("managers");
        	if (iForm.getRollForwardRoomData()) names.add("roons");
        	if (iForm.getRollForwardSubjectAreas()) names.add("subjects");
        	if (iForm.getRollForwardInstructorData()) names.add("instructors");
        	if (iForm.getRollForwardCourseOfferings()) names.add("courses");
        	if (iForm.getRollForwardClassInstructors()) names.add("class instructors");
        	if (iForm.getAddNewCourseOfferings()) names.add("new courses");
        	if (iForm.getRollForwardExamConfiguration()) names.add("exam config");
        	if (iForm.getRollForwardMidtermExams()) names.add("midter exams");
        	if (iForm.getRollForwardFinalExams()) names.add("final exams");
        	if (iForm.getRollForwardStudents()) names.add("students");
        	String name = names.toString().replace("[", "").replace("]", "");
        	if (name.length() > 50) name = name.substring(0, 47) + "...";
        	return name;
		}

		@Override
		public double progress() {
			return 100 * iProgress / 14;
		}

		@Override
		public String type() {
			return "Roll Forward";
		}
		
	}
	
	protected void setToFromSessionsInForm(RollForwardSessionForm rollForwardSessionForm){
		List sessionList = new ArrayList();
		sessionList.addAll(Session.getAllSessions());
		rollForwardSessionForm.setFromSessions(new ArrayList());
		rollForwardSessionForm.setToSessions(new ArrayList());
		DepartmentStatusType statusType = DepartmentStatusType.findByRef("initial");
		Session session = null;
		for (int i = (sessionList.size() - 1); i >= 0; i--){
			session = (Session)sessionList.get(i);
			if (session.getStatusType().getUniqueId().equals(statusType.getUniqueId())) {
				rollForwardSessionForm.getToSessions().add(session);
				if (rollForwardSessionForm.getSessionToRollForwardTo() == null){
					rollForwardSessionForm.setSessionToRollForwardTo(session.getUniqueId());
				}
			} else {
				rollForwardSessionForm.getFromSessions().add(session);				
			}
		}
	}
	
	protected Set getSubjectAreas(Long selectedSessionId) {
		Set subjects = new TreeSet();
		Session session = null;
		if (selectedSessionId == null){
			DepartmentStatusType statusType = DepartmentStatusType.findByRef("initial");
			boolean found = false;
			TreeSet allSessions = Session.getAllSessions();
			List sessionList = new ArrayList();
			sessionList.addAll(Session.getAllSessions());
			for (int i = (sessionList.size() - 1); i >= 0; i--){
				session = (Session)sessionList.get(i);
				if (session.getStatusType().getUniqueId().equals(statusType.getUniqueId())){
					found =  true;
				}
			}
			if (!found){
				session = null;
				if (allSessions.size() > 0){
					session = (Session)allSessions.last();
				}
			}
		} else {
			session = Session.getSessionById(selectedSessionId);
		}
		
		if (session != null) subjects = session.getSubjectAreas();
		return(subjects);
	}

}
