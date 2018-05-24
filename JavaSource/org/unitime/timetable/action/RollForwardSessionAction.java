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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.unitime.commons.web.WebTable;
import org.unitime.commons.web.WebTable.WebTableLine;
import org.unitime.timetable.form.RollForwardSessionForm;
import org.unitime.timetable.gwt.command.server.GwtRpcServlet;
import org.unitime.timetable.gwt.shared.ReservationInterface;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.PointInTimeData;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.PointInTimeDataDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.service.SolverServerService;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.SessionRollForward;
import org.unitime.timetable.util.queue.QueueItem;


/** 
 * MyEclipse Struts
 * Creation date: 02-27-2007
 * 
 * XDoclet definition:
 * @struts.action path="/exportSessionToMsf" name="exportSessionToMsfForm" input="/form/exportSessionToMsf.jsp" scope="request" validate="true"
 *
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
@Service("/rollForwardSession")
public class RollForwardSessionAction extends Action {
	
	@Autowired SessionContext sessionContext;
	
	@Autowired ApplicationContext applicationContext;
	
	@Autowired SolverServerService solverServerService;
	
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
		
        MessageResources rsc = getResources(request);
        
        RollForwardSessionForm rollForwardSessionForm = (RollForwardSessionForm) form;
        // Get operation
        String op = request.getParameter("op");		  
           
        if (op != null && op.equals(rsc.getMessage("button.rollForward"))) {
    		sessionContext.checkPermission(Right.SessionRollForward);

    		ActionMessages errors = rollForwardSessionForm.validate(mapping, request);
            if (errors.isEmpty()) {
            	solverServerService.getQueueProcessor().add(new RollForwardQueueItem(
            			SessionDAO.getInstance().get(rollForwardSessionForm.getSessionToRollForwardTo()), sessionContext.getUser(), (RollForwardSessionForm)rollForwardSessionForm.clone()));
            } else {
                saveErrors(request, errors);
            }
        }

		if (request.getParameter("remove") != null) {
			solverServerService.getQueueProcessor().remove(request.getParameter("remove"));
	    }
		WebTable table = getQueueTable(request, rollForwardSessionForm);
	    if (table != null) {
	    	request.setAttribute("table", table.printTable(WebTable.getOrder(sessionContext,"rollForwardSession.ord")));
	    }
	    
		setToFromSessionsInForm(rollForwardSessionForm);
		rollForwardSessionForm.setSubjectAreas(getSubjectAreas(rollForwardSessionForm.getSessionToRollForwardTo()));
		rollForwardSessionForm.setDepartments(getDepartments(rollForwardSessionForm.getSessionToRollForwardTo()));
		rollForwardSessionForm.setFromPointInTimeDataSnapshots(getPointInTimeDataSnapshots(rollForwardSessionForm.getSessionToRollForwardTo()));
		if (op == null)
			setExpirationDates(rollForwardSessionForm);
		if (rollForwardSessionForm.getSubpartLocationPrefsAction() == null){
			rollForwardSessionForm.setSubpartLocationPrefsAction(SessionRollForward.ROLL_PREFS_ACTION);
		}
		if (rollForwardSessionForm.getSubpartTimePrefsAction() == null){
			rollForwardSessionForm.setSubpartTimePrefsAction(SessionRollForward.ROLL_PREFS_ACTION);			
		}
		if (rollForwardSessionForm.getClassPrefsAction() == null){
			rollForwardSessionForm.setClassPrefsAction(SessionRollForward.DO_NOT_ROLL_ACTION);
		}
		if (rollForwardSessionForm.getRollForwardDistributions() == null)
			rollForwardSessionForm.setRollForwardDistributions(SessionRollForward.DistributionMode.MIXED.name());
		if (rollForwardSessionForm.getCancelledClassAction() == null) {
			rollForwardSessionForm.setCancelledClassAction(SessionRollForward.CancelledClassAction.REOPEN.name());
		}
		if (rollForwardSessionForm.getMidtermExamsPrefsAction() == null)
			rollForwardSessionForm.setMidtermExamsPrefsAction(SessionRollForward.EXAMS_ROOM_PREFS);
		if (rollForwardSessionForm.getFinalExamsPrefsAction() == null)
			rollForwardSessionForm.setFinalExamsPrefsAction(SessionRollForward.EXAMS_ROOM_PREFS);

  		return mapping.findForward("displayRollForwardSessionForm");
	}
	
	private WebTable getQueueTable(HttpServletRequest request, RollForwardSessionForm form) {
        WebTable.setOrder(sessionContext,"rollForwardSession.ord",request.getParameter("ord"),1);
		String log = request.getParameter("log");
		Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.TIME_SHORT);
		
		List<QueueItem> queue = solverServerService.getQueueProcessor().getItems(null, null, "Roll Forward");
		if (queue.isEmpty()) return null;
		WebTable table = new WebTable(9, null, "rollForwardSession.do?ord=%%",
				new String[] { "Name", "Status", "Progress", "Owner", "Session", "Created", "Started", "Finished", "Output"},
				new String[] { "left", "left", "right", "left", "left", "left", "left", "left", "center"},
				new boolean[] { true, true, true, true, true, true, true, true, true});
		Date now = new Date();
		long timeToShow = 24 * 1000 * 60 * 60;
		for (QueueItem item: queue) {
			if (item.finished() != null && now.getTime() - item.finished().getTime() > timeToShow) continue;
			if (item.getSession() == null) continue;
			String name = item.name();
			if (name.length() > 60) name = name.substring(0, 57) + "...";
			String delete = null;
			if (sessionContext.getUser().getExternalUserId().equals(item.getOwnerId()) && (item.started() == null || item.finished() != null)) {
				delete = "<img src='images/action_delete.png' border='0' onClick=\"if (confirm('Do you really want to remove this roll forward?')) document.location='rollForwardSession.do?remove="+item.getId()+"'; event.cancelBubble=true;\">";
			}
			WebTableLine line = table.addLine("onClick=\"document.location='rollForwardSession.do?log=" + item.getId() + "';\"",
					new String[] {
						name + (delete == null ? "": " " + delete),
						item.status(),
						(item.progress() <= 0.0 || item.progress() >= 1.0 ? "" : String.valueOf(Math.round(100 * item.progress())) + "%"),
						item.getOwnerName(),
						item.getSession().getLabel(),
						df.format(item.created()),
						item.started() == null ? "" : df.format(item.started()),
						item.finished() == null ? "" : df.format(item.finished()),
						item.hasOutput() ? "<A href='"+item.getOutputLink()+"'>"+item.getOutputName().substring(item.getOutputName().lastIndexOf('.') + 1).toUpperCase()+"</A>" : ""
					},
					new Comparable[] {
						item.created().getTime(),
						item.status(),
						item.progress(),
						item.getOwnerName(),
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
		}
		return table;
	}
	
	private static class RollForwardQueueItem extends QueueItem {
		private static final long serialVersionUID = 1L;
		private RollForwardSessionForm iForm;
		private int iProgress = 0;
		private ActionErrors iErrors = new ActionErrors();
		
		public RollForwardQueueItem(Session session, UserContext owner, RollForwardSessionForm form) {
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
	        SessionRollForward sessionRollForward = new SessionRollForward(this);
	        Session toAcadSession = Session.getSessionById(iForm.getSessionToRollForwardTo());
			if (toAcadSession == null){
	   			iErrors.add("mustSelectSession", new ActionMessage("errors.rollForward.missingToSession"));
			}
			if (iErrors.isEmpty()){
				iForm.validateDepartmentRollForward(toAcadSession, iErrors);
			}
        	if (iErrors.isEmpty() && iForm.getRollForwardDepartments()) {
				setStatus("Departments ...");
	        	sessionRollForward.rollDepartmentsForward(iErrors, iForm);	
	        }
	        iProgress++;
			if (iErrors.isEmpty()){
				iForm.validateCurriculaRollForward(toAcadSession, iErrors);
			}
        	if (iErrors.isEmpty() && iForm.getRollForwardSessionConfig()) {
				setStatus("Session Configuration ...");
        	    sessionRollForward.rollSessionConfigurationForward(iErrors, iForm);
        	}
	        iProgress++;
			if (iErrors.isEmpty()){
				iForm.validateManagerRollForward(toAcadSession, iErrors);
			}
        	if (iErrors.isEmpty() && iForm.getRollForwardManagers()) {
				setStatus("Managers ...");
        		sessionRollForward.rollManagersForward(iErrors, iForm);
        	}
	        iProgress++;
			if (iErrors.isEmpty()){
				iForm.validateBuildingAndRoomRollForward(toAcadSession, iErrors);
			}
        	if (iErrors.isEmpty() && iForm.getRollForwardRoomData()) {
				setStatus("Rooms ...");
        		sessionRollForward.rollBuildingAndRoomDataForward(iErrors, iForm);
        	}
	        iProgress++;
			if (iErrors.isEmpty()){
				iForm.validateDatePatternRollForward(toAcadSession, iErrors);
			}
	        if (iErrors.isEmpty() && iForm.getRollForwardDatePatterns()) {
				setStatus("Date patterns ...");
	        	sessionRollForward.rollDatePatternsForward(iErrors, iForm);
	        }
	        iProgress++;
			if (iErrors.isEmpty()){
				iForm.validateTimePatternRollForward(toAcadSession, iErrors);
			}
            if (iErrors.isEmpty() && iForm.getRollForwardTimePatterns()) {
				setStatus("Time patterns ...");
	        	sessionRollForward.rollTimePatternsForward(iErrors, iForm);
	        }
	        iProgress++;
			if (iErrors.isEmpty()){
				iForm.validateSubjectAreaRollForward(toAcadSession, iErrors);
			}
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
			if (iErrors.isEmpty()){
				iForm.validateCourseOfferingRollForward(toAcadSession, iErrors);
			}
			if (iErrors.isEmpty() && iForm.getRollForwardCourseOfferings()) {
				setStatus("Courses ...");
        		sessionRollForward.rollCourseOfferingsForward(iErrors, iForm);
        	}
	        iProgress++;
			if (iErrors.isEmpty()){
				iForm.validateClassInstructorRollForward(toAcadSession, iErrors);
			}
        	if (iErrors.isEmpty() && iForm.getRollForwardClassInstructors()) {
				setStatus("Class instructors ...");
        		sessionRollForward.rollClassInstructorsForward(iErrors, iForm);
        	}
	        iProgress++;
	        if (iErrors.isEmpty()){
				iForm.validateOfferingCoordinatorsRollForward(toAcadSession, iErrors);
			}
        	if (iErrors.isEmpty() && iForm.getRollForwardOfferingCoordinators()) {
				setStatus("Offering coordinators ...");
        		sessionRollForward.rollOfferingCoordinatorsForward(iErrors, iForm);
        	}
			iProgress++;
			if (iErrors.isEmpty()){
				iForm.validateTeachingRequestsRollForward(toAcadSession, iErrors);
			}
        	if (iErrors.isEmpty() && iForm.getRollForwardTeachingRequests()) {
				setStatus("Teaching requests ...");
        		sessionRollForward.rollTeachingRequestsForward(iErrors, iForm);
        	}
	        iProgress++;
        	if (iErrors.isEmpty() && iForm.getAddNewCourseOfferings()) {
				setStatus("New courses ...");
        		sessionRollForward.addNewCourseOfferings(iErrors, iForm);
        	}
	        iProgress++;
			if (iErrors.isEmpty()){
				iForm.validateExamConfigurationRollForward(toAcadSession, iErrors);
			}
			if (iErrors.isEmpty() && iForm.getRollForwardExamConfiguration()) {
				setStatus("Exam config ...");
        		sessionRollForward.rollExamConfigurationDataForward(iErrors, iForm);
        	}
	        iProgress++;
			if (iErrors.isEmpty()){
				iForm.validateMidtermExamRollForward(toAcadSession, iErrors);
			}
        	if (iErrors.isEmpty() && iForm.getRollForwardMidtermExams()) {
				setStatus("Midterm exams ...");
        		sessionRollForward.rollMidtermExamsForward(iErrors, iForm);
        	}
	        iProgress++;
			if (iErrors.isEmpty()){
				iForm.validateFinalExamRollForward(toAcadSession, iErrors);
			}
        	if (iErrors.isEmpty() && iForm.getRollForwardFinalExams()) {
				setStatus("Final exams ...");
        		sessionRollForward.rollFinalExamsForward(iErrors, iForm);
        	}
	        iProgress++;
			if (iErrors.isEmpty()){
				iForm.validateLastLikeDemandRollForward(toAcadSession, iErrors);
			}
			if (iErrors.isEmpty() && iForm.getRollForwardStudents()) {
				setStatus("Students ...");
        	    sessionRollForward.rollStudentsForward(iErrors, iForm);
        	}
	        iProgress++;
			if (iErrors.isEmpty()){
				iForm.validateCurriculaRollForward(toAcadSession, iErrors);
			}
        	if (iErrors.isEmpty() && iForm.getRollForwardCurricula()) {
				setStatus("Curricula ...");
        	    sessionRollForward.rollCurriculaForward(iErrors, iForm);
        	}
	        iProgress++;
        	if (iErrors.isEmpty() && iForm.getRollForwardReservations()) {
				setStatus("Reservations ...");
        	    sessionRollForward.rollReservationsForward(iErrors, iForm);
        	}
	        iProgress++;
	        if (iErrors.isEmpty() && iForm.getRollForwardPeriodicTasks()) {
				setStatus("Scheduled Tasks ...");
        	    sessionRollForward.rollPeriodicTasksForward(iErrors, iForm);
        	}
	        iProgress++;
	        if (!iErrors.isEmpty()) {
	        	setError(new Exception(((ActionMessage)iErrors.get().next()).getValues()[0].toString()));
	        } else {
	        	log("All done.");
	        }
		}

		@Override
		public String name() {
			List<String> names = new ArrayList<String>();
        	if (iForm.getRollForwardDepartments()) names.add("departments");
			if (iForm.getRollForwardSessionConfig()) names.add("configuration");
        	if (iForm.getRollForwardManagers()) names.add("managers");
        	if (iForm.getRollForwardRoomData()) names.add("rooms");
			if (iForm.getRollForwardDatePatterns()) names.add("date patterns");
            if (iForm.getRollForwardTimePatterns()) names.add("time patterns");
        	if (iForm.getRollForwardSubjectAreas()) names.add("subjects");
        	if (iForm.getRollForwardInstructorData()) names.add("instructors");
        	if (iForm.getRollForwardCourseOfferings()) names.add("courses");
        	if (iForm.getRollForwardClassInstructors()) names.add("class instructors");
        	if (iForm.getRollForwardOfferingCoordinators()) names.add("offering coordinators");
        	if (iForm.getRollForwardTeachingRequests()) names.add("teaching requests");
        	if (iForm.getAddNewCourseOfferings()) names.add("new courses");
        	if (iForm.getRollForwardExamConfiguration()) names.add("exam config");
        	if (iForm.getRollForwardMidtermExams()) names.add("midterm exams");
        	if (iForm.getRollForwardFinalExams()) names.add("final exams");
        	if (iForm.getRollForwardStudents()) names.add("students");
        	if (iForm.getRollForwardCurricula()) names.add("curricula");
        	if (iForm.getRollForwardReservations()) names.add("reservations");
        	if (iForm.getRollForwardPeriodicTasks()) names.add("scheduled tasks");
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
		List<Session> sessionList = new ArrayList<Session>();
		sessionList.addAll(Session.getAllSessions());
		rollForwardSessionForm.setFromSessions(new ArrayList<Session>());
		rollForwardSessionForm.setToSessions(new ArrayList<Session>());
		Session session = null;
		for (int i = (sessionList.size() - 1); i >= 0; i--){
			session = (Session)sessionList.get(i);
			if (session.getStatusType().isAllowRollForward()) {
				rollForwardSessionForm.getToSessions().add(session);
				if (rollForwardSessionForm.getSessionToRollForwardTo() == null){
					rollForwardSessionForm.setSessionToRollForwardTo(session.getUniqueId());
				}
			} else {
				rollForwardSessionForm.getFromSessions().add(session);				
			}
		}
	}
	
	protected Set<SubjectArea> getSubjectAreas(Long selectedSessionId) {
		Set<SubjectArea> subjects = new TreeSet<SubjectArea>();
		Session session = null;
		if (selectedSessionId == null){
			boolean found = false;
			TreeSet<Session> allSessions = Session.getAllSessions();
			List<Session> sessionList = new ArrayList<Session>();
			sessionList.addAll(Session.getAllSessions());
			for (int i = (sessionList.size() - 1); i >= 0; i--){
				session = (Session)sessionList.get(i);
				if (session.getStatusType().isAllowRollForward()){
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
	
	protected Set<Department> getDepartments(Long selectedSessionId) {
		Set<Department> departments = new TreeSet<Department>();
		Session session = null;
		if (selectedSessionId == null){
			boolean found = false;
			TreeSet<Session> allSessions = Session.getAllSessions();
			List<Session> sessionList = new ArrayList<Session>();
			sessionList.addAll(Session.getAllSessions());
			for (int i = (sessionList.size() - 1); i >= 0; i--){
				session = (Session)sessionList.get(i);
				if (session.getStatusType().isAllowRollForward()){
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
		
		if (session != null) departments = session.getDepartments();
		return(departments);
	}

	protected Set<PointInTimeData> getPointInTimeDataSnapshots(Long selectedSessionId) {
		Set<PointInTimeData> pointInTimeDataSnapshots = new TreeSet<PointInTimeData>();
		if (selectedSessionId != null){
			StringBuilder sb = new StringBuilder();
			
			sb.append("from PointInTimeData pitd where pitd.session.uniqueId in (select distinct rfio.session ")
			  .append(" from InstructionalOffering rfio, Session s inner join s.instructionalOfferings as io ")
			  .append(" where s.uniqueId = :sessId ")
              .append(" and rfio.uniqueId = io.uniqueIdRolledForwardFrom )")
              .append(" and pitd.savedSuccessfully = true ");
			pointInTimeDataSnapshots.addAll((List<PointInTimeData>)PointInTimeDataDAO.getInstance()
					.getSession()
					.createQuery(sb.toString())
					.setLong("sessId", selectedSessionId.longValue())
					.list());
		}
		
		return(pointInTimeDataSnapshots);

	}

	protected void setExpirationDates(RollForwardSessionForm form) {
		if (form.getSessionToRollForwardTo() != null) {
			ReservationInterface.DefaultExpirationDates dates = GwtRpcServlet.execute(new ReservationInterface.ReservationDefaultExpirationDatesRpcRequest(form.getSessionToRollForwardTo()), applicationContext, sessionContext);
			if (dates != null) {
				Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_ENTRY_FORMAT);
				form.setExpirationCourseReservations(dates.hasExpirationDate("course") ? df.format(dates.getExpirationDate("course")) : null);
				form.setExpirationCurriculumReservations(dates.hasExpirationDate("curriculum") ? df.format(dates.getExpirationDate("curriculum")) : null);
				form.setExpirationGroupReservations(dates.hasExpirationDate("group") ? df.format(dates.getExpirationDate("group")) : null);
			}
		} else {
			form.setExpirationCourseReservations(null);
			form.setExpirationCurriculumReservations(null);
			form.setExpirationGroupReservations(null);
		}
	}

}
