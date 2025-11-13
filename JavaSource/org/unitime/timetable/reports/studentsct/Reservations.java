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
package org.unitime.timetable.reports.studentsct;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.util.CSVFile;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.studentsct.StudentSectioningModel;
import org.cpsolver.studentsct.model.Course;
import org.cpsolver.studentsct.model.CourseRequest;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.Offering;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.report.AbstractStudentSectioningReport;
import org.cpsolver.studentsct.reservation.Reservation;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.ReservationInterface;
import org.unitime.timetable.gwt.shared.ReservationInterface.Areas;
import org.unitime.timetable.gwt.shared.ReservationInterface.CourseReservation;
import org.unitime.timetable.gwt.shared.ReservationInterface.CurriculumReservation;
import org.unitime.timetable.gwt.shared.ReservationInterface.GroupReservation;
import org.unitime.timetable.gwt.shared.ReservationInterface.IdName;
import org.unitime.timetable.gwt.shared.ReservationInterface.IndividualReservation;
import org.unitime.timetable.gwt.shared.ReservationInterface.LCReservation;
import org.unitime.timetable.gwt.shared.ReservationInterface.OverrideReservation;
import org.unitime.timetable.gwt.shared.ReservationInterface.UniversalReservation;
import org.unitime.timetable.solver.studentsct.StudentSolver;

/**
 * @author Tomas Muller
 */
public class Reservations extends AbstractStudentSectioningReport {
	protected static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
    
    public Reservations(StudentSectioningModel model) {
        super(model);
    }
    
	@Override
	public CSVFile createTable(Assignment<Request, Enrollment> assignment, DataProperties properties) {
		CSVFile csv = new CSVFile();
		csv.setHeader(new CSVFile.CSVField[] {
				new CSVFile.CSVField("__Offering"),
        		new CSVFile.CSVField(MESSAGES.colInstructionalOffering().replace("<br>", "\n")),
        		new CSVFile.CSVField(MESSAGES.colReservationType().replace("<br>", "\n")),
        		new CSVFile.CSVField(MESSAGES.colOwner()),
        		new CSVFile.CSVField(MESSAGES.colRestrictions()),
        		new CSVFile.CSVField(MESSAGES.colReservedSpace().replace("<br>", "\n")),
        		new CSVFile.CSVField(MESSAGES.colCurrentEnrollment().replace("<br>", "\n")),
                });
		
		List<ReservationInterface> reservations = new ArrayList<ReservationInterface>();
		Map<Long, Set<Long>> studentTable = new HashMap<Long, Set<Long>>();
		for (Offering offering: getModel().getOfferings()) {
			if (offering.isDummy()) continue;
			for (Reservation r: offering.getReservations()) {
				// if (r.isExpired()) continue;
				Set<Long> studentIds = new HashSet<Long>();
				for (Course course: r.getOffering().getCourses())
					for (CourseRequest cr: course.getRequests())
						if (r.isApplicable(cr.getStudent(), course) && matches(cr) && matches(offering.getCourse(cr.getStudent())))
							studentIds.add(cr.getStudent().getId());
				if (studentIds != null && !studentIds.isEmpty()) {
					int assigned = 0, total = 0;
					for (Enrollment e: r.getEnrollments(assignment)) {
						total ++;
						if (studentIds.contains(e.getStudent().getId()))
							assigned ++;
					}
					
					ReservationInterface reservation = StudentSolver.convert(r, getModel(), assignment);
					reservation.setEnrollment(assigned);
					reservation.setLastLike(total);
					reservations.add(reservation);
					studentTable.put(reservation.getId(), studentIds);
				}
			}
		}
		Collections.sort(reservations);
		for (ReservationInterface reservation: reservations) {
			Set<Long> studentIds = studentTable.get(reservation.getId());
			int total = reservation.getLastLike();
			int assigned = reservation.getEnrollment();
			List<CSVFile.CSVField> line = new ArrayList<CSVFile.CSVField>();
			
			
			line.add(new CSVFile.CSVField(reservation.getOffering().getId()));
			
			String course = reservation.getOffering().getAbbv();
			for (ReservationInterface.Course c: reservation.getOffering().getCourses())
				if (!c.isControl())
					course += "\n  " + c.getAbbv();
			line.add(new CSVFile.CSVField(course));

			String flags = "";
			if (reservation.isAllowOverlaps())
				flags += "\n  " + MESSAGES.checkCanOverlap();
			if (reservation.isOverLimit())
				flags += "\n  " + MESSAGES.checkCanOverLimit();
			if (reservation.isMustBeUsed())
				flags += "\n  " + MESSAGES.checkMustBeUsed();
			if (reservation.isExpired())
				flags += "\n  " + MESSAGES.checkAllwaysExpired();
			
			Integer limit = reservation.getReservationLimit();
			
			if (reservation instanceof CourseReservation) {
				line.add(new CSVFile.CSVField(MESSAGES.reservationCourseAbbv() + flags));
				ReservationInterface.Course c = ((CourseReservation)reservation).getCourse();
				limit = c.getLimit();
				line.add(new CSVFile.CSVField(c.getAbbv()));
			} else if (reservation instanceof IndividualReservation) {
				if (reservation.isOverride()) {
					line.add(new CSVFile.CSVField(MESSAGES.reservationIndividualOverrideAbbv() + flags));
				} else if (reservation instanceof OverrideReservation) {
					String type = CONSTANTS.reservationOverrideTypeAbbv()[((OverrideReservation)reservation).getType().ordinal()];
					line.add(new CSVFile.CSVField(type == null ? ((OverrideReservation)reservation).getType().name() + flags: type));
				} else {
					line.add(new CSVFile.CSVField(MESSAGES.reservationIndividualAbbv() + flags));
				}
				String students = "";
				if (limit == null)
					limit = ((IndividualReservation) reservation).getStudents().size();
				for (IdName student: ((IndividualReservation) reservation).getStudents()) {
					if (studentIds.contains(student.getId()))
						students += (students.isEmpty() ? "" : "\n") + student.getName();
				}
				line.add(new CSVFile.CSVField(students));
			} else if (reservation instanceof GroupReservation) {
				if (reservation.isOverride()) {
					line.add(new CSVFile.CSVField(MESSAGES.reservationStudentGroupOverrideAbbv() + flags));
				} else {
					line.add(new CSVFile.CSVField(MESSAGES.reservationStudentGroupAbbv() + flags));
				}
				IdName group = ((GroupReservation) reservation).getGroup();
				line.add(new CSVFile.CSVField(group.getAbbv() + " - " + group.getName() + " (" + group.getLimit() + ")"));
			} else if (reservation instanceof LCReservation) {
				if (reservation.getOffering().getCourses().size() > 1) {
					ReservationInterface.Course c = ((LCReservation) reservation).getCourse();
					line.add(new CSVFile.CSVField(MESSAGES.reservationLearningCommunityAbbv() + "\n  " + c.getAbbv() + flags));
				} else {
					line.add(new CSVFile.CSVField(MESSAGES.reservationLearningCommunityAbbv() + flags));
				}
				IdName group = ((LCReservation) reservation).getGroup();
				line.add(new CSVFile.CSVField(group.getAbbv() + " - " + group.getName() + " (" + group.getLimit() + ")"));
			} else if (reservation instanceof CurriculumReservation) {
				if (reservation.isOverride())
					line.add(new CSVFile.CSVField(MESSAGES.reservationCurriculumOverride() + flags));
				else
					line.add(new CSVFile.CSVField(MESSAGES.reservationCurriculumAbbv() + flags));
				Areas curriculum = ((CurriculumReservation) reservation).getCurriculum();
				String owner = "";
				for (IdName area: curriculum.getAreas()) {
					owner += (owner.isEmpty() ? "" : "\n") + area.getAbbv() + " - " + area.getName();
					for (IdName major: curriculum.getMajors()) {
						if (!area.getId().equals(major.getParentId())) continue;
						owner += (owner.isEmpty() ? "  " : "\n  ") + major.getAbbv() + " - " + major.getName();
						for (IdName conc: curriculum.getConcentrations()) {
							if (!major.getId().equals(conc.getParentId())) continue;
							owner += (owner.isEmpty() ? "    " : "\n    ") + conc.getAbbv() + " - " + conc.getName();
						}
					}
					for (IdName minor: curriculum.getMinors()) {
						if (!area.getId().equals(minor.getParentId())) continue;
						owner += (owner.isEmpty() ? "  " : "\n  ") + minor.getAbbv() + " - " + minor.getName();
					}
				}
				boolean firstClasf = true;
				for (IdName clasf: curriculum.getClassifications()) {
					if (curriculum.getAreas().size() == 1)
						owner += (owner.isEmpty() ? "  " : "\n  ") + clasf.getAbbv() + " - " + clasf.getName();
					else if (firstClasf) {
						owner += (owner.isEmpty() ? "\n" : "\n\n") + clasf.getAbbv() + " - " + clasf.getName();
						firstClasf = false;
					} else 
						owner += (owner.isEmpty() ? "" : "\n") + clasf.getAbbv() + " - " + clasf.getName();
				}
				for (IdName major: curriculum.getMajors()) {
					if (major.getParentId() != null) continue;
					owner += (owner.isEmpty() ? "  " : "\n  ") + major.getAbbv() + " - " + major.getName();
					for (IdName conc: curriculum.getConcentrations()) {
						if (!major.getId().equals(conc.getParentId())) continue;
						owner += (owner.isEmpty() ? "    " : "\n    ") + conc.getAbbv() + " - " + conc.getName();
					}
				}
				for (IdName minor: curriculum.getMinors()) {
					if (minor.getParentId() != null) continue;
					owner += (owner.isEmpty() ? "  " : "\n  ") + minor.getAbbv() + " - " + minor.getName();
				}
				line.add(new CSVFile.CSVField(owner));
			} else if (reservation instanceof UniversalReservation) {
				line.add(new CSVFile.CSVField(MESSAGES.reservationUniversalOverrideAbbv() + flags));
				String filter = ((UniversalReservation) reservation).getFilter();
				line.add(new CSVFile.CSVField(filter == null ? "" : filter));
			} else {
				line.add(new CSVFile.CSVField(MESSAGES.reservationUnknownAbbv() + flags));
				line.add(new CSVFile.CSVField(""));
			}
			String restrictions = "";
			for (ReservationInterface.Config config: reservation.getConfigs()) {
				restrictions += (restrictions.isEmpty() ? "" : "\n") + MESSAGES.selectionConfiguration(config.getName(), config.getLimit() == null ? MESSAGES.configUnlimited() : config.getLimit().toString());
			}
			for (ReservationInterface.Clazz clazz: reservation.getClasses()) {
				restrictions += (restrictions.isEmpty() ? "" : "\n") + clazz.getName() + (clazz.getLimit() == null ? "" : " (" + clazz.getLimit() + ")");
			}
			
			line.add(new CSVFile.CSVField(restrictions));
			line.add(new CSVFile.CSVField(limit == null ? "\u221E" : String.valueOf(limit)));
			line.add(new CSVFile.CSVField(assigned < total ? assigned + " / " + total : String.valueOf(assigned)));

			csv.addLine(line);
		}
		

		return csv;
	}

}
