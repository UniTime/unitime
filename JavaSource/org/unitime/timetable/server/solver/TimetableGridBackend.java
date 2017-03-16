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
package org.unitime.timetable.server.solver;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.hibernate.Query;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.server.Query.TermMatcher;
import org.unitime.timetable.gwt.shared.TimetableGridInterface.TimetableGridRequest;
import org.unitime.timetable.gwt.shared.TimetableGridInterface.TimetableGridResponse;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;
import org.unitime.timetable.gwt.shared.SolverInterface.PageMessage;
import org.unitime.timetable.gwt.shared.SolverInterface.PageMessageType;
import org.unitime.timetable.gwt.shared.TimetableGridInterface.TimetableGridCell;
import org.unitime.timetable.gwt.shared.TimetableGridInterface.TimetableGridModel;
import org.unitime.timetable.model.ConstraintInfo;
import org.unitime.timetable.model.CurriculumClassification;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.SolutionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.solver.TimetableGridHelper.BgMode;
import org.unitime.timetable.server.solver.TimetableGridHelper.OrderBy;
import org.unitime.timetable.server.solver.TimetableGridHelper.ResourceType;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.solver.ui.StudentGroupInfo;
import org.unitime.timetable.solver.ui.TimetableInfo;
import org.unitime.timetable.util.RoomAvailability;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(TimetableGridRequest.class)
public class TimetableGridBackend implements GwtRpcImplementation<TimetableGridRequest, TimetableGridResponse> {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	
	@Autowired SolverService<SolverProxy> courseTimetablingSolverService;
	
	@Override
	public TimetableGridResponse execute(TimetableGridRequest request, SessionContext context) {
		context.checkPermission(Right.TimetableGrid);
		
		context.getUser().setProperty("TimetableGridTable.week", request.getFilter().getParameterValue("weeks"));
		context.getUser().setProperty("TimetableGridTable.resourceType", request.getFilter().getParameterValue("resource"));
		context.getUser().setProperty("TimetableGridTable.findString", request.getFilter().getParameterValue("filter"));
		context.getUser().setProperty("TimetableGridTable.day", request.getFilter().getParameterValue("days"));
		context.getUser().setProperty("TimetableGridTable.times", request.getFilter().getParameterValue("times"));
		context.getUser().setProperty("TimetableGridTable.dispMode", request.getFilter().getParameterValue("dispMode"));
		context.getUser().setProperty("TimetableGridTable.bgMode", request.getFilter().getParameterValue("background"));
		context.getUser().setProperty("TimetableGridTable.showUselessTimes", request.getFilter().getParameterValue("showFreeTimes"));
		context.getUser().setProperty("TimetableGridTable.showComments", request.getFilter().getParameterValue("showPreferences"));
		context.getUser().setProperty("TimetableGridTable.showInstructors", request.getFilter().getParameterValue("showInstructors"));
		context.getUser().setProperty("TimetableGridTable.showEvents", request.getFilter().getParameterValue("showEvents"));
		context.getUser().setProperty("TimetableGridTable.showTimes", request.getFilter().getParameterValue("showTimes"));
		context.getUser().setProperty("TimetableGridTable.orderBy", request.getFilter().getParameterValue("orderBy"));
		
		TimetableGridResponse response = new TimetableGridResponse();
		
		Session acadSession = SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId());
		DatePattern defaultDatePattern = acadSession.getDefaultDatePatternNotNull();
		response.setDefaultDatePatternName(defaultDatePattern == null ? null : defaultDatePattern.getName());
    	TimetableGridContext cx = new TimetableGridContext(request.getFilter(), acadSession);
    	String instructorFormat = context.getUser().getProperty(UserProperty.NameFormat);
    	if (instructorFormat != null)
    		cx.setInstructorNameFormat(instructorFormat);
    	response.setWeekOffset(cx.getWeekOffset());
    	
    	SolverProxy solver = courseTimetablingSolverService.getSolver();
    	if (solver != null) {
    		cx.setInstructorNameFormat(solver.getProperties().getProperty("General.InstructorFormat", cx.getInstructorNameFormat()));
    		boolean fixInstructors = ApplicationProperty.TimeGridFixInstructors.isTrue() && cx.isShowInstructor();
    		List<TimetableGridModel> models = solver.getTimetableGridTables(cx);
    		if (models != null)
    			for (TimetableGridModel model: models) {
    				if (fixInstructors)
    					TimetableGridSolverHelper.fixInstructors(model, cx);
    				if (cx.isShowCrossLists())
    					TimetableGridSolverHelper.addCrosslistedNames(model, cx);
    				TimetableGridHelper.computeIndexes(model, cx);
    				response.addModel(model);
    			}
    		String ts = null;
        	try {
        		ts = solver.getProperties().getProperty("RoomAvailability.TimeStamp");
        	} catch (Exception e) {}
            if (ts == null)
            	response.addPageMessage(new PageMessage(PageMessageType.WARNING, MESSAGES.warnCourseSolverNoRoomAvailability()));
            else
            	response.addPageMessage(new PageMessage(PageMessageType.INFO, MESSAGES.infoCourseSolverRoomAvailabilityLastUpdated(ts)));
    	} else {
        	org.unitime.timetable.gwt.server.Query filter = null;
        	String filterStr = request.getFilter().getParameterValue("filter");
        	if (filterStr != null && !filterStr.trim().isEmpty())
        		filter = new org.unitime.timetable.gwt.server.Query(filterStr);
        	
        	String solutionIdsStr = (String)context.getAttribute(SessionAttribute.SelectedSolution);
        	if (solutionIdsStr == null || solutionIdsStr.isEmpty()) {
        		for (SolverGroup g: SolverGroup.getUserSolverGroups(context.getUser())) {
            		for (Long id: (List<Long>)SolutionDAO.getInstance().getSession().createQuery(
            				"select s.uniqueId from Solution s where s.commited = true and s.owner = :groupId")
            				.setLong("groupId", g.getUniqueId()).setCacheable(true).list()) {
            			if (solutionIdsStr == null)
            				solutionIdsStr = id.toString();
            			else
            				solutionIdsStr += (solutionIdsStr.isEmpty() ? "" : ",") + id;
            		}
        		}
        	}
    		if (solutionIdsStr == null || solutionIdsStr.isEmpty()) 
    			throw new GwtRpcException(MESSAGES.errorTimetableGridNoSolution());
    		
    		Transaction tx = null;
    		try {
    			SolutionDAO dao = new SolutionDAO();
    			org.hibernate.Session hibSession = dao.getSession();
    			if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
    				tx = hibSession.beginTransaction();
    			
    			if (cx.getResourceType() == ResourceType.ROOM.ordinal()) {
    				if (RoomAvailability.getInstance() != null) {
    			        RoomAvailability.getInstance().activate(acadSession, cx.getSessionStartDate(), cx.getSessionEndDate(), RoomAvailabilityInterface.sClassType, false);
    		            String ts = RoomAvailability.getInstance().getTimeStamp(cx.getSessionStartDate(), cx.getSessionEndDate(), RoomAvailabilityInterface.sClassType);
    		            if (ts == null)
    		            	response.addPageMessage(new PageMessage(PageMessageType.WARNING, MESSAGES.warnCourseSolverNoRoomAvailability()));
    		            else
    		            	response.addPageMessage(new PageMessage(PageMessageType.INFO, MESSAGES.infoCourseSolverRoomAvailabilityLastUpdated(ts)));
    				}
    				
    				Query q = hibSession.createQuery(
    						"select distinct r from "+
    						"Location as r inner join r.assignments as a where "+
    						"a.solution.uniqueId in ("+solutionIdsStr+")");
    				q.setCacheable(true);
    				for (Iterator i=q.list().iterator();i.hasNext();) {
    					Location room = (Location)i.next();
    					if (!match(filter, room)) continue;
    					response.addModel(TimetableGridSolutionHelper.createModel(solutionIdsStr, room, hibSession, cx));
    				}
    			} else if (cx.getResourceType() == ResourceType.INSTRUCTOR.ordinal()) {
    				if (RoomAvailability.getInstance() != null && cx.isShowEvents()) {
    			        RoomAvailability.getInstance().activate(acadSession, cx.getSessionStartDate(), cx.getSessionEndDate(), RoomAvailabilityInterface.sClassType, false);
    		            String ts = RoomAvailability.getInstance().getTimeStamp(cx.getSessionStartDate(), cx.getSessionEndDate(), RoomAvailabilityInterface.sClassType);
    		            if (ts == null)
    		            	response.addPageMessage(new PageMessage(PageMessageType.WARNING, MESSAGES.warnCourseSolverNoRoomAvailability()));
    		            else
    		            	response.addPageMessage(new PageMessage(PageMessageType.INFO, MESSAGES.infoCourseSolverRoomAvailabilityLastUpdated(ts)));
    				}
                    String instructorNameFormat = UserProperty.NameFormat.get(context.getUser());
    				Query q = null;
    				if (ApplicationProperty.TimetableGridUseClassInstructors.isTrue()) {
    					q = hibSession.createQuery(
    							"select distinct i.instructor from "+
    							"ClassInstructor as i inner join i.classInstructing.assignments as a where "+
    							"a.solution.uniqueId in ("+solutionIdsStr+")");
    				} else {
    					q = hibSession.createQuery(
    							"select distinct i from "+
    							"DepartmentalInstructor as i inner join i.assignments as a where "+
    							"a.solution.uniqueId in ("+solutionIdsStr+")");
    				}
    				q.setCacheable(true);
    				HashSet puids = new HashSet();
    				for (Iterator i=q.list().iterator();i.hasNext();) {
    					DepartmentalInstructor instructor = (DepartmentalInstructor)i.next();
    					String name = (instructor.getLastName()+", "+instructor.getFirstName()+" "+instructor.getMiddleName()).trim();
    					if (!match(filter, name)) continue;
    					if (instructor.getExternalUniqueId() == null || instructor.getExternalUniqueId().isEmpty() || puids.add(instructor.getExternalUniqueId())) {
    						TimetableGridModel m = TimetableGridSolutionHelper.createModel(solutionIdsStr, instructor, hibSession, cx);
                            m.setName(instructor.getName(instructorNameFormat));
    						response.addModel(m);
                        }
    				}
    			} else if (cx.getResourceType() == ResourceType.DEPARTMENT.ordinal()) {
    				Query q = hibSession.createQuery(
    						"select distinct d from "+
    						"Assignment a inner join a.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings as o inner join o.subjectArea.department as d where "+
    						"a.solution.uniqueId in ("+solutionIdsStr+") and o.isControl=true");
    				q.setCacheable(true);
    				for (Iterator i=q.list().iterator();i.hasNext();) {
    					Department dept = (Department)i.next();
    					String name = dept.getAbbreviation();
    					if (!match(filter, name)) continue;
    					response.addModel(TimetableGridSolutionHelper.createModel(solutionIdsStr, dept, hibSession, cx));
    				}
    			} else if (cx.getResourceType() == ResourceType.SUBJECT_AREA.ordinal()) {
    				Query q = hibSession.createQuery(
    						"select distinct sa from "+
    						"Assignment a inner join a.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings as o inner join o.subjectArea as sa where "+
    						"a.solution.uniqueId in ("+solutionIdsStr+") and o.isControl=true");
    				q.setCacheable(true);
    				for (Iterator i=q.list().iterator();i.hasNext();) {
    					SubjectArea sa = (SubjectArea)i.next();
    					String name = sa.getSubjectAreaAbbreviation();
    					if (!match(filter, name)) continue;
    					response.addModel(TimetableGridSolutionHelper.createModel(solutionIdsStr, sa, hibSession, cx));
    				}
    			} else if (cx.getResourceType() == ResourceType.CURRICULUM.ordinal()) {
    				Query q = hibSession.createQuery(
    						"select distinct cc.classification from "+
    						"CurriculumCourse cc, Assignment a inner join a.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings as co where "+
    						"a.solution.uniqueId in ("+solutionIdsStr+") and co = cc.course");
    				q.setCacheable(true);
    				for (Iterator i=q.list().iterator();i.hasNext();) {
    					CurriculumClassification cc = (CurriculumClassification)i.next();
    					String name = cc.getCurriculum().getAbbv() + " " + cc.getName();
    					if (!match(filter, name)) continue;
    					response.addModel(TimetableGridSolutionHelper.createModel(solutionIdsStr, cc, hibSession, cx));
    				}
    			} else if (cx.getResourceType() == ResourceType.STUDENT_GROUP.ordinal()) {
    				Query q = hibSession.createQuery(
    						"select distinct c from ConstraintInfo c inner join c.assignments a where a.solution.uniqueId in ("+solutionIdsStr+") and c.definition.name = 'GroupInfo'");
    				q.setCacheable(true);
    				for (Iterator i=q.list().iterator();i.hasNext();) {
    					ConstraintInfo g = (ConstraintInfo)i.next();
    					if (!match(filter, g.getOpt())) continue;
    					TimetableInfo info = g.getInfo();
    					if (info != null && info instanceof StudentGroupInfo)
    						response.addModel(TimetableGridSolutionHelper.createModel(solutionIdsStr, (StudentGroupInfo)info, hibSession, cx));
    				}
    				if (response.getModels().isEmpty()) {
    					q = hibSession.createQuery(
    							"select distinct r.group from StudentGroupReservation r, Assignment a inner join a.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering as io where "+
    							"a.solution.uniqueId in ("+solutionIdsStr+") and io = r.instructionalOffering");
    					q.setCacheable(true);
    					for (Iterator i=q.list().iterator();i.hasNext();) {
    						StudentGroup g = (StudentGroup)i.next();
    						if (match(filter, g.getGroupName()) || match(filter, g.getGroupAbbreviation()))
    							response.addModel(TimetableGridSolutionHelper.createModel(solutionIdsStr, g, hibSession, cx));
    					}					
    				}
    			}
    			if (tx!=null) tx.commit();
    		} catch (GwtRpcException e) {
    			if (tx!=null) tx.rollback();
    			Debug.error(e);
    			throw e;
    		} catch (Exception e) {
    			if (tx!=null) tx.rollback();
    			Debug.error(e);
    			throw new GwtRpcException(e.getMessage(), e);
    		}
    		
    		for (TimetableGridModel model: response.getModels())
    			TimetableGridHelper.computeIndexes(model, cx);
    	}
    	
    	if (response.getDefaultDatePatternName() != null)
    		for (TimetableGridModel model: response.getModels())
    			for (TimetableGridCell cell: model.getCells())
    				if (cell.hasDate() && response.getDefaultDatePatternName().equals(cell.getDate()))
    					cell.setDate(null);
    	
		final OrderBy order = OrderBy.values()[Integer.parseInt(request.getFilter().getParameterValue("orderBy", "0"))];
		Collections.sort(response.getModels(), new Comparator<TimetableGridModel>() {
			@Override
			public int compare(TimetableGridModel m1, TimetableGridModel m2) {
				int cmp = compareModels(m1, m2);
				if (cmp != 0) return cmp;
				return m1.getName().compareTo(m2.getName());
			}
			
			public int compareModels(TimetableGridModel m1, TimetableGridModel m2) {
				switch (order) {
				case NameAsc:
					return m1.getName().compareTo(m2.getName());
				case NameDesc:
					return m2.getName().compareTo(m1.getName());
				case SizeAsc:
					return Double.compare(m1.getSize(),m2.getSize());
				case SizeDesc:
					return Double.compare(m2.getSize(),m1.getSize());
				case TypeAsc:
					if (m1.getType()!=null && m2.getType()!=null) {
                        int cmp = m1.getType().compareTo(m2.getType());
                        if (cmp!=0) return cmp;
                    }
                    return m1.getName().compareTo(m2.getName());
				case TypeDesc:
                    if (m1.getType()!=null && m2.getType()!=null) {
                        int cmp = m2.getType().compareTo(m1.getType());
                        if (cmp!=0) return cmp;
                    }
                    return m2.getName().compareTo(m1.getName());
				case UtilizationAsc:
					return Double.compare(m1.getUtilization(), m2.getUtilization());
				case UtilizationDesc:
					return Double.compare(m2.getUtilization(), m1.getUtilization());
				}
				return 0;
			}
		});
		
		switch (BgMode.values()[cx.getBgMode()]) {
		case TimePref:
			response.addAssignedLegend(TimetableGridHelper.pref2color(PreferenceLevel.sRequired), MESSAGES.legendRequiredTime());
            response.addAssignedLegend(TimetableGridHelper.pref2color(PreferenceLevel.sStronglyPreferred), MESSAGES.legendStronglyPreferredTime());
            response.addAssignedLegend(TimetableGridHelper.pref2color(PreferenceLevel.sPreferred), MESSAGES.legendPreferredTime());
            response.addAssignedLegend(TimetableGridHelper.pref2color(PreferenceLevel.sNeutral), MESSAGES.legendNoTimePreference());
            response.addAssignedLegend(TimetableGridHelper.pref2color(PreferenceLevel.sDiscouraged), MESSAGES.legendDiscouragedTime());
            response.addAssignedLegend(TimetableGridHelper.pref2color(PreferenceLevel.sStronglyDiscouraged), MESSAGES.legendStronglyDiscouragedTime());
            response.addAssignedLegend(TimetableGridHelper.pref2color(PreferenceLevel.sProhibited), MESSAGES.legendProhibitedTime());
			break;
		case RoomPref:
			response.addAssignedLegend(TimetableGridHelper.pref2color(PreferenceLevel.sRequired), MESSAGES.legendRequiredRoom());
            response.addAssignedLegend(TimetableGridHelper.pref2color(PreferenceLevel.sStronglyPreferred), MESSAGES.legendStronglyPreferredRoom());
            response.addAssignedLegend(TimetableGridHelper.pref2color(PreferenceLevel.sPreferred), MESSAGES.legendPreferredRoom());
            response.addAssignedLegend(TimetableGridHelper.pref2color(PreferenceLevel.sNeutral), MESSAGES.legendNoRoomPreference());
            response.addAssignedLegend(TimetableGridHelper.pref2color(PreferenceLevel.sDiscouraged), MESSAGES.legendDiscouragedRoom());
            response.addAssignedLegend(TimetableGridHelper.pref2color(PreferenceLevel.sStronglyDiscouraged), MESSAGES.legendStronglyDiscouragedRoom());
            response.addAssignedLegend(TimetableGridHelper.pref2color(PreferenceLevel.sProhibited), MESSAGES.legendProhibitedRoom());
            break;
		case StudentConf:
            for (int nrConflicts = 0; nrConflicts <= 15; nrConflicts++)
            	if (nrConflicts < 15)
            		response.addAssignedLegend(TimetableGridHelper.conflicts2color(nrConflicts), MESSAGES.legendStudentConflicts(String.valueOf(nrConflicts)));
            	else
            		response.addAssignedLegend(TimetableGridHelper.conflicts2color(nrConflicts), MESSAGES.legendStudentConflictsOrMore(String.valueOf(nrConflicts)));
            break;
		case InstructorBtbPref:
			response.addAssignedLegend(TimetableGridHelper.pref2color(PreferenceLevel.sNeutral), MESSAGES.legendInstructorBTBNoPreference());
            response.addAssignedLegend(TimetableGridHelper.pref2color(PreferenceLevel.sDiscouraged), MESSAGES.legendInstructorBTBDiscouraged());
            response.addAssignedLegend(TimetableGridHelper.pref2color(PreferenceLevel.sStronglyDiscouraged), MESSAGES.legendInstructorBTBStronglyDiscouraged());
            response.addAssignedLegend(TimetableGridHelper.pref2color(PreferenceLevel.sProhibited), MESSAGES.legendInstructorBTBProhibited());
            break;
		case DistributionConstPref:
            response.addAssignedLegend(TimetableGridHelper.pref2color(PreferenceLevel.sNeutral), MESSAGES.legendDistributionNoViolation());
            response.addAssignedLegend(TimetableGridHelper.pref2color(PreferenceLevel.sDiscouraged), MESSAGES.legendDistributionDiscouraged());
            response.addAssignedLegend(TimetableGridHelper.pref2color(PreferenceLevel.sStronglyDiscouraged), MESSAGES.legendDistributionStronglyDiscouraged());
            response.addAssignedLegend(TimetableGridHelper.pref2color(PreferenceLevel.sProhibited), MESSAGES.legendDistributionProhibited());
            break;
		case Perturbations:
            response.addAssignedLegend(TimetableGridHelper.pref2color(PreferenceLevel.sStronglyPreferred), MESSAGES.legendPerturbationNoChange());
            response.addAssignedLegend(TimetableGridHelper.pref2color(PreferenceLevel.sNeutral), MESSAGES.legendPerturbationNoInitial());
            response.addAssignedLegend(TimetableGridHelper.pref2color(PreferenceLevel.sDiscouraged), MESSAGES.legendPerturbationRoomChanged());
            response.addAssignedLegend(TimetableGridHelper.pref2color(PreferenceLevel.sStronglyDiscouraged), MESSAGES.legendPerturbationTimeChanged());
            response.addAssignedLegend(TimetableGridHelper.pref2color(PreferenceLevel.sProhibited), MESSAGES.legendPerturbationBothChanged());
            break;
		case PerturbationPenalty:
			for (int nrConflicts = 0; nrConflicts <= 15; nrConflicts++)
            	response.addAssignedLegend(TimetableGridHelper.conflicts2color(nrConflicts), 
            			(nrConflicts == 0 ? MESSAGES.legendPerturbationNoPenalty()
            			:nrConflicts == 15? MESSAGES.legendPerturbationPenaltyAbove("15")
            			:MESSAGES.legendPerturbationPenaltyBelow(String.valueOf(nrConflicts))));
            break;
		case HardConflicts:
            response.addAssignedLegend(TimetableGridHelper.pref2color(PreferenceLevel.sRequired), MESSAGES.legendHardRequired());
            response.addAssignedLegend(TimetableGridHelper.pref2color(PreferenceLevel.sStronglyPreferred), MESSAGES.legendHardStronglyPreferred());
            response.addAssignedLegend(TimetableGridHelper.pref2color(PreferenceLevel.sPreferred), MESSAGES.legendHardPreferred());
            response.addAssignedLegend(TimetableGridHelper.pref2color(PreferenceLevel.sNeutral), MESSAGES.legendHardNeutral());
            response.addAssignedLegend(TimetableGridHelper.pref2color(PreferenceLevel.sDiscouraged), MESSAGES.legendHardDiscouraged());
            response.addAssignedLegend(TimetableGridHelper.pref2color(PreferenceLevel.sStronglyDiscouraged), MESSAGES.legendHardStronglyDiscouraged());
            break;
		case DepartmentalBalancing:
			for (int nrConflicts = 0; nrConflicts <= 3; nrConflicts++)
				response.addAssignedLegend(TimetableGridHelper.conflicts2colorFast(nrConflicts),
						(nrConflicts == 0 ? MESSAGES.legendNoPenalty()
						:nrConflicts == 3 ? MESSAGES.legendPenaltyEqualAbove("3")
						:MESSAGES.legendPenaltyEqual(String.valueOf(nrConflicts))));
			break;
		case TooBigRooms:
			response.addAssignedLegend(TimetableGridHelper.pref2color(PreferenceLevel.sRequired), MESSAGES.legendTooBigRoomsRequired());
            response.addAssignedLegend(TimetableGridHelper.pref2color(PreferenceLevel.sNeutral), MESSAGES.legendTooBigRoomsNeutral());
            response.addAssignedLegend(TimetableGridHelper.pref2color(PreferenceLevel.sDiscouraged), MESSAGES.legendTooBigRoomsDiscouraged());
            response.addAssignedLegend(TimetableGridHelper.pref2color(PreferenceLevel.sStronglyDiscouraged), MESSAGES.legendTooBigRoomsStronglyDiscouraged());
            break;
		case StudentGroups:
			for (int percentage = 0; percentage <= 100; percentage += 5)
				response.addAssignedLegend(TimetableGridHelper.percentage2color(percentage), MESSAGES.legendStudentGroups(String.valueOf(percentage)));
			break;
        }
		response.addNotAssignedLegend(TimetableGridHelper.sBgColorNotAvailable, MESSAGES.legendTimeNotAvailable());
        response.addNotAssignedLegend(TimetableGridHelper.pref2color(PreferenceLevel.sNeutral), MESSAGES.legendNoPreference());
        if (cx.isShowFreeTimes()) {
        	response.addNotAssignedLegend(TimetableGridHelper.pref2color(PreferenceLevel.sDiscouraged), MESSAGES.legendFreeTimeDiscouraged());
        	response.addNotAssignedLegend(TimetableGridHelper.pref2color(PreferenceLevel.sStronglyDiscouraged), MESSAGES.legendFreeTimeStronglyDiscouraged());
        	response.addNotAssignedLegend(TimetableGridHelper.pref2color(PreferenceLevel.sProhibited), MESSAGES.legendFreeTimeProhibited());
        }
		return response;
	}
	
	private boolean match(org.unitime.timetable.gwt.server.Query q, final String name) {
		return q == null || q.match(new TermMatcher() {
			@Override
			public boolean match(String attr, String term) {
				if (term.isEmpty()) return true;
				if (attr == null) {
					term: for (StringTokenizer s = new StringTokenizer(term, " ,"); s.hasMoreTokens(); ) {
						String termToken = s.nextToken();
						for (StringTokenizer t = new StringTokenizer(name, " ,"); t.hasMoreTokens(); ) {
							String token = t.nextToken();
							if (token.toLowerCase().startsWith(termToken.toLowerCase())) continue term;
						}
						return false;
					}
					return true;
				} else if ("regex".equals(attr) || "regexp".equals(attr) || "re".equals(attr)) {
					return name.matches(term);
				} else if ("find".equals(attr)) {
					return name.toLowerCase().indexOf(term.toLowerCase()) >= 0;
				}
				return false;
			}
		});
	}
	
	private static enum Size {
		eq, lt, gt, le, ge
	};
    
    private boolean match(org.unitime.timetable.gwt.server.Query q, final Location location) {
    	return q == null || q.match(new TermMatcher() {
			@Override
			public boolean match(String attr, String term) {
				if (term.isEmpty()) return true;
				if (attr == null) {
					for (StringTokenizer s = new StringTokenizer(location.getLabel(), " ,"); s.hasMoreTokens(); ) {
						String token = s.nextToken();
						if (term.equalsIgnoreCase(token)) return true;
					}
				} else if ("regex".equals(attr) || "regexp".equals(attr) || "re".equals(attr)) {
					return location.getLabel().matches(term);
				} else if ("find".equals(attr)) {
					return location.getLabel().toLowerCase().indexOf(term.toLowerCase()) >= 0;
				} else if ("type".equals(attr)) {
					return term.equalsIgnoreCase(location.getRoomType().getReference()) || term.equalsIgnoreCase(location.getRoomType().getLabel());
				} else if ("size".equals(attr)) {
					int min = 0, max = Integer.MAX_VALUE;
					Size prefix = Size.eq;
					String number = term;
					if (number.startsWith("<=")) { prefix = Size.le; number = number.substring(2); }
					else if (number.startsWith(">=")) { prefix = Size.ge; number = number.substring(2); }
					else if (number.startsWith("<")) { prefix = Size.lt; number = number.substring(1); }
					else if (number.startsWith(">")) { prefix = Size.gt; number = number.substring(1); }
					else if (number.startsWith("=")) { prefix = Size.eq; number = number.substring(1); }
					try {
						int a = Integer.parseInt(number);
						switch (prefix) {
							case eq: min = max = a; break; // = a
							case le: max = a; break; // <= a
							case ge: min = a; break; // >= a
							case lt: max = a - 1; break; // < a
							case gt: min = a + 1; break; // > a
						}
					} catch (NumberFormatException e) {}
					if (term.contains("..")) {
						try {
							String a = term.substring(0, term.indexOf('.'));
							String b = term.substring(term.indexOf("..") + 2);
							min = Integer.parseInt(a); max = Integer.parseInt(b);
						} catch (NumberFormatException e) {}
					}
					return min <= location.getCapacity() && location.getCapacity() <= max;
				}
				return false;
			}
		});
	}
}
