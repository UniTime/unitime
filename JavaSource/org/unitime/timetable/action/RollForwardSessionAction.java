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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.hibernate.Transaction;
import org.unitime.commons.web.WebTable;
import org.unitime.commons.web.WebTable.WebTableLine;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.form.RollForwardSessionForm;
import org.unitime.timetable.gwt.command.server.GwtRpcServlet;
import org.unitime.timetable.gwt.shared.ReservationInterface;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.PointInTimeData;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.PointInTimeDataDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.SessionRollForward;
import org.unitime.timetable.util.queue.QueueItem;


/** 
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
@Action(value = "rollForwardSession", results = {
		@Result(name = "displayRollForwardSessionForm", type = "tiles", location = "rollForwardSession.tiles")
	})
@TilesDefinition(name = "rollForwardSession.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Roll Forward Session"),
		@TilesPutAttribute(name = "body", value = "/admin/rollForwardSession.jsp")
	})
public class RollForwardSessionAction extends UniTimeAction<RollForwardSessionForm> {
	private static final long serialVersionUID = -5982958315864704198L;
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);
	
	private String remove;
	
	public String getRemove() { return remove; }
	public void setRemove(String remove) { this.remove = remove; }
	
	@Override
	public String execute() throws Exception {
		if (form == null) form = new RollForwardSessionForm();
        
        // Get operation
           
        if (MSG.actionRollForward().equals(op)) {
    		sessionContext.checkPermission(Right.SessionRollForward);

    		form.validate(this);
            if (!hasFieldErrors()) {
            	getSolverServerService().getQueueProcessor().add(new RollForwardQueueItem(
            			SessionDAO.getInstance().get(form.getSessionToRollForwardTo()), sessionContext.getUser(), (RollForwardSessionForm)form.clone()));
            }
        }

		if (remove != null && !remove.isEmpty()) {
			getSolverServerService().getQueueProcessor().remove(remove);
	    }
		
		WebTable queueTable = getQueueTable();
		if (queueTable != null && !queueTable.getLines().isEmpty()) {
			request.setAttribute("table", queueTable.printTable(WebTable.getOrder(sessionContext,"rollForwardSession.ord")));
		}
	    
		setToFromSessionsInForm();
		form.setSubjectAreas(getSubjectAreas(form.getSessionToRollForwardTo()));
		form.setDepartments(getDepartments(form.getSessionToRollForwardTo()));
		form.setFromPointInTimeDataSnapshots(getPointInTimeDataSnapshots(form.getSessionToRollForwardTo()));
		if (op == null)
			setExpirationDates(form);
		if (form.getSubpartLocationPrefsAction() == null){
			form.setSubpartLocationPrefsAction(SessionRollForward.ROLL_PREFS_ACTION);
		}
		if (form.getSubpartTimePrefsAction() == null){
			form.setSubpartTimePrefsAction(SessionRollForward.ROLL_PREFS_ACTION);			
		}
		if (form.getClassPrefsAction() == null){
			form.setClassPrefsAction(SessionRollForward.DO_NOT_ROLL_ACTION);
		}
		if (form.getRollForwardDistributions() == null)
			form.setRollForwardDistributions(SessionRollForward.DistributionMode.MIXED.name());
		if (form.getCancelledClassAction() == null) {
			form.setCancelledClassAction(SessionRollForward.CancelledClassAction.REOPEN.name());
		}
		if (form.getMidtermExamsPrefsAction() == null)
			form.setMidtermExamsPrefsAction(SessionRollForward.EXAMS_ROOM_PREFS);
		if (form.getFinalExamsPrefsAction() == null)
			form.setFinalExamsPrefsAction(SessionRollForward.EXAMS_ROOM_PREFS);

  		return "displayRollForwardSessionForm";
	}
	
	public WebTable getQueueTable() {
        WebTable.setOrder(sessionContext,"rollForwardSession.ord",request.getParameter("ord"),1);
		String log = request.getParameter("log");
		Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.TIME_SHORT);
		
		List<QueueItem> queue = getSolverServerService().getQueueProcessor().getItems(null, null, "Roll Forward");
		if (queue.isEmpty()) return null;
		WebTable table = new WebTable(9, null, "rollForwardSession.action?ord=%%",
				new String[] {
						MSG.fieldQueueName(),
						MSG.fieldQueueStatus(),
						MSG.fieldQueueProgress(),
						MSG.fieldQueueOwner(),
						MSG.fieldQueueSession(),
						MSG.fieldQueueCreated(),
						MSG.fieldQueueStarted(),
						MSG.fieldQueueFinished(),
						MSG.fieldQueueOutput()},
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
				delete = "<img src='images/action_delete.png' border='0' onClick=\"if (confirm('Do you really want to remove this roll forward?')) document.location='rollForwardSession.action?remove="+item.getId()+"'; event.cancelBubble=true;\">";
			}
			WebTableLine line = table.addLine("onClick=\"document.location='rollForwardSession.action?log=" + item.getId() + "';\"",
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
				saveErrors(((RollForwardQueueItem)item).getErrors());
				line.setBgColor("rgb(168,187,225)");
			}
		}
		return table;
	}
	
	protected void saveErrors(List<RollForwardError> errors) {
		if (errors != null)
			for (RollForwardError e: errors)
				addFieldError(e.getType(), e.getMessage());
	}
	
	public static class RollForwardError implements Serializable {
		private static final long serialVersionUID = -8383522549018220760L;
		private String iType, iMessage;
		RollForwardError(String type, String message) {
			iType = type; iMessage = message;
		}
		public String getType() { return iType; }
		public String getMessage() { return iMessage; }
	}
	
	public static class RollForwardErrors extends ArrayList<RollForwardError> {
		private static final long serialVersionUID = 6152383035137322209L;

		public void addFieldError(String type, String message) {
			add(new RollForwardError(type, message));
		}
	}
	
	private static class RollForwardQueueItem extends QueueItem {
		private static final long serialVersionUID = 1L;
		private RollForwardSessionForm iForm;
		private int iProgress = 0;
		private RollForwardErrors iErrors = new RollForwardErrors();
		
		public RollForwardQueueItem(Session session, UserContext owner, RollForwardSessionForm form) {
			super(session, owner);
			iForm = form;
		}
		
		public RollForwardErrors getErrors() {
			return iErrors;
		}
		
		public RollForwardSessionForm getForm() {
			return iForm;
		}
		
		@Override
		public void error(Object message, Throwable t) {
			super.error(message, t);
			setError(t);
		}
		
		@Override
		protected void execute() throws Exception {
			SessionRollForward sessionRollForward = new SessionRollForward(this);
	        Session toAcadSession = Session.getSessionById(iForm.getSessionToRollForwardTo());
			if (toAcadSession == null){
	   			iErrors.addFieldError("mustSelectSession", MSG.errorRollForwardMissingToSession());
			}
			org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();

        	if (iErrors.isEmpty() && iForm.getRollForwardDepartments()) {
    			Transaction tx = hibSession.beginTransaction();
    			try {
    				setStatus(MSG.rollForwardDepartments() + " ...");
    				if (iForm.validateDepartmentRollForward(toAcadSession, iErrors))
    					sessionRollForward.rollDepartmentsForward(iErrors, iForm);	
    		        tx.commit();
    			} catch (Exception e) {
    				tx.rollback();
    				error(MSG.errorRollForwardFailedAll(MSG.rollForwardDepartments()), e);
    				iErrors.addFieldError("rollForward", e.getMessage());
    			}
    			hibSession.clear();
	        }
	        iProgress++;

        	if (iErrors.isEmpty() && iForm.getRollForwardSessionConfig()) {
        		Transaction tx = hibSession.beginTransaction();
    			try {
    				setStatus(MSG.rollForwardSessionConfiguration() + " ...");
    				sessionRollForward.rollSessionConfigurationForward(iErrors, iForm);
    				tx.commit();
    			} catch (Exception e) {
    				tx.rollback();
    				error(MSG.errorRollForwardFailedAll(MSG.rollForwardSessionConfiguration()), e);
    				iErrors.addFieldError("rollForward", e.getMessage());
    			}
    			hibSession.clear();
        	}
	        iProgress++;

        	if (iErrors.isEmpty() && iForm.getRollForwardManagers()) {
        		Transaction tx = hibSession.beginTransaction();
    			try {
    				setStatus(MSG.rollForwardManagers() + " ...");
    				if (iForm.validateManagerRollForward(toAcadSession, iErrors))
    					sessionRollForward.rollManagersForward(iErrors, iForm);
    				tx.commit();
    			} catch (Exception e) {
    				tx.rollback();
    				error(MSG.errorRollForwardFailedAll(MSG.rollForwardManagers()), e);
    				iErrors.addFieldError("rollForward", e.getMessage());
    			}
    			hibSession.clear();
        	}
	        iProgress++;

        	if (iErrors.isEmpty() && iForm.getRollForwardRoomData()) {
        		Transaction tx = hibSession.beginTransaction();
    			try {
    				setStatus(MSG.rollForwardRooms() + " ...");
    				if (iForm.validateBuildingAndRoomRollForward(toAcadSession, iErrors))
    					sessionRollForward.rollBuildingAndRoomDataForward(iErrors, iForm);
    				tx.commit();
    			} catch (Exception e) {
    				tx.rollback();
    				error(MSG.errorRollForwardFailedAll(MSG.rollForwardRooms()), e);
    				iErrors.addFieldError("rollForward", e.getMessage());
    			}
    			hibSession.clear();
        	}
	        iProgress++;
	        
	        if (iErrors.isEmpty() && iForm.getRollForwardDatePatterns()) {
        		Transaction tx = hibSession.beginTransaction();
    			try {
    				setStatus(MSG.rollForwardDatePatterns() + " ...");
    				if (iForm.validateDatePatternRollForward(toAcadSession, iErrors))
    					sessionRollForward.rollDatePatternsForward(iErrors, iForm);
    				tx.commit();
    			} catch (Exception e) {
    				tx.rollback();
    				error(MSG.errorRollForwardFailedAll(MSG.rollForwardDatePatterns()), e);
    				iErrors.addFieldError("rollForward", e.getMessage());
    			}
    			hibSession.clear();
	        }
	        iProgress++;
	        
            if (iErrors.isEmpty() && iForm.getRollForwardTimePatterns()) {
            	Transaction tx = hibSession.beginTransaction();
    			try {
    				setStatus(MSG.rollForwardTimePatterns() + " ...");
    				if (iForm.validateTimePatternRollForward(toAcadSession, iErrors))
    					sessionRollForward.rollTimePatternsForward(iErrors, iForm);
    				tx.commit();
    			} catch (Exception e) {
    				tx.rollback();
    				error(MSG.errorRollForwardFailedAll(MSG.rollForwardTimePatterns()), e);
    				iErrors.addFieldError("rollForward", e.getMessage());
    			}
    			hibSession.clear();
	        }
	        iProgress++;

	        if (iErrors.isEmpty() && iForm.getRollForwardLearningManagementSystems()) {
            	Transaction tx = hibSession.beginTransaction();
    			try {
    				setStatus(MSG.rollForwardLMSInfo() + " ...");
    				if (iForm.validateLearningManagementSystemRollForward(toAcadSession, iErrors))
    					sessionRollForward.rollLearningManagementSystemInfoForward(iErrors, iForm);
    				tx.commit();
    			} catch (Exception e) {
    				tx.rollback();
    				error(MSG.errorRollForwardFailedAll(MSG.rollForwardLMSInfo()), e);
    				iErrors.addFieldError("rollForward", e.getMessage());
    			}
    			hibSession.clear();
	        }
	        iProgress++;
	        
	        if (iErrors.isEmpty() && iForm.getRollForwardSubjectAreas()) {
	        	Transaction tx = hibSession.beginTransaction();
    			try {
    				setStatus(MSG.rollForwardSubjectAreas() + " ...");
    				if (iForm.validateSubjectAreaRollForward(toAcadSession, iErrors))
    					sessionRollForward.rollSubjectAreasForward(iErrors, iForm);
    				tx.commit();
    			} catch (Exception e) {
    				tx.rollback();
    				error(MSG.errorRollForwardFailedAll(MSG.rollForwardSubjectAreas()), e);
    				iErrors.addFieldError("rollForward", e.getMessage());
    			}
    			hibSession.clear();
	    	}
	        iProgress++;

	    	if (iErrors.isEmpty() && iForm.getRollForwardInstructorData()) {
	    		Transaction tx = hibSession.beginTransaction();
    			try {
    				setStatus(MSG.rollForwardInstructors() + " ...");
    				sessionRollForward.rollInstructorDataForward(iErrors, iForm);
    				tx.commit();
    			} catch (Exception e) {
    				tx.rollback();
    				error(MSG.errorRollForwardFailedAll(MSG.rollForwardInstructors()), e);
    				iErrors.addFieldError("rollForward", e.getMessage());
    			}
    			hibSession.clear();
	    	}
	        iProgress++;

			if (iErrors.isEmpty() && iForm.getRollForwardCourseOfferings()) {
				setStatus(MSG.rollForwardCourseOfferings() + " ...");
				sessionRollForward.rollCourseOfferingsForward(iErrors, iForm);
	    	}
	        iProgress++;

	    	if (iErrors.isEmpty() && iForm.getRollForwardClassInstructors()) {
	    		setStatus(MSG.rollForwardClassInstructors() + " ...");
	    		sessionRollForward.rollClassInstructorsForward(iErrors, iForm);
	    	}
	        iProgress++;

	    	if (iErrors.isEmpty() && iForm.getRollForwardOfferingCoordinators()) {
				setStatus(MSG.rollForwardOfferingCoordinators() + " ...");
				sessionRollForward.rollOfferingCoordinatorsForward(iErrors, iForm);
	    	}
			iProgress++;

	    	if (iErrors.isEmpty() && iForm.getRollForwardTeachingRequests()) {
				setStatus(MSG.rollForwardTeachingRequests() + " ...");
				sessionRollForward.rollTeachingRequestsForward(iErrors, iForm);
	    	}
	        iProgress++;

	    	if (iErrors.isEmpty() && iForm.getAddNewCourseOfferings()) {
	    		setStatus(MSG.rollForwardNewCourses() + " ...");
	    		sessionRollForward.addNewCourseOfferings(iErrors, iForm);
	    	}
	        iProgress++;

			if (iErrors.isEmpty() && iForm.getRollForwardExamConfiguration()) {
				Transaction tx = hibSession.beginTransaction();
    			try {
    				setStatus(MSG.rollForwardExamConfiguration() + " ...");
    				if (iForm.validateExamConfigurationRollForward(toAcadSession, iErrors))
    					sessionRollForward.rollExamConfigurationDataForward(iErrors, iForm);
    				tx.commit();
    			} catch (Exception e) {
    				tx.rollback();
    				error(MSG.errorRollForwardFailedAll(MSG.rollForwardExamConfiguration()), e);
    				iErrors.addFieldError("rollForward", e.getMessage());
    			}
    			hibSession.clear();
	    	}
	        iProgress++;

	    	if (iErrors.isEmpty() && iForm.getRollForwardMidtermExams()) {
	    		Transaction tx = hibSession.beginTransaction();
    			try {
    				setStatus(MSG.rollForwardMidtermExams() + " ...");
    	    		if (iForm.validateMidtermExamRollForward(toAcadSession, iErrors))
    	    			sessionRollForward.rollMidtermExamsForward(iErrors, iForm);
    	    		tx.commit();
    			} catch (Exception e) {
    				tx.rollback();
    				error(MSG.errorRollForwardFailedAll(MSG.rollForwardMidtermExams()), e);
    				iErrors.addFieldError("rollForward", e.getMessage());
    			}
    			hibSession.clear();
	    	}
	        iProgress++;

	    	if (iErrors.isEmpty() && iForm.getRollForwardFinalExams()) {
	    		Transaction tx = hibSession.beginTransaction();
    			try {
    				setStatus(MSG.rollForwardFinalExams() + " ...");
    				if (iForm.validateFinalExamRollForward(toAcadSession, iErrors))
    					sessionRollForward.rollFinalExamsForward(iErrors, iForm);
    				tx.commit();
    			} catch (Exception e) {
    				tx.rollback();
    				error(MSG.errorRollForwardFailedAll(MSG.rollForwardFinalExams()), e);
    				iErrors.addFieldError("rollForward", e.getMessage());
    			}
    			hibSession.clear();
	    	}
	        iProgress++;

			if (iErrors.isEmpty() && iForm.getRollForwardStudents()) {
				Transaction tx = hibSession.beginTransaction();
    			try {
    				setStatus(MSG.rollForwardStudents() + " ...");
    				if (iForm.validateLastLikeDemandRollForward(toAcadSession, iErrors))
    					sessionRollForward.rollStudentsForward(iErrors, iForm);
    				tx.commit();
    			} catch (Exception e) {
    				tx.rollback();
    				error(MSG.errorRollForwardFailedAll(MSG.rollForwardStudents()), e);
    				iErrors.addFieldError("rollForward", e.getMessage());
    			}
    			hibSession.clear();
	    	}
	        iProgress++;

	    	if (iErrors.isEmpty() && iForm.getRollForwardCurricula()) {
	    		Transaction tx = hibSession.beginTransaction();
    			try {
    				setStatus(MSG.rollForwardCurricula() + " ...");
    				if (iForm.validateCurriculaRollForward(toAcadSession, iErrors))
    					sessionRollForward.rollCurriculaForward(iErrors, iForm);
    				tx.commit();
    			} catch (Exception e) {
    				tx.rollback();
    				error(MSG.errorRollForwardFailedAll(MSG.rollForwardCurricula()), e);
    				iErrors.addFieldError("rollForward", e.getMessage());
    			}
    			hibSession.clear();
	    	}
	        iProgress++;

	    	if (iErrors.isEmpty() && iForm.getRollForwardReservations()) {
	    		Transaction tx = hibSession.beginTransaction();
    			try {
    				setStatus(MSG.rollForwardReservations() + " ...");
    	    	    sessionRollForward.rollReservationsForward(iErrors, iForm);
    	    	    tx.commit();
    			} catch (Exception e) {
    				tx.rollback();
    				error(MSG.errorRollForwardFailedAll(MSG.rollForwardReservations()), e);
    				iErrors.addFieldError("rollForward", e.getMessage());
    			}
    			hibSession.clear();
	    	}
	        iProgress++;

	        if (iErrors.isEmpty() && iForm.getRollForwardPeriodicTasks()) {
	        	Transaction tx = hibSession.beginTransaction();
    			try {
    				setStatus(MSG.rollForwardScheduledTasks() + " ...");
    	    	    sessionRollForward.rollPeriodicTasksForward(iErrors, iForm);
    	    	    tx.commit();
    			} catch (Exception e) {
    				tx.rollback();
    				error(MSG.errorRollForwardFailedAll(MSG.rollForwardScheduledTasks()), e);
    				iErrors.addFieldError("rollForward", e.getMessage());
    			}
    			hibSession.clear();
	    	}
	        iProgress++;

	        if (!iErrors.isEmpty()) {
	        	setError(new Exception(iErrors.get(0).getMessage()));
	        } else {
	        	log(MSG.logAllDone());
	        }
		}

		@Override
		public String name() {
			List<String> names = new ArrayList<String>();
        	if (iForm.getRollForwardDepartments()) names.add(MSG.rollForwardDepartments());
			if (iForm.getRollForwardSessionConfig()) names.add(MSG.rollForwardConfiguration());
        	if (iForm.getRollForwardManagers()) names.add(MSG.rollForwardManagers());
        	if (iForm.getRollForwardRoomData()) names.add(MSG.rollForwardRooms());
			if (iForm.getRollForwardDatePatterns()) names.add(MSG.rollForwardDatePatterns());
            if (iForm.getRollForwardTimePatterns()) names.add(MSG.rollForwardTimePatterns());
            if (iForm.getRollForwardLearningManagementSystems()) names.add(MSG.rollForwardLMS());
        	if (iForm.getRollForwardSubjectAreas()) names.add(MSG.rollForwardSubjectAreas());
        	if (iForm.getRollForwardInstructorData()) names.add(MSG.rollForwardInstructors());
        	if (iForm.getRollForwardCourseOfferings()) names.add(MSG.rollForwardCourseOfferings());
        	if (iForm.getRollForwardClassInstructors()) names.add(MSG.rollForwardClassInstructors());
        	if (iForm.getRollForwardOfferingCoordinators()) names.add(MSG.rollForwardOfferingCoordinators());
        	if (iForm.getRollForwardTeachingRequests()) names.add(MSG.rollForwardTeachingRequests());
        	if (iForm.getAddNewCourseOfferings()) names.add(MSG.rollForwardNewCourses());
        	if (iForm.getRollForwardExamConfiguration()) names.add(MSG.rollForwardExamConfiguration());
        	if (iForm.getRollForwardMidtermExams()) names.add(MSG.rollForwardMidtermExams());
        	if (iForm.getRollForwardFinalExams()) names.add(MSG.rollForwardFinalExams());
        	if (iForm.getRollForwardStudents()) names.add(MSG.rollForwardStudents());
        	if (iForm.getRollForwardCurricula()) names.add(MSG.rollForwardCurricula());
        	if (iForm.getRollForwardReservations()) names.add(MSG.rollForwardReservations());
        	if (iForm.getRollForwardPeriodicTasks()) names.add(MSG.rollForwardScheduledTasks());
        	String name = names.toString().replace("[", "").replace("]", "");
        	if (name.length() > 50) name = name.substring(0, 47) + "...";
        	return name;
		}

		@Override
		public double progress() {
			return 100 * iProgress / 21;
		}

		@Override
		public String type() {
			return "Roll Forward";
		}
		
	}
	
	protected void setToFromSessionsInForm(){
		List<Session> sessionList = new ArrayList<Session>();
		sessionList.addAll(Session.getAllSessions());
		List<Session> fromSessions = new ArrayList<Session>(); form.setFromSessions(fromSessions);
		List<Session> toSessions = new ArrayList<Session>(); form.setToSessions(toSessions);
		Session session = null;
		for (int i = (sessionList.size() - 1); i >= 0; i--){
			session = (Session)sessionList.get(i);
			if (session.getStatusType().isAllowRollForward()) {
				toSessions.add(session);
				if (form.getSessionToRollForwardTo() == null){
					form.setSessionToRollForwardTo(session.getUniqueId());
				}
			} else {
				fromSessions.add(session);				
			}
		}
		Long currentSessionId = form.getSessionToRollForwardTo();
		if (currentSessionId == null || currentSessionId <= 0l)
			currentSessionId = sessionContext.getUser().getCurrentAcademicSessionId();
		Session currentSession = (currentSessionId == null ? null : SessionDAO.getInstance().get(currentSessionId));
		if (currentSession != null) {
			Collections.sort(fromSessions, new SessionComparator(currentSession.getAcademicInitiative()));
			Collections.sort(toSessions, new SessionComparator(currentSession.getAcademicInitiative()));
		}
	}
	
	protected Set<SubjectArea> getSubjectAreas(Long selectedSessionId) {
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
		
		Set<SubjectArea> subjects = new TreeSet<SubjectArea>();
		if (session != null) subjects.addAll(session.getSubjectAreas());
		return(subjects);
	}
	
	protected Set<Department> getDepartments(Long selectedSessionId) {
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
		
		Set<Department> departments = new TreeSet<Department>();
		if (session != null) departments.addAll(session.getDepartments());
		return(departments);
	}

	protected Set<PointInTimeData> getPointInTimeDataSnapshots(Long selectedSessionId) {
		Set<PointInTimeData> pointInTimeDataSnapshots = new TreeSet<PointInTimeData>();
		if (selectedSessionId != null){
			StringBuilder sb = new StringBuilder();
			
			sb.append("from PointInTimeData pitd where pitd.session.uniqueId in (select distinct rfio.session.uniqueId ")
			  .append(" from InstructionalOffering rfio, Session s inner join s.instructionalOfferings as io ")
			  .append(" where s.uniqueId = :sessId ")
              .append(" and rfio.uniqueId = io.uniqueIdRolledForwardFrom )")
              .append(" and pitd.savedSuccessfully = true ");
			pointInTimeDataSnapshots.addAll((List<PointInTimeData>)PointInTimeDataDAO.getInstance()
					.getSession()
					.createQuery(sb.toString(), PointInTimeData.class)
					.setParameter("sessId", selectedSessionId)
					.list());
		}
		
		return(pointInTimeDataSnapshots);

	}

	protected void setExpirationDates(RollForwardSessionForm form) {
		if (form.getSessionToRollForwardTo() != null) {
			ReservationInterface.DefaultExpirationDates dates = GwtRpcServlet.execute(new ReservationInterface.ReservationDefaultExpirationDatesRpcRequest(form.getSessionToRollForwardTo()), getApplicationContext(), sessionContext);
			if (dates != null) {
				Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_ENTRY_FORMAT);
				form.setExpirationCourseReservations(dates.hasExpirationDate("course") ? df.format(dates.getExpirationDate("course")) : null);
				form.setExpirationCurriculumReservations(dates.hasExpirationDate("curriculum") ? df.format(dates.getExpirationDate("curriculum")) : null);
				form.setExpirationGroupReservations(dates.hasExpirationDate("group") ? df.format(dates.getExpirationDate("group")) : null);
				form.setStartDateCourseReservations(dates.hasStartDate("course") ? df.format(dates.getStartDate("course")) : null);
				form.setStartDateCurriculumReservations(dates.hasStartDate("curriculum") ? df.format(dates.getStartDate("curriculum")) : null);
				form.setStartDateGroupReservations(dates.hasStartDate("group") ? df.format(dates.getStartDate("group")) : null);
			}
		} else {
			form.setExpirationCourseReservations(null);
			form.setExpirationCurriculumReservations(null);
			form.setExpirationGroupReservations(null);
			form.setStartDateCourseReservations(null);
			form.setStartDateCurriculumReservations(null);
			form.setStartDateGroupReservations(null);
		}
	}
	
	public static class SessionComparator implements Comparator<Session> {
		private String iPreferCampus = null;
		public SessionComparator(String currentCampus) {
			iPreferCampus = currentCampus;
		}
		@Override
		public int compare(Session s1, Session s2) {
			boolean c1 = s1.getAcademicInitiative().equals(iPreferCampus);
			boolean c2 = s2.getAcademicInitiative().equals(iPreferCampus);
			if (c1 != c2)
				return (c1 ? -1 : 1);
			int cmp = s1.getAcademicInitiative().compareTo(s2.getAcademicInitiative());
			if (cmp!=0) return cmp;
			
			cmp = s2.getSessionBeginDateTime().compareTo(s1.getSessionBeginDateTime());
			if (cmp!=0) return cmp;
			
			return s1.getUniqueId().compareTo(s2.getUniqueId());
		}
	}

}
