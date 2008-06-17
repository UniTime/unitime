/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC
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

use mysql;

-- MySQL 5:
drop user timetable;

create user timetable identified by 'unitime';

grant all on timetable.* to timetable;

flush privileges;

-- MySQL 4
/*
delete from user where user='timetable';

insert into user (host,user,password) values ('%', 'timetable', password('unitime'));

grant all on timetable.* to timetable;

flush privileges;
*/

-- ----------------------------------------------------------------------
-- MySQL Migration Toolkit
-- SQL Create Script
-- ----------------------------------------------------------------------

SET FOREIGN_KEY_CHECKS = 0;

DROP DATABASE IF EXISTS `timetable`;

CREATE DATABASE `timetable`
  CHARACTER SET utf8;

USE `timetable`;
-- -------------------------------------
-- Tables

DROP TABLE IF EXISTS `timetable`.`academic_area`;
CREATE TABLE `timetable`.`academic_area` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `session_id` DECIMAL(20, 0) NULL,
  `academic_area_abbreviation` VARCHAR(10) BINARY NULL,
  `short_title` VARCHAR(50) BINARY NULL,
  `long_title` VARCHAR(100) BINARY NULL,
  `external_uid` VARCHAR(40) BINARY NULL,
  PRIMARY KEY (`uniqueid`),
  UNIQUE INDEX `uk_academic_area` (`session_id`, `academic_area_abbreviation`(10)),
  CONSTRAINT `fk_academic_area_session` FOREIGN KEY `fk_academic_area_session` (`session_id`)
    REFERENCES `timetable`.`sessions` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`academic_classification`;
CREATE TABLE `timetable`.`academic_classification` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `session_id` DECIMAL(20, 0) NULL,
  `code` VARCHAR(10) BINARY NULL,
  `name` VARCHAR(50) BINARY NULL,
  `external_uid` VARCHAR(40) BINARY NULL,
  PRIMARY KEY (`uniqueid`),
  CONSTRAINT `fk_acad_class_session` FOREIGN KEY `fk_acad_class_session` (`session_id`)
    REFERENCES `timetable`.`sessions` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`acad_area_reservation`;
CREATE TABLE `timetable`.`acad_area_reservation` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `owner` DECIMAL(20, 0) NULL,
  `reservation_type` DECIMAL(20, 0) NULL,
  `acad_classification` DECIMAL(20, 0) NULL,
  `acad_area` DECIMAL(20, 0) NULL,
  `priority` INT(5) NULL,
  `reserved` BIGINT(10) NULL,
  `prior_enrollment` BIGINT(10) NULL,
  `projected_enrollment` BIGINT(10) NULL,
  `owner_class_id` VARCHAR(1) BINARY NULL,
  `requested` BIGINT(10) NULL,
  `last_modified_time` DATETIME NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_acad_area_resv_acad_area` (`acad_area`),
  INDEX `idx_acad_area_resv_acad_class` (`acad_classification`),
  INDEX `idx_acad_area_resv_owner` (`owner`),
  INDEX `idx_acad_area_resv_owner_cls` (`owner_class_id`(1)),
  INDEX `idx_acad_area_resv_type` (`reservation_type`),
  CONSTRAINT `fk_acad_area_resv_acad_area` FOREIGN KEY `fk_acad_area_resv_acad_area` (`acad_area`)
    REFERENCES `timetable`.`academic_area` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_acad_area_resv_acad_class` FOREIGN KEY `fk_acad_area_resv_acad_class` (`acad_classification`)
    REFERENCES `timetable`.`academic_classification` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_acad_area_resv_type` FOREIGN KEY `fk_acad_area_resv_type` (`reservation_type`)
    REFERENCES `timetable`.`reservation_type` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`application_config`;
CREATE TABLE `timetable`.`application_config` (
  `name` VARCHAR(255) BINARY NOT NULL,
  `value` VARCHAR(4000) BINARY NULL,
  `description` VARCHAR(100) BINARY NULL,
  PRIMARY KEY (`name`)
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`assigned_instructors`;
CREATE TABLE `timetable`.`assigned_instructors` (
  `assignment_id` DECIMAL(20, 0) NOT NULL,
  `instructor_id` DECIMAL(20, 0) NOT NULL,
  `last_modified_time` DATETIME NULL,
  PRIMARY KEY (`instructor_id`, `assignment_id`),
  INDEX `idx_assigned_instructors` (`assignment_id`),
  CONSTRAINT `fk_assigned_instrs_assignment` FOREIGN KEY `fk_assigned_instrs_assignment` (`assignment_id`)
    REFERENCES `timetable`.`assignment` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_assigned_instrs_instructor` FOREIGN KEY `fk_assigned_instrs_instructor` (`instructor_id`)
    REFERENCES `timetable`.`departmental_instructor` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`assigned_rooms`;
CREATE TABLE `timetable`.`assigned_rooms` (
  `assignment_id` DECIMAL(20, 0) NOT NULL,
  `room_id` DECIMAL(20, 0) NOT NULL,
  `last_modified_time` DATETIME NULL,
  PRIMARY KEY (`room_id`, `assignment_id`),
  INDEX `idx_assigned_rooms` (`assignment_id`),
  CONSTRAINT `fk_assigned_rooms_assignment` FOREIGN KEY `fk_assigned_rooms_assignment` (`assignment_id`)
    REFERENCES `timetable`.`assignment` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`assignment`;
CREATE TABLE `timetable`.`assignment` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `days` BIGINT(10) NULL,
  `slot` BIGINT(10) NULL,
  `time_pattern_id` DECIMAL(20, 0) NULL,
  `solution_id` DECIMAL(20, 0) NULL,
  `class_id` DECIMAL(20, 0) NULL,
  `class_name` VARCHAR(100) BINARY NULL,
  `last_modified_time` DATETIME NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_assignment_class` (`class_id`),
  INDEX `idx_assignment_solution_index` (`solution_id`),
  INDEX `idx_assignment_time_pattern` (`time_pattern_id`),
  CONSTRAINT `fk_assignment_class` FOREIGN KEY `fk_assignment_class` (`class_id`)
    REFERENCES `timetable`.`class_` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_assignment_solution` FOREIGN KEY `fk_assignment_solution` (`solution_id`)
    REFERENCES `timetable`.`solution` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_assignment_time_pattern` FOREIGN KEY `fk_assignment_time_pattern` (`time_pattern_id`)
    REFERENCES `timetable`.`time_pattern` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`building`;
CREATE TABLE `timetable`.`building` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `session_id` DECIMAL(20, 0) NULL,
  `abbreviation` VARCHAR(10) BINARY NULL,
  `name` VARCHAR(100) BINARY NULL,
  `coordinate_x` BIGINT(10) NULL,
  `coordinate_y` BIGINT(10) NULL,
  `external_uid` VARCHAR(40) BINARY NULL,
  PRIMARY KEY (`uniqueid`),
  UNIQUE INDEX `uk_building` (`session_id`, `abbreviation`(10)),
  CONSTRAINT `fk_building_session` FOREIGN KEY `fk_building_session` (`session_id`)
    REFERENCES `timetable`.`sessions` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`building_pref`;
CREATE TABLE `timetable`.`building_pref` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `owner_id` DECIMAL(20, 0) NULL,
  `pref_level_id` DECIMAL(20, 0) NULL,
  `bldg_id` DECIMAL(20, 0) NULL,
  `distance_from` INT(5) NULL,
  `last_modified_time` DATETIME NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_building_pref_bldg` (`bldg_id`),
  INDEX `idx_building_pref_level` (`pref_level_id`),
  INDEX `idx_building_pref_owner` (`owner_id`),
  CONSTRAINT `fk_building_pref_bldg` FOREIGN KEY `fk_building_pref_bldg` (`bldg_id`)
    REFERENCES `timetable`.`building` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_building_pref_level` FOREIGN KEY `fk_building_pref_level` (`pref_level_id`)
    REFERENCES `timetable`.`preference_level` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`change_log`;
CREATE TABLE `timetable`.`change_log` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `session_id` DECIMAL(20, 0) NULL,
  `manager_id` DECIMAL(20, 0) NULL,
  `time_stamp` DATETIME NULL,
  `obj_type` VARCHAR(255) BINARY NULL,
  `obj_uid` DECIMAL(20, 0) NULL,
  `obj_title` VARCHAR(255) BINARY NULL,
  `subj_area_id` DECIMAL(20, 0) NULL,
  `department_id` DECIMAL(20, 0) NULL,
  `source` VARCHAR(50) BINARY NULL,
  `operation` VARCHAR(50) BINARY NULL,
  `detail` LONGBLOB NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_change_log_department` (`department_id`),
  INDEX `idx_change_log_object` (`obj_type`(255), `obj_uid`),
  INDEX `idx_change_log_sessionmgr` (`session_id`, `manager_id`),
  INDEX `idx_change_log_subjarea` (`subj_area_id`),
  CONSTRAINT `fk_change_log_department` FOREIGN KEY `fk_change_log_department` (`department_id`)
    REFERENCES `timetable`.`department` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_change_log_manager` FOREIGN KEY `fk_change_log_manager` (`manager_id`)
    REFERENCES `timetable`.`timetable_manager` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_change_log_session` FOREIGN KEY `fk_change_log_session` (`session_id`)
    REFERENCES `timetable`.`sessions` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_change_log_subjarea` FOREIGN KEY `fk_change_log_subjarea` (`subj_area_id`)
    REFERENCES `timetable`.`subject_area` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`class_`;
CREATE TABLE `timetable`.`class_` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `subpart_id` DECIMAL(20, 0) NULL,
  `expected_capacity` INT(4) NULL,
  `nbr_rooms` INT(4) NULL,
  `parent_class_id` DECIMAL(20, 0) NULL,
  `owner_id` DECIMAL(20, 0) NULL,
  `room_capacity` INT(4) NULL,
  `notes` VARCHAR(1000) BINARY NULL,
  `date_pattern_id` DECIMAL(20, 0) NULL,
  `managing_dept` DECIMAL(20, 0) NULL,
  `display_instructor` INT(1) NULL,
  `sched_print_note` VARCHAR(40) BINARY NULL,
  `class_suffix` VARCHAR(6) BINARY NULL,
  `display_in_sched_book` INT(1) NULL DEFAULT 1,
  `max_expected_capacity` INT(4) NULL,
  `room_ratio` DECIMAL(22, 0) NULL DEFAULT 1.0,
  `section_number` INT(5) NULL,
  `last_modified_time` DATETIME NULL,
  `uid_rolled_fwd_from` DECIMAL(20, 0) NULL,
  `external_uid` VARCHAR(40) BINARY NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_class_datepatt` (`date_pattern_id`),
  INDEX `idx_class_managing_dept` (`managing_dept`),
  INDEX `idx_class_parent` (`parent_class_id`),
  INDEX `idx_class_subpart_id` (`subpart_id`),
  CONSTRAINT `fk_class_datepatt` FOREIGN KEY `fk_class_datepatt` (`date_pattern_id`)
    REFERENCES `timetable`.`date_pattern` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_class_parent` FOREIGN KEY `fk_class_parent` (`parent_class_id`)
    REFERENCES `timetable`.`class_` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_class_scheduling_subpart` FOREIGN KEY `fk_class_scheduling_subpart` (`subpart_id`)
    REFERENCES `timetable`.`scheduling_subpart` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`class_instructor`;
CREATE TABLE `timetable`.`class_instructor` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `class_id` DECIMAL(20, 0) NULL,
  `instructor_id` DECIMAL(20, 0) NULL,
  `percent_share` INT(3) NULL,
  `is_lead` INT(1) NULL,
  `last_modified_time` DATETIME NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_class_instructor_class` (`class_id`),
  INDEX `idx_class_instructor_instr` (`instructor_id`),
  CONSTRAINT `fk_class_instructor_class` FOREIGN KEY `fk_class_instructor_class` (`class_id`)
    REFERENCES `timetable`.`class_` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_class_instructor_instr` FOREIGN KEY `fk_class_instructor_instr` (`instructor_id`)
    REFERENCES `timetable`.`departmental_instructor` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`class_waitlist`;
CREATE TABLE `timetable`.`class_waitlist` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `student_id` DECIMAL(20, 0) NULL,
  `course_request_id` DECIMAL(20, 0) NULL,
  `class_id` DECIMAL(20, 0) NULL,
  `type` BIGINT(10) NULL DEFAULT 0,
  `timestamp` DATETIME NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_class_waitlist_class` (`class_id`),
  INDEX `idx_class_waitlist_req` (`course_request_id`),
  INDEX `idx_class_waitlist_student` (`student_id`),
  CONSTRAINT `fk_class_waitlist_class` FOREIGN KEY `fk_class_waitlist_class` (`class_id`)
    REFERENCES `timetable`.`class_` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_class_waitlist_request` FOREIGN KEY `fk_class_waitlist_request` (`course_request_id`)
    REFERENCES `timetable`.`course_request` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_class_waitlist_student` FOREIGN KEY `fk_class_waitlist_student` (`student_id`)
    REFERENCES `timetable`.`student` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`constraint_info`;
CREATE TABLE `timetable`.`constraint_info` (
  `assignment_id` DECIMAL(20, 0) NOT NULL,
  `solver_info_id` DECIMAL(20, 0) NOT NULL,
  PRIMARY KEY (`solver_info_id`, `assignment_id`),
  INDEX `idx_constraint_info` (`assignment_id`),
  CONSTRAINT `fk_constraint_info_assignment` FOREIGN KEY `fk_constraint_info_assignment` (`assignment_id`)
    REFERENCES `timetable`.`assignment` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_constraint_info_solver` FOREIGN KEY `fk_constraint_info_solver` (`solver_info_id`)
    REFERENCES `timetable`.`solver_info` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`course_catalog`;
CREATE TABLE `timetable`.`course_catalog` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `session_id` DECIMAL(20, 0) NULL,
  `external_uid` VARCHAR(40) BINARY NULL,
  `subject` VARCHAR(10) BINARY NULL,
  `course_nbr` VARCHAR(10) BINARY NULL,
  `title` VARCHAR(100) BINARY NULL,
  `perm_id` VARCHAR(20) BINARY NULL,
  `approval_type` VARCHAR(20) BINARY NULL,
  `designator_req` INT(1) NULL,
  `prev_subject` VARCHAR(10) BINARY NULL,
  `prev_crs_nbr` VARCHAR(10) BINARY NULL,
  `credit_type` VARCHAR(20) BINARY NULL,
  `credit_unit_type` VARCHAR(20) BINARY NULL,
  `credit_format` VARCHAR(20) BINARY NULL,
  `fixed_min_credit` BIGINT(10) NULL,
  `max_credit` BIGINT(10) NULL,
  `frac_credit_allowed` INT(1) NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_course_catalog` (`session_id`, `subject`(10), `course_nbr`(10))
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`course_credit_type`;
CREATE TABLE `timetable`.`course_credit_type` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `reference` VARCHAR(20) BINARY NULL,
  `label` VARCHAR(60) BINARY NULL,
  `abbreviation` VARCHAR(10) BINARY NULL,
  `legacy_crse_master_code` VARCHAR(10) BINARY NULL,
  PRIMARY KEY (`uniqueid`),
  UNIQUE INDEX `uk_course_credit_type_ref` (`reference`(20))
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`course_credit_unit_config`;
CREATE TABLE `timetable`.`course_credit_unit_config` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `credit_format` VARCHAR(20) BINARY NULL,
  `owner_id` DECIMAL(20, 0) NULL,
  `credit_type` DECIMAL(20, 0) NULL,
  `credit_unit_type` DECIMAL(20, 0) NULL,
  `defines_credit_at_course_level` INT(1) NULL,
  `fixed_units` DECIMAL(22, 0) NULL,
  `min_units` DECIMAL(22, 0) NULL,
  `max_units` DECIMAL(22, 0) NULL,
  `fractional_incr_allowed` INT(1) NULL,
  `instr_offr_id` DECIMAL(20, 0) NULL,
  `last_modified_time` DATETIME NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_crs_crdt_unit_cfg_crd_type` (`credit_type`),
  INDEX `idx_crs_crdt_unit_cfg_io_own` (`instr_offr_id`),
  INDEX `idx_crs_crdt_unit_cfg_owner` (`owner_id`),
  CONSTRAINT `fk_crs_crdt_unit_cfg_crdt_type` FOREIGN KEY `fk_crs_crdt_unit_cfg_crdt_type` (`credit_type`)
    REFERENCES `timetable`.`course_credit_type` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_crs_crdt_unit_cfg_io_own` FOREIGN KEY `fk_crs_crdt_unit_cfg_io_own` (`instr_offr_id`)
    REFERENCES `timetable`.`instructional_offering` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_crs_crdt_unit_cfg_owner` FOREIGN KEY `fk_crs_crdt_unit_cfg_owner` (`owner_id`)
    REFERENCES `timetable`.`scheduling_subpart` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`course_credit_unit_type`;
CREATE TABLE `timetable`.`course_credit_unit_type` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `reference` VARCHAR(20) BINARY NULL,
  `label` VARCHAR(60) BINARY NULL,
  `abbreviation` VARCHAR(10) BINARY NULL,
  PRIMARY KEY (`uniqueid`),
  UNIQUE INDEX `uk_crs_crdt_unit_type_ref` (`reference`(20))
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`course_demand`;
CREATE TABLE `timetable`.`course_demand` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `student_id` DECIMAL(20, 0) NULL,
  `priority` BIGINT(10) NULL,
  `waitlist` INT(1) NULL,
  `is_alternative` INT(1) NULL,
  `timestamp` DATETIME NULL,
  `free_time_id` DECIMAL(20, 0) NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_course_demand_free_time` (`free_time_id`),
  INDEX `idx_course_demand_student` (`student_id`),
  CONSTRAINT `fk_course_demand_free_time` FOREIGN KEY `fk_course_demand_free_time` (`free_time_id`)
    REFERENCES `timetable`.`free_time` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_course_demand_student` FOREIGN KEY `fk_course_demand_student` (`student_id`)
    REFERENCES `timetable`.`student` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`course_offering`;
CREATE TABLE `timetable`.`course_offering` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `course_nbr` VARCHAR(10) BINARY NULL,
  `is_control` INT(1) NULL,
  `perm_id` VARCHAR(20) BINARY NULL,
  `proj_demand` BIGINT(10) NULL,
  `instr_offr_id` DECIMAL(20, 0) NULL,
  `subject_area_id` DECIMAL(20, 0) NULL,
  `title` VARCHAR(90) BINARY NULL,
  `schedule_book_note` VARCHAR(1000) BINARY NULL,
  `demand_offering_id` DECIMAL(20, 0) NULL,
  `demand_offering_type` DECIMAL(20, 0) NULL,
  `nbr_expected_stdents` BIGINT(10) NULL DEFAULT 0,
  `external_uid` VARCHAR(40) BINARY NULL,
  `last_modified_time` DATETIME NULL,
  `uid_rolled_fwd_from` DECIMAL(20, 0) NULL,
  `lastlike_demand` BIGINT(10) NULL DEFAULT 0,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_course_offering_control` (`is_control`),
  INDEX `idx_course_offering_demd_offr` (`demand_offering_id`),
  INDEX `idx_course_offering_instr_offr` (`instr_offr_id`),
  UNIQUE INDEX `uk_course_offering_subj_crs` (`course_nbr`(10), `subject_area_id`),
  CONSTRAINT `fk_course_offering_demand_offr` FOREIGN KEY `fk_course_offering_demand_offr` (`demand_offering_id`)
    REFERENCES `timetable`.`course_offering` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_course_offering_instr_offr` FOREIGN KEY `fk_course_offering_instr_offr` (`instr_offr_id`)
    REFERENCES `timetable`.`instructional_offering` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_course_offering_subj_area` FOREIGN KEY `fk_course_offering_subj_area` (`subject_area_id`)
    REFERENCES `timetable`.`subject_area` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`course_request`;
CREATE TABLE `timetable`.`course_request` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `course_demand_id` DECIMAL(20, 0) NULL,
  `course_offering_id` DECIMAL(20, 0) NULL,
  `ord` BIGINT(10) NULL,
  `allow_overlap` INT(1) NULL,
  `credit` BIGINT(10) NULL DEFAULT 0,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_course_request_demand` (`course_demand_id`),
  INDEX `idx_course_request_offering` (`course_offering_id`),
  CONSTRAINT `fk_course_request_demand` FOREIGN KEY `fk_course_request_demand` (`course_demand_id`)
    REFERENCES `timetable`.`course_demand` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_course_request_offering` FOREIGN KEY `fk_course_request_offering` (`course_offering_id`)
    REFERENCES `timetable`.`course_offering` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`course_request_option`;
CREATE TABLE `timetable`.`course_request_option` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `course_request_id` DECIMAL(20, 0) NULL,
  `option_type` BIGINT(10) NULL,
  `value` LONGBLOB NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_course_request_option_req` (`course_request_id`),
  CONSTRAINT `fk_course_request_options_req` FOREIGN KEY `fk_course_request_options_req` (`course_request_id`)
    REFERENCES `timetable`.`course_request` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`course_reservation`;
CREATE TABLE `timetable`.`course_reservation` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `owner` DECIMAL(20, 0) NULL,
  `reservation_type` DECIMAL(20, 0) NULL,
  `course_offering` DECIMAL(20, 0) NULL,
  `priority` INT(5) NULL,
  `reserved` BIGINT(10) NULL,
  `prior_enrollment` BIGINT(10) NULL,
  `projected_enrollment` BIGINT(10) NULL,
  `owner_class_id` VARCHAR(1) BINARY NULL,
  `requested` BIGINT(10) NULL,
  `last_modified_time` DATETIME NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_course_resv_crs_offr` (`course_offering`),
  INDEX `idx_course_resv_owner` (`owner`),
  INDEX `idx_course_resv_owner_cls` (`owner_class_id`(1)),
  INDEX `idx_course_resv_type` (`reservation_type`),
  CONSTRAINT `fk_course_reserv_type` FOREIGN KEY `fk_course_reserv_type` (`reservation_type`)
    REFERENCES `timetable`.`reservation_type` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_course_resv_crs_offr` FOREIGN KEY `fk_course_resv_crs_offr` (`course_offering`)
    REFERENCES `timetable`.`course_offering` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`course_subpart_credit`;
CREATE TABLE `timetable`.`course_subpart_credit` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `course_catalog_id` DECIMAL(20, 0) NULL,
  `subpart_id` VARCHAR(10) BINARY NULL,
  `credit_type` VARCHAR(20) BINARY NULL,
  `credit_unit_type` VARCHAR(20) BINARY NULL,
  `credit_format` VARCHAR(20) BINARY NULL,
  `fixed_min_credit` BIGINT(10) NULL,
  `max_credit` BIGINT(10) NULL,
  `frac_credit_allowed` INT(1) NULL,
  PRIMARY KEY (`uniqueid`),
  CONSTRAINT `fk_subpart_cred_crs` FOREIGN KEY `fk_subpart_cred_crs` (`course_catalog_id`)
    REFERENCES `timetable`.`course_catalog` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`crse_credit_format`;
CREATE TABLE `timetable`.`crse_credit_format` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `reference` VARCHAR(20) BINARY NULL,
  `label` VARCHAR(60) BINARY NULL,
  `abbreviation` VARCHAR(10) BINARY NULL,
  PRIMARY KEY (`uniqueid`),
  UNIQUE INDEX `uk_crse_credit_format_ref` (`reference`(20))
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`date_pattern`;
CREATE TABLE `timetable`.`date_pattern` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `name` VARCHAR(50) BINARY NULL,
  `pattern` VARCHAR(366) BINARY NULL,
  `offset` BIGINT(10) NULL,
  `type` BIGINT(10) NULL,
  `visible` INT(1) NULL,
  `session_id` DECIMAL(20, 0) NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_date_pattern_session` (`session_id`),
  CONSTRAINT `fk_date_pattern_session` FOREIGN KEY `fk_date_pattern_session` (`session_id`)
    REFERENCES `timetable`.`sessions` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`date_pattern_dept`;
CREATE TABLE `timetable`.`date_pattern_dept` (
  `dept_id` DECIMAL(20, 0) NOT NULL,
  `pattern_id` DECIMAL(20, 0) NOT NULL,
  PRIMARY KEY (`pattern_id`, `dept_id`),
  CONSTRAINT `fk_date_pattern_dept_date` FOREIGN KEY `fk_date_pattern_dept_date` (`pattern_id`)
    REFERENCES `timetable`.`date_pattern` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_date_pattern_dept_dept` FOREIGN KEY `fk_date_pattern_dept_dept` (`dept_id`)
    REFERENCES `timetable`.`department` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`demand_offr_type`;
CREATE TABLE `timetable`.`demand_offr_type` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `reference` VARCHAR(20) BINARY NULL,
  `label` VARCHAR(60) BINARY NULL,
  PRIMARY KEY (`uniqueid`),
  UNIQUE INDEX `uk_demand_offr_type_label` (`label`(60)),
  UNIQUE INDEX `uk_demand_offr_type_ref` (`reference`(20))
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`department`;
CREATE TABLE `timetable`.`department` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `session_id` DECIMAL(20, 0) NULL,
  `abbreviation` VARCHAR(20) BINARY NULL,
  `name` VARCHAR(100) BINARY NULL,
  `dept_code` VARCHAR(50) BINARY NULL,
  `external_uid` VARCHAR(40) BINARY NULL,
  `rs_color` VARCHAR(6) BINARY NULL,
  `external_manager` INT(1) NULL,
  `external_mgr_label` VARCHAR(30) BINARY NULL,
  `external_mgr_abbv` VARCHAR(10) BINARY NULL,
  `solver_group_id` DECIMAL(20, 0) NULL,
  `status_type` DECIMAL(20, 0) NULL,
  `dist_priority` BIGINT(10) NULL DEFAULT 0,
  `allow_req_time` INT(1) NULL DEFAULT 0,
  `allow_req_room` INT(1) NULL DEFAULT 0,
  `last_modified_time` DATETIME NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_department_solver_grp` (`solver_group_id`),
  INDEX `idx_department_status_type` (`status_type`),
  UNIQUE INDEX `uk_department_dept_code` (`session_id`, `dept_code`(50)),
  CONSTRAINT `fk_department_solver_group` FOREIGN KEY `fk_department_solver_group` (`solver_group_id`)
    REFERENCES `timetable`.`solver_group` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_department_status_type` FOREIGN KEY `fk_department_status_type` (`status_type`)
    REFERENCES `timetable`.`dept_status_type` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`departmental_instructor`;
CREATE TABLE `timetable`.`departmental_instructor` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `external_uid` VARCHAR(40) BINARY NULL,
  `career_acct` VARCHAR(20) BINARY NULL,
  `lname` VARCHAR(26) BINARY NULL,
  `fname` VARCHAR(20) BINARY NULL,
  `mname` VARCHAR(20) BINARY NULL,
  `pos_code_type` DECIMAL(20, 0) NULL,
  `note` VARCHAR(20) BINARY NULL,
  `department_uniqueid` DECIMAL(20, 0) NULL,
  `ignore_too_far` INT(1) NULL DEFAULT 0,
  `last_modified_time` DATETIME NULL,
  `email` VARCHAR(200) BINARY NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_dept_instr_dept` (`department_uniqueid`),
  INDEX `idx_dept_instr_position_type` (`pos_code_type`),
  CONSTRAINT `fk_dept_instr_dept` FOREIGN KEY `fk_dept_instr_dept` (`department_uniqueid`)
    REFERENCES `timetable`.`department` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_dept_instr_pos_code_type` FOREIGN KEY `fk_dept_instr_pos_code_type` (`pos_code_type`)
    REFERENCES `timetable`.`position_type` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`dept_status_type`;
CREATE TABLE `timetable`.`dept_status_type` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `reference` VARCHAR(20) BINARY NULL,
  `label` VARCHAR(60) BINARY NULL,
  `status` BIGINT(10) NULL,
  `apply` BIGINT(10) NULL,
  `ord` BIGINT(10) NULL,
  PRIMARY KEY (`uniqueid`)
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`dept_to_tt_mgr`;
CREATE TABLE `timetable`.`dept_to_tt_mgr` (
  `timetable_mgr_id` DECIMAL(20, 0) NOT NULL,
  `department_id` DECIMAL(20, 0) NOT NULL,
  PRIMARY KEY (`department_id`, `timetable_mgr_id`),
  CONSTRAINT `fk_dept_to_tt_mgr_dept` FOREIGN KEY `fk_dept_to_tt_mgr_dept` (`department_id`)
    REFERENCES `timetable`.`department` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_dept_to_tt_mgr_mgr` FOREIGN KEY `fk_dept_to_tt_mgr_mgr` (`timetable_mgr_id`)
    REFERENCES `timetable`.`timetable_manager` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`designator`;
CREATE TABLE `timetable`.`designator` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `subject_area_id` DECIMAL(20, 0) NULL,
  `instructor_id` DECIMAL(20, 0) NULL,
  `code` VARCHAR(3) BINARY NULL,
  `last_modified_time` DATETIME NULL,
  PRIMARY KEY (`uniqueid`),
  UNIQUE INDEX `uk_designator_code` (`subject_area_id`, `instructor_id`, `code`(3)),
  CONSTRAINT `fk_designator_instructor` FOREIGN KEY `fk_designator_instructor` (`instructor_id`)
    REFERENCES `timetable`.`departmental_instructor` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_designator_subj_area` FOREIGN KEY `fk_designator_subj_area` (`subject_area_id`)
    REFERENCES `timetable`.`subject_area` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`distribution_object`;
CREATE TABLE `timetable`.`distribution_object` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `dist_pref_id` DECIMAL(20, 0) NULL,
  `sequence_number` INT(3) NULL,
  `pref_group_id` DECIMAL(20, 0) NULL,
  `last_modified_time` DATETIME NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_distribution_object_pg` (`pref_group_id`),
  INDEX `idx_distribution_object_pref` (`dist_pref_id`),
  CONSTRAINT `fk_distribution_object_pref` FOREIGN KEY `fk_distribution_object_pref` (`dist_pref_id`)
    REFERENCES `timetable`.`distribution_pref` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`distribution_pref`;
CREATE TABLE `timetable`.`distribution_pref` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `owner_id` DECIMAL(20, 0) NULL,
  `pref_level_id` DECIMAL(20, 0) NULL,
  `dist_type_id` DECIMAL(20, 0) NULL,
  `grouping` BIGINT(10) NULL,
  `last_modified_time` DATETIME NULL,
  `uid_rolled_fwd_from` DECIMAL(20, 0) NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_distribution_pref_level` (`pref_level_id`),
  INDEX `idx_distribution_pref_owner` (`owner_id`),
  INDEX `idx_distribution_pref_type` (`dist_type_id`),
  CONSTRAINT `fk_distribution_pref_dist_type` FOREIGN KEY `fk_distribution_pref_dist_type` (`dist_type_id`)
    REFERENCES `timetable`.`distribution_type` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_distribution_pref_level` FOREIGN KEY `fk_distribution_pref_level` (`pref_level_id`)
    REFERENCES `timetable`.`preference_level` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`distribution_type`;
CREATE TABLE `timetable`.`distribution_type` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `reference` VARCHAR(20) BINARY NULL,
  `label` VARCHAR(60) BINARY NULL,
  `sequencing_required` VARCHAR(1) BINARY NULL DEFAULT '0',
  `req_id` INT(6) NULL,
  `allowed_pref` VARCHAR(10) BINARY NULL,
  `description` VARCHAR(2048) BINARY NULL,
  `abbreviation` VARCHAR(20) BINARY NULL,
  `instructor_pref` INT(1) NULL DEFAULT 0,
  `exam_pref` INT(1) NULL DEFAULT 0,
  PRIMARY KEY (`uniqueid`),
  UNIQUE INDEX `uk_distribution_type_req_id` (`req_id`)
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`dist_type_dept`;
CREATE TABLE `timetable`.`dist_type_dept` (
  `dist_type_id` DECIMAL(19, 0) NOT NULL,
  `dept_id` DECIMAL(20, 0) NOT NULL,
  PRIMARY KEY (`dist_type_id`, `dept_id`),
  CONSTRAINT `fk_dist_type_dept_dept` FOREIGN KEY `fk_dist_type_dept_dept` (`dept_id`)
    REFERENCES `timetable`.`department` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_dist_type_dept_type` FOREIGN KEY `fk_dist_type_dept_type` (`dist_type_id`)
    REFERENCES `timetable`.`distribution_type` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`event`;
CREATE TABLE `timetable`.`event` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `event_name` VARCHAR(100) BINARY NULL,
  `min_capacity` BIGINT(10) NULL,
  `max_capacity` BIGINT(10) NULL,
  `sponsoring_org` DECIMAL(20, 0) NULL,
  `main_contact_id` DECIMAL(20, 0) NULL,
  `class_id` DECIMAL(20, 0) NULL,
  `exam_id` DECIMAL(20, 0) NULL,
  `event_type` BIGINT(10) NULL,
  `req_attd` INT(1) NULL,
  PRIMARY KEY (`uniqueid`),
  CONSTRAINT `fk_event_class` FOREIGN KEY `fk_event_class` (`class_id`)
    REFERENCES `timetable`.`class_` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_event_exam` FOREIGN KEY `fk_event_exam` (`exam_id`)
    REFERENCES `timetable`.`exam` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_event_main_contact` FOREIGN KEY `fk_event_main_contact` (`main_contact_id`)
    REFERENCES `timetable`.`event_contact` (`uniqueid`)
    ON DELETE SET NULL
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`event_contact`;
CREATE TABLE `timetable`.`event_contact` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `external_id` VARCHAR(40) BINARY NULL,
  `email` VARCHAR(100) BINARY NOT NULL,
  `phone` VARCHAR(10) BINARY NOT NULL,
  `firstname` VARCHAR(20) BINARY NULL,
  `middlename` VARCHAR(20) BINARY NULL,
  `lastname` VARCHAR(30) BINARY NULL,
  PRIMARY KEY (`uniqueid`)
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`event_join_event_contact`;
CREATE TABLE `timetable`.`event_join_event_contact` (
  `event_id` DECIMAL(20, 0) NOT NULL,
  `event_contact_id` DECIMAL(20, 0) NOT NULL,
  CONSTRAINT `fk_event_contact_join` FOREIGN KEY `fk_event_contact_join` (`event_contact_id`)
    REFERENCES `timetable`.`event_contact` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_event_id_join` FOREIGN KEY `fk_event_id_join` (`event_id`)
    REFERENCES `timetable`.`event` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`event_note`;
CREATE TABLE `timetable`.`event_note` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `event_id` DECIMAL(20, 0) NOT NULL,
  `note_id` DECIMAL(20, 0) NULL,
  `text_note` VARCHAR(1000) BINARY NULL,
  PRIMARY KEY (`uniqueid`),
  CONSTRAINT `fk_event_note_event` FOREIGN KEY `fk_event_note_event` (`event_id`)
    REFERENCES `timetable`.`event` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_event_note_std_note` FOREIGN KEY `fk_event_note_std_note` (`note_id`)
    REFERENCES `timetable`.`standard_event_note` (`uniqueid`)
    ON DELETE SET NULL
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`exact_time_mins`;
CREATE TABLE `timetable`.`exact_time_mins` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `mins_min` INT(4) NULL,
  `mins_max` INT(4) NULL,
  `nr_slots` INT(4) NULL,
  `break_time` INT(4) NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_exact_time_mins` (`mins_min`, `mins_max`)
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`exam`;
CREATE TABLE `timetable`.`exam` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `session_id` DECIMAL(20, 0) NOT NULL,
  `name` VARCHAR(100) BINARY NULL,
  `note` VARCHAR(1000) BINARY NULL,
  `length` BIGINT(10) NOT NULL,
  `max_nbr_rooms` BIGINT(10) NOT NULL DEFAULT 1,
  `seating_type` BIGINT(10) NOT NULL,
  `assigned_period` DECIMAL(20, 0) NULL,
  `assigned_pref` VARCHAR(100) BINARY NULL,
  `exam_type` BIGINT(10) NULL DEFAULT 0,
  `avg_period` BIGINT(10) NULL,
  `uid_rolled_fwd_from` DECIMAL(20, 0) NULL,
  PRIMARY KEY (`uniqueid`),
  CONSTRAINT `fk_exam_period` FOREIGN KEY `fk_exam_period` (`assigned_period`)
    REFERENCES `timetable`.`exam_period` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_exam_session` FOREIGN KEY `fk_exam_session` (`session_id`)
    REFERENCES `timetable`.`sessions` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`exam_instructor`;
CREATE TABLE `timetable`.`exam_instructor` (
  `exam_id` DECIMAL(20, 0) NOT NULL,
  `instructor_id` DECIMAL(20, 0) NOT NULL,
  PRIMARY KEY (`exam_id`, `instructor_id`),
  CONSTRAINT `fk_exam_instructor_exam` FOREIGN KEY `fk_exam_instructor_exam` (`exam_id`)
    REFERENCES `timetable`.`exam` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_exam_instructor_instructor` FOREIGN KEY `fk_exam_instructor_instructor` (`instructor_id`)
    REFERENCES `timetable`.`departmental_instructor` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`exam_location_pref`;
CREATE TABLE `timetable`.`exam_location_pref` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `location_id` DECIMAL(20, 0) NOT NULL,
  `pref_level_id` DECIMAL(20, 0) NOT NULL,
  `period_id` DECIMAL(20, 0) NOT NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_exam_location_pref` (`location_id`),
  CONSTRAINT `fk_exam_location_pref_period` FOREIGN KEY `fk_exam_location_pref_period` (`period_id`)
    REFERENCES `timetable`.`exam_period` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_exam_location_pref_pref` FOREIGN KEY `fk_exam_location_pref_pref` (`pref_level_id`)
    REFERENCES `timetable`.`preference_level` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`exam_owner`;
CREATE TABLE `timetable`.`exam_owner` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `exam_id` DECIMAL(20, 0) NOT NULL,
  `owner_id` DECIMAL(20, 0) NOT NULL,
  `owner_type` BIGINT(10) NOT NULL,
  `course_id` DECIMAL(20, 0) NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_exam_owner_course` (`course_id`),
  INDEX `idx_exam_owner_exam` (`exam_id`),
  INDEX `idx_exam_owner_owner` (`owner_id`, `owner_type`),
  CONSTRAINT `fk_exam_owner_course` FOREIGN KEY `fk_exam_owner_course` (`course_id`)
    REFERENCES `timetable`.`course_offering` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_exam_owner_exam` FOREIGN KEY `fk_exam_owner_exam` (`exam_id`)
    REFERENCES `timetable`.`exam` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`exam_period`;
CREATE TABLE `timetable`.`exam_period` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `session_id` DECIMAL(20, 0) NOT NULL,
  `date_ofs` BIGINT(10) NOT NULL,
  `start_slot` BIGINT(10) NOT NULL,
  `length` BIGINT(10) NOT NULL,
  `pref_level_id` DECIMAL(20, 0) NOT NULL,
  `exam_type` BIGINT(10) NULL DEFAULT 0,
  PRIMARY KEY (`uniqueid`),
  CONSTRAINT `fk_exam_period_pref` FOREIGN KEY `fk_exam_period_pref` (`pref_level_id`)
    REFERENCES `timetable`.`preference_level` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_exam_period_session` FOREIGN KEY `fk_exam_period_session` (`session_id`)
    REFERENCES `timetable`.`sessions` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`exam_period_pref`;
CREATE TABLE `timetable`.`exam_period_pref` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `owner_id` DECIMAL(20, 0) NOT NULL,
  `pref_level_id` DECIMAL(20, 0) NOT NULL,
  `period_id` DECIMAL(20, 0) NOT NULL,
  PRIMARY KEY (`uniqueid`),
  CONSTRAINT `fk_exam_period_pref_period` FOREIGN KEY `fk_exam_period_pref_period` (`period_id`)
    REFERENCES `timetable`.`exam_period` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_exam_period_pref_pref` FOREIGN KEY `fk_exam_period_pref_pref` (`pref_level_id`)
    REFERENCES `timetable`.`preference_level` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`exam_room_assignment`;
CREATE TABLE `timetable`.`exam_room_assignment` (
  `exam_id` DECIMAL(20, 0) NOT NULL,
  `location_id` DECIMAL(20, 0) NOT NULL,
  PRIMARY KEY (`exam_id`, `location_id`),
  CONSTRAINT `fk_exam_room_exam` FOREIGN KEY `fk_exam_room_exam` (`exam_id`)
    REFERENCES `timetable`.`exam` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`external_building`;
CREATE TABLE `timetable`.`external_building` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `session_id` DECIMAL(20, 0) NULL,
  `external_uid` VARCHAR(40) BINARY NULL,
  `abbreviation` VARCHAR(10) BINARY NULL,
  `coordinate_x` BIGINT(10) NULL,
  `coordinate_y` BIGINT(10) NULL,
  `display_name` VARCHAR(100) BINARY NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_external_building` (`session_id`, `abbreviation`(10))
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`external_room`;
CREATE TABLE `timetable`.`external_room` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `external_bldg_id` DECIMAL(20, 0) NULL,
  `external_uid` VARCHAR(40) BINARY NULL,
  `room_number` VARCHAR(10) BINARY NULL,
  `coordinate_x` BIGINT(10) NULL,
  `coordinate_y` BIGINT(10) NULL,
  `capacity` BIGINT(10) NULL,
  `classification` VARCHAR(20) BINARY NULL,
  `scheduled_room_type` VARCHAR(20) BINARY NULL,
  `instructional` INT(1) NULL,
  `display_name` VARCHAR(100) BINARY NULL,
  `exam_capacity` BIGINT(10) NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_external_room` (`external_bldg_id`, `room_number`(10)),
  CONSTRAINT `fk_ext_room_building` FOREIGN KEY `fk_ext_room_building` (`external_bldg_id`)
    REFERENCES `timetable`.`external_building` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`external_room_department`;
CREATE TABLE `timetable`.`external_room_department` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `external_room_id` DECIMAL(20, 0) NULL,
  `department_code` VARCHAR(50) BINARY NULL,
  `percent` BIGINT(10) NULL,
  `assignment_type` VARCHAR(20) BINARY NULL,
  PRIMARY KEY (`uniqueid`),
  CONSTRAINT `fk_ext_dept_room` FOREIGN KEY `fk_ext_dept_room` (`external_room_id`)
    REFERENCES `timetable`.`external_room` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`external_room_feature`;
CREATE TABLE `timetable`.`external_room_feature` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `external_room_id` DECIMAL(20, 0) NULL,
  `name` VARCHAR(20) BINARY NULL,
  `value` VARCHAR(20) BINARY NULL,
  PRIMARY KEY (`uniqueid`),
  CONSTRAINT `fk_ext_ftr_room` FOREIGN KEY `fk_ext_ftr_room` (`external_room_id`)
    REFERENCES `timetable`.`external_room` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`free_time`;
CREATE TABLE `timetable`.`free_time` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `name` VARCHAR(50) BINARY NULL,
  `day_code` BIGINT(10) NULL,
  `start_slot` BIGINT(10) NULL,
  `length` BIGINT(10) NULL,
  `category` BIGINT(10) NULL,
  `session_id` DECIMAL(20, 0) NULL,
  PRIMARY KEY (`uniqueid`),
  CONSTRAINT `fk_free_time_session` FOREIGN KEY `fk_free_time_session` (`session_id`)
    REFERENCES `timetable`.`sessions` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`history`;
CREATE TABLE `timetable`.`history` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `subclass` VARCHAR(10) BINARY NULL,
  `old_value` VARCHAR(20) BINARY NULL,
  `new_value` VARCHAR(20) BINARY NULL,
  `old_number` VARCHAR(20) BINARY NULL,
  `new_number` VARCHAR(20) BINARY NULL,
  `session_id` DECIMAL(20, 0) NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_history_session` (`session_id`),
  CONSTRAINT `fk_history_session` FOREIGN KEY `fk_history_session` (`session_id`)
    REFERENCES `timetable`.`sessions` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`individual_reservation`;
CREATE TABLE `timetable`.`individual_reservation` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `owner` DECIMAL(20, 0) NULL,
  `reservation_type` DECIMAL(20, 0) NULL,
  `priority` INT(5) NULL,
  `external_uid` VARCHAR(40) BINARY NULL,
  `over_limit` INT(1) NULL,
  `expiration_date` DATETIME NULL,
  `owner_class_id` VARCHAR(1) BINARY NULL,
  `last_modified_time` DATETIME NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_individual_resv_owner` (`owner`),
  INDEX `idx_individual_resv_owner_cls` (`owner_class_id`(1)),
  INDEX `idx_individual_resv_type` (`reservation_type`),
  CONSTRAINT `fk_individual_resv_type` FOREIGN KEY `fk_individual_resv_type` (`reservation_type`)
    REFERENCES `timetable`.`reservation_type` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`instructional_offering`;
CREATE TABLE `timetable`.`instructional_offering` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `session_id` DECIMAL(20, 0) NULL,
  `instr_offering_perm_id` BIGINT(10) NULL,
  `not_offered` INT(1) NULL,
  `limit` INT(4) NULL,
  `consent_type` DECIMAL(20, 0) NULL,
  `designator_required` INT(1) NULL,
  `last_modified_time` DATETIME NULL,
  `uid_rolled_fwd_from` DECIMAL(20, 0) NULL,
  `external_uid` VARCHAR(40) BINARY NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_instr_offr_consent` (`consent_type`),
  CONSTRAINT `fk_instr_offr_consent_type` FOREIGN KEY `fk_instr_offr_consent_type` (`consent_type`)
    REFERENCES `timetable`.`offr_consent_type` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`instr_offering_config`;
CREATE TABLE `timetable`.`instr_offering_config` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `config_limit` BIGINT(10) NULL,
  `instr_offr_id` DECIMAL(20, 0) NULL,
  `unlimited_enrollment` INT(1) NULL,
  `name` VARCHAR(10) BINARY NULL,
  `last_modified_time` DATETIME NULL,
  `uid_rolled_fwd_from` DECIMAL(20, 0) NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_instr_offr_cfg_instr_offr` (`instr_offr_id`),
  UNIQUE INDEX `uk_instr_offr_cfg_name` (`uniqueid`, `name`(10)),
  CONSTRAINT `fk_instr_offr_cfg_instr_offr` FOREIGN KEY `fk_instr_offr_cfg_instr_offr` (`instr_offr_id`)
    REFERENCES `timetable`.`instructional_offering` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`itype_desc`;
CREATE TABLE `timetable`.`itype_desc` (
  `itype` INT(2) NOT NULL,
  `abbv` VARCHAR(7) BINARY NULL,
  `description` VARCHAR(50) BINARY NULL,
  `sis_ref` VARCHAR(20) BINARY NULL,
  `basic` INT(1) NULL,
  `parent` INT(2) NULL,
  `organized` INT(1) NULL,
  PRIMARY KEY (`itype`)
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`jenrl`;
CREATE TABLE `timetable`.`jenrl` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `jenrl` DOUBLE NULL,
  `solution_id` DECIMAL(20, 0) NULL,
  `class1_id` DECIMAL(20, 0) NULL,
  `class2_id` DECIMAL(20, 0) NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_jenrl` (`solution_id`),
  INDEX `idx_jenrl_class1` (`class1_id`),
  INDEX `idx_jenrl_class2` (`class2_id`),
  CONSTRAINT `fk_jenrl_class1` FOREIGN KEY `fk_jenrl_class1` (`class1_id`)
    REFERENCES `timetable`.`class_` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_jenrl_class2` FOREIGN KEY `fk_jenrl_class2` (`class2_id`)
    REFERENCES `timetable`.`class_` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_jenrl_solution` FOREIGN KEY `fk_jenrl_solution` (`solution_id`)
    REFERENCES `timetable`.`solution` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`lastlike_course_demand`;
CREATE TABLE `timetable`.`lastlike_course_demand` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `student_id` DECIMAL(20, 0) NULL,
  `subject_area_id` DECIMAL(20, 0) NULL,
  `course_nbr` VARCHAR(10) BINARY NULL,
  `priority` BIGINT(10) NULL DEFAULT 0,
  `course_perm_id` VARCHAR(20) BINARY NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_ll_course_demand_course` (`subject_area_id`, `course_nbr`(10)),
  INDEX `idx_ll_course_demand_permid` (`course_perm_id`(20)),
  INDEX `idx_ll_course_demand_student` (`student_id`),
  CONSTRAINT `fk_ll_course_demand_student` FOREIGN KEY `fk_ll_course_demand_student` (`student_id`)
    REFERENCES `timetable`.`student` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_ll_course_demand_subjarea` FOREIGN KEY `fk_ll_course_demand_subjarea` (`subject_area_id`)
    REFERENCES `timetable`.`subject_area` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`manager_settings`;
CREATE TABLE `timetable`.`manager_settings` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `key_id` DECIMAL(20, 0) NULL,
  `value` VARCHAR(100) BINARY NULL,
  `user_uniqueid` DECIMAL(20, 0) NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_manager_settings_key` (`key_id`),
  INDEX `idx_manager_settings_manager` (`user_uniqueid`),
  CONSTRAINT `fk_manager_settings_key` FOREIGN KEY `fk_manager_settings_key` (`key_id`)
    REFERENCES `timetable`.`settings` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_manager_settings_user` FOREIGN KEY `fk_manager_settings_user` (`user_uniqueid`)
    REFERENCES `timetable`.`timetable_manager` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`meeting`;
CREATE TABLE `timetable`.`meeting` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `event_id` DECIMAL(20, 0) NOT NULL,
  `meeting_date` DATETIME NOT NULL,
  `start_period` BIGINT(10) NOT NULL,
  `start_offset` BIGINT(10) NULL,
  `stop_period` BIGINT(10) NOT NULL,
  `stop_offset` BIGINT(10) NULL,
  `location_perm_id` DECIMAL(20, 0) NULL,
  `class_can_override` INT(1) NOT NULL,
  `approved_date` DATETIME NULL,
  PRIMARY KEY (`uniqueid`),
  CONSTRAINT `fk_meeting_event` FOREIGN KEY `fk_meeting_event` (`event_id`)
    REFERENCES `timetable`.`event` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`non_university_location`;
CREATE TABLE `timetable`.`non_university_location` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `session_id` DECIMAL(20, 0) NULL,
  `name` VARCHAR(20) BINARY NULL,
  `capacity` BIGINT(10) NULL,
  `coordinate_x` BIGINT(10) NULL,
  `coordinate_y` BIGINT(10) NULL,
  `ignore_too_far` INT(1) NULL,
  `manager_ids` VARCHAR(200) BINARY NULL,
  `pattern` VARCHAR(350) BINARY NULL,
  `ignore_room_check` INT(1) NULL DEFAULT 0,
  `display_name` VARCHAR(100) BINARY NULL,
  `exam_capacity` BIGINT(10) NULL DEFAULT 0,
  `permanent_id` DECIMAL(20, 0) NOT NULL,
  `exam_type` BIGINT(10) NULL DEFAULT 0,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_non_univ_loc_session` (`session_id`),
  CONSTRAINT `fk_non_univ_loc_session` FOREIGN KEY `fk_non_univ_loc_session` (`session_id`)
    REFERENCES `timetable`.`sessions` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`offr_consent_type`;
CREATE TABLE `timetable`.`offr_consent_type` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `reference` VARCHAR(20) BINARY NULL,
  `label` VARCHAR(60) BINARY NULL,
  PRIMARY KEY (`uniqueid`),
  UNIQUE INDEX `uk_offr_consent_type_label` (`label`(60)),
  UNIQUE INDEX `uk_offr_consent_type_ref` (`reference`(20))
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`offr_group`;
CREATE TABLE `timetable`.`offr_group` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `session_id` DECIMAL(20, 0) NULL,
  `name` VARCHAR(20) BINARY NULL,
  `description` VARCHAR(200) BINARY NULL,
  `department_id` DECIMAL(20, 0) NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_offr_group_dept` (`department_id`),
  INDEX `idx_offr_group_session` (`session_id`),
  CONSTRAINT `fk_offr_group_dept` FOREIGN KEY `fk_offr_group_dept` (`department_id`)
    REFERENCES `timetable`.`department` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_offr_group_session` FOREIGN KEY `fk_offr_group_session` (`session_id`)
    REFERENCES `timetable`.`sessions` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`offr_group_offering`;
CREATE TABLE `timetable`.`offr_group_offering` (
  `offr_group_id` DECIMAL(20, 0) NOT NULL,
  `instr_offering_id` DECIMAL(20, 0) NOT NULL,
  PRIMARY KEY (`instr_offering_id`, `offr_group_id`),
  CONSTRAINT `fk_offr_group_instr_offr` FOREIGN KEY `fk_offr_group_instr_offr` (`instr_offering_id`)
    REFERENCES `timetable`.`instructional_offering` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_offr_group_offr_offr_grp` FOREIGN KEY `fk_offr_group_offr_offr_grp` (`offr_group_id`)
    REFERENCES `timetable`.`offr_group` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`position_code_to_type`;
CREATE TABLE `timetable`.`position_code_to_type` (
  `position_code` CHAR(5) BINARY NOT NULL,
  `pos_code_type` DECIMAL(20, 0) NULL,
  PRIMARY KEY (`position_code`),
  INDEX `idx_pos_code_to_type_type` (`pos_code_type`),
  CONSTRAINT `fk_pos_code_to_type_code_type` FOREIGN KEY `fk_pos_code_to_type_code_type` (`pos_code_type`)
    REFERENCES `timetable`.`position_type` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`position_type`;
CREATE TABLE `timetable`.`position_type` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `reference` VARCHAR(20) BINARY NULL,
  `label` VARCHAR(60) BINARY NULL,
  `sort_order` INT(4) NULL,
  PRIMARY KEY (`uniqueid`),
  UNIQUE INDEX `uk_position_type_label` (`label`(60)),
  UNIQUE INDEX `uk_position_type_ref` (`reference`(20))
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`pos_acad_area_major`;
CREATE TABLE `timetable`.`pos_acad_area_major` (
  `academic_area_id` DECIMAL(20, 0) NOT NULL,
  `major_id` DECIMAL(20, 0) NOT NULL,
  PRIMARY KEY (`major_id`, `academic_area_id`),
  CONSTRAINT `fk_pos_acad_area_major_area` FOREIGN KEY `fk_pos_acad_area_major_area` (`academic_area_id`)
    REFERENCES `timetable`.`academic_area` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_pos_acad_area_major_major` FOREIGN KEY `fk_pos_acad_area_major_major` (`major_id`)
    REFERENCES `timetable`.`pos_major` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`pos_acad_area_minor`;
CREATE TABLE `timetable`.`pos_acad_area_minor` (
  `academic_area_id` DECIMAL(20, 0) NOT NULL,
  `minor_id` DECIMAL(20, 0) NOT NULL,
  PRIMARY KEY (`minor_id`, `academic_area_id`),
  CONSTRAINT `fk_pos_acad_area_minor_area` FOREIGN KEY `fk_pos_acad_area_minor_area` (`academic_area_id`)
    REFERENCES `timetable`.`academic_area` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_pos_acad_area_minor_minor` FOREIGN KEY `fk_pos_acad_area_minor_minor` (`minor_id`)
    REFERENCES `timetable`.`pos_minor` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`pos_major`;
CREATE TABLE `timetable`.`pos_major` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `code` VARCHAR(10) BINARY NULL,
  `name` VARCHAR(50) BINARY NULL,
  `external_uid` VARCHAR(20) BINARY NULL,
  `session_id` DECIMAL(20, 0) NULL,
  PRIMARY KEY (`uniqueid`),
  CONSTRAINT `fk_pos_major_session` FOREIGN KEY `fk_pos_major_session` (`session_id`)
    REFERENCES `timetable`.`sessions` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`pos_minor`;
CREATE TABLE `timetable`.`pos_minor` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `code` VARCHAR(10) BINARY NULL,
  `name` VARCHAR(50) BINARY NULL,
  `external_uid` VARCHAR(40) BINARY NULL,
  `session_id` DECIMAL(20, 0) NULL,
  PRIMARY KEY (`uniqueid`),
  CONSTRAINT `fk_pos_minor_session` FOREIGN KEY `fk_pos_minor_session` (`session_id`)
    REFERENCES `timetable`.`sessions` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`pos_reservation`;
CREATE TABLE `timetable`.`pos_reservation` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `owner` DECIMAL(20, 0) NULL,
  `reservation_type` DECIMAL(20, 0) NULL,
  `acad_classification` DECIMAL(20, 0) NULL,
  `pos_major` DECIMAL(20, 0) NULL,
  `priority` INT(5) NULL,
  `reserved` BIGINT(10) NULL,
  `prior_enrollment` BIGINT(10) NULL,
  `projected_enrollment` BIGINT(10) NULL,
  `owner_class_id` VARCHAR(1) BINARY NULL,
  `requested` BIGINT(10) NULL,
  `last_modified_time` DATETIME NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_pos_resv_acad_class` (`acad_classification`),
  INDEX `idx_pos_resv_major` (`pos_major`),
  INDEX `idx_pos_resv_owner` (`owner`),
  INDEX `idx_pos_resv_owner_cls` (`owner_class_id`(1)),
  INDEX `idx_pos_resv_type` (`reservation_type`),
  CONSTRAINT `fk_pos_resv_acad_class` FOREIGN KEY `fk_pos_resv_acad_class` (`acad_classification`)
    REFERENCES `timetable`.`academic_classification` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_pos_resv_major` FOREIGN KEY `fk_pos_resv_major` (`pos_major`)
    REFERENCES `timetable`.`pos_major` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_pos_resv_type` FOREIGN KEY `fk_pos_resv_type` (`reservation_type`)
    REFERENCES `timetable`.`reservation_type` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`preference_level`;
CREATE TABLE `timetable`.`preference_level` (
  `pref_id` INT(2) NULL,
  `pref_prolog` VARCHAR(2) BINARY NULL,
  `pref_name` VARCHAR(20) BINARY NULL,
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  PRIMARY KEY (`uniqueid`),
  UNIQUE INDEX `uk_preference_level_pref_id` (`pref_id`)
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`related_course_info`;
CREATE TABLE `timetable`.`related_course_info` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `event_id` DECIMAL(20, 0) NOT NULL,
  `owner_id` DECIMAL(20, 0) NOT NULL,
  `owner_type` BIGINT(10) NOT NULL,
  `course_id` DECIMAL(20, 0) NOT NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_event_owner_event` (`event_id`),
  INDEX `idx_event_owner_owner` (`owner_id`, `owner_type`),
  CONSTRAINT `fk_event_owner_course` FOREIGN KEY `fk_event_owner_course` (`course_id`)
    REFERENCES `timetable`.`course_offering` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_event_owner_event` FOREIGN KEY `fk_event_owner_event` (`event_id`)
    REFERENCES `timetable`.`event` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`reservation_type`;
CREATE TABLE `timetable`.`reservation_type` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `reference` VARCHAR(20) BINARY NULL,
  `label` VARCHAR(60) BINARY NULL,
  PRIMARY KEY (`uniqueid`),
  UNIQUE INDEX `uk_reservation_type_label` (`label`(60)),
  UNIQUE INDEX `uk_reservation_type_ref` (`reference`(20))
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`roles`;
CREATE TABLE `timetable`.`roles` (
  `role_id` DECIMAL(20, 0) NOT NULL,
  `reference` VARCHAR(20) BINARY NULL,
  `abbv` VARCHAR(40) BINARY NULL,
  PRIMARY KEY (`role_id`),
  UNIQUE INDEX `uk_roles_abbv` (`abbv`(40)),
  UNIQUE INDEX `uk_roles_reference` (`reference`(20))
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`room`;
CREATE TABLE `timetable`.`room` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `external_uid` VARCHAR(40) BINARY NULL,
  `session_id` DECIMAL(20, 0) NULL,
  `building_id` DECIMAL(20, 0) NULL,
  `room_number` VARCHAR(10) BINARY NULL,
  `capacity` BIGINT(10) NULL,
  `coordinate_x` BIGINT(10) NULL,
  `coordinate_y` BIGINT(10) NULL,
  `scheduled_room_type` VARCHAR(20) BINARY NULL,
  `ignore_too_far` INT(1) NULL,
  `manager_ids` VARCHAR(200) BINARY NULL,
  `pattern` VARCHAR(350) BINARY NULL,
  `ignore_room_check` INT(1) NULL DEFAULT 0,
  `classification` VARCHAR(20) BINARY NULL,
  `display_name` VARCHAR(100) BINARY NULL,
  `exam_capacity` BIGINT(10) NULL DEFAULT 0,
  `permanent_id` DECIMAL(20, 0) NOT NULL,
  `exam_type` BIGINT(10) NULL DEFAULT 0,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_room_building` (`building_id`),
  UNIQUE INDEX `uk_room` (`session_id`, `building_id`, `room_number`(10)),
  CONSTRAINT `fk_room_building` FOREIGN KEY `fk_room_building` (`building_id`)
    REFERENCES `timetable`.`building` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_room_session` FOREIGN KEY `fk_room_session` (`session_id`)
    REFERENCES `timetable`.`sessions` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`room_dept`;
CREATE TABLE `timetable`.`room_dept` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `room_id` DECIMAL(20, 0) NULL,
  `department_id` DECIMAL(20, 0) NULL,
  `is_control` INT(1) NULL DEFAULT 0,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_room_dept_dept` (`department_id`),
  INDEX `idx_room_dept_room` (`room_id`),
  CONSTRAINT `fk_room_dept_dept` FOREIGN KEY `fk_room_dept_dept` (`department_id`)
    REFERENCES `timetable`.`department` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`room_feature`;
CREATE TABLE `timetable`.`room_feature` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `discriminator` VARCHAR(10) BINARY NULL,
  `label` VARCHAR(20) BINARY NULL,
  `sis_reference` VARCHAR(20) BINARY NULL,
  `sis_value` VARCHAR(20) BINARY NULL,
  `department_id` DECIMAL(20, 0) NULL,
  `abbv` VARCHAR(20) BINARY NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_room_feature_dept` (`department_id`),
  CONSTRAINT `fk_room_feature_dept` FOREIGN KEY `fk_room_feature_dept` (`department_id`)
    REFERENCES `timetable`.`department` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`room_feature_pref`;
CREATE TABLE `timetable`.`room_feature_pref` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `owner_id` DECIMAL(20, 0) NULL,
  `pref_level_id` DECIMAL(20, 0) NULL,
  `room_feature_id` DECIMAL(20, 0) NULL,
  `last_modified_time` DATETIME NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_room_feat_pref_level` (`pref_level_id`),
  INDEX `idx_room_feat_pref_owner` (`owner_id`),
  INDEX `idx_room_feat_pref_room_feat` (`room_feature_id`),
  CONSTRAINT `fk_room_feat_pref_level` FOREIGN KEY `fk_room_feat_pref_level` (`pref_level_id`)
    REFERENCES `timetable`.`preference_level` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_room_feat_pref_room_feat` FOREIGN KEY `fk_room_feat_pref_room_feat` (`room_feature_id`)
    REFERENCES `timetable`.`room_feature` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`room_group`;
CREATE TABLE `timetable`.`room_group` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `session_id` DECIMAL(20, 0) NULL,
  `name` VARCHAR(20) BINARY NULL,
  `description` VARCHAR(200) BINARY NULL,
  `global` INT(1) NULL,
  `default_group` INT(1) NULL,
  `department_id` DECIMAL(20, 0) NULL,
  `abbv` VARCHAR(20) BINARY NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_room_group_dept` (`department_id`),
  INDEX `idx_room_group_session` (`session_id`),
  CONSTRAINT `fk_room_group_dept` FOREIGN KEY `fk_room_group_dept` (`department_id`)
    REFERENCES `timetable`.`department` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_room_group_session` FOREIGN KEY `fk_room_group_session` (`session_id`)
    REFERENCES `timetable`.`sessions` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`room_group_pref`;
CREATE TABLE `timetable`.`room_group_pref` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `owner_id` DECIMAL(20, 0) NULL,
  `pref_level_id` DECIMAL(20, 0) NULL,
  `room_group_id` DECIMAL(20, 0) NULL,
  `last_modified_time` DATETIME NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_room_group_pref_level` (`pref_level_id`),
  INDEX `idx_room_group_pref_owner` (`owner_id`),
  INDEX `idx_room_group_pref_room_grp` (`room_group_id`),
  CONSTRAINT `fk_room_group_pref_level` FOREIGN KEY `fk_room_group_pref_level` (`pref_level_id`)
    REFERENCES `timetable`.`preference_level` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_room_group_pref_room_grp` FOREIGN KEY `fk_room_group_pref_room_grp` (`room_group_id`)
    REFERENCES `timetable`.`room_group` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`room_group_room`;
CREATE TABLE `timetable`.`room_group_room` (
  `room_group_id` DECIMAL(20, 0) NOT NULL,
  `room_id` DECIMAL(20, 0) NOT NULL,
  PRIMARY KEY (`room_group_id`, `room_id`),
  CONSTRAINT `fk_room_group_room_room_grp` FOREIGN KEY `fk_room_group_room_room_grp` (`room_group_id`)
    REFERENCES `timetable`.`room_group` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`room_join_room_feature`;
CREATE TABLE `timetable`.`room_join_room_feature` (
  `room_id` DECIMAL(20, 0) NULL,
  `feature_id` DECIMAL(20, 0) NULL,
  UNIQUE INDEX `uk_room_join_room_feat_rm_feat` (`room_id`, `feature_id`),
  CONSTRAINT `fk_room_join_room_feat_rm_feat` FOREIGN KEY `fk_room_join_room_feat_rm_feat` (`feature_id`)
    REFERENCES `timetable`.`room_feature` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`room_pref`;
CREATE TABLE `timetable`.`room_pref` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `owner_id` DECIMAL(20, 0) NULL,
  `pref_level_id` DECIMAL(20, 0) NULL,
  `room_id` DECIMAL(20, 0) NULL,
  `last_modified_time` DATETIME NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_room_pref_level` (`pref_level_id`),
  INDEX `idx_room_pref_owner` (`owner_id`),
  CONSTRAINT `fk_room_pref_level` FOREIGN KEY `fk_room_pref_level` (`pref_level_id`)
    REFERENCES `timetable`.`preference_level` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`scheduling_subpart`;
CREATE TABLE `timetable`.`scheduling_subpart` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `min_per_wk` INT(4) NULL,
  `parent` DECIMAL(20, 0) NULL,
  `config_id` DECIMAL(20, 0) NULL,
  `itype` INT(2) NULL,
  `date_pattern_id` DECIMAL(20, 0) NULL,
  `auto_time_spread` INT(1) NULL DEFAULT 1,
  `subpart_suffix` VARCHAR(5) BINARY NULL,
  `student_allow_overlap` INT(1) NULL DEFAULT 0,
  `last_modified_time` DATETIME NULL,
  `uid_rolled_fwd_from` DECIMAL(20, 0) NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_sched_subpart_config` (`config_id`),
  INDEX `idx_sched_subpart_date_pattern` (`date_pattern_id`),
  INDEX `idx_sched_subpart_itype` (`itype`),
  INDEX `idx_sched_subpart_parent` (`parent`),
  CONSTRAINT `fk_sched_subpart_config` FOREIGN KEY `fk_sched_subpart_config` (`config_id`)
    REFERENCES `timetable`.`instr_offering_config` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_sched_subpart_date_pattern` FOREIGN KEY `fk_sched_subpart_date_pattern` (`date_pattern_id`)
    REFERENCES `timetable`.`date_pattern` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_sched_subpart_itype` FOREIGN KEY `fk_sched_subpart_itype` (`itype`)
    REFERENCES `timetable`.`itype_desc` (`itype`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_sched_subpart_parent` FOREIGN KEY `fk_sched_subpart_parent` (`parent`)
    REFERENCES `timetable`.`scheduling_subpart` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`sectioning_info`;
CREATE TABLE `timetable`.`sectioning_info` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `class_id` DECIMAL(20, 0) NULL,
  `nbr_exp_students` DOUBLE NULL,
  `nbr_hold_students` DOUBLE NULL,
  PRIMARY KEY (`uniqueid`),
  CONSTRAINT `fk_sectioning_info_class` FOREIGN KEY `fk_sectioning_info_class` (`class_id`)
    REFERENCES `timetable`.`class_` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`sessions`;
CREATE TABLE `timetable`.`sessions` (
  `academic_initiative` VARCHAR(20) BINARY NULL,
  `session_begin_date_time` DATETIME NULL,
  `classes_end_date_time` DATETIME NULL,
  `session_end_date_time` DATETIME NULL,
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `holidays` VARCHAR(366) BINARY NULL,
  `def_datepatt_id` DECIMAL(20, 0) NULL,
  `status_type` DECIMAL(20, 0) NULL,
  `last_modified_time` DATETIME NULL,
  `academic_year` VARCHAR(4) BINARY NULL,
  `academic_term` VARCHAR(20) BINARY NULL,
  `exam_begin_date` DATETIME NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_sessions_date_pattern` (`def_datepatt_id`),
  INDEX `idx_sessions_status_type` (`status_type`),
  CONSTRAINT `fk_sessions_status_type` FOREIGN KEY `fk_sessions_status_type` (`status_type`)
    REFERENCES `timetable`.`dept_status_type` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_session_datepatt` FOREIGN KEY `fk_session_datepatt` (`def_datepatt_id`)
    REFERENCES `timetable`.`date_pattern` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`settings`;
CREATE TABLE `timetable`.`settings` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `name` VARCHAR(30) BINARY NULL,
  `default_value` VARCHAR(100) BINARY NULL,
  `allowed_values` VARCHAR(500) BINARY NULL,
  `description` VARCHAR(100) BINARY NULL,
  PRIMARY KEY (`uniqueid`)
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`solution`;
CREATE TABLE `timetable`.`solution` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `created` DATETIME NULL,
  `valid` INT(1) NULL,
  `commited` INT(1) NULL,
  `commit_date` DATETIME NULL,
  `note` VARCHAR(1000) BINARY NULL,
  `creator` VARCHAR(250) BINARY NULL,
  `owner_id` DECIMAL(20, 0) NULL,
  `last_modified_time` DATETIME NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_solution_owner` (`owner_id`),
  CONSTRAINT `fk_solution_owner` FOREIGN KEY `fk_solution_owner` (`owner_id`)
    REFERENCES `timetable`.`solver_group` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`solver_group`;
CREATE TABLE `timetable`.`solver_group` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `name` VARCHAR(50) BINARY NULL,
  `abbv` VARCHAR(50) BINARY NULL,
  `session_id` DECIMAL(20, 0) NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_solver_group_session` (`session_id`),
  CONSTRAINT `fk_solver_group_session` FOREIGN KEY `fk_solver_group_session` (`session_id`)
    REFERENCES `timetable`.`sessions` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`solver_gr_to_tt_mgr`;
CREATE TABLE `timetable`.`solver_gr_to_tt_mgr` (
  `solver_group_id` DECIMAL(20, 0) NOT NULL,
  `timetable_mgr_id` DECIMAL(20, 0) NOT NULL,
  PRIMARY KEY (`timetable_mgr_id`, `solver_group_id`),
  CONSTRAINT `fk_solver_gr_to_tt_mgr_solvgrp` FOREIGN KEY `fk_solver_gr_to_tt_mgr_solvgrp` (`solver_group_id`)
    REFERENCES `timetable`.`solver_group` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_solver_gr_to_tt_mgr_tt_mgr` FOREIGN KEY `fk_solver_gr_to_tt_mgr_tt_mgr` (`timetable_mgr_id`)
    REFERENCES `timetable`.`timetable_manager` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`solver_info`;
CREATE TABLE `timetable`.`solver_info` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `type` BIGINT(10) NULL,
  `value` LONGBLOB NULL,
  `opt` VARCHAR(250) BINARY NULL,
  `solver_info_def_id` DECIMAL(20, 0) NULL,
  `solution_id` DECIMAL(20, 0) NULL,
  `assignment_id` DECIMAL(20, 0) NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_solver_info` (`assignment_id`),
  INDEX `idx_solver_info_solution` (`solution_id`, `solver_info_def_id`),
  CONSTRAINT `fk_solver_info_assignment` FOREIGN KEY `fk_solver_info_assignment` (`assignment_id`)
    REFERENCES `timetable`.`assignment` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_solver_info_def` FOREIGN KEY `fk_solver_info_def` (`solver_info_def_id`)
    REFERENCES `timetable`.`solver_info_def` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_solver_info_solution` FOREIGN KEY `fk_solver_info_solution` (`solution_id`)
    REFERENCES `timetable`.`solution` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`solver_info_def`;
CREATE TABLE `timetable`.`solver_info_def` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `name` VARCHAR(100) BINARY NULL,
  `description` VARCHAR(1000) BINARY NULL,
  `implementation` VARCHAR(250) BINARY NULL,
  PRIMARY KEY (`uniqueid`)
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`solver_parameter`;
CREATE TABLE `timetable`.`solver_parameter` (
  `uniqueid` DECIMAL(20, 0) NULL,
  `value` VARCHAR(2048) BINARY NULL,
  `solver_param_def_id` DECIMAL(20, 0) NULL,
  `solution_id` DECIMAL(20, 0) NULL,
  `solver_predef_setting_id` DECIMAL(20, 0) NULL,
  INDEX `idx_solver_param_def` (`solver_param_def_id`),
  INDEX `idx_solver_param_predef` (`solver_predef_setting_id`),
  INDEX `idx_solver_param_solution` (`solution_id`),
  CONSTRAINT `fk_solver_param_def` FOREIGN KEY `fk_solver_param_def` (`solver_param_def_id`)
    REFERENCES `timetable`.`solver_parameter_def` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_solver_param_predef_stg` FOREIGN KEY `fk_solver_param_predef_stg` (`solver_predef_setting_id`)
    REFERENCES `timetable`.`solver_predef_setting` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_solver_param_solution` FOREIGN KEY `fk_solver_param_solution` (`solution_id`)
    REFERENCES `timetable`.`solution` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`solver_parameter_def`;
CREATE TABLE `timetable`.`solver_parameter_def` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `name` VARCHAR(100) BINARY NULL,
  `default_value` VARCHAR(2048) BINARY NULL,
  `description` VARCHAR(1000) BINARY NULL,
  `type` VARCHAR(250) BINARY NULL,
  `ord` BIGINT(10) NULL,
  `visible` INT(1) NULL,
  `solver_param_group_id` DECIMAL(20, 0) NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_solv_param_def_gr` (`solver_param_group_id`),
  CONSTRAINT `fk_solv_param_def_solv_par_grp` FOREIGN KEY `fk_solv_param_def_solv_par_grp` (`solver_param_group_id`)
    REFERENCES `timetable`.`solver_parameter_group` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`solver_parameter_group`;
CREATE TABLE `timetable`.`solver_parameter_group` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `name` VARCHAR(100) BINARY NULL,
  `description` VARCHAR(1000) BINARY NULL,
  `condition` VARCHAR(250) BINARY NULL,
  `ord` BIGINT(10) NULL,
  `param_type` BIGINT(10) NULL DEFAULT 0,
  PRIMARY KEY (`uniqueid`)
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`solver_predef_setting`;
CREATE TABLE `timetable`.`solver_predef_setting` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `name` VARCHAR(100) BINARY NULL,
  `description` VARCHAR(1000) BINARY NULL,
  `appearance` BIGINT(10) NULL,
  PRIMARY KEY (`uniqueid`)
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`staff`;
CREATE TABLE `timetable`.`staff` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `external_uid` VARCHAR(40) BINARY NULL,
  `fname` VARCHAR(50) BINARY NULL,
  `mname` VARCHAR(50) BINARY NULL,
  `lname` VARCHAR(100) BINARY NULL,
  `pos_code` VARCHAR(20) BINARY NULL,
  `dept` VARCHAR(50) BINARY NULL,
  `email` VARCHAR(200) BINARY NULL,
  PRIMARY KEY (`uniqueid`)
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`standard_event_note`;
CREATE TABLE `timetable`.`standard_event_note` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `reference` VARCHAR(20) BINARY NULL,
  `note` VARCHAR(1000) BINARY NULL,
  PRIMARY KEY (`uniqueid`)
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`student`;
CREATE TABLE `timetable`.`student` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `external_uid` VARCHAR(40) BINARY NULL,
  `first_name` VARCHAR(50) BINARY NULL,
  `middle_name` VARCHAR(50) BINARY NULL,
  `last_name` VARCHAR(100) BINARY NULL,
  `email` VARCHAR(200) BINARY NULL,
  `free_time_cat` BIGINT(10) NULL DEFAULT 0,
  `schedule_preference` BIGINT(10) NULL DEFAULT 0,
  `status_type_id` DECIMAL(20, 0) NULL,
  `status_change_date` DATETIME NULL,
  `session_id` DECIMAL(20, 0) NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_student_session` (`session_id`),
  CONSTRAINT `fk_student_session` FOREIGN KEY `fk_student_session` (`session_id`)
    REFERENCES `timetable`.`sessions` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_student_status_student` FOREIGN KEY `fk_student_status_student` (`status_type_id`)
    REFERENCES `timetable`.`student_status_type` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`student_acad_area`;
CREATE TABLE `timetable`.`student_acad_area` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `student_id` DECIMAL(20, 0) NULL,
  `acad_clasf_id` DECIMAL(20, 0) NULL,
  `acad_area_id` DECIMAL(20, 0) NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_student_acad_area` (`student_id`, `acad_area_id`, `acad_clasf_id`),
  UNIQUE INDEX `uk_student_acad_area` (`student_id`, `acad_clasf_id`, `acad_area_id`),
  CONSTRAINT `fk_student_acad_area_area` FOREIGN KEY `fk_student_acad_area_area` (`acad_area_id`)
    REFERENCES `timetable`.`academic_area` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_student_acad_area_clasf` FOREIGN KEY `fk_student_acad_area_clasf` (`acad_clasf_id`)
    REFERENCES `timetable`.`academic_classification` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_student_acad_area_student` FOREIGN KEY `fk_student_acad_area_student` (`student_id`)
    REFERENCES `timetable`.`student` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`student_accomodation`;
CREATE TABLE `timetable`.`student_accomodation` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `name` VARCHAR(50) BINARY NULL,
  `abbreviation` VARCHAR(20) BINARY NULL,
  `external_uid` VARCHAR(40) BINARY NULL,
  `session_id` DECIMAL(20, 0) NULL,
  PRIMARY KEY (`uniqueid`),
  CONSTRAINT `fk_student_accom_session` FOREIGN KEY `fk_student_accom_session` (`session_id`)
    REFERENCES `timetable`.`sessions` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`student_class_enrl`;
CREATE TABLE `timetable`.`student_class_enrl` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `student_id` DECIMAL(20, 0) NULL,
  `course_request_id` DECIMAL(20, 0) NULL,
  `class_id` DECIMAL(20, 0) NULL,
  `timestamp` DATETIME NULL,
  `course_offering_id` DECIMAL(20, 0) NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_student_class_enrl_class` (`class_id`),
  INDEX `idx_student_class_enrl_req` (`course_request_id`),
  INDEX `idx_student_class_enrl_student` (`student_id`),
  CONSTRAINT `fk_student_class_enrl_class` FOREIGN KEY `fk_student_class_enrl_class` (`class_id`)
    REFERENCES `timetable`.`class_` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_student_class_enrl_course` FOREIGN KEY `fk_student_class_enrl_course` (`course_offering_id`)
    REFERENCES `timetable`.`course_offering` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_student_class_enrl_request` FOREIGN KEY `fk_student_class_enrl_request` (`course_request_id`)
    REFERENCES `timetable`.`course_request` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_student_class_enrl_student` FOREIGN KEY `fk_student_class_enrl_student` (`student_id`)
    REFERENCES `timetable`.`student` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`student_enrl`;
CREATE TABLE `timetable`.`student_enrl` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `student_id` DECIMAL(20, 0) NULL,
  `solution_id` DECIMAL(20, 0) NULL,
  `class_id` DECIMAL(20, 0) NULL,
  `last_modified_time` DATETIME NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_student_enrl` (`solution_id`),
  INDEX `idx_student_enrl_class` (`class_id`),
  CONSTRAINT `fk_student_enrl_class` FOREIGN KEY `fk_student_enrl_class` (`class_id`)
    REFERENCES `timetable`.`class_` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_student_enrl_solution` FOREIGN KEY `fk_student_enrl_solution` (`solution_id`)
    REFERENCES `timetable`.`solution` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`student_enrl_msg`;
CREATE TABLE `timetable`.`student_enrl_msg` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `message` VARCHAR(255) BINARY NULL,
  `msg_level` BIGINT(10) NULL DEFAULT 0,
  `type` BIGINT(10) NULL DEFAULT 0,
  `timestamp` DATETIME NULL,
  `course_demand_id` DECIMAL(20, 0) NULL,
  `ord` BIGINT(10) NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_student_enrl_msg_dem` (`course_demand_id`),
  CONSTRAINT `fk_student_enrl_msg_demand` FOREIGN KEY `fk_student_enrl_msg_demand` (`course_demand_id`)
    REFERENCES `timetable`.`course_demand` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`student_group`;
CREATE TABLE `timetable`.`student_group` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `session_id` DECIMAL(20, 0) NULL,
  `group_abbreviation` VARCHAR(30) BINARY NULL,
  `group_name` VARCHAR(90) BINARY NULL,
  `external_uid` VARCHAR(40) BINARY NULL,
  PRIMARY KEY (`uniqueid`),
  UNIQUE INDEX `uk_student_group_session_sis` (`session_id`, `group_abbreviation`(30)),
  CONSTRAINT `fk_student_group_session` FOREIGN KEY `fk_student_group_session` (`session_id`)
    REFERENCES `timetable`.`sessions` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`student_group_reservation`;
CREATE TABLE `timetable`.`student_group_reservation` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `owner` DECIMAL(20, 0) NULL,
  `reservation_type` DECIMAL(20, 0) NULL,
  `student_group` DECIMAL(20, 0) NULL,
  `priority` INT(5) NULL,
  `reserved` BIGINT(10) NULL,
  `prior_enrollment` BIGINT(10) NULL,
  `projected_enrollment` BIGINT(10) NULL,
  `owner_class_id` VARCHAR(1) BINARY NULL,
  `requested` BIGINT(10) NULL,
  `last_modified_time` DATETIME NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_stu_grp_resv_owner` (`owner`),
  INDEX `idx_stu_grp_resv_owner_cls` (`owner_class_id`(1)),
  INDEX `idx_stu_grp_resv_student_group` (`student_group`),
  INDEX `idx_stu_grp_resv_type` (`reservation_type`),
  CONSTRAINT `fk_stu_grp_resv_reserv_type` FOREIGN KEY `fk_stu_grp_resv_reserv_type` (`reservation_type`)
    REFERENCES `timetable`.`reservation_type` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_stu_grp_resv_stu_grp` FOREIGN KEY `fk_stu_grp_resv_stu_grp` (`student_group`)
    REFERENCES `timetable`.`student_group` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`student_major`;
CREATE TABLE `timetable`.`student_major` (
  `student_id` DECIMAL(20, 0) NOT NULL,
  `major_id` DECIMAL(20, 0) NOT NULL,
  PRIMARY KEY (`student_id`, `major_id`),
  CONSTRAINT `fk_student_major_major` FOREIGN KEY `fk_student_major_major` (`major_id`)
    REFERENCES `timetable`.`pos_major` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_student_major_student` FOREIGN KEY `fk_student_major_student` (`student_id`)
    REFERENCES `timetable`.`student` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`student_minor`;
CREATE TABLE `timetable`.`student_minor` (
  `student_id` DECIMAL(20, 0) NOT NULL,
  `minor_id` DECIMAL(20, 0) NOT NULL,
  PRIMARY KEY (`student_id`, `minor_id`),
  CONSTRAINT `fk_student_minor_minor` FOREIGN KEY `fk_student_minor_minor` (`minor_id`)
    REFERENCES `timetable`.`pos_minor` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_student_minor_student` FOREIGN KEY `fk_student_minor_student` (`student_id`)
    REFERENCES `timetable`.`student` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`student_sect_hist`;
CREATE TABLE `timetable`.`student_sect_hist` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `student_id` DECIMAL(20, 0) NULL,
  `data` LONGBLOB NULL,
  `type` BIGINT(10) NULL,
  `timestamp` DATETIME NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_student_sect_hist_student` (`student_id`),
  CONSTRAINT `fk_student_sect_hist_student` FOREIGN KEY `fk_student_sect_hist_student` (`student_id`)
    REFERENCES `timetable`.`student` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`student_status_type`;
CREATE TABLE `timetable`.`student_status_type` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `abbreviation` VARCHAR(20) BINARY NULL,
  `name` VARCHAR(50) BINARY NULL,
  PRIMARY KEY (`uniqueid`)
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`student_to_acomodation`;
CREATE TABLE `timetable`.`student_to_acomodation` (
  `student_id` DECIMAL(20, 0) NOT NULL,
  `accomodation_id` DECIMAL(20, 0) NOT NULL,
  PRIMARY KEY (`student_id`, `accomodation_id`),
  CONSTRAINT `fk_student_acomodation_accom` FOREIGN KEY `fk_student_acomodation_accom` (`student_id`)
    REFERENCES `timetable`.`student` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_student_acomodation_student` FOREIGN KEY `fk_student_acomodation_student` (`accomodation_id`)
    REFERENCES `timetable`.`student_accomodation` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`student_to_group`;
CREATE TABLE `timetable`.`student_to_group` (
  `student_id` DECIMAL(20, 0) NOT NULL,
  `group_id` DECIMAL(20, 0) NOT NULL,
  PRIMARY KEY (`student_id`, `group_id`),
  CONSTRAINT `fk_student_group_group` FOREIGN KEY `fk_student_group_group` (`student_id`)
    REFERENCES `timetable`.`student` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_student_group_student` FOREIGN KEY `fk_student_group_student` (`group_id`)
    REFERENCES `timetable`.`student_group` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`subject_area`;
CREATE TABLE `timetable`.`subject_area` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `session_id` DECIMAL(20, 0) NULL,
  `subject_area_abbreviation` VARCHAR(10) BINARY NULL,
  `short_title` VARCHAR(50) BINARY NULL,
  `long_title` VARCHAR(100) BINARY NULL,
  `schedule_book_only` VARCHAR(1) BINARY NULL,
  `pseudo_subject_area` VARCHAR(1) BINARY NULL,
  `department_uniqueid` DECIMAL(20, 0) NULL,
  `external_uid` VARCHAR(40) BINARY NULL,
  `last_modified_time` DATETIME NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_subject_area_dept` (`department_uniqueid`),
  UNIQUE INDEX `uk_subject_area` (`session_id`, `subject_area_abbreviation`(10)),
  CONSTRAINT `fk_subject_area_dept` FOREIGN KEY `fk_subject_area_dept` (`department_uniqueid`)
    REFERENCES `timetable`.`department` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`timetable_manager`;
CREATE TABLE `timetable`.`timetable_manager` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `external_uid` VARCHAR(40) BINARY NULL,
  `first_name` VARCHAR(20) BINARY NULL,
  `middle_name` VARCHAR(20) BINARY NULL,
  `last_name` VARCHAR(30) BINARY NULL,
  `email_address` VARCHAR(135) BINARY NULL,
  `last_modified_time` DATETIME NULL,
  PRIMARY KEY (`uniqueid`),
  UNIQUE INDEX `uk_timetable_manager_puid` (`external_uid`(40))
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`time_pattern`;
CREATE TABLE `timetable`.`time_pattern` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `name` VARCHAR(50) BINARY NULL,
  `mins_pmt` BIGINT(10) NULL,
  `slots_pmt` BIGINT(10) NULL,
  `nr_mtgs` BIGINT(10) NULL,
  `visible` INT(1) NULL,
  `type` BIGINT(10) NULL,
  `break_time` INT(3) NULL,
  `session_id` DECIMAL(20, 0) NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_time_pattern_session` (`session_id`),
  CONSTRAINT `fk_time_pattern_session` FOREIGN KEY `fk_time_pattern_session` (`session_id`)
    REFERENCES `timetable`.`sessions` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`time_pattern_days`;
CREATE TABLE `timetable`.`time_pattern_days` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `day_code` BIGINT(10) NULL,
  `time_pattern_id` DECIMAL(20, 0) NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_time_pattern_days` (`time_pattern_id`),
  CONSTRAINT `fk_time_pattern_days_time_patt` FOREIGN KEY `fk_time_pattern_days_time_patt` (`time_pattern_id`)
    REFERENCES `timetable`.`time_pattern` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`time_pattern_dept`;
CREATE TABLE `timetable`.`time_pattern_dept` (
  `dept_id` DECIMAL(20, 0) NOT NULL,
  `pattern_id` DECIMAL(20, 0) NOT NULL,
  PRIMARY KEY (`pattern_id`, `dept_id`),
  CONSTRAINT `fk_time_pattern_dept_dept` FOREIGN KEY `fk_time_pattern_dept_dept` (`dept_id`)
    REFERENCES `timetable`.`department` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_time_pattern_dept_pattern` FOREIGN KEY `fk_time_pattern_dept_pattern` (`pattern_id`)
    REFERENCES `timetable`.`time_pattern` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`time_pattern_time`;
CREATE TABLE `timetable`.`time_pattern_time` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `start_slot` BIGINT(10) NULL,
  `time_pattern_id` DECIMAL(20, 0) NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_time_pattern_time` (`time_pattern_id`),
  CONSTRAINT `fk_time_pattern_time` FOREIGN KEY `fk_time_pattern_time` (`time_pattern_id`)
    REFERENCES `timetable`.`time_pattern` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`time_pref`;
CREATE TABLE `timetable`.`time_pref` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `owner_id` DECIMAL(20, 0) NULL,
  `pref_level_id` DECIMAL(20, 0) NULL,
  `preference` VARCHAR(2048) BINARY NULL,
  `time_pattern_id` DECIMAL(20, 0) NULL,
  `last_modified_time` DATETIME NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_time_pref_owner` (`owner_id`),
  INDEX `idx_time_pref_pref_level` (`pref_level_id`),
  INDEX `idx_time_pref_time_ptrn` (`time_pattern_id`),
  CONSTRAINT `fk_time_pref_pref_level` FOREIGN KEY `fk_time_pref_pref_level` (`pref_level_id`)
    REFERENCES `timetable`.`preference_level` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_time_pref_time_ptrn` FOREIGN KEY `fk_time_pref_time_ptrn` (`time_pattern_id`)
    REFERENCES `timetable`.`time_pattern` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`tmtbl_mgr_to_roles`;
CREATE TABLE `timetable`.`tmtbl_mgr_to_roles` (
  `manager_id` DECIMAL(20, 0) NULL,
  `role_id` DECIMAL(20, 0) NULL,
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `is_primary` INT(1) NULL,
  PRIMARY KEY (`uniqueid`),
  UNIQUE INDEX `uk_tmtbl_mgr_to_roles_mgr_role` (`manager_id`, `role_id`),
  CONSTRAINT `fk_tmtbl_mgr_to_roles_manager` FOREIGN KEY `fk_tmtbl_mgr_to_roles_manager` (`manager_id`)
    REFERENCES `timetable`.`timetable_manager` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_tmtbl_mgr_to_roles_role` FOREIGN KEY `fk_tmtbl_mgr_to_roles_role` (`role_id`)
    REFERENCES `timetable`.`roles` (`role_id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`users`;
CREATE TABLE `timetable`.`users` (
  `username` VARCHAR(15) BINARY NOT NULL,
  `password` VARCHAR(25) BINARY NULL,
  `external_uid` VARCHAR(40) BINARY NULL,
  PRIMARY KEY (`username`)
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`user_data`;
CREATE TABLE `timetable`.`user_data` (
  `external_uid` VARCHAR(12) BINARY NOT NULL,
  `name` VARCHAR(100) BINARY NOT NULL,
  `value` VARCHAR(2048) BINARY NULL,
  PRIMARY KEY (`name`, `external_uid`)
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`waitlist`;
CREATE TABLE `timetable`.`waitlist` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `student_id` DECIMAL(20, 0) NULL,
  `course_offering_id` DECIMAL(20, 0) NULL,
  `type` BIGINT(10) NULL DEFAULT 0,
  `timestamp` DATETIME NULL,
  PRIMARY KEY (`uniqueid`),
  INDEX `idx_waitlist_offering` (`course_offering_id`),
  INDEX `idx_waitlist_student` (`student_id`),
  CONSTRAINT `fk_waitlist_course_offering` FOREIGN KEY `fk_waitlist_course_offering` (`course_offering_id`)
    REFERENCES `timetable`.`course_offering` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_waitlist_student` FOREIGN KEY `fk_waitlist_student` (`student_id`)
    REFERENCES `timetable`.`student` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`xconflict`;
CREATE TABLE `timetable`.`xconflict` (
  `uniqueid` DECIMAL(20, 0) NOT NULL,
  `conflict_type` BIGINT(10) NOT NULL,
  `distance` DOUBLE NULL,
  PRIMARY KEY (`uniqueid`)
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`xconflict_exam`;
CREATE TABLE `timetable`.`xconflict_exam` (
  `conflict_id` DECIMAL(20, 0) NOT NULL,
  `exam_id` DECIMAL(20, 0) NOT NULL,
  PRIMARY KEY (`exam_id`, `conflict_id`),
  INDEX `idx_xconflict_exam` (`exam_id`),
  CONSTRAINT `fk_xconflict_ex_conf` FOREIGN KEY `fk_xconflict_ex_conf` (`conflict_id`)
    REFERENCES `timetable`.`xconflict` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_xconflict_ex_exam` FOREIGN KEY `fk_xconflict_ex_exam` (`exam_id`)
    REFERENCES `timetable`.`exam` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`xconflict_instructor`;
CREATE TABLE `timetable`.`xconflict_instructor` (
  `conflict_id` DECIMAL(20, 0) NOT NULL,
  `instructor_id` DECIMAL(20, 0) NOT NULL,
  PRIMARY KEY (`instructor_id`, `conflict_id`),
  CONSTRAINT `fk_xconflict_in_conf` FOREIGN KEY `fk_xconflict_in_conf` (`conflict_id`)
    REFERENCES `timetable`.`xconflict` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_xconflict_in_instructor` FOREIGN KEY `fk_xconflict_in_instructor` (`instructor_id`)
    REFERENCES `timetable`.`departmental_instructor` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

DROP TABLE IF EXISTS `timetable`.`xconflict_student`;
CREATE TABLE `timetable`.`xconflict_student` (
  `conflict_id` DECIMAL(20, 0) NOT NULL,
  `student_id` DECIMAL(20, 0) NOT NULL,
  PRIMARY KEY (`student_id`, `conflict_id`),
  CONSTRAINT `fk_xconflict_st_conf` FOREIGN KEY `fk_xconflict_st_conf` (`conflict_id`)
    REFERENCES `timetable`.`xconflict` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_xconflict_st_student` FOREIGN KEY `fk_xconflict_st_student` (`student_id`)
    REFERENCES `timetable`.`student` (`uniqueid`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;



SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------------------------------------------------
-- EOF

DROP TABLE IF EXISTS `timetable`.`hibernate_unique_key`;
CREATE TABLE  `timetable`.`hibernate_unique_key` (
  `next_hi` decimal(20,0) default 10000000
)
ENGINE = INNODB;

delete from `timetable`.`hibernate_unique_key`;

insert into `timetable`.`hibernate_unique_key` values (10000000);
