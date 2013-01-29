/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/


spool blank-data.log
set feedback off
set define off

prompt Disabling triggers for DATE_PATTERN...
alter table DATE_PATTERN disable all triggers;
prompt Disabling triggers for DEPT_STATUS_TYPE...
alter table DEPT_STATUS_TYPE disable all triggers;
prompt Disabling triggers for SESSIONS...
alter table SESSIONS disable all triggers;
prompt Disabling triggers for ACADEMIC_AREA...
alter table ACADEMIC_AREA disable all triggers;
prompt Disabling triggers for ACADEMIC_CLASSIFICATION...
alter table ACADEMIC_CLASSIFICATION disable all triggers;
prompt Disabling triggers for RESERVATION_TYPE...
alter table RESERVATION_TYPE disable all triggers;
prompt Disabling triggers for ACAD_AREA_RESERVATION...
alter table ACAD_AREA_RESERVATION disable all triggers;
prompt Disabling triggers for APPLICATION_CONFIG...
alter table APPLICATION_CONFIG disable all triggers;
prompt Disabling triggers for OFFR_CONSENT_TYPE...
alter table OFFR_CONSENT_TYPE disable all triggers;
prompt Disabling triggers for INSTRUCTIONAL_OFFERING...
alter table INSTRUCTIONAL_OFFERING disable all triggers;
prompt Disabling triggers for INSTR_OFFERING_CONFIG...
alter table INSTR_OFFERING_CONFIG disable all triggers;
prompt Disabling triggers for ITYPE_DESC...
alter table ITYPE_DESC disable all triggers;
prompt Disabling triggers for SCHEDULING_SUBPART...
alter table SCHEDULING_SUBPART disable all triggers;
prompt Disabling triggers for CLASS_...
alter table CLASS_ disable all triggers;
prompt Disabling triggers for SOLVER_GROUP...
alter table SOLVER_GROUP disable all triggers;
prompt Disabling triggers for SOLUTION...
alter table SOLUTION disable all triggers;
prompt Disabling triggers for TIME_PATTERN...
alter table TIME_PATTERN disable all triggers;
prompt Disabling triggers for ASSIGNMENT...
alter table ASSIGNMENT disable all triggers;
prompt Disabling triggers for POSITION_TYPE...
alter table POSITION_TYPE disable all triggers;
prompt Disabling triggers for DEPARTMENT...
alter table DEPARTMENT disable all triggers;
prompt Disabling triggers for DEPARTMENTAL_INSTRUCTOR...
alter table DEPARTMENTAL_INSTRUCTOR disable all triggers;
prompt Disabling triggers for ASSIGNED_INSTRUCTORS...
alter table ASSIGNED_INSTRUCTORS disable all triggers;
prompt Disabling triggers for ASSIGNED_ROOMS...
alter table ASSIGNED_ROOMS disable all triggers;
prompt Disabling triggers for BUILDING...
alter table BUILDING disable all triggers;
prompt Disabling triggers for PREFERENCE_LEVEL...
alter table PREFERENCE_LEVEL disable all triggers;
prompt Disabling triggers for BUILDING_PREF...
alter table BUILDING_PREF disable all triggers;
prompt Disabling triggers for CLASS_INSTRUCTOR...
alter table CLASS_INSTRUCTOR disable all triggers;
prompt Disabling triggers for STUDENT_STATUS_TYPE...
alter table STUDENT_STATUS_TYPE disable all triggers;
prompt Disabling triggers for STUDENT...
alter table STUDENT disable all triggers;
prompt Disabling triggers for FREE_TIME...
alter table FREE_TIME disable all triggers;
prompt Disabling triggers for COURSE_DEMAND...
alter table COURSE_DEMAND disable all triggers;
prompt Disabling triggers for SUBJECT_AREA...
alter table SUBJECT_AREA disable all triggers;
prompt Disabling triggers for COURSE_OFFERING...
alter table COURSE_OFFERING disable all triggers;
prompt Disabling triggers for COURSE_REQUEST...
alter table COURSE_REQUEST disable all triggers;
prompt Disabling triggers for CLASS_WAITLIST...
alter table CLASS_WAITLIST disable all triggers;
prompt Disabling triggers for CONSTRAINT_INFO...
alter table CONSTRAINT_INFO disable all triggers;
prompt Disabling triggers for COURSE_CATALOG...
alter table COURSE_CATALOG disable all triggers;
prompt Disabling triggers for COURSE_CREDIT_TYPE...
alter table COURSE_CREDIT_TYPE disable all triggers;
prompt Disabling triggers for COURSE_CREDIT_UNIT_CONFIG...
alter table COURSE_CREDIT_UNIT_CONFIG disable all triggers;
prompt Disabling triggers for COURSE_CREDIT_UNIT_TYPE...
alter table COURSE_CREDIT_UNIT_TYPE disable all triggers;
prompt Disabling triggers for COURSE_RESERVATION...
alter table COURSE_RESERVATION disable all triggers;
prompt Disabling triggers for COURSE_SUBPART_CREDIT...
alter table COURSE_SUBPART_CREDIT disable all triggers;
prompt Disabling triggers for CRSE_CREDIT_FORMAT...
alter table CRSE_CREDIT_FORMAT disable all triggers;
prompt Disabling triggers for CURRICULUM...
alter table CURRICULUM disable all triggers;
prompt Disabling triggers for CURRICULUM_COURSE...
alter table CURRICULUM_COURSE disable all triggers;
prompt Disabling triggers for CURRICULUM_GROUP...
alter table CURRICULUM_GROUP disable all triggers;
prompt Disabling triggers for CURRICULUM_COURSE_GROUP...
alter table CURRICULUM_COURSE_GROUP disable all triggers;
prompt Disabling triggers for POS_MAJOR...
alter table POS_MAJOR disable all triggers;
prompt Disabling triggers for CURRICULUM_MAJOR...
alter table CURRICULUM_MAJOR disable all triggers;
prompt Disabling triggers for CURRICULUM_RULE...
alter table CURRICULUM_RULE disable all triggers;
prompt Disabling triggers for DATE_PATTERN_DEPT...
alter table DATE_PATTERN_DEPT disable all triggers;
prompt Disabling triggers for DEMAND_OFFR_TYPE...
alter table DEMAND_OFFR_TYPE disable all triggers;
prompt Disabling triggers for TIMETABLE_MANAGER...
alter table TIMETABLE_MANAGER disable all triggers;
prompt Disabling triggers for DEPT_TO_TT_MGR...
alter table DEPT_TO_TT_MGR disable all triggers;
prompt Disabling triggers for DESIGNATOR...
alter table DESIGNATOR disable all triggers;
prompt Disabling triggers for DISTRIBUTION_TYPE...
alter table DISTRIBUTION_TYPE disable all triggers;
prompt Disabling triggers for DISTRIBUTION_PREF...
alter table DISTRIBUTION_PREF disable all triggers;
prompt Disabling triggers for DISTRIBUTION_OBJECT...
alter table DISTRIBUTION_OBJECT disable all triggers;
prompt Disabling triggers for DIST_TYPE_DEPT...
alter table DIST_TYPE_DEPT disable all triggers;
prompt Disabling triggers for EVENT_CONTACT...
alter table EVENT_CONTACT disable all triggers;
prompt Disabling triggers for EXAM_PERIOD...
alter table EXAM_PERIOD disable all triggers;
prompt Disabling triggers for EXAM...
alter table EXAM disable all triggers;
prompt Disabling triggers for SPONSORING_ORGANIZATION...
alter table SPONSORING_ORGANIZATION disable all triggers;
prompt Disabling triggers for EVENT...
alter table EVENT disable all triggers;
prompt Disabling triggers for EVENT_JOIN_EVENT_CONTACT...
alter table EVENT_JOIN_EVENT_CONTACT disable all triggers;
prompt Disabling triggers for EVENT_NOTE...
alter table EVENT_NOTE disable all triggers;
prompt Disabling triggers for EXACT_TIME_MINS...
alter table EXACT_TIME_MINS disable all triggers;
prompt Disabling triggers for EXAM_INSTRUCTOR...
alter table EXAM_INSTRUCTOR disable all triggers;
prompt Disabling triggers for EXAM_LOCATION_PREF...
alter table EXAM_LOCATION_PREF disable all triggers;
prompt Disabling triggers for EXAM_OWNER...
alter table EXAM_OWNER disable all triggers;
prompt Disabling triggers for EXAM_PERIOD_PREF...
alter table EXAM_PERIOD_PREF disable all triggers;
prompt Disabling triggers for EXAM_ROOM_ASSIGNMENT...
alter table EXAM_ROOM_ASSIGNMENT disable all triggers;
prompt Disabling triggers for EXTERNAL_BUILDING...
alter table EXTERNAL_BUILDING disable all triggers;
prompt Disabling triggers for ROOM_TYPE...
alter table ROOM_TYPE disable all triggers;
prompt Disabling triggers for EXTERNAL_ROOM...
alter table EXTERNAL_ROOM disable all triggers;
prompt Disabling triggers for EXTERNAL_ROOM_DEPARTMENT...
alter table EXTERNAL_ROOM_DEPARTMENT disable all triggers;
prompt Disabling triggers for EXTERNAL_ROOM_FEATURE...
alter table EXTERNAL_ROOM_FEATURE disable all triggers;
prompt Disabling triggers for HISTORY...
alter table HISTORY disable all triggers;
prompt Disabling triggers for HT_PREFERENCE...
alter table HT_PREFERENCE disable all triggers;
prompt Disabling triggers for INDIVIDUAL_RESERVATION...
alter table INDIVIDUAL_RESERVATION disable all triggers;
prompt Disabling triggers for JENRL...
alter table JENRL disable all triggers;
prompt Disabling triggers for LASTLIKE_COURSE_DEMAND...
alter table LASTLIKE_COURSE_DEMAND disable all triggers;
prompt Disabling triggers for SETTINGS...
alter table SETTINGS disable all triggers;
prompt Disabling triggers for MANAGER_SETTINGS...
alter table MANAGER_SETTINGS disable all triggers;
prompt Disabling triggers for MEETING...
alter table MEETING disable all triggers;
prompt Disabling triggers for NON_UNIVERSITY_LOCATION...
alter table NON_UNIVERSITY_LOCATION disable all triggers;
prompt Disabling triggers for OFFR_GROUP...
alter table OFFR_GROUP disable all triggers;
prompt Disabling triggers for OFFR_GROUP_OFFERING...
alter table OFFR_GROUP_OFFERING disable all triggers;
prompt Disabling triggers for POSITION_CODE_TO_TYPE...
alter table POSITION_CODE_TO_TYPE disable all triggers;
prompt Disabling triggers for POS_ACAD_AREA_MAJOR...
alter table POS_ACAD_AREA_MAJOR disable all triggers;
prompt Disabling triggers for POS_MINOR...
alter table POS_MINOR disable all triggers;
prompt Disabling triggers for POS_ACAD_AREA_MINOR...
alter table POS_ACAD_AREA_MINOR disable all triggers;
prompt Disabling triggers for POS_RESERVATION...
alter table POS_RESERVATION disable all triggers;
prompt Disabling triggers for RELATED_COURSE_INFO...
alter table RELATED_COURSE_INFO disable all triggers;
prompt Disabling triggers for ROLES...
alter table ROLES disable all triggers;
prompt Disabling triggers for ROOM...
alter table ROOM disable all triggers;
prompt Disabling triggers for ROOM_DEPT...
alter table ROOM_DEPT disable all triggers;
prompt Disabling triggers for ROOM_FEATURE...
alter table ROOM_FEATURE disable all triggers;
prompt Disabling triggers for ROOM_FEATURE_PREF...
alter table ROOM_FEATURE_PREF disable all triggers;
prompt Disabling triggers for ROOM_GROUP...
alter table ROOM_GROUP disable all triggers;
prompt Disabling triggers for ROOM_GROUP_PREF...
alter table ROOM_GROUP_PREF disable all triggers;
prompt Disabling triggers for ROOM_GROUP_ROOM...
alter table ROOM_GROUP_ROOM disable all triggers;
prompt Disabling triggers for ROOM_JOIN_ROOM_FEATURE...
alter table ROOM_JOIN_ROOM_FEATURE disable all triggers;
prompt Disabling triggers for ROOM_PREF...
alter table ROOM_PREF disable all triggers;
prompt Disabling triggers for ROOM_TYPE_OPTION...
alter table ROOM_TYPE_OPTION disable all triggers;
prompt Disabling triggers for SECTIONING_INFO...
alter table SECTIONING_INFO disable all triggers;
prompt Disabling triggers for SOLVER_GR_TO_TT_MGR...
alter table SOLVER_GR_TO_TT_MGR disable all triggers;
prompt Disabling triggers for SOLVER_INFO_DEF...
alter table SOLVER_INFO_DEF disable all triggers;
prompt Disabling triggers for SOLVER_PARAMETER_GROUP...
alter table SOLVER_PARAMETER_GROUP disable all triggers;
prompt Disabling triggers for SOLVER_PARAMETER_DEF...
alter table SOLVER_PARAMETER_DEF disable all triggers;
prompt Disabling triggers for SOLVER_PREDEF_SETTING...
alter table SOLVER_PREDEF_SETTING disable all triggers;
prompt Disabling triggers for SOLVER_PARAMETER...
alter table SOLVER_PARAMETER disable all triggers;
prompt Disabling triggers for STAFF...
alter table STAFF disable all triggers;
prompt Disabling triggers for STANDARD_EVENT_NOTE...
alter table STANDARD_EVENT_NOTE disable all triggers;
prompt Disabling triggers for STUDENT_ACAD_AREA...
alter table STUDENT_ACAD_AREA disable all triggers;
prompt Disabling triggers for STUDENT_ACCOMODATION...
alter table STUDENT_ACCOMODATION disable all triggers;
prompt Disabling triggers for STUDENT_CLASS_ENRL...
alter table STUDENT_CLASS_ENRL disable all triggers;
prompt Disabling triggers for STUDENT_ENRL...
alter table STUDENT_ENRL disable all triggers;
prompt Disabling triggers for STUDENT_ENRL_MSG...
alter table STUDENT_ENRL_MSG disable all triggers;
prompt Disabling triggers for STUDENT_GROUP...
alter table STUDENT_GROUP disable all triggers;
prompt Disabling triggers for STUDENT_GROUP_RESERVATION...
alter table STUDENT_GROUP_RESERVATION disable all triggers;
prompt Disabling triggers for STUDENT_MAJOR...
alter table STUDENT_MAJOR disable all triggers;
prompt Disabling triggers for STUDENT_MINOR...
alter table STUDENT_MINOR disable all triggers;
prompt Disabling triggers for STUDENT_TO_ACOMODATION...
alter table STUDENT_TO_ACOMODATION disable all triggers;
prompt Disabling triggers for STUDENT_TO_GROUP...
alter table STUDENT_TO_GROUP disable all triggers;
prompt Disabling triggers for TIME_PATTERN_DAYS...
alter table TIME_PATTERN_DAYS disable all triggers;
prompt Disabling triggers for TIME_PATTERN_DEPT...
alter table TIME_PATTERN_DEPT disable all triggers;
prompt Disabling triggers for TIME_PATTERN_TIME...
alter table TIME_PATTERN_TIME disable all triggers;
prompt Disabling triggers for TIME_PREF...
alter table TIME_PREF disable all triggers;
prompt Disabling triggers for TMTBL_MGR_TO_ROLES...
alter table TMTBL_MGR_TO_ROLES disable all triggers;
prompt Disabling triggers for USERS...
alter table USERS disable all triggers;
prompt Disabling triggers for USER_DATA...
alter table USER_DATA disable all triggers;
prompt Disabling triggers for WAITLIST...
alter table WAITLIST disable all triggers;
prompt Disabling triggers for XCONFLICT...
alter table XCONFLICT disable all triggers;
prompt Disabling triggers for XCONFLICT_EXAM...
alter table XCONFLICT_EXAM disable all triggers;
prompt Disabling triggers for XCONFLICT_INSTRUCTOR...
alter table XCONFLICT_INSTRUCTOR disable all triggers;
prompt Disabling triggers for XCONFLICT_STUDENT...
alter table XCONFLICT_STUDENT disable all triggers;
prompt Disabling foreign key constraints for DATE_PATTERN...
alter table DATE_PATTERN disable constraint FK_DATE_PATTERN_SESSION;
prompt Disabling foreign key constraints for SESSIONS...
alter table SESSIONS disable constraint FK_SESSIONS_STATUS_TYPE;
alter table SESSIONS disable constraint FK_SESSION_DATEPATT;
prompt Disabling foreign key constraints for ACADEMIC_AREA...
alter table ACADEMIC_AREA disable constraint FK_ACADEMIC_AREA_SESSION;
prompt Disabling foreign key constraints for ACADEMIC_CLASSIFICATION...
alter table ACADEMIC_CLASSIFICATION disable constraint FK_ACAD_CLASS_SESSION;
prompt Disabling foreign key constraints for ACAD_AREA_RESERVATION...
alter table ACAD_AREA_RESERVATION disable constraint FK_ACAD_AREA_RESV_ACAD_AREA;
alter table ACAD_AREA_RESERVATION disable constraint FK_ACAD_AREA_RESV_ACAD_CLASS;
alter table ACAD_AREA_RESERVATION disable constraint FK_ACAD_AREA_RESV_TYPE;
prompt Disabling foreign key constraints for INSTRUCTIONAL_OFFERING...
alter table INSTRUCTIONAL_OFFERING disable constraint FK_INSTR_OFFR_CONSENT_TYPE;
prompt Disabling foreign key constraints for INSTR_OFFERING_CONFIG...
alter table INSTR_OFFERING_CONFIG disable constraint FK_INSTR_OFFR_CFG_INSTR_OFFR;
prompt Disabling foreign key constraints for SCHEDULING_SUBPART...
alter table SCHEDULING_SUBPART disable constraint FK_SCHED_SUBPART_CONFIG;
alter table SCHEDULING_SUBPART disable constraint FK_SCHED_SUBPART_DATE_PATTERN;
alter table SCHEDULING_SUBPART disable constraint FK_SCHED_SUBPART_ITYPE;
alter table SCHEDULING_SUBPART disable constraint FK_SCHED_SUBPART_PARENT;
prompt Disabling foreign key constraints for CLASS_...
alter table CLASS_ disable constraint FK_CLASS_DATEPATT;
alter table CLASS_ disable constraint FK_CLASS_PARENT;
alter table CLASS_ disable constraint FK_CLASS_SCHEDULING_SUBPART;
prompt Disabling foreign key constraints for SOLVER_GROUP...
alter table SOLVER_GROUP disable constraint FK_SOLVER_GROUP_SESSION;
prompt Disabling foreign key constraints for SOLUTION...
alter table SOLUTION disable constraint FK_SOLUTION_OWNER;
prompt Disabling foreign key constraints for TIME_PATTERN...
alter table TIME_PATTERN disable constraint FK_TIME_PATTERN_SESSION;
prompt Disabling foreign key constraints for ASSIGNMENT...
alter table ASSIGNMENT disable constraint FK_ASSIGNMENT_CLASS;
alter table ASSIGNMENT disable constraint FK_ASSIGNMENT_SOLUTION;
alter table ASSIGNMENT disable constraint FK_ASSIGNMENT_TIME_PATTERN;
prompt Disabling foreign key constraints for DEPARTMENT...
alter table DEPARTMENT disable constraint FK_DEPARTMENT_SOLVER_GROUP;
alter table DEPARTMENT disable constraint FK_DEPARTMENT_STATUS_TYPE;
prompt Disabling foreign key constraints for DEPARTMENTAL_INSTRUCTOR...
alter table DEPARTMENTAL_INSTRUCTOR disable constraint FK_DEPT_INSTR_DEPT;
alter table DEPARTMENTAL_INSTRUCTOR disable constraint FK_DEPT_INSTR_POS_CODE_TYPE;
prompt Disabling foreign key constraints for ASSIGNED_INSTRUCTORS...
alter table ASSIGNED_INSTRUCTORS disable constraint FK_ASSIGNED_INSTRS_ASSIGNMENT;
alter table ASSIGNED_INSTRUCTORS disable constraint FK_ASSIGNED_INSTRS_INSTRUCTOR;
prompt Disabling foreign key constraints for ASSIGNED_ROOMS...
alter table ASSIGNED_ROOMS disable constraint FK_ASSIGNED_ROOMS_ASSIGNMENT;
prompt Disabling foreign key constraints for BUILDING...
alter table BUILDING disable constraint FK_BUILDING_SESSION;
prompt Disabling foreign key constraints for BUILDING_PREF...
alter table BUILDING_PREF disable constraint FK_BUILDING_PREF_BLDG;
alter table BUILDING_PREF disable constraint FK_BUILDING_PREF_LEVEL;
prompt Disabling foreign key constraints for CLASS_INSTRUCTOR...
alter table CLASS_INSTRUCTOR disable constraint FK_CLASS_INSTRUCTOR_CLASS;
alter table CLASS_INSTRUCTOR disable constraint FK_CLASS_INSTRUCTOR_INSTR;
prompt Disabling foreign key constraints for STUDENT...
alter table STUDENT disable constraint FK_STUDENT_SESSION;
alter table STUDENT disable constraint FK_STUDENT_STATUS_STUDENT;
prompt Disabling foreign key constraints for FREE_TIME...
alter table FREE_TIME disable constraint FK_FREE_TIME_SESSION;
prompt Disabling foreign key constraints for COURSE_DEMAND...
alter table COURSE_DEMAND disable constraint FK_COURSE_DEMAND_FREE_TIME;
alter table COURSE_DEMAND disable constraint FK_COURSE_DEMAND_STUDENT;
prompt Disabling foreign key constraints for SUBJECT_AREA...
alter table SUBJECT_AREA disable constraint FK_SUBJECT_AREA_DEPT;
prompt Disabling foreign key constraints for COURSE_OFFERING...
alter table COURSE_OFFERING disable constraint FK_COURSE_OFFERING_DEMAND_OFFR;
alter table COURSE_OFFERING disable constraint FK_COURSE_OFFERING_INSTR_OFFR;
alter table COURSE_OFFERING disable constraint FK_COURSE_OFFERING_SUBJ_AREA;
prompt Disabling foreign key constraints for COURSE_REQUEST...
alter table COURSE_REQUEST disable constraint FK_COURSE_REQUEST_DEMAND;
alter table COURSE_REQUEST disable constraint FK_COURSE_REQUEST_OFFERING;
prompt Disabling foreign key constraints for CLASS_WAITLIST...
alter table CLASS_WAITLIST disable constraint FK_CLASS_WAITLIST_CLASS;
alter table CLASS_WAITLIST disable constraint FK_CLASS_WAITLIST_REQUEST;
alter table CLASS_WAITLIST disable constraint FK_CLASS_WAITLIST_STUDENT;
prompt Disabling foreign key constraints for CONSTRAINT_INFO...
alter table CONSTRAINT_INFO disable constraint FK_CONSTRAINT_INFO_ASSIGNMENT;
alter table CONSTRAINT_INFO disable constraint FK_CONSTRAINT_INFO_SOLVER;
prompt Disabling foreign key constraints for COURSE_CREDIT_UNIT_CONFIG...
alter table COURSE_CREDIT_UNIT_CONFIG disable constraint FK_CRS_CRDT_UNIT_CFG_CRDT_TYPE;
alter table COURSE_CREDIT_UNIT_CONFIG disable constraint FK_CRS_CRDT_UNIT_CFG_IO_OWN;
alter table COURSE_CREDIT_UNIT_CONFIG disable constraint FK_CRS_CRDT_UNIT_CFG_OWNER;
prompt Disabling foreign key constraints for COURSE_RESERVATION...
alter table COURSE_RESERVATION disable constraint FK_COURSE_RESERV_TYPE;
alter table COURSE_RESERVATION disable constraint FK_COURSE_RESV_CRS_OFFR;
prompt Disabling foreign key constraints for COURSE_SUBPART_CREDIT...
alter table COURSE_SUBPART_CREDIT disable constraint FK_SUBPART_CRED_CRS;
prompt Disabling foreign key constraints for CURRICULUM...
alter table CURRICULUM disable constraint FK_CURRICULUM_ACAD_AREA;
alter table CURRICULUM disable constraint FK_CURRICULUM_DEPT;
prompt Disabling foreign key constraints for CURRICULUM_COURSE...
alter table CURRICULUM_COURSE disable constraint FK_CURRICULUM_COURSE_CLASF;
alter table CURRICULUM_COURSE disable constraint FK_CURRICULUM_COURSE_COURSE;
prompt Disabling foreign key constraints for CURRICULUM_GROUP...
alter table CURRICULUM_GROUP disable constraint FK_CURRICULUM_GROUP_CURRICULUM;
prompt Disabling foreign key constraints for CURRICULUM_COURSE_GROUP...
alter table CURRICULUM_COURSE_GROUP disable constraint FK_CUR_COURSE_GROUP_COURSE;
alter table CURRICULUM_COURSE_GROUP disable constraint FK_CUR_COURSE_GROUP_GROUP;
prompt Disabling foreign key constraints for POS_MAJOR...
alter table POS_MAJOR disable constraint FK_POS_MAJOR_SESSION;
prompt Disabling foreign key constraints for CURRICULUM_MAJOR...
alter table CURRICULUM_MAJOR disable constraint FK_CURRICULUM_MAJOR_CURRICULUM;
alter table CURRICULUM_MAJOR disable constraint FK_CURRICULUM_MAJOR_MAJOR;
prompt Disabling foreign key constraints for CURRICULUM_RULE...
alter table CURRICULUM_RULE disable constraint FK_CUR_RULE_ACAD_AREA;
alter table CURRICULUM_RULE disable constraint FK_CUR_RULE_ACAD_CLASF;
alter table CURRICULUM_RULE disable constraint FK_CUR_RULE_MAJOR;
prompt Disabling foreign key constraints for DATE_PATTERN_DEPT...
alter table DATE_PATTERN_DEPT disable constraint FK_DATE_PATTERN_DEPT_DATE;
alter table DATE_PATTERN_DEPT disable constraint FK_DATE_PATTERN_DEPT_DEPT;
prompt Disabling foreign key constraints for DEPT_TO_TT_MGR...
alter table DEPT_TO_TT_MGR disable constraint FK_DEPT_TO_TT_MGR_DEPT;
alter table DEPT_TO_TT_MGR disable constraint FK_DEPT_TO_TT_MGR_MGR;
prompt Disabling foreign key constraints for DESIGNATOR...
alter table DESIGNATOR disable constraint FK_DESIGNATOR_INSTRUCTOR;
alter table DESIGNATOR disable constraint FK_DESIGNATOR_SUBJ_AREA;
prompt Disabling foreign key constraints for DISTRIBUTION_PREF...
alter table DISTRIBUTION_PREF disable constraint FK_DISTRIBUTION_PREF_DIST_TYPE;
alter table DISTRIBUTION_PREF disable constraint FK_DISTRIBUTION_PREF_LEVEL;
prompt Disabling foreign key constraints for DISTRIBUTION_OBJECT...
alter table DISTRIBUTION_OBJECT disable constraint FK_DISTRIBUTION_OBJECT_PREF;
prompt Disabling foreign key constraints for DIST_TYPE_DEPT...
alter table DIST_TYPE_DEPT disable constraint FK_DIST_TYPE_DEPT_DEPT;
alter table DIST_TYPE_DEPT disable constraint FK_DIST_TYPE_DEPT_TYPE;
prompt Disabling foreign key constraints for EXAM_PERIOD...
alter table EXAM_PERIOD disable constraint FK_EXAM_PERIOD_PREF;
alter table EXAM_PERIOD disable constraint FK_EXAM_PERIOD_SESSION;
prompt Disabling foreign key constraints for EXAM...
alter table EXAM disable constraint FK_EXAM_PERIOD;
alter table EXAM disable constraint FK_EXAM_SESSION;
prompt Disabling foreign key constraints for EVENT...
alter table EVENT disable constraint FK_EVENT_CLASS;
alter table EVENT disable constraint FK_EVENT_EXAM;
alter table EVENT disable constraint FK_EVENT_MAIN_CONTACT;
alter table EVENT disable constraint FK_EVENT_SPONSOR_ORG;
prompt Disabling foreign key constraints for EVENT_JOIN_EVENT_CONTACT...
alter table EVENT_JOIN_EVENT_CONTACT disable constraint FK_EVENT_CONTACT_JOIN;
alter table EVENT_JOIN_EVENT_CONTACT disable constraint FK_EVENT_ID_JOIN;
prompt Disabling foreign key constraints for EVENT_NOTE...
alter table EVENT_NOTE disable constraint FK_EVENT_NOTE_EVENT;
prompt Disabling foreign key constraints for EXAM_INSTRUCTOR...
alter table EXAM_INSTRUCTOR disable constraint FK_EXAM_INSTRUCTOR_EXAM;
alter table EXAM_INSTRUCTOR disable constraint FK_EXAM_INSTRUCTOR_INSTRUCTOR;
prompt Disabling foreign key constraints for EXAM_LOCATION_PREF...
alter table EXAM_LOCATION_PREF disable constraint FK_EXAM_LOCATION_PREF_PERIOD;
alter table EXAM_LOCATION_PREF disable constraint FK_EXAM_LOCATION_PREF_PREF;
prompt Disabling foreign key constraints for EXAM_OWNER...
alter table EXAM_OWNER disable constraint FK_EXAM_OWNER_COURSE;
alter table EXAM_OWNER disable constraint FK_EXAM_OWNER_EXAM;
prompt Disabling foreign key constraints for EXAM_PERIOD_PREF...
alter table EXAM_PERIOD_PREF disable constraint FK_EXAM_PERIOD_PREF_PERIOD;
alter table EXAM_PERIOD_PREF disable constraint FK_EXAM_PERIOD_PREF_PREF;
prompt Disabling foreign key constraints for EXAM_ROOM_ASSIGNMENT...
alter table EXAM_ROOM_ASSIGNMENT disable constraint FK_EXAM_ROOM_EXAM;
prompt Disabling foreign key constraints for EXTERNAL_ROOM...
alter table EXTERNAL_ROOM disable constraint FK_EXTERNAL_ROOM_TYPE;
alter table EXTERNAL_ROOM disable constraint FK_EXT_ROOM_BUILDING;
prompt Disabling foreign key constraints for EXTERNAL_ROOM_DEPARTMENT...
alter table EXTERNAL_ROOM_DEPARTMENT disable constraint FK_EXT_DEPT_ROOM;
prompt Disabling foreign key constraints for EXTERNAL_ROOM_FEATURE...
alter table EXTERNAL_ROOM_FEATURE disable constraint FK_EXT_FTR_ROOM;
prompt Disabling foreign key constraints for HISTORY...
alter table HISTORY disable constraint FK_HISTORY_SESSION;
prompt Disabling foreign key constraints for INDIVIDUAL_RESERVATION...
alter table INDIVIDUAL_RESERVATION disable constraint FK_INDIVIDUAL_RESV_TYPE;
prompt Disabling foreign key constraints for JENRL...
alter table JENRL disable constraint FK_JENRL_CLASS1;
alter table JENRL disable constraint FK_JENRL_CLASS2;
alter table JENRL disable constraint FK_JENRL_SOLUTION;
prompt Disabling foreign key constraints for LASTLIKE_COURSE_DEMAND...
alter table LASTLIKE_COURSE_DEMAND disable constraint FK_LL_COURSE_DEMAND_STUDENT;
alter table LASTLIKE_COURSE_DEMAND disable constraint FK_LL_COURSE_DEMAND_SUBJAREA;
prompt Disabling foreign key constraints for MANAGER_SETTINGS...
alter table MANAGER_SETTINGS disable constraint FK_MANAGER_SETTINGS_KEY;
alter table MANAGER_SETTINGS disable constraint FK_MANAGER_SETTINGS_USER;
prompt Disabling foreign key constraints for MEETING...
alter table MEETING disable constraint FK_MEETING_EVENT;
prompt Disabling foreign key constraints for NON_UNIVERSITY_LOCATION...
alter table NON_UNIVERSITY_LOCATION disable constraint FK_LOCATION_TYPE;
alter table NON_UNIVERSITY_LOCATION disable constraint FK_NON_UNIV_LOC_SESSION;
prompt Disabling foreign key constraints for OFFR_GROUP...
alter table OFFR_GROUP disable constraint FK_OFFR_GROUP_DEPT;
alter table OFFR_GROUP disable constraint FK_OFFR_GROUP_SESSION;
prompt Disabling foreign key constraints for OFFR_GROUP_OFFERING...
alter table OFFR_GROUP_OFFERING disable constraint FK_OFFR_GROUP_INSTR_OFFR;
alter table OFFR_GROUP_OFFERING disable constraint FK_OFFR_GROUP_OFFR_OFFR_GRP;
prompt Disabling foreign key constraints for POSITION_CODE_TO_TYPE...
alter table POSITION_CODE_TO_TYPE disable constraint FK_POS_CODE_TO_TYPE_CODE_TYPE;
prompt Disabling foreign key constraints for POS_ACAD_AREA_MAJOR...
alter table POS_ACAD_AREA_MAJOR disable constraint FK_POS_ACAD_AREA_MAJOR_AREA;
alter table POS_ACAD_AREA_MAJOR disable constraint FK_POS_ACAD_AREA_MAJOR_MAJOR;
prompt Disabling foreign key constraints for POS_MINOR...
alter table POS_MINOR disable constraint FK_POS_MINOR_SESSION;
prompt Disabling foreign key constraints for POS_ACAD_AREA_MINOR...
alter table POS_ACAD_AREA_MINOR disable constraint FK_POS_ACAD_AREA_MINOR_AREA;
alter table POS_ACAD_AREA_MINOR disable constraint FK_POS_ACAD_AREA_MINOR_MINOR;
prompt Disabling foreign key constraints for POS_RESERVATION...
alter table POS_RESERVATION disable constraint FK_POS_RESV_ACAD_CLASS;
alter table POS_RESERVATION disable constraint FK_POS_RESV_MAJOR;
alter table POS_RESERVATION disable constraint FK_POS_RESV_TYPE;
prompt Disabling foreign key constraints for RELATED_COURSE_INFO...
alter table RELATED_COURSE_INFO disable constraint FK_EVENT_OWNER_COURSE;
alter table RELATED_COURSE_INFO disable constraint FK_EVENT_OWNER_EVENT;
prompt Disabling foreign key constraints for ROOM...
alter table ROOM disable constraint FK_ROOM_BUILDING;
alter table ROOM disable constraint FK_ROOM_SESSION;
alter table ROOM disable constraint FK_ROOM_TYPE;
prompt Disabling foreign key constraints for ROOM_DEPT...
alter table ROOM_DEPT disable constraint FK_ROOM_DEPT_DEPT;
prompt Disabling foreign key constraints for ROOM_FEATURE...
alter table ROOM_FEATURE disable constraint FK_ROOM_FEATURE_DEPT;
prompt Disabling foreign key constraints for ROOM_FEATURE_PREF...
alter table ROOM_FEATURE_PREF disable constraint FK_ROOM_FEAT_PREF_LEVEL;
alter table ROOM_FEATURE_PREF disable constraint FK_ROOM_FEAT_PREF_ROOM_FEAT;
prompt Disabling foreign key constraints for ROOM_GROUP...
alter table ROOM_GROUP disable constraint FK_ROOM_GROUP_DEPT;
alter table ROOM_GROUP disable constraint FK_ROOM_GROUP_SESSION;
prompt Disabling foreign key constraints for ROOM_GROUP_PREF...
alter table ROOM_GROUP_PREF disable constraint FK_ROOM_GROUP_PREF_LEVEL;
alter table ROOM_GROUP_PREF disable constraint FK_ROOM_GROUP_PREF_ROOM_GRP;
prompt Disabling foreign key constraints for ROOM_GROUP_ROOM...
alter table ROOM_GROUP_ROOM disable constraint FK_ROOM_GROUP_ROOM_ROOM_GRP;
prompt Disabling foreign key constraints for ROOM_JOIN_ROOM_FEATURE...
alter table ROOM_JOIN_ROOM_FEATURE disable constraint FK_ROOM_JOIN_ROOM_FEAT_RM_FEAT;
prompt Disabling foreign key constraints for ROOM_PREF...
alter table ROOM_PREF disable constraint FK_ROOM_PREF_LEVEL;
prompt Disabling foreign key constraints for ROOM_TYPE_OPTION...
alter table ROOM_TYPE_OPTION disable constraint FK_RTYPE_OPTION_SESSION;
alter table ROOM_TYPE_OPTION disable constraint FK_RTYPE_OPTION_TYPE;
prompt Disabling foreign key constraints for SECTIONING_INFO...
alter table SECTIONING_INFO disable constraint FK_SECTIONING_INFO_CLASS;
prompt Disabling foreign key constraints for SOLVER_GR_TO_TT_MGR...
alter table SOLVER_GR_TO_TT_MGR disable constraint FK_SOLVER_GR_TO_TT_MGR_SOLVGRP;
alter table SOLVER_GR_TO_TT_MGR disable constraint FK_SOLVER_GR_TO_TT_MGR_TT_MGR;
prompt Disabling foreign key constraints for SOLVER_PARAMETER_DEF...
alter table SOLVER_PARAMETER_DEF disable constraint FK_SOLV_PARAM_DEF_SOLV_PAR_GRP;
prompt Disabling foreign key constraints for SOLVER_PARAMETER...
alter table SOLVER_PARAMETER disable constraint FK_SOLVER_PARAM_DEF;
alter table SOLVER_PARAMETER disable constraint FK_SOLVER_PARAM_PREDEF_STG;
alter table SOLVER_PARAMETER disable constraint FK_SOLVER_PARAM_SOLUTION;
prompt Disabling foreign key constraints for STUDENT_ACAD_AREA...
alter table STUDENT_ACAD_AREA disable constraint FK_STUDENT_ACAD_AREA_AREA;
alter table STUDENT_ACAD_AREA disable constraint FK_STUDENT_ACAD_AREA_CLASF;
alter table STUDENT_ACAD_AREA disable constraint FK_STUDENT_ACAD_AREA_STUDENT;
prompt Disabling foreign key constraints for STUDENT_ACCOMODATION...
alter table STUDENT_ACCOMODATION disable constraint FK_STUDENT_ACCOM_SESSION;
prompt Disabling foreign key constraints for STUDENT_CLASS_ENRL...
alter table STUDENT_CLASS_ENRL disable constraint FK_STUDENT_CLASS_ENRL_CLASS;
alter table STUDENT_CLASS_ENRL disable constraint FK_STUDENT_CLASS_ENRL_COURSE;
alter table STUDENT_CLASS_ENRL disable constraint FK_STUDENT_CLASS_ENRL_REQUEST;
alter table STUDENT_CLASS_ENRL disable constraint FK_STUDENT_CLASS_ENRL_STUDENT;
prompt Disabling foreign key constraints for STUDENT_ENRL...
alter table STUDENT_ENRL disable constraint FK_STUDENT_ENRL_CLASS;
alter table STUDENT_ENRL disable constraint FK_STUDENT_ENRL_SOLUTION;
prompt Disabling foreign key constraints for STUDENT_ENRL_MSG...
alter table STUDENT_ENRL_MSG disable constraint FK_STUDENT_ENRL_MSG_DEMAND;
prompt Disabling foreign key constraints for STUDENT_GROUP...
alter table STUDENT_GROUP disable constraint FK_STUDENT_GROUP_SESSION;
prompt Disabling foreign key constraints for STUDENT_GROUP_RESERVATION...
alter table STUDENT_GROUP_RESERVATION disable constraint FK_STU_GRP_RESV_RESERV_TYPE;
alter table STUDENT_GROUP_RESERVATION disable constraint FK_STU_GRP_RESV_STU_GRP;
prompt Disabling foreign key constraints for STUDENT_MAJOR...
alter table STUDENT_MAJOR disable constraint FK_STUDENT_MAJOR_MAJOR;
alter table STUDENT_MAJOR disable constraint FK_STUDENT_MAJOR_STUDENT;
prompt Disabling foreign key constraints for STUDENT_MINOR...
alter table STUDENT_MINOR disable constraint FK_STUDENT_MINOR_MINOR;
alter table STUDENT_MINOR disable constraint FK_STUDENT_MINOR_STUDENT;
prompt Disabling foreign key constraints for STUDENT_TO_ACOMODATION...
alter table STUDENT_TO_ACOMODATION disable constraint FK_STUDENT_ACOMODATION_ACCOM;
alter table STUDENT_TO_ACOMODATION disable constraint FK_STUDENT_ACOMODATION_STUDENT;
prompt Disabling foreign key constraints for STUDENT_TO_GROUP...
alter table STUDENT_TO_GROUP disable constraint FK_STUDENT_GROUP_GROUP;
alter table STUDENT_TO_GROUP disable constraint FK_STUDENT_GROUP_STUDENT;
prompt Disabling foreign key constraints for TIME_PATTERN_DAYS...
alter table TIME_PATTERN_DAYS disable constraint FK_TIME_PATTERN_DAYS_TIME_PATT;
prompt Disabling foreign key constraints for TIME_PATTERN_DEPT...
alter table TIME_PATTERN_DEPT disable constraint FK_TIME_PATTERN_DEPT_DEPT;
alter table TIME_PATTERN_DEPT disable constraint FK_TIME_PATTERN_DEPT_PATTERN;
prompt Disabling foreign key constraints for TIME_PATTERN_TIME...
alter table TIME_PATTERN_TIME disable constraint FK_TIME_PATTERN_TIME;
prompt Disabling foreign key constraints for TIME_PREF...
alter table TIME_PREF disable constraint FK_TIME_PREF_PREF_LEVEL;
alter table TIME_PREF disable constraint FK_TIME_PREF_TIME_PTRN;
prompt Disabling foreign key constraints for TMTBL_MGR_TO_ROLES...
alter table TMTBL_MGR_TO_ROLES disable constraint FK_TMTBL_MGR_TO_ROLES_MANAGER;
alter table TMTBL_MGR_TO_ROLES disable constraint FK_TMTBL_MGR_TO_ROLES_ROLE;
prompt Disabling foreign key constraints for WAITLIST...
alter table WAITLIST disable constraint FK_WAITLIST_COURSE_OFFERING;
alter table WAITLIST disable constraint FK_WAITLIST_STUDENT;
prompt Disabling foreign key constraints for XCONFLICT_EXAM...
alter table XCONFLICT_EXAM disable constraint FK_XCONFLICT_EX_CONF;
alter table XCONFLICT_EXAM disable constraint FK_XCONFLICT_EX_EXAM;
prompt Disabling foreign key constraints for XCONFLICT_INSTRUCTOR...
alter table XCONFLICT_INSTRUCTOR disable constraint FK_XCONFLICT_IN_CONF;
alter table XCONFLICT_INSTRUCTOR disable constraint FK_XCONFLICT_IN_INSTRUCTOR;
prompt Disabling foreign key constraints for XCONFLICT_STUDENT...
alter table XCONFLICT_STUDENT disable constraint FK_XCONFLICT_ST_CONF;
alter table XCONFLICT_STUDENT disable constraint FK_XCONFLICT_ST_STUDENT;
prompt Loading DATE_PATTERN...
prompt Table is empty
prompt Loading DEPT_STATUS_TYPE...
insert into DEPT_STATUS_TYPE (uniqueid, reference, label, status, apply, ord)
values (265, 'initial', 'Initial Data Load', 0, 1, 0);
insert into DEPT_STATUS_TYPE (uniqueid, reference, label, status, apply, ord)
values (266, 'input', 'Input Data Entry', 1721, 1, 1);
insert into DEPT_STATUS_TYPE (uniqueid, reference, label, status, apply, ord)
values (267, 'timetabling', 'Timetabling', 4025, 1, 2);
insert into DEPT_STATUS_TYPE (uniqueid, reference, label, status, apply, ord)
values (268, 'publish', 'Timetable Published', 61961, 1, 4);
insert into DEPT_STATUS_TYPE (uniqueid, reference, label, status, apply, ord)
values (269, 'finished', 'Session Finished', 521, 1, 5);
insert into DEPT_STATUS_TYPE (uniqueid, reference, label, status, apply, ord)
values (270, 'dept_input', 'External Mgr. Input Data Entry', 135, 2, 6);
insert into DEPT_STATUS_TYPE (uniqueid, reference, label, status, apply, ord)
values (271, 'dept_timetabling', 'External Mgr. Timetabling', 423, 2, 7);
insert into DEPT_STATUS_TYPE (uniqueid, reference, label, status, apply, ord)
values (272, 'dept_publish', 'External Mgr. Timetable Published', 1, 2, 9);
insert into DEPT_STATUS_TYPE (uniqueid, reference, label, status, apply, ord)
values (385, 'dept_readonly_ni', 'External Mgr. Timetabling (No Instructor Assignments)', 391, 2, 8);
insert into DEPT_STATUS_TYPE (uniqueid, reference, label, status, apply, ord)
values (325, 'dept_readonly', 'Department Read Only', 9, 2, 10);
insert into DEPT_STATUS_TYPE (uniqueid, reference, label, status, apply, ord)
values (326, 'dept_edit', 'Department Allow Edit', 441, 2, 11);
insert into DEPT_STATUS_TYPE (uniqueid, reference, label, status, apply, ord)
values (414, 'exams', 'Examination Timetabling', 3593, 1, 3);
commit;
prompt 12 records loaded
prompt Loading SESSIONS...
insert into SESSIONS (academic_initiative, session_begin_date_time, classes_end_date_time, session_end_date_time, uniqueid, holidays, def_datepatt_id, status_type, last_modified_time, academic_year, academic_term, exam_begin_date, event_begin_date, event_end_date)
values ('blank', to_date('06-09-2010', 'dd-mm-yyyy'), to_date('05-12-2010', 'dd-mm-yyyy'), to_date('12-12-2010', 'dd-mm-yyyy'), 239259, '00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000', null, 266, null, '2010', 'Fall', to_date('06-12-2010', 'dd-mm-yyyy'), to_date('01-09-2010', 'dd-mm-yyyy'), to_date('31-12-2010', 'dd-mm-yyyy'));
commit;
prompt 1 records loaded
prompt Loading ACADEMIC_AREA...
prompt Table is empty
prompt Loading ACADEMIC_CLASSIFICATION...
prompt Table is empty
prompt Loading RESERVATION_TYPE...
insert into RESERVATION_TYPE (uniqueid, reference, label)
values (288, 'perm', 'Permanent');
insert into RESERVATION_TYPE (uniqueid, reference, label)
values (289, 'temp', 'Temporary');
insert into RESERVATION_TYPE (uniqueid, reference, label)
values (290, 'info', null);
commit;
prompt 3 records loaded
prompt Loading ACAD_AREA_RESERVATION...
prompt Table is empty
prompt Loading APPLICATION_CONFIG...
insert into APPLICATION_CONFIG (name, value, description)
values ('tmtbl.system_message', 'Welcome to Woebegon College test suite.', 'Message displayed to users when they first log in to Timetabling');
insert into APPLICATION_CONFIG (name, value, description)
values ('tmtbl.access_level', 'all', 'Access Levels: all | {dept code}(:{dept code})*');
insert into APPLICATION_CONFIG (name, value, description)
values ('tmtbl.db.version', '59', 'Timetabling database version (please do not change -- this key is used by automatic database update)');
commit;
prompt 3 records loaded
prompt Loading OFFR_CONSENT_TYPE...
insert into OFFR_CONSENT_TYPE (uniqueid, reference, label)
values (225, 'instructor', 'Consent of Instructor');
insert into OFFR_CONSENT_TYPE (uniqueid, reference, label)
values (226, 'department', 'Consent of Department');
commit;
prompt 2 records loaded
prompt Loading INSTRUCTIONAL_OFFERING...
prompt Table is empty
prompt Loading INSTR_OFFERING_CONFIG...
prompt Table is empty
prompt Loading ITYPE_DESC...
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (10, 'Lec  ', 'Lecture', 'lec', 1, null, 1);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (11, 'Lec 1', 'Lecture 1', 'lec', 0, 10, 1);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (12, 'Lec 2', 'Lecture 2', 'lec', 0, 10, 1);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (13, 'Lec 3', 'Lecture 3', 'lec', 0, 10, 1);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (14, 'Lec 4', 'Lecture 4', 'lec', 0, 10, 1);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (15, 'Lec 5', 'Lecture 5', 'lec', 0, 10, 1);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (16, 'Lec 6', 'Lecture 6', 'lec', 0, 10, 1);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (17, 'Lec 7', 'Lecture 7', 'lec', 0, 10, 1);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (18, 'Lec 8', 'Lecture 8', 'lec', 0, 10, 1);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (19, 'Lec 9', 'Lecture 9', 'lec', 0, 10, 1);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (20, 'Rec  ', 'Recitation', 'rec', 1, null, 1);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (21, 'Rec 1', 'Recitation 1', 'rec', 0, 20, 1);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (22, 'Rec 2', 'Recitation 2', 'rec', 0, 20, 1);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (23, 'Rec 3', 'Recitation 3', 'rec', 0, 20, 1);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (24, 'Rec 4', 'Recitation 4', 'rec', 0, 20, 1);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (25, 'Prsn ', 'Presentation', 'prsn', 1, null, 1);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (26, 'Prsn1', 'Presentation 1', 'prsn', 0, 25, 1);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (27, 'Prsn2', 'Presentation 2', 'prsn', 0, 25, 1);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (28, 'Prsn3', 'Presentation 3 ', 'prsn', 0, 25, 1);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (29, 'Prsn4', 'Presentation 4', 'prsn', 0, 25, 1);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (30, 'Lab  ', 'Laboratory', 'lab', 1, null, 1);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (31, 'Lab 1', 'Laboratory 1', 'lab', 0, 30, 1);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (32, 'Lab 2', 'Laboratory 2', 'lab', 0, 30, 1);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (33, 'Lab 3', 'Laboratory 3', 'lab', 0, 30, 1);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (34, 'Lab 4', 'Laboratory 4', 'lab', 0, 30, 1);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (35, 'LabP ', 'Laboratory Preparation', 'labP', 1, null, 1);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (36, 'LabP1', 'Laboratory Preparation 1', 'labP', 0, 35, 1);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (37, 'LabP2', 'Laboratory Preparation 2', 'labP', 0, 35, 1);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (38, 'LabP3', 'Laboratory Preparation 3', 'labP', 0, 35, 1);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (39, 'LabP4', 'Laboratory Preparation 4', 'labP', 0, 35, 1);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (40, 'Stdo ', 'Studio', 'stdo', 1, null, 1);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (41, 'Stdo1', 'Studio 1', 'stdo', 0, 40, 1);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (42, 'Stdo2', 'Studio 2', 'stdo', 0, 40, 1);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (43, 'Stdo3', 'Studio 3', 'stdo', 0, 40, 1);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (44, 'Stdo4', 'Studio 4', 'stdo', 0, 40, 1);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (45, 'Dist ', 'Distance Learning', 'dist', 1, null, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (46, 'Dist1', 'Distance Learning 1', 'dist', 0, 45, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (47, 'Dist2', 'Distance Learning 2', 'dist', 0, 45, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (48, 'Dist3', 'Distance Learning 3', 'dist', 0, 45, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (49, 'Dist4', 'Distance Learning 4', 'dist', 0, 45, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (50, 'Clin ', 'Clinic', 'clin', 1, null, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (51, 'Clin1', 'Clinic 1', 'clin', 0, 50, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (52, 'Clin2', 'Clinic 2', 'clin', 0, 50, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (53, 'Clin3', 'Clinic 3', 'clin', 0, 50, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (54, 'Clin4', 'Clinic 4', 'clin', 0, 50, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (55, 'Clin5', 'Clinic 5', 'clin', 0, 50, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (56, 'Clin6', 'Clinic 6', 'clin', 0, 50, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (57, 'Clin7', 'Clinic 7', 'clin', 0, 50, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (58, 'Clin8', 'Clinic 8', 'clin', 0, 50, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (59, 'Clin9', 'Clinic 9', 'clin', 0, 50, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (60, 'Expr ', 'Experiential', 'expr', 1, null, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (61, 'Expr1', 'Experiential 1', 'expr', 0, 60, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (62, 'Expr2', 'Experiential 2', 'expr', 0, 60, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (63, 'Expr3', 'Experiential 3', 'expr', 0, 60, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (64, 'Expr4', 'Experiential 4', 'expr', 0, 60, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (65, 'Expr5', 'Experiential 5', 'expr', 0, 60, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (66, 'Expr6', 'Experiential 6', 'expr', 0, 60, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (67, 'Expr7', 'Experiential 7', 'expr', 0, 60, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (68, 'Expr8', 'Experiential 8', 'expr', 0, 60, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (69, 'Expr9', 'Experiential 9', 'expr', 0, 60, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (70, 'Res  ', 'Research', 'res', 1, null, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (71, 'Res 1', 'Research 1', 'res', 0, 70, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (72, 'Res 2', 'Research 2', 'res', 0, 70, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (73, 'Res 3', 'Research 3', 'res', 0, 70, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (74, 'Res 4', 'Research 4', 'res', 0, 70, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (75, 'Res 5', 'Research 5', 'res', 0, 70, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (76, 'Res 6', 'Research 6', 'res', 0, 70, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (77, 'Res 7', 'Research 7', 'res', 0, 70, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (78, 'Res 8', 'Research 8', 'res', 0, 70, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (79, 'Res 9', 'Research 9', 'res', 0, 70, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (80, 'Ind  ', 'Individual Study', 'ind', 1, null, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (81, 'Ind 1', 'Individual Study 1', 'ind', 0, 80, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (82, 'Ind 2', 'Individual Study 2', 'ind', 0, 80, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (83, 'Ind 3', 'Individual Study 3', 'ind', 0, 80, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (84, 'Ind 4', 'Individual Study 4', 'ind', 0, 80, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (85, 'Ind 5', 'Individual Study 5', 'ind', 0, 80, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (86, 'Ind 6', 'Individual Study 6', 'ind', 0, 80, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (87, 'Ind 7', 'Individual Study 7', 'ind', 0, 80, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (88, 'Ind 8', 'Individual Study 8', 'ind', 0, 80, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (89, 'Ind 9', 'Individual Study 9', 'ind', 0, 80, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (90, 'Pso  ', 'Practice Study Observation', 'pso', 1, null, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (91, 'Pso 1', 'Practice Study Observation 1', 'pso', 0, 90, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (92, 'Pso 2', 'Practice Study Observation 2', 'pso', 0, 90, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (93, 'Pso 3', 'Practice Study Observation 3', 'pso', 0, 90, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (94, 'Pso 4', 'Practice Study Observation 4', 'pso', 0, 90, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (95, 'Pso 5', 'Practice Study Observation 5', 'pso', 0, 90, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (96, 'Pso 6', 'Practice Study Observation 6', 'pso', 0, 90, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (97, 'Pso 7', 'Practice Study Observation 7', 'pso', 0, 90, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (98, 'Pso 8', 'Practice Study Observation 8', 'pso', 0, 90, 0);
insert into ITYPE_DESC (itype, abbv, description, sis_ref, basic, parent, organized)
values (99, 'Pso 9', 'Practice Study Observation 9', 'pso', 0, 90, 0);
commit;
prompt 90 records loaded
prompt Loading SCHEDULING_SUBPART...
prompt Table is empty
prompt Loading CLASS_...
prompt Table is empty
prompt Loading SOLVER_GROUP...
prompt Table is empty
prompt Loading SOLUTION...
prompt Table is empty
prompt Loading TIME_PATTERN...
prompt Table is empty
prompt Loading ASSIGNMENT...
prompt Table is empty
prompt Loading POSITION_TYPE...
insert into POSITION_TYPE (uniqueid, reference, label, sort_order)
values (18, 'ADMIN_STAFF', 'Administrative/Professional Staff', 1300);
insert into POSITION_TYPE (uniqueid, reference, label, sort_order)
values (19, 'UNDRGRD_TEACH_ASST', 'Undergrad Teaching Assistant', 1400);
insert into POSITION_TYPE (uniqueid, reference, label, sort_order)
values (1, 'PROF', 'Professor', 100);
insert into POSITION_TYPE (uniqueid, reference, label, sort_order)
values (2, 'ASSOC_PROF', 'Associate Professor', 200);
insert into POSITION_TYPE (uniqueid, reference, label, sort_order)
values (3, 'ASST_PROF', 'Assistant Professor', 300);
insert into POSITION_TYPE (uniqueid, reference, label, sort_order)
values (4, 'INSTRUCTOR', 'Instructor', 800);
insert into POSITION_TYPE (uniqueid, reference, label, sort_order)
values (5, 'CLIN_PROF', 'Clinical / Professional', 500);
insert into POSITION_TYPE (uniqueid, reference, label, sort_order)
values (6, 'CONT_LEC', 'Continuing Lecturer', 600);
insert into POSITION_TYPE (uniqueid, reference, label, sort_order)
values (7, 'LTD_LEC', 'Limited-Term Lecturer', 700);
insert into POSITION_TYPE (uniqueid, reference, label, sort_order)
values (8, 'VISIT_FAC', 'Visiting Faculty', 400);
insert into POSITION_TYPE (uniqueid, reference, label, sort_order)
values (9, 'POST_DOC', 'Post Doctoral', 1500);
insert into POSITION_TYPE (uniqueid, reference, label, sort_order)
values (10, 'ADJUNCT_FAC', 'Adjunct Faculty', 1000);
insert into POSITION_TYPE (uniqueid, reference, label, sort_order)
values (11, 'GRAD_TEACH_ASST', 'Graduate Teaching Assistant', 1200);
insert into POSITION_TYPE (uniqueid, reference, label, sort_order)
values (12, 'GRAD_LEC', 'Graduate Lecturer', 1100);
insert into POSITION_TYPE (uniqueid, reference, label, sort_order)
values (13, 'CLERICAL_STAFF', 'Clerical Staff', 1600);
insert into POSITION_TYPE (uniqueid, reference, label, sort_order)
values (14, 'SERVICE_STAFF', 'Service Staff', 1700);
insert into POSITION_TYPE (uniqueid, reference, label, sort_order)
values (15, 'FELLOWSHIP', 'Fellowship', 1800);
insert into POSITION_TYPE (uniqueid, reference, label, sort_order)
values (16, 'EMERITUS', 'Emeritus Faculty', 900);
insert into POSITION_TYPE (uniqueid, reference, label, sort_order)
values (17, 'OTHER', 'Other', 2000);
commit;
prompt 19 records loaded
prompt Loading DEPARTMENT...
prompt Table is empty
prompt Loading DEPARTMENTAL_INSTRUCTOR...
prompt Table is empty
prompt Loading ASSIGNED_INSTRUCTORS...
prompt Table is empty
prompt Loading ASSIGNED_ROOMS...
prompt Table is empty
prompt Loading BUILDING...
prompt Table is empty
prompt Loading PREFERENCE_LEVEL...
insert into PREFERENCE_LEVEL (pref_id, pref_prolog, pref_name, uniqueid)
values (1, 'R', 'Required', 1);
insert into PREFERENCE_LEVEL (pref_id, pref_prolog, pref_name, uniqueid)
values (2, '-2', 'Strongly Preferred', 2);
insert into PREFERENCE_LEVEL (pref_id, pref_prolog, pref_name, uniqueid)
values (3, '-1', 'Preferred', 3);
insert into PREFERENCE_LEVEL (pref_id, pref_prolog, pref_name, uniqueid)
values (4, '0', 'Neutral', 4);
insert into PREFERENCE_LEVEL (pref_id, pref_prolog, pref_name, uniqueid)
values (5, '1', 'Discouraged', 5);
insert into PREFERENCE_LEVEL (pref_id, pref_prolog, pref_name, uniqueid)
values (6, '2', 'Strongly Discouraged', 6);
insert into PREFERENCE_LEVEL (pref_id, pref_prolog, pref_name, uniqueid)
values (7, 'P', 'Prohibited', 7);
commit;
prompt 7 records loaded
prompt Loading BUILDING_PREF...
prompt Table is empty
prompt Loading CLASS_INSTRUCTOR...
prompt Table is empty
prompt Loading STUDENT_STATUS_TYPE...
prompt Table is empty
prompt Loading STUDENT...
prompt Table is empty
prompt Loading FREE_TIME...
prompt Table is empty
prompt Loading COURSE_DEMAND...
prompt Table is empty
prompt Loading SUBJECT_AREA...
prompt Table is empty
prompt Loading COURSE_OFFERING...
prompt Table is empty
prompt Loading COURSE_REQUEST...
prompt Table is empty
prompt Loading CLASS_WAITLIST...
prompt Table is empty
prompt Loading CONSTRAINT_INFO...
prompt Table is empty
prompt Loading COURSE_CATALOG...
prompt Table is empty
prompt Loading COURSE_CREDIT_TYPE...
insert into COURSE_CREDIT_TYPE (uniqueid, reference, label, abbreviation, legacy_crse_master_code)
values (238, 'collegiate', 'Collegiate Credit', null, ' ');
insert into COURSE_CREDIT_TYPE (uniqueid, reference, label, abbreviation, legacy_crse_master_code)
values (239, 'continuingEdUnits', 'Continuing Education Units', 'CEU', 'Q');
insert into COURSE_CREDIT_TYPE (uniqueid, reference, label, abbreviation, legacy_crse_master_code)
values (240, 'equivalent', 'Equivalent Credit', 'EQV', 'E');
insert into COURSE_CREDIT_TYPE (uniqueid, reference, label, abbreviation, legacy_crse_master_code)
values (241, 'mastersCredit', 'Masters Credit', 'MS', 'M');
insert into COURSE_CREDIT_TYPE (uniqueid, reference, label, abbreviation, legacy_crse_master_code)
values (242, 'phdThesisCredit', 'Phd Thesis Credit', 'PhD', 'T');
commit;
prompt 5 records loaded
prompt Loading COURSE_CREDIT_UNIT_CONFIG...
prompt Table is empty
prompt Loading COURSE_CREDIT_UNIT_TYPE...
insert into COURSE_CREDIT_UNIT_TYPE (uniqueid, reference, label, abbreviation)
values (248, 'semesterHours', 'Semester Hours', null);
commit;
prompt 1 records loaded
prompt Loading COURSE_RESERVATION...
prompt Table is empty
prompt Loading COURSE_SUBPART_CREDIT...
prompt Table is empty
prompt Loading CRSE_CREDIT_FORMAT...
insert into CRSE_CREDIT_FORMAT (uniqueid, reference, label, abbreviation)
values (243, 'arrangeHours', 'Arrange Hours', 'AH');
insert into CRSE_CREDIT_FORMAT (uniqueid, reference, label, abbreviation)
values (244, 'fixedUnit', 'Fixed Unit', null);
insert into CRSE_CREDIT_FORMAT (uniqueid, reference, label, abbreviation)
values (245, 'variableMinMax', 'Variable Min/Max', null);
insert into CRSE_CREDIT_FORMAT (uniqueid, reference, label, abbreviation)
values (246, 'variableRange', 'Variable Range', null);
commit;
prompt 4 records loaded
prompt Loading CURRICULUM...
prompt Table is empty
prompt Loading CURRICULUM_COURSE...
prompt Table is empty
prompt Loading CURRICULUM_GROUP...
prompt Table is empty
prompt Loading CURRICULUM_COURSE_GROUP...
prompt Table is empty
prompt Loading POS_MAJOR...
prompt Table is empty
prompt Loading CURRICULUM_MAJOR...
prompt Table is empty
prompt Loading CURRICULUM_RULE...
prompt Table is empty
prompt Loading DATE_PATTERN_DEPT...
prompt Table is empty
prompt Loading DEMAND_OFFR_TYPE...
prompt Table is empty
prompt Loading TIMETABLE_MANAGER...
insert into TIMETABLE_MANAGER (uniqueid, external_uid, first_name, middle_name, last_name, email_address, last_modified_time)
values (470, '1', 'Deafult', null, 'Admin', 'demo@unitime.org', null);
commit;
prompt 1 records loaded
prompt Loading DEPT_TO_TT_MGR...
prompt Table is empty
prompt Loading DESIGNATOR...
prompt Table is empty
prompt Loading DISTRIBUTION_TYPE...
insert into DISTRIBUTION_TYPE (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref)
values (205, 'BTB_DAY', 'Back-To-Back Day', '0', 26, 'P43210R', 'Classes must be offered on adjacent days and may be placed in different rooms.<BR>When prohibited or (strongly) discouraged: classes can not be taught on adjacent days. They also can not be taught on the same days. This means that there must be at least one day between these classes.', 'BTB Day', 0, 0);
insert into DISTRIBUTION_TYPE (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref)
values (206, 'MIN_GRUSE(10x1h)', 'Minimize Use Of 1h Groups', '0', 27, 'P43210R', 'Minimize number of groups of time that are used by the given classes. The time is spread into the following 10 groups of one hour: 7:30a-8:30a, 8:30a-9:30a, 9:30a-10:30a, ... 4:30p-5:30p.', 'Min 1h Groups', 0, 0);
insert into DISTRIBUTION_TYPE (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref)
values (207, 'MIN_GRUSE(5x2h)', 'Minimize Use Of 2h Groups', '0', 28, 'P43210R', 'Minimize number of groups of time that are used by the given classes. The time is spread into the following 5 groups of two hours: 7:30a-9:30a, 9:30a-11:30a, 11:30a-1:30p, 1:30p-3:30p, 3:30p-5:30p.', 'Min 2h Groups', 0, 0);
insert into DISTRIBUTION_TYPE (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref)
values (208, 'MIN_GRUSE(3x3h)', 'Minimize Use Of 3h Groups', '0', 29, 'P43210R', 'Minimize number of groups of time that are used by the given classes. The time is spread into the following 3 groups: 7:30a-10:30a, 10:30a-2:30p, 2:30p-5:30p.', 'Min 3h Groups', 0, 0);
insert into DISTRIBUTION_TYPE (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref)
values (209, 'MIN_GRUSE(2x5h)', 'Minimize Use Of 5h Groups', '0', 30, 'P43210R', 'Minimize number of groups of time that are used by the given classes. The time is spread into the following 2 groups: 7:30a-12:30a, 12:30a-5:30p.', 'Min 5h Groups', 0, 0);
insert into DISTRIBUTION_TYPE (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref)
values (161, 'SAME_STUDENTS', 'Same Students', '0', 20, '210R', 'Given classes are treated as they are attended by the same students, i.e., they cannot overlap in time and if they are back-to-back the assigned rooms cannot be too far (student limit is used).', 'Same Students', 0, 0);
insert into DISTRIBUTION_TYPE (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref)
values (162, 'SAME_INSTR', 'Same Instructor', '0', 21, '210R', 'Given classes are treated as they are taught by the same instructor, i.e., they cannot overlap in time and if they are back-to-back the assigned rooms cannot be too far (instructor limit is used).<BR>If the constraint is required and the classes are back-to-back, discouraged and strongly discouraged distances between assigned rooms are also considered.', 'Same Instr', 0, 0);
insert into DISTRIBUTION_TYPE (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref)
values (163, 'CAN_SHARE_ROOM', 'Can Share Room', '0', 22, '2R', 'Given classes can share the room (use the room in the same time) if the room is big enough.', 'Share Room', 0, 0);
insert into DISTRIBUTION_TYPE (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref)
values (164, 'SPREAD', 'Spread In Time', '0', 23, '2R', 'Given classes have to be spread in time (overlapping of the classes in time needs to be minimized).', 'Time Spread', 0, 0);
insert into DISTRIBUTION_TYPE (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref)
values (185, 'MIN_ROOM_USE', 'Minimize Number Of Rooms Used', '0', 25, 'P43210R', 'Minimize number of rooms used by the given set of classes.', 'Min Rooms', 1, 0);
insert into DISTRIBUTION_TYPE (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref)
values (165, 'PRECEDENCE', 'Precedence', '1', 24, 'P43210R', 'Given classes have to be taught in the given order (the first meeting of the first class has to end before the first meeting of the second class etc.)<BR>When prohibited or (strongly) discouraged: classes have to be taught in the order reverse to the given one', 'Precede', 0, 0);
insert into DISTRIBUTION_TYPE (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref)
values (61, 'BTB', 'Back-To-Back & Same Room', '0', 1, 'P43210R', 'Classes must be offered in adjacent time segments and must be placed in the same room. Given classes must also be taught on the same days.<BR>When prohibited or (strongly) discouraged: classes cannot be back-to-back. There must be at least half-hour between these classes, and they must be taught on the same days and in the same room.', 'BTB Same Room', 1, 0);
insert into DISTRIBUTION_TYPE (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref)
values (62, 'BTB_TIME', 'Back-To-Back', '0', 2, 'P43210R', 'Classes must be offered in adjacent time segments but may be placed in different rooms. Given classes must also be taught on the same days.<BR>When prohibited or (strongly) discouraged: no pair of classes can be taught back-to-back. They may not overlap in time, but must be taught on the same days. This means that there must be at least half-hour between these classes. ', 'BTB', 1, 0);
insert into DISTRIBUTION_TYPE (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref)
values (63, 'SAME_TIME', 'Same Time', '0', 3, 'P43210R', 'Given classes must be taught at the same time of day (independent of the actual day the classes meet). For the classes of the same length, this is the same constraint as <i>same start</i>. For classes of different length, the shorter one cannot start before, nor end after, the longer one.<BR>When prohibited or (strongly) discouraged: one class may not meet on any day at a time of day that overlaps with that of the other. For example, one class can not meet M 7:30 while the other meets F 7:30. Note the difference here from the <i>different time</i> constraint that only prohibits the actual class meetings from overlapping.', 'Same Time', 0, 0);
insert into DISTRIBUTION_TYPE (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref)
values (64, 'SAME_DAYS', 'Same Days', '0', 4, 'P43210R', 'Given classes must be taught on the same days. In case of classes of different time patterns, a class with fewer meetings must meet on a subset of the days used by the class with more meetings. For example, if one class pattern is 3x50, all others given in the constraint can only be taught on Monday, Wednesday, or Friday. For a 2x100 class MW, MF, WF is allowed but TTh is prohibited.<BR>When prohibited or (strongly) discouraged: any pair of classes classes cannot be taught on the same days (cannot overlap in days). For instance, if one class is MFW, the second has to be TTh.', 'Same Days', 1, 0);
insert into DISTRIBUTION_TYPE (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref)
values (65, 'NHB(1)', '1 Hour Between', '0', 5, 'P43210R', 'Given classes must have exactly 1 hour in between the end of one and the beginning of another. As with the <i>back-to-back time</i> constraint, given classes must be taught on the same days.<BR>When prohibited or (strongly) discouraged: classes can not have 1 hour in between. They may not overlap in time but must be taught on the same days.', '1h Btw', 0, 0);
insert into DISTRIBUTION_TYPE (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref)
values (66, 'NHB(2)', '2 Hours Between', '0', 6, 'P43210R', 'Given classes must have exactly 2 hours in between the end of one and the beginning of another. As with the <i>back-to-back time</i> constraint, given classes must be taught on the same days.<BR>When prohibited or (strongly) discouraged: classes can not have 2 hours in between. They may not overlap in time but must be taught on the same days.', '2h Btw', 0, 0);
insert into DISTRIBUTION_TYPE (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref)
values (67, 'NHB(3)', '3 Hours Between', '0', 7, 'P43210R', 'Given classes must have exactly 3 hours in between the end of one and the beginning of another. As with the <i>back-to-back time</i> constraint, given classes must be taught on the same days.<BR>When prohibited or (strongly) discouraged: classes can not have 3 hours in between. They may not overlap in time but must be taught on the same days.', '3h Btw', 0, 0);
insert into DISTRIBUTION_TYPE (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref)
values (68, 'NHB(4)', '4 Hours Between', '0', 8, 'P43210R', 'Given classes must have exactly 4 hours in between the end of one and the beginning of another. As with the <i>back-to-back time</i> constraint, given classes must be taught on the same days.<BR>When prohibited or (strongly) discouraged: classes can not have 4 hours in between. They may not overlap in time but must be taught on the same days.', '4h Btw', 0, 0);
insert into DISTRIBUTION_TYPE (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref)
values (69, 'NHB(5)', '5 Hours Between', '0', 9, 'P43210R', 'Given classes must have exactly 5 hours in between the end of one and the beginning of another. As with the <i>back-to-back time</i> constraint, given classes must be taught on the same days.<BR>When prohibited or (strongly) discouraged: classes can not have 5 hours in between. They may not overlap in time but must be taught on the same days.', '5h Btw', 0, 0);
insert into DISTRIBUTION_TYPE (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref)
values (70, 'NHB(6)', '6 Hours Between', '0', 10, 'P43210R', 'Given classes must have exactly 6 hours in between the end of one and the beginning of another. As with the <i>back-to-back time</i> constraint, given classes must be taught on the same days.<BR>When prohibited or (strongly) discouraged: classes can not have 6 hours in between. They may not overlap in time but must be taught on the same days.', '6h Btw', 0, 0);
insert into DISTRIBUTION_TYPE (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref)
values (71, 'NHB(7)', '7 Hours Between', '0', 11, 'P43210R', 'Given classes must have exactly 7 hours in between the end of one and the beginning of another. As with the <i>back-to-back time</i> constraint, given classes must be taught on the same days.<BR>When prohibited or (strongly) discouraged: classes can not have 7 hours in between. They may not overlap in time but must be taught on the same days.', '7h Btw', 0, 0);
insert into DISTRIBUTION_TYPE (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref)
values (72, 'NHB(8)', '8 Hours Between', '0', 12, 'P43210R', 'Given classes must have exactly 8 hours in between the end of one and the beginning of another. As with the <i>back-to-back time</i> constraint, given classes must be taught on the same days.<BR>When prohibited or (strongly) discouraged: classes can not have 8 hours in between. They may not overlap in time but must be taught on the same days.', '8h Btw', 0, 0);
insert into DISTRIBUTION_TYPE (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref)
values (73, 'DIFF_TIME', 'Different Time', '0', 13, 'P43210R', 'Given classes cannot overlap in time. They may be taught at the same time of day if they are on different days. For instance, MF 7:30 is compatible with TTh 7:30.<BR>When prohibited or (strongly) discouraged: every pair of classes in the constraint must overlap in time.', 'Diff Time', 0, 0);
insert into DISTRIBUTION_TYPE (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref)
values (74, 'NHB(1.5)', '90 Minutes Between', '0', 14, 'P43210R', 'Given classes must have exactly 90 minutes in between the end of one and the beginning of another. As with the <i>back-to-back time</i> constraint, given classes must be taught on the same days.<BR>When prohibited or (strongly) discouraged: classes can not have 90 minutes in between. They may not overlap in time but must be taught on the same days.', '90min Btw', 0, 0);
insert into DISTRIBUTION_TYPE (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref)
values (75, 'NHB(4.5)', '4.5 Hours Between', '0', 15, 'P43210R', 'Given classes must have exactly 4.5 hours in between the end of one and the beginning of another. As with the <i>back-to-back time</i> constraint, given classes must be taught on the same days.<BR>When prohibited or (strongly) discouraged: classes can not have 4.5 hours in between. They may not overlap in time but must be taught on the same days.', '4.5h Btw', 0, 0);
insert into DISTRIBUTION_TYPE (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref)
values (101, 'SAME_ROOM', 'Same Room', '0', 17, 'P43210R', 'Given classes must be taught in the same room.<BR>When prohibited or (strongly) discouraged: any pair of classes in the constraint cannot be taught in the same room.', 'Same Room', 1, 0);
insert into DISTRIBUTION_TYPE (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref)
values (102, 'NHB_GTE(1)', 'At Least 1 Hour Between', '0', 18, 'P43210R', 'Given classes have to have 1 hour or more in between.<BR>When prohibited or (strongly) discouraged: given classes have to have less than 1 hour in between.', '>=1h Btw', 1, 0);
insert into DISTRIBUTION_TYPE (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref)
values (103, 'SAME_START', 'Same Start Time', '0', 16, 'P43210R', 'Given classes must start during the same half-hour period of a day (independent of the actual day the classes meet). For instance, MW 7:30 is compatible with TTh 7:30 but not with MWF 8:00.<BR>When prohibited or (strongly) discouraged: any pair of classes in the given constraint cannot start during the same half-hour period of any day of the week.', 'Same Start', 0, 0);
insert into DISTRIBUTION_TYPE (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref)
values (104, 'NHB_LT(6)', 'Less Than 6 Hours Between', '0', 19, 'P43210R', 'Given classes must have less than 6 hours from end of first class to the beginning of the next.  Given classes must also be taught on the same days.<BR>When prohibited or (strongly) discouraged: given classes must have 6 or more hours between. This constraint does not carry over from classes taught at the end of one day to the beginning of the next.', '<6h Btw', 1, 0);
insert into DISTRIBUTION_TYPE (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref)
values (345, 'CH_NOTOVERLAP', 'Children Cannot Overlap', '0', 33, '210R', 'If parent classes do not overlap in time, children classes can not overlap in time as well.<br>Note: This constraint only needs to be put on the parent classes. Preferred configurations are Required All Classes or Pairwise (Strongly) Preferred.', 'Ch No Ovlap', 0, 0);
insert into DISTRIBUTION_TYPE (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref)
values (305, 'NDB_GT_1', 'More Than 1 Day Between', '0', 32, 'P43210R', 'Given classes must have two or more days in between.<br>When prohibited or (strongly) discouraged: given classes must be offered on adjacent days or with at most one day in between.', '>1d Btw', 0, 0);
insert into DISTRIBUTION_TYPE (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref)
values (365, 'FOLLOWING_DAY', 'Next Day', '1', 34, 'P43210R', 'The second class has to be placed on the following day of the first class (if the first class is on Friday, second class have to be on Monday).<br> When prohibited or (strongly) discouraged: The second class has to be placed on the previous day of the first class (if the first class is on Monday, second class have to be on Friday).<br> Note: This constraint works only between pairs of classes.', 'Next Day', 0, 0);
insert into DISTRIBUTION_TYPE (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref)
values (366, 'EVERY_OTHER_DAY', 'Two Days After', '1', 35, 'P43210R', 'The second class has to be placed two days after the first class (Monday &rarr; Wednesday, Tuesday &rarr; Thurday, Wednesday &rarr; Friday, Thursday &rarr; Monday, Friday &rarr; Tuesday).<br> When prohibited or (strongly) discouraged: The second class has to be placed two days before the first class (Monday &rarr; Thursday, Tuesday &rarr; Friday, Wednesday &rarr; Monday, Thursday &rarr; Tuesday, Friday &rarr; Wednesday).<br> Note: This constraint works only between pairs of classes.', '2d After', 0, 0);
insert into DISTRIBUTION_TYPE (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref)
values (367, 'MEET_WITH', 'Meet Together', '0', 31, '2R', 'Given classes are meeting together (same as if the given classes require constraints Can Share Room, Same Room, Same Time and Same Days all together).', 'Meet Together', 0, 0);
insert into DISTRIBUTION_TYPE (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref)
values (405, 'EX_SAME_PER', 'Same Period', '0', 36, 'P43210R', 'Exams are to be placed at the same period. <BR>When prohibited or (strongly) discouraged: exams are to be placed at different periods.', 'Same Per', 0, 1);
insert into DISTRIBUTION_TYPE (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref)
values (406, 'EX_SAME_ROOM', 'Same Room', '0', 37, 'P43210R', 'Exams are to be placed at the same room(s). <BR>When prohibited or (strongly) discouraged: exams are to be placed at different rooms.', 'Same Room', 0, 1);
insert into DISTRIBUTION_TYPE (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref)
values (407, 'EX_PRECEDENCE', 'Precedence', '1', 38, 'P43210R', 'Exams are to be placed in the given order. <BR>When prohibited or (strongly) discouraged: exams are to be placed in the order reverse to the given one.', 'Precede', 0, 1);
commit;
prompt 38 records loaded
prompt Loading DISTRIBUTION_PREF...
prompt Table is empty
prompt Loading DISTRIBUTION_OBJECT...
prompt Table is empty
prompt Loading DIST_TYPE_DEPT...
prompt Table is empty
prompt Loading EVENT_CONTACT...
prompt Table is empty
prompt Loading EXAM_PERIOD...
prompt Table is empty
prompt Loading EXAM...
prompt Table is empty
prompt Loading SPONSORING_ORGANIZATION...
prompt Table is empty
prompt Loading EVENT...
prompt Table is empty
prompt Loading EVENT_JOIN_EVENT_CONTACT...
prompt Table is empty
prompt Loading EVENT_NOTE...
prompt Table is empty
prompt Loading EXACT_TIME_MINS...
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214405, 0, 0, 0, 0);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214406, 1, 5, 1, 0);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214407, 6, 10, 2, 0);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214408, 11, 15, 4, 0);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214409, 16, 20, 5, 0);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214410, 21, 25, 6, 0);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214411, 26, 30, 7, 0);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214412, 31, 35, 8, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214413, 36, 40, 10, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214414, 41, 45, 11, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214415, 46, 50, 12, 10);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214416, 51, 55, 13, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214417, 56, 60, 14, 10);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214418, 61, 65, 16, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214419, 66, 70, 17, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214420, 71, 75, 18, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214421, 76, 80, 19, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214422, 81, 85, 20, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214423, 86, 90, 21, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214424, 91, 95, 23, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214425, 96, 100, 24, 10);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214426, 101, 105, 25, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214427, 106, 110, 26, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214428, 111, 115, 28, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214429, 116, 120, 29, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214430, 121, 125, 30, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214431, 126, 130, 31, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214432, 131, 135, 32, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214433, 136, 140, 34, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214434, 141, 145, 35, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214435, 146, 150, 36, 10);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214436, 151, 155, 37, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214437, 156, 160, 38, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214438, 161, 165, 40, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214439, 166, 170, 41, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214440, 171, 175, 42, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214441, 176, 180, 43, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214442, 181, 185, 44, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214443, 186, 190, 46, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214444, 191, 195, 47, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214445, 196, 200, 48, 10);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214446, 201, 205, 49, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214447, 206, 210, 50, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214448, 211, 215, 52, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214449, 216, 220, 53, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214450, 221, 225, 54, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214451, 226, 230, 55, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214452, 231, 235, 56, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214453, 236, 240, 58, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214454, 241, 245, 59, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214455, 246, 250, 60, 10);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214456, 251, 255, 61, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214457, 256, 260, 62, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214458, 261, 265, 64, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214459, 266, 270, 65, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214460, 271, 275, 66, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214461, 276, 280, 67, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214462, 281, 285, 68, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214463, 286, 290, 70, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214464, 291, 295, 71, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214465, 296, 300, 72, 10);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214466, 301, 305, 73, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214467, 306, 310, 74, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214468, 311, 315, 76, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214469, 316, 320, 77, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214470, 321, 325, 78, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214471, 326, 330, 79, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214472, 331, 335, 80, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214473, 336, 340, 82, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214474, 341, 345, 83, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214475, 346, 350, 84, 10);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214476, 351, 355, 85, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214477, 356, 360, 86, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214478, 361, 365, 88, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214479, 366, 370, 89, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214480, 371, 375, 90, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214481, 376, 380, 91, 5);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214482, 381, 385, 92, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214483, 386, 390, 94, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214484, 391, 395, 95, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214485, 396, 400, 96, 10);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214486, 401, 405, 97, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214487, 406, 410, 98, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214488, 411, 415, 100, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214489, 416, 420, 101, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214490, 421, 425, 102, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214491, 426, 430, 103, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214492, 431, 435, 104, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214493, 436, 440, 106, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214494, 441, 445, 107, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214495, 446, 450, 108, 10);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214496, 451, 455, 109, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214497, 456, 460, 110, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214498, 461, 465, 112, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214499, 466, 470, 113, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214500, 471, 475, 114, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214501, 476, 480, 115, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214502, 481, 485, 116, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214503, 486, 490, 118, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214504, 491, 495, 119, 15);
commit;
prompt 100 records committed...
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214505, 496, 500, 120, 10);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214506, 501, 505, 121, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214507, 506, 510, 122, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214508, 511, 515, 124, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214509, 516, 520, 125, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214510, 521, 525, 126, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214511, 526, 530, 127, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214512, 531, 535, 128, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214513, 536, 540, 130, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214514, 541, 545, 131, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214515, 546, 550, 132, 10);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214516, 551, 555, 133, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214517, 556, 560, 134, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214518, 561, 565, 136, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214519, 566, 570, 137, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214520, 571, 575, 138, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214521, 576, 580, 139, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214522, 581, 585, 140, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214523, 586, 590, 142, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214524, 591, 595, 143, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214525, 596, 600, 144, 10);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214526, 601, 605, 145, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214527, 606, 610, 146, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214528, 611, 615, 148, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214529, 616, 620, 149, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214530, 621, 625, 150, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214531, 626, 630, 151, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214532, 631, 635, 152, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214533, 636, 640, 154, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214534, 641, 645, 155, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214535, 646, 650, 156, 10);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214536, 651, 655, 157, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214537, 656, 660, 158, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214538, 661, 665, 160, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214539, 666, 670, 161, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214540, 671, 675, 162, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214541, 676, 680, 163, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214542, 681, 685, 164, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214543, 686, 690, 166, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214544, 691, 695, 167, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214545, 696, 700, 168, 10);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214546, 701, 705, 169, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214547, 706, 710, 170, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214548, 711, 715, 172, 15);
insert into EXACT_TIME_MINS (uniqueid, mins_min, mins_max, nr_slots, break_time)
values (214549, 716, 720, 173, 15);
commit;
prompt 145 records loaded
prompt Loading EXAM_INSTRUCTOR...
prompt Table is empty
prompt Loading EXAM_LOCATION_PREF...
prompt Table is empty
prompt Loading EXAM_OWNER...
prompt Table is empty
prompt Loading EXAM_PERIOD_PREF...
prompt Table is empty
prompt Loading EXAM_ROOM_ASSIGNMENT...
prompt Table is empty
prompt Loading EXTERNAL_BUILDING...
prompt Table is empty
prompt Loading ROOM_TYPE...
insert into ROOM_TYPE (uniqueid, reference, label, ord, is_room)
values (425, 'genClassroom', 'Classrooms', 0, 1);
insert into ROOM_TYPE (uniqueid, reference, label, ord, is_room)
values (426, 'computingLab', 'Computing Laboratories', 1, 1);
insert into ROOM_TYPE (uniqueid, reference, label, ord, is_room)
values (427, 'departmental', 'Additional Instructional Rooms', 2, 1);
insert into ROOM_TYPE (uniqueid, reference, label, ord, is_room)
values (428, 'specialUse', 'Special Use Rooms', 3, 1);
insert into ROOM_TYPE (uniqueid, reference, label, ord, is_room)
values (429, 'nonUniversity', 'Outside Locations', 4, 0);
commit;
prompt 5 records loaded
prompt Loading EXTERNAL_ROOM...
prompt Table is empty
prompt Loading EXTERNAL_ROOM_DEPARTMENT...
prompt Table is empty
prompt Loading EXTERNAL_ROOM_FEATURE...
prompt Table is empty
prompt Loading HISTORY...
prompt Table is empty
prompt Loading HT_PREFERENCE...
prompt Table is empty
prompt Loading INDIVIDUAL_RESERVATION...
prompt Table is empty
prompt Loading JENRL...
prompt Table is empty
prompt Loading LASTLIKE_COURSE_DEMAND...
prompt Table is empty
prompt Loading SETTINGS...
insert into SETTINGS (uniqueid, name, default_value, allowed_values, description)
values (148, 'roomFeaturesInOneColumn', 'yes', 'yes,no', 'Display Room Features In One Column');
insert into SETTINGS (uniqueid, name, default_value, allowed_values, description)
values (88, 'jsConfirm', 'yes', 'yes,no', 'Display confirmation dialogs');
insert into SETTINGS (uniqueid, name, default_value, allowed_values, description)
values (85, 'name', 'last-initial', 'last-first,first-last,initial-last,last-initial,first-middle-last,short', 'Instructor name display format');
insert into SETTINGS (uniqueid, name, default_value, allowed_values, description)
values (42, 'timeGrid', 'vertical', 'horizontal,vertical,text', 'Time grid display format');
insert into SETTINGS (uniqueid, name, default_value, allowed_values, description)
values (86, 'cfgAutoCalc', 'yes', 'yes,no', 'Automatically calculate number of classes and room size when editing configuration');
insert into SETTINGS (uniqueid, name, default_value, allowed_values, description)
values (87, 'timeGridSize', 'Workdays x Daytime', 'Workdays x Daytime,All Week x Daytime,Workdays x Evening,All Week x Evening,All Week x All Times', 'Time grid default selection');
insert into SETTINGS (uniqueid, name, default_value, allowed_values, description)
values (89, 'inheritInstrPref', 'never', 'ask,always,never', 'Inherit instructor preferences on a class');
insert into SETTINGS (uniqueid, name, default_value, allowed_values, description)
values (108, 'showVarLimits', 'no', 'yes,no', 'Show the option to set variable class limits');
insert into SETTINGS (uniqueid, name, default_value, allowed_values, description)
values (128, 'keepSort', 'no', 'yes,no', 'Sort classes on detail pages');
insert into SETTINGS (uniqueid, name, default_value, allowed_values, description)
values (168, 'dispLastChanges', 'yes', 'yes,no', 'Display information from the change log in pages.');
insert into SETTINGS (uniqueid, name, default_value, allowed_values, description)
values (188, 'printNoteDisplay', 'icon', 'icon,shortened text,full text', 'Display an icon or shortened text when a class has a schedule print note.');
insert into SETTINGS (uniqueid, name, default_value, allowed_values, description)
values (189, 'crsOffrNoteDisplay', 'icon', 'icon,shortened text,full text', 'Display an icon or shortened text when a course offering has a schedule note.');
insert into SETTINGS (uniqueid, name, default_value, allowed_values, description)
values (190, 'mgrNoteDisplay', 'icon', 'icon,shortened text,full text', 'Display an icon or shortened text when a class has a note to the schedule manager.');
insert into SETTINGS (uniqueid, name, default_value, allowed_values, description)
values (208, 'unitime.menu.style', 'Dynamic On Top', 'Dynamic On Top,Static On Top,Tree On Side,Stack On Side', 'Menu style');
commit;
prompt 14 records loaded
prompt Loading MANAGER_SETTINGS...
prompt Table is empty
prompt Loading MEETING...
prompt Table is empty
prompt Loading NON_UNIVERSITY_LOCATION...
prompt Table is empty
prompt Loading OFFR_GROUP...
prompt Table is empty
prompt Loading OFFR_GROUP_OFFERING...
prompt Table is empty
prompt Loading POSITION_CODE_TO_TYPE...
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2271F', 9);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2266F', 7);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2212F', 8);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2283F', 9);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2293F', 9);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2201F', 10);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2133S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2135S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2136S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2138S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2142S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2143S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2145S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2150S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2151S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2152S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2154S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2155S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2157S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2161S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2164S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2166S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2167S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2168S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2174S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2181S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2183S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2184S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2185S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2196S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2198S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2199S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2200S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2201S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2202S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2204S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2205S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2206S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2207S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2208S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2212S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2223S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2224S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2235S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2245S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2263S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2341S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2346S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2357S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2359S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2406S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2504S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2505S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2555S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2556S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2618S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2627S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2638S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2648S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2669S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2702S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2705S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2816S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2817S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2907S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2908S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2939S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('3102S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('3104S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('3105S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('3106S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('3107S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('3201S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('3203S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('3204S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('3205S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('3206S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('3225S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('3241S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('3304S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('3306S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('3507S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('3509S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('4105S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('4106S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('4256S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('4267S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('4277S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('4278S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('4289S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('4298S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('4309S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('4408S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('4409S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('4456S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('4457S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('4459S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('4509S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('4726S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('4727S', 14);
commit;
prompt 100 records committed...
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('4728S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5006S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5013S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5014S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5015S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5016S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5023S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5024S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5025S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5026S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5034S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5035S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5036S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5043S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5044S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5046S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5315S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5316S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5325S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5326S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5336S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5355S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5413S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5414S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5416S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5417S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5418S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5423S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5424S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5426S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5427S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5433S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5434S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5435S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5437S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5516S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5517S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5526S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5536S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5616S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5626S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5636S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5706S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7147S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7148S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7149S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7155S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7156S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7165S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7166S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7167S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7169S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7175S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7176S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7177S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7185S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7186S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7187S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7197S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7205S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7207S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7208S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7214S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7216S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7235S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7256S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7316S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7317S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7318S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7413S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7414S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7415S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7416S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7417S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7454S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7455S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7456S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7458S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7466S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7473S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7474S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7475S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7476S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7478S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7544S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7545S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7546S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7547S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7558S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7559S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7564S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7567S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7577S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7586S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7587S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7588S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7596S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7606S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7607S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7608S', 14);
commit;
prompt 200 records committed...
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7616S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7617S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7626S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7627S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7635S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7636S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7637S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7645S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7657S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7675S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7688S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7698S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7708S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7709S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7715S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7716S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7718S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7719S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7727S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7737S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7916S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7918S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7925S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7926S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7927S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7929S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7938S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7939S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8016S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8017S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8019S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8026S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8027S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8028S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8036S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8038S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8039S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8046S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8047S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8057S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8058S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8066S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8078S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8079S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8086S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8087S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8088S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8089S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8097S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8099S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8109S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8116S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2093S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2102S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2124S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0005C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0112C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8139S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8204S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8268S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2215S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2252S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2401S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2617S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0018F', 1);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0118F', 1);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0028F', 1);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0128F', 1);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2228F', 1);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0006F', 1);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0106F', 1);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0206F', 1);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2206F', 1);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0005F', 2);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0105F', 2);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0205F', 2);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2205F', 2);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0004F', 3);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0104F', 3);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0204F', 3);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2204F', 3);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0003F', 4);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0103F', 4);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1103F', 4);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2203F', 4);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('22T3F', 6);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2263F', 7);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0062G', 11);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2262G', 11);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0040A', 18);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0070A', 18);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0002Z', 15);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0003Z', 15);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0004Z', 15);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0005Z', 15);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0024Z', 15);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0025Z', 15);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0034Z', 15);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('9999Z', 15);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0001F', 10);
commit;
prompt 300 records committed...
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0017F', 1);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0027F', 1);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0117F', 1);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0127F', 1);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('9991F', 10);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('9999F', 10);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2264F', 16);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2208F', 17);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1022C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1023C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1103C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1111C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2015C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('3013C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('3014C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('3015C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('4023C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('4024C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('4025C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5005C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5014C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5023C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5024C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5025C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5052C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5053C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5055C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5082C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('9999C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P000C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P001C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P003C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P004C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P005C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P993C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P994C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('S001C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('S002C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('S003C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('S004C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('S005C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('S007C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('T001C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('T002C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('T003C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('T004C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0000S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0004S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0009S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0213S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0504S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0505S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0506S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0507S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0508S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0514S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0516S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0517S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0518S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0623S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0627S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0635S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0636S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0637S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0643S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0644S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0645S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0646S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0647S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0651S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0653S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0655S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1005S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1006S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1018S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1019S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1029S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1039S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1116S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1117S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1118S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1119S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1126S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1128S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1129S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1134S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1136S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1137S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1139S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1146S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1147S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1148S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1156S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1157S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1167S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1168S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1169S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1177S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1178S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1179S', 14);
commit;
prompt 400 records committed...
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1186S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1187S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1189S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1196S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1197S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1198S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1207S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1208S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1209S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1216S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1217S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1219S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1226S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1227S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1228S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1229S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1235S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1236S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1237S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1238S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1246S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1256S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1257S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1258S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1259S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1268S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1269S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1287S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1288S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1289S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1297S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1298S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1299S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1306S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1308S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1309S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1316S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1317S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1318S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1326S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1327S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1328S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1329S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1336S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1338S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1339S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1347S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1348S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1356S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1357S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1358S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1359S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1365S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1367S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1368S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1369S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1378S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1379S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1398S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1406S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1407S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1408S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1455S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1457S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1497S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1498S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1504S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1506S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1507S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1508S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1523S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1524S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1526S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1527S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1528S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1529S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1546S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1547S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1548S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1549S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1595S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1597S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1687S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1688S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1696S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1697S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1699S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1706S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1707S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1708S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1717S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1718S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1726S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1727S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1737S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1777S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1778S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1816S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1835S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1836S', 14);
commit;
prompt 500 records committed...
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1838S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1847S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1848S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1849S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1867S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1909S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2006S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2011S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2012S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2022S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2023S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2024S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2025S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2039S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2042S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2043S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2044S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2045S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2047S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2048S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2049S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2053S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2055S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2063S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2064S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2065S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2066S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2067S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2073S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2074S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2075S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2076S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2081S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2085S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2091S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2094S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2096S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2097S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2101S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2103S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2115S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2116S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2121S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2122S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2125S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2126S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2127S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0000C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0002C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0011C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0012C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0013C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0014C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0015C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0113C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0114C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0134C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0211C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0212C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0213C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0214C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0215C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0233C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0234C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0312C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0313C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0314C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0315C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0342C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0343C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0344C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0345C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0522C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0523C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0524C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0525C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0572C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0573C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0574C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0602C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('060SC', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0623C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0624C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0802C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0803C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0902C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0903C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0904C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0905C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0912C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0913C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0914C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0922C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0923C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0924C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1002C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1003C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1012C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1013C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1104C', 13);
commit;
prompt 600 records committed...
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('4022C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5022C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5054C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P002C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P995C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('S006C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('T005C', 13);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0217S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0509S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0626S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0638S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0648S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1007S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1111S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1127S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1138S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1149S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1176S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1188S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1199S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1218S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1234S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1239S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1267S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1296S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1307S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1319S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1337S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1349S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1366S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1397S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1409S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1505S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1525S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1537S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1596S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1698S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1709S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1738S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1837S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1859S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2017S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2041S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2046S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2061S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2071S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2077S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2095S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2105S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2123S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2134S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2144S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2153S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2165S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2182S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2197S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2203S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2209S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2225S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2345S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2408S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2557S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2659S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2818S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('3103S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('3202S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('3226S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('3508S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('4268S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('4407S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('4458S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('4729S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5017S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5033S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5045S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5335S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5415S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5425S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5436S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5527S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5707S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5803S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5804S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5805S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('5806S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('6023S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('6026S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('6027S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('6076S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('6078S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('6079S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('6107S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('6206S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('6207S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('6307S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('6308S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('6309S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('6317S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('6406S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('6407S', 14);
commit;
prompt 700 records committed...
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('6408S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('6409S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('6417S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('6418S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('6419S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('6487S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('6488S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('6489S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('6516S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('6517S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('6518S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('6519S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('6586S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('6587S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('6588S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('6589S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('6646S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('6647S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('6648S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('6649S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('6706S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('6707S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('6708S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('6709S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('6807S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('6858S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7117S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7118S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7126S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7127S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7128S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7129S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7136S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7137S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7138S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7144S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7145S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7146S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7154S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7168S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7178S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7206S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7236S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7326S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7424S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7468S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7477S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7548S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7578S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7605S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7618S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7638S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7697S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7717S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7917S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('7928S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8018S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8037S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8056S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8085S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8098S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8118S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8136S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8177S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8205S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8269S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8287S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8293S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8308S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8388S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8454S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8468S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8479S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8526S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8576S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8597S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8619S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8658S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8689S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8699S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8717S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8728S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8738S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8749S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8765S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8777S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8805S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8816S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8837S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8857S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8875S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8886S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8938S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8985S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('9037S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('9078S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('9153S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P000S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P006S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P053S', 14);
commit;
prompt 800 records committed...
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P256S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P333S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P415S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P993S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('S001S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('S007S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('T004S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0000A', 18);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0030A', 18);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('9999A', 18);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0042G', 11);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0002F', 8);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0007F', 1);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0108F', 1);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('02C3F', 5);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0207F', 1);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2273F', 9);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0256F', 8);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2254F', 8);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2258F', 8);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2246F', 8);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('9951F', 8);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0107F', 1);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0001Z', 15);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8117S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8119S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8126S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8127S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8128S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8129S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8137S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8138S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8144S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8154S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8178S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8202S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8203S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8206S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8207S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8266S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8267S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8276S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8277S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8278S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8279S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8286S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8288S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8289S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8291S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8292S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8302S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8303S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8305S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8306S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8307S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8309S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8317S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8318S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8319S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8386S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8399S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8408S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8409S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8453S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8456S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8457S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8458S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8466S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8467S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8469S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8471S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8476S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8477S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8478S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8507S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8517S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8518S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8525S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8527S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8528S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8529S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8567S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8568S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8577S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8578S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8594S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8595S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8596S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8598S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8616S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8617S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8618S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8624S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8625S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8626S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8627S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8628S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8667S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8668S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8686S', 14);
commit;
prompt 900 records committed...
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8687S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8688S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8695S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8696S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8697S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8698S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8706S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8707S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8708S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8715S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8716S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8718S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8719S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8725S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8726S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8727S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8729S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8735S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8736S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8737S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8739S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8745S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8746S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8747S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8748S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8754S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8756S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8757S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8758S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8759S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8766S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8767S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8775S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8776S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8787S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8788S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8789S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8797S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8798S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8806S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8807S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8808S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8809S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8815S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8817S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8827S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8828S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8829S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8838S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8848S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8849S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8855S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8856S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8858S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8859S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8866S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8867S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8874S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8876S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8877S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8878S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8879S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8895S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8897S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8898S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8927S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8929S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8957S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8958S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8959S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8965S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('8975S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('9017S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('9019S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('9027S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('9029S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('9039S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('9055S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('9056S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('9066S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('9067S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('9106S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('9116S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('9127S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('9137S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('9148S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('9154S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('9155S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('9168S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('9999S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P001S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P002S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P003S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P004S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P005S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P007S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P008S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P009S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P046S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P047S', 14);
commit;
prompt 1000 records committed...
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P206S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P225S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P226S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P255S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P276S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P303S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P304S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P313S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P323S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P343S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P353S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P354S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P363S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P414S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P525S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P526S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P654S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P725S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P994S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P995S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P996S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P997S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('P998S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('S002S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('S003S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('S004S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('S005S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('S006S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('S008S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('T001S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('T002S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('T003S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('T005S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('T006S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('T007S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('T008S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('T009S', 14);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0001A', 18);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0010A', 18);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0020A', 18);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0050A', 18);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0060A', 18);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0080A', 18);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0090A', 18);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0001G', 11);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0002G', 11);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0003G', 11);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0012G', 11);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0022G', 11);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0032G', 11);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0052G', 11);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0063G', 11);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0072G', 11);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0074G', 11);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('9999G', 11);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0036F', 1);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0065F', 7);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('02C6F', 5);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('02C5F', 5);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('02C4F', 5);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0008F', 1);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0035F', 2);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0046F', 1);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0066F', 7);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2274F', 9);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0253F', 8);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0254F', 8);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0255F', 8);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1253F', 8);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2252F', 8);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2253F', 8);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2255F', 8);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2256F', 8);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2257F', 8);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('22C4F', 8);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2243F', 8);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2244F', 8);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('2245F', 8);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('0075F', 9);
insert into POSITION_CODE_TO_TYPE (position_code, pos_code_type)
values ('1263F', 6);
commit;
prompt 1080 records loaded
prompt Loading POS_ACAD_AREA_MAJOR...
prompt Table is empty
prompt Loading POS_MINOR...
prompt Table is empty
prompt Loading POS_ACAD_AREA_MINOR...
prompt Table is empty
prompt Loading POS_RESERVATION...
prompt Table is empty
prompt Loading RELATED_COURSE_INFO...
prompt Table is empty
prompt Loading ROLES...
insert into ROLES (role_id, reference, abbv)
values (1, 'Administrator', 'Administrator');
insert into ROLES (role_id, reference, abbv)
values (21, 'Dept Sched Mgr', 'Department Schedule Manager');
insert into ROLES (role_id, reference, abbv)
values (41, 'View All', 'View All User');
insert into ROLES (role_id, reference, abbv)
values (61, 'Exam Mgr', 'Examination Timetabling Manager');
insert into ROLES (role_id, reference, abbv)
values (81, 'Event Mgr', 'Event Manager');
insert into ROLES (role_id, reference, abbv)
values (101, 'Curriculum Mgr', 'Curriculum Manager');
commit;
prompt 6 records loaded
prompt Loading ROOM...
prompt Table is empty
prompt Loading ROOM_DEPT...
prompt Table is empty
prompt Loading ROOM_FEATURE...
insert into ROOM_FEATURE (uniqueid, discriminator, label, sis_reference, sis_value, department_id, abbv)
values (468, 'global', 'Chalkboard < 20 Ft.', 'feetOfChalkboard', '< 20', null, 'Ch<20F');
insert into ROOM_FEATURE (uniqueid, discriminator, label, sis_reference, sis_value, department_id, abbv)
values (469, 'global', 'Chalkboard >= 20 Ft.', 'feetOfChalkboard', '>= 20', null, 'Ch>=20F');
insert into ROOM_FEATURE (uniqueid, discriminator, label, sis_reference, sis_value, department_id, abbv)
values (123, 'global', 'Audio Recording', 'audioRecording', null, null, 'AudRec');
insert into ROOM_FEATURE (uniqueid, discriminator, label, sis_reference, sis_value, department_id, abbv)
values (125, 'global', 'Computer', 'puccComputer', null, null, 'Comp');
insert into ROOM_FEATURE (uniqueid, discriminator, label, sis_reference, sis_value, department_id, abbv)
values (437, 'global', 'Fixed Seating', 'fixedSeating', null, null, 'FixSeat');
insert into ROOM_FEATURE (uniqueid, discriminator, label, sis_reference, sis_value, department_id, abbv)
values (438, 'global', 'Computer Projection', 'computerProjection', null, null, 'CompPr');
insert into ROOM_FEATURE (uniqueid, discriminator, label, sis_reference, sis_value, department_id, abbv)
values (440, 'global', 'Tables and Chairs', 'seatingType', 'tablesAndChairs', null, 'Tbls&Chrs');
insert into ROOM_FEATURE (uniqueid, discriminator, label, sis_reference, sis_value, department_id, abbv)
values (441, 'global', 'Tablet Arm Chairs', 'seatingType', 'tabletArmChairs', null, 'TblArmChr');
insert into ROOM_FEATURE (uniqueid, discriminator, label, sis_reference, sis_value, department_id, abbv)
values (442, 'global', 'Theater Seats', 'seatingType', 'theaterSeats', null, 'ThtrSeat');
commit;
prompt 9 records loaded
prompt Loading ROOM_FEATURE_PREF...
prompt Table is empty
prompt Loading ROOM_GROUP...
prompt Table is empty
prompt Loading ROOM_GROUP_PREF...
prompt Table is empty
prompt Loading ROOM_GROUP_ROOM...
prompt Table is empty
prompt Loading ROOM_JOIN_ROOM_FEATURE...
prompt Table is empty
prompt Loading ROOM_PREF...
prompt Table is empty
prompt Loading ROOM_TYPE_OPTION...
insert into ROOM_TYPE_OPTION (room_type, session_id, status, message)
values (425, 239259, 0, null);
insert into ROOM_TYPE_OPTION (room_type, session_id, status, message)
values (426, 239259, 0, null);
insert into ROOM_TYPE_OPTION (room_type, session_id, status, message)
values (427, 239259, 0, null);
insert into ROOM_TYPE_OPTION (room_type, session_id, status, message)
values (428, 239259, 0, null);
insert into ROOM_TYPE_OPTION (room_type, session_id, status, message)
values (429, 239259, 0, null);
commit;
prompt 5 records loaded
prompt Loading SECTIONING_INFO...
prompt Table is empty
prompt Loading SOLVER_GR_TO_TT_MGR...
prompt Table is empty
prompt Loading SOLVER_INFO_DEF...
insert into SOLVER_INFO_DEF (uniqueid, name, description, implementation)
values (1, 'GlobalInfo', 'Global solution information table', 'org.unitime.timetable.solver.ui.PropertiesInfo');
insert into SOLVER_INFO_DEF (uniqueid, name, description, implementation)
values (2, 'CBSInfo', 'Conflict-based statistics', 'org.unitime.timetable.solver.ui.ConflictStatisticsInfo');
insert into SOLVER_INFO_DEF (uniqueid, name, description, implementation)
values (3, 'AssignmentInfo', 'Preferences of a single assignment', 'org.unitime.timetable.solver.ui.AssignmentPreferenceInfo');
insert into SOLVER_INFO_DEF (uniqueid, name, description, implementation)
values (4, 'DistributionInfo', 'Distribution (group constraint) preferences', 'org.unitime.timetable.solver.ui.GroupConstraintInfo');
insert into SOLVER_INFO_DEF (uniqueid, name, description, implementation)
values (5, 'JenrlInfo', 'Student conflicts', 'org.unitime.timetable.solver.ui.JenrlInfo');
insert into SOLVER_INFO_DEF (uniqueid, name, description, implementation)
values (6, 'LogInfo', 'Solver Log', 'org.unitime.timetable.solver.ui.LogInfo');
insert into SOLVER_INFO_DEF (uniqueid, name, description, implementation)
values (7, 'BtbInstructorInfo', 'Back-to-back instructor preferences', 'org.unitime.timetable.solver.ui.BtbInstructorConstraintInfo');
commit;
prompt 7 records loaded
prompt Loading SOLVER_PARAMETER_GROUP...
insert into SOLVER_PARAMETER_GROUP (uniqueid, name, description, condition, ord, param_type)
values (41, 'Distance', 'Distances', null, 14, 0);
insert into SOLVER_PARAMETER_GROUP (uniqueid, name, description, condition, ord, param_type)
values (21, 'TimePreferences', 'Default Time Preferences', null, 13, 0);
insert into SOLVER_PARAMETER_GROUP (uniqueid, name, description, condition, ord, param_type)
values (1, 'Basic', 'Basic Settings', null, 0, 0);
insert into SOLVER_PARAMETER_GROUP (uniqueid, name, description, condition, ord, param_type)
values (2, 'General', 'General Settings', null, 1, 0);
insert into SOLVER_PARAMETER_GROUP (uniqueid, name, description, condition, ord, param_type)
values (3, 'MPP', 'Minimal-perturbation Setting', null, 2, 0);
insert into SOLVER_PARAMETER_GROUP (uniqueid, name, description, condition, ord, param_type)
values (4, 'Perturbations', 'Perturbation Penalty', null, 3, 0);
insert into SOLVER_PARAMETER_GROUP (uniqueid, name, description, condition, ord, param_type)
values (5, 'DepartmentSpread', 'Departmental Balancing', null, 4, 0);
insert into SOLVER_PARAMETER_GROUP (uniqueid, name, description, condition, ord, param_type)
values (6, 'ConflictStatistics', 'Conflict-based Statistics', null, 5, 0);
insert into SOLVER_PARAMETER_GROUP (uniqueid, name, description, condition, ord, param_type)
values (7, 'Termination', 'Termination Conditions', null, 6, 0);
insert into SOLVER_PARAMETER_GROUP (uniqueid, name, description, condition, ord, param_type)
values (8, 'Comparator', 'Solution Comparator Weights', null, 7, 0);
insert into SOLVER_PARAMETER_GROUP (uniqueid, name, description, condition, ord, param_type)
values (9, 'Variable', 'Lecture Selection', null, 8, 0);
insert into SOLVER_PARAMETER_GROUP (uniqueid, name, description, condition, ord, param_type)
values (10, 'Value', 'Placement Selection', null, 9, 0);
insert into SOLVER_PARAMETER_GROUP (uniqueid, name, description, condition, ord, param_type)
values (11, 'Classes', 'Implementations', null, 10, 0);
insert into SOLVER_PARAMETER_GROUP (uniqueid, name, description, condition, ord, param_type)
values (12, 'Spread', 'Same Subpart Balancing', null, 11, 0);
insert into SOLVER_PARAMETER_GROUP (uniqueid, name, description, condition, ord, param_type)
values (13, 'SearchIntensification', 'Search Intensification', null, 12, 0);
insert into SOLVER_PARAMETER_GROUP (uniqueid, name, description, condition, ord, param_type)
values (61, 'Neighbour', 'Neighbour Selection', null, 15, 0);
insert into SOLVER_PARAMETER_GROUP (uniqueid, name, description, condition, ord, param_type)
values (81, 'OnFlySectioning', 'On Fly Student Sectioning', null, 16, 0);
insert into SOLVER_PARAMETER_GROUP (uniqueid, name, description, condition, ord, param_type)
values (82, 'ExamBasic', 'Basic Parameters', null, 17, 1);
insert into SOLVER_PARAMETER_GROUP (uniqueid, name, description, condition, ord, param_type)
values (83, 'ExamWeights', 'Examination Weights', null, 18, 1);
insert into SOLVER_PARAMETER_GROUP (uniqueid, name, description, condition, ord, param_type)
values (84, 'Exam', 'General Parameters', null, 19, 1);
insert into SOLVER_PARAMETER_GROUP (uniqueid, name, description, condition, ord, param_type)
values (85, 'ExamGD', 'Great Deluge Parameters', null, 20, 1);
insert into SOLVER_PARAMETER_GROUP (uniqueid, name, description, condition, ord, param_type)
values (86, 'ExamSA', 'Simulated Annealing Parameters', null, 21, 1);
insert into SOLVER_PARAMETER_GROUP (uniqueid, name, description, condition, ord, param_type)
values (101, 'StudentSctBasic', 'Basic Parameters', null, 22, 2);
insert into SOLVER_PARAMETER_GROUP (uniqueid, name, description, condition, ord, param_type)
values (102, 'StudentSct', 'General Parameters', null, 23, 2);
commit;
prompt 24 records loaded
prompt Loading SOLVER_PARAMETER_DEF...
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (109, 'Placement.NrAssignmentsWeight2', '0.0', 'Number of assignments weight (level 2)', 'double', 20, 0, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (110, 'Placement.NrConflictsWeight2', '0.0', 'Number of conflicts weight (level 2)', 'double', 21, 1, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (111, 'Placement.WeightedConflictsWeight2', '0.0', 'Weighted conflicts weight (CBS, level 2)', 'double', 22, 1, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (112, 'Placement.NrPotentialConflictsWeight2', '0.0', 'Number of potential conflicts weight (CBS, level 2)', 'double', 23, 0, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (113, 'Placement.MPP_DeltaInitialAssignmentWeight2', '%Comparator.PerturbationPenaltyWeight%', 'Delta initial assigments weight (MPP, level 2)', 'double', 24, 0, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (114, 'Placement.NrHardStudConfsWeight2', '%Comparator.HardStudentConflictWeight%', 'Hard student conflicts weight (level 2)', 'double', 25, 0, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (115, 'Placement.NrStudConfsWeight2', '%Comparator.StudentConflictWeight%', 'Student conflicts weight (level 2)', 'double', 26, 0, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (116, 'Placement.TimePreferenceWeight2', '%Comparator.TimePreferenceWeight%', 'Time preference weight (level 2)', 'double', 27, 0, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (117, 'Placement.DeltaTimePreferenceWeight2', '0.0', 'Time preference delta weight (level 2)', 'double', 28, 0, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (118, 'Placement.ConstrPreferenceWeight2', '%Comparator.ContrPreferenceWeight%', 'Constraint preference weight (level 2)', 'double', 29, 0, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (119, 'Placement.RoomPreferenceWeight2', '%Comparator.RoomPreferenceWeight%', 'Room preference weight (level 2)', 'double', 30, 0, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (120, 'Placement.UselessSlotsWeight2', '%Comparator.UselessSlotWeight%', 'Useless slot weight (level 2)', 'double', 31, 0, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (121, 'Placement.TooBigRoomWeight2', '%Comparator.TooBigRoomWeight%', 'Too big room weight (level 2)', 'double', 32, 0, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (122, 'Placement.DistanceInstructorPreferenceWeight2', '%Comparator.DistanceInstructorPreferenceWeight%', 'Back-to-back instructor preferences weight (level 2)', 'double', 33, 0, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (123, 'Placement.DeptSpreadPenaltyWeight2', '%Comparator.DeptSpreadPenaltyWeight%', 'Department balancing: penalty of when a slot over initial allowance is used (level 2)', 'double', 34, 0, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (124, 'Placement.ThresholdKoef2', '0.1', 'Threshold koeficient (level 2)', 'double', 35, 0, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (125, 'Placement.NrAssignmentsWeight3', '0.0', 'Number of assignments weight (level 3)', 'double', 36, 0, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (221, 'Neighbour.Class', 'net.sf.cpsolver.coursett.heuristics.NeighbourSelectionWithSuggestions', 'Neighbour Selection', 'text', 7, 0, 11);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (222, 'Neighbour.SuggestionProbability', '0.1', 'Probability of using suggestions', 'double', 0, 1, 61);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (223, 'Neighbour.SuggestionTimeout', '500', 'Suggestions timeout', 'integer', 1, 1, 61);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (224, 'Neighbour.SuggestionDepth', '4', 'Suggestions depth', 'integer', 2, 1, 61);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (225, 'Neighbour.SuggestionProbabilityAllAssigned', '0.5', 'Probability of using suggestions (when all classes are assigned)', 'double', 3, 1, 61);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (241, 'General.IgnoreRoomSharing', 'false', 'Ignore Room Sharing', 'boolean', 12, 1, 2);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (126, 'Placement.NrConflictsWeight3', '0.0', 'Number of conflicts weight (level 3)', 'double', 37, 0, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (127, 'Placement.WeightedConflictsWeight3', '0.0', 'Weighted conflicts weight (CBS, level 3)', 'double', 38, 0, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (128, 'Placement.NrPotentialConflictsWeight3', '0.0', 'Number of potential conflicts weight (CBS, level 3)', 'double', 39, 0, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (129, 'Placement.MPP_DeltaInitialAssignmentWeight3', '0.0', 'Delta initial assigments weight (MPP, level 3)', 'double', 40, 0, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (130, 'Placement.NrHardStudConfsWeight3', '0.0', 'Hard student conflicts weight (level 3)', 'double', 41, 0, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (131, 'Placement.NrStudConfsWeight3', '0.0', 'Student conflicts weight (level 3)', 'double', 42, 0, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (132, 'Placement.TimePreferenceWeight3', '0.0', 'Time preference weight (level 3)', 'double', 43, 0, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (133, 'Placement.DeltaTimePreferenceWeight3', '0.0', 'Time preference delta weight (level 3)', 'double', 44, 0, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (134, 'Placement.ConstrPreferenceWeight3', '0.0', 'Constraint preference weight (level 3)', 'double', 45, 0, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (135, 'Placement.RoomPreferenceWeight3', '0.0', 'Room preference weight (level 3)', 'double', 46, 0, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (136, 'Placement.UselessSlotsWeight3', '0.0', 'Useless slot weight (level 3)', 'double', 47, 0, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (137, 'Placement.TooBigRoomWeight3', '0.0', 'Too big room weight (level 3)', 'double', 48, 0, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (138, 'Placement.DistanceInstructorPreferenceWeight3', '0.0', 'Back-to-back instructor preferences weight (level 3)', 'double', 49, 0, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (139, 'Placement.DeptSpreadPenaltyWeight3', '0.0', 'Department balancing: penalty of when a slot over initial allowance is used (level 3)', 'double', 50, 0, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (140, 'Placement.SpreadPenaltyWeight1', '0.1', 'Same subpart balancing: penalty of when a slot over initial allowance is used (level 1)', 'double', 51, 1, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (141, 'Placement.SpreadPenaltyWeight2', '%Comparator.SpreadPenaltyWeight%', 'Same subpart balancing: penalty of when a slot over initial allowance is used (level 2)', 'double', 52, 0, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (142, 'Placement.SpreadPenaltyWeight3', '0.0', 'Same subpart balancing: penalty of when a slot over initial allowance is used (level 3)', 'double', 53, 0, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (143, 'Placement.NrCommitedStudConfsWeight1', '0.5', 'Commited student conlict weight (level 1)', 'double', 54, 1, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (144, 'Placement.NrCommitedStudConfsWeight2', '%Comparator.CommitedStudentConflictWeight%', 'Commited student conlict weight (level 2)', 'double', 55, 0, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (145, 'Placement.NrCommitedStudConfsWeight3', '0.0', 'Commited student conlict weight (level 3)', 'double', 56, 0, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (146, 'SearchIntensification.IterationLimit', '100', 'Iteration limit (number of iteration after which the search is restarted to the best known solution)', 'integer', 0, 1, 13);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (147, 'SearchIntensification.ResetInterval', '5', 'Number of consecutive restarts to increase iteration limit (if this number of restarts is reached, iteration limit is increased)', 'integer', 1, 1, 13);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (148, 'SearchIntensification.MultiplyInterval', '2', 'Iteration limit incremental coefficient (when a better solution is found, iteration limit is changed back to initial)', 'integer', 2, 1, 13);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (149, 'SearchIntensification.Multiply', '2', 'Reset conflict-based statistics (number of consecutive restarts after which CBS is cleared, zero means no reset of CBS)', 'integer', 3, 1, 13);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (150, 'General.SearchIntensification', 'true', 'Use search intensification', 'boolean', 6, 1, 2);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (162, 'General.SettingsId', '-1', 'Settings Id', 'integer', 8, 0, 2);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (201, 'General.SolverWarnings', null, 'Solver Warnings', 'text', 10, 0, 2);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (202, 'General.AutoSameStudentsConstraint', 'SAME_STUDENTS', 'Automatic same student constraint', 'enum(SAME_STUDENTS,DIFF_TIME)', 11, 1, 2);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (203, 'Instructor.NoPreferenceLimit', '0.0', 'Instructor Constraint: No Preference Limit', 'double', 0, 1, 41);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (204, 'Instructor.DiscouragedLimit', '50.0', 'Instructor Constraint: Discouraged Limit', 'double', 1, 1, 41);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (205, 'Instructor.ProhibitedLimit', '200.0', 'Instructor Constraint: Prohibited Limit', 'double', 2, 1, 41);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (206, 'Student.DistanceLimit', '67.0', 'Student Conflict: Distance Limit (deprecated)', 'double', 3, 0, 41);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (207, 'Student.DistanceLimit75min', '100.0', 'Student Conflict: Distance Limit (after 75min class, deprecated)', 'double', 4, 0, 41);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (161, 'Placement.CanUnassingSingleton', 'true', 'Can unassign a singleton value', 'boolean', 57, 1, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (181, 'TimePreferences.Weight', '0.0', 'Time preferences weight', 'double', 0, 1, 21);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (182, 'TimePreferences.Pref', '2222222222222224222222222222222223333222222222222222222222222224222222222222222223333222222222222222222222222224222222222222222223333222222222222222222222222224222222222222222223333222222222222222222222222224222222222222222223333222222222222222222222', 'Time preferences', 'timepref', 1, 1, 21);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (1, 'Basic.Mode', 'Initial', 'Solver mode', 'enum(Initial,MPP)', 0, 1, 1);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (2, 'Basic.WhenFinished', 'No Action', 'When finished', 'enum(No Action,Save,Save as New,Save and Unload,Save as New and Unload)', 1, 1, 1);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (3, 'Basic.DisobeyHard', 'false', 'Allow breaking of hard constraints', 'boolean', 6, 1, 1);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (4, 'General.SwitchStudents', 'true', 'Students sectioning', 'boolean', 2, 0, 1);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (5, 'General.DeptBalancing', 'false', 'Use departmental balancing', 'boolean', 9, 1, 2);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (6, 'General.CBS', 'true', 'Use conflict-based statistics', 'boolean', 0, 1, 2);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (7, 'General.SaveBestUnassigned', '-1', 'Minimal number of unassigned variables to save best solution found (-1 always save)', 'integer', 1, 0, 2);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (9, 'General.UseDistanceConstraints', 'true', 'Use building distances', 'boolean', 2, 0, 2);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (10, 'General.Spread', 'true', 'Use same subpart balancing', 'boolean', 3, 1, 2);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (11, 'General.AutoSameStudents', 'true', 'Use automatic same_students constraints', 'boolean', 4, 1, 2);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (12, 'General.NormalizedPrefDecreaseFactor', '0.77', 'Time preference normalization decrease factor', 'double', 5, 1, 2);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (13, 'Global.LoadStudentEnrlsFromSolution', 'false', 'Load student enrollments from solution<BR>(faster, but it ignores new classes)', 'boolean', 7, 1, 2);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (14, 'DeptBalancing.SpreadFactor', '1.2', 'Initial allowance of the slots for a particular time', 'double', 0, 1, 5);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (15, 'DeptBalancing.Unassignments2Weaken', '0', 'Increase the initial allowance when it causes the given number of unassignments', 'integer', 1, 1, 5);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (16, 'Spread.SpreadFactor', '1.2', 'Initial allowance of the slots for a particular time', 'double', 0, 1, 12);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (17, 'Spread.Unassignments2Weaken', '50', 'Increase the initial allowance when it causes the given number of unassignments', 'integer', 1, 1, 12);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (18, 'ConflictStatistics.Ageing', '1.0', 'Ageing (koef)', 'double', 0, 0, 6);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (19, 'ConflictStatistics.AgeingHalfTime', '0', 'Ageing -- half time (number of iteration)', 'integer', 1, 0, 6);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (20, 'ConflictStatistics.Print', 'true', 'Print conflict statistics', 'boolean', 2, 0, 6);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (21, 'ConflictStatistics.PrintInterval', '-1', 'Number of iterations to print CBS (-1 just keep in memory and save within the solution)', 'integer', 3, 0, 6);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (22, 'PerturbationCounter.Class', 'net.sf.cpsolver.coursett.heuristics.UniversalPerturbationsCounter', 'Perturbations counter', 'text', 0, 0, 11);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (23, 'Termination.Class', 'net.sf.cpsolver.ifs.termination.MPPTerminationCondition', 'Termination condition', 'text', 1, 0, 11);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (24, 'Comparator.Class', 'net.sf.cpsolver.coursett.heuristics.TimetableComparator', 'Solution comparator', 'text', 2, 0, 11);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (25, 'Variable.Class', 'net.sf.cpsolver.coursett.heuristics.LectureSelection', 'Lecture selection', 'text', 3, 0, 11);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (26, 'Value.Class', 'net.sf.cpsolver.coursett.heuristics.PlacementSelection', 'Placement selection', 'text', 4, 0, 11);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (27, 'TimetableLoader', 'org.unitime.timetable.solver.TimetableDatabaseLoader', 'Loader class', 'text', 5, 0, 11);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (28, 'TimetableSaver', 'org.unitime.timetable.solver.TimetableDatabaseSaver', 'Saver class', 'text', 6, 0, 11);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (29, 'Perturbations.DifferentPlacement', '0.0', 'Different value than initial is assigned', 'double', 0, 1, 4);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (30, 'Perturbations.AffectedStudentWeight', '0.1', 'Number of students which are enrolled in a class which is placed to a different location than initial', 'double', 1, 1, 4);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (32, 'Perturbations.AffectedInstructorWeight', '0.0', 'Number of classes which are placed to a different room than initial', 'double', 3, 1, 4);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (33, 'Perturbations.AffectedInstructorWeight', '0.0', 'Number of classes which are placed to a different room than initial', 'double', 4, 1, 4);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (34, 'Perturbations.DifferentRoomWeight', '0.0', 'Number of classes which are placed to a different room than initial', 'double', 5, 1, 4);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (35, 'Perturbations.DifferentBuildingWeight', '0.0', 'Number of classes which are placed to a different building than initial', 'double', 6, 1, 4);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (36, 'Perturbations.DifferentTimeWeight', '0.0', 'Number of classes which are placed in a different time than initial', 'double', 7, 1, 4);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (37, 'Perturbations.DifferentDayWeight', '0.0', 'Number of classes which are placed in a different days than initial', 'double', 8, 1, 4);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (38, 'Perturbations.DifferentHourWeight', '0.0', 'Number of classes which are placed in a different hours than initial', 'double', 9, 1, 4);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (39, 'Perturbations.DeltaStudentConflictsWeight', '0.0', 'Difference of student conflicts of classes assigned to current placements instead of initial placements', 'double', 10, 1, 4);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (40, 'Perturbations.NewStudentConflictsWeight', '0.0', 'New created student conflicts -- particular students are taken into account', 'double', 11, 1, 4);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (41, 'Perturbations.TooFarForInstructorsWeight', '0.0', 'New placement of a class is too far from the intial placement (instructor-wise)', 'double', 12, 1, 4);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (42, 'Perturbations.TooFarForStudentsWeight', '0.0', 'New placement of a class is too far from the intial placement (student-wise)', 'double', 13, 1, 4);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (43, 'Perturbations.DeltaInstructorDistancePreferenceWeight', '0.0', 'Difference between number of instructor distance preferences of the initial ', 'double', 14, 1, 4);
commit;
prompt 100 records committed...
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (44, 'Perturbations.DeltaRoomPreferenceWeight', '0.0', 'Difference between room preferences of the initial and the current solution', 'double', 15, 1, 4);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (45, 'Perturbations.DeltaTimePreferenceWeight', '0.0', 'Difference between time preferences of the initial and the current solution', 'double', 16, 1, 4);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (46, 'Perturbations.AffectedStudentByTimeWeight', '0.0', 'Number of students which are enrolled in a class which is placed to a different time than initial', 'double', 17, 1, 4);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (47, 'Perturbations.AffectedInstructorByTimeWeight', '0.0', 'Number of instructors which are assigned to classes which are placed to different time than initial', 'double', 18, 1, 4);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (48, 'Perturbations.AffectedStudentByRoomWeight', '0.0', 'Number of students which are enrolled in a class which is placed to a different room than initial', 'double', 19, 1, 4);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (49, 'Perturbations.AffectedInstructorByRoomWeight', '0.0', 'Number of instructors which are assigned to classes which are placed to different room than initial', 'double', 20, 1, 4);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (50, 'Perturbations.AffectedStudentByBldgWeight', '0.0', 'Number of students which are enrolled in a class which is placed to a different building than initial', 'double', 21, 1, 4);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (51, 'Perturbations.AffectedInstructorByBldgWeight', '0.0', 'Number of instructors which are assigned to classes which are placed to different building than initial', 'double', 22, 1, 4);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (52, 'Termination.MinPerturbances', '-1', 'Minimal allowed number of perturbances (-1 not use)', 'integer', 0, 0, 7);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (53, 'Termination.MaxIters', '-1', 'Maximal number of iteration', 'integer', 1, 0, 7);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (54, 'Termination.TimeOut', '1800', 'Maximal solver time (in sec)', 'integer', 2, 1, 7);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (55, 'Termination.StopWhenComplete', 'false', 'Stop computation when a complete solution is found', 'boolean', 3, 1, 7);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (56, 'Comparator.HardStudentConflictWeight', '0.8', 'Weight of hard student conflict', 'double', 0, 1, 8);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (57, 'Comparator.StudentConflictWeight', '0.2', 'Weight of student conflict', 'double', 1, 1, 8);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (58, 'Comparator.TimePreferenceWeight', '0.3', 'Time preferences weight', 'double', 2, 1, 8);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (59, 'Comparator.ContrPreferenceWeight', '2.0', 'Distribution preferences weight', 'double', 3, 1, 8);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (60, 'Comparator.RoomPreferenceWeight', '1.0', 'Room preferences weight', 'double', 4, 1, 8);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (61, 'Comparator.UselessSlotWeight', '0.1', 'Useless slots weight', 'double', 5, 1, 8);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (62, 'Comparator.TooBigRoomWeight', '0.1', 'Too big room weight', 'double', 6, 1, 8);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (63, 'Comparator.DistanceInstructorPreferenceWeight', '1.0', 'Back-to-back instructor preferences weight', 'double', 7, 1, 8);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (64, 'Comparator.PerturbationPenaltyWeight', '1.0', 'Perturbation penalty weight', 'double', 8, 1, 8);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (65, 'Comparator.DeptSpreadPenaltyWeight', '1.0', 'Department balancing weight', 'double', 9, 1, 8);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (66, 'Comparator.SpreadPenaltyWeight', '1.0', 'Same subpart balancing weight', 'double', 10, 1, 8);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (67, 'Comparator.CommitedStudentConflictWeight', '1.0', 'Commited student conflict weight', 'double', 11, 1, 8);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (68, 'Lecture.RouletteWheelSelection', 'true', 'Roulette wheel selection', 'boolean', 0, 0, 9);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (69, 'Lecture.RandomWalkProb', '1.0', 'Random walk probability', 'double', 1, 0, 9);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (70, 'Lecture.DomainSizeWeight', '30.0', 'Domain size weight', 'double', 2, 0, 9);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (71, 'Lecture.NrAssignmentsWeight', '10.0', 'Number of assignments weight', 'double', 3, 0, 9);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (72, 'Lecture.InitialAssignmentWeight', '20.0', 'Initial assignment weight', 'double', 4, 0, 9);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (73, 'Lecture.NrConstraintsWeight', '0.0', 'Number of constraint weight', 'double', 5, 0, 9);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (74, 'Lecture.HardStudentConflictWeight', '%Comparator.HardStudentConflictWeight%', 'Hard student conflict weight', 'double', 6, 0, 9);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (75, 'Lecture.StudentConflictWeight', '%Comparator.StudentConflictWeight%', 'Student conflict weight', 'double', 7, 0, 9);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (76, 'Lecture.TimePreferenceWeight', '%Comparator.TimePreferenceWeight%', 'Time preference weight', 'double', 8, 0, 9);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (77, 'Lecture.ContrPreferenceWeight', '%Comparator.ContrPreferenceWeight%', 'Constraint preference weight', 'double', 9, 0, 9);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (78, 'Lecture.RoomPreferenceWeight', '%Comparator.RoomPreferenceWeight%', 'Room preference weight', 'double', 10, 0, 9);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (79, 'Lecture.UselessSlotWeight', '%Comparator.UselessSlotWeight%', 'Useless slot weight', 'double', 11, 0, 9);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (81, 'Lecture.TooBigRoomWeight', '%Comparator.TooBigRoomWeight%', 'Too big room weight', 'double', 12, 0, 9);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (82, 'Lecture.DistanceInstructorPreferenceWeight', '%Comparator.DistanceInstructorPreferenceWeight%', 'Back-to-back instructor preferences weight', 'double', 13, 0, 9);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (83, 'Lecture.DeptSpreadPenaltyWeight', '%Comparator.DeptSpreadPenaltyWeight%', 'Department balancing weight', 'double', 14, 0, 9);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (84, 'Lecture.SelectionSubSet', 'true', 'Selection among subset of lectures (faster)', 'boolean', 15, 0, 9);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (85, 'Lecture.SelectionSubSetMinSize', '10', 'Minimal subset size', 'integer', 16, 0, 9);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (86, 'Lecture.SelectionSubSetPart', '0.2', 'Subset size in percentage of all lectures available for selection', 'double', 17, 0, 9);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (87, 'Lecture.SpreadPenaltyWeight', '%Comparator.SpreadPenaltyWeight%', 'Same subpart balancing weight', 'double', 18, 0, 9);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (88, 'Lecture.CommitedStudentConflictWeight', '%Comparator.CommitedStudentConflictWeight%', 'Commited student conflict weight', 'double', 19, 0, 9);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (89, 'Placement.RandomWalkProb', '0.00', 'Random walk probability', 'double', 0, 1, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (90, 'Placement.MPP_InitialProb', '0.20', 'MPP initial selection probability ', 'double', 1, 1, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (91, 'Placement.MPP_Limit', '-1', 'MPP limit (-1 for no limit)', 'integer', 2, 1, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (92, 'Placement.MPP_PenaltyLimit', '-1.0', 'Limit of the perturbations penalty (-1 for no limit)', 'double', 3, 1, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (93, 'Placement.NrAssignmentsWeight1', '0.0', 'Number of assignments weight (level 1)', 'double', 4, 0, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (94, 'Placement.NrConflictsWeight1', '1.0', 'Number of conflicts weight (level 1)', 'double', 5, 1, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (95, 'Placement.WeightedConflictsWeight1', '2.0', 'Weighted conflicts weight (CBS, level 1)', 'double', 6, 1, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (96, 'Placement.NrPotentialConflictsWeight1', '0.0', 'Number of potential conflicts weight (CBS, level 1)', 'double', 7, 0, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (97, 'Placement.MPP_DeltaInitialAssignmentWeight1', '0.1', 'Delta initial assigments weight (MPP, level 1)', 'double', 8, 1, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (98, 'Placement.NrHardStudConfsWeight1', '0.3', 'Hard student conflicts weight (level 1)', 'double', 9, 1, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (99, 'Placement.NrStudConfsWeight1', '0.05', 'Student conflicts weight (level 1)', 'double', 10, 1, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (100, 'Placement.TimePreferenceWeight1', '0.0', 'Time preference weight (level 1)', 'double', 11, 0, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (101, 'Placement.DeltaTimePreferenceWeight1', '0.2', 'Time preference delta weight (level 1)', 'double', 12, 1, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (102, 'Placement.ConstrPreferenceWeight1', '0.25', 'Constraint preference weight (level 1)', 'double', 13, 1, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (103, 'Placement.RoomPreferenceWeight1', '0.1', 'Room preference weight (level 1)', 'double', 14, 1, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (104, 'Placement.UselessSlotsWeight1', '0.0', 'Useless slot weight (level 1)', 'double', 15, 1, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (105, 'Placement.TooBigRoomWeight1', '0.01', 'Too big room weight (level 1)', 'double', 16, 1, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (106, 'Placement.DistanceInstructorPreferenceWeight1', '0.1', 'Back-to-back instructor preferences weight (level 1)', 'double', 17, 1, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (107, 'Placement.DeptSpreadPenaltyWeight1', '0.1', 'Department balancing: penalty of when a slot over initial allowance is used (level 1)', 'double', 18, 1, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (108, 'Placement.ThresholdKoef1', '0.1', 'Threshold koeficient (level 1)', 'double', 19, 1, 10);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (261, 'OnFlySectioning.Enabled', 'false', 'Enable on fly sectioning (if enabled, students will be resectioned after each iteration)', 'boolean', 0, 1, 81);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (262, 'OnFlySectioning.Recursive', 'true', 'Recursively resection lectures affected by a student swap', 'boolean', 1, 1, 81);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (263, 'OnFlySectioning.ConfigAsWell', 'false', 'Resection students between configurations as well', 'boolean', 2, 1, 81);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (264, 'ExamBasic.Mode', 'Initial', 'Solver mode', 'enum(Initial,MPP)', 0, 1, 82);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (265, 'ExamBasic.WhenFinished', 'No Action', 'When finished', 'enum(No Action,Save,Save and Unload)', 1, 1, 82);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (266, 'Exams.MaxRooms', '4', 'Default number of room splits per exam', 'integer', 0, 1, 83);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (267, 'Exams.IsDayBreakBackToBack', 'false', 'Consider back-to-back over day break', 'boolean', 1, 1, 83);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (268, 'Exams.DirectConflictWeight', '1000.0', 'Direct conflict weight', 'double', 2, 1, 83);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (269, 'Exams.MoreThanTwoADayWeight', '100.0', 'Three or more exams a day conflict weight', 'double', 3, 1, 83);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (270, 'Exams.BackToBackConflictWeight', '10.0', 'Back-to-back conflict weight', 'double', 4, 1, 83);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (271, 'Exams.DistanceBackToBackConflictWeight', '25.0', 'Distance back-to-back conflict weight', 'double', 5, 1, 83);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (272, 'Exams.BackToBackDistance', '-1', 'Back-to-back distance (-1 means disabled)', 'double', 6, 1, 83);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (273, 'Exams.PeriodWeight', '1.0', 'Period preference weight', 'double', 7, 1, 83);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (274, 'Exams.RoomWeight', '1.0', 'Room preference weight', 'double', 8, 1, 83);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (275, 'Exams.DistributionWeight', '1.0', 'Distribution preference weight', 'double', 9, 1, 83);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (276, 'Exams.RoomSplitWeight', '10.0', 'Room split weight', 'double', 10, 1, 83);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (277, 'Exams.RoomSizeWeight', '0.001', 'Excessive room size weight', 'double', 11, 1, 83);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (279, 'Exams.RotationWeight', '0.001', 'Exam rotation weight', 'double', 12, 1, 83);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (280, 'Neighbour.Class', 'net.sf.cpsolver.exam.heuristics.ExamNeighbourSelection', 'Examination timetabling neighbour selection class', 'text', 0, 0, 84);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (281, 'Termination.TimeOut', '1800', 'Maximal solver time (in sec)', 'integer', 1, 1, 84);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (282, 'Exam.Algorithm', 'Great Deluge', 'Used heuristics', 'enum(Great Deluge,Simulated Annealing)', 2, 1, 84);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (283, 'HillClimber.MaxIdle', '25000', 'Hill Climber: maximal idle iteration', 'integer', 3, 1, 84);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (284, 'Termination.StopWhenComplete', 'false', 'Stop when a complete solution if found', 'boolean', 4, 0, 84);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (285, 'General.SaveBestUnassigned', '-1', 'Save best when x unassigned', 'integer', 5, 0, 84);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (286, 'GreatDeluge.CoolRate', '0.99999995', 'Cooling rate', 'double', 0, 1, 85);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (287, 'GreatDeluge.UpperBoundRate', '1.05', 'Upper bound rate', 'double', 1, 1, 85);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (288, 'GreatDeluge.LowerBoundRate', '0.95', 'Lower bound rate', 'double', 2, 1, 85);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (289, 'SimulatedAnnealing.InitialTemperature', '1.5', 'Initial temperature', 'double', 0, 1, 86);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (290, 'SimulatedAnnealing.CoolingRate', '0.95', 'Cooling rate', 'double', 1, 1, 86);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (291, 'SimulatedAnnealing.TemperatureLength', '25000', 'Temperature length', 'integer', 2, 1, 86);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (292, 'SimulatedAnnealing.ReheatLengthCoef', '5', 'Reheat length coefficient', 'double', 3, 1, 86);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (301, 'Exams.InstructorDirectConflictWeight', '0.0', 'Direct instructor conflict weight', 'double', 13, 1, 83);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (302, 'Exams.InstructorMoreThanTwoADayWeight', '0.0', 'Three or more exams a day instructor conflict weight', 'double', 14, 1, 83);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (303, 'Exams.InstructorBackToBackConflictWeight', '0.0', 'Back-to-back instructor conflict weight', 'double', 15, 1, 83);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (304, 'Exams.InstructorDistanceBackToBackConflictWeight', '0.0', 'Distance back-to-back instructor conflict weight', 'double', 16, 1, 83);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (305, 'Exams.PerturbationWeight', '0.001', 'Perturbation penalty weight', 'double', 17, 1, 83);
commit;
prompt 200 records committed...
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (321, 'Exams.RoomSplitDistanceWeight', '0.01', 'If an examination in split between two or more rooms, weight for an average distance between these rooms', 'double', 18, 1, 83);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (322, 'Exams.LargeSize', '-1', 'Large Exam Penalty: minimal size of a large exam (disabled if -1)', 'integer', 19, 1, 83);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (323, 'Exams.LargePeriod', '0.67', 'Large Exam Penalty: first discouraged period = number of periods x this factor', 'double', 20, 1, 83);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (324, 'Exams.LargeWeight', '1.0', 'Large Exam Penalty: weight of a large exam that is assigned on or after the first discouraged period', 'double', 21, 1, 83);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (325, 'StudentSctBasic.Mode', 'Initial', 'Solver mode', 'enum(Initial,MPP)', 0, 1, 101);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (326, 'StudentSctBasic.WhenFinished', 'No Action', 'When finished', 'enum(No Action,Save,Save and Unload)', 1, 1, 101);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (327, 'Termination.Class', 'net.sf.cpsolver.ifs.termination.GeneralTerminationCondition', 'Student sectioning termination class', 'text', 0, 0, 102);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (328, 'Termination.StopWhenComplete', 'true', 'Stop when a complete solution if found', 'boolean', 1, 1, 102);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (329, 'Termination.TimeOut', '28800', 'Maximal solver time (in sec)', 'integer', 2, 1, 102);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (330, 'Comparator.Class', 'net.sf.cpsolver.ifs.solution.GeneralSolutionComparator', 'Student sectioning solution comparator class', 'text', 3, 0, 102);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (331, 'Value.Class', 'net.sf.cpsolver.studentsct.heuristics.EnrollmentSelection', 'Student sectioning value selection class', 'text', 4, 0, 102);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (332, 'Value.WeightConflicts', '1.0', 'CBS weight', 'double', 5, 0, 102);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (333, 'Value.WeightNrAssignments', '0.0', 'Number of past assignments weight', 'double', 6, 0, 102);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (334, 'Variable.Class', 'net.sf.cpsolver.ifs.heuristics.GeneralVariableSelection', 'Student sectioning variable selection class', 'text', 7, 0, 102);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (335, 'Neighbour.Class', 'net.sf.cpsolver.studentsct.heuristics.StudentSctNeighbourSelection', 'Student sectioning neighbour selection class', 'text', 8, 0, 102);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (336, 'General.SaveBestUnassigned', '-1', 'Save best even when no complete solution is found', 'integer', 9, 0, 102);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (337, 'StudentSct.StudentDist', 'true', 'Use student distance conflicts', 'boolean', 10, 1, 102);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (338, 'StudentSct.CBS', 'true', 'Use conflict-based statistics', 'boolean', 11, 1, 102);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (339, 'Load.IncludeCourseDemands', 'true', 'Load real student requests', 'boolean', 12, 0, 102);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (340, 'Load.IncludeLastLikeStudents', 'false', 'Load last-like  course demands', 'boolean', 13, 0, 102);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (341, 'SectionLimit.PreferDummyStudents', 'true', 'Section limit constraint: favour unassignment of last-like course requests', 'boolean', 14, 0, 102);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (342, 'Student.DummyStudentWeight', '0.01', 'Last-like student request weight', 'double', 15, 1, 102);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (343, 'Neighbour.BranchAndBoundMinimizePenalty', 'false', 'Branch&bound: If true, section penalties (instead of section values) are minimized', 'boolean', 16, 0, 102);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (344, 'Neighbour.BranchAndBoundTimeout', '5000', 'Branch&bound: Timeout for each neighbour selection (in milliseconds)', 'integer', 17, 1, 102);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (345, 'Neighbour.RandomUnassignmentProb', '0.5', 'Random Unassignment: Probability of a random selection of a student', 'double', 18, 1, 102);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (346, 'Neighbour.RandomUnassignmentOfProblemStudentProb', '0.9', 'Random Unassignment: Probability of a random selection of a problematic student', 'double', 19, 1, 102);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (347, 'Neighbour.SwapStudentsTimeout', '5000', 'Student Swap: Timeout for each neighbour selection (in milliseconds)', 'integer', 20, 1, 102);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (348, 'Neighbour.SwapStudentsMaxValues', '100', 'Student Swap: Limit for the number of considered values for each course request', 'integer', 21, 1, 102);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (349, 'Neighbour.MaxValues', '100', 'Backtrack: Limit on the number of enrollments to be visited of each course request', 'integer', 22, 1, 102);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (350, 'Neighbour.BackTrackTimeout', '5000', 'Backtrack: Timeout for each neighbour selection (in milliseconds)', 'integer', 23, 1, 102);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (351, 'Neighbour.BackTrackDepth', '4', 'Backtrack: Search depth', 'integer', 24, 1, 102);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (352, 'CourseRequest.SameTimePrecise', 'true', 'More precise (but slower) computation of enrollments of a course request while skipping enrollments with the same times', 'boolean', 25, 0, 102);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (361, 'Exams.PeriodSizeWeight', '1.0', 'Examination period x examination size weight', 'double', 22, 1, 83);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (362, 'Exams.PeriodIndexWeight', '0.0000001', 'Examination period index weight', 'double', 23, 1, 83);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (363, 'Exams.RoomPerturbationWeight', '0.1', 'Room perturbation penalty (change of room) weight', 'double', 24, 1, 83);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (364, 'Comparator.Class', 'net.sf.cpsolver.ifs.solution.GeneralSolutionComparator', 'Examination solution comparator class', 'text', 6, 0, 84);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (365, 'General.IgnoreCommittedStudentConflicts', 'false', 'Do not load committed student conflicts (deprecated)', 'boolean', 13, 0, 2);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (366, 'General.WeightStudents', 'true', 'Weight last-like students (deprecated)', 'boolean', 14, 0, 2);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (367, 'Curriculum.StudentCourseDemadsClass', 'Projected Student Course Demands', 'Student course demands', 'enum(Last Like Student Course Demands,Weighted Last Like Student Course Demands,Projected Student Course Demands,Curricula Course Demands,Curricula Last Like Course Demands,Enrolled Student Course Demands)', 4, 1, 1);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (368, 'General.CommittedStudentConflicts', 'Load', 'Committed student conflicts', 'enum(Load,Compute,Ignore)', 5, 1, 1);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (369, 'General.LoadCommittedAssignments', 'false', 'Load committed assignments', 'boolean', 3, 1, 1);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (370, 'Distances.Ellipsoid', 'DEFAULT', 'Ellipsoid to be used to compute distances', 'enum(DEFAULT,LEGACY,WGS84,GRS80,Airy1830,Intl1924,Clarke1880,GRS67)', 5, 1, 41);
insert into SOLVER_PARAMETER_DEF (uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id)
values (371, 'Distances.Speed', '67.0', 'Student speed in meters per minute', 'double', 6, 1, 41);
commit;
prompt 243 records loaded
prompt Loading SOLVER_PREDEF_SETTING...
insert into SOLVER_PREDEF_SETTING (uniqueid, name, description, appearance)
values (1, 'Default.Interactive', 'Interactive', 0);
insert into SOLVER_PREDEF_SETTING (uniqueid, name, description, appearance)
values (2, 'Default.Validate', 'Validate', 1);
insert into SOLVER_PREDEF_SETTING (uniqueid, name, description, appearance)
values (3, 'Default.Check', 'Check', 1);
insert into SOLVER_PREDEF_SETTING (uniqueid, name, description, appearance)
values (4, 'Default.Solver', 'Default', 1);
insert into SOLVER_PREDEF_SETTING (uniqueid, name, description, appearance)
values (101, 'Exam.Default', 'Default', 2);
insert into SOLVER_PREDEF_SETTING (uniqueid, name, description, appearance)
values (121, 'StudentSct.Default', 'Default', 3);
commit;
prompt 6 records loaded
prompt Loading SOLVER_PARAMETER...
insert into SOLVER_PARAMETER (uniqueid, value, solver_param_def_id, solution_id, solver_predef_setting_id)
values (24921, '300', 54, null, 3);
insert into SOLVER_PARAMETER (uniqueid, value, solver_param_def_id, solution_id, solver_predef_setting_id)
values (1701, 'false', 10, null, 3);
insert into SOLVER_PARAMETER (uniqueid, value, solver_param_def_id, solution_id, solver_predef_setting_id)
values (1, 'MPP', 1, null, 1);
insert into SOLVER_PARAMETER (uniqueid, value, solver_param_def_id, solution_id, solver_predef_setting_id)
values (2, 'on', 3, null, 1);
insert into SOLVER_PARAMETER (uniqueid, value, solver_param_def_id, solution_id, solver_predef_setting_id)
values (3, 'false', 4, null, 1);
insert into SOLVER_PARAMETER (uniqueid, value, solver_param_def_id, solution_id, solver_predef_setting_id)
values (5, '0', 17, null, 1);
insert into SOLVER_PARAMETER (uniqueid, value, solver_param_def_id, solution_id, solver_predef_setting_id)
values (6, 'false', 4, null, 2);
insert into SOLVER_PARAMETER (uniqueid, value, solver_param_def_id, solution_id, solver_predef_setting_id)
values (7, 'Save and Unload', 2, null, 2);
insert into SOLVER_PARAMETER (uniqueid, value, solver_param_def_id, solution_id, solver_predef_setting_id)
values (8, '0', 54, null, 2);
insert into SOLVER_PARAMETER (uniqueid, value, solver_param_def_id, solution_id, solver_predef_setting_id)
values (11, '0.0', 56, null, 3);
insert into SOLVER_PARAMETER (uniqueid, value, solver_param_def_id, solution_id, solver_predef_setting_id)
values (12, '0.0', 57, null, 3);
insert into SOLVER_PARAMETER (uniqueid, value, solver_param_def_id, solution_id, solver_predef_setting_id)
values (13, '0.0', 58, null, 3);
insert into SOLVER_PARAMETER (uniqueid, value, solver_param_def_id, solution_id, solver_predef_setting_id)
values (14, '0.0', 59, null, 3);
insert into SOLVER_PARAMETER (uniqueid, value, solver_param_def_id, solution_id, solver_predef_setting_id)
values (15, '0.0', 60, null, 3);
insert into SOLVER_PARAMETER (uniqueid, value, solver_param_def_id, solution_id, solver_predef_setting_id)
values (16, '0.0', 61, null, 3);
insert into SOLVER_PARAMETER (uniqueid, value, solver_param_def_id, solution_id, solver_predef_setting_id)
values (17, '0.0', 62, null, 3);
insert into SOLVER_PARAMETER (uniqueid, value, solver_param_def_id, solution_id, solver_predef_setting_id)
values (24922, 'false', 4, null, 3);
insert into SOLVER_PARAMETER (uniqueid, value, solver_param_def_id, solution_id, solver_predef_setting_id)
values (18, '0.0', 63, null, 3);
insert into SOLVER_PARAMETER (uniqueid, value, solver_param_def_id, solution_id, solver_predef_setting_id)
values (19, '0.0', 64, null, 3);
insert into SOLVER_PARAMETER (uniqueid, value, solver_param_def_id, solution_id, solver_predef_setting_id)
values (20, '0.0', 65, null, 3);
insert into SOLVER_PARAMETER (uniqueid, value, solver_param_def_id, solution_id, solver_predef_setting_id)
values (21, '0.0', 66, null, 3);
insert into SOLVER_PARAMETER (uniqueid, value, solver_param_def_id, solution_id, solver_predef_setting_id)
values (22, '0.0', 67, null, 3);
insert into SOLVER_PARAMETER (uniqueid, value, solver_param_def_id, solution_id, solver_predef_setting_id)
values (24, '1.0', 94, null, 3);
insert into SOLVER_PARAMETER (uniqueid, value, solver_param_def_id, solution_id, solver_predef_setting_id)
values (25, '1.0', 95, null, 3);
insert into SOLVER_PARAMETER (uniqueid, value, solver_param_def_id, solution_id, solver_predef_setting_id)
values (27, '0.0', 97, null, 3);
insert into SOLVER_PARAMETER (uniqueid, value, solver_param_def_id, solution_id, solver_predef_setting_id)
values (28, '0.0', 98, null, 3);
insert into SOLVER_PARAMETER (uniqueid, value, solver_param_def_id, solution_id, solver_predef_setting_id)
values (29, '0.0', 99, null, 3);
insert into SOLVER_PARAMETER (uniqueid, value, solver_param_def_id, solution_id, solver_predef_setting_id)
values (31, '0.0', 101, null, 3);
insert into SOLVER_PARAMETER (uniqueid, value, solver_param_def_id, solution_id, solver_predef_setting_id)
values (32, '0.0', 102, null, 3);
insert into SOLVER_PARAMETER (uniqueid, value, solver_param_def_id, solution_id, solver_predef_setting_id)
values (33, '0.0', 103, null, 3);
insert into SOLVER_PARAMETER (uniqueid, value, solver_param_def_id, solution_id, solver_predef_setting_id)
values (34, '0.0', 104, null, 3);
insert into SOLVER_PARAMETER (uniqueid, value, solver_param_def_id, solution_id, solver_predef_setting_id)
values (35, '0.0', 105, null, 3);
insert into SOLVER_PARAMETER (uniqueid, value, solver_param_def_id, solution_id, solver_predef_setting_id)
values (36, '0.0', 106, null, 3);
insert into SOLVER_PARAMETER (uniqueid, value, solver_param_def_id, solution_id, solver_predef_setting_id)
values (37, '0.0', 107, null, 3);
insert into SOLVER_PARAMETER (uniqueid, value, solver_param_def_id, solution_id, solver_predef_setting_id)
values (38, '0.0', 140, null, 3);
insert into SOLVER_PARAMETER (uniqueid, value, solver_param_def_id, solution_id, solver_predef_setting_id)
values (39, '0.0', 143, null, 3);
insert into SOLVER_PARAMETER (uniqueid, value, solver_param_def_id, solution_id, solver_predef_setting_id)
values (40, '0.0', 108, null, 3);
insert into SOLVER_PARAMETER (uniqueid, value, solver_param_def_id, solution_id, solver_predef_setting_id)
values (20974, 'DIFF_TIME', 202, null, 1);
insert into SOLVER_PARAMETER (uniqueid, value, solver_param_def_id, solution_id, solver_predef_setting_id)
values (20975, 'on', 55, null, 3);
insert into SOLVER_PARAMETER (uniqueid, value, solver_param_def_id, solution_id, solver_predef_setting_id)
values (90439, '20.0', 99, null, 4);
insert into SOLVER_PARAMETER (uniqueid, value, solver_param_def_id, solution_id, solver_predef_setting_id)
values (91742, '7.6', 56, null, 4);
insert into SOLVER_PARAMETER (uniqueid, value, solver_param_def_id, solution_id, solver_predef_setting_id)
values (91743, '2.4', 57, null, 4);
insert into SOLVER_PARAMETER (uniqueid, value, solver_param_def_id, solution_id, solver_predef_setting_id)
values (94676, 'on', 261, null, 4);
insert into SOLVER_PARAMETER (uniqueid, value, solver_param_def_id, solution_id, solver_predef_setting_id)
values (94677, 'on', 13, null, 4);
commit;
prompt 44 records loaded
prompt Loading STAFF...
prompt Table is empty
prompt Loading STANDARD_EVENT_NOTE...
prompt Table is empty
prompt Loading STUDENT_ACAD_AREA...
prompt Table is empty
prompt Loading STUDENT_ACCOMODATION...
prompt Table is empty
prompt Loading STUDENT_CLASS_ENRL...
prompt Table is empty
prompt Loading STUDENT_ENRL...
prompt Table is empty
prompt Loading STUDENT_ENRL_MSG...
prompt Table is empty
prompt Loading STUDENT_GROUP...
prompt Table is empty
prompt Loading STUDENT_GROUP_RESERVATION...
prompt Table is empty
prompt Loading STUDENT_MAJOR...
prompt Table is empty
prompt Loading STUDENT_MINOR...
prompt Table is empty
prompt Loading STUDENT_TO_ACOMODATION...
prompt Table is empty
prompt Loading STUDENT_TO_GROUP...
prompt Table is empty
prompt Loading TIME_PATTERN_DAYS...
prompt Table is empty
prompt Loading TIME_PATTERN_DEPT...
prompt Table is empty
prompt Loading TIME_PATTERN_TIME...
prompt Table is empty
prompt Loading TIME_PREF...
prompt Table is empty
prompt Loading TMTBL_MGR_TO_ROLES...
insert into TMTBL_MGR_TO_ROLES (manager_id, role_id, uniqueid, is_primary, receive_emails)
values (470, 1, 510, 1, 1);
commit;
prompt 1 records loaded
prompt Loading USERS...
insert into USERS (username, password, external_uid)
values ('admin', 'ISMvKXpXpadDiUoOSoAfww==', '1');
commit;
prompt 1 records loaded
prompt Loading USER_DATA...
prompt Table is empty
prompt Loading WAITLIST...
prompt Table is empty
prompt Loading XCONFLICT...
prompt Table is empty
prompt Loading XCONFLICT_EXAM...
prompt Table is empty
prompt Loading XCONFLICT_INSTRUCTOR...
prompt Table is empty
prompt Loading XCONFLICT_STUDENT...
prompt Table is empty
prompt Enabling foreign key constraints for DATE_PATTERN...
alter table DATE_PATTERN enable constraint FK_DATE_PATTERN_SESSION;
prompt Enabling foreign key constraints for SESSIONS...
alter table SESSIONS enable constraint FK_SESSIONS_STATUS_TYPE;
alter table SESSIONS enable constraint FK_SESSION_DATEPATT;
prompt Enabling foreign key constraints for ACADEMIC_AREA...
alter table ACADEMIC_AREA enable constraint FK_ACADEMIC_AREA_SESSION;
prompt Enabling foreign key constraints for ACADEMIC_CLASSIFICATION...
alter table ACADEMIC_CLASSIFICATION enable constraint FK_ACAD_CLASS_SESSION;
prompt Enabling foreign key constraints for ACAD_AREA_RESERVATION...
alter table ACAD_AREA_RESERVATION enable constraint FK_ACAD_AREA_RESV_ACAD_AREA;
alter table ACAD_AREA_RESERVATION enable constraint FK_ACAD_AREA_RESV_ACAD_CLASS;
alter table ACAD_AREA_RESERVATION enable constraint FK_ACAD_AREA_RESV_TYPE;
prompt Enabling foreign key constraints for INSTRUCTIONAL_OFFERING...
alter table INSTRUCTIONAL_OFFERING enable constraint FK_INSTR_OFFR_CONSENT_TYPE;
prompt Enabling foreign key constraints for INSTR_OFFERING_CONFIG...
alter table INSTR_OFFERING_CONFIG enable constraint FK_INSTR_OFFR_CFG_INSTR_OFFR;
prompt Enabling foreign key constraints for SCHEDULING_SUBPART...
alter table SCHEDULING_SUBPART enable constraint FK_SCHED_SUBPART_CONFIG;
alter table SCHEDULING_SUBPART enable constraint FK_SCHED_SUBPART_DATE_PATTERN;
alter table SCHEDULING_SUBPART enable constraint FK_SCHED_SUBPART_ITYPE;
alter table SCHEDULING_SUBPART enable constraint FK_SCHED_SUBPART_PARENT;
prompt Enabling foreign key constraints for CLASS_...
alter table CLASS_ enable constraint FK_CLASS_DATEPATT;
alter table CLASS_ enable constraint FK_CLASS_PARENT;
alter table CLASS_ enable constraint FK_CLASS_SCHEDULING_SUBPART;
prompt Enabling foreign key constraints for SOLVER_GROUP...
alter table SOLVER_GROUP enable constraint FK_SOLVER_GROUP_SESSION;
prompt Enabling foreign key constraints for SOLUTION...
alter table SOLUTION enable constraint FK_SOLUTION_OWNER;
prompt Enabling foreign key constraints for TIME_PATTERN...
alter table TIME_PATTERN enable constraint FK_TIME_PATTERN_SESSION;
prompt Enabling foreign key constraints for ASSIGNMENT...
alter table ASSIGNMENT enable constraint FK_ASSIGNMENT_CLASS;
alter table ASSIGNMENT enable constraint FK_ASSIGNMENT_SOLUTION;
alter table ASSIGNMENT enable constraint FK_ASSIGNMENT_TIME_PATTERN;
prompt Enabling foreign key constraints for DEPARTMENT...
alter table DEPARTMENT enable constraint FK_DEPARTMENT_SOLVER_GROUP;
alter table DEPARTMENT enable constraint FK_DEPARTMENT_STATUS_TYPE;
prompt Enabling foreign key constraints for DEPARTMENTAL_INSTRUCTOR...
alter table DEPARTMENTAL_INSTRUCTOR enable constraint FK_DEPT_INSTR_DEPT;
alter table DEPARTMENTAL_INSTRUCTOR enable constraint FK_DEPT_INSTR_POS_CODE_TYPE;
prompt Enabling foreign key constraints for ASSIGNED_INSTRUCTORS...
alter table ASSIGNED_INSTRUCTORS enable constraint FK_ASSIGNED_INSTRS_ASSIGNMENT;
alter table ASSIGNED_INSTRUCTORS enable constraint FK_ASSIGNED_INSTRS_INSTRUCTOR;
prompt Enabling foreign key constraints for ASSIGNED_ROOMS...
alter table ASSIGNED_ROOMS enable constraint FK_ASSIGNED_ROOMS_ASSIGNMENT;
prompt Enabling foreign key constraints for BUILDING...
alter table BUILDING enable constraint FK_BUILDING_SESSION;
prompt Enabling foreign key constraints for BUILDING_PREF...
alter table BUILDING_PREF enable constraint FK_BUILDING_PREF_BLDG;
alter table BUILDING_PREF enable constraint FK_BUILDING_PREF_LEVEL;
prompt Enabling foreign key constraints for CLASS_INSTRUCTOR...
alter table CLASS_INSTRUCTOR enable constraint FK_CLASS_INSTRUCTOR_CLASS;
alter table CLASS_INSTRUCTOR enable constraint FK_CLASS_INSTRUCTOR_INSTR;
prompt Enabling foreign key constraints for STUDENT...
alter table STUDENT enable constraint FK_STUDENT_SESSION;
alter table STUDENT enable constraint FK_STUDENT_STATUS_STUDENT;
prompt Enabling foreign key constraints for FREE_TIME...
alter table FREE_TIME enable constraint FK_FREE_TIME_SESSION;
prompt Enabling foreign key constraints for COURSE_DEMAND...
alter table COURSE_DEMAND enable constraint FK_COURSE_DEMAND_FREE_TIME;
alter table COURSE_DEMAND enable constraint FK_COURSE_DEMAND_STUDENT;
prompt Enabling foreign key constraints for SUBJECT_AREA...
alter table SUBJECT_AREA enable constraint FK_SUBJECT_AREA_DEPT;
prompt Enabling foreign key constraints for COURSE_OFFERING...
alter table COURSE_OFFERING enable constraint FK_COURSE_OFFERING_DEMAND_OFFR;
alter table COURSE_OFFERING enable constraint FK_COURSE_OFFERING_INSTR_OFFR;
alter table COURSE_OFFERING enable constraint FK_COURSE_OFFERING_SUBJ_AREA;
prompt Enabling foreign key constraints for COURSE_REQUEST...
alter table COURSE_REQUEST enable constraint FK_COURSE_REQUEST_DEMAND;
alter table COURSE_REQUEST enable constraint FK_COURSE_REQUEST_OFFERING;
prompt Enabling foreign key constraints for CLASS_WAITLIST...
alter table CLASS_WAITLIST enable constraint FK_CLASS_WAITLIST_CLASS;
alter table CLASS_WAITLIST enable constraint FK_CLASS_WAITLIST_REQUEST;
alter table CLASS_WAITLIST enable constraint FK_CLASS_WAITLIST_STUDENT;
prompt Enabling foreign key constraints for CONSTRAINT_INFO...
alter table CONSTRAINT_INFO enable constraint FK_CONSTRAINT_INFO_ASSIGNMENT;
alter table CONSTRAINT_INFO enable constraint FK_CONSTRAINT_INFO_SOLVER;
prompt Enabling foreign key constraints for COURSE_CREDIT_UNIT_CONFIG...
alter table COURSE_CREDIT_UNIT_CONFIG enable constraint FK_CRS_CRDT_UNIT_CFG_CRDT_TYPE;
alter table COURSE_CREDIT_UNIT_CONFIG enable constraint FK_CRS_CRDT_UNIT_CFG_IO_OWN;
alter table COURSE_CREDIT_UNIT_CONFIG enable constraint FK_CRS_CRDT_UNIT_CFG_OWNER;
prompt Enabling foreign key constraints for COURSE_RESERVATION...
alter table COURSE_RESERVATION enable constraint FK_COURSE_RESERV_TYPE;
alter table COURSE_RESERVATION enable constraint FK_COURSE_RESV_CRS_OFFR;
prompt Enabling foreign key constraints for COURSE_SUBPART_CREDIT...
alter table COURSE_SUBPART_CREDIT enable constraint FK_SUBPART_CRED_CRS;
prompt Enabling foreign key constraints for CURRICULUM...
alter table CURRICULUM enable constraint FK_CURRICULUM_ACAD_AREA;
alter table CURRICULUM enable constraint FK_CURRICULUM_DEPT;
prompt Enabling foreign key constraints for CURRICULUM_COURSE...
alter table CURRICULUM_COURSE enable constraint FK_CURRICULUM_COURSE_CLASF;
alter table CURRICULUM_COURSE enable constraint FK_CURRICULUM_COURSE_COURSE;
prompt Enabling foreign key constraints for CURRICULUM_GROUP...
alter table CURRICULUM_GROUP enable constraint FK_CURRICULUM_GROUP_CURRICULUM;
prompt Enabling foreign key constraints for CURRICULUM_COURSE_GROUP...
alter table CURRICULUM_COURSE_GROUP enable constraint FK_CUR_COURSE_GROUP_COURSE;
alter table CURRICULUM_COURSE_GROUP enable constraint FK_CUR_COURSE_GROUP_GROUP;
prompt Enabling foreign key constraints for POS_MAJOR...
alter table POS_MAJOR enable constraint FK_POS_MAJOR_SESSION;
prompt Enabling foreign key constraints for CURRICULUM_MAJOR...
alter table CURRICULUM_MAJOR enable constraint FK_CURRICULUM_MAJOR_CURRICULUM;
alter table CURRICULUM_MAJOR enable constraint FK_CURRICULUM_MAJOR_MAJOR;
prompt Enabling foreign key constraints for CURRICULUM_RULE...
alter table CURRICULUM_RULE enable constraint FK_CUR_RULE_ACAD_AREA;
alter table CURRICULUM_RULE enable constraint FK_CUR_RULE_ACAD_CLASF;
alter table CURRICULUM_RULE enable constraint FK_CUR_RULE_MAJOR;
prompt Enabling foreign key constraints for DATE_PATTERN_DEPT...
alter table DATE_PATTERN_DEPT enable constraint FK_DATE_PATTERN_DEPT_DATE;
alter table DATE_PATTERN_DEPT enable constraint FK_DATE_PATTERN_DEPT_DEPT;
prompt Enabling foreign key constraints for DEPT_TO_TT_MGR...
alter table DEPT_TO_TT_MGR enable constraint FK_DEPT_TO_TT_MGR_DEPT;
alter table DEPT_TO_TT_MGR enable constraint FK_DEPT_TO_TT_MGR_MGR;
prompt Enabling foreign key constraints for DESIGNATOR...
alter table DESIGNATOR enable constraint FK_DESIGNATOR_INSTRUCTOR;
alter table DESIGNATOR enable constraint FK_DESIGNATOR_SUBJ_AREA;
prompt Enabling foreign key constraints for DISTRIBUTION_PREF...
alter table DISTRIBUTION_PREF enable constraint FK_DISTRIBUTION_PREF_DIST_TYPE;
alter table DISTRIBUTION_PREF enable constraint FK_DISTRIBUTION_PREF_LEVEL;
prompt Enabling foreign key constraints for DISTRIBUTION_OBJECT...
alter table DISTRIBUTION_OBJECT enable constraint FK_DISTRIBUTION_OBJECT_PREF;
prompt Enabling foreign key constraints for DIST_TYPE_DEPT...
alter table DIST_TYPE_DEPT enable constraint FK_DIST_TYPE_DEPT_DEPT;
alter table DIST_TYPE_DEPT enable constraint FK_DIST_TYPE_DEPT_TYPE;
prompt Enabling foreign key constraints for EXAM_PERIOD...
alter table EXAM_PERIOD enable constraint FK_EXAM_PERIOD_PREF;
alter table EXAM_PERIOD enable constraint FK_EXAM_PERIOD_SESSION;
prompt Enabling foreign key constraints for EXAM...
alter table EXAM enable constraint FK_EXAM_PERIOD;
alter table EXAM enable constraint FK_EXAM_SESSION;
prompt Enabling foreign key constraints for EVENT...
alter table EVENT enable constraint FK_EVENT_CLASS;
alter table EVENT enable constraint FK_EVENT_EXAM;
alter table EVENT enable constraint FK_EVENT_MAIN_CONTACT;
alter table EVENT enable constraint FK_EVENT_SPONSOR_ORG;
prompt Enabling foreign key constraints for EVENT_JOIN_EVENT_CONTACT...
alter table EVENT_JOIN_EVENT_CONTACT enable constraint FK_EVENT_CONTACT_JOIN;
alter table EVENT_JOIN_EVENT_CONTACT enable constraint FK_EVENT_ID_JOIN;
prompt Enabling foreign key constraints for EVENT_NOTE...
alter table EVENT_NOTE enable constraint FK_EVENT_NOTE_EVENT;
prompt Enabling foreign key constraints for EXAM_INSTRUCTOR...
alter table EXAM_INSTRUCTOR enable constraint FK_EXAM_INSTRUCTOR_EXAM;
alter table EXAM_INSTRUCTOR enable constraint FK_EXAM_INSTRUCTOR_INSTRUCTOR;
prompt Enabling foreign key constraints for EXAM_LOCATION_PREF...
alter table EXAM_LOCATION_PREF enable constraint FK_EXAM_LOCATION_PREF_PERIOD;
alter table EXAM_LOCATION_PREF enable constraint FK_EXAM_LOCATION_PREF_PREF;
prompt Enabling foreign key constraints for EXAM_OWNER...
alter table EXAM_OWNER enable constraint FK_EXAM_OWNER_COURSE;
alter table EXAM_OWNER enable constraint FK_EXAM_OWNER_EXAM;
prompt Enabling foreign key constraints for EXAM_PERIOD_PREF...
alter table EXAM_PERIOD_PREF enable constraint FK_EXAM_PERIOD_PREF_PERIOD;
alter table EXAM_PERIOD_PREF enable constraint FK_EXAM_PERIOD_PREF_PREF;
prompt Enabling foreign key constraints for EXAM_ROOM_ASSIGNMENT...
alter table EXAM_ROOM_ASSIGNMENT enable constraint FK_EXAM_ROOM_EXAM;
prompt Enabling foreign key constraints for EXTERNAL_ROOM...
alter table EXTERNAL_ROOM enable constraint FK_EXTERNAL_ROOM_TYPE;
alter table EXTERNAL_ROOM enable constraint FK_EXT_ROOM_BUILDING;
prompt Enabling foreign key constraints for EXTERNAL_ROOM_DEPARTMENT...
alter table EXTERNAL_ROOM_DEPARTMENT enable constraint FK_EXT_DEPT_ROOM;
prompt Enabling foreign key constraints for EXTERNAL_ROOM_FEATURE...
alter table EXTERNAL_ROOM_FEATURE enable constraint FK_EXT_FTR_ROOM;
prompt Enabling foreign key constraints for HISTORY...
alter table HISTORY enable constraint FK_HISTORY_SESSION;
prompt Enabling foreign key constraints for INDIVIDUAL_RESERVATION...
alter table INDIVIDUAL_RESERVATION enable constraint FK_INDIVIDUAL_RESV_TYPE;
prompt Enabling foreign key constraints for JENRL...
alter table JENRL enable constraint FK_JENRL_CLASS1;
alter table JENRL enable constraint FK_JENRL_CLASS2;
alter table JENRL enable constraint FK_JENRL_SOLUTION;
prompt Enabling foreign key constraints for LASTLIKE_COURSE_DEMAND...
alter table LASTLIKE_COURSE_DEMAND enable constraint FK_LL_COURSE_DEMAND_STUDENT;
alter table LASTLIKE_COURSE_DEMAND enable constraint FK_LL_COURSE_DEMAND_SUBJAREA;
prompt Enabling foreign key constraints for MANAGER_SETTINGS...
alter table MANAGER_SETTINGS enable constraint FK_MANAGER_SETTINGS_KEY;
alter table MANAGER_SETTINGS enable constraint FK_MANAGER_SETTINGS_USER;
prompt Enabling foreign key constraints for MEETING...
alter table MEETING enable constraint FK_MEETING_EVENT;
prompt Enabling foreign key constraints for NON_UNIVERSITY_LOCATION...
alter table NON_UNIVERSITY_LOCATION enable constraint FK_LOCATION_TYPE;
alter table NON_UNIVERSITY_LOCATION enable constraint FK_NON_UNIV_LOC_SESSION;
prompt Enabling foreign key constraints for OFFR_GROUP...
alter table OFFR_GROUP enable constraint FK_OFFR_GROUP_DEPT;
alter table OFFR_GROUP enable constraint FK_OFFR_GROUP_SESSION;
prompt Enabling foreign key constraints for OFFR_GROUP_OFFERING...
alter table OFFR_GROUP_OFFERING enable constraint FK_OFFR_GROUP_INSTR_OFFR;
alter table OFFR_GROUP_OFFERING enable constraint FK_OFFR_GROUP_OFFR_OFFR_GRP;
prompt Enabling foreign key constraints for POSITION_CODE_TO_TYPE...
alter table POSITION_CODE_TO_TYPE enable constraint FK_POS_CODE_TO_TYPE_CODE_TYPE;
prompt Enabling foreign key constraints for POS_ACAD_AREA_MAJOR...
alter table POS_ACAD_AREA_MAJOR enable constraint FK_POS_ACAD_AREA_MAJOR_AREA;
alter table POS_ACAD_AREA_MAJOR enable constraint FK_POS_ACAD_AREA_MAJOR_MAJOR;
prompt Enabling foreign key constraints for POS_MINOR...
alter table POS_MINOR enable constraint FK_POS_MINOR_SESSION;
prompt Enabling foreign key constraints for POS_ACAD_AREA_MINOR...
alter table POS_ACAD_AREA_MINOR enable constraint FK_POS_ACAD_AREA_MINOR_AREA;
alter table POS_ACAD_AREA_MINOR enable constraint FK_POS_ACAD_AREA_MINOR_MINOR;
prompt Enabling foreign key constraints for POS_RESERVATION...
alter table POS_RESERVATION enable constraint FK_POS_RESV_ACAD_CLASS;
alter table POS_RESERVATION enable constraint FK_POS_RESV_MAJOR;
alter table POS_RESERVATION enable constraint FK_POS_RESV_TYPE;
prompt Enabling foreign key constraints for RELATED_COURSE_INFO...
alter table RELATED_COURSE_INFO enable constraint FK_EVENT_OWNER_COURSE;
alter table RELATED_COURSE_INFO enable constraint FK_EVENT_OWNER_EVENT;
prompt Enabling foreign key constraints for ROOM...
alter table ROOM enable constraint FK_ROOM_BUILDING;
alter table ROOM enable constraint FK_ROOM_SESSION;
alter table ROOM enable constraint FK_ROOM_TYPE;
prompt Enabling foreign key constraints for ROOM_DEPT...
alter table ROOM_DEPT enable constraint FK_ROOM_DEPT_DEPT;
prompt Enabling foreign key constraints for ROOM_FEATURE...
alter table ROOM_FEATURE enable constraint FK_ROOM_FEATURE_DEPT;
prompt Enabling foreign key constraints for ROOM_FEATURE_PREF...
alter table ROOM_FEATURE_PREF enable constraint FK_ROOM_FEAT_PREF_LEVEL;
alter table ROOM_FEATURE_PREF enable constraint FK_ROOM_FEAT_PREF_ROOM_FEAT;
prompt Enabling foreign key constraints for ROOM_GROUP...
alter table ROOM_GROUP enable constraint FK_ROOM_GROUP_DEPT;
alter table ROOM_GROUP enable constraint FK_ROOM_GROUP_SESSION;
prompt Enabling foreign key constraints for ROOM_GROUP_PREF...
alter table ROOM_GROUP_PREF enable constraint FK_ROOM_GROUP_PREF_LEVEL;
alter table ROOM_GROUP_PREF enable constraint FK_ROOM_GROUP_PREF_ROOM_GRP;
prompt Enabling foreign key constraints for ROOM_GROUP_ROOM...
alter table ROOM_GROUP_ROOM enable constraint FK_ROOM_GROUP_ROOM_ROOM_GRP;
prompt Enabling foreign key constraints for ROOM_JOIN_ROOM_FEATURE...
alter table ROOM_JOIN_ROOM_FEATURE enable constraint FK_ROOM_JOIN_ROOM_FEAT_RM_FEAT;
prompt Enabling foreign key constraints for ROOM_PREF...
alter table ROOM_PREF enable constraint FK_ROOM_PREF_LEVEL;
prompt Enabling foreign key constraints for ROOM_TYPE_OPTION...
alter table ROOM_TYPE_OPTION enable constraint FK_RTYPE_OPTION_SESSION;
alter table ROOM_TYPE_OPTION enable constraint FK_RTYPE_OPTION_TYPE;
prompt Enabling foreign key constraints for SECTIONING_INFO...
alter table SECTIONING_INFO enable constraint FK_SECTIONING_INFO_CLASS;
prompt Enabling foreign key constraints for SOLVER_GR_TO_TT_MGR...
alter table SOLVER_GR_TO_TT_MGR enable constraint FK_SOLVER_GR_TO_TT_MGR_SOLVGRP;
alter table SOLVER_GR_TO_TT_MGR enable constraint FK_SOLVER_GR_TO_TT_MGR_TT_MGR;
prompt Enabling foreign key constraints for SOLVER_PARAMETER_DEF...
alter table SOLVER_PARAMETER_DEF enable constraint FK_SOLV_PARAM_DEF_SOLV_PAR_GRP;
prompt Enabling foreign key constraints for SOLVER_PARAMETER...
alter table SOLVER_PARAMETER enable constraint FK_SOLVER_PARAM_DEF;
alter table SOLVER_PARAMETER enable constraint FK_SOLVER_PARAM_PREDEF_STG;
alter table SOLVER_PARAMETER enable constraint FK_SOLVER_PARAM_SOLUTION;
prompt Enabling foreign key constraints for STUDENT_ACAD_AREA...
alter table STUDENT_ACAD_AREA enable constraint FK_STUDENT_ACAD_AREA_AREA;
alter table STUDENT_ACAD_AREA enable constraint FK_STUDENT_ACAD_AREA_CLASF;
alter table STUDENT_ACAD_AREA enable constraint FK_STUDENT_ACAD_AREA_STUDENT;
prompt Enabling foreign key constraints for STUDENT_ACCOMODATION...
alter table STUDENT_ACCOMODATION enable constraint FK_STUDENT_ACCOM_SESSION;
prompt Enabling foreign key constraints for STUDENT_CLASS_ENRL...
alter table STUDENT_CLASS_ENRL enable constraint FK_STUDENT_CLASS_ENRL_CLASS;
alter table STUDENT_CLASS_ENRL enable constraint FK_STUDENT_CLASS_ENRL_COURSE;
alter table STUDENT_CLASS_ENRL enable constraint FK_STUDENT_CLASS_ENRL_REQUEST;
alter table STUDENT_CLASS_ENRL enable constraint FK_STUDENT_CLASS_ENRL_STUDENT;
prompt Enabling foreign key constraints for STUDENT_ENRL...
alter table STUDENT_ENRL enable constraint FK_STUDENT_ENRL_CLASS;
alter table STUDENT_ENRL enable constraint FK_STUDENT_ENRL_SOLUTION;
prompt Enabling foreign key constraints for STUDENT_ENRL_MSG...
alter table STUDENT_ENRL_MSG enable constraint FK_STUDENT_ENRL_MSG_DEMAND;
prompt Enabling foreign key constraints for STUDENT_GROUP...
alter table STUDENT_GROUP enable constraint FK_STUDENT_GROUP_SESSION;
prompt Enabling foreign key constraints for STUDENT_GROUP_RESERVATION...
alter table STUDENT_GROUP_RESERVATION enable constraint FK_STU_GRP_RESV_RESERV_TYPE;
alter table STUDENT_GROUP_RESERVATION enable constraint FK_STU_GRP_RESV_STU_GRP;
prompt Enabling foreign key constraints for STUDENT_MAJOR...
alter table STUDENT_MAJOR enable constraint FK_STUDENT_MAJOR_MAJOR;
alter table STUDENT_MAJOR enable constraint FK_STUDENT_MAJOR_STUDENT;
prompt Enabling foreign key constraints for STUDENT_MINOR...
alter table STUDENT_MINOR enable constraint FK_STUDENT_MINOR_MINOR;
alter table STUDENT_MINOR enable constraint FK_STUDENT_MINOR_STUDENT;
prompt Enabling foreign key constraints for STUDENT_TO_ACOMODATION...
alter table STUDENT_TO_ACOMODATION enable constraint FK_STUDENT_ACOMODATION_ACCOM;
alter table STUDENT_TO_ACOMODATION enable constraint FK_STUDENT_ACOMODATION_STUDENT;
prompt Enabling foreign key constraints for STUDENT_TO_GROUP...
alter table STUDENT_TO_GROUP enable constraint FK_STUDENT_GROUP_GROUP;
alter table STUDENT_TO_GROUP enable constraint FK_STUDENT_GROUP_STUDENT;
prompt Enabling foreign key constraints for TIME_PATTERN_DAYS...
alter table TIME_PATTERN_DAYS enable constraint FK_TIME_PATTERN_DAYS_TIME_PATT;
prompt Enabling foreign key constraints for TIME_PATTERN_DEPT...
alter table TIME_PATTERN_DEPT enable constraint FK_TIME_PATTERN_DEPT_DEPT;
alter table TIME_PATTERN_DEPT enable constraint FK_TIME_PATTERN_DEPT_PATTERN;
prompt Enabling foreign key constraints for TIME_PATTERN_TIME...
alter table TIME_PATTERN_TIME enable constraint FK_TIME_PATTERN_TIME;
prompt Enabling foreign key constraints for TIME_PREF...
alter table TIME_PREF enable constraint FK_TIME_PREF_PREF_LEVEL;
alter table TIME_PREF enable constraint FK_TIME_PREF_TIME_PTRN;
prompt Enabling foreign key constraints for TMTBL_MGR_TO_ROLES...
alter table TMTBL_MGR_TO_ROLES enable constraint FK_TMTBL_MGR_TO_ROLES_MANAGER;
alter table TMTBL_MGR_TO_ROLES enable constraint FK_TMTBL_MGR_TO_ROLES_ROLE;
prompt Enabling foreign key constraints for WAITLIST...
alter table WAITLIST enable constraint FK_WAITLIST_COURSE_OFFERING;
alter table WAITLIST enable constraint FK_WAITLIST_STUDENT;
prompt Enabling foreign key constraints for XCONFLICT_EXAM...
alter table XCONFLICT_EXAM enable constraint FK_XCONFLICT_EX_CONF;
alter table XCONFLICT_EXAM enable constraint FK_XCONFLICT_EX_EXAM;
prompt Enabling foreign key constraints for XCONFLICT_INSTRUCTOR...
alter table XCONFLICT_INSTRUCTOR enable constraint FK_XCONFLICT_IN_CONF;
alter table XCONFLICT_INSTRUCTOR enable constraint FK_XCONFLICT_IN_INSTRUCTOR;
prompt Enabling foreign key constraints for XCONFLICT_STUDENT...
alter table XCONFLICT_STUDENT enable constraint FK_XCONFLICT_ST_CONF;
alter table XCONFLICT_STUDENT enable constraint FK_XCONFLICT_ST_STUDENT;
prompt Enabling triggers for DATE_PATTERN...
alter table DATE_PATTERN enable all triggers;
prompt Enabling triggers for DEPT_STATUS_TYPE...
alter table DEPT_STATUS_TYPE enable all triggers;
prompt Enabling triggers for SESSIONS...
alter table SESSIONS enable all triggers;
prompt Enabling triggers for ACADEMIC_AREA...
alter table ACADEMIC_AREA enable all triggers;
prompt Enabling triggers for ACADEMIC_CLASSIFICATION...
alter table ACADEMIC_CLASSIFICATION enable all triggers;
prompt Enabling triggers for RESERVATION_TYPE...
alter table RESERVATION_TYPE enable all triggers;
prompt Enabling triggers for ACAD_AREA_RESERVATION...
alter table ACAD_AREA_RESERVATION enable all triggers;
prompt Enabling triggers for APPLICATION_CONFIG...
alter table APPLICATION_CONFIG enable all triggers;
prompt Enabling triggers for OFFR_CONSENT_TYPE...
alter table OFFR_CONSENT_TYPE enable all triggers;
prompt Enabling triggers for INSTRUCTIONAL_OFFERING...
alter table INSTRUCTIONAL_OFFERING enable all triggers;
prompt Enabling triggers for INSTR_OFFERING_CONFIG...
alter table INSTR_OFFERING_CONFIG enable all triggers;
prompt Enabling triggers for ITYPE_DESC...
alter table ITYPE_DESC enable all triggers;
prompt Enabling triggers for SCHEDULING_SUBPART...
alter table SCHEDULING_SUBPART enable all triggers;
prompt Enabling triggers for CLASS_...
alter table CLASS_ enable all triggers;
prompt Enabling triggers for SOLVER_GROUP...
alter table SOLVER_GROUP enable all triggers;
prompt Enabling triggers for SOLUTION...
alter table SOLUTION enable all triggers;
prompt Enabling triggers for TIME_PATTERN...
alter table TIME_PATTERN enable all triggers;
prompt Enabling triggers for ASSIGNMENT...
alter table ASSIGNMENT enable all triggers;
prompt Enabling triggers for POSITION_TYPE...
alter table POSITION_TYPE enable all triggers;
prompt Enabling triggers for DEPARTMENT...
alter table DEPARTMENT enable all triggers;
prompt Enabling triggers for DEPARTMENTAL_INSTRUCTOR...
alter table DEPARTMENTAL_INSTRUCTOR enable all triggers;
prompt Enabling triggers for ASSIGNED_INSTRUCTORS...
alter table ASSIGNED_INSTRUCTORS enable all triggers;
prompt Enabling triggers for ASSIGNED_ROOMS...
alter table ASSIGNED_ROOMS enable all triggers;
prompt Enabling triggers for BUILDING...
alter table BUILDING enable all triggers;
prompt Enabling triggers for PREFERENCE_LEVEL...
alter table PREFERENCE_LEVEL enable all triggers;
prompt Enabling triggers for BUILDING_PREF...
alter table BUILDING_PREF enable all triggers;
prompt Enabling triggers for CLASS_INSTRUCTOR...
alter table CLASS_INSTRUCTOR enable all triggers;
prompt Enabling triggers for STUDENT_STATUS_TYPE...
alter table STUDENT_STATUS_TYPE enable all triggers;
prompt Enabling triggers for STUDENT...
alter table STUDENT enable all triggers;
prompt Enabling triggers for FREE_TIME...
alter table FREE_TIME enable all triggers;
prompt Enabling triggers for COURSE_DEMAND...
alter table COURSE_DEMAND enable all triggers;
prompt Enabling triggers for SUBJECT_AREA...
alter table SUBJECT_AREA enable all triggers;
prompt Enabling triggers for COURSE_OFFERING...
alter table COURSE_OFFERING enable all triggers;
prompt Enabling triggers for COURSE_REQUEST...
alter table COURSE_REQUEST enable all triggers;
prompt Enabling triggers for CLASS_WAITLIST...
alter table CLASS_WAITLIST enable all triggers;
prompt Enabling triggers for CONSTRAINT_INFO...
alter table CONSTRAINT_INFO enable all triggers;
prompt Enabling triggers for COURSE_CATALOG...
alter table COURSE_CATALOG enable all triggers;
prompt Enabling triggers for COURSE_CREDIT_TYPE...
alter table COURSE_CREDIT_TYPE enable all triggers;
prompt Enabling triggers for COURSE_CREDIT_UNIT_CONFIG...
alter table COURSE_CREDIT_UNIT_CONFIG enable all triggers;
prompt Enabling triggers for COURSE_CREDIT_UNIT_TYPE...
alter table COURSE_CREDIT_UNIT_TYPE enable all triggers;
prompt Enabling triggers for COURSE_RESERVATION...
alter table COURSE_RESERVATION enable all triggers;
prompt Enabling triggers for COURSE_SUBPART_CREDIT...
alter table COURSE_SUBPART_CREDIT enable all triggers;
prompt Enabling triggers for CRSE_CREDIT_FORMAT...
alter table CRSE_CREDIT_FORMAT enable all triggers;
prompt Enabling triggers for CURRICULUM...
alter table CURRICULUM enable all triggers;
prompt Enabling triggers for CURRICULUM_COURSE...
alter table CURRICULUM_COURSE enable all triggers;
prompt Enabling triggers for CURRICULUM_GROUP...
alter table CURRICULUM_GROUP enable all triggers;
prompt Enabling triggers for CURRICULUM_COURSE_GROUP...
alter table CURRICULUM_COURSE_GROUP enable all triggers;
prompt Enabling triggers for POS_MAJOR...
alter table POS_MAJOR enable all triggers;
prompt Enabling triggers for CURRICULUM_MAJOR...
alter table CURRICULUM_MAJOR enable all triggers;
prompt Enabling triggers for CURRICULUM_RULE...
alter table CURRICULUM_RULE enable all triggers;
prompt Enabling triggers for DATE_PATTERN_DEPT...
alter table DATE_PATTERN_DEPT enable all triggers;
prompt Enabling triggers for DEMAND_OFFR_TYPE...
alter table DEMAND_OFFR_TYPE enable all triggers;
prompt Enabling triggers for TIMETABLE_MANAGER...
alter table TIMETABLE_MANAGER enable all triggers;
prompt Enabling triggers for DEPT_TO_TT_MGR...
alter table DEPT_TO_TT_MGR enable all triggers;
prompt Enabling triggers for DESIGNATOR...
alter table DESIGNATOR enable all triggers;
prompt Enabling triggers for DISTRIBUTION_TYPE...
alter table DISTRIBUTION_TYPE enable all triggers;
prompt Enabling triggers for DISTRIBUTION_PREF...
alter table DISTRIBUTION_PREF enable all triggers;
prompt Enabling triggers for DISTRIBUTION_OBJECT...
alter table DISTRIBUTION_OBJECT enable all triggers;
prompt Enabling triggers for DIST_TYPE_DEPT...
alter table DIST_TYPE_DEPT enable all triggers;
prompt Enabling triggers for EVENT_CONTACT...
alter table EVENT_CONTACT enable all triggers;
prompt Enabling triggers for EXAM_PERIOD...
alter table EXAM_PERIOD enable all triggers;
prompt Enabling triggers for EXAM...
alter table EXAM enable all triggers;
prompt Enabling triggers for SPONSORING_ORGANIZATION...
alter table SPONSORING_ORGANIZATION enable all triggers;
prompt Enabling triggers for EVENT...
alter table EVENT enable all triggers;
prompt Enabling triggers for EVENT_JOIN_EVENT_CONTACT...
alter table EVENT_JOIN_EVENT_CONTACT enable all triggers;
prompt Enabling triggers for EVENT_NOTE...
alter table EVENT_NOTE enable all triggers;
prompt Enabling triggers for EXACT_TIME_MINS...
alter table EXACT_TIME_MINS enable all triggers;
prompt Enabling triggers for EXAM_INSTRUCTOR...
alter table EXAM_INSTRUCTOR enable all triggers;
prompt Enabling triggers for EXAM_LOCATION_PREF...
alter table EXAM_LOCATION_PREF enable all triggers;
prompt Enabling triggers for EXAM_OWNER...
alter table EXAM_OWNER enable all triggers;
prompt Enabling triggers for EXAM_PERIOD_PREF...
alter table EXAM_PERIOD_PREF enable all triggers;
prompt Enabling triggers for EXAM_ROOM_ASSIGNMENT...
alter table EXAM_ROOM_ASSIGNMENT enable all triggers;
prompt Enabling triggers for EXTERNAL_BUILDING...
alter table EXTERNAL_BUILDING enable all triggers;
prompt Enabling triggers for ROOM_TYPE...
alter table ROOM_TYPE enable all triggers;
prompt Enabling triggers for EXTERNAL_ROOM...
alter table EXTERNAL_ROOM enable all triggers;
prompt Enabling triggers for EXTERNAL_ROOM_DEPARTMENT...
alter table EXTERNAL_ROOM_DEPARTMENT enable all triggers;
prompt Enabling triggers for EXTERNAL_ROOM_FEATURE...
alter table EXTERNAL_ROOM_FEATURE enable all triggers;
prompt Enabling triggers for HISTORY...
alter table HISTORY enable all triggers;
prompt Enabling triggers for HT_PREFERENCE...
alter table HT_PREFERENCE enable all triggers;
prompt Enabling triggers for INDIVIDUAL_RESERVATION...
alter table INDIVIDUAL_RESERVATION enable all triggers;
prompt Enabling triggers for JENRL...
alter table JENRL enable all triggers;
prompt Enabling triggers for LASTLIKE_COURSE_DEMAND...
alter table LASTLIKE_COURSE_DEMAND enable all triggers;
prompt Enabling triggers for SETTINGS...
alter table SETTINGS enable all triggers;
prompt Enabling triggers for MANAGER_SETTINGS...
alter table MANAGER_SETTINGS enable all triggers;
prompt Enabling triggers for MEETING...
alter table MEETING enable all triggers;
prompt Enabling triggers for NON_UNIVERSITY_LOCATION...
alter table NON_UNIVERSITY_LOCATION enable all triggers;
prompt Enabling triggers for OFFR_GROUP...
alter table OFFR_GROUP enable all triggers;
prompt Enabling triggers for OFFR_GROUP_OFFERING...
alter table OFFR_GROUP_OFFERING enable all triggers;
prompt Enabling triggers for POSITION_CODE_TO_TYPE...
alter table POSITION_CODE_TO_TYPE enable all triggers;
prompt Enabling triggers for POS_ACAD_AREA_MAJOR...
alter table POS_ACAD_AREA_MAJOR enable all triggers;
prompt Enabling triggers for POS_MINOR...
alter table POS_MINOR enable all triggers;
prompt Enabling triggers for POS_ACAD_AREA_MINOR...
alter table POS_ACAD_AREA_MINOR enable all triggers;
prompt Enabling triggers for POS_RESERVATION...
alter table POS_RESERVATION enable all triggers;
prompt Enabling triggers for RELATED_COURSE_INFO...
alter table RELATED_COURSE_INFO enable all triggers;
prompt Enabling triggers for ROLES...
alter table ROLES enable all triggers;
prompt Enabling triggers for ROOM...
alter table ROOM enable all triggers;
prompt Enabling triggers for ROOM_DEPT...
alter table ROOM_DEPT enable all triggers;
prompt Enabling triggers for ROOM_FEATURE...
alter table ROOM_FEATURE enable all triggers;
prompt Enabling triggers for ROOM_FEATURE_PREF...
alter table ROOM_FEATURE_PREF enable all triggers;
prompt Enabling triggers for ROOM_GROUP...
alter table ROOM_GROUP enable all triggers;
prompt Enabling triggers for ROOM_GROUP_PREF...
alter table ROOM_GROUP_PREF enable all triggers;
prompt Enabling triggers for ROOM_GROUP_ROOM...
alter table ROOM_GROUP_ROOM enable all triggers;
prompt Enabling triggers for ROOM_JOIN_ROOM_FEATURE...
alter table ROOM_JOIN_ROOM_FEATURE enable all triggers;
prompt Enabling triggers for ROOM_PREF...
alter table ROOM_PREF enable all triggers;
prompt Enabling triggers for ROOM_TYPE_OPTION...
alter table ROOM_TYPE_OPTION enable all triggers;
prompt Enabling triggers for SECTIONING_INFO...
alter table SECTIONING_INFO enable all triggers;
prompt Enabling triggers for SOLVER_GR_TO_TT_MGR...
alter table SOLVER_GR_TO_TT_MGR enable all triggers;
prompt Enabling triggers for SOLVER_INFO_DEF...
alter table SOLVER_INFO_DEF enable all triggers;
prompt Enabling triggers for SOLVER_PARAMETER_GROUP...
alter table SOLVER_PARAMETER_GROUP enable all triggers;
prompt Enabling triggers for SOLVER_PARAMETER_DEF...
alter table SOLVER_PARAMETER_DEF enable all triggers;
prompt Enabling triggers for SOLVER_PREDEF_SETTING...
alter table SOLVER_PREDEF_SETTING enable all triggers;
prompt Enabling triggers for SOLVER_PARAMETER...
alter table SOLVER_PARAMETER enable all triggers;
prompt Enabling triggers for STAFF...
alter table STAFF enable all triggers;
prompt Enabling triggers for STANDARD_EVENT_NOTE...
alter table STANDARD_EVENT_NOTE enable all triggers;
prompt Enabling triggers for STUDENT_ACAD_AREA...
alter table STUDENT_ACAD_AREA enable all triggers;
prompt Enabling triggers for STUDENT_ACCOMODATION...
alter table STUDENT_ACCOMODATION enable all triggers;
prompt Enabling triggers for STUDENT_CLASS_ENRL...
alter table STUDENT_CLASS_ENRL enable all triggers;
prompt Enabling triggers for STUDENT_ENRL...
alter table STUDENT_ENRL enable all triggers;
prompt Enabling triggers for STUDENT_ENRL_MSG...
alter table STUDENT_ENRL_MSG enable all triggers;
prompt Enabling triggers for STUDENT_GROUP...
alter table STUDENT_GROUP enable all triggers;
prompt Enabling triggers for STUDENT_GROUP_RESERVATION...
alter table STUDENT_GROUP_RESERVATION enable all triggers;
prompt Enabling triggers for STUDENT_MAJOR...
alter table STUDENT_MAJOR enable all triggers;
prompt Enabling triggers for STUDENT_MINOR...
alter table STUDENT_MINOR enable all triggers;
prompt Enabling triggers for STUDENT_TO_ACOMODATION...
alter table STUDENT_TO_ACOMODATION enable all triggers;
prompt Enabling triggers for STUDENT_TO_GROUP...
alter table STUDENT_TO_GROUP enable all triggers;
prompt Enabling triggers for TIME_PATTERN_DAYS...
alter table TIME_PATTERN_DAYS enable all triggers;
prompt Enabling triggers for TIME_PATTERN_DEPT...
alter table TIME_PATTERN_DEPT enable all triggers;
prompt Enabling triggers for TIME_PATTERN_TIME...
alter table TIME_PATTERN_TIME enable all triggers;
prompt Enabling triggers for TIME_PREF...
alter table TIME_PREF enable all triggers;
prompt Enabling triggers for TMTBL_MGR_TO_ROLES...
alter table TMTBL_MGR_TO_ROLES enable all triggers;
prompt Enabling triggers for USERS...
alter table USERS enable all triggers;
prompt Enabling triggers for USER_DATA...
alter table USER_DATA enable all triggers;
prompt Enabling triggers for WAITLIST...
alter table WAITLIST enable all triggers;
prompt Enabling triggers for XCONFLICT...
alter table XCONFLICT enable all triggers;
prompt Enabling triggers for XCONFLICT_EXAM...
alter table XCONFLICT_EXAM enable all triggers;
prompt Enabling triggers for XCONFLICT_INSTRUCTOR...
alter table XCONFLICT_INSTRUCTOR enable all triggers;
prompt Enabling triggers for XCONFLICT_STUDENT...
alter table XCONFLICT_STUDENT enable all triggers;
set feedback on
set define on
prompt Done.
