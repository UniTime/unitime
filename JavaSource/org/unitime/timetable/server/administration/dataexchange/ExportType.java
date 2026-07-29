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
package org.unitime.timetable.server.administration.dataexchange;

import java.util.Properties;

public enum ExportType {
	COURSES("offerings", "Course Offerings",
			"tmtbl.export.timetable", "false",
			"tmtbl.export.exam.type", "none"),
	COURSES_WITH_TIME("offerings", "Course Offerings (including course timetable)",
			"tmtbl.export.timetable", "true",
			"tmtbl.export.exam.type", "none"),
	COURSES_WITH_EXAMS("offerings", "Course Offerings (including exams)", 
			"tmtbl.export.timetable", "false",
			"tmtbl.export.exam.type", "all"),
	COURSES_ALL("offerings", "Course Offerings (including course timetable and exams)", 
			"tmtbl.export.timetable", "true",
			"tmtbl.export.exam.type", "all"),
	TIMETABLE("timetable", "Course Timetable"),
	EXAMS("exams", "Examinations",
			"tmtbl.export.exam", "true",
			"tmtbl.export.exam.type", "all"),
	EXAMS_FINAL("exams", "Examinations (only finals)",
			"tmtbl.export.exam", "true",
			"tmtbl.export.exam.type", "final"),
	EXAMS_MIDTERM("exams", "Examinations (only midterm)",
			"tmtbl.export.exam", "true",
			"tmtbl.export.exam.type", "midterm"),
	CURRICULA("curricula", "Curricula"),
	STUDENTS("students", "Students"),
	STUDENT_ENRL("studentEnrollments", "Student class enrollments"),
	REQUESTS("request", "Student course requests"),
	RESERVATIONS("reservations", "Reservations"),
	SESSION("session", "Academic Session"),
	PERMISSIONS("permissions", "Permissions"),
	TRAVELTIMES("traveltimes", "Travel Times"),
	ROOM_SHARING("roomSharing", "Room Sharing"),
	POINT_IN_TIME_DATA("pointInTimeData", "Point-In-Time Data"),
	PREFERENCES("preferences", "Course Timetabling Preferences"),
	SESSION_SETUP("sessionSetup", "Academic Session Setup"),
	STUDENT_ADVISORS("studentAdvisors", "Student Advisors"),
	STUDENT_STATUSES("studentStatuses", "Student Scheduling Statuses"),
	INSTRUCTOR_SURVEYS("instructorSurveys", "Instructor Surveys"),
	LAST_LIKE_COURSE_DEMANDS("lastLikeCourseDemand", "Last-Like Student Course Demands"),
	SCRIPTS("scripts", "Scripts"),
	REPORTS("reports", "Reports"),
	;
	
	private String iType, iLabel;
	private String[] iOptions;
	ExportType(String type, String label, String... options) {
		iType = type; iLabel = label; iOptions = options;
	}
	public String getType() { return iType; }
	public String getLabel() { return iLabel; }
	public void setOptions(Properties config) {
		for (int i = 0; i < iOptions.length; i += 2)
			config.put(iOptions[i], iOptions[i + 1]);
	}
}
