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

use mysql;

drop user timetable@localhost;

create user timetable@localhost identified by password '*2E46E61A1C47ADC309CADC6DF8D89654F013D3DD';

grant all on timetable.* to timetable@localhost;

flush privileges;

set @saved_fk_checks=@@foreign_key_checks, foreign_key_checks=0;
set @saved_cs_client = @@character_set_client, character_set_client = utf8;

drop database if exists timetable;

create database timetable character set utf8;

use timetable;

create table `academic_area` (
  `uniqueid` decimal(20,0) not null,
  `session_id` decimal(20,0) default null,
  `academic_area_abbreviation` varchar(10) default null,
  `long_title` varchar(100) default null,
  `external_uid` varchar(40) default null,
  primary key (`uniqueid`),
  UNIQUE key `uk_academic_area` (`session_id`,`academic_area_abbreviation`),
  key `idx_academic_area_abbv` (`academic_area_abbreviation`,`session_id`),
  constraint `fk_academic_area_session` foreign key (`session_id`) references `sessions` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `academic_classification` (
  `uniqueid` decimal(20,0) not null,
  `session_id` decimal(20,0) default null,
  `code` varchar(10) default null,
  `name` varchar(50) default null,
  `external_uid` varchar(40) default null,
  primary key (`uniqueid`),
  key `idx_academic_clasf_code` (`code`,`session_id`),
  key `fk_acad_class_session` (`session_id`),
  constraint `fk_acad_class_session` foreign key (`session_id`) references `sessions` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `application_config` (
  `name` varchar(255) not null,
  `value` varchar(4000) default null,
  `description` varchar(500) default null,
  primary key (`name`)
) ENGINE=InnoDB;

create table `assigned_instructors` (
  `assignment_id` decimal(20,0) not null,
  `instructor_id` decimal(20,0) not null,
  `last_modified_time` datetime default null,
  primary key (`assignment_id`,`instructor_id`),
  key `idx_assigned_instructors` (`assignment_id`),
  key `fk_assigned_instrs_instructor` (`instructor_id`),
  constraint `fk_assigned_instrs_assignment` foreign key (`assignment_id`) references `assignment` (`uniqueid`) on delete cascade,
  constraint `fk_assigned_instrs_instructor` foreign key (`instructor_id`) references `departmental_instructor` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `assigned_rooms` (
  `assignment_id` decimal(20,0) not null,
  `room_id` decimal(20,0) not null,
  `last_modified_time` datetime default null,
  primary key (`assignment_id`,`room_id`),
  key `idx_assigned_rooms` (`assignment_id`),
  constraint `fk_assigned_rooms_assignment` foreign key (`assignment_id`) references `assignment` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `assignment` (
  `uniqueid` decimal(20,0) not null,
  `days` bigint(10) default null,
  `slot` bigint(10) default null,
  `time_pattern_id` decimal(20,0) default null,
  `solution_id` decimal(20,0) default null,
  `class_id` decimal(20,0) default null,
  `class_name` varchar(100) default null,
  `last_modified_time` datetime default null,
  `date_pattern_id` decimal(20,0) default null,
  primary key (`uniqueid`),
  key `idx_assignment_class` (`class_id`),
  key `idx_assignment_solution_index` (`solution_id`),
  key `idx_assignment_time_pattern` (`time_pattern_id`),
  key `fk_assignment_date_pattern` (`date_pattern_id`),
  constraint `fk_assignment_date_pattern` foreign key (`date_pattern_id`) references `date_pattern` (`uniqueid`) on delete set null,
  constraint `fk_assignment_class` foreign key (`class_id`) references `class_` (`uniqueid`) on delete cascade,
  constraint `fk_assignment_solution` foreign key (`solution_id`) references `solution` (`uniqueid`) on delete cascade,
  constraint `fk_assignment_time_pattern` foreign key (`time_pattern_id`) references `time_pattern` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `building` (
  `uniqueid` decimal(20,0) not null,
  `session_id` decimal(20,0) default null,
  `abbreviation` varchar(10) default null,
  `name` varchar(100) default null,
  `coordinate_x` double default null,
  `coordinate_y` double default null,
  `external_uid` varchar(40) default null,
  primary key (`uniqueid`),
  UNIQUE key `uk_building` (`session_id`,`abbreviation`),
  constraint `fk_building_session` foreign key (`session_id`) references `sessions` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `building_pref` (
  `uniqueid` decimal(20,0) not null,
  `owner_id` decimal(20,0) default null,
  `pref_level_id` decimal(20,0) default null,
  `bldg_id` decimal(20,0) default null,
  `distance_from` int(5) default null,
  `last_modified_time` datetime default null,
  primary key (`uniqueid`),
  key `idx_building_pref_bldg` (`bldg_id`),
  key `idx_building_pref_level` (`pref_level_id`),
  key `idx_building_pref_owner` (`owner_id`),
  constraint `fk_building_pref_bldg` foreign key (`bldg_id`) references `building` (`uniqueid`) on delete cascade,
  constraint `fk_building_pref_level` foreign key (`pref_level_id`) references `preference_level` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `change_log` (
  `uniqueid` decimal(20,0) not null,
  `session_id` decimal(20,0) default null,
  `manager_id` decimal(20,0) default null,
  `time_stamp` datetime default null,
  `obj_type` varchar(255) default null,
  `obj_uid` decimal(20,0) default null,
  `obj_title` varchar(255) default null,
  `subj_area_id` decimal(20,0) default null,
  `department_id` decimal(20,0) default null,
  `source` varchar(50) default null,
  `operation` varchar(50) default null,
  `detail` longblob,
  primary key (`uniqueid`),
  key `idx_change_log_department` (`department_id`),
  key `idx_change_log_object` (`obj_type`,`obj_uid`),
  key `idx_change_log_sessionmgr` (`session_id`,`manager_id`),
  key `idx_change_log_subjarea` (`subj_area_id`),
  key `fk_change_log_manager` (`manager_id`),
  constraint `fk_change_log_department` foreign key (`department_id`) references `department` (`uniqueid`) on delete cascade,
  constraint `fk_change_log_manager` foreign key (`manager_id`) references `timetable_manager` (`uniqueid`) on delete cascade,
  constraint `fk_change_log_session` foreign key (`session_id`) references `sessions` (`uniqueid`) on delete cascade,
  constraint `fk_change_log_subjarea` foreign key (`subj_area_id`) references `subject_area` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `class_` (
  `uniqueid` decimal(20,0) not null,
  `subpart_id` decimal(20,0) default null,
  `expected_capacity` int(4) default null,
  `nbr_rooms` int(4) default null,
  `parent_class_id` decimal(20,0) default null,
  `owner_id` decimal(20,0) default null,
  `room_capacity` int(4) default null,
  `notes` varchar(1000) default null,
  `date_pattern_id` decimal(20,0) default null,
  `managing_dept` decimal(20,0) default null,
  `display_instructor` int(1) default null,
  `sched_print_note` varchar(2000) default null,
  `class_suffix` varchar(10) default null,
  `display_in_sched_book` int(1) default '1',
  `max_expected_capacity` int(4) default null,
  `room_ratio` double default null,
  `section_number` int(5) default null,
  `last_modified_time` datetime default null,
  `uid_rolled_fwd_from` decimal(20,0) default null,
  `external_uid` varchar(40) default null,
  `enrollment` int(4) default null,
  primary key (`uniqueid`),
  key `idx_class_datepatt` (`date_pattern_id`),
  key `idx_class_managing_dept` (`managing_dept`),
  key `idx_class_parent` (`parent_class_id`),
  key `idx_class_subpart_id` (`subpart_id`),
  constraint `fk_class_datepatt` foreign key (`date_pattern_id`) references `date_pattern` (`uniqueid`) on delete cascade,
  constraint `fk_class_parent` foreign key (`parent_class_id`) references `class_` (`uniqueid`) on delete cascade,
  constraint `fk_class_scheduling_subpart` foreign key (`subpart_id`) references `scheduling_subpart` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `class_instructor` (
  `uniqueid` decimal(20,0) not null,
  `class_id` decimal(20,0) default null,
  `instructor_id` decimal(20,0) default null,
  `percent_share` int(3) default null,
  `is_lead` int(1) default null,
  `last_modified_time` datetime default null,
  primary key (`uniqueid`),
  key `idx_class_instructor_class` (`class_id`),
  key `idx_class_instructor_instr` (`instructor_id`),
  constraint `fk_class_instructor_class` foreign key (`class_id`) references `class_` (`uniqueid`) on delete cascade,
  constraint `fk_class_instructor_instr` foreign key (`instructor_id`) references `departmental_instructor` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `class_waitlist` (
  `uniqueid` decimal(20,0) not null,
  `student_id` decimal(20,0) default null,
  `course_request_id` decimal(20,0) default null,
  `class_id` decimal(20,0) default null,
  `type` bigint(10) default '0',
  `timestamp` datetime default null,
  primary key (`uniqueid`),
  key `idx_class_waitlist_class` (`class_id`),
  key `idx_class_waitlist_req` (`course_request_id`),
  key `idx_class_waitlist_student` (`student_id`),
  constraint `fk_class_waitlist_class` foreign key (`class_id`) references `class_` (`uniqueid`) on delete cascade,
  constraint `fk_class_waitlist_request` foreign key (`course_request_id`) references `course_request` (`uniqueid`) on delete cascade,
  constraint `fk_class_waitlist_student` foreign key (`student_id`) references `student` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `constraint_info` (
  `assignment_id` decimal(20,0) not null,
  `solver_info_id` decimal(20,0) not null,
  primary key (`solver_info_id`,`assignment_id`),
  key `idx_constraint_info` (`assignment_id`),
  constraint `fk_constraint_info_assignment` foreign key (`assignment_id`) references `assignment` (`uniqueid`) on delete cascade,
  constraint `fk_constraint_info_solver` foreign key (`solver_info_id`) references `solver_info` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `course_catalog` (
  `uniqueid` decimal(20,0) not null,
  `session_id` decimal(20,0) default null,
  `external_uid` varchar(40) default null,
  `subject` varchar(10) default null,
  `course_nbr` varchar(10) default null,
  `title` varchar(100) default null,
  `perm_id` varchar(20) default null,
  `approval_type` varchar(20) default null,
  `designator_req` int(1) default null,
  `prev_subject` varchar(10) default null,
  `prev_crs_nbr` varchar(10) default null,
  `credit_type` varchar(20) default null,
  `credit_unit_type` varchar(20) default null,
  `credit_format` varchar(20) default null,
  `fixed_min_credit` double default null,
  `max_credit` double default null,
  `frac_credit_allowed` int(1) default null,
  primary key (`uniqueid`),
  key `idx_course_catalog` (`session_id`,`subject`,`course_nbr`)
) ENGINE=InnoDB;

create table `course_credit_type` (
  `uniqueid` decimal(20,0) not null,
  `reference` varchar(20) default null,
  `label` varchar(60) default null,
  `abbreviation` varchar(10) default null,
  `legacy_crse_master_code` varchar(10) default null,
  primary key (`uniqueid`),
  UNIQUE key `uk_course_credit_type_ref` (`reference`)
) ENGINE=InnoDB;

create table `course_credit_unit_config` (
  `uniqueid` decimal(20,0) not null,
  `credit_format` varchar(20) default null,
  `owner_id` decimal(20,0) default null,
  `credit_type` decimal(20,0) default null,
  `credit_unit_type` decimal(20,0) default null,
  `defines_credit_at_course_level` int(1) default null,
  `fixed_units` double default null,
  `min_units` double default null,
  `max_units` double default null,
  `fractional_incr_allowed` int(1) default null,
  `instr_offr_id` decimal(20,0) default null,
  `last_modified_time` datetime default null,
  primary key (`uniqueid`),
  key `idx_crs_crdt_unit_cfg_crd_type` (`credit_type`),
  key `idx_crs_crdt_unit_cfg_io_own` (`instr_offr_id`),
  key `idx_crs_crdt_unit_cfg_owner` (`owner_id`),
  constraint `fk_crs_crdt_unit_cfg_crdt_type` foreign key (`credit_type`) references `course_credit_type` (`uniqueid`) on delete cascade,
  constraint `fk_crs_crdt_unit_cfg_io_own` foreign key (`instr_offr_id`) references `instructional_offering` (`uniqueid`) on delete cascade,
  constraint `fk_crs_crdt_unit_cfg_owner` foreign key (`owner_id`) references `scheduling_subpart` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `course_credit_unit_type` (
  `uniqueid` decimal(20,0) not null,
  `reference` varchar(20) default null,
  `label` varchar(60) default null,
  `abbreviation` varchar(10) default null,
  primary key (`uniqueid`),
  UNIQUE key `uk_crs_crdt_unit_type_ref` (`reference`)
) ENGINE=InnoDB;

create table `course_demand` (
  `uniqueid` decimal(20,0) not null,
  `student_id` decimal(20,0) default null,
  `priority` bigint(10) default null,
  `waitlist` int(1) default null,
  `is_alternative` int(1) default null,
  `timestamp` datetime default null,
  `free_time_id` decimal(20,0) default null,
  `changed_by` varchar(40) default null,
  primary key (`uniqueid`),
  key `idx_course_demand_free_time` (`free_time_id`),
  key `idx_course_demand_student` (`student_id`),
  constraint `fk_course_demand_free_time` foreign key (`free_time_id`) references `free_time` (`uniqueid`) on delete cascade,
  constraint `fk_course_demand_student` foreign key (`student_id`) references `student` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `course_offering` (
  `uniqueid` decimal(20,0) not null,
  `course_nbr` varchar(10) default null,
  `is_control` int(1) default null,
  `perm_id` varchar(20) default null,
  `proj_demand` bigint(10) default null,
  `instr_offr_id` decimal(20,0) default null,
  `subject_area_id` decimal(20,0) default null,
  `title` varchar(90) default null,
  `schedule_book_note` varchar(1000) default null,
  `demand_offering_id` decimal(20,0) default null,
  `demand_offering_type` decimal(20,0) default null,
  `nbr_expected_stdents` bigint(10) default '0',
  `external_uid` varchar(40) default null,
  `last_modified_time` datetime default null,
  `uid_rolled_fwd_from` decimal(20,0) default null,
  `lastlike_demand` bigint(10) default '0',
  `enrollment` bigint(10) default null,
  `reservation` bigint(10) default null,
  `course_type_id` decimal(20,0) default null,
  `consent_type` decimal(20,0) default null,
  primary key (`uniqueid`),
  UNIQUE key `uk_course_offering_subj_crs` (`course_nbr`,`subject_area_id`),
  key `idx_course_offering_control` (`is_control`),
  key `idx_course_offering_demd_offr` (`demand_offering_id`),
  key `idx_course_offering_instr_offr` (`instr_offr_id`),
  key `fk_course_offering_subj_area` (`subject_area_id`),
  key `fk_course_offering_type` (`course_type_id`),
  key `fk_course_consent_type` (`consent_type`),
  constraint `fk_course_consent_type` foreign key (`consent_type`) references `offr_consent_type` (`uniqueid`) on delete cascade,
  constraint `fk_course_offering_demand_offr` foreign key (`demand_offering_id`) references `course_offering` (`uniqueid`) on delete set null,
  constraint `fk_course_offering_instr_offr` foreign key (`instr_offr_id`) references `instructional_offering` (`uniqueid`) on delete cascade,
  constraint `fk_course_offering_subj_area` foreign key (`subject_area_id`) references `subject_area` (`uniqueid`) on delete cascade,
  constraint `fk_course_offering_type` foreign key (`course_type_id`) references `course_type` (`uniqueid`) on delete set null
) ENGINE=InnoDB;

create table `course_request` (
  `uniqueid` decimal(20,0) not null,
  `course_demand_id` decimal(20,0) default null,
  `course_offering_id` decimal(20,0) default null,
  `ord` bigint(10) default null,
  `allow_overlap` int(1) default null,
  `credit` bigint(10) default '0',
  primary key (`uniqueid`),
  key `idx_course_request_demand` (`course_demand_id`),
  key `idx_course_request_offering` (`course_offering_id`),
  constraint `fk_course_request_demand` foreign key (`course_demand_id`) references `course_demand` (`uniqueid`) on delete cascade,
  constraint `fk_course_request_offering` foreign key (`course_offering_id`) references `course_offering` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `course_request_option` (
  `uniqueid` decimal(20,0) not null,
  `course_request_id` decimal(20,0) default null,
  `option_type` bigint(10) default null,
  `value` longblob,
  primary key (`uniqueid`),
  key `idx_course_request_option_req` (`course_request_id`),
  constraint `fk_course_request_options_req` foreign key (`course_request_id`) references `course_request` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `course_subpart_credit` (
  `uniqueid` decimal(20,0) not null,
  `course_catalog_id` decimal(20,0) default null,
  `subpart_id` varchar(10) default null,
  `credit_type` varchar(20) default null,
  `credit_unit_type` varchar(20) default null,
  `credit_format` varchar(20) default null,
  `fixed_min_credit` double default null,
  `max_credit` double default null,
  `frac_credit_allowed` int(1) default null,
  primary key (`uniqueid`),
  key `fk_subpart_cred_crs` (`course_catalog_id`),
  constraint `fk_subpart_cred_crs` foreign key (`course_catalog_id`) references `course_catalog` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `course_type` (
  `uniqueid` decimal(20,0) not null,
  `reference` varchar(20) not null,
  `label` varchar(60) not null,
  primary key (`uniqueid`)
) ENGINE=InnoDB;

create table `crse_credit_format` (
  `uniqueid` decimal(20,0) not null,
  `reference` varchar(20) default null,
  `label` varchar(60) default null,
  `abbreviation` varchar(10) default null,
  primary key (`uniqueid`),
  UNIQUE key `uk_crse_credit_format_ref` (`reference`)
) ENGINE=InnoDB;

create table `curriculum` (
  `uniqueid` decimal(20,0) not null,
  `abbv` varchar(20) not null,
  `name` varchar(60) not null,
  `acad_area_id` decimal(20,0) default null,
  `dept_id` decimal(20,0) not null,
  primary key (`uniqueid`),
  UNIQUE key `pk_curricula` (`uniqueid`),
  key `fk_curriculum_acad_area` (`acad_area_id`),
  key `fk_curriculum_dept` (`dept_id`),
  constraint `fk_curriculum_acad_area` foreign key (`acad_area_id`) references `academic_area` (`uniqueid`) on delete cascade,
  constraint `fk_curriculum_dept` foreign key (`dept_id`) references `department` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `curriculum_clasf` (
  `uniqueid` decimal(20,0) not null,
  `curriculum_id` decimal(20,0) not null,
  `name` varchar(20) not null,
  `acad_clasf_id` decimal(20,0) default null,
  `nr_students` bigint(10) not null,
  `ord` bigint(10) not null,
  `students` longtext default null,
  primary key (`uniqueid`),
  UNIQUE key `pk_curricula_clasf` (`uniqueid`),
  key `fk_curriculum_clasf_acad_clasf` (`acad_clasf_id`),
  key `fk_curriculum_clasf_curriculum` (`curriculum_id`),
  constraint `fk_curriculum_clasf_acad_clasf` foreign key (`acad_clasf_id`) references `academic_classification` (`uniqueid`) on delete cascade,
  constraint `fk_curriculum_clasf_curriculum` foreign key (`curriculum_id`) references `curriculum` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `curriculum_course` (
  `uniqueid` decimal(20,0) not null,
  `course_id` decimal(20,0) not null,
  `cur_clasf_id` decimal(20,0) not null,
  `pr_share` double not null,
  `ord` bigint(10) not null,
  primary key (`uniqueid`),
  UNIQUE key `pk_curricula_course` (`uniqueid`),
  key `fk_curriculum_course_clasf` (`cur_clasf_id`),
  key `fk_curriculum_course_course` (`course_id`),
  constraint `fk_curriculum_course_clasf` foreign key (`cur_clasf_id`) references `curriculum_clasf` (`uniqueid`) on delete cascade,
  constraint `fk_curriculum_course_course` foreign key (`course_id`) references `course_offering` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `curriculum_course_group` (
  `group_id` decimal(20,0) not null,
  `cur_course_id` decimal(20,0) not null,
  primary key (`group_id`,`cur_course_id`),
  key `fk_cur_course_group_course` (`cur_course_id`),
  constraint `fk_cur_course_group_course` foreign key (`cur_course_id`) references `curriculum_course` (`uniqueid`) on delete cascade,
  constraint `fk_cur_course_group_group` foreign key (`group_id`) references `curriculum_group` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `curriculum_group` (
  `uniqueid` decimal(20,0) not null,
  `name` varchar(20) not null,
  `color` varchar(20) default null,
  `type` bigint(10) not null,
  `curriculum_id` decimal(20,0) not null,
  primary key (`uniqueid`),
  key `fk_curriculum_group_curriculum` (`curriculum_id`),
  constraint `fk_curriculum_group_curriculum` foreign key (`curriculum_id`) references `curriculum` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `curriculum_major` (
  `curriculum_id` decimal(20,0) not null,
  `major_id` decimal(20,0) not null,
  primary key (`curriculum_id`,`major_id`),
  key `fk_curriculum_major_major` (`major_id`),
  constraint `fk_curriculum_major_curriculum` foreign key (`curriculum_id`) references `curriculum` (`uniqueid`) on delete cascade,
  constraint `fk_curriculum_major_major` foreign key (`major_id`) references `pos_major` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `curriculum_rule` (
  `uniqueid` decimal(20,0) not null,
  `acad_area_id` decimal(20,0) not null,
  `major_id` decimal(20,0) default null,
  `acad_clasf_id` decimal(20,0) not null,
  `projection` double not null,
  primary key (`uniqueid`),
  key `idx_cur_rule_areadept` (`acad_area_id`,`acad_clasf_id`),
  key `fk_cur_rule_acad_clasf` (`acad_clasf_id`),
  key `fk_cur_rule_major` (`major_id`),
  constraint `fk_cur_rule_acad_area` foreign key (`acad_area_id`) references `academic_area` (`uniqueid`) on delete cascade,
  constraint `fk_cur_rule_acad_clasf` foreign key (`acad_clasf_id`) references `academic_classification` (`uniqueid`) on delete cascade,
  constraint `fk_cur_rule_major` foreign key (`major_id`) references `pos_major` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `date_mapping` (
  `uniqueid` decimal(20,0) not null,
  `session_id` decimal(20,0) not null,
  `class_date` bigint(10) not null,
  `event_date` bigint(10) not null,
  `note` varchar(1000) default null,
  primary key (`uniqueid`),
  key `fk_event_date_session` (`session_id`),
  constraint `fk_event_date_session` foreign key (`session_id`) references `sessions` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `date_pattern` (
  `uniqueid` decimal(20,0) not null,
  `name` varchar(50) default null,
  `pattern` varchar(366) default null,
  `offset` bigint(10) default null,
  `type` bigint(10) default null,
  `visible` int(1) default null,
  `session_id` decimal(20,0) default null,
  primary key (`uniqueid`),
  key `idx_date_pattern_session` (`session_id`),
  constraint `fk_date_pattern_session` foreign key (`session_id`) references `sessions` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `date_pattern_dept` (
  `dept_id` decimal(20,0) not null,
  `pattern_id` decimal(20,0) not null,
  primary key (`dept_id`,`pattern_id`),
  key `fk_date_pattern_dept_date` (`pattern_id`),
  constraint `fk_date_pattern_dept_date` foreign key (`pattern_id`) references `date_pattern` (`uniqueid`) on delete cascade,
  constraint `fk_date_pattern_dept_dept` foreign key (`dept_id`) references `department` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `date_pattern_parent` (
  `date_pattern_id` decimal(20,0) not null,
  `parent_id` decimal(20,0) not null,
  primary key (`date_pattern_id`,`parent_id`),
  key `fk_date_patt_parent_parent` (`parent_id`),
  constraint `fk_date_patt_parent_date_patt` foreign key (`date_pattern_id`) references `date_pattern` (`uniqueid`) on delete cascade,
  constraint `fk_date_patt_parent_parent` foreign key (`parent_id`) references `date_pattern` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `date_pattern_pref` (
  `uniqueid` decimal(20,0) not null,
  `owner_id` decimal(20,0) not null,
  `pref_level_id` decimal(20,0) not null,
  `date_pattern_id` decimal(20,0) not null,
  primary key (`uniqueid`),
  key `fk_datepatt_pref_pref_level` (`pref_level_id`),
  key `fk_datepatt_pref_date_pat` (`date_pattern_id`),
  constraint `fk_datepatt_pref_date_pat` foreign key (`date_pattern_id`) references `date_pattern` (`uniqueid`) on delete cascade,
  constraint `fk_datepatt_pref_pref_level` foreign key (`pref_level_id`) references `preference_level` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `demand_offr_type` (
  `uniqueid` decimal(20,0) not null,
  `reference` varchar(20) default null,
  `label` varchar(60) default null,
  primary key (`uniqueid`),
  UNIQUE key `uk_demand_offr_type_label` (`label`),
  UNIQUE key `uk_demand_offr_type_ref` (`reference`)
) ENGINE=InnoDB;

create table `department` (
  `uniqueid` decimal(20,0) not null,
  `session_id` decimal(20,0) default null,
  `abbreviation` varchar(20) default null,
  `name` varchar(100) default null,
  `dept_code` varchar(50) default null,
  `external_uid` varchar(40) default null,
  `rs_color` varchar(6) default null,
  `external_manager` int(1) default null,
  `external_mgr_label` varchar(30) default null,
  `external_mgr_abbv` varchar(10) default null,
  `solver_group_id` decimal(20,0) default null,
  `status_type` decimal(20,0) default null,
  `dist_priority` bigint(10) default '0',
  `allow_req_time` int(1) default '0',
  `allow_req_room` int(1) default '0',
  `last_modified_time` datetime default null,
  `allow_req_dist` int(1) default '0',
  `allow_events` int(1) default '0',
  primary key (`uniqueid`),
  UNIQUE key `uk_department_dept_code` (`session_id`,`dept_code`),
  key `idx_department_solver_grp` (`solver_group_id`),
  key `idx_department_status_type` (`status_type`),
  constraint `fk_department_solver_group` foreign key (`solver_group_id`) references `solver_group` (`uniqueid`) on delete cascade,
  constraint `fk_department_status_type` foreign key (`status_type`) references `dept_status_type` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `departmental_instructor` (
  `uniqueid` decimal(20,0) not null,
  `external_uid` varchar(40) default null,
  `career_acct` varchar(20) default null,
  `lname` varchar(100) default null,
  `fname` varchar(100) default null,
  `mname` varchar(100) default null,
  `pos_code_type` decimal(20,0) default null,
  `note` varchar(20) default null,
  `department_uniqueid` decimal(20,0) default null,
  `ignore_too_far` int(1) default '0',
  `last_modified_time` datetime default null,
  `email` varchar(200) default null,
  `role_id` decimal(20,0) default null,
  primary key (`uniqueid`),
  key `idx_dept_instr_dept` (`department_uniqueid`),
  key `idx_dept_instr_position_type` (`pos_code_type`),
  key `fk_instructor_role` (`role_id`),
  constraint `fk_instructor_role` foreign key (`role_id`) references `roles` (`role_id`) on delete set null,
  constraint `fk_dept_instr_dept` foreign key (`department_uniqueid`) references `department` (`uniqueid`) on delete cascade,
  constraint `fk_dept_instr_pos_code_type` foreign key (`pos_code_type`) references `position_type` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `dept_status_type` (
  `uniqueid` decimal(20,0) not null,
  `reference` varchar(20) default null,
  `label` varchar(60) default null,
  `status` bigint(10) default null,
  `apply` bigint(10) default null,
  `ord` bigint(10) default null,
  primary key (`uniqueid`)
) ENGINE=InnoDB;

create table `dept_to_tt_mgr` (
  `timetable_mgr_id` decimal(20,0) not null,
  `department_id` decimal(20,0) not null,
  primary key (`timetable_mgr_id`,`department_id`),
  key `fk_dept_to_tt_mgr_dept` (`department_id`),
  constraint `fk_dept_to_tt_mgr_dept` foreign key (`department_id`) references `department` (`uniqueid`) on delete cascade,
  constraint `fk_dept_to_tt_mgr_mgr` foreign key (`timetable_mgr_id`) references `timetable_manager` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `designator` (
  `uniqueid` decimal(20,0) not null,
  `subject_area_id` decimal(20,0) default null,
  `instructor_id` decimal(20,0) default null,
  `code` varchar(3) default null,
  `last_modified_time` datetime default null,
  primary key (`uniqueid`),
  UNIQUE key `uk_designator_code` (`subject_area_id`,`instructor_id`,`code`),
  key `fk_designator_instructor` (`instructor_id`),
  constraint `fk_designator_instructor` foreign key (`instructor_id`) references `departmental_instructor` (`uniqueid`) on delete cascade,
  constraint `fk_designator_subj_area` foreign key (`subject_area_id`) references `subject_area` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `dist_type_dept` (
  `dist_type_id` decimal(19,0) not null,
  `dept_id` decimal(20,0) not null,
  primary key (`dist_type_id`,`dept_id`),
  key `fk_dist_type_dept_dept` (`dept_id`),
  constraint `fk_dist_type_dept_dept` foreign key (`dept_id`) references `department` (`uniqueid`) on delete cascade,
  constraint `fk_dist_type_dept_type` foreign key (`dist_type_id`) references `distribution_type` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `distribution_object` (
  `uniqueid` decimal(20,0) not null,
  `dist_pref_id` decimal(20,0) default null,
  `sequence_number` int(3) default null,
  `pref_group_id` decimal(20,0) default null,
  `last_modified_time` datetime default null,
  primary key (`uniqueid`),
  key `idx_distribution_object_pg` (`pref_group_id`),
  key `idx_distribution_object_pref` (`dist_pref_id`),
  constraint `fk_distribution_object_pref` foreign key (`dist_pref_id`) references `distribution_pref` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `distribution_pref` (
  `uniqueid` decimal(20,0) not null,
  `owner_id` decimal(20,0) default null,
  `pref_level_id` decimal(20,0) default null,
  `dist_type_id` decimal(20,0) default null,
  `grouping` bigint(10) default null,
  `last_modified_time` datetime default null,
  `uid_rolled_fwd_from` decimal(20,0) default null,
  primary key (`uniqueid`),
  key `idx_distribution_pref_level` (`pref_level_id`),
  key `idx_distribution_pref_owner` (`owner_id`),
  key `idx_distribution_pref_type` (`dist_type_id`),
  constraint `fk_distribution_pref_dist_type` foreign key (`dist_type_id`) references `distribution_type` (`uniqueid`) on delete cascade,
  constraint `fk_distribution_pref_level` foreign key (`pref_level_id`) references `preference_level` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `distribution_type` (
  `uniqueid` decimal(20,0) not null,
  `reference` varchar(20) default null,
  `label` varchar(60) default null,
  `sequencing_required` varchar(1) default '0',
  `req_id` int(6) default null,
  `allowed_pref` varchar(10) default null,
  `description` varchar(2048) default null,
  `abbreviation` varchar(20) default null,
  `instructor_pref` int(1) default '0',
  `exam_pref` int(1) default '0',
  primary key (`uniqueid`),
  UNIQUE key `uk_distribution_type_req_id` (`req_id`)
) ENGINE=InnoDB;

create table `event` (
  `uniqueid` decimal(20,0) not null,
  `event_name` varchar(100) default null,
  `min_capacity` bigint(10) default null,
  `max_capacity` bigint(10) default null,
  `sponsoring_org` decimal(20,0) default null,
  `main_contact_id` decimal(20,0) default null,
  `class_id` decimal(20,0) default null,
  `exam_id` decimal(20,0) default null,
  `event_type` bigint(10) default null,
  `req_attd` int(1) default null,
  `email` varchar(1000) default null,
  `sponsor_org_id` decimal(20,0) default null,
  `expiration_date` date default null,
  primary key (`uniqueid`),
  key `idx_event_class` (`class_id`),
  key `idx_event_exam` (`exam_id`),
  key `fk_event_main_contact` (`main_contact_id`),
  key `fk_event_sponsor_org` (`sponsor_org_id`),
  constraint `fk_event_class` foreign key (`class_id`) references `class_` (`uniqueid`) on delete cascade,
  constraint `fk_event_exam` foreign key (`exam_id`) references `exam` (`uniqueid`) on delete cascade,
  constraint `fk_event_main_contact` foreign key (`main_contact_id`) references `event_contact` (`uniqueid`) on delete set null,
  constraint `fk_event_sponsor_org` foreign key (`sponsor_org_id`) references `sponsoring_organization` (`uniqueid`) on delete set null
) ENGINE=InnoDB;

create table `event_contact` (
  `uniqueid` decimal(20,0) not null,
  `external_id` varchar(40) default null,
  `email` varchar(200) default null,
  `phone` varchar(25) default null,
  `firstname` varchar(100) default null,
  `middlename` varchar(100) default null,
  `lastname` varchar(100) default null,
  primary key (`uniqueid`)
) ENGINE=InnoDB;

create table `event_join_event_contact` (
  `event_id` decimal(20,0) not null,
  `event_contact_id` decimal(20,0) not null,
  key `fk_event_contact_join` (`event_contact_id`),
  key `fk_event_id_join` (`event_id`),
  constraint `fk_event_contact_join` foreign key (`event_contact_id`) references `event_contact` (`uniqueid`) on delete cascade,
  constraint `fk_event_id_join` foreign key (`event_id`) references `event` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `event_note` (
  `uniqueid` decimal(20,0) not null,
  `event_id` decimal(20,0) not null,
  `text_note` varchar(1000) default null,
  `time_stamp` datetime default null,
  `note_type` bigint(10) NOT NULL default '0',
  `uname` varchar(100) default null,
  `meetings` longtext default null,
  `attached_file` longblob,
  `attached_name` varchar(260) default null,
  `attached_content` varchar(260) default null,
  `user_id` varchar(40) default null,
  primary key (`uniqueid`),
  key `fk_event_note_event` (`event_id`),
  constraint `fk_event_note_event` foreign key (`event_id`) references `event` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `event_note_meeting` (
  `note_id` decimal(20,0) not null,
  `meeting_id` decimal(20,0) not null,
  primary key (`note_id`,`meeting_id`),
  key `fk_event_note_mtg` (`meeting_id`),
  constraint `fk_event_note_mtg` foreign key (`meeting_id`) references `meeting` (`uniqueid`) on delete cascade,
  constraint `fk_event_note_note` foreign key (`note_id`) references `event_note` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `exact_time_mins` (
  `uniqueid` decimal(20,0) not null,
  `mins_min` int(4) default null,
  `mins_max` int(4) default null,
  `nr_slots` int(4) default null,
  `break_time` int(4) default null,
  primary key (`uniqueid`),
  key `idx_exact_time_mins` (`mins_min`,`mins_max`)
) ENGINE=InnoDB;

create table `exam` (
  `uniqueid` decimal(20,0) not null,
  `session_id` decimal(20,0) not null,
  `name` varchar(100) default null,
  `note` varchar(1000) default null,
  `length` bigint(10) not null,
  `max_nbr_rooms` bigint(10) NOT NULL default '1',
  `seating_type` bigint(10) not null,
  `assigned_period` decimal(20,0) default null,
  `assigned_pref` varchar(100) default null,
  `avg_period` bigint(10) default null,
  `uid_rolled_fwd_from` decimal(20,0) default null,
  `exam_size` bigint(10) default null,
  `print_offset` bigint(10) default null,
  `exam_type_id` decimal(20,0) not null,
  primary key (`uniqueid`),
  key `fk_exam_period` (`assigned_period`),
  key `fk_exam_session` (`session_id`),
  key `fk_exam_type` (`exam_type_id`),
  constraint `fk_exam_period` foreign key (`assigned_period`) references `exam_period` (`uniqueid`) on delete cascade,
  constraint `fk_exam_session` foreign key (`session_id`) references `sessions` (`uniqueid`) on delete cascade,
  constraint `fk_exam_type` foreign key (`exam_type_id`) references `exam_type` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `exam_instructor` (
  `exam_id` decimal(20,0) not null,
  `instructor_id` decimal(20,0) not null,
  primary key (`exam_id`,`instructor_id`),
  key `fk_exam_instructor_instructor` (`instructor_id`),
  constraint `fk_exam_instructor_exam` foreign key (`exam_id`) references `exam` (`uniqueid`) on delete cascade,
  constraint `fk_exam_instructor_instructor` foreign key (`instructor_id`) references `departmental_instructor` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `exam_location_pref` (
  `uniqueid` decimal(20,0) not null,
  `location_id` decimal(20,0) not null,
  `pref_level_id` decimal(20,0) not null,
  `period_id` decimal(20,0) not null,
  primary key (`uniqueid`),
  key `idx_exam_location_pref` (`location_id`),
  key `fk_exam_location_pref_period` (`period_id`),
  key `fk_exam_location_pref_pref` (`pref_level_id`),
  constraint `fk_exam_location_pref_period` foreign key (`period_id`) references `exam_period` (`uniqueid`) on delete cascade,
  constraint `fk_exam_location_pref_pref` foreign key (`pref_level_id`) references `preference_level` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `exam_owner` (
  `uniqueid` decimal(20,0) not null,
  `exam_id` decimal(20,0) not null,
  `owner_id` decimal(20,0) not null,
  `owner_type` bigint(10) not null,
  `course_id` decimal(20,0) default null,
  primary key (`uniqueid`),
  key `idx_exam_owner_course` (`course_id`),
  key `idx_exam_owner_exam` (`exam_id`),
  key `idx_exam_owner_owner` (`owner_id`,`owner_type`),
  constraint `fk_exam_owner_course` foreign key (`course_id`) references `course_offering` (`uniqueid`) on delete cascade,
  constraint `fk_exam_owner_exam` foreign key (`exam_id`) references `exam` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `exam_period` (
  `uniqueid` decimal(20,0) not null,
  `session_id` decimal(20,0) not null,
  `date_ofs` bigint(10) not null,
  `start_slot` bigint(10) not null,
  `length` bigint(10) not null,
  `pref_level_id` decimal(20,0) not null,
  `event_start_offset` bigint(10) NOT NULL default '0',
  `event_stop_offset` bigint(10) NOT NULL default '0',
  `exam_type_id` decimal(20,0) not null,
  primary key (`uniqueid`),
  key `fk_exam_period_pref` (`pref_level_id`),
  key `fk_exam_period_session` (`session_id`),
  key `fk_exam_period_type` (`exam_type_id`),
  constraint `fk_exam_period_pref` foreign key (`pref_level_id`) references `preference_level` (`uniqueid`) on delete cascade,
  constraint `fk_exam_period_session` foreign key (`session_id`) references `sessions` (`uniqueid`) on delete cascade,
  constraint `fk_exam_period_type` foreign key (`exam_type_id`) references `exam_type` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `exam_period_pref` (
  `uniqueid` decimal(20,0) not null,
  `owner_id` decimal(20,0) not null,
  `pref_level_id` decimal(20,0) not null,
  `period_id` decimal(20,0) not null,
  primary key (`uniqueid`),
  key `fk_exam_period_pref_period` (`period_id`),
  key `fk_exam_period_pref_pref` (`pref_level_id`),
  constraint `fk_exam_period_pref_period` foreign key (`period_id`) references `exam_period` (`uniqueid`) on delete cascade,
  constraint `fk_exam_period_pref_pref` foreign key (`pref_level_id`) references `preference_level` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `exam_room_assignment` (
  `exam_id` decimal(20,0) not null,
  `location_id` decimal(20,0) not null,
  primary key (`exam_id`,`location_id`),
  constraint `fk_exam_room_exam` foreign key (`exam_id`) references `exam` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `exam_type` (
  `uniqueid` decimal(20,0) not null,
  `reference` varchar(20) not null,
  `label` varchar(60) not null,
  `xtype` bigint(10) not null,
  primary key (`uniqueid`)
) ENGINE=InnoDB;

create table `external_building` (
  `uniqueid` decimal(20,0) not null,
  `session_id` decimal(20,0) default null,
  `external_uid` varchar(40) default null,
  `abbreviation` varchar(10) default null,
  `coordinate_x` double default null,
  `coordinate_y` double default null,
  `display_name` varchar(100) default null,
  primary key (`uniqueid`),
  key `idx_external_building` (`session_id`,`abbreviation`)
) ENGINE=InnoDB;

create table `external_room` (
  `uniqueid` decimal(20,0) not null,
  `external_bldg_id` decimal(20,0) default null,
  `external_uid` varchar(40) default null,
  `room_number` varchar(10) default null,
  `coordinate_x` double default null,
  `coordinate_y` double default null,
  `capacity` bigint(10) default null,
  `classification` varchar(20) default null,
  `instructional` int(1) default null,
  `display_name` varchar(100) default null,
  `exam_capacity` bigint(10) default null,
  `room_type` decimal(20,0) default null,
  `area` double default null,
  primary key (`uniqueid`),
  key `idx_external_room` (`external_bldg_id`,`room_number`),
  key `fk_external_room_type` (`room_type`),
  constraint `fk_external_room_type` foreign key (`room_type`) references `room_type` (`uniqueid`) on delete cascade,
  constraint `fk_ext_room_building` foreign key (`external_bldg_id`) references `external_building` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `external_room_department` (
  `uniqueid` decimal(20,0) not null,
  `external_room_id` decimal(20,0) default null,
  `department_code` varchar(50) default null,
  `percent` bigint(10) default null,
  `assignment_type` varchar(20) default null,
  primary key (`uniqueid`),
  key `fk_ext_dept_room` (`external_room_id`),
  constraint `fk_ext_dept_room` foreign key (`external_room_id`) references `external_room` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `external_room_feature` (
  `uniqueid` decimal(20,0) not null,
  `external_room_id` decimal(20,0) default null,
  `name` varchar(20) default null,
  `value` varchar(20) default null,
  primary key (`uniqueid`),
  key `fk_ext_ftr_room` (`external_room_id`),
  constraint `fk_ext_ftr_room` foreign key (`external_room_id`) references `external_room` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `feature_type` (
  `uniqueid` decimal(20,0) not null,
  `reference` varchar(20) not null,
  `label` varchar(60) not null,
  `events` int(1) not null,
  primary key (`uniqueid`)
) ENGINE=InnoDB;

create table `free_time` (
  `uniqueid` decimal(20,0) not null,
  `name` varchar(50) default null,
  `day_code` bigint(10) default null,
  `start_slot` bigint(10) default null,
  `length` bigint(10) default null,
  `category` bigint(10) default null,
  `session_id` decimal(20,0) default null,
  primary key (`uniqueid`),
  key `fk_free_time_session` (`session_id`),
  constraint `fk_free_time_session` foreign key (`session_id`) references `sessions` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `hibernate_unique_key` (
  `next_hi` decimal(20,0) default '32'
) ENGINE=InnoDB;

create table `history` (
  `uniqueid` decimal(20,0) not null,
  `subclass` varchar(10) default null,
  `old_value` varchar(20) default null,
  `new_value` varchar(20) default null,
  `old_number` varchar(20) default null,
  `new_number` varchar(20) default null,
  `session_id` decimal(20,0) default null,
  primary key (`uniqueid`),
  key `idx_history_session` (`session_id`),
  constraint `fk_history_session` foreign key (`session_id`) references `sessions` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `instr_offering_config` (
  `uniqueid` decimal(20,0) not null,
  `config_limit` bigint(10) default null,
  `instr_offr_id` decimal(20,0) default null,
  `unlimited_enrollment` int(1) default null,
  `name` varchar(10) default null,
  `last_modified_time` datetime default null,
  `uid_rolled_fwd_from` decimal(20,0) default null,
  primary key (`uniqueid`),
  UNIQUE key `uk_instr_offr_cfg_name` (`uniqueid`,`name`),
  key `idx_instr_offr_cfg_instr_offr` (`instr_offr_id`),
  constraint `fk_instr_offr_cfg_instr_offr` foreign key (`instr_offr_id`) references `instructional_offering` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `instructional_offering` (
  `uniqueid` decimal(20,0) not null,
  `session_id` decimal(20,0) default null,
  `instr_offering_perm_id` bigint(10) default null,
  `not_offered` int(1) default null,
  `limit` int(4) default null,
  `designator_required` int(1) default null,
  `last_modified_time` datetime default null,
  `uid_rolled_fwd_from` decimal(20,0) default null,
  `external_uid` varchar(40) default null,
  `req_reservation` int(1) NOT NULL default '0',
  `wk_enroll` bigint(10) default null,
  `wk_change` bigint(10) default null,
  `wk_drop` bigint(10) default null,
  primary key (`uniqueid`)
) ENGINE=InnoDB;

create table `itype_desc` (
  `itype` int(2) not null,
  `abbv` varchar(7) default null,
  `description` varchar(50) default null,
  `sis_ref` varchar(20) default null,
  `basic` int(1) default null,
  `parent` int(2) default null,
  `organized` int(1) default null,
  primary key (`itype`)
) ENGINE=InnoDB;

create table `jenrl` (
  `uniqueid` decimal(20,0) not null,
  `jenrl` double default null,
  `solution_id` decimal(20,0) default null,
  `class1_id` decimal(20,0) default null,
  `class2_id` decimal(20,0) default null,
  primary key (`uniqueid`),
  key `idx_jenrl` (`solution_id`),
  key `idx_jenrl_class1` (`class1_id`),
  key `idx_jenrl_class2` (`class2_id`),
  constraint `fk_jenrl_class1` foreign key (`class1_id`) references `class_` (`uniqueid`) on delete cascade,
  constraint `fk_jenrl_class2` foreign key (`class2_id`) references `class_` (`uniqueid`) on delete cascade,
  constraint `fk_jenrl_solution` foreign key (`solution_id`) references `solution` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `lastlike_course_demand` (
  `uniqueid` decimal(20,0) not null,
  `student_id` decimal(20,0) default null,
  `subject_area_id` decimal(20,0) default null,
  `course_nbr` varchar(10) default null,
  `priority` bigint(10) default '0',
  `course_perm_id` varchar(20) default null,
  primary key (`uniqueid`),
  key `idx_ll_course_demand_course` (`subject_area_id`,`course_nbr`),
  key `idx_ll_course_demand_permid` (`course_perm_id`),
  key `idx_ll_course_demand_student` (`student_id`),
  constraint `fk_ll_course_demand_student` foreign key (`student_id`) references `student` (`uniqueid`) on delete cascade,
  constraint `fk_ll_course_demand_subjarea` foreign key (`subject_area_id`) references `subject_area` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `manager_settings` (
  `uniqueid` decimal(20,0) not null,
  `key_id` decimal(20,0) default null,
  `value` varchar(100) default null,
  `user_uniqueid` decimal(20,0) default null,
  primary key (`uniqueid`),
  key `idx_manager_settings_key` (`key_id`),
  key `idx_manager_settings_manager` (`user_uniqueid`),
  constraint `fk_manager_settings_key` foreign key (`key_id`) references `settings` (`uniqueid`) on delete cascade,
  constraint `fk_manager_settings_user` foreign key (`user_uniqueid`) references `timetable_manager` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `meeting` (
  `uniqueid` decimal(20,0) not null,
  `event_id` decimal(20,0) not null,
  `meeting_date` datetime not null,
  `start_period` bigint(10) not null,
  `start_offset` bigint(10) default null,
  `stop_period` bigint(10) not null,
  `stop_offset` bigint(10) default null,
  `location_perm_id` decimal(20,0) default null,
  `class_can_override` int(1) not null,
  `approval_date` date default null,
  `approval_status` bigint(10) NOT NULL default '0',
  primary key (`uniqueid`),
  key `fk_meeting_event` (`event_id`),
  constraint `fk_meeting_event` foreign key (`event_id`) references `event` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `message_log` (
  `uniqueid` decimal(20,0) not null,
  `time_stamp` datetime not null,
  `log_level` decimal(10,0) not null,
  `message` longtext default null,
  `logger` varchar(255) not null,
  `thread` varchar(100) default null,
  `ndc` longtext default null,
  `exception` longtext default null,
  primary key (`uniqueid`),
  key `idx_message_log` (`time_stamp`,`log_level`)
) ENGINE=InnoDB;

create table `non_university_location` (
  `uniqueid` decimal(20,0) not null,
  `session_id` decimal(20,0) default null,
  `name` varchar(20) default null,
  `capacity` bigint(10) default null,
  `coordinate_x` double default null,
  `coordinate_y` double default null,
  `ignore_too_far` int(1) default null,
  `manager_ids` varchar(3000) default null,
  `pattern` varchar(2048) default null,
  `ignore_room_check` int(1) default '0',
  `display_name` varchar(100) default null,
  `exam_capacity` bigint(10) default '0',
  `permanent_id` decimal(20,0) not null,
  `room_type` decimal(20,0) default null,
  `event_dept_id` decimal(20,0) default null,
  `area` double default null,
  `break_time` bigint(10) default null,
  `event_status` bigint(10) default null,
  `note` varchar(2048) default null,
  `availability` varchar(2048) default null,
  `external_uid` varchar(40) default null,
  `share_note` varchar(2048) default null,
  primary key (`uniqueid`),
  key `idx_location_permid` (`permanent_id`,`session_id`),
  key `idx_non_univ_loc_session` (`session_id`),
  key `fk_location_type` (`room_type`),
  key `fk_loc_event_dept` (`event_dept_id`),
  constraint `fk_location_type` foreign key (`room_type`) references `room_type` (`uniqueid`) on delete cascade,
  constraint `fk_loc_event_dept` foreign key (`event_dept_id`) references `department` (`uniqueid`) on delete set null,
  constraint `fk_non_univ_loc_session` foreign key (`session_id`) references `sessions` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `offering_coordinator` (
  `offering_id` decimal(20,0) not null,
  `instructor_id` decimal(20,0) not null,
  primary key (`offering_id`,`instructor_id`),
  key `fk_offering_coord_instructor` (`instructor_id`),
  constraint `fk_offering_coord_instructor` foreign key (`instructor_id`) references `departmental_instructor` (`uniqueid`) on delete cascade,
  constraint `fk_offering_coord_offering` foreign key (`offering_id`) references `instructional_offering` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `offr_consent_type` (
  `uniqueid` decimal(20,0) not null,
  `reference` varchar(20) default null,
  `label` varchar(60) default null,
  `abbv` varchar(20) default null,
  primary key (`uniqueid`),
  UNIQUE key `uk_offr_consent_type_label` (`label`),
  UNIQUE key `uk_offr_consent_type_ref` (`reference`)
) ENGINE=InnoDB;

create table `offr_group` (
  `uniqueid` decimal(20,0) not null,
  `session_id` decimal(20,0) default null,
  `name` varchar(20) default null,
  `description` varchar(200) default null,
  `department_id` decimal(20,0) default null,
  primary key (`uniqueid`),
  key `idx_offr_group_dept` (`department_id`),
  key `idx_offr_group_session` (`session_id`),
  constraint `fk_offr_group_dept` foreign key (`department_id`) references `department` (`uniqueid`) on delete cascade,
  constraint `fk_offr_group_session` foreign key (`session_id`) references `sessions` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `offr_group_offering` (
  `offr_group_id` decimal(20,0) not null,
  `instr_offering_id` decimal(20,0) not null,
  primary key (`offr_group_id`,`instr_offering_id`),
  key `fk_offr_group_instr_offr` (`instr_offering_id`),
  constraint `fk_offr_group_instr_offr` foreign key (`instr_offering_id`) references `instructional_offering` (`uniqueid`) on delete cascade,
  constraint `fk_offr_group_offr_offr_grp` foreign key (`offr_group_id`) references `offr_group` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `pos_acad_area_major` (
  `academic_area_id` decimal(20,0) not null,
  `major_id` decimal(20,0) not null,
  primary key (`academic_area_id`,`major_id`),
  key `fk_pos_acad_area_major_major` (`major_id`),
  constraint `fk_pos_acad_area_major_area` foreign key (`academic_area_id`) references `academic_area` (`uniqueid`) on delete cascade,
  constraint `fk_pos_acad_area_major_major` foreign key (`major_id`) references `pos_major` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `pos_acad_area_minor` (
  `academic_area_id` decimal(20,0) not null,
  `minor_id` decimal(20,0) not null,
  primary key (`academic_area_id`,`minor_id`),
  key `fk_pos_acad_area_minor_minor` (`minor_id`),
  constraint `fk_pos_acad_area_minor_area` foreign key (`academic_area_id`) references `academic_area` (`uniqueid`) on delete cascade,
  constraint `fk_pos_acad_area_minor_minor` foreign key (`minor_id`) references `pos_minor` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `pos_major` (
  `uniqueid` decimal(20,0) not null,
  `code` varchar(10) default null,
  `name` varchar(50) default null,
  `external_uid` varchar(20) default null,
  `session_id` decimal(20,0) default null,
  primary key (`uniqueid`),
  key `idx_pos_major_code` (`code`,`session_id`),
  key `fk_pos_major_session` (`session_id`),
  constraint `fk_pos_major_session` foreign key (`session_id`) references `sessions` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `pos_minor` (
  `uniqueid` decimal(20,0) not null,
  `code` varchar(10) default null,
  `name` varchar(50) default null,
  `external_uid` varchar(40) default null,
  `session_id` decimal(20,0) default null,
  primary key (`uniqueid`),
  key `fk_pos_minor_session` (`session_id`),
  constraint `fk_pos_minor_session` foreign key (`session_id`) references `sessions` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `position_type` (
  `uniqueid` decimal(20,0) not null,
  `reference` varchar(20) default null,
  `label` varchar(60) default null,
  `sort_order` int(4) default null,
  primary key (`uniqueid`),
  UNIQUE key `uk_position_type_label` (`label`),
  UNIQUE key `uk_position_type_ref` (`reference`)
) ENGINE=InnoDB;

create table `preference_level` (
  `pref_id` int(2) default null,
  `pref_prolog` varchar(2) default null,
  `pref_name` varchar(20) default null,
  `uniqueid` decimal(20,0) not null,
  primary key (`uniqueid`),
  UNIQUE key `uk_preference_level_pref_id` (`pref_id`)
) ENGINE=InnoDB;

create table `query_log` (
  `uniqueid` decimal(20,0) not null,
  `time_stamp` datetime not null,
  `time_spent` decimal(20,0) not null,
  `uri` varchar(255) not null,
  `type` decimal(10,0) not null,
  `session_id` varchar(32) default null,
  `userid` varchar(40) default null,
  `query` longtext default null,
  `exception` longtext default null,
  primary key (`uniqueid`),
  key `idx_query_log` (`time_stamp`)
) ENGINE=InnoDB;

create table `related_course_info` (
  `uniqueid` decimal(20,0) not null,
  `event_id` decimal(20,0) not null,
  `owner_id` decimal(20,0) not null,
  `owner_type` bigint(10) not null,
  `course_id` decimal(20,0) not null,
  primary key (`uniqueid`),
  key `idx_event_owner_event` (`event_id`),
  key `idx_event_owner_owner` (`owner_id`,`owner_type`),
  key `fk_event_owner_course` (`course_id`),
  constraint `fk_event_owner_course` foreign key (`course_id`) references `course_offering` (`uniqueid`) on delete cascade,
  constraint `fk_event_owner_event` foreign key (`event_id`) references `event` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `reservation` (
  `uniqueid` decimal(20,0) not null,
  `reservation_type` bigint(10) not null,
  `expiration_date` datetime default null,
  `reservation_limit` bigint(10) default null,
  `offering_id` decimal(20,0) not null,
  `group_id` decimal(20,0) default null,
  `area_id` decimal(20,0) default null,
  `course_id` decimal(20,0) default null,
  primary key (`uniqueid`),
  key `fk_reservation_offering` (`offering_id`),
  key `fk_reservation_student_group` (`group_id`),
  key `fk_reservation_area` (`area_id`),
  key `fk_reservation_course` (`course_id`),
  constraint `fk_reservation_course` foreign key (`course_id`) references `course_offering` (`uniqueid`) on delete cascade,
  constraint `fk_reservation_area` foreign key (`area_id`) references `academic_area` (`uniqueid`) on delete cascade,
  constraint `fk_reservation_offering` foreign key (`offering_id`) references `instructional_offering` (`uniqueid`) on delete cascade,
  constraint `fk_reservation_student_group` foreign key (`group_id`) references `student_group` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `reservation_clasf` (
  `reservation_id` decimal(20,0) not null,
  `acad_clasf_id` decimal(20,0) not null,
  primary key (`reservation_id`,`acad_clasf_id`),
  key `fk_res_clasf_clasf` (`acad_clasf_id`),
  constraint `fk_res_clasf_reservation` foreign key (`reservation_id`) references `reservation` (`uniqueid`) on delete cascade,
  constraint `fk_res_clasf_clasf` foreign key (`acad_clasf_id`) references `academic_classification` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `reservation_class` (
  `reservation_id` decimal(20,0) not null,
  `class_id` decimal(20,0) not null,
  primary key (`reservation_id`,`class_id`),
  key `fk_res_class_class` (`class_id`),
  constraint `fk_res_class_reservation` foreign key (`reservation_id`) references `reservation` (`uniqueid`) on delete cascade,
  constraint `fk_res_class_class` foreign key (`class_id`) references `class_` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `reservation_config` (
  `reservation_id` decimal(20,0) not null,
  `config_id` decimal(20,0) not null,
  primary key (`reservation_id`,`config_id`),
  key `fk_res_config_config` (`config_id`),
  constraint `fk_res_config_reservation` foreign key (`reservation_id`) references `reservation` (`uniqueid`) on delete cascade,
  constraint `fk_res_config_config` foreign key (`config_id`) references `instr_offering_config` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `reservation_major` (
  `reservation_id` decimal(20,0) not null,
  `major_id` decimal(20,0) not null,
  primary key (`reservation_id`,`major_id`),
  key `fk_res_majors_major` (`major_id`),
  constraint `fk_res_majors_reservation` foreign key (`reservation_id`) references `reservation` (`uniqueid`) on delete cascade,
  constraint `fk_res_majors_major` foreign key (`major_id`) references `pos_major` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `reservation_student` (
  `reservation_id` decimal(20,0) not null,
  `student_id` decimal(20,0) not null,
  primary key (`reservation_id`,`student_id`),
  key `fk_res_student_student` (`student_id`),
  constraint `fk_res_student_reservation` foreign key (`reservation_id`) references `reservation` (`uniqueid`) on delete cascade,
  constraint `fk_res_student_student` foreign key (`student_id`) references `student` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `rights` (
  `role_id` decimal(20,0) not null,
  `value` varchar(200) not null,
  primary key (`role_id`,`value`),
  constraint `fk_rights_role` foreign key (`role_id`) references `roles` (`role_id`) on delete cascade
) ENGINE=InnoDB;

create table `roles` (
  `role_id` decimal(20,0) not null,
  `reference` varchar(20) default null,
  `abbv` varchar(40) default null,
  `manager` int(1) default '1',
  `enabled` int(1) default '1',
  `instructor` int(1) default '0',
  primary key (`role_id`),
  UNIQUE key `uk_roles_abbv` (`abbv`),
  UNIQUE key `uk_roles_reference` (`reference`)
) ENGINE=InnoDB;

create table `room` (
  `uniqueid` decimal(20,0) not null,
  `external_uid` varchar(40) default null,
  `session_id` decimal(20,0) default null,
  `building_id` decimal(20,0) default null,
  `room_number` varchar(10) default null,
  `capacity` bigint(10) default null,
  `coordinate_x` double default null,
  `coordinate_y` double default null,
  `ignore_too_far` int(1) default null,
  `manager_ids` varchar(3000) default null,
  `pattern` varchar(2048) default null,
  `ignore_room_check` int(1) default '0',
  `classification` varchar(20) default null,
  `display_name` varchar(100) default null,
  `exam_capacity` bigint(10) default '0',
  `permanent_id` decimal(20,0) not null,
  `room_type` decimal(20,0) default null,
  `event_dept_id` decimal(20,0) default null,
  `area` double default null,
  `break_time` bigint(10) default null,
  `event_status` bigint(10) default null,
  `note` varchar(2048) default null,
  `availability` varchar(2048) default null,
  `share_note` varchar(2048) default null,
  primary key (`uniqueid`),
  UNIQUE key `uk_room` (`session_id`,`building_id`,`room_number`),
  key `idx_room_building` (`building_id`),
  key `idx_room_permid` (`permanent_id`,`session_id`),
  key `fk_room_type` (`room_type`),
  key `fk_room_event_dept` (`event_dept_id`),
  constraint `fk_room_building` foreign key (`building_id`) references `building` (`uniqueid`) on delete cascade,
  constraint `fk_room_event_dept` foreign key (`event_dept_id`) references `department` (`uniqueid`) on delete set null,
  constraint `fk_room_session` foreign key (`session_id`) references `sessions` (`uniqueid`) on delete cascade,
  constraint `fk_room_type` foreign key (`room_type`) references `room_type` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `room_dept` (
  `uniqueid` decimal(20,0) not null,
  `room_id` decimal(20,0) default null,
  `department_id` decimal(20,0) default null,
  `is_control` int(1) default '0',
  primary key (`uniqueid`),
  key `idx_room_dept_dept` (`department_id`),
  key `idx_room_dept_room` (`room_id`),
  constraint `fk_room_dept_dept` foreign key (`department_id`) references `department` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `room_exam_type` (
  `location_id` decimal(20,0) not null,
  `exam_type_id` decimal(20,0) not null,
  primary key (`location_id`,`exam_type_id`),
  key `fk_room_exam_type` (`exam_type_id`),
  constraint `fk_room_exam_type` foreign key (`exam_type_id`) references `exam_type` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `room_feature` (
  `uniqueid` decimal(20,0) not null,
  `discriminator` varchar(10) default null,
  `label` varchar(60) default null,
  `sis_reference` varchar(20) default null,
  `sis_value` varchar(20) default null,
  `department_id` decimal(20,0) default null,
  `abbv` varchar(60) default null,
  `session_id` decimal(20,0) default null,
  `feature_type_id` decimal(20,0) default null,
  primary key (`uniqueid`),
  key `idx_room_feature_dept` (`department_id`),
  key `fk_room_feature_session` (`session_id`),
  key `fk_feature_type` (`feature_type_id`),
  constraint `fk_feature_type` foreign key (`feature_type_id`) references `feature_type` (`uniqueid`) on delete set null,
  constraint `fk_room_feature_dept` foreign key (`department_id`) references `department` (`uniqueid`) on delete cascade,
  constraint `fk_room_feature_session` foreign key (`session_id`) references `sessions` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `room_feature_pref` (
  `uniqueid` decimal(20,0) not null,
  `owner_id` decimal(20,0) default null,
  `pref_level_id` decimal(20,0) default null,
  `room_feature_id` decimal(20,0) default null,
  `last_modified_time` datetime default null,
  primary key (`uniqueid`),
  key `idx_room_feat_pref_level` (`pref_level_id`),
  key `idx_room_feat_pref_owner` (`owner_id`),
  key `idx_room_feat_pref_room_feat` (`room_feature_id`),
  constraint `fk_room_feat_pref_level` foreign key (`pref_level_id`) references `preference_level` (`uniqueid`) on delete cascade,
  constraint `fk_room_feat_pref_room_feat` foreign key (`room_feature_id`) references `room_feature` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `room_group` (
  `uniqueid` decimal(20,0) not null,
  `session_id` decimal(20,0) default null,
  `name` varchar(60) default null,
  `description` varchar(200) default null,
  `global` int(1) default null,
  `default_group` int(1) default null,
  `department_id` decimal(20,0) default null,
  `abbv` varchar(60) default null,
  primary key (`uniqueid`),
  key `idx_room_group_dept` (`department_id`),
  key `idx_room_group_session` (`session_id`),
  constraint `fk_room_group_dept` foreign key (`department_id`) references `department` (`uniqueid`) on delete cascade,
  constraint `fk_room_group_session` foreign key (`session_id`) references `sessions` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `room_group_pref` (
  `uniqueid` decimal(20,0) not null,
  `owner_id` decimal(20,0) default null,
  `pref_level_id` decimal(20,0) default null,
  `room_group_id` decimal(20,0) default null,
  `last_modified_time` datetime default null,
  primary key (`uniqueid`),
  key `idx_room_group_pref_level` (`pref_level_id`),
  key `idx_room_group_pref_owner` (`owner_id`),
  key `idx_room_group_pref_room_grp` (`room_group_id`),
  constraint `fk_room_group_pref_level` foreign key (`pref_level_id`) references `preference_level` (`uniqueid`) on delete cascade,
  constraint `fk_room_group_pref_room_grp` foreign key (`room_group_id`) references `room_group` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `room_group_room` (
  `room_group_id` decimal(20,0) not null,
  `room_id` decimal(20,0) not null,
  primary key (`room_group_id`,`room_id`),
  constraint `fk_room_group_room_room_grp` foreign key (`room_group_id`) references `room_group` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `room_join_room_feature` (
  `room_id` decimal(20,0) default null,
  `feature_id` decimal(20,0) default null,
  UNIQUE key `uk_room_join_room_feat_rm_feat` (`room_id`,`feature_id`),
  key `fk_room_join_room_feat_rm_feat` (`feature_id`),
  constraint `fk_room_join_room_feat_rm_feat` foreign key (`feature_id`) references `room_feature` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `room_pref` (
  `uniqueid` decimal(20,0) not null,
  `owner_id` decimal(20,0) default null,
  `pref_level_id` decimal(20,0) default null,
  `room_id` decimal(20,0) default null,
  `last_modified_time` datetime default null,
  primary key (`uniqueid`),
  key `idx_room_pref_level` (`pref_level_id`),
  key `idx_room_pref_owner` (`owner_id`),
  constraint `fk_room_pref_level` foreign key (`pref_level_id`) references `preference_level` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `room_type` (
  `uniqueid` decimal(20,0) not null,
  `reference` varchar(20) not null,
  `label` varchar(60) not null,
  `ord` bigint(10) not null,
  `is_room` int(1) NOT NULL default '1',
  primary key (`uniqueid`)
) ENGINE=InnoDB;

create table `room_type_option` (
  `room_type` decimal(20,0) not null,
  `status` bigint(10) not null,
  `message` varchar(2048) default null,
  `break_time` bigint(10) NOT NULL default '0',
  `department_id` decimal(20,0) not null,
  primary key (`room_type`,`department_id`),
  key `fk_rtype_option_department` (`department_id`),
  constraint `fk_rtype_option_department` foreign key (`department_id`) references `department` (`uniqueid`) on delete cascade,
  constraint `fk_rtype_option_type` foreign key (`room_type`) references `room_type` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `saved_hql` (
  `uniqueid` decimal(20,0) not null,
  `name` varchar(100) not null,
  `description` varchar(1000) default null,
  `query` longtext not null,
  `type` decimal(10,0) not null,
  primary key (`uniqueid`)
) ENGINE=InnoDB;

create table `scheduling_subpart` (
  `uniqueid` decimal(20,0) not null,
  `min_per_wk` int(4) default null,
  `parent` decimal(20,0) default null,
  `config_id` decimal(20,0) default null,
  `itype` int(2) default null,
  `date_pattern_id` decimal(20,0) default null,
  `auto_time_spread` int(1) default '1',
  `subpart_suffix` varchar(5) default null,
  `student_allow_overlap` int(1) default '0',
  `last_modified_time` datetime default null,
  `uid_rolled_fwd_from` decimal(20,0) default null,
  primary key (`uniqueid`),
  key `idx_sched_subpart_config` (`config_id`),
  key `idx_sched_subpart_date_pattern` (`date_pattern_id`),
  key `idx_sched_subpart_itype` (`itype`),
  key `idx_sched_subpart_parent` (`parent`),
  constraint `fk_sched_subpart_config` foreign key (`config_id`) references `instr_offering_config` (`uniqueid`) on delete cascade,
  constraint `fk_sched_subpart_date_pattern` foreign key (`date_pattern_id`) references `date_pattern` (`uniqueid`) on delete cascade,
  constraint `fk_sched_subpart_itype` foreign key (`itype`) references `itype_desc` (`itype`) on delete cascade,
  constraint `fk_sched_subpart_parent` foreign key (`parent`) references `scheduling_subpart` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `script` (
  `uniqueid` decimal(20,0) not null,
  `name` varchar(128) not null,
  `description` varchar(1024) default null,
  `engine` varchar(32) not null,
  `permission` varchar(128) default null,
  `script` longtext not null,
  primary key (`uniqueid`)
) ENGINE=InnoDB;

create table `script_parameter` (
  `script_id` decimal(20,0) not null,
  `name` varchar(128) not null,
  `label` varchar(256) default null,
  `type` varchar(2048) not null,
  `default_value` varchar(2048) default null,
  primary key (`script_id`,`name`),
  constraint `fk_script_parameter` foreign key (`script_id`) references `script` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `sectioning_course_types` (
  `sectioning_status_id` decimal(20,0) not null,
  `course_type_id` decimal(20,0) not null,
  primary key (`sectioning_status_id`,`course_type_id`),
  key `fk_sect_course_type` (`course_type_id`),
  constraint `fk_sect_course_type` foreign key (`course_type_id`) references `course_type` (`uniqueid`) on delete cascade,
  constraint `fk_sect_course_status` foreign key (`sectioning_status_id`) references `sectioning_status` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `sectioning_info` (
  `uniqueid` decimal(20,0) not null,
  `class_id` decimal(20,0) default null,
  `nbr_exp_students` double default null,
  `nbr_hold_students` double default null,
  primary key (`uniqueid`),
  key `fk_sectioning_info_class` (`class_id`),
  constraint `fk_sectioning_info_class` foreign key (`class_id`) references `class_` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `sectioning_log` (
  `uniqueid` decimal(20,0) not null,
  `time_stamp` datetime not null,
  `student` varchar(40) not null,
  `session_id` decimal(20,0) not null,
  `operation` varchar(20) not null,
  `action` longblob not null,
  `result` bigint(10) default null,
  `user_id` varchar(40) default null,
  primary key (`uniqueid`),
  key `fk_sectioning_log_session` (`session_id`),
  key `idx_sectioning_log` (`time_stamp`,`student`,`session_id`,`operation`),
  constraint `fk_sectioning_log_session` foreign key (`session_id`) references `sessions` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `sectioning_queue` (
  `uniqueid` decimal(20,0) not null,
  `session_id` decimal(20,0) not null,
  `type` bigint(10) not null,
  `time_stamp` datetime not null,
  `message` longtext default null,
  primary key (`uniqueid`),
  key `idx_sect_queue_session_ts` (`session_id`,`time_stamp`)
) ENGINE=InnoDB;

create table `sectioning_status` (
  `uniqueid` decimal(20,0) not null,
  `reference` varchar(20) not null,
  `label` varchar(60) not null,
  `status` bigint(10) not null,
  `message` varchar(200) default null,
  primary key (`uniqueid`)
) ENGINE=InnoDB;

create table `session_config` (
  `session_id` decimal(20,0) not null,
  `name` varchar(255) not null,
  `value` varchar(4000) default null,
  `description` varchar(500) default null,
  primary key (`session_id`,`name`),
  constraint `fk_session_config` foreign key (`session_id`) references `sessions` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `sessions` (
  `academic_initiative` varchar(20) default null,
  `session_begin_date_time` datetime default null,
  `classes_end_date_time` datetime default null,
  `session_end_date_time` datetime default null,
  `uniqueid` decimal(20,0) not null,
  `holidays` varchar(400) default null,
  `def_datepatt_id` decimal(20,0) default null,
  `status_type` decimal(20,0) default null,
  `last_modified_time` datetime default null,
  `academic_year` varchar(4) default null,
  `academic_term` varchar(20) default null,
  `exam_begin_date` datetime default null,
  `event_begin_date` datetime default null,
  `event_end_date` datetime default null,
  `sect_status` decimal(20,0) default null,
  `wk_enroll` bigint(10) NOT NULL default '1',
  `wk_change` bigint(10) NOT NULL default '1',
  `wk_drop` bigint(10) NOT NULL default '1',
  primary key (`uniqueid`),
  key `idx_sessions_date_pattern` (`def_datepatt_id`),
  key `idx_sessions_status_type` (`status_type`),
  key `fk_session_sect_status` (`sect_status`),
  constraint `fk_sessions_status_type` foreign key (`status_type`) references `dept_status_type` (`uniqueid`) on delete cascade,
  constraint `fk_session_datepatt` foreign key (`def_datepatt_id`) references `date_pattern` (`uniqueid`) on delete cascade,
  constraint `fk_session_sect_status` foreign key (`sect_status`) references `sectioning_status` (`uniqueid`) on delete set null
) ENGINE=InnoDB;

create table `settings` (
  `uniqueid` decimal(20,0) not null,
  `name` varchar(30) default null,
  `default_value` varchar(100) default null,
  `allowed_values` varchar(500) default null,
  `description` varchar(100) default null,
  primary key (`uniqueid`)
) ENGINE=InnoDB;

create table `solution` (
  `uniqueid` decimal(20,0) not null,
  `created` datetime default null,
  `valid` int(1) default null,
  `commited` int(1) default null,
  `commit_date` datetime default null,
  `note` varchar(1000) default null,
  `creator` varchar(250) default null,
  `owner_id` decimal(20,0) default null,
  `last_modified_time` datetime default null,
  primary key (`uniqueid`),
  key `idx_solution_owner` (`owner_id`),
  constraint `fk_solution_owner` foreign key (`owner_id`) references `solver_group` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `solver_gr_to_tt_mgr` (
  `solver_group_id` decimal(20,0) not null,
  `timetable_mgr_id` decimal(20,0) not null,
  primary key (`solver_group_id`,`timetable_mgr_id`),
  key `fk_solver_gr_to_tt_mgr_tt_mgr` (`timetable_mgr_id`),
  constraint `fk_solver_gr_to_tt_mgr_solvgrp` foreign key (`solver_group_id`) references `solver_group` (`uniqueid`) on delete cascade,
  constraint `fk_solver_gr_to_tt_mgr_tt_mgr` foreign key (`timetable_mgr_id`) references `timetable_manager` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `solver_group` (
  `uniqueid` decimal(20,0) not null,
  `name` varchar(50) default null,
  `abbv` varchar(50) default null,
  `session_id` decimal(20,0) default null,
  primary key (`uniqueid`),
  key `idx_solver_group_session` (`session_id`),
  constraint `fk_solver_group_session` foreign key (`session_id`) references `sessions` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `solver_info` (
  `uniqueid` decimal(20,0) not null,
  `type` bigint(10) default null,
  `value` longblob,
  `opt` varchar(250) default null,
  `solver_info_def_id` decimal(20,0) default null,
  `solution_id` decimal(20,0) default null,
  `assignment_id` decimal(20,0) default null,
  primary key (`uniqueid`),
  key `idx_solver_info` (`assignment_id`),
  key `idx_solver_info_solution` (`solution_id`,`solver_info_def_id`),
  key `fk_solver_info_def` (`solver_info_def_id`),
  constraint `fk_solver_info_assignment` foreign key (`assignment_id`) references `assignment` (`uniqueid`) on delete cascade,
  constraint `fk_solver_info_def` foreign key (`solver_info_def_id`) references `solver_info_def` (`uniqueid`) on delete cascade,
  constraint `fk_solver_info_solution` foreign key (`solution_id`) references `solution` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `solver_info_def` (
  `uniqueid` decimal(20,0) not null,
  `name` varchar(100) default null,
  `description` varchar(1000) default null,
  `implementation` varchar(250) default null,
  primary key (`uniqueid`)
) ENGINE=InnoDB;

create table `solver_parameter` (
  `uniqueid` decimal(20,0) default null,
  `value` varchar(2048) default null,
  `solver_param_def_id` decimal(20,0) default null,
  `solution_id` decimal(20,0) default null,
  `solver_predef_setting_id` decimal(20,0) default null,
  key `idx_solver_param_def` (`solver_param_def_id`),
  key `idx_solver_param_predef` (`solver_predef_setting_id`),
  key `idx_solver_param_solution` (`solution_id`),
  constraint `fk_solver_param_def` foreign key (`solver_param_def_id`) references `solver_parameter_def` (`uniqueid`) on delete cascade,
  constraint `fk_solver_param_predef_stg` foreign key (`solver_predef_setting_id`) references `solver_predef_setting` (`uniqueid`) on delete cascade,
  constraint `fk_solver_param_solution` foreign key (`solution_id`) references `solution` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `solver_parameter_def` (
  `uniqueid` decimal(20,0) not null,
  `name` varchar(100) default null,
  `default_value` varchar(2048) default null,
  `description` varchar(1000) default null,
  `type` varchar(250) default null,
  `ord` bigint(10) default null,
  `visible` int(1) default null,
  `solver_param_group_id` decimal(20,0) default null,
  primary key (`uniqueid`),
  key `idx_solv_param_def_gr` (`solver_param_group_id`),
  constraint `fk_solv_param_def_solv_par_grp` foreign key (`solver_param_group_id`) references `solver_parameter_group` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `solver_parameter_group` (
  `uniqueid` decimal(20,0) not null,
  `name` varchar(100) default null,
  `description` varchar(1000) default null,
  `condition` varchar(250) default null,
  `ord` bigint(10) default null,
  `param_type` bigint(10) default '0',
  primary key (`uniqueid`)
) ENGINE=InnoDB;

create table `solver_predef_setting` (
  `uniqueid` decimal(20,0) not null,
  `name` varchar(100) default null,
  `description` varchar(1000) default null,
  `appearance` bigint(10) default null,
  primary key (`uniqueid`)
) ENGINE=InnoDB;

create table `sponsoring_organization` (
  `uniqueid` decimal(20,0) not null,
  `name` varchar(100) not null,
  `email` varchar(200) default null,
  primary key (`uniqueid`)
) ENGINE=InnoDB;

create table `staff` (
  `uniqueid` decimal(20,0) not null,
  `external_uid` varchar(40) default null,
  `fname` varchar(100) default null,
  `mname` varchar(100) default null,
  `lname` varchar(100) default null,
  `pos_code` varchar(20) default null,
  `dept` varchar(50) default null,
  `email` varchar(200) default null,
  `pos_type` decimal(20,0) default null,
  primary key (`uniqueid`),
  key `fk_staff_pos_type` (`pos_type`),
  constraint `fk_staff_pos_type` foreign key (`pos_type`) references `position_type` (`uniqueid`) on delete set null
) ENGINE=InnoDB;

create table `standard_event_note` (
  `uniqueid` decimal(20,0) not null,
  `reference` varchar(20) default null,
  `note` varchar(1000) default null,
  `discriminator` varchar(10) default 'global',
  `session_id` decimal(20,0) default null,
  `department_id` decimal(20,0) default null,
  primary key (`uniqueid`),
  key `fk_stdevt_note_session` (`session_id`),
  key `fk_stdevt_note_dept` (`department_id`),
  constraint `fk_stdevt_note_dept` foreign key (`department_id`) references `department` (`uniqueid`) on delete cascade,
  constraint `fk_stdevt_note_session` foreign key (`session_id`) references `sessions` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `student` (
  `uniqueid` decimal(20,0) not null,
  `external_uid` varchar(40) default null,
  `first_name` varchar(100) default null,
  `middle_name` varchar(100) default null,
  `last_name` varchar(100) default null,
  `email` varchar(200) default null,
  `free_time_cat` bigint(10) default '0',
  `schedule_preference` bigint(10) default '0',
  `session_id` decimal(20,0) default null,
  `sect_status` decimal(20,0) default null,
  `schedule_emailed` datetime default null,
  primary key (`uniqueid`),
  key `idx_student_session` (`session_id`),
  key `idx_student_external_uid` (`external_uid`),
  key `fk_student_sect_status` (`sect_status`),
  constraint `fk_student_sect_status` foreign key (`sect_status`) references `sectioning_status` (`uniqueid`) on delete set null,
  constraint `fk_student_session` foreign key (`session_id`) references `sessions` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `student_acad_area` (
  `uniqueid` decimal(20,0) not null,
  `student_id` decimal(20,0) default null,
  `acad_clasf_id` decimal(20,0) default null,
  `acad_area_id` decimal(20,0) default null,
  primary key (`uniqueid`),
  UNIQUE key `uk_student_acad_area` (`student_id`,`acad_clasf_id`,`acad_area_id`),
  key `idx_student_acad_area` (`student_id`,`acad_area_id`,`acad_clasf_id`),
  key `fk_student_acad_area_area` (`acad_area_id`),
  key `fk_student_acad_area_clasf` (`acad_clasf_id`),
  constraint `fk_student_acad_area_area` foreign key (`acad_area_id`) references `academic_area` (`uniqueid`) on delete cascade,
  constraint `fk_student_acad_area_clasf` foreign key (`acad_clasf_id`) references `academic_classification` (`uniqueid`) on delete cascade,
  constraint `fk_student_acad_area_student` foreign key (`student_id`) references `student` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `student_accomodation` (
  `uniqueid` decimal(20,0) not null,
  `name` varchar(50) default null,
  `abbreviation` varchar(20) default null,
  `external_uid` varchar(40) default null,
  `session_id` decimal(20,0) default null,
  primary key (`uniqueid`),
  key `fk_student_accom_session` (`session_id`),
  constraint `fk_student_accom_session` foreign key (`session_id`) references `sessions` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `student_class_enrl` (
  `uniqueid` decimal(20,0) not null,
  `student_id` decimal(20,0) default null,
  `course_request_id` decimal(20,0) default null,
  `class_id` decimal(20,0) default null,
  `timestamp` datetime default null,
  `course_offering_id` decimal(20,0) default null,
  `approved_date` datetime default null,
  `approved_by` varchar(40) default null,
  `changed_by` varchar(40) default null,
  primary key (`uniqueid`),
  key `idx_student_class_enrl_class` (`class_id`),
  key `idx_student_class_enrl_course` (`course_offering_id`),
  key `idx_student_class_enrl_req` (`course_request_id`),
  key `idx_student_class_enrl_student` (`student_id`),
  constraint `fk_student_class_enrl_class` foreign key (`class_id`) references `class_` (`uniqueid`) on delete cascade,
  constraint `fk_student_class_enrl_course` foreign key (`course_offering_id`) references `course_offering` (`uniqueid`) on delete cascade,
  constraint `fk_student_class_enrl_request` foreign key (`course_request_id`) references `course_request` (`uniqueid`) on delete cascade,
  constraint `fk_student_class_enrl_student` foreign key (`student_id`) references `student` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `student_enrl` (
  `uniqueid` decimal(20,0) not null,
  `student_id` decimal(20,0) default null,
  `solution_id` decimal(20,0) default null,
  `class_id` decimal(20,0) default null,
  `last_modified_time` datetime default null,
  primary key (`uniqueid`),
  key `idx_student_enrl` (`solution_id`),
  key `idx_student_enrl_assignment` (`solution_id`,`class_id`),
  key `idx_student_enrl_class` (`class_id`),
  constraint `fk_student_enrl_class` foreign key (`class_id`) references `class_` (`uniqueid`) on delete cascade,
  constraint `fk_student_enrl_solution` foreign key (`solution_id`) references `solution` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `student_enrl_msg` (
  `uniqueid` decimal(20,0) not null,
  `message` varchar(255) default null,
  `msg_level` bigint(10) default '0',
  `type` bigint(10) default '0',
  `timestamp` datetime default null,
  `course_demand_id` decimal(20,0) default null,
  `ord` bigint(10) default null,
  primary key (`uniqueid`),
  key `idx_student_enrl_msg_dem` (`course_demand_id`),
  constraint `fk_student_enrl_msg_demand` foreign key (`course_demand_id`) references `course_demand` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `student_group` (
  `uniqueid` decimal(20,0) not null,
  `session_id` decimal(20,0) default null,
  `group_abbreviation` varchar(30) default null,
  `group_name` varchar(90) default null,
  `external_uid` varchar(40) default null,
  primary key (`uniqueid`),
  UNIQUE key `uk_student_group_session_sis` (`session_id`,`group_abbreviation`),
  constraint `fk_student_group_session` foreign key (`session_id`) references `sessions` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `student_major` (
  `student_id` decimal(20,0) not null,
  `major_id` decimal(20,0) not null,
  primary key (`student_id`,`major_id`),
  key `fk_student_major_major` (`major_id`),
  constraint `fk_student_major_major` foreign key (`major_id`) references `pos_major` (`uniqueid`) on delete cascade,
  constraint `fk_student_major_student` foreign key (`student_id`) references `student` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `student_minor` (
  `student_id` decimal(20,0) not null,
  `minor_id` decimal(20,0) not null,
  primary key (`student_id`,`minor_id`),
  key `fk_student_minor_minor` (`minor_id`),
  constraint `fk_student_minor_minor` foreign key (`minor_id`) references `pos_minor` (`uniqueid`) on delete cascade,
  constraint `fk_student_minor_student` foreign key (`student_id`) references `student` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `student_sect_hist` (
  `uniqueid` decimal(20,0) not null,
  `student_id` decimal(20,0) default null,
  `data` longblob,
  `type` bigint(10) default null,
  `timestamp` datetime default null,
  primary key (`uniqueid`),
  key `idx_student_sect_hist_student` (`student_id`),
  constraint `fk_student_sect_hist_student` foreign key (`student_id`) references `student` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `student_to_acomodation` (
  `student_id` decimal(20,0) not null,
  `accomodation_id` decimal(20,0) not null,
  primary key (`student_id`,`accomodation_id`),
  key `fk_student_acomodation_student` (`accomodation_id`),
  constraint `fk_student_acomodation_accom` foreign key (`student_id`) references `student` (`uniqueid`) on delete cascade,
  constraint `fk_student_acomodation_student` foreign key (`accomodation_id`) references `student_accomodation` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `student_to_group` (
  `student_id` decimal(20,0) not null,
  `group_id` decimal(20,0) not null,
  primary key (`student_id`,`group_id`),
  key `fk_student_group_student` (`group_id`),
  constraint `fk_student_group_group` foreign key (`student_id`) references `student` (`uniqueid`) on delete cascade,
  constraint `fk_student_group_student` foreign key (`group_id`) references `student_group` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `subject_area` (
  `uniqueid` decimal(20,0) not null,
  `session_id` decimal(20,0) default null,
  `subject_area_abbreviation` varchar(10) default null,
  `long_title` varchar(100) default null,
  `department_uniqueid` decimal(20,0) default null,
  `external_uid` varchar(40) default null,
  `last_modified_time` datetime default null,
  primary key (`uniqueid`),
  UNIQUE key `uk_subject_area` (`session_id`,`subject_area_abbreviation`),
  key `idx_subject_area_dept` (`department_uniqueid`),
  constraint `fk_subject_area_dept` foreign key (`department_uniqueid`) references `department` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `time_pattern` (
  `uniqueid` decimal(20,0) not null,
  `name` varchar(50) default null,
  `mins_pmt` bigint(10) default null,
  `slots_pmt` bigint(10) default null,
  `nr_mtgs` bigint(10) default null,
  `visible` int(1) default null,
  `type` bigint(10) default null,
  `break_time` int(3) default null,
  `session_id` decimal(20,0) default null,
  primary key (`uniqueid`),
  key `idx_time_pattern_session` (`session_id`),
  constraint `fk_time_pattern_session` foreign key (`session_id`) references `sessions` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `time_pattern_days` (
  `uniqueid` decimal(20,0) not null,
  `day_code` bigint(10) default null,
  `time_pattern_id` decimal(20,0) default null,
  primary key (`uniqueid`),
  key `idx_time_pattern_days` (`time_pattern_id`),
  constraint `fk_time_pattern_days_time_patt` foreign key (`time_pattern_id`) references `time_pattern` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `time_pattern_dept` (
  `dept_id` decimal(20,0) not null,
  `pattern_id` decimal(20,0) not null,
  primary key (`dept_id`,`pattern_id`),
  key `fk_time_pattern_dept_pattern` (`pattern_id`),
  constraint `fk_time_pattern_dept_dept` foreign key (`dept_id`) references `department` (`uniqueid`) on delete cascade,
  constraint `fk_time_pattern_dept_pattern` foreign key (`pattern_id`) references `time_pattern` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `time_pattern_time` (
  `uniqueid` decimal(20,0) not null,
  `start_slot` bigint(10) default null,
  `time_pattern_id` decimal(20,0) default null,
  primary key (`uniqueid`),
  key `idx_time_pattern_time` (`time_pattern_id`),
  constraint `fk_time_pattern_time` foreign key (`time_pattern_id`) references `time_pattern` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `time_pref` (
  `uniqueid` decimal(20,0) not null,
  `owner_id` decimal(20,0) default null,
  `pref_level_id` decimal(20,0) default null,
  `preference` varchar(2048) default null,
  `time_pattern_id` decimal(20,0) default null,
  `last_modified_time` datetime default null,
  primary key (`uniqueid`),
  key `idx_time_pref_owner` (`owner_id`),
  key `idx_time_pref_pref_level` (`pref_level_id`),
  key `idx_time_pref_time_ptrn` (`time_pattern_id`),
  constraint `fk_time_pref_pref_level` foreign key (`pref_level_id`) references `preference_level` (`uniqueid`) on delete cascade,
  constraint `fk_time_pref_time_ptrn` foreign key (`time_pattern_id`) references `time_pattern` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `timetable_manager` (
  `uniqueid` decimal(20,0) not null,
  `external_uid` varchar(40) default null,
  `first_name` varchar(100) default null,
  `middle_name` varchar(100) default null,
  `last_name` varchar(100) default null,
  `email_address` varchar(200) default null,
  `last_modified_time` datetime default null,
  primary key (`uniqueid`),
  UNIQUE key `uk_timetable_manager_puid` (`external_uid`)
) ENGINE=InnoDB;

create table `tmtbl_mgr_to_roles` (
  `manager_id` decimal(20,0) default null,
  `role_id` decimal(20,0) default null,
  `uniqueid` decimal(20,0) not null,
  `is_primary` int(1) default null,
  `receive_emails` int(1) default '1',
  primary key (`uniqueid`),
  UNIQUE key `uk_tmtbl_mgr_to_roles_mgr_role` (`manager_id`,`role_id`),
  key `fk_tmtbl_mgr_to_roles_role` (`role_id`),
  constraint `fk_tmtbl_mgr_to_roles_manager` foreign key (`manager_id`) references `timetable_manager` (`uniqueid`) on delete cascade,
  constraint `fk_tmtbl_mgr_to_roles_role` foreign key (`role_id`) references `roles` (`role_id`) on delete cascade
) ENGINE=InnoDB;

create table `travel_time` (
  `uniqueid` decimal(20,0) not null,
  `session_id` decimal(20,0) not null,
  `loc1_id` decimal(20,0) not null,
  `loc2_id` decimal(20,0) not null,
  `distance` decimal(10,0) not null,
  primary key (`uniqueid`),
  key `fk_trvltime_session` (`session_id`),
  constraint `fk_trvltime_session` foreign key (`session_id`) references `sessions` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `user_data` (
  `external_uid` varchar(12) not null,
  `name` varchar(100) not null,
  `value` varchar(4000) default null,
  primary key (`external_uid`,`name`)
) ENGINE=InnoDB;

create table `users` (
  `username` varchar(15) not null,
  `password` varchar(25) default null,
  `external_uid` varchar(40) default null,
  primary key (`username`)
) ENGINE=InnoDB;

create table `waitlist` (
  `uniqueid` decimal(20,0) not null,
  `student_id` decimal(20,0) default null,
  `course_offering_id` decimal(20,0) default null,
  `type` bigint(10) default '0',
  `timestamp` datetime default null,
  primary key (`uniqueid`),
  key `idx_waitlist_offering` (`course_offering_id`),
  key `idx_waitlist_student` (`student_id`),
  constraint `fk_waitlist_course_offering` foreign key (`course_offering_id`) references `course_offering` (`uniqueid`) on delete cascade,
  constraint `fk_waitlist_student` foreign key (`student_id`) references `student` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `xconflict` (
  `uniqueid` decimal(20,0) not null,
  `conflict_type` bigint(10) not null,
  `distance` double default null,
  primary key (`uniqueid`)
) ENGINE=InnoDB;

create table `xconflict_exam` (
  `conflict_id` decimal(20,0) not null,
  `exam_id` decimal(20,0) not null,
  primary key (`conflict_id`,`exam_id`),
  key `idx_xconflict_exam` (`exam_id`),
  constraint `fk_xconflict_ex_conf` foreign key (`conflict_id`) references `xconflict` (`uniqueid`) on delete cascade,
  constraint `fk_xconflict_ex_exam` foreign key (`exam_id`) references `exam` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `xconflict_instructor` (
  `conflict_id` decimal(20,0) not null,
  `instructor_id` decimal(20,0) not null,
  primary key (`conflict_id`,`instructor_id`),
  key `fk_xconflict_in_instructor` (`instructor_id`),
  constraint `fk_xconflict_in_conf` foreign key (`conflict_id`) references `xconflict` (`uniqueid`) on delete cascade,
  constraint `fk_xconflict_in_instructor` foreign key (`instructor_id`) references `departmental_instructor` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

create table `xconflict_student` (
  `conflict_id` decimal(20,0) not null,
  `student_id` decimal(20,0) not null,
  primary key (`conflict_id`,`student_id`),
  key `idx_xconflict_st_student` (`student_id`),
  constraint `fk_xconflict_st_conf` foreign key (`conflict_id`) references `xconflict` (`uniqueid`) on delete cascade,
  constraint `fk_xconflict_st_student` foreign key (`student_id`) references `student` (`uniqueid`) on delete cascade
) ENGINE=InnoDB;

set foreign_key_checks=@saved_fk_checks;
set character_set_client=@saved_cs_client;