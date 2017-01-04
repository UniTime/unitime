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

create sequence point_in_time_seq
	minvalue 1
	maxvalue 99999999999999999999
	start with 1
	increment by 1
	cache 20;

create table point_in_time_data (
	uniqueid number(20,0) primary key not null,
	session_id number(20,0) not null,
	timestamp timestamp not null,
	name varchar2(100),
	note varchar2(1000),
	saved_successfully number(1, 0) not null
) ;

alter table point_in_time_data add constraint fk_pitd_to_s foreign key (session_id)
	references sessions (uniqueid) on delete cascade;

create table pit_instr_offering (
	uniqueid number(20,0) primary key not null,
	point_in_time_data_id number(20,0) not null,
	instr_offering_id number(20,0),
	instr_offering_perm_id number(10, 0),
	demand number(4, 0),
	offr_limit number(10, 0),
	uid_rolled_fwd_from number(20,0),
	external_uid varchar2(40)
) ;


alter table pit_instr_offering add constraint fk_pit_io_to_pitd foreign key (point_in_time_data_id)
	references point_in_time_data (uniqueid) on delete cascade;

alter table pit_instr_offering add constraint fk_pit_io_to_io foreign key (instr_offering_id)
	references instructional_offering (uniqueid) on delete set null;

create table pit_course_offering (
	uniqueid number(20,0) primary key not null,
	course_offering_id number(20,0),
	subject_area_id number(20,0),
	pit_instr_offr_id number(20,0) not null,
	course_nbr varchar2(40),
	is_control number(1, 0) default null,
    perm_id varchar2(20) default null,
    proj_demand number(10, 0) default null,
    title varchar2(200) default null,
	nbr_expected_stdents number(10, 0) default '0',
	external_uid varchar2(40),
	uid_rolled_fwd_from number(20,0),
	lastlike_demand number(10, 0) default '0',
	course_type_id number(20,0) default null
) ;

alter table pit_course_offering add constraint fk_pit_co_to_pit_io foreign key (pit_instr_offr_id)
	references pit_instr_offering (uniqueid) on delete cascade;

alter table pit_course_offering add constraint fk_pit_co_to_sa foreign key (subject_area_id)
	references subject_area (uniqueid) on delete cascade;

alter table pit_course_offering add constraint fk_pit_co_to_co foreign key (course_offering_id)
	references course_offering (uniqueid) on delete set null;

alter table pit_course_offering add constraint fk_pit_co_to_ct foreign key (course_type_id)
 references course_type (uniqueid) on delete set null;

create table pit_instr_offer_config (
	uniqueid number(20,0) primary key not null,
	instr_offering_config_id number(20,0),
	pit_instr_offr_id number(20,0) not null,
	unlimited_enrollment number(1, 0) default null,
    name varchar2(10) default null,
    uid_rolled_fwd_from number(20,0) default null,
	duration_type_id number(20,0) default null,
    instr_method_id number(20,0) default null
) ;

alter table pit_instr_offer_config add constraint fk_pit_ioc_to_pit_io foreign key (pit_instr_offr_id)
	references pit_instr_offering (uniqueid) on delete cascade;

alter table pit_instr_offer_config add constraint fk_pit_ioc_to_ioc foreign key (instr_offering_config_id)
	references instr_offering_config (uniqueid) on delete set null;

alter table pit_instr_offer_config add constraint fk_pit_ioc_to_dt foreign key (duration_type_id)
 references duration_type (uniqueid) on delete set null;

alter table pit_instr_offer_config add constraint fk_pit_ioc_to_im foreign key (instr_method_id)
 references instructional_method (uniqueid) on delete set null;

create table pit_sched_subpart (
	uniqueid number(20,0) primary key not null,
	scheduling_subpart_id number(20,0),
	pit_parent_id number(20,0) default null,
	pit_config_id number(20,0) not null,
	min_per_wk number(4, 0) default null,
    itype number(2, 0) default null,
    subpart_suffix varchar2(5) default null,
    credit_type number(20,0) default null,
    credit_unit_type number(20,0) default null,
    credit float default null,
    student_allow_overlap number(1, 0) default '0',
    uid_rolled_fwd_from number(20,0) default null
) ;

alter table pit_sched_subpart add constraint fk_pit_ss_to_pit_ioc foreign key (pit_config_id)
	references pit_instr_offer_config (uniqueid) on delete cascade;

alter table pit_sched_subpart add constraint fk_pit_ss_to_ss foreign key (scheduling_subpart_id)
	references scheduling_subpart (uniqueid) on delete set null;

alter table pit_sched_subpart add constraint fk_pit_ss_to_parent_pit_ss foreign key (pit_parent_id)
 references pit_sched_subpart (uniqueid) on delete cascade;

alter table pit_sched_subpart add constraint fk_pit_ss_to_itype foreign key (itype)
 references itype_desc (itype) on delete cascade;

create table pit_class (
	uniqueid number(20,0) primary key not null,
	class_id number(20,0),
	pit_subpart_id number(20,0) not null,
	pit_parent_id number(20,0) default null,
	class_limit number(10, 0),
	nbr_rooms number(4, 0) default null,
    date_pattern_id number(20,0) default null,
    time_pattern_id number(20,0) default null,
    managing_dept number(20,0) default null,
    class_suffix varchar2(10) default null,
    enabled_for_stu_sched number(1, 0) default '1',
    section_number number(5, 0) default null,
    uid_rolled_fwd_from number(20,0) default null,
    external_uid varchar2(40) default null
) ;

alter table pit_class add constraint fk_pit_c_to_pit_ss foreign key (pit_subpart_id)
	references pit_sched_subpart (uniqueid) on delete cascade;

alter table pit_class add constraint fk_pit_c_to_c foreign key (class_id)
	references class_ (uniqueid) on delete set null;

alter table pit_class add constraint fk_pit_c_to_parent_pit_c foreign key (pit_parent_id)
 references pit_class (uniqueid) on delete cascade;

alter table pit_class add constraint fk_pit_c_to_dp foreign key (date_pattern_id)
 references date_pattern (uniqueid) on delete cascade;

alter table pit_class add constraint fk_pit_c_to_tp foreign key (time_pattern_id)
 references time_pattern (uniqueid) on delete cascade;

alter table pit_class add constraint fk_pit_c_to_d foreign key (managing_dept)
 references department (uniqueid) on delete cascade;

create table pit_student (
	uniqueid number(20,0) primary key not null,
	point_in_time_data_id number(20,0) not null,
	student_id number(20,0),
	external_uid varchar2(40) default null,
    first_name varchar2(100) default null,
    middle_name varchar2(100) default null,
    last_name varchar2(100) default null,
    email varchar2(200) default null
) ;


alter table pit_student add constraint fk_pit_stu_to_pitd foreign key (point_in_time_data_id)
	references point_in_time_data (uniqueid) on delete cascade;

alter table pit_student add constraint fk_pit_stu_to_stu foreign key (student_id)
	references student (uniqueid) on delete set null;

create table pit_student_class_enrl (
	uniqueid number(20,0) primary key not null,
	pit_student_id number(20,0) not null,
	pit_class_id number(20,0) not null,
	pit_course_offering_id number(20,0) not null,
	timestamp timestamp,
    changed_by varchar2(40) default null
) ;

alter table pit_student_class_enrl add constraint fk_pit_sce_to_pit_stu foreign key (pit_student_id)
	references pit_student (uniqueid) on delete cascade;

alter table pit_student_class_enrl add constraint fk_pit_sce_to_pit_c foreign key (pit_class_id)
	references pit_class (uniqueid) on delete cascade;

alter table pit_student_class_enrl add constraint fk_pit_sce_to_pit_co foreign key (pit_course_offering_id)
	references pit_course_offering (uniqueid) on delete cascade;

create table pit_stu_aa_major_clasf (
	uniqueid number(20,0) primary key not null,
	pit_student_id number(20,0) not null,
	acad_clasf_id number(20,0) not null,
	acad_area_id number(20,0) not null,
	major_id number(20,0) not null
	) ;

alter table pit_stu_aa_major_clasf add constraint fk_pit_stuamc_to_pit_stu foreign key (pit_student_id)
	references pit_student (uniqueid) on delete cascade;

alter table pit_stu_aa_major_clasf add constraint fk_pit_stuamc_to_ac foreign key (acad_clasf_id)
	references academic_classification (uniqueid) on delete cascade;

alter table pit_stu_aa_major_clasf add constraint fk_pit_stuamc_to_aa foreign key (acad_area_id)
	references academic_area (uniqueid) on delete cascade;

alter table pit_stu_aa_major_clasf add constraint fk_pit_stuamc_to_pm foreign key (major_id)
	references pos_major (uniqueid) on delete cascade;

create table pit_stu_aa_minor_clasf (
	uniqueid number(20,0) primary key not null,
	pit_student_id number(20,0) not null,
	acad_clasf_id number(20,0) not null,
	acad_area_id number(20,0) not null,
	minor_id number(20,0) not null
	) ;

alter table pit_stu_aa_minor_clasf add constraint fk_pit_stuamnc_to_pit_stu foreign key (pit_student_id)
	references pit_student (uniqueid) on delete cascade;

alter table pit_stu_aa_minor_clasf add constraint fk_pit_stuamnc_to_ac foreign key (acad_clasf_id)
	references academic_classification (uniqueid) on delete cascade;

alter table pit_stu_aa_minor_clasf add constraint fk_pit_stuamnc_to_aa foreign key (acad_area_id)
	references academic_area (uniqueid) on delete cascade;

alter table pit_stu_aa_minor_clasf add constraint fk_pit_stuamnc_to_pmn foreign key (minor_id)
	references pos_minor (uniqueid) on delete cascade;

create table pit_dept_instructor (
	uniqueid number(20,0) primary key not null,
	point_in_time_data_id number(20,0) not null,
	dept_instructor_id number(20,0),
    external_uid varchar2(40) default null,
    career_acct varchar2(20) default null,
    lname varchar2(100) default null,
    fname varchar2(100) default null,
    mname varchar2(100) default null,
    pos_code_type number(20,0) default null,
    department_id number(20,0) default null,
    email varchar2(200) default null
	) ;

alter table pit_dept_instructor add constraint fk_pit_di_to_pitd foreign key (point_in_time_data_id)
	references point_in_time_data (uniqueid) on delete cascade

alter table pit_dept_instructor add constraint fk_pit_di_to_di foreign key (dept_instructor_id)
	references departmental_instructor (uniqueid) on delete set null;

alter table pit_dept_instructor add constraint fk_pit_di_to_d foreign key (department_id)
	references department (uniqueid) on delete cascade;

alter table %SCHEMA%.pit_dept_instructor add constraint fk_pit_di_to_pt foreign key (pos_code_type)
	references %SCHEMA%.position_type (uniqueid) on delete set null;

create table pit_class_instructor (
	uniqueid number(20,0) primary key not null,
	pit_class_id number(20,0) not null,
	pit_dept_instr_id number(20,0) not null,
	percent_share number(3, 0) default null,
	normalized_pct_share number(3, 0) default null,
    responsibility_id number(20,0) default null,
    is_lead number(1, 0) default null
	) ;

alter table pit_class_instructor add constraint fk_pit_ci_to_pit_di foreign key (pit_dept_instr_id)
	references pit_dept_instructor (uniqueid) on delete cascade;

alter table pit_class_instructor add constraint fk_pit_ci_to_pit_c foreign key (pit_class_id)
	references pit_class (uniqueid) on delete cascade;

alter table pit_class_instructor add constraint fk_pit_ci_to_tr foreign key (responsibility_id)
	references teaching_responsibility (uniqueid) on delete cascade;

create table pit_class_event (
	uniqueid number(20,0) primary key not null,
	pit_class_id number(20,0) not null,
	event_name varchar2(100)
	) ;

alter table pit_class_event add constraint fk_pit_ce_to_pit_c foreign key (pit_class_id)
	references pit_class (uniqueid) on delete cascade;

create table pit_class_meeting (
	uniqueid number(20,0) primary key not null,
	pit_class_event_id number(20,0) not null,
    meeting_date timestamp not null,
    start_period number(10, 0) not null,
    start_offset number(10, 0),
    stop_period number(10, 0) not null,
    stop_offset number(10, 0),
    location_perm_id number(20,0),
    time_pattern_min_per_mtg number(10, 0),
    calculated_min_per_mtg number(10, 0)
	) ;

alter table pit_class_meeting add constraint fk_pit_cm_to_pit_ce foreign key (pit_class_event_id)
	references pit_class_event (uniqueid) on delete cascade;

create table pit_class_mtg_util_period (
	uniqueid number(20,0) primary key not null,
	pit_class_meeting_id number(20,0) not null,
    time_slot number(10, 0) not null
	) ;

alter table pit_class_mtg_util_period add constraint fk_pit_cmup_to_pit_cm foreign key (pit_class_meeting_id)
	references pit_class_meeting (uniqueid) on delete cascade;

insert into rights (role_id, value)
select distinct r.role_id, 'PointInTimeData'
from roles r, rights g where g.role_id = r.role_id and g.value = 'HQLReports';

insert into rights (role_id, value)
select distinct r.role_id, 'PointInTimeDataEdit'
from roles r, rights g where g.role_id = r.role_id and g.value = 'DataExchange';

insert into rights (role_id, value)
select distinct r.role_id, 'PointInTimeDataReports'
from roles r, rights g where g.role_id = r.role_id and g.value = 'HQLReports';

/* TODO: set up appropriate permissions for Point in Time data
    insert into rights (role_id, value)
	   select distinct r.role_id, 'InstructorGlobalAttributeEdit'
	   from roles r, rights g where g.role_id = r.role_id and g.value = 'GlobalRoomFeatureEdit';
*/

/*
 * Update database version
 */

update application_config set value='171' where name='tmtbl.db.version';

commit;
