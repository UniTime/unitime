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
package org.unitime.timetable.server.sectioning;

import java.util.Collection;

import org.cpsolver.studentsct.report.DistanceConflictTable;
import org.cpsolver.studentsct.report.RequestGroupTable;
import org.cpsolver.studentsct.report.SectionConflictTable;
import org.cpsolver.studentsct.report.TimeOverlapConflictTable;
import org.cpsolver.studentsct.report.UnbalancedSectionsTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.client.sectioning.SectioningReports.ReportTypeInterface;
import org.unitime.timetable.gwt.client.sectioning.SectioningReports.SectioningReportTypesRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.reports.studentsct.IndividualStudentTimeOverlaps;
import org.unitime.timetable.reports.studentsct.PerturbationsReport;
import org.unitime.timetable.reports.studentsct.StudentAvailabilityConflicts;
import org.unitime.timetable.reports.studentsct.UnasignedCourseRequests;
import org.unitime.timetable.reports.studentsct.UnusedReservations;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.solver.studentsct.StudentSolverProxy;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(SectioningReportTypesRpcRequest.class)
public class SectioningReportTypesBackend implements GwtRpcImplementation<SectioningReportTypesRpcRequest, GwtRpcResponseList<ReportTypeInterface>> {
	protected static final StudentSectioningMessages SCT_MSG = Localization.create(StudentSectioningMessages.class);
	@Autowired SolverService<StudentSolverProxy> studentSectioningSolverService;
	
	public static enum ReportType {
		TIME_CONFLICTS("Time Conflicts", SectionConflictTable.class.getName(), "type", "OVERLAPS", "overlapsIncludeAll", "true"),
		AVAILABLE_CONFLICTS("Availability Conflicts", SectionConflictTable.class.getName(), "type", "UNAVAILABILITIES", "overlapsIncludeAll", "true"),
		SECTION_CONFLICTS("Time & Availability Conflicts", SectionConflictTable.class.getName(), "type", "OVERLAPS_AND_UNAVAILABILITIES", "overlapsIncludeAll", "true"),
		UNBALANCED_SECTIONS("Unbalanced Classes", UnbalancedSectionsTable.class.getName()),
		DISTANCE_CONFLICTS("Distance Conflicts", DistanceConflictTable.class.getName()),
		TIME_OVERLAPS("Time Overlaps", TimeOverlapConflictTable.class.getName()),
		REQUEST_GROUPS("Request Groups", RequestGroupTable.class.getName()),
		PERTURBATIONS("Perturbations", PerturbationsReport.class.getName()),
		INDIVIDUAL_TIME_OVERLAPS("Individual Student Time Overlaps", IndividualStudentTimeOverlaps.class.getName()),
		NOT_ALLOWED_TIME_OVERLAPS("Not Allowed Time Overlaps", IndividualStudentTimeOverlaps.class.getName(), "includeAllowedOverlaps", "false"),
		INDIVIDUAL_TIME_OVERLAPS_BT("Individual Student Time Overlaps (Exclude Break Times)", IndividualStudentTimeOverlaps.class.getName(), "ignoreBreakTimeConflicts", "true"),
		NOT_ALLOWED_TIME_OVERLAPS_BT("Not Allowed Time Overlaps (Exclude Break Times)", IndividualStudentTimeOverlaps.class.getName(), "ignoreBreakTimeConflicts", "true", "includeAllowedOverlaps", "false"),
		TEACHING_CONFLICTS("Teaching Conflicts", StudentAvailabilityConflicts.class.getName()),
		TEACHING_CONFLICTS_NA("Teaching Conflicts (Exclude Allowed)", StudentAvailabilityConflicts.class.getName(), "includeAllowedOverlaps", "false"),
		NOT_ASSIGNED_COURSE_REQUESTS(SCT_MSG.reportUnassignedCourseRequests(), UnasignedCourseRequests.class.getName()),
		UNUSED_GROUP_RES(SCT_MSG.reportUnusedGroupReservations(), UnusedReservations.class.getName(), "type", "group"),
		UNUSED_INDIVIDUAL_RES(SCT_MSG.reportUnusedIndividualReservations(), UnusedReservations.class.getName(), "type", "individual"),
		UNUSED_OVERRIDE_RES(SCT_MSG.reportUnusedOverrideReservations(), UnusedReservations.class.getName(), "type", "override"),
		;
		
		String iName, iImplementation;
		String[] iParameters;
		ReportType(String name, String implementation, String... params) {
			iName = name; iImplementation = implementation; iParameters = params;
		}
		
		public String getName() { return iName; }
		public String getImplementation() { return iImplementation; }
		public String[] getParameters() { return iParameters; }
		public ReportTypeInterface toReportTypeInterface() {
			return new ReportTypeInterface(name(), iName, iImplementation, iParameters);
		}
	}

	@Override
	public GwtRpcResponseList<ReportTypeInterface> execute(SectioningReportTypesRpcRequest request, SessionContext context) {
		GwtRpcResponseList<ReportTypeInterface> ret = new GwtRpcResponseList<ReportTypeInterface>();
		for (ReportType type: ReportType.values())
			ret.add(type.toReportTypeInterface());

		if (!request.isOnline()) {
			StudentSolverProxy solver = studentSectioningSolverService.getSolver();
			if (solver != null) {
				Collection<ReportTypeInterface> types = solver.getReportTypes();
				if (types != null && !types.isEmpty())
					ret.addAll(types);
			}
		}

		return ret;
	}

}
