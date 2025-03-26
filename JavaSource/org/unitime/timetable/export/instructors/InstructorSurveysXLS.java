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
package org.unitime.timetable.export.instructors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.Exporter;
import org.unitime.timetable.export.PDFPrinter.A;
import org.unitime.timetable.export.PDFPrinter.F;
import org.unitime.timetable.export.XLSPrinter;
import org.unitime.timetable.export.courses.OfferingsCSV.Filter;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.InstructorCourseRequirement;
import org.unitime.timetable.model.InstructorCourseRequirementNote;
import org.unitime.timetable.model.InstructorCourseRequirementType;
import org.unitime.timetable.model.InstructorSurvey;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.TimePatternModel;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.ClassInstructorComparator;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.instructor.InstructorsTableBuilder;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.ClassAssignmentProxy.AssignmentInfo;
import org.unitime.timetable.solver.service.AssignmentService;
import org.unitime.timetable.solver.ui.AssignmentPreferenceInfo;
import org.unitime.timetable.spring.SpringApplicationContextHolder;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.Formats.Format;

/**
 * @author Tomas Muller
 */
@Service("org.unitime.timetable.export.Exporter:instructor-surveys.xls")
public class InstructorSurveysXLS implements Exporter {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	protected static final CourseMessages CMSG = Localization.create(CourseMessages.class);
	protected static final StudentSectioningMessages SECTMSG = Localization.create(StudentSectioningMessages.class);
	protected static final GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	
	@Override
	public String reference() { return "instructor-surveys.xls"; }
	
	

	@Override
	public void export(ExportHelper helper) throws IOException {
		String dept = helper.getParameter("department");
		if (dept == null)
			dept = helper.getParameter("deptId");
		if (dept == null)
			throw new IllegalArgumentException("Department not provided or not found.");
		Department department = null;
		try {
			department = DepartmentDAO.getInstance().get(Long.valueOf(dept));
		} catch (Exception e) {
			department = Department.findByDeptCode(dept, helper.getAcademicSessionId());
		}
		if (department == null)
			throw new IllegalArgumentException("Department '" + dept + "' not found.");
		helper.getSessionContext().checkPermission(department, Right.InstructorSurveyAdmin);
		
		Map<String, InstructorSurvey> surveys = InstructorSurvey.getInstructorSurveysForDepartment(department.getUniqueId());
		if (surveys == null || surveys.isEmpty())
			throw new IllegalArgumentException("There are no instructor surveys filled in for " + department.getLabel() + ".");
		
		XLSPrinter printer = new XLSPrinter(helper.getOutputStream(), false);
		helper.setup(printer.getContentType(), department.getDeptCode() + " " + reference(), false);
		printer.getWorkbook().setSheetName(printer.getSheetIndex(), MESSAGES.sectGeneralPreferences()); 
		
		printer.printHeader(
				MESSAGES.colExternalId(),
				MESSAGES.colNamePerson(),
				MESSAGES.colRequestedTimePrefs(),
				MESSAGES.colRequestedRoomPrefs(),
				MESSAGES.colRequestedDistPrefs(),
				MESSAGES.colRequestedOtherPrefs(),
				CMSG.columnInstrSurveyPDF(),
				MESSAGES.colTimePreferences().replace("<br>", "\n"),
				MESSAGES.colRoomPreferences().replace("<br>", "\n"),
				MESSAGES.colDistributionPreferences().replace("<br>", "\n")
				);
		printer.flush();
		
		List<DepartmentalInstructor> instructors = new ArrayList<DepartmentalInstructor>(DepartmentalInstructor.findInstructorsForDepartment(department.getUniqueId()));
		final String instructorNameFormat = helper.getSessionContext().getUser().getProperty(UserProperty.NameFormat);
		Collections.sort(instructors, new Comparator<DepartmentalInstructor>() {
			@Override
			public int compare(DepartmentalInstructor i1, DepartmentalInstructor i2) {
				String n1 = i1.getName(instructorNameFormat);
				String n2 = i2.getName(instructorNameFormat);
				int cmp = n1.compareTo(n2);
				if (cmp != 0) return cmp;
				cmp = (i1.getExternalUniqueId() == null ? "" : i1.getExternalUniqueId()).compareTo(i2.getExternalUniqueId() == null ? "" : i2.getExternalUniqueId());
				if (cmp != 0) return cmp;
				return i1.compareTo(i2);
			}
		});
		Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_TIME_STAMP);
		InstructorsTableBuilder builder = new InstructorsTableBuilder(helper.getSessionContext(), null, null);
		Filter filter = new Filter(helper);
		for (DepartmentalInstructor instructor: instructors) {
			if (instructor.getExternalUniqueId() == null) continue;
			if (!builder.matches(instructor, filter)) continue;
			InstructorSurvey survey = surveys.get(instructor.getExternalUniqueId());
			if (survey == null) continue;
			A extId = new A(instructor.getExternalUniqueId());
			A name = new A(instructor.getName(instructorNameFormat));
			List<A> time = new ArrayList<A>();
			List<A> room = new ArrayList<A>();
			List<A> dist = new ArrayList<A>();
			List<A> itime = new ArrayList<A>();
			List<A> iroom = new ArrayList<A>();
			List<A> idist = new ArrayList<A>();
			addPreferences(time, room, dist, instructor, survey.getPreferences(), instructor.getPreferences(), instructorNameFormat);
			addPreferences(itime, iroom, idist, instructor, instructor.getPreferences(), survey.getPreferences(), instructorNameFormat);
			A other = new A(survey.getNote() == null ? "" : survey.getNote());
			A is = null;
			if (survey.getSubmitted() != null) {
				is = new A(df.format(survey.getSubmitted()));
			} else {
				is = new A(CMSG.instrSurveyNotSubmitted());
				is.setColor("#dcb414");
			}
			printer.printLine(
					extId,
					name,
					new A(time),
					new A(room),
					new A(dist),
					other,
					is,
					new A(itime),
					new A(iroom),
					new A(idist));
			printer.flush();
		}
		
		printer.newSheet();
		printer.getWorkbook().setSheetName(printer.getSheetIndex(), MESSAGES.sectCoursePreferences());
		
		final List<InstructorCourseRequirementType> types = InstructorCourseRequirementType.getInstructorCourseRequirementTypes();
		int nrCols = 0;
		int colExtId = nrCols ++;
		int colName = nrCols ++;
		int colRTime = nrCols ++;
		int colRRoom = nrCols ++;
		int colRDist = nrCols ++;
		int colROther = nrCols ++;
		int colSurv = nrCols ++;
		int colCourse = nrCols ++;
		int colReq = nrCols;
		nrCols += types.size();
		int colClass = nrCols ++;
		int colTime = nrCols ++;
		int colRoom = nrCols ++;
		int colDist = nrCols ++;
		int colATime = nrCols ++;
		int colARoom = nrCols ++;
		String[] head = new String[nrCols];
		
		head[colExtId] = MESSAGES.colExternalId();
		head[colName] = MESSAGES.colNamePerson();
		head[colRTime] = MESSAGES.colRequestedTimePrefs();
		head[colRRoom] = MESSAGES.colRequestedRoomPrefs();
		head[colRDist] = MESSAGES.colRequestedDistPrefs();
		head[colROther] = MESSAGES.colRequestedOtherPrefs();
		head[colSurv] = CMSG.columnInstrSurveyPDF();
		head[colCourse] = MESSAGES.colCourse();
		for (int i = 0; i < types.size(); i++)
			head[colReq + i] = types.get(i).getReference();
		
		head[colClass] = CMSG.columnClass();
		head[colTime] = CMSG.columnTimePref();
		head[colRoom] = CMSG.columnRoomPref();
		head[colDist] = CMSG.columnDistributionPref();
		head[colATime] = CMSG.columnAssignedTime();
		head[colARoom] = CMSG.columnAssignedRoom();
		
		printer.printHeader(head);
		printer.flush();
		
		ClassAssignmentProxy classAssignment = getClassAssignmentService().getAssignment();
		for (DepartmentalInstructor instructor: instructors) {
			if (instructor.getExternalUniqueId() == null) continue;
			if (!builder.matches(instructor, filter)) continue;
			InstructorSurvey survey = surveys.get(instructor.getExternalUniqueId());
			if (survey == null) continue;
			List<InstructorCourseRequirement> reqs = new ArrayList<InstructorCourseRequirement>(survey.getCourseRequirements());
			Collections.sort(reqs, new Comparator<InstructorCourseRequirement>() {
				@Override
				public int compare(InstructorCourseRequirement o1, InstructorCourseRequirement o2) {
					String c1 = (o1.getCourseOffering() != null ? o1.getCourseOffering().getCourseName() : o1.getCourse());
					String c2 = (o2.getCourseOffering() != null ? o2.getCourseOffering().getCourseName() : o2.getCourse());
					int cmp = c1.compareTo(c2);
					if (cmp != 0) return cmp;
					for (InstructorCourseRequirementType type: types) {
						InstructorCourseRequirementNote n1 = o1.getNote(type);
						InstructorCourseRequirementNote n2 = o1.getNote(type);
						cmp = (n1 == null ? "" : n1.getNote()).compareTo(n2 == null ? "" : n2.getNote());
						if (cmp != 0) return cmp;
					}
					return o1.getUniqueId().compareTo(o2.getUniqueId());
				}
			});
			
			int firstRowIns = -1;
			int firstRowCrs = -1;
			String prevCourse = null;
			Iterator<ClassInstructor> classIterator = null;
			List<ClassInstructor> allClasses = new ArrayList<ClassInstructor>(instructor.getClasses().size());
			for (ClassInstructor ci: instructor.getClasses()) {
				if (ci.isLead() && !ci.getClassInstructing().isCancelled()) allClasses.add(ci);
			}
			Collections.sort(allClasses, new ClassInstructorComparator(new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY)));
			if (survey.getCourseRequirements().isEmpty() && allClasses.isEmpty()) continue;
			for (InstructorCourseRequirement req: reqs) {
				A[] line = new A[nrCols];
				String course = (req.getCourseOffering() != null ? req.getCourseOffering().getCourseName() : req.getCourse());
				if (firstRowIns < 0) {
					firstRowIns = printer.getRow();
					line[colExtId] = new A(instructor.getExternalUniqueId());
					line[colName] = new A(instructor.getName(instructorNameFormat));
					if (survey.getSubmitted() != null) {
						line[colSurv] = new A(df.format(survey.getSubmitted()));
					} else {
						line[colSurv] = new A(CMSG.instrSurveyNotSubmitted());
						line[colSurv].setColor("#dcb414");
					}
					List<A> time = new ArrayList<A>();
					List<A> room = new ArrayList<A>();
					List<A> dist = new ArrayList<A>();
					addPreferences(time, room, dist, instructor, survey.getPreferences(), instructorNameFormat);
					A other = new A(survey.getNote() == null ? "" : survey.getNote());
					line[colRTime] = new A(time);
					line[colRRoom] = new A(room);
					line[colRDist] = new A(dist);
					line[colROther] = other;
				}
				if (prevCourse == null || !course.equals(prevCourse)) {
					if (classIterator != null && classIterator.hasNext()) {
						int lastRowCrs = printer.getRow() - 1;
						while (classIterator.hasNext()) {
							ClassInstructor ci = classIterator.next();
							A[] x = new A[nrCols];
							fillClassInfo(x, ci, colClass, instructorNameFormat, classAssignment);
							printer.printLine(x);
						}
						if (lastRowCrs < printer.getRow() - 1) {
							for (int i = 0; i < types.size(); i++)
								printer.getSheet().addMergedRegion(new CellRangeAddress(lastRowCrs, printer.getRow() -1, colReq + i, colReq + i));
						}
					}
					if (firstRowCrs >= 0 && firstRowCrs < printer.getRow() - 1)
						printer.getSheet().addMergedRegion(new CellRangeAddress(firstRowCrs, printer.getRow() -1, colCourse, colCourse));
					prevCourse = course;
					classIterator = null;
					if (req.getCourseOffering() != null) {
						List<ClassInstructor> classes = new ArrayList<ClassInstructor>();
						for (Iterator<ClassInstructor> i = allClasses.iterator(); i.hasNext(); ) {
							ClassInstructor ci = i.next();
							if (ci.isLead() && ci.getClassInstructing().getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getCourseOfferings().contains(req.getCourseOffering())) {
								classes.add(ci);
								i.remove();
							}
						}
						classIterator = classes.iterator();
					}
					firstRowCrs = printer.getRow();
					if (req.getCourseOffering() == null)
						line[colCourse] = new A(req.getCourse());
					else {
						String title = req.getCourseOffering().getTitle();
						if (title != null && !title.isEmpty()) {
							A t = new A("  " + title); t.setColor("#808B96");
							line[colCourse] = new A(new A(req.getCourseOffering().getCourseName()), t);
						} else {
							line[colCourse] = new A(req.getCourseOffering().getCourseName());
						}
					}
				} 
				for (int i = 0; i < types.size(); i++) {
					InstructorCourseRequirementNote n = req.getNote(types.get(i));
					line[colReq + i] = new A(n == null ? "" : n.getNote());
				}
				if (classIterator != null && classIterator.hasNext()) {
					ClassInstructor ci = classIterator.next();
					fillClassInfo(line, ci, colClass, instructorNameFormat, classAssignment);
				}
				printer.printLine(line);
			}
			if (classIterator != null && classIterator.hasNext()) {
				int lastRowCrs = printer.getRow() - 1;
				while (classIterator.hasNext()) {
					ClassInstructor ci = classIterator.next();
					A[] x = new A[nrCols];
					fillClassInfo(x, ci, colClass, instructorNameFormat, classAssignment);
					printer.printLine(x);
				}
				if (lastRowCrs < printer.getRow() - 1) {
					for (int i = 0; i < types.size(); i++)
						printer.getSheet().addMergedRegion(new CellRangeAddress(lastRowCrs, printer.getRow() -1, colReq + i, colReq + i));
				}
			}
			if (firstRowCrs >= 0 && firstRowCrs < printer.getRow() - 1)
				printer.getSheet().addMergedRegion(new CellRangeAddress(firstRowCrs, printer.getRow() -1, colCourse, colCourse));
			if (!allClasses.isEmpty()) {
				firstRowCrs = -1;
				for (ClassInstructor ci: allClasses) {
					A[] line = new A[nrCols];
					if (firstRowIns < 0) {
						firstRowIns = printer.getRow();
						line[colExtId] = new A(instructor.getExternalUniqueId());
						line[colName] = new A(instructor.getName(instructorNameFormat));
						if (survey.getSubmitted() != null) {
							line[colSurv] = new A(df.format(survey.getSubmitted()));
						} else {
							line[colSurv] = new A(CMSG.instrSurveyNotSubmitted());
							line[colSurv].setColor("#dcb414");
						}
						List<A> time = new ArrayList<A>();
						List<A> room = new ArrayList<A>();
						List<A> dist = new ArrayList<A>();
						addPreferences(time, room, dist, instructor, survey.getPreferences(), instructorNameFormat);
						A other = new A(survey.getNote() == null ? "" : survey.getNote());
						line[colRTime] = new A(time);
						line[colRRoom] = new A(room);
						line[colRDist] = new A(dist);
						line[colROther] = other;
					}
					if (firstRowCrs < 0) {
						firstRowCrs = printer.getRow();
						line[colCourse] = new A(CMSG.courseOtherInstructorClasses());
					}
					fillClassInfo(line, ci, colClass, instructorNameFormat, classAssignment);
					printer.printLine(line);
				}
				printer.getSheet().addMergedRegion(new CellRangeAddress(firstRowCrs, printer.getRow() -1, colCourse, colCourse + types.size()));
			}
			if (firstRowCrs >= 0 && firstRowCrs < printer.getRow() -1) {
				printer.getSheet().addMergedRegion(new CellRangeAddress(firstRowIns, printer.getRow() -1, colExtId, colExtId));
				printer.getSheet().addMergedRegion(new CellRangeAddress(firstRowIns, printer.getRow() -1, colName, colName));
				printer.getSheet().addMergedRegion(new CellRangeAddress(firstRowIns, printer.getRow() -1, colSurv, colSurv));
				printer.getSheet().addMergedRegion(new CellRangeAddress(firstRowIns, printer.getRow() -1, colRTime, colRTime));
				printer.getSheet().addMergedRegion(new CellRangeAddress(firstRowIns, printer.getRow() -1, colRRoom, colRRoom));
				printer.getSheet().addMergedRegion(new CellRangeAddress(firstRowIns, printer.getRow() -1, colRDist, colRDist));
				printer.getSheet().addMergedRegion(new CellRangeAddress(firstRowIns, printer.getRow() -1, colROther, colROther));
			}
			printer.flush();
		}
		
		printer.close();
	}
	
	protected AssignmentService<ClassAssignmentProxy> getClassAssignmentService() {
		return (AssignmentService<ClassAssignmentProxy>)SpringApplicationContextHolder.getBean("classAssignmentService");
	}
	
	protected void fillClassInfo(A[] line, ClassInstructor ci, int colClass, String instructorNameFormat, ClassAssignmentProxy classAssignment) {
		line[colClass + 0] = new A(ci.getClassInstructing().getClassLabel(ApplicationProperty.DistributionsShowClassSufix.isTrue()));
		List<A> time = new ArrayList<A>();
		List<A> room = new ArrayList<A>();
		List<A> dist = new ArrayList<A>();
		for (RoomPref pref: ci.getClassInstructing().effectivePreferences(RoomPref.class)) {
			A a = new A(pref.getPrefLevel().getPrefAbbv() + " " + pref.preferenceAbbv(instructorNameFormat));
			a.setColor(PreferenceLevel.prolog2color(pref.getPrefLevel().getPrefProlog()));
			room.add(a);
		}
		for (RoomGroupPref pref: ci.getClassInstructing().effectivePreferences(RoomGroupPref.class)) {
			A a = new A(pref.getPrefLevel().getPrefAbbv() + " " + pref.preferenceAbbv(instructorNameFormat));
			a.setColor(PreferenceLevel.prolog2color(pref.getPrefLevel().getPrefProlog()));
			room.add(a);
		}
		for (RoomFeaturePref pref: ci.getClassInstructing().effectivePreferences(RoomFeaturePref.class)) {
			A a = new A(pref.getPrefLevel().getPrefAbbv() + " " + pref.preferenceAbbv(instructorNameFormat));
			a.setColor(PreferenceLevel.prolog2color(pref.getPrefLevel().getPrefProlog()));
			room.add(a);
		}
		for (BuildingPref pref: ci.getClassInstructing().effectivePreferences(BuildingPref.class)) {
			A a = new A(pref.getPrefLevel().getPrefAbbv() + " " + pref.preferenceAbbv(instructorNameFormat));
			a.setColor(PreferenceLevel.prolog2color(pref.getPrefLevel().getPrefProlog()));
			room.add(a);
		}
		for (TimePref pref: ci.getClassInstructing().effectivePreferences(TimePref.class)) {
			TimePatternModel m = pref.getTimePatternModel();
			time.add(new A(pref.getTimePattern().getName()));
			time.addAll(timePrefToA(m));
		}
		for (DistributionPref pref: ci.getClassInstructing().effectivePreferences(DistributionPref.class)) {
			A a = new A(pref.getPrefLevel().getPrefAbbv() + " " + pref.preferenceAbbv(instructorNameFormat));
			a.setColor(PreferenceLevel.prolog2color(pref.getPrefLevel().getPrefProlog()));
			dist.add(a);
		}
		line[colClass + 1] = new A(time);
		line[colClass + 2] = new A(room);
		line[colClass + 3] = new A(dist);
		if (classAssignment != null) {
			AssignmentInfo a = null;
			AssignmentPreferenceInfo p = null;
			try {
				a = classAssignment.getAssignment(ci.getClassInstructing());
				p = classAssignment.getAssignmentInfo(ci.getClassInstructing());
			} catch (Exception e) {}
			if (a != null && p != null && a.getTimeLocation() != null) {
				line[colClass + 4] = new A(a.getTimeLocation().getLongName(CONSTANTS.useAmPm()));
				if (p.getTimePreference() != Constants.sPreferenceLevelNeutral)
					line[colClass + 4].setColor(PreferenceLevel.int2color(p.getTimePreference()));
			
			} if (a != null && p != null && a.getRooms() != null && !a.getRooms().isEmpty()) {
				List<A> rx = new ArrayList<A>();
				for (Location r: a.getRooms()) {
					A ax = new A(r.getLabel());
					if (p.getRoomPreference(r.getUniqueId()) != Constants.sPreferenceLevelNeutral)
						ax.setColor(PreferenceLevel.int2color(p.getRoomPreference(r.getUniqueId())));
					rx.add(ax);
				}
				line[colClass + 5] = new A(rx);
			}
		}
	}
	
	protected List<A> timePrefToA(TimePatternModel m, TimePref pref, TimePref other) {
		List<A> ret = timePrefToA(m);
		if (other != null) {
			for (A a: ret) {
				boolean match = false;
				TimePatternModel x = ((TimePref) other).getTimePatternModel();
				x.setMode("|" + ApplicationProperty.InstructorSurveyTimePreferences.value());
				for (A b: timePrefToA(x)) {
					if (a.equals(b)) match = true;
				}
				if (!match)
					a.set(F.UNDERLINE);
			}
		} else {
			for (A a: ret)
				a.set(F.UNDERLINE);
		}
		return ret;
	}
	
	protected void addPreference(List<A> ret, Preference pref, PreferenceLevel instructorPref, String instructorNameFormat) {
		A a = new A(pref.getPrefLevel().getPrefName() + " " + pref.preferenceText(instructorNameFormat));
		a.setColor(PreferenceLevel.prolog2color(pref.getPrefLevel().getPrefProlog()));
		if (instructorPref == null)
			a.set(F.UNDERLINE);
		else if (PreferenceLevel.prolog2int(instructorPref.getPrefProlog()) != PreferenceLevel.prolog2int(pref.getPrefLevel().getPrefProlog()))
			a.set(F.UNDERLINE);
		ret.add(a);
		if (pref.getPrefLevel().isHard() && pref.getNote() != null && !pref.getNote().isEmpty())
			ret.add(new A(" - " + pref.getNote()));
	}
	
	protected void addPreference(List<A> ret, Preference pref, String instructorNameFormat) {
		A a = new A((pref.getPrefLevel().getPrefAbbv() == null || pref.getPrefLevel().getPrefAbbv().isEmpty() ? "" : pref.getPrefLevel().getPrefAbbv() + " ") +
				pref.preferenceText(instructorNameFormat));
		a.setColor(PreferenceLevel.prolog2color(pref.getPrefLevel().getPrefProlog()));
		ret.add(a);
		if (pref.getPrefLevel().isHard() && pref.getNote() != null && !pref.getNote().isEmpty())
			ret.add(new A(" - " + pref.getNote()));
	}
	
	protected boolean applies(DepartmentalInstructor instructor, Preference pref) {
		if (pref instanceof RoomPref && !instructor.getAvailableRooms().contains(((RoomPref)pref).getRoom())) return false;
		if (pref instanceof RoomGroupPref && !instructor.getAvailableRoomGroups().contains(((RoomGroupPref)pref).getRoomGroup())) return false;
		if (pref instanceof RoomFeaturePref && !instructor.getAvailableRoomFeatures().contains(((RoomFeaturePref)pref).getRoomFeature())) return false;
		if (pref instanceof BuildingPref && !instructor.getAvailableBuildings().contains(((BuildingPref)pref).getBuilding())) return false;
		return true;
	}
	
	protected boolean match(Preference p1, Preference p2) {
		if (p1.getType() != p2.getType()) return false;
		if (p1 instanceof RoomPref && ((RoomPref)p1).getRoom().equals(((RoomPref)p2).getRoom()))
			return true;
		if (p1 instanceof RoomGroupPref && ((RoomGroupPref)p1).getRoomGroup().equals(((RoomGroupPref)p2).getRoomGroup()))
			return true;
		if (p1 instanceof RoomFeaturePref && ((RoomFeaturePref)p1).getRoomFeature().equals(((RoomFeaturePref)p2).getRoomFeature()))
			return true;
		if (p1 instanceof BuildingPref && ((BuildingPref)p1).getBuilding().equals(((BuildingPref)p2).getBuilding()))
			return true;
		if (p1 instanceof DistributionPref && ((DistributionPref)p1).getDistributionType().equals(((DistributionPref)p2).getDistributionType()))
			return true;
		if (p1 instanceof TimePref) return true;
		return false;
	}
	
	protected void addPreferences(List<A> time, List<A> room, List<A> dist, DepartmentalInstructor instructor, Collection<Preference> preferences, Collection<Preference> compare, String instructorNameFormat) {
		for (Preference pref: sorted(preferences)) {
			if (!applies(instructor, pref)) continue;
			Preference otherPref = null;
			PreferenceLevel other = null;
			for (Preference p: compare)
				if (match(pref, p)) {
					otherPref = p;
					other = p.getPrefLevel();
					break;
				}
			if (pref instanceof RoomPref || pref instanceof RoomGroupPref || pref instanceof RoomFeaturePref || pref instanceof BuildingPref) {
				addPreference(room, pref, other, instructorNameFormat);
			} else if (pref instanceof TimePref) {
				TimePatternModel m = ((TimePref) pref).getTimePatternModel();
				m.setMode("|" + ApplicationProperty.InstructorSurveyTimePreferences.value());
				time.addAll(timePrefToA(m, (TimePref)pref, (TimePref)otherPref));
				if (m.hasProgibitedPreferences() && pref.getNote() != null && !pref.getNote().isEmpty())
					time.add(new A(pref.getNote()));
			} else if (pref instanceof DistributionPref) {
				addPreference(dist, pref, other, instructorNameFormat);
			}
		}
	}
	
	protected void addPreferences(List<A> time, List<A> room, List<A> dist, DepartmentalInstructor instructor, Collection<Preference> preferences, String instructorNameFormat) {
		for (Preference pref: sorted(preferences)) {
			if (!applies(instructor, pref)) continue;
			if (pref instanceof RoomPref || pref instanceof RoomGroupPref || pref instanceof RoomFeaturePref || pref instanceof BuildingPref) {
				addPreference(room, pref, instructorNameFormat);
			} else if (pref instanceof TimePref) {
				TimePatternModel m = ((TimePref) pref).getTimePatternModel();
				m.setMode("|" + ApplicationProperty.InstructorSurveyTimePreferences.value());
				time.addAll(timePrefToA(m));
				if (m.hasProgibitedPreferences() && pref.getNote() != null && !pref.getNote().isEmpty())
					time.add(new A(pref.getNote()));
			} else if (pref instanceof DistributionPref) {
				addPreference(dist, pref, instructorNameFormat);
			}
		}
	}
	
	protected List<A> timePrefToA(TimePatternModel m) {
    	Integer firstDayOfWeek = ApplicationProperty.TimePatternFirstDayOfWeek.intValue();
    	List<A> ret = new ArrayList<A>();
    	if (m.isExactTime()) {
    		int days = m.getExactDays();
    		int startSlot = m.getExactStartSlot();
    		StringBuffer sb = new StringBuffer();
    		for (int i=0;i<Constants.DAY_CODES.length;i++) {
    			int j = (firstDayOfWeek == null ? i : (i + firstDayOfWeek) % 7);
    			if ((Constants.DAY_CODES[j]&days)!=0)
    				sb.append(CONSTANTS.shortDays()[j]);
    		}
    		sb.append(" ");
    		sb.append(Constants.toTime(startSlot * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN));
    		ret.add(new A(sb.toString()));
    		return ret;
    	} else {
    		boolean canMergeDays = true;
    		for (int i = 0; canMergeDays && i+1 < m.getNrDays(); i++) {
    			for (int j = i+1; canMergeDays && j < m.getNrDays(); j++) {
    				if ((m.getDayCode(i) & m.getDayCode(j))!=0) canMergeDays = false;
    			}
    		}
    		boolean out[][] = new boolean [m.getNrDays()][m.getNrTimes()];
            for (int i = 0; i < m.getNrDays(); i++)
                   for (int j = 0; j < m.getNrTimes(); j++)
                	   out[i][j]=false;
            for (int i = 0; i < m.getNrDays(); i++)
                for (int j = 0; j < m.getNrTimes(); j++) {
             	   if (out[i][j]) continue;
             	   out[i][j]=true;
             	   if (PreferenceLevel.sNeutral.equals(m.getPreference(i,j))) continue;
             	   int endDay = i, endTime = j;
             	   while (endTime+1<m.getNrTimes() && !out[i][endTime+1] && m.getPreference(i,endTime+1).equals(m.getPreference(i,j)))
             		   endTime++;
             	   if (i==0) {
             		   boolean same = true;
             		   for (int k=i;k+1<m.getNrDays();k++) {
                 		   for (int x=j;x<=endTime;x++) {
                 			   if (!out[k+1][x] && !m.getPreference(i,x).equals(m.getPreference(k+1,x))) {
                 				   same = false; break;
                 			   }
                 			   if (!same) break;
                 		   }
                 		   if (!same) break;
             		   }
             		   if (same) endDay = m.getNrDays()-1;
             	   }
             	   while (canMergeDays && endDay+1<m.getNrDays()) {
             		   boolean same = true;
             		   for (int x=j;x<=endTime;x++)
             			   if (!out[endDay+1][x] && !m.getPreference(i,x).equals(m.getPreference(endDay+1,x))) {
             				   same = false; break;
             			   }
             		   if (!same) break;
             		   endDay++;
             	   }
             	   for (int a=i;a<=endDay;a++)
             		   for (int b=j;b<=endTime;b++)
             			   out[a][b]=true;
             	   StringBuffer sb = new StringBuffer(PreferenceLevel.prolog2abbv(m.getPreference(i,j))+" ");
             	   int nrDays = 0;
              	  for (int x=0;x<Constants.DAY_CODES.length;x++) {
             		  boolean thisDay = false;
             		  for (int a=i;a<=endDay;a++)
             			  if ((m.getDayCode(a) & Constants.DAY_CODES[x])!=0)
             				 thisDay = true;
             		  if (thisDay) nrDays++;
             	  }
             	   
             	  for (int x=0;x<Constants.DAY_CODES.length;x++) {
             		  int y = (firstDayOfWeek == null ? x : (x + firstDayOfWeek) % 7);
             		  boolean thisDay = false;
             		  for (int a=i;a<=endDay;a++)
             			  if ((m.getDayCode(a) & Constants.DAY_CODES[y])!=0)
             				 thisDay = true;
             		  if (thisDay)
             			  sb.append(nrDays==1?CONSTANTS.days()[y]:CONSTANTS.shortDays()[y]);
             	  }
             	  String d1 = " ";
             	  String d2 = " ";
             	  for (int x = 0; x < 7; x++) {
             		 int y = (firstDayOfWeek == null ? x : (x + firstDayOfWeek) % 7);
             		 if (x < 5) d1 += CONSTANTS.shortDays()[y];
             		  d2 += CONSTANTS.shortDays()[y];
             	  }
             	  if (m.getTimePattern()!=null && sb.toString().endsWith(d1))
             		  sb.delete(sb.length()-d1.length(), sb.length());
             	  if (m.getTimePattern()==null && sb.toString().endsWith(d2))
             		  sb.delete(sb.length()-d2.length(), sb.length());
             	  if (j==0 && endTime+1==m.getNrTimes()) {
             		  //all day
             	  } else {
             		  sb.append(" ");
             		  sb.append(Constants.toTime(m.getSlot(j)));
             		  sb.append(" - ");
                      sb.append(Constants.toTime(m.getSlot(endTime) + m.getSlotsPerMtg()*Constants.SLOT_LENGTH_MIN - m.getBreakTime()));
             	  }
             	  A a = new A(sb.toString());
             	  a.setColor(PreferenceLevel.prolog2color(m.getPreference(i,j)));
             	  ret.add(a);
                }
            return ret;
    	}
    }
	
	protected <T extends Preference> List<T> sorted(Collection<T> input) {
		List<T> list = new ArrayList<T>(input);
		Collections.sort(list, new Comparator<Preference>() {
			@Override
			public int compare(Preference p1, Preference p2) {
				if (p1.getType() != p2.getType())
					return p1.getType().compareTo(p2.getType());
				int cmp = p1.preferenceText().compareTo(p2.preferenceText());
				if (cmp != 0) return cmp;
				return p1.compareTo(p2);
			}
		});
		return list;
	}
}
