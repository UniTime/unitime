/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org
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

/*
 * This scripts truncates all the timetabling tables and populates
 * read-only tables with initial values. It also creates an 
 * administrator user (username=admin, password=admin) and an
 * academic session with one department and a solver group.
 */

-- Disable foreign key checks
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;

DELETE FROM `timetable`.`acad_area_reservation`;
DELETE FROM `timetable`.`academic_area`;
DELETE FROM `timetable`.`academic_classification`;
DELETE FROM `timetable`.`application_config`;
DELETE FROM `timetable`.`assigned_instructors`;
DELETE FROM `timetable`.`assigned_rooms`;
DELETE FROM `timetable`.`assignment`;
DELETE FROM `timetable`.`building`;
DELETE FROM `timetable`.`building_pref`;
DELETE FROM `timetable`.`change_log`;
DELETE FROM `timetable`.`class_`;
DELETE FROM `timetable`.`class_instructor`;
DELETE FROM `timetable`.`class_waitlist`;
DELETE FROM `timetable`.`constraint_info`;
DELETE FROM `timetable`.`course_catalog`;
DELETE FROM `timetable`.`course_credit_type`;
DELETE FROM `timetable`.`course_credit_unit_config`;
DELETE FROM `timetable`.`course_credit_unit_type`;
DELETE FROM `timetable`.`course_demand`;
DELETE FROM `timetable`.`course_offering`;
DELETE FROM `timetable`.`course_request`;
DELETE FROM `timetable`.`course_request_option`;
DELETE FROM `timetable`.`course_reservation`;
DELETE FROM `timetable`.`course_subpart_credit`;
DELETE FROM `timetable`.`crse_credit_format`;
DELETE FROM `timetable`.`date_pattern`;
DELETE FROM `timetable`.`date_pattern_dept`;
DELETE FROM `timetable`.`demand_offr_type`;
DELETE FROM `timetable`.`department`;
DELETE FROM `timetable`.`departmental_instructor`;
DELETE FROM `timetable`.`dept_status_type`;
DELETE FROM `timetable`.`dept_to_tt_mgr`;
DELETE FROM `timetable`.`designator`;
DELETE FROM `timetable`.`dist_type_dept`;
DELETE FROM `timetable`.`distribution_object`;
DELETE FROM `timetable`.`distribution_pref`;
DELETE FROM `timetable`.`distribution_type`;
DELETE FROM `timetable`.`exact_time_mins`;
DELETE FROM `timetable`.`external_building`;
DELETE FROM `timetable`.`external_room`;
DELETE FROM `timetable`.`external_room_department`;
DELETE FROM `timetable`.`external_room_feature`;
DELETE FROM `timetable`.`free_time`;
DELETE FROM `timetable`.`history`;
DELETE FROM `timetable`.`individual_reservation`;
DELETE FROM `timetable`.`instr_offering_config`;
DELETE FROM `timetable`.`instructional_offering`;
DELETE FROM `timetable`.`itype_desc`;
DELETE FROM `timetable`.`jenrl`;
DELETE FROM `timetable`.`lastlike_course_demand`;
DELETE FROM `timetable`.`manager_settings`;
DELETE FROM `timetable`.`non_university_location`;
DELETE FROM `timetable`.`offr_consent_type`;
DELETE FROM `timetable`.`offr_group`;
DELETE FROM `timetable`.`offr_group_offering`;
DELETE FROM `timetable`.`plan_table`;
DELETE FROM `timetable`.`pos_acad_area_major`;
DELETE FROM `timetable`.`pos_acad_area_minor`;
DELETE FROM `timetable`.`pos_major`;
DELETE FROM `timetable`.`pos_minor`;
DELETE FROM `timetable`.`pos_reservation`;
DELETE FROM `timetable`.`position_code_to_type`;
DELETE FROM `timetable`.`position_type`;
DELETE FROM `timetable`.`preference_level`;
DELETE FROM `timetable`.`reservation_type`;
DELETE FROM `timetable`.`roles`;
DELETE FROM `timetable`.`room`;
DELETE FROM `timetable`.`room_dept`;
DELETE FROM `timetable`.`room_feature`;
DELETE FROM `timetable`.`room_feature_pref`;
DELETE FROM `timetable`.`room_group`;
DELETE FROM `timetable`.`room_group_pref`;
DELETE FROM `timetable`.`room_group_room`;
DELETE FROM `timetable`.`room_join_room_feature`;
DELETE FROM `timetable`.`room_pref`;
DELETE FROM `timetable`.`scheduling_subpart`;
DELETE FROM `timetable`.`sectioning_info`;
DELETE FROM `timetable`.`sessions`;
DELETE FROM `timetable`.`settings`;
DELETE FROM `timetable`.`solution`;
DELETE FROM `timetable`.`solver_gr_to_tt_mgr`;
DELETE FROM `timetable`.`solver_group`;
DELETE FROM `timetable`.`solver_info`;
DELETE FROM `timetable`.`solver_info_def`;
DELETE FROM `timetable`.`solver_parameter`;
DELETE FROM `timetable`.`solver_parameter_def`;
DELETE FROM `timetable`.`solver_parameter_group`;
DELETE FROM `timetable`.`solver_predef_setting`;
DELETE FROM `timetable`.`staff`;
DELETE FROM `timetable`.`student`;
DELETE FROM `timetable`.`student_acad_area`;
DELETE FROM `timetable`.`student_accomodation`;
DELETE FROM `timetable`.`student_class_enrl`;
DELETE FROM `timetable`.`student_enrl`;
DELETE FROM `timetable`.`student_enrl_msg`;
DELETE FROM `timetable`.`student_group`;
DELETE FROM `timetable`.`student_group_reservation`;
DELETE FROM `timetable`.`student_major`;
DELETE FROM `timetable`.`student_minor`;
DELETE FROM `timetable`.`student_sect_hist`;
DELETE FROM `timetable`.`student_status_type`;
DELETE FROM `timetable`.`student_to_acomodation`;
DELETE FROM `timetable`.`student_to_group`;
DELETE FROM `timetable`.`subject_area`;
DELETE FROM `timetable`.`time_pattern`;
DELETE FROM `timetable`.`time_pattern_days`;
DELETE FROM `timetable`.`time_pattern_dept`;
DELETE FROM `timetable`.`time_pattern_time`;
DELETE FROM `timetable`.`time_pref`;
DELETE FROM `timetable`.`timetable_manager`;
DELETE FROM `timetable`.`tmtbl_mgr_to_roles`;
DELETE FROM `timetable`.`user_data`;
DELETE FROM `timetable`.`users`;
DELETE FROM `timetable`.`waitlist`;

INSERT INTO `timetable`.`application_config`(`name`, `value`, `description`)
VALUES ('tmtbl.system_message', '', 'Message displayed to users when they first log in to Timetabling'),
  ('tmtbl.access_level', 'all', 'Access Levels: all | {dept code}(:{dept code})*');


INSERT INTO `timetable`.`course_credit_type`(`uniqueid`, `reference`, `label`, `abbreviation`, `legacy_crse_master_code`)
VALUES (1, 'collegiate', 'Collegiate Credit', NULL, ' '),
  (2, 'continuingEdUnits', 'Continuing Education Units', 'CEU', 'Q'),
  (3, 'equivalent', 'Equivalent Credit', 'EQV', 'E'),
  (4, 'mastersCredit', 'Masters Credit', 'MS', 'M'),
  (5, 'phdThesisCredit', 'Phd Thesis Credit', 'PhD', 'T');

INSERT INTO `timetable`.`course_credit_unit_type`(`uniqueid`, `reference`, `label`, `abbreviation`)
VALUES (6, 'semesterHours', 'Semester Hours', NULL);

INSERT INTO `timetable`.`crse_credit_format`(`uniqueid`, `reference`, `label`, `abbreviation`)
VALUES (7, 'arrangeHours', 'Arrange Hours', 'AH'),
  (8, 'fixedUnit', 'Fixed Unit', NULL),
  (9, 'variableMinMax', 'Variable Min/Max', NULL),
  (10, 'variableRange', 'Variable Range', NULL);

INSERT INTO `timetable`.`offr_consent_type`(`uniqueid`, `reference`, `label`)
VALUES (11, 'instructor', 'Consent of Instructor'),
  (12, 'department', 'Consent of Department');

INSERT INTO `timetable`.`dept_status_type`(`uniqueid`, `reference`, `label`, `status`, `apply`, `ord`)
VALUES (13, 'initial', 'Initial Data Load', 0, 1, 0),
  (14, 'input', 'Input Data Entry', 185, 1, 1),
  (15, 'timetabling', 'Timetabling', 441, 1, 2),
  (16, 'publish', 'Timetable Published', 9, 1, 3),
  (17, 'finished', 'Session Finished', 9, 1, 4),
  (18, 'dept_input', 'External Mgr. Input Data Entry', 135, 2, 5),
  (19, 'dept_timetabling', 'External Mgr. Timetabling', 423, 2, 6),
  (20, 'dept_publish', 'External Mgr. Timetable Published', 1, 2, 8),
  (21, 'dept_readonly_ni', 'External Mgr. Timetabling (No Instructor Assignments)', 391, 2, 7),
  (22, 'dept_readonly', 'Department Read Only', 9, 2, 9),
  (23, 'dept_edit', 'Department Allow Edit', 441, 2, 10);
  
INSERT INTO `timetable`.`settings`(`uniqueid`, `name`, `default_value`, `allowed_values`, `description`)
VALUES (24, 'jsConfirm', 'yes', 'yes,no', 'Display confirmation dialogs'),
  (25, 'name', 'last-initial', 'last-first,first-last,initial-last,last-initial,first-middle-last,short', 'Instructor name display format'),
  (26, 'timeGrid', 'vertical', 'horizontal,vertical,text', 'Time grid display format'),
  (27, 'cfgAutoCalc', 'yes', 'yes,no', 'Automatically calculate number of classes and room size when editing configuration'),
  (28, 'timeGridSize', 'Workdays x Daytime', 'Workdays x Daytime,All Week x Daytime,Workdays x Evening,All Week x Evening,All Week x All Times', 'Time grid default selection'),
  (29, 'inheritInstrPref', 'never', 'ask,always,never', 'Inherit instructor preferences on a class'),
  (30, 'showVarLimits', 'no', 'yes,no', 'Show the option to set variable class limits'),
  (31, 'keepSort', 'no', 'yes,no', 'Sort classes on detail pages'),
  (32, 'roomFeaturesInOneColumn', 'yes', 'yes,no', 'Display Room Features In One Column');

INSERT INTO `timetable`.`distribution_type`(`uniqueid`, `reference`, `label`, `sequencing_required`, `req_id`, `allowed_pref`, `description`, `abbreviation`, `instructor_pref`)
VALUES (33, 'BTB_DAY', 'Back-To-Back Day', '0', 26, 'P43210R', 'Classes must be offered on adjacent days and may be placed in different rooms.<BR>When prohibited or (strongly) discouraged: classes can not be taught on adjacent days. They also can not be taught on the same days. This means that there must be at least one day between these classes.', 'BTB Day', 0),
  (34, 'MIN_GRUSE(10x1h)', 'Minimize Use Of 1h Groups', '0', 27, 'P43210R', 'Minimize number of groups of time that are used by the given classes. The time is spread into the following 10 groups of one hour: 7:30a-8:30a, 8:30a-9:30a, 9:30a-10:30a, ... 4:30p-5:30p.', 'Min 1h Groups', 0),
  (35, 'MIN_GRUSE(5x2h)', 'Minimize Use Of 2h Groups', '0', 28, 'P43210R', 'Minimize number of groups of time that are used by the given classes. The time is spread into the following 5 groups of two hours: 7:30a-9:30a, 9:30a-11:30a, 11:30a-1:30p, 1:30p-3:30p, 3:30p-5:30p.', 'Min 2h Groups', 0),
  (36, 'MIN_GRUSE(3x3h)', 'Minimize Use Of 3h Groups', '0', 29, 'P43210R', 'Minimize number of groups of time that are used by the given classes. The time is spread into the following 3 groups: 7:30a-10:30a, 10:30a-2:30p, 2:30p-5:30p.', 'Min 3h Groups', 0),
  (37, 'MIN_GRUSE(2x5h)', 'Minimize Use Of 5h Groups', '0', 30, 'P43210R', 'Minimize number of groups of time that are used by the given classes. The time is spread into the following 2 groups: 7:30a-12:30a, 12:30a-5:30p.', 'Min 5h Groups', 0),
  (38, 'SAME_STUDENTS', 'Same Students', '0', 20, '210R', 'Given classes are treated as they are attended by the same students, i.e., they cannot overlap in time and if they are back-to-back the assigned rooms cannot be too far (student limit is used).', 'Same Students', 0),
  (39, 'SAME_INSTR', 'Same Instructor', '0', 21, '210R', 'Given classes are treated as they are taught by the same instructor, i.e., they cannot overlap in time and if they are back-to-back the assigned rooms cannot be too far (instructor limit is used).<BR>If the constraint is required and the classes are back-to-back, discouraged and strongly discouraged distances between assigned rooms are also considered.', 'Same Instr', 0),
  (40, 'CAN_SHARE_ROOM', 'Can Share Room', '0', 22, '2R', 'Given classes can share the room (use the room in the same time) if the room is big enough.', 'Share Room', 0),
  (41, 'SPREAD', 'Spread In Time', '0', 23, '2R', 'Given classes have to be spread in time (overlapping of the classes in time needs to be minimized).', 'Time Spread', 0),
  (42, 'MIN_ROOM_USE', 'Minimize Number Of Rooms Used', '0', 25, 'P43210R', 'Minimize number of rooms used by the given set of classes.', 'Min Rooms', 1),
  (43, 'PRECEDENCE', 'Precedence', '1', 24, 'P43210R', 'Given classes have to be taught in the given order (the first meeting of the first class has to end before the first meeting of the second class etc.)<BR>When prohibited or (strongly) discouraged: classes have to be taught in the order reverse to the given one', 'Precede', 0),
  (44, 'BTB', 'Back-To-Back & Same Room', '0', 1, 'P43210R', 'Classes must be offered in adjacent time segments and must be placed in the same room. Given classes must also be taught on the same days.<BR>When prohibited or (strongly) discouraged: classes cannot be back-to-back. There must be at least half-hour between these classes, and they must be taught on the same days and in the same room.', 'BTB Same Room', 1),
  (45, 'BTB_TIME', 'Back-To-Back', '0', 2, 'P43210R', 'Classes must be offered in adjacent time segments but may be placed in different rooms. Given classes must also be taught on the same days.<BR>When prohibited or (strongly) discouraged: no pair of classes can be taught back-to-back. They may not overlap in time, but must be taught on the same days. This means that there must be at least half-hour between these classes. ', 'BTB', 1),
  (46, 'SAME_TIME', 'Same Time', '0', 3, 'P43210R', 'Given classes must be taught at the same time of day (independent of the actual day the classes meet). For the classes of the same length, this is the same constraint as <i>same start</i>. For classes of different length, the shorter one cannot start before, nor end after, the longer one.<BR>When prohibited or (strongly) discouraged: one class may not meet on any day at a time of day that overlaps with that of the other. For example, one class can not meet M 7:30 while the other meets F 7:30. Note the difference here from the <i>different time</i> constraint that only prohibits the actual class meetings from overlapping.', 'Same Time', 0),
  (47, 'SAME_DAYS', 'Same Days', '0', 4, 'P43210R', 'Given classes must be taught on the same days. In case of classes of different time patterns, a class with fewer meetings must meet on a subset of the days used by the class with more meetings. For example, if one class pattern is 3x50, all others given in the constraint can only be taught on Monday, Wednesday, or Friday. For a 2x100 class MW, MF, WF is allowed but TTh is prohibited.<BR>When prohibited or (strongly) discouraged: any pair of classes classes cannot be taught on the same days (cannot overlap in days). For instance, if one class is MFW, the second has to be TTh.', 'Same Days', 1),
  (48, 'NHB(1)', '1 Hour Between', '0', 5, 'P43210R', 'Given classes must have exactly 1 hour in between the end of one and the beginning of another. As with the <i>back-to-back time</i> constraint, given classes must be taught on the same days.<BR>When prohibited or (strongly) discouraged: classes can not have 1 hour in between. They may not overlap in time but must be taught on the same days.', '1h Btw', 0),
  (49, 'NHB(2)', '2 Hours Between', '0', 6, 'P43210R', 'Given classes must have exactly 2 hours in between the end of one and the beginning of another. As with the <i>back-to-back time</i> constraint, given classes must be taught on the same days.<BR>When prohibited or (strongly) discouraged: classes can not have 2 hours in between. They may not overlap in time but must be taught on the same days.', '2h Btw', 0),
  (50, 'NHB(3)', '3 Hours Between', '0', 7, 'P43210R', 'Given classes must have exactly 3 hours in between the end of one and the beginning of another. As with the <i>back-to-back time</i> constraint, given classes must be taught on the same days.<BR>When prohibited or (strongly) discouraged: classes can not have 3 hours in between. They may not overlap in time but must be taught on the same days.', '3h Btw', 0),
  (51, 'NHB(4)', '4 Hours Between', '0', 8, 'P43210R', 'Given classes must have exactly 4 hours in between the end of one and the beginning of another. As with the <i>back-to-back time</i> constraint, given classes must be taught on the same days.<BR>When prohibited or (strongly) discouraged: classes can not have 4 hours in between. They may not overlap in time but must be taught on the same days.', '4h Btw', 0),
  (52, 'NHB(5)', '5 Hours Between', '0', 9, 'P43210R', 'Given classes must have exactly 5 hours in between the end of one and the beginning of another. As with the <i>back-to-back time</i> constraint, given classes must be taught on the same days.<BR>When prohibited or (strongly) discouraged: classes can not have 5 hours in between. They may not overlap in time but must be taught on the same days.', '5h Btw', 0),
  (53, 'NHB(6)', '6 Hours Between', '0', 10, 'P43210R', 'Given classes must have exactly 6 hours in between the end of one and the beginning of another. As with the <i>back-to-back time</i> constraint, given classes must be taught on the same days.<BR>When prohibited or (strongly) discouraged: classes can not have 6 hours in between. They may not overlap in time but must be taught on the same days.', '6h Btw', 0),
  (54, 'NHB(7)', '7 Hours Between', '0', 11, 'P43210R', 'Given classes must have exactly 7 hours in between the end of one and the beginning of another. As with the <i>back-to-back time</i> constraint, given classes must be taught on the same days.<BR>When prohibited or (strongly) discouraged: classes can not have 7 hours in between. They may not overlap in time but must be taught on the same days.', '7h Btw', 0),
  (55, 'NHB(8)', '8 Hours Between', '0', 12, 'P43210R', 'Given classes must have exactly 8 hours in between the end of one and the beginning of another. As with the <i>back-to-back time</i> constraint, given classes must be taught on the same days.<BR>When prohibited or (strongly) discouraged: classes can not have 8 hours in between. They may not overlap in time but must be taught on the same days.', '8h Btw', 0),
  (56, 'DIFF_TIME', 'Different Time', '0', 13, 'P43210R', 'Given classes cannot overlap in time. They may be taught at the same time of day if they are on different days. For instance, MF 7:30 is compatible with TTh 7:30.<BR>When prohibited or (strongly) discouraged: every pair of classes in the constraint must overlap in time.', 'Diff Time', 0),
  (57, 'NHB(1.5)', '90 Minutes Between', '0', 14, 'P43210R', 'Given classes must have exactly 90 minutes in between the end of one and the beginning of another. As with the <i>back-to-back time</i> constraint, given classes must be taught on the same days.<BR>When prohibited or (strongly) discouraged: classes can not have 90 minutes in between. They may not overlap in time but must be taught on the same days.', '90min Btw', 0),
  (58, 'NHB(4.5)', '4.5 Hours Between', '0', 15, 'P43210R', 'Given classes must have exactly 4.5 hours in between the end of one and the beginning of another. As with the <i>back-to-back time</i> constraint, given classes must be taught on the same days.<BR>When prohibited or (strongly) discouraged: classes can not have 4.5 hours in between. They may not overlap in time but must be taught on the same days.', '4.5h Btw', 0),
  (59, 'SAME_ROOM', 'Same Room', '0', 17, 'P43210R', 'Given classes must be taught in the same room.<BR>When prohibited or (strongly) discouraged: any pair of classes in the constraint cannot be taught in the same room.', 'Same Room', 1),
  (60, 'NHB_GTE(1)', 'At Least 1 Hour Between', '0', 18, 'P43210R', 'Given classes have to have 1 hour or more in between.<BR>When prohibited or (strongly) discouraged: given classes have to have less than 1 hour in between.', '>=1h Btw', 1),
  (61, 'SAME_START', 'Same Start Time', '0', 16, 'P43210R', 'Given classes must start during the same half-hour period of a day (independent of the actual day the classes meet). For instance, MW 7:30 is compatible with TTh 7:30 but not with MWF 8:00.<BR>When prohibited or (strongly) discouraged: any pair of classes in the given constraint cannot start during the same half-hour period of any day of the week.', 'Same Start', 0),
  (62, 'NHB_LT(6)', 'Less Than 6 Hours Between', '0', 19, 'P43210R', 'Given classes must have less than 6 hours from end of first class to the beginning of the next.  Given classes must also be taught on the same days.<BR>When prohibited or (strongly) discouraged: given classes must have 6 or more hours between. This constraint does not carry over from classes taught at the end of one day to the beginning of the next.', '<6h Btw', 1),
  (63, 'CH_NOTOVERLAP', 'Children Cannot Overlap', '0', 33, '210R', 'If parent classes do not overlap in time, children classes can not overlap in time as well.<br>Note: This constraint only needs to be put on the parent classes. Preferred configurations are Required All Classes or Pairwise (Strongly) Preferred.', 'Ch No Ovlap', 0),
  (64, 'NDB_GT_1', 'More Than 1 Day Between', '0', 32, 'P43210R', 'Given classes must have two or more days in between.<br>When prohibited or (strongly) discouraged: given classes must be offered on adjacent days or with at most one day in between.', '>1d Btw', 0),
  (65, 'FOLLOWING_DAY', 'Next Day', '1', 34, 'P43210R', 'The second class has to be placed on the following day of the first class (if the first class is on Friday, second class have to be on Monday).<br> When prohibited or (strongly) discouraged: The second class has to be placed on the previous day of the first class (if the first class is on Monday, second class have to be on Friday).<br> Note: This constraint works only between pairs of classes.', 'Next Day', 0),
  (66, 'EVERY_OTHER_DAY', 'Two Days After', '1', 35, 'P43210R', 'The second class has to be placed two days after the first class (Monday &rarr; Wednesday, Tuesday &rarr; Thurday, Wednesday &rarr; Friday, Thursday &rarr; Monday, Friday &rarr; Tuesday).<br> When prohibited or (strongly) discouraged: The second class has to be placed two days before the first class (Monday &rarr; Thursday, Tuesday &rarr; Friday, Wednesday &rarr; Monday, Thursday &rarr; Tuesday, Friday &rarr; Wednesday).<br> Note: This constraint works only between pairs of classes.', '2d After', 0),
  (67, 'MEET_WITH', 'Meet Together', '0', 31, '2R', 'Given classes are meeting together (same as if the given classes require constraints Can Share Room, Same Room, Same Time and Same Days all together).', 'Meet Together', 0);
  
INSERT INTO `timetable`.`itype_desc`(`itype`, `description`, `sis_ref`, `abbv`, `basic`)
VALUES (10, 'Lecture', 'lec', 'Lec  ', 1),
  (20, 'Recitation', 'rec', 'Rec  ', 1),
  (25, 'Presentation', 'prsn', 'Prsn ', 1),
  (30, 'Laboratory', 'lab', 'Lab  ', 1),
  (35, 'Laboratory Preparation', 'labP', 'LabP ', 1),
  (40, 'Studio', 'stdo', 'Stdo ', 1),
  (45, 'Distance Learning', 'dist', 'Dist ', 1),
  (50, 'Clinic', 'clin', 'Clin ', 1),
  (60, 'Experiential', 'expr', 'Expr ', 1),
  (70, 'Research', 'res', 'Res  ', 1),
  (80, 'Individual Study', 'ind', 'Ind  ', 1),
  (90, 'Practice Study Observation', 'pso', 'Pso  ', 1);

INSERT INTO `timetable`.`position_type`(`uniqueid`, `reference`, `label`, `sort_order`)
VALUES (68, 'PROF', 'Professor', 100),
  (69, 'ASSOC_PROF', 'Associate Professor', 200),
  (70, 'ASST_PROF', 'Assistant Professor', 300),
  (71, 'INSTRUCTOR', 'Instructor', 800),
  (72, 'CLIN_PROF', 'Clinical / Professional', 500),
  (73, 'CONT_LEC', 'Continuing Lecturer', 600),
  (74, 'LTD_LEC', 'Limited-Term Lecturer', 700),
  (75, 'VISIT_FAC', 'Visiting Faculty', 400),
  (76, 'POST_DOC', 'Post Doctoral', 1500),
  (77, 'ADJUNCT_FAC', 'Adjunct Faculty', 1000),
  (78, 'GRAD_TEACH_ASST', 'Graduate Teaching Assistant', 1200),
  (79, 'GRAD_LEC', 'Graduate Lecturer', 1100),
  (80, 'CLERICAL_STAFF', 'Clerical Staff', 1600),
  (81, 'SERVICE_STAFF', 'Service Staff', 1700),
  (82, 'FELLOWSHIP', 'Fellowship', 1800),
  (83, 'EMERITUS', 'Emeritus Faculty', 900),
  (84, 'OTHER', 'Other', 2000),
  (85, 'ADMIN_STAFF', 'Administrative/Professional Staff', 1300),
  (86, 'UNDRGRD_TEACH_ASST', 'Undergrad Teaching Assistant', 1400);

  INSERT INTO `timetable`.`preference_level`(`pref_id`, `pref_prolog`, `pref_name`, `uniqueid`)
VALUES (1, 'R', 'Required', 1),
  (2, '-2', 'Strongly Preferred', 2),
  (3, '-1', 'Preferred', 3),
  (4, '0', 'Neutral', 4),
  (5, '1', 'Discouraged', 5),
  (6, '2', 'Strongly Discouraged', 6),
  (7, 'P', 'Prohibited', 7);

INSERT INTO `timetable`.`reservation_type`(`uniqueid`, `reference`, `label`)
VALUES (87, 'perm', 'Permanent'),
  (88, 'temp', 'Temporary'),
  (89, 'info', NULL);

INSERT INTO `timetable`.`roles`(`role_id`, `reference`, `abbv`)
VALUES (1, 'Administrator', 'Administrator'),
  (21, 'Dept Sched Mgr', 'Department Schedule Manager'),
  (41, 'View All', 'View All User');
  
INSERT INTO `timetable`.`exact_time_mins`(`uniqueid`, `mins_min`, `mins_max`, `nr_slots`, `break_time`)
VALUES (405, 0, 0, 0, 0),
  (406, 1, 5, 1, 0),
  (407, 6, 10, 2, 0),
  (408, 11, 15, 4, 0),
  (409, 16, 20, 5, 0),
  (410, 21, 25, 6, 0),
  (411, 26, 30, 7, 0),
  (412, 31, 35, 8, 15),
  (413, 36, 40, 10, 15),
  (414, 41, 45, 11, 15),
  (415, 46, 50, 12, 10),
  (416, 51, 55, 13, 15),
  (417, 56, 60, 14, 10),
  (418, 61, 65, 16, 15),
  (419, 66, 70, 17, 15),
  (420, 71, 75, 18, 15),
  (421, 76, 80, 19, 15),
  (422, 81, 85, 20, 15),
  (423, 86, 90, 21, 15),
  (424, 91, 95, 23, 15),
  (425, 96, 100, 24, 10),
  (426, 101, 105, 25, 15),
  (427, 106, 110, 26, 15),
  (428, 111, 115, 28, 15),
  (429, 116, 120, 29, 15),
  (430, 121, 125, 30, 15),
  (431, 126, 130, 31, 15),
  (432, 131, 135, 32, 15),
  (433, 136, 140, 34, 15),
  (434, 141, 145, 35, 15),
  (435, 146, 150, 36, 10),
  (436, 151, 155, 37, 15),
  (437, 156, 160, 38, 15),
  (438, 161, 165, 40, 15),
  (439, 166, 170, 41, 15),
  (440, 171, 175, 42, 15),
  (441, 176, 180, 43, 15),
  (442, 181, 185, 44, 15),
  (443, 186, 190, 46, 15),
  (444, 191, 195, 47, 15),
  (445, 196, 200, 48, 10),
  (446, 201, 205, 49, 15),
  (447, 206, 210, 50, 15),
  (448, 211, 215, 52, 15),
  (449, 216, 220, 53, 15),
  (450, 221, 225, 54, 15),
  (451, 226, 230, 55, 15),
  (452, 231, 235, 56, 15),
  (453, 236, 240, 58, 15),
  (454, 241, 245, 59, 15),
  (455, 246, 250, 60, 10),
  (456, 251, 255, 61, 15),
  (457, 256, 260, 62, 15),
  (458, 261, 265, 64, 15),
  (459, 266, 270, 65, 15),
  (460, 271, 275, 66, 15),
  (461, 276, 280, 67, 15),
  (462, 281, 285, 68, 15),
  (463, 286, 290, 70, 15),
  (464, 291, 295, 71, 15),
  (465, 296, 300, 72, 10),
  (466, 301, 305, 73, 15),
  (467, 306, 310, 74, 15),
  (468, 311, 315, 76, 15),
  (469, 316, 320, 77, 15),
  (470, 321, 325, 78, 15),
  (471, 326, 330, 79, 15),
  (472, 331, 335, 80, 15),
  (473, 336, 340, 82, 15),
  (474, 341, 345, 83, 15),
  (475, 346, 350, 84, 10),
  (476, 351, 355, 85, 15),
  (477, 356, 360, 86, 15),
  (478, 361, 365, 88, 15),
  (479, 366, 370, 89, 15),
  (480, 371, 375, 90, 15),
  (481, 376, 380, 91, 5),
  (482, 381, 385, 92, 15),
  (483, 386, 390, 94, 15),
  (484, 391, 395, 95, 15),
  (485, 396, 400, 96, 10),
  (486, 401, 405, 97, 15),
  (487, 406, 410, 98, 15),
  (488, 411, 415, 100, 15),
  (489, 416, 420, 101, 15),
  (490, 421, 425, 102, 15),
  (491, 426, 430, 103, 15),
  (492, 431, 435, 104, 15),
  (493, 436, 440, 106, 15),
  (494, 441, 445, 107, 15),
  (495, 446, 450, 108, 10),
  (496, 451, 455, 109, 15),
  (497, 456, 460, 110, 15),
  (498, 461, 465, 112, 15),
  (499, 466, 470, 113, 15),
  (500, 471, 475, 114, 15),
  (501, 476, 480, 115, 15),
  (502, 481, 485, 116, 15),
  (503, 486, 490, 118, 15),
  (504, 491, 495, 119, 15),
  (505, 496, 500, 120, 10),
  (506, 501, 505, 121, 15),
  (507, 506, 510, 122, 15),
  (508, 511, 515, 124, 15),
  (509, 516, 520, 125, 15),
  (510, 521, 525, 126, 15),
  (511, 526, 530, 127, 15),
  (512, 531, 535, 128, 15),
  (513, 536, 540, 130, 15),
  (514, 541, 545, 131, 15),
  (515, 546, 550, 132, 10),
  (516, 551, 555, 133, 15),
  (517, 556, 560, 134, 15),
  (518, 561, 565, 136, 15),
  (519, 566, 570, 137, 15),
  (520, 571, 575, 138, 15),
  (521, 576, 580, 139, 15),
  (522, 581, 585, 140, 15),
  (523, 586, 590, 142, 15),
  (524, 591, 595, 143, 15),
  (525, 596, 600, 144, 10),
  (526, 601, 605, 145, 15),
  (527, 606, 610, 146, 15),
  (528, 611, 615, 148, 15),
  (529, 616, 620, 149, 15),
  (530, 621, 625, 150, 15),
  (531, 626, 630, 151, 15),
  (532, 631, 635, 152, 15),
  (533, 636, 640, 154, 15),
  (534, 641, 645, 155, 15),
  (535, 646, 650, 156, 10),
  (536, 651, 655, 157, 15),
  (537, 656, 660, 158, 15),
  (538, 661, 665, 160, 15),
  (539, 666, 670, 161, 15),
  (540, 671, 675, 162, 15),
  (541, 676, 680, 163, 15),
  (542, 681, 685, 164, 15),
  (543, 686, 690, 166, 15),
  (544, 691, 695, 167, 15),
  (545, 696, 700, 168, 10),
  (546, 701, 705, 169, 15),
  (547, 706, 710, 170, 15),
  (548, 711, 715, 172, 15),
  (549, 716, 720, 173, 15);

INSERT INTO `timetable`.`solver_info_def`(`uniqueid`, `name`, `description`, `implementation`)
VALUES (1, 'GlobalInfo', 'Global solution information table', 'org.unitime.timetable.solver.ui.PropertiesInfo'),
  (2, 'CBSInfo', 'Conflict-based statistics', 'org.unitime.timetable.solver.ui.ConflictStatisticsInfo'),
  (3, 'AssignmentInfo', 'Preferences of a single assignment', 'org.unitime.timetable.solver.ui.AssignmentPreferenceInfo'),
  (4, 'DistributionInfo', 'Distribution (group constraint) preferences', 'org.unitime.timetable.solver.ui.GroupConstraintInfo'),
  (5, 'JenrlInfo', 'Student conflicts', 'org.unitime.timetable.solver.ui.JenrlInfo'),
  (6, 'LogInfo', 'Solver Log', 'org.unitime.timetable.solver.ui.LogInfo'),
  (7, 'BtbInstructorInfo', 'Back-to-back instructor preferences', 'org.unitime.timetable.solver.ui.BtbInstructorConstraintInfo');

INSERT INTO `timetable`.`solver_parameter`(`uniqueid`, `value`, `solver_param_def_id`, `solution_id`, `solver_predef_setting_id`)
VALUES 
  (1, 'MPP', 1, NULL, 1),
  (2, 'on', 3, NULL, 1),
  (3, 'false', 4, NULL, 1),
  (4, '0', 17, NULL, 1),
  (5, 'false', 4, NULL, 2),
  (6, 'Save and Unload', 2, NULL, 2),
  (7, '0', 54, NULL, 2),
  (8, '0.0', 56, NULL, 3),
  (9, '0.0', 57, NULL, 3),
  (10, '0.0', 58, NULL, 3),
  (11, '0.0', 59, NULL, 3),
  (12, '0.0', 60, NULL, 3),
  (13, '0.0', 61, NULL, 3),
  (14, '0.0', 62, NULL, 3),
  (15, 'false', 4, NULL, 3),
  (16, '0.0', 63, NULL, 3),
  (17, '0.0', 64, NULL, 3),
  (18, '0.0', 65, NULL, 3),
  (19, '0.0', 66, NULL, 3),
  (20, '0.0', 67, NULL, 3),
  (21, '1.0', 94, NULL, 3),
  (22, '1.0', 95, NULL, 3),
  (23, '0.0', 97, NULL, 3),
  (24, '0.0', 98, NULL, 3),
  (25, '0.0', 99, NULL, 3),
  (26, '0.0', 101, NULL, 3),
  (27, '0.0', 102, NULL, 3),
  (28, '0.0', 103, NULL, 3),
  (29, '0.0', 104, NULL, 3),
  (30, '0.0', 105, NULL, 3),
  (31, '0.0', 106, NULL, 3),
  (32, '0.0', 107, NULL, 3),
  (33, '0.0', 140, NULL, 3),
  (34, '0.0', 143, NULL, 3),
  (35, '0.0', 108, NULL, 3),
  (40, 'DIFF_TIME', 202, NULL, 1),
  (41, 'on', 55, NULL, 3),
  (42, '20.0', 99, NULL, 4),
  (43, '7.6', 56, NULL, 4),
  (44, '2.4', 57, NULL, 4),
  (45, 'on', 261, NULL, 4),
  (50, 'on', 13, NULL, 4),
  (51, '300', 54, NULL, 3),
  (52, 'false', 10, NULL, 3);

INSERT INTO `timetable`.`solver_parameter_def`(`uniqueid`, `name`, `default_value`, `description`, `type`, `ord`, `visible`, `solver_param_group_id`)
VALUES (109, 'Placement.NrAssignmentsWeight2', '0.0', 'Number of assignments weight (level 2)', 'double', 20, 0, 10),
  (110, 'Placement.NrConflictsWeight2', '0.0', 'Number of conflicts weight (level 2)', 'double', 21, 1, 10),
  (111, 'Placement.WeightedConflictsWeight2', '0.0', 'Weighted conflicts weight (CBS, level 2)', 'double', 22, 1, 10),
  (112, 'Placement.NrPotentialConflictsWeight2', '0.0', 'Number of potential conflicts weight (CBS, level 2)', 'double', 23, 0, 10),
  (113, 'Placement.MPP_DeltaInitialAssignmentWeight2', '%Comparator.PerturbationPenaltyWeight%', 'Delta initial assigments weight (MPP, level 2)', 'double', 24, 0, 10),
  (114, 'Placement.NrHardStudConfsWeight2', '%Comparator.HardStudentConflictWeight%', 'Hard student conflicts weight (level 2)', 'double', 25, 0, 10),
  (115, 'Placement.NrStudConfsWeight2', '%Comparator.StudentConflictWeight%', 'Student conflicts weight (level 2)', 'double', 26, 0, 10),
  (116, 'Placement.TimePreferenceWeight2', '%Comparator.TimePreferenceWeight%', 'Time preference weight (level 2)', 'double', 27, 0, 10),
  (117, 'Placement.DeltaTimePreferenceWeight2', '0.0', 'Time preference delta weight (level 2)', 'double', 28, 0, 10),
  (118, 'Placement.ConstrPreferenceWeight2', '%Comparator.ContrPreferenceWeight%', 'Constraint preference weight (level 2)', 'double', 29, 0, 10),
  (119, 'Placement.RoomPreferenceWeight2', '%Comparator.RoomPreferenceWeight%', 'Room preference weight (level 2)', 'double', 30, 0, 10),
  (120, 'Placement.UselessSlotsWeight2', '%Comparator.UselessSlotWeight%', 'Useless slot weight (level 2)', 'double', 31, 0, 10),
  (121, 'Placement.TooBigRoomWeight2', '%Comparator.TooBigRoomWeight%', 'Too big room weight (level 2)', 'double', 32, 0, 10),
  (122, 'Placement.DistanceInstructorPreferenceWeight2', '%Comparator.DistanceInstructorPreferenceWeight%', 'Back-to-back instructor preferences weight (level 2)', 'double', 33, 0, 10),
  (123, 'Placement.DeptSpreadPenaltyWeight2', '%Comparator.DeptSpreadPenaltyWeight%', 'Department balancing: penalty of when a slot over initial allowance is used (level 2)', 'double', 34, 0, 10),
  (124, 'Placement.ThresholdKoef2', '0.1', 'Threshold koeficient (level 2)', 'double', 35, 0, 10),
  (125, 'Placement.NrAssignmentsWeight3', '0.0', 'Number of assignments weight (level 3)', 'double', 36, 0, 10),
  (221, 'Neighbour.Class', 'net.sf.cpsolver.coursett.heuristics.NeighbourSelectionWithSuggestions', 'Neighbour Selection', 'text', 7, 0, 11),
  (222, 'Neighbour.SuggestionProbability', '0.1', 'Probability of using suggestions', 'double', 0, 1, 61),
  (223, 'Neighbour.SuggestionTimeout', '500', 'Suggestions timeout', 'integer', 1, 1, 61),
  (224, 'Neighbour.SuggestionDepth', '4', 'Suggestions depth', 'integer', 2, 1, 61),
  (225, 'Neighbour.SuggestionProbabilityAllAssigned', '0.5', 'Probability of using suggestions (when all classes are assigned)', 'double', 3, 1, 61),
  (241, 'General.IgnoreRoomSharing', 'false', 'Ignore Room Sharing', 'boolean', 12, 1, 2),
  (126, 'Placement.NrConflictsWeight3', '0.0', 'Number of conflicts weight (level 3)', 'double', 37, 0, 10),
  (127, 'Placement.WeightedConflictsWeight3', '0.0', 'Weighted conflicts weight (CBS, level 3)', 'double', 38, 0, 10),
  (128, 'Placement.NrPotentialConflictsWeight3', '0.0', 'Number of potential conflicts weight (CBS, level 3)', 'double', 39, 0, 10),
  (129, 'Placement.MPP_DeltaInitialAssignmentWeight3', '0.0', 'Delta initial assigments weight (MPP, level 3)', 'double', 40, 0, 10),
  (130, 'Placement.NrHardStudConfsWeight3', '0.0', 'Hard student conflicts weight (level 3)', 'double', 41, 0, 10),
  (131, 'Placement.NrStudConfsWeight3', '0.0', 'Student conflicts weight (level 3)', 'double', 42, 0, 10),
  (132, 'Placement.TimePreferenceWeight3', '0.0', 'Time preference weight (level 3)', 'double', 43, 0, 10),
  (133, 'Placement.DeltaTimePreferenceWeight3', '0.0', 'Time preference delta weight (level 3)', 'double', 44, 0, 10),
  (134, 'Placement.ConstrPreferenceWeight3', '0.0', 'Constraint preference weight (level 3)', 'double', 45, 0, 10),
  (135, 'Placement.RoomPreferenceWeight3', '0.0', 'Room preference weight (level 3)', 'double', 46, 0, 10),
  (136, 'Placement.UselessSlotsWeight3', '0.0', 'Useless slot weight (level 3)', 'double', 47, 0, 10),
  (137, 'Placement.TooBigRoomWeight3', '0.0', 'Too big room weight (level 3)', 'double', 48, 0, 10),
  (138, 'Placement.DistanceInstructorPreferenceWeight3', '0.0', 'Back-to-back instructor preferences weight (level 3)', 'double', 49, 0, 10),
  (139, 'Placement.DeptSpreadPenaltyWeight3', '0.0', 'Department balancing: penalty of when a slot over initial allowance is used (level 3)', 'double', 50, 0, 10),
  (140, 'Placement.SpreadPenaltyWeight1', '0.1', 'Same subpart balancing: penalty of when a slot over initial allowance is used (level 1)', 'double', 51, 1, 10),
  (141, 'Placement.SpreadPenaltyWeight2', '%Comparator.SpreadPenaltyWeight%', 'Same subpart balancing: penalty of when a slot over initial allowance is used (level 2)', 'double', 52, 0, 10),
  (142, 'Placement.SpreadPenaltyWeight3', '0.0', 'Same subpart balancing: penalty of when a slot over initial allowance is used (level 3)', 'double', 53, 0, 10),
  (143, 'Placement.NrCommitedStudConfsWeight1', '0.5', 'Commited student conlict weight (level 1)', 'double', 54, 1, 10),
  (144, 'Placement.NrCommitedStudConfsWeight2', '%Comparator.CommitedStudentConflictWeight%', 'Commited student conlict weight (level 2)', 'double', 55, 0, 10),
  (145, 'Placement.NrCommitedStudConfsWeight3', '0.0', 'Commited student conlict weight (level 3)', 'double', 56, 0, 10),
  (146, 'SearchIntensification.IterationLimit', '100', 'Iteration limit (number of iteration after which the search is restarted to the best known solution)', 'integer', 0, 1, 13),
  (147, 'SearchIntensification.ResetInterval', '5', 'Number of consecutive restarts to increase iteration limit (if this number of restarts is reached, iteration limit is increased)', 'integer', 1, 1, 13),
  (148, 'SearchIntensification.MultiplyInterval', '2', 'Iteration limit incremental coefficient (when a better solution is found, iteration limit is changed back to initial)', 'integer', 2, 1, 13),
  (149, 'SearchIntensification.Multiply', '2', 'Reset conflict-based statistics (number of consecutive restarts after which CBS is cleared, zero means no reset of CBS)', 'integer', 3, 1, 13),
  (150, 'General.SearchIntensification', 'true', 'Use search intensification', 'boolean', 6, 1, 2),
  (162, 'General.SettingsId', '-1', 'Settings Id', 'integer', 8, 0, 2),
  (201, 'General.SolverWarnings', NULL, 'Solver Warnings', 'text', 10, 0, 2),
  (202, 'General.AutoSameStudentsConstraint', 'SAME_STUDENTS', 'Automatic same student constraint', 'enum(SAME_STUDENTS,DIFF_TIME)', 11, 1, 2),
  (203, 'Instructor.NoPreferenceLimit', '0.0', 'Instructor Constraint: No Preference Limit', 'double', 0, 1, 41),
  (204, 'Instructor.DiscouragedLimit', '5.0', 'Instructor Constraint: Discouraged Limit', 'double', 1, 1, 41),
  (205, 'Instructor.ProhibitedLimit', '20.0', 'Instructor Constraint: Prohibited Limit', 'double', 2, 1, 41),
  (206, 'Student.DistanceLimit', '67.0', 'Student Conflict: Distance Limit', 'double', 3, 1, 41),
  (207, 'Student.DistanceLimit75min', '100.0', 'Student Conflict: Distance Limit (after 75min class)', 'double', 4, 1, 41),
  (161, 'Placement.CanUnassingSingleton', 'true', 'Can unassign a singleton value', 'boolean', 57, 1, 10),
  (181, 'TimePreferences.Weight', '0.0', 'Time preferences weight', 'double', 0, 1, 21),
  (182, 'TimePreferences.Pref', '2222222222222224222222222222222223333222222222222222222222222224222222222222222223333222222222222222222222222224222222222222222223333222222222222222222222222224222222222222222223333222222222222222222222222224222222222222222223333222222222222222222222', 'Time preferences', 'timepref', 1, 1, 21),
  (1, 'Basic.Mode', 'Initial', 'Solver mode', 'enum(Initial,MPP)', 0, 1, 1),
  (2, 'Basic.WhenFinished', 'No Action', 'When finished', 'enum(No Action,Save,Save as New,Save and Unload,Save as New and Unload)', 1, 1, 1),
  (3, 'Basic.DisobeyHard', 'false', 'Allow breaking of hard constraints', 'boolean', 2, 1, 1),
  (4, 'General.SwitchStudents', 'true', 'Student final sectioning', 'boolean', 3, 1, 1),
  (5, 'General.DeptBalancing', 'false', 'Use departmental balancing', 'boolean', 9, 1, 2),
  (6, 'General.CBS', 'true', 'Use conflict-based statistics', 'boolean', 0, 1, 2),
  (7, 'General.SaveBestUnassigned', '-1', 'Minimal number of unassigned variables to save best solution found (-1 always save)', 'integer', 1, 0, 2),
  (9, 'General.UseDistanceConstraints', 'true', 'Use building distances', 'boolean', 2, 0, 2),
  (10, 'General.Spread', 'true', 'Use same subpart balancing', 'boolean', 3, 1, 2),
  (11, 'General.AutoSameStudents', 'true', 'Use automatic same_students constraints', 'boolean', 4, 1, 2),
  (12, 'General.NormalizedPrefDecreaseFactor', '0.77', 'Time preference normalization decrease factor', 'double', 5, 1, 2),
  (13, 'Global.LoadStudentEnrlsFromSolution', 'false', 'Load student enrollments from solution<BR>(faster, but it ignores new classes)', 'boolean', 7, 1, 2),
  (14, 'DeptBalancing.SpreadFactor', '1.2', 'Initial allowance of the slots for a particular time', 'double', 0, 1, 5),
  (15, 'DeptBalancing.Unassignments2Weaken', '0', 'Increase the initial allowance when it causes the given number of unassignments', 'integer', 1, 1, 5),
  (16, 'Spread.SpreadFactor', '1.2', 'Initial allowance of the slots for a particular time', 'double', 0, 1, 12),
  (17, 'Spread.Unassignments2Weaken', '50', 'Increase the initial allowance when it causes the given number of unassignments', 'integer', 1, 1, 12),
  (18, 'ConflictStatistics.Ageing', '1.0', 'Ageing (koef)', 'double', 0, 0, 6),
  (19, 'ConflictStatistics.AgeingHalfTime', '0', 'Ageing -- half time (number of iteration)', 'integer', 1, 0, 6),
  (20, 'ConflictStatistics.Print', 'true', 'Print conflict statistics', 'boolean', 2, 0, 6),
  (21, 'ConflictStatistics.PrintInterval', '-1', 'Number of iterations to print CBS (-1 just keep in memory and save within the solution)', 'integer', 3, 0, 6),
  (22, 'PerturbationCounter.Class', 'net.sf.cpsolver.coursett.heuristics.UniversalPerturbationsCounter', 'Perturbations counter', 'text', 0, 0, 11),
  (23, 'Termination.Class', 'net.sf.cpsolver.ifs.termination.MPPTerminationCondition', 'Termination condition', 'text', 1, 0, 11),
  (24, 'Comparator.Class', 'net.sf.cpsolver.coursett.heuristics.TimetableComparator', 'Solution comparator', 'text', 2, 0, 11),
  (25, 'Variable.Class', 'net.sf.cpsolver.coursett.heuristics.LectureSelection', 'Lecture selection', 'text', 3, 0, 11),
  (26, 'Value.Class', 'net.sf.cpsolver.coursett.heuristics.PlacementSelection', 'Placement selection', 'text', 4, 0, 11),
  (27, 'TimetableLoader', 'org.unitime.timetable.solver.TimetableDatabaseLoader', 'Loader class', 'text', 5, 0, 11),
  (28, 'TimetableSaver', 'org.unitime.timetable.solver.TimetableDatabaseSaver', 'Saver class', 'text', 6, 0, 11),
  (29, 'Perturbations.DifferentPlacement', '0.0', 'Different value than initial is assigned', 'double', 0, 1, 4),
  (30, 'Perturbations.AffectedStudentWeight', '0.1', 'Number of students which are enrolled in a class which is placed to a different location than initial', 'double', 1, 1, 4),
  (32, 'Perturbations.AffectedInstructorWeight', '0.0', 'Number of classes which are placed to a different room than initial', 'double', 3, 1, 4),
  (33, 'Perturbations.AffectedInstructorWeight', '0.0', 'Number of classes which are placed to a different room than initial', 'double', 4, 1, 4),
  (34, 'Perturbations.DifferentRoomWeight', '0.0', 'Number of classes which are placed to a different room than initial', 'double', 5, 1, 4),
  (35, 'Perturbations.DifferentBuildingWeight', '0.0', 'Number of classes which are placed to a different building than initial', 'double', 6, 1, 4),
  (36, 'Perturbations.DifferentTimeWeight', '0.0', 'Number of classes which are placed in a different time than initial', 'double', 7, 1, 4),
  (37, 'Perturbations.DifferentDayWeight', '0.0', 'Number of classes which are placed in a different days than initial', 'double', 8, 1, 4),
  (38, 'Perturbations.DifferentHourWeight', '0.0', 'Number of classes which are placed in a different hours than initial', 'double', 9, 1, 4),
  (39, 'Perturbations.DeltaStudentConflictsWeight', '0.0', 'Difference of student conflicts of classes assigned to current placements instead of initial placements', 'double', 10, 1, 4),
  (40, 'Perturbations.NewStudentConflictsWeight', '0.0', 'New created student conflicts -- particular students are taken into account', 'double', 11, 1, 4),
  (41, 'Perturbations.TooFarForInstructorsWeight', '0.0', 'New placement of a class is too far from the intial placement (instructor-wise)', 'double', 12, 1, 4),
  (42, 'Perturbations.TooFarForStudentsWeight', '0.0', 'New placement of a class is too far from the intial placement (student-wise)', 'double', 13, 1, 4),
  (43, 'Perturbations.DeltaInstructorDistancePreferenceWeight', '0.0', 'Difference between number of instructor distance preferences of the initial ', 'double', 14, 1, 4),
  (44, 'Perturbations.DeltaRoomPreferenceWeight', '0.0', 'Difference between room preferences of the initial and the current solution', 'double', 15, 1, 4),
  (45, 'Perturbations.DeltaTimePreferenceWeight', '0.0', 'Difference between time preferences of the initial and the current solution', 'double', 16, 1, 4),
  (46, 'Perturbations.AffectedStudentByTimeWeight', '0.0', 'Number of students which are enrolled in a class which is placed to a different time than initial', 'double', 17, 1, 4),
  (47, 'Perturbations.AffectedInstructorByTimeWeight', '0.0', 'Number of instructors which are assigned to classes which are placed to different time than initial', 'double', 18, 1, 4),
  (48, 'Perturbations.AffectedStudentByRoomWeight', '0.0', 'Number of students which are enrolled in a class which is placed to a different room than initial', 'double', 19, 1, 4),
  (49, 'Perturbations.AffectedInstructorByRoomWeight', '0.0', 'Number of instructors which are assigned to classes which are placed to different room than initial', 'double', 20, 1, 4),
  (50, 'Perturbations.AffectedStudentByBldgWeight', '0.0', 'Number of students which are enrolled in a class which is placed to a different building than initial', 'double', 21, 1, 4),
  (51, 'Perturbations.AffectedInstructorByBldgWeight', '0.0', 'Number of instructors which are assigned to classes which are placed to different building than initial', 'double', 22, 1, 4),
  (52, 'Termination.MinPerturbances', '-1', 'Minimal allowed number of perturbances (-1 not use)', 'integer', 0, 0, 7),
  (53, 'Termination.MaxIters', '-1', 'Maximal number of iteration', 'integer', 1, 0, 7),
  (54, 'Termination.TimeOut', '1800', 'Maximal solver time (in sec)', 'integer', 2, 1, 7),
  (55, 'Termination.StopWhenComplete', 'false', 'Stop computation when a complete solution is found', 'boolean', 3, 1, 7),
  (56, 'Comparator.HardStudentConflictWeight', '0.8', 'Weight of hard student conflict', 'double', 0, 1, 8),
  (57, 'Comparator.StudentConflictWeight', '0.2', 'Weight of student conflict', 'double', 1, 1, 8),
  (58, 'Comparator.TimePreferenceWeight', '0.3', 'Time preferences weight', 'double', 2, 1, 8),
  (59, 'Comparator.ContrPreferenceWeight', '2.0', 'Distribution preferences weight', 'double', 3, 1, 8),
  (60, 'Comparator.RoomPreferenceWeight', '1.0', 'Room preferences weight', 'double', 4, 1, 8),
  (61, 'Comparator.UselessSlotWeight', '0.1', 'Useless slots weight', 'double', 5, 1, 8),
  (62, 'Comparator.TooBigRoomWeight', '0.1', 'Too big room weight', 'double', 6, 1, 8),
  (63, 'Comparator.DistanceInstructorPreferenceWeight', '1.0', 'Back-to-back instructor preferences weight', 'double', 7, 1, 8),
  (64, 'Comparator.PerturbationPenaltyWeight', '1.0', 'Perturbation penalty weight', 'double', 8, 1, 8),
  (65, 'Comparator.DeptSpreadPenaltyWeight', '1.0', 'Department balancing weight', 'double', 9, 1, 8),
  (66, 'Comparator.SpreadPenaltyWeight', '1.0', 'Same subpart balancing weight', 'double', 10, 1, 8),
  (67, 'Comparator.CommitedStudentConflictWeight', '1.0', 'Commited student conflict weight', 'double', 11, 1, 8),
  (68, 'Lecture.RouletteWheelSelection', 'true', 'Roulette wheel selection', 'boolean', 0, 0, 9),
  (69, 'Lecture.RandomWalkProb', '1.0', 'Random walk probability', 'double', 1, 0, 9),
  (70, 'Lecture.DomainSizeWeight', '30.0', 'Domain size weight', 'double', 2, 0, 9),
  (71, 'Lecture.NrAssignmentsWeight', '10.0', 'Number of assignments weight', 'double', 3, 0, 9),
  (72, 'Lecture.InitialAssignmentWeight', '20.0', 'Initial assignment weight', 'double', 4, 0, 9),
  (73, 'Lecture.NrConstraintsWeight', '0.0', 'Number of constraint weight', 'double', 5, 0, 9),
  (74, 'Lecture.HardStudentConflictWeight', '%Comparator.HardStudentConflictWeight%', 'Hard student conflict weight', 'double', 6, 0, 9),
  (75, 'Lecture.StudentConflictWeight', '%Comparator.StudentConflictWeight%', 'Student conflict weight', 'double', 7, 0, 9),
  (76, 'Lecture.TimePreferenceWeight', '%Comparator.TimePreferenceWeight%', 'Time preference weight', 'double', 8, 0, 9),
  (77, 'Lecture.ContrPreferenceWeight', '%Comparator.ContrPreferenceWeight%', 'Constraint preference weight', 'double', 9, 0, 9),
  (78, 'Lecture.RoomPreferenceWeight', '%Comparator.RoomPreferenceWeight%', 'Room preference weight', 'double', 10, 0, 9),
  (79, 'Lecture.UselessSlotWeight', '%Comparator.UselessSlotWeight%', 'Useless slot weight', 'double', 11, 0, 9),
  (81, 'Lecture.TooBigRoomWeight', '%Comparator.TooBigRoomWeight%', 'Too big room weight', 'double', 12, 0, 9),
  (82, 'Lecture.DistanceInstructorPreferenceWeight', '%Comparator.DistanceInstructorPreferenceWeight%', 'Back-to-back instructor preferences weight', 'double', 13, 0, 9),
  (83, 'Lecture.DeptSpreadPenaltyWeight', '%Comparator.DeptSpreadPenaltyWeight%', 'Department balancing weight', 'double', 14, 0, 9),
  (84, 'Lecture.SelectionSubSet', 'true', 'Selection among subset of lectures (faster)', 'boolean', 15, 0, 9),
  (85, 'Lecture.SelectionSubSetMinSize', '10', 'Minimal subset size', 'integer', 16, 0, 9),
  (86, 'Lecture.SelectionSubSetPart', '0.2', 'Subset size in percentage of all lectures available for selection', 'double', 17, 0, 9),
  (87, 'Lecture.SpreadPenaltyWeight', '%Comparator.SpreadPenaltyWeight%', 'Same subpart balancing weight', 'double', 18, 0, 9),
  (88, 'Lecture.CommitedStudentConflictWeight', '%Comparator.CommitedStudentConflictWeight%', 'Commited student conflict weight', 'double', 19, 0, 9),
  (89, 'Placement.RandomWalkProb', '0.00', 'Random walk probability', 'double', 0, 1, 10),
  (90, 'Placement.MPP_InitialProb', '0.20', 'MPP initial selection probability ', 'double', 1, 1, 10),
  (91, 'Placement.MPP_Limit', '-1', 'MPP limit (-1 for no limit)', 'integer', 2, 1, 10),
  (92, 'Placement.MPP_PenaltyLimit', '-1.0', 'Limit of the perturbations penalty (-1 for no limit)', 'double', 3, 1, 10),
  (93, 'Placement.NrAssignmentsWeight1', '0.0', 'Number of assignments weight (level 1)', 'double', 4, 0, 10),
  (94, 'Placement.NrConflictsWeight1', '1.0', 'Number of conflicts weight (level 1)', 'double', 5, 1, 10),
  (95, 'Placement.WeightedConflictsWeight1', '2.0', 'Weighted conflicts weight (CBS, level 1)', 'double', 6, 1, 10),
  (96, 'Placement.NrPotentialConflictsWeight1', '0.0', 'Number of potential conflicts weight (CBS, level 1)', 'double', 7, 0, 10),
  (97, 'Placement.MPP_DeltaInitialAssignmentWeight1', '0.1', 'Delta initial assigments weight (MPP, level 1)', 'double', 8, 1, 10),
  (98, 'Placement.NrHardStudConfsWeight1', '0.3', 'Hard student conflicts weight (level 1)', 'double', 9, 1, 10),
  (99, 'Placement.NrStudConfsWeight1', '0.05', 'Student conflicts weight (level 1)', 'double', 10, 1, 10),
  (100, 'Placement.TimePreferenceWeight1', '0.0', 'Time preference weight (level 1)', 'double', 11, 0, 10),
  (101, 'Placement.DeltaTimePreferenceWeight1', '0.2', 'Time preference delta weight (level 1)', 'double', 12, 1, 10),
  (102, 'Placement.ConstrPreferenceWeight1', '0.25', 'Constraint preference weight (level 1)', 'double', 13, 1, 10),
  (103, 'Placement.RoomPreferenceWeight1', '0.1', 'Room preference weight (level 1)', 'double', 14, 1, 10),
  (104, 'Placement.UselessSlotsWeight1', '0.0', 'Useless slot weight (level 1)', 'double', 15, 1, 10),
  (105, 'Placement.TooBigRoomWeight1', '0.01', 'Too big room weight (level 1)', 'double', 16, 1, 10),
  (106, 'Placement.DistanceInstructorPreferenceWeight1', '0.1', 'Back-to-back instructor preferences weight (level 1)', 'double', 17, 1, 10),
  (107, 'Placement.DeptSpreadPenaltyWeight1', '0.1', 'Department balancing: penalty of when a slot over initial allowance is used (level 1)', 'double', 18, 1, 10),
  (108, 'Placement.ThresholdKoef1', '0.1', 'Threshold koeficient (level 1)', 'double', 19, 1, 10),
  (261, 'OnFlySectioning.Enabled', 'false', 'Enable on fly sectioning (if enabled, students will be resectioned after each iteration)', 'boolean', 1, 1, 81),
  (262, 'OnFlySectioning.Recursive', 'true', 'Recursively resection lectures affected by a student swap', 'boolean', 2, 1, 81),
  (263, 'OnFlySectioning.ConfigAsWell', 'false', 'Resection students between configurations as well', 'boolean', 3, 1, 81);

INSERT INTO `timetable`.`solver_parameter_group`(`uniqueid`, `name`, `description`, `condition`, `ord`)
VALUES (41, 'Distance', 'Distances', NULL, 14),
  (21, 'TimePreferences', 'Default Time Preferences', NULL, 13),
  (1, 'Basic', 'Basic Settings', NULL, 0),
  (2, 'General', 'General Settings', NULL, 1),
  (3, 'MPP', 'Minimal-perturbation Setting', NULL, 2),
  (4, 'Perturbations', 'Perturbation Penalty', NULL, 3),
  (5, 'DepartmentSpread', 'Departmental Balancing', NULL, 4),
  (6, 'ConflictStatistics', 'Conflict-based Statistics', NULL, 5),
  (7, 'Termination', 'Termination Conditions', NULL, 6),
  (8, 'Comparator', 'Solution Comparator Weights', NULL, 7),
  (9, 'Variable', 'Lecture Selection', NULL, 8),
  (10, 'Value', 'Placement Selection', NULL, 9),
  (11, 'Classes', 'Implementations', NULL, 10),
  (12, 'Spread', 'Same Subpart Balancing', NULL, 11),
  (13, 'SearchIntensification', 'Search Intensification', NULL, 12),
  (61, 'Neighbour', 'Neighbour Selection', NULL, 15),
  (81, 'OnFlySectioning', 'On Fly Student Sectioning', NULL, 16);

INSERT INTO `timetable`.`solver_predef_setting`(`uniqueid`, `name`, `description`, `appearance`)
VALUES (1, 'Default.Interactive', 'Interactive', 0),
  (2, 'Default.Validate', 'Validate', 1),
  (3, 'Default.Check', 'Check', 1),
  (4, 'Default.Solver', 'Default', 1);
  
INSERT INTO `timetable`.`users`(`username`, `password`, `external_uid`) 
VALUES ('admin', 'ISMvKXpXpadDiUoOSoAfww==', '1');

INSERT INTO `timetable`.`timetable_manager`(`uniqueid`, `external_uid`, `first_name`, `middle_name`, `last_name`, `email_address`, `last_modified_time`)
VALUES (100, '1', 'Change', NULL, 'Me', 'change-me@unitime.org', NULL);

INSERT INTO `timetable`.`tmtbl_mgr_to_roles`(`uniqueid`, `manager_id`, `role_id`, `is_primary`)
VALUES (101, 100, 1, 1);

INSERT INTO `timetable`.`sessions`(`academic_initiative`, `session_begin_date_time`, `classes_end_date_time`, `session_end_date_time`, `uniqueid`, `holidays`, `def_datepatt_id`, `status_type`, `last_modified_time`, `academic_year`, `academic_term`)
VALUES ('default', '2007-08-20 00:00:00', '2007-12-08 23:59:00', '2007-12-15 23:59:00', 102, '00000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000022000000000000000000000000000000000000000000222200000000000000000000000000000000000000000000000000000000000000000000', 103, 14, NULL, '2007', 'Fal');

INSERT INTO `timetable`.`date_pattern`(`uniqueid`, `name`, `pattern`, `offset`, `type`, `visible`, `session_id`)
VALUES (103, 'Full Term', '111111011111100111110111111011111101111110111111000111101111110111111011111101111110111111011000001111110111111', 0, 0, 1, 102);

INSERT INTO `timetable`.`department`(`uniqueid`, `session_id`, `abbreviation`, `name`, `dept_code`, `external_uid`, `rs_color`, `external_manager`, `external_mgr_label`, `external_mgr_abbv`, `solver_group_id`, `status_type`, `dist_priority`, `allow_req_time`, `allow_req_room`, `last_modified_time`)
VALUES (104, 102, 'Tmtbl', 'Timetabling Office', '0001', NULL, NULL, 0, NULL, NULL, 106, NULL, 0, 0, 0, NULL);

INSERT INTO `timetable`.`dept_to_tt_mgr`(`timetable_mgr_id`, `department_id`)
VALUES (100, 104);

INSERT INTO `timetable`.`room_group`(`uniqueid`, `session_id`, `name`, `abbv`, `description`, `global`, `default_group`, `department_id`)
VALUES (105, 102, 'Classroom', 'Classroom', 'Default room group for departmental classes', 1, 1, NULL);

INSERT INTO `timetable`.`solver_group`(`uniqueid`, `name`, `abbv`, `session_id`)
VALUES (106, 'Central Timetable', 'Central', 102);

INSERT INTO `timetable`.`solver_gr_to_tt_mgr`(`solver_group_id`, `timetable_mgr_id`)
VALUES (106, 100);

update `timetable`.`hibernate_unique_key` set `next_hi`=1000;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;

-- End of script