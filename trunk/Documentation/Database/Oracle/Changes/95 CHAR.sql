/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC
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

alter table building modify external_uid varchar2(40 char);
alter table building modify abbreviation varchar2(10 char);
alter table building modify name varchar2(100 char);
alter table subject_area modify external_uid varchar2(40 char);
alter table subject_area modify subject_area_abbreviation varchar2(10 char);
alter table subject_area modify short_title varchar2(50 char);
alter table subject_area modify long_title varchar2(100 char);
alter table distribution_type modify allowed_pref varchar2(10 char);
alter table offr_consent_type modify abbv varchar2(20 char);
alter table crse_credit_format modify abbreviation varchar2(10 char);
alter table course_credit_type modify legacy_crse_master_code varchar2(10 char);
alter table course_credit_type modify abbreviation varchar2(10 char);
alter table sectioning_status modify message varchar2(200 char);
alter table room modify room_number varchar2(10 char);
alter table room modify external_uid varchar2(40 char);
alter table room modify classification varchar2(20 char);
alter table non_university_location modify name varchar2(20 char);
alter table room_feature modify label varchar2(20 char);
alter table room_feature modify abbv varchar2(20 char);
alter table room_feature modify sis_reference varchar2(20 char);
alter table room_feature modify sis_value varchar2(20 char);
alter table room_group modify name varchar2(20 char);
alter table room_group modify abbv varchar2(20 char);
alter table room_group modify description varchar2(200 char);
alter table course_offering modify perm_id varchar2(20 char);
alter table course_offering modify course_nbr varchar2(10 char);
alter table course_offering modify title varchar2(90 char);
alter table course_offering modify schedule_book_note varchar2(1000 char);
alter table course_offering modify external_uid varchar2(40 char);
alter table course_offering modify course_nbr varchar2(10 char);
alter table instructional_offering modify external_uid varchar2(40 char);
alter table instr_offering_config modify name varchar2(10 char);
alter table time_pattern modify name varchar2(50 char);
alter table preference_level modify pref_prolog varchar2(2 char);
alter table preference_level modify pref_name varchar2(20 char);
alter table time_pref modify preference varchar2(2048 char);
alter table scheduling_subpart modify subpart_suffix varchar2(5 char);
alter table class_ modify notes varchar2(1000 char);
alter table class_ modify sched_print_note varchar2(2000 char);
alter table class_ modify class_suffix varchar2(10 char);
alter table class_ modify external_uid varchar2(40 char);
alter table department modify external_uid varchar2(40 char);
alter table department modify dept_code varchar2(50 char);
alter table department modify abbreviation varchar2(20 char);
alter table department modify name varchar2(100 char);
alter table department modify rs_color varchar2(6 char);
alter table department modify external_mgr_label varchar2(30 char);
alter table department modify external_mgr_abbv varchar2(10 char);
alter table sessions modify academic_initiative varchar2(20 char);
alter table sessions modify academic_year varchar2(4 char);
alter table sessions modify academic_term varchar2(20 char);
alter table sessions modify holidays varchar2(366 char);
alter table departmental_instructor modify external_uid varchar2(40 char);
alter table departmental_instructor modify career_acct varchar2(20 char);
alter table departmental_instructor modify fname varchar2(100 char);
alter table departmental_instructor modify mname varchar2(100 char);
alter table departmental_instructor modify lname varchar2(100 char);
alter table departmental_instructor modify note varchar2(20 char);
alter table departmental_instructor modify email varchar2(200 char);
alter table exam modify name varchar2(100 char);
alter table exam modify note varchar2(1000 char);
alter table exam modify assigned_pref varchar2(100 char);
alter table solver_parameter_def modify name varchar2(100 char);
alter table solver_parameter_def modify default_value varchar2(2048 char);
alter table solver_parameter_def modify description varchar2(1000 char);
alter table solver_parameter_def modify type varchar2(250 char);
alter table itype_desc modify abbv varchar2(7 char);
alter table itype_desc modify description varchar2(50 char);
alter table itype_desc modify sis_ref varchar2(20 char);
alter table settings modify name varchar2(30 char);
alter table settings modify default_value varchar2(100 char);
alter table settings modify allowed_values varchar2(500 char);
alter table settings modify description varchar2(100 char);
alter table manager_settings modify value varchar2(100 char);
alter table solver_parameter modify value varchar2(2048 char);
alter table solver_parameter_group modify name varchar2(100 char);
alter table solver_parameter_group modify description varchar2(1000 char);
alter table solver_predef_setting modify name varchar2(100 char);
alter table solver_predef_setting modify description varchar2(1000 char);
alter table solver_info_def modify name varchar2(100 char);
alter table solver_info_def modify description varchar2(1000 char);
alter table solver_info_def modify implementation varchar2(250 char);
alter table solver_info modify opt varchar2(250 char);
alter table solution modify note varchar2(1000 char);
alter table solution modify creator varchar2(250 char);
alter table assignment modify class_name varchar2(100 char);
alter table user_data modify value varchar2(2048 char);
alter table date_pattern modify name varchar2(50 char);
alter table date_pattern modify pattern varchar2(366 char);
alter table course_credit_unit_config modify credit_format varchar2(20 char);
alter table academic_area modify external_uid varchar2(40 char);
alter table academic_area modify academic_area_abbreviation varchar2(10 char);
alter table academic_area modify short_title varchar2(50 char);
alter table academic_area modify long_title varchar2(100 char);
alter table academic_classification modify external_uid varchar2(40 char);
alter table academic_classification modify code varchar2(10 char);
alter table academic_classification modify name varchar2(50 char);
alter table staff modify external_uid varchar2(40 char);
alter table staff modify fname varchar2(100 char);
alter table staff modify mname varchar2(100 char);
alter table staff modify lname varchar2(100 char);
alter table staff modify dept varchar2(50 char);
alter table staff modify email varchar2(200 char);
alter table pos_major modify external_uid varchar2(40 char);
alter table pos_major modify code varchar2(10 char);
alter table pos_major modify name varchar2(50 char);
alter table pos_minor modify external_uid varchar2(40 char);
alter table pos_minor modify code varchar2(10 char);
alter table pos_minor modify name varchar2(50 char);
alter table student_group modify group_abbreviation varchar2(30 char);
alter table student_group modify group_name varchar2(90 char);
alter table student_group modify external_uid varchar2(40 char);
alter table application_config modify value varchar2(4000 char);
alter table application_config modify description varchar2(100 char);
alter table history modify old_value varchar2(20 char);
alter table history modify new_value varchar2(20 char);
alter table history modify old_number varchar2(4 char);
alter table history modify new_number varchar2(4 char);
alter table course_demand modify changed_by varchar2(40 char);
alter table free_time modify name varchar2(50 char);
alter table lastlike_course_demand modify course_nbr varchar2(10 char);
alter table lastlike_course_demand modify course_perm_id varchar2(20 char);
alter table student modify external_uid varchar2(40 char);
alter table student modify first_name varchar2(100 char);
alter table student modify middle_name varchar2(100 char);
alter table student modify last_name varchar2(100 char);
alter table student modify email varchar2(200 char);
alter table student_accomodation modify name varchar2(50 char);
alter table student_accomodation modify abbreviation varchar2(20 char);
alter table student_accomodation modify external_uid varchar2(40 char);
alter table student_class_enrl modify approved_by varchar2(40 char);
alter table student_class_enrl modify changed_by varchar2(40 char);
alter table student_enrl_msg modify message varchar2(255 char);
alter table change_log modify obj_type varchar2(255 char);
alter table change_log modify obj_title varchar2(255 char);
alter table change_log modify source varchar2(50 char);
alter table change_log modify operation varchar2(50 char);
alter table course_catalog modify external_uid varchar2(40 char);
alter table course_catalog modify subject varchar2(10 char);
alter table course_catalog modify course_nbr varchar2(10 char);
alter table course_catalog modify title varchar2(100 char);
alter table course_catalog modify perm_id varchar2(20 char);
alter table course_catalog modify approval_type varchar2(20 char);
alter table course_catalog modify prev_subject varchar2(10 char);
alter table course_catalog modify prev_crs_nbr varchar2(10 char);
alter table course_catalog modify credit_type varchar2(20 char);
alter table course_catalog modify credit_unit_type varchar2(20 char);
alter table course_catalog modify credit_format varchar2(20 char);
alter table course_subpart_credit modify subpart_id varchar2(10 char);
alter table course_subpart_credit modify credit_type varchar2(20 char);
alter table course_subpart_credit modify credit_unit_type varchar2(20 char);
alter table course_subpart_credit modify credit_format varchar2(20 char);
alter table external_building modify external_uid varchar2(40 char);
alter table external_building modify abbreviation varchar2(10 char);
alter table external_building modify display_name varchar2(100 char);
alter table external_room modify external_uid varchar2(40 char);
alter table external_room modify room_number varchar2(10 char);
alter table external_room modify classification varchar2(20 char);
alter table external_room modify display_name varchar2(100 char);
alter table external_room_department modify department_code varchar2(50 char);
alter table external_room_department modify assignment_type varchar2(20 char);
alter table external_room_feature modify name varchar2(20 char);
alter table external_room_feature modify value varchar2(20 char);
alter table event_note modify text_note varchar2(1000 char);
alter table event_note modify uname varchar2(100 char);
alter table event_note modify meetings varchar2(2000 char);
alter table event modify event_name varchar2(100 char);
alter table event modify email varchar2(1000 char);
alter table room_type_option modify message varchar2(200 char);
alter table curriculum modify abbv varchar2(20 char);
alter table curriculum modify name varchar2(60 char);
alter table curriculum_clasf modify name varchar2(20 char);
alter table curriculum_group modify name varchar2(20 char);
alter table curriculum_group modify color varchar2(20 char);
alter table query_log modify uri varchar2(255 char);
alter table query_log modify session_id varchar2(32 char);
alter table query_log modify userid varchar2(40 char);
alter table sectioning_log modify student varchar2(40 char);
alter table sectioning_log modify operation varchar2(20 char);
alter table sectioning_log modify user_id varchar2(40 char);
alter table saved_hql modify name varchar2(100 char);
alter table saved_hql modify description varchar2(1000 char);
		
/*
 * Update database version
 */

update application_config set value='95' where name='tmtbl.db.version';

commit;
