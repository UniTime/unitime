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


/* Instructor Survey Course Request Note Types */
create table instr_crsreq_note_type (
  uniqueid number(20,0) constraint nn_instr_crsreq_ntype_id not null,
  reference varchar2(20 char) constraint nn_instr_crsreq_ntype_ref not null,
  label varchar2(60 char) constraint nn_instr_crsreq_ntype_lab not null,
  length number(10) constraint nn_instr_crsreq_ntype_length not null,
  sort_order number(10) constraint nn_instr_crsreq_ntype_sort not null
);
alter table instr_crsreq_note_type add constraint pk_instr_crsreq_note_type primary key (uniqueid);

insert into rights (role_id, value)
  select distinct r.role_id, 'InstructorSurveyNoteTypes'
  from roles r, rights g where g.role_id = r.role_id and g.value = 'Majors';
insert into rights (role_id, value)
  select distinct r.role_id, 'InstructorSurveyNoteTypeEdit'
  from roles r, rights g where g.role_id = r.role_id and g.value = 'MajorEdit';
insert into rights (role_id, value)
  select distinct r.role_id, 'InstructorSurvey'
  from roles r, rights g where g.role_id = r.role_id and g.value = 'InstructorPreferences';
insert into rights (role_id, value)
  select distinct r.role_id, 'InstructorSurveyAdmin'
  from roles r, rights g where g.role_id = r.role_id and g.value = 'InstructorAdd';

insert into instr_crsreq_note_type (uniqueid, reference, label, length, sort_order) values
  (ref_table_seq.nextval, 'Section(s)', 'Section Identification', 8, 0);
insert into instr_crsreq_note_type (uniqueid, reference, label, length, sort_order) values
  (ref_table_seq.nextval, 'Notes', 'Instructor Requirements', 82, 1);

/* Preference notes */
alter table room_pref add note varchar2(2048 char);
alter table room_feature_pref add note varchar2(2048 char);
alter table building_pref add note varchar2(2048 char);
alter table time_pref add note varchar2(2048 char);
alter table date_pattern_pref add note varchar2(2048 char);
alter table distribution_pref add note varchar2(2048 char);
alter table room_group_pref add note varchar2(2048 char);
alter table exam_period_pref add note varchar2(2048 char);
alter table attribute_pref add note varchar2(2048 char);
alter table course_pref add note varchar2(2048 char);
alter table instructor_pref add note varchar2(2048 char);

/* Instructor Survey */
create table instructor_survey (
  uniqueid number(20,0) constraint nn_instructor_survey_id not null,
  session_id number(20,0) constraint nn_instructor_survey_sess not null,
  external_uid varchar2(40 char) constraint nn_instructor_survey_ext not null,
  email varchar2(200 char),
  note varchar2(2048 char),
  submitted timestamp
);
alter table instructor_survey add constraint pk_instructor_survey primary key (uniqueid);
alter table instructor_survey add constraint fk_instr_surv_session foreign key (session_id)
  references sessions (uniqueid) on delete cascade;

/* Instructor Survey Course Requirements */
create table instr_crsreq (
  uniqueid number(20,0) constraint nn_instr_crsreq_id not null,
  survey_id number(20,0) constraint nn_instr_crsreq_survey not null,
  course varchar2(1024 char),
  course_offering_id number(20,0)
);
alter table instr_crsreq add constraint pk_instr_crsreq primary key (uniqueid);
alter table instr_crsreq add constraint fk_instr_crsreq_survey foreign key (survey_id)
  references instructor_survey (uniqueid) on delete cascade;
alter table instr_crsreq add constraint fk_instr_crsreq_course foreign key (course_offering_id)
  references course_offering (uniqueid) on delete set null;

/* Instructor Survey Course Requirement Notes */
create table instr_crsreq_note (
  requirement_id number(20,0) constraint nn_instr_crsreq_note_req not null,
  type_id number(20,0) constraint nn_instr_crsreq_note_req_type not null,
  note varchar2(2048 char)
);
alter table instr_crsreq_note add constraint pk_instr_crsreq_note primary key (requirement_id, type_id);
alter table instr_crsreq_note add constraint fk_instr_crsreq_note_req foreign key (requirement_id)
  references instr_crsreq (uniqueid) on delete cascade;
alter table instr_crsreq_note add constraint fk_instr_crsreq_note_type foreign key (type_id)
  references instr_crsreq_note_type (uniqueid) on delete cascade;  
/*
 * Update database version
 */
  
update application_config set value='256' where name='tmtbl.db.version';

commit;
