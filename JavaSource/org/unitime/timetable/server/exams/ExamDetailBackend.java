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
package org.unitime.timetable.server.exams;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.HtmlUtils;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamDetailReponse;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamDetailRequest;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamDetailRequest.Action;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface.Alignment;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.RoomInterface;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamConflict;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.StudentAccomodation.AccommodationCounter;
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.courses.DistributionsTableBuilder;
import org.unitime.timetable.server.courses.SubpartDetailBackend;
import org.unitime.timetable.server.rooms.PeriodPreferencesBackend;
import org.unitime.timetable.solver.exam.ExamAssignmentProxy;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamRoomInfo;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.JavascriptFunctions;
import org.unitime.timetable.webutil.Navigation;
import org.unitime.timetable.webutil.BackTracker.BackItem;

@GwtRpcImplements(ExamDetailRequest.class)
public class ExamDetailBackend implements GwtRpcImplementation<ExamDetailRequest, ExamDetailReponse>{
	protected final static ExaminationMessages MSG = Localization.create(ExaminationMessages.class);
	protected final static CourseMessages CMSG = Localization.create(CourseMessages.class);
	protected static final GwtMessages GWT = Localization.create(GwtMessages.class);
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	
	@Autowired SolverService<ExamSolverProxy> examinationSolverService;

	@Override
	public ExamDetailReponse execute(ExamDetailRequest request, SessionContext context) {
		org.hibernate.Session hibSession = ExamDAO.getInstance().getSession();
		Exam exam = ExamDAO.getInstance().get(request.getExamId(), hibSession);
		context.checkPermission(exam, Right.ExaminationDetail);
		
		if (exam == null)
			throw new GwtRpcException(MSG.errorNoExamId());
		
		
		if (request.getAction() == Action.DELETE) {
			context.checkPermission(exam, Right.ExaminationDelete);
            Transaction tx = null;
            try {
                tx = hibSession.beginTransaction();
                ChangeLog.addChange(
                        hibSession, 
                        context,
                        exam, 
                        ChangeLog.Source.EXAM_EDIT, 
                        ChangeLog.Operation.DELETE, 
                        exam.firstSubjectArea(), 
                        exam.firstDepartment());
                exam.deleteDependentObjects(hibSession, false);
                for (Iterator<ExamConflict> j=exam.getConflicts().iterator();j.hasNext();) {
                    ExamConflict conf = j.next();
                    for (Iterator<Exam> i=conf.getExams().iterator();i.hasNext();) {
                        Exam x = i.next();
                        if (!x.equals(exam)) x.getConflicts().remove(conf);
                    }
                    hibSession.remove(conf);
                    j.remove();
                }
                hibSession.remove(exam);
                tx.commit();
            } catch (Exception e) {
                if (tx!=null) tx.rollback();
                throw e;
            }
			ExamDetailReponse response = new ExamDetailReponse();
			BackItem back = BackTracker.getBackItem(context, 2);
	    	if (back != null) {
	    		response.setUrl(back.getUrl() + (back.getUrl().indexOf('?') >= 0 ? "&" : "?") + "backId=-1&backType=PreferenceGroup");
            } else {
            	response.setUrl("examinations");
            }
			return response;
		}
		
        BackTracker.markForBack(context,
        		"examination?id=" + request.getExamId(),
        		MSG.backExam(exam.getLabel()),
        		true, false);

		ExamDetailReponse response = new ExamDetailReponse();
		response.setConfirms(JavascriptFunctions.isJsConfirm(context));
		
		response.setExamId(exam.getUniqueId());
		response.setExamName(exam.getLabel());
		
		if (exam.getName() != null && !exam.getName().isEmpty())
			response.addProperty(MSG.propExamName()).setText(exam.getName());
		else
			response.addProperty(MSG.propExamName()).setText(exam.getLabel()).addStyle("font-style: italic;");
		response.addProperty(MSG.propExamType()).setText(exam.getExamType().getLabel());
		response.addProperty(MSG.propExamLength()).setText(exam.getLength());
		response.addProperty(MSG.propExamSeatingType()).setText(exam.getSeatingType() == Exam.sSeatingTypeNormal ? MSG.seatingNormal() : MSG.seatingExam());
		response.addProperty(MSG.propExamMaxRooms()).setText(exam.getMaxNbrRooms());
		response.addProperty(MSG.propExamSize()).setText(exam.getSize());
		if (exam.getPrintOffset() != null && exam.getPrintOffset() != 0)
			response.addProperty(MSG.propExamPrintOffset()).setText(exam.getPrintOffset()).add(" " + MSG.offsetUnitMinutes());
		if (exam.getInstructors() != null && !exam.getInstructors().isEmpty()) {
			TableInterface table = new TableInterface();
			String nameFormat = UserProperty.NameFormat.get(context.getUser());
			for (DepartmentalInstructor instructor: new TreeSet<DepartmentalInstructor>(exam.getInstructors())) {
				LineInterface line = table.addLine();
				if (context.hasPermission(instructor, Right.InstructorDetail))
					line.setURL("instructorDetail.action?instructorId=" + instructor.getUniqueId());
				line.addCell(instructor.getName(nameFormat));
				if (instructor.getEmail() != null && !instructor.getEmail().isEmpty())
					line.addCell(instructor.getEmail());
				else
					line.addCell();
			}
			response.addProperty(MSG.propExamInstructors()).setTable(table);
		}
		if (exam.getAvgPeriod() != null) {
			ExamPeriod ep = exam.getAveragePeriod();
			if (ep != null)
				response.addProperty(MSG.propExamAvgPeriod()).setText(ep.getName());
		}
		if (CommonValues.Yes.eq(context.getUser().getProperty(UserProperty.DisplayLastChanges))) {
        	ChangeLog cl = ChangeLog.findLastChange(exam);
        	if (cl != null)
        		response.addProperty(GWT.propLastChange()).add(cl.getShortLabel());
        	else
        		response.addProperty(GWT.propLastChange()).add(GWT.notApplicable()).addStyle("font-style: italic;");
		}
		if (exam.getNote() != null && !exam.getNote().trim().isEmpty())
			response.addProperty(CMSG.propertyRequestsNotes()).setText(exam.getNote()).addStyle("white-space: pre-wrap;");
        List<AccommodationCounter> acc = StudentAccomodation.getAccommodations(exam);
        if (acc != null && !acc.isEmpty()) {
        	CellInterface c = response.addProperty(MSG.propExamStudentAccommodations());
        	TableInterface table = new TableInterface();
        	for (AccommodationCounter ac: acc)
        		table.addProperty(ac.getAccommodation().getName() + ":").setText(String.valueOf(ac.getCount()));
        	c.setTable(table);
        }
        
        response.setOwners(getOwnersTable(context, exam));
        
        response.setAssignment(getAssignmentTable(context, exam));
        
        response.setPreferences(getPreferenceTable(context, exam, Preference.Type.PERIOD,
        		Preference.Type.ROOM_GROUP, Preference.Type.ROOM, Preference.Type.BUILDING, Preference.Type.ROOM_FEATURE));
        
        DistributionsTableBuilder distBuilder = new DistributionsTableBuilder(context, null, null);
    	response.setDistributions(distBuilder.getDistPrefsTableForExam(exam));
		
		if (context.hasPermission(exam, Right.ExaminationEdit))
			response.addOperation("edit");
		if (context.hasPermission(exam, Right.ExaminationClone))
			response.addOperation("clone");
		if (context.hasPermission(exam, Right.DistributionPreferenceExam)) {
			if (ApplicationProperty.LegacyExamDistributions.isTrue())
				response.addOperation("add-distribution-legacy");
			else
				response.addOperation("add-distribution");
		}
		if (context.hasPermission(exam, Right.ExaminationAssignment))
			response.addOperation("assign");
		if (context.hasPermission(exam, Right.ExaminationDelete))
			response.addOperation("delete");
		BackItem back = BackTracker.getBackItem(context, 2);
    	if (back != null) {
    		response.addOperation("back");
    		response.setBackTitle(back.getTitle());
    		response.setBackUrl(back.getUrl() +
    				(back.getUrl().indexOf('?') >= 0 ? "&" : "?") +
    				"backId=" + exam.getUniqueId() + "&backType=PreferenceGroup");
    	}
    	response.setNextId(Navigation.getNext(context, Navigation.sInstructionalOfferingLevel, exam.getUniqueId()));
    	response.setPreviousId(Navigation.getPrevious(context, Navigation.sInstructionalOfferingLevel, exam.getUniqueId()));
    	if (response.getPreviousId() != null && context.hasPermission(response.getPreviousId(), "Exam", Right.ExaminationDetail))
    		response.addOperation("previous");
    	if (response.getNextId() != null && context.hasPermission(response.getNextId(), "Exam", Right.ExaminationDetail))
    		response.addOperation("next");
		
		return response;
	}
	
	public static TableInterface getOwnersTable(SessionContext context, Exam exam) {
        TableInterface table = new TableInterface();
        table.setName(MSG.sectExamOwners());
		LineInterface header = table.addHeader();
		header.addCell(MSG.colExamOwnerObject());
		header.addCell(MSG.colExamOwnerType()).setTextAlignment(Alignment.CENTER);
		header.addCell(MSG.colExamOwnerTitle());
		header.addCell(MSG.colExamOwnerManager());
		header.addCell(MSG.colExamOwnerStudents()).setTextAlignment(Alignment.RIGHT);
		header.addCell(MSG.colExamOwnerLimit()).setTextAlignment(Alignment.RIGHT);
		header.addCell(MSG.colExamOwnerAssignment());
		for (CellInterface cell: header.getCells()) {
    		cell.setClassName("WebTableHeader");
    		cell.setText(cell.getText().replace("<br>", "\n"));
    	}
		for (ExamOwner owner: new TreeSet<ExamOwner>(exam.getOwners())) {
			LineInterface line = table.addLine();
			switch (owner.getOwnerType()) {
            case ExamOwner.sOwnerTypeClass :
                Class_ clazz = (Class_)owner.getOwnerObject();
                if (context.hasPermission(clazz, Right.ClassDetail))
                	line.setURL("classDetail.action?cid="+clazz.getUniqueId());
                line.addCell().setText(owner.getLabel());
                line.addCell().setText(MSG.examTypeClass()).setTextAlignment(Alignment.CENTER);
                String title = clazz.getSchedulePrintNote();
                if (title==null || title.isEmpty()) title=clazz.getSchedulingSubpart().getControllingCourseOffering().getTitle();
                line.addCell().setText(title);
                line.addCell().setText(clazz.getManagingDept().getShortLabel());
                line.addCell().setText(owner.countStudents()).setTextAlignment(Alignment.RIGHT);
                line.addCell().setText(owner.getLimit()).setTextAlignment(Alignment.RIGHT);
                if (clazz.getCommittedAssignment() != null)
                	line.addCell().setText(clazz.getCommittedAssignment().getPlacement().getLongName(CONSTANTS.useAmPm()));
                else
                	line.addCell();
                if (clazz.isCancelled())
                	for (CellInterface cell: line.getCells()) {
                		cell.setColor("gray");
                		cell.addStyle("font-style: italic;");
                		cell.setTitle(CMSG.classNoteCancelled(clazz.getClassLabel()));
                	}
                break;
            case ExamOwner.sOwnerTypeConfig :
                InstrOfferingConfig config = (InstrOfferingConfig)owner.getOwnerObject();
                if (context.hasPermission(config.getInstructionalOffering(), Right.InstructionalOfferingDetail))
                	line.setURL("instructionalOfferingDetail.action?io="+config.getInstructionalOffering().getUniqueId());
                line.addCell().setText(owner.getLabel());
                line.addCell().setText(MSG.examTypeConfig()).setTextAlignment(Alignment.CENTER);
                line.addCell().setText(config.getControllingCourseOffering().getTitle());
                line.addCell().setText(config.getInstructionalOffering().getControllingCourseOffering().getDepartment().getShortLabel());
                line.addCell().setText(owner.countStudents()).setTextAlignment(Alignment.RIGHT);
                line.addCell().setText(owner.getLimit()).setTextAlignment(Alignment.RIGHT);
                line.addCell();
                break;
            case ExamOwner.sOwnerTypeOffering :
                InstructionalOffering offering = (InstructionalOffering)owner.getOwnerObject();
                if (context.hasPermission(offering, Right.InstructionalOfferingDetail))
                	line.setURL("instructionalOfferingDetail.action?io="+offering.getUniqueId());
                line.addCell().setText(owner.getLabel());
                line.addCell().setText(MSG.examTypeOffering()).setTextAlignment(Alignment.CENTER);
                line.addCell().setText(offering.getControllingCourseOffering().getTitle());
                line.addCell().setText(offering.getControllingCourseOffering().getDepartment().getShortLabel());
                line.addCell().setText(owner.countStudents()).setTextAlignment(Alignment.RIGHT);
                line.addCell().setText(owner.getLimit()).setTextAlignment(Alignment.RIGHT);
                line.addCell();
                break;
            case ExamOwner.sOwnerTypeCourse :
                CourseOffering course = (CourseOffering)owner.getOwnerObject();
                if (context.hasPermission(course.getInstructionalOffering(), Right.InstructionalOfferingDetail))
                	line.setURL("instructionalOfferingDetail.action?io="+course.getInstructionalOffering().getUniqueId());
                line.addCell().setText(owner.getLabel());
                line.addCell().setText(MSG.examTypeCourse()).setTextAlignment(Alignment.CENTER);
                line.addCell().setText(course.getTitle());
                line.addCell().setText(course.getDepartment().getShortLabel());
                line.addCell().setText(owner.countStudents()).setTextAlignment(Alignment.RIGHT);
                line.addCell().setText(owner.getLimit()).setTextAlignment(Alignment.RIGHT);
                line.addCell();
                break;
			}
        }
        if (!table.hasLines())
        	table.setErrorMessage(MSG.warnNoExamOwners());

		return table;
	}
	
	public TableInterface getAssignmentTable(SessionContext context, Exam exam) {
        ExamAssignmentInfo ea = null;
        ExamAssignmentProxy examAssignment = (ExamAssignmentProxy)examinationSolverService.getSolver();
        if (examAssignment!=null && examAssignment.getExamTypeId().equals(exam.getExamType().getUniqueId())) {
            ea = examAssignment.getAssignmentInfo(exam.getUniqueId());
        } else if (exam.getAssignedPeriod()!=null)
            ea = new ExamAssignmentInfo(exam);

        if (ea == null || ea.getPeriod() == null) return null;
        
        TableInterface table = new TableInterface();
        table.setName(MSG.sectExamAssignment());
        table.addProperty(MSG.propExamAssignedPeriod()).addItem(ea.getPeriodCell());
        if (!ea.getRooms().isEmpty()) {
        	CellInterface cell = table.addProperty(ea.getRooms().size() > 1 ? MSG.propExamAssignedRooms() : MSG.propExamAssignedRoom());
        	for (ExamRoomInfo room: ea.getRooms())
        		cell.addItem(room.toCell().setInline(false));
        }
        if (ea.getNrDistributionConflicts() > 0)
        	table.addProperty(MSG.propExamViolatedDistConstraints()).setTable(ea.generateDistributionConflictTable());
        if (ea.getHasConflicts())
        	table.addProperty(MSG.propExamStudentConflicts()).setTable(ea.generateConflictTable());
        if (ea.getHasInstructorConflicts())
        	table.addProperty(MSG.propExamInstructorConflicts()).setTable(ea.generateInstructorConflictTable());
        
        return table;
	}
	
	public TableInterface getPreferenceTable(SessionContext context, Exam pg, Preference.Type... types) {
		TableInterface table = new TableInterface();
		boolean hasNotAvailable = false;
		boolean excap = (pg.getSeatingType() == Exam.sSeatingTypeExam);
		for (Preference.Type type: types) {
			switch (type) {
			case ROOM_GROUP:
				Set<RoomGroupPref> roomGrouPrefs = pg.effectivePreferences(RoomGroupPref.class);
				if (!roomGrouPrefs.isEmpty()) {
					CellInterface rpCell = table.addProperty(CMSG.propertyRoomGroups());
					for (RoomGroupPref rp: roomGrouPrefs) {
						CellInterface cell = rpCell.add(null).setInline(false);
						if (rp.getPrefLevel().getPrefId().intValue() != 4)
							cell.setColor(PreferenceLevel.prolog2color(rp.getPrefLevel().getPrefProlog()));
						cell.setText(rp.getRoomGroup().getNameWithTitle());
						String hint = HtmlUtils.htmlEscape(CMSG.prefTitleRoomGroup(rp.getPrefLevel().getPrefName(), cell.getText()));
						cell.setAria(CMSG.prefTitleRoomGroup(rp.getPrefLevel().getPrefName(), cell.getText()));
						cell.setMouseOver("$wnd.showGwtHint($wnd.lastMouseOverElement, '" + hint + "');");
						cell.setMouseOut("$wnd.hideGwtHint();");
					}
				}
				break;
			case ROOM:
				Set<RoomPref> roomPrefs = pg.effectivePreferences(RoomPref.class);
				if (!roomPrefs.isEmpty()) {
					CellInterface rpCell = table.addProperty(CMSG.propertyRooms());
					for (RoomPref rp: roomPrefs) {
						CellInterface cell = rpCell.add(null).setInline(false);
						if (rp.getPrefLevel().getPrefId().intValue() != 4)
							cell.setColor(PreferenceLevel.prolog2color(rp.getPrefLevel().getPrefProlog()));
						cell.setText(excap ? rp.getRoom().getLabelWithExamCapacity() : rp.getRoom().getLabelWithCapacity());
						cell.setAria(CMSG.prefTitleRoom(rp.getPrefLevel().getPrefName(), cell.getText()));
						cell.setMouseOver("$wnd.showGwtRoomHint($wnd.lastMouseOverElement, '" + rp.getRoom().getUniqueId() + "', '" + rp.getPrefLevel().getPrefName() + " " + CMSG.prefRoom() + " {0} ({1})');");
				    	cell.setMouseOut("$wnd.hideGwtRoomHint();");
					}
				}
				break;
			case BUILDING:
				Set<BuildingPref> buildingPrefs = pg.effectivePreferences(BuildingPref.class);
				if (!buildingPrefs.isEmpty()) {
					CellInterface rpCell = table.addProperty(CMSG.propertyBuildings());
					for (BuildingPref rp: buildingPrefs) {
						CellInterface cell = rpCell.add(null).setInline(false);
						if (rp.getPrefLevel().getPrefId().intValue() != 4)
							cell.setColor(PreferenceLevel.prolog2color(rp.getPrefLevel().getPrefProlog()));
						cell.setText(rp.getBuilding().getAbbrName());
						cell.setAria(CMSG.prefTitleBuilding(rp.getPrefLevel().getPrefName(), cell.getText()));
						cell.setMouseOver("$wnd.showGwtRoomHint($wnd.lastMouseOverElement, '-" + rp.getBuilding().getUniqueId() + "', '" + rp.getPrefLevel().getPrefName() + " " + CMSG.prefBuilding() + " {0}');");
						cell.setMouseOut("$wnd.hideGwtRoomHint();");
					}
				}
				break;
			case ROOM_FEATURE:
				Set<RoomFeaturePref> roomFeaturePrefs = pg.effectivePreferences(RoomFeaturePref.class);
				if (!roomFeaturePrefs.isEmpty()) {
					CellInterface rpCell = table.addProperty(CMSG.propertyRoomFeatures());
					for (RoomFeaturePref rp: roomFeaturePrefs) {
						CellInterface cell = rpCell.add(null).setInline(false);
						if (rp.getPrefLevel().getPrefId().intValue() != 4)
							cell.setColor(PreferenceLevel.prolog2color(rp.getPrefLevel().getPrefProlog()));
						cell.setText(rp.getRoomFeature().getLabelWithType());
						cell.setAria(CMSG.prefTitleRoomFeature(rp.getPrefLevel().getPrefName(), cell.getText()));
						String hint = HtmlUtils.htmlEscape(CMSG.prefTitleRoomFeature(rp.getPrefLevel().getPrefName(), cell.getText()));
						cell.setMouseOver("$wnd.showGwtHint($wnd.lastMouseOverElement, '" + hint + "');");
						cell.setMouseOut("$wnd.hideGwtHint();");
					}
				}
				break;
			case PERIOD:
				RoomInterface.PeriodPreferenceModel model = new PeriodPreferencesBackend().loadExamPeriodPreferences(
						examinationSolverService.getSolver(),
	        			pg,
	        			pg.getExamType(),
	        			context);
				if (model.hasNotAvailable()) hasNotAvailable = true;
				table.addProperty(CMSG.propertyExaminationPeriods()).setPeriodPreference(model);
			}
		}
		
		if (!table.hasProperties()) return null;
		
		table.addProperty(SubpartDetailBackend.getLegend(hasNotAvailable));
		
		return table;
	}

}
