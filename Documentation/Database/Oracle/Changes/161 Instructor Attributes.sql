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
create table attribute_type (
	uniqueid number(20,0) constraint nn_attribute_type_uniqueid not null,
	reference varchar2(20) constraint nn_attribute_type_reference not null,
	label varchar2(60) constraint nn_attribute_type_label not null,
	conjunctive number(1,0) constraint nn_attribute_type_conjunctive not null,
	required number(1,0) constraint nn_attribute_type_required not null
);
alter table attribute_type add constraint pk_attribute_type primary key (uniqueid);

insert into attribute_type (uniqueid, reference, label, conjunctive, required) values 
	(ref_table_seq.nextval, 'Performance', 'Performance Level', 0, 1);
insert into attribute_type (uniqueid, reference, label, conjunctive, required) values 
	(ref_table_seq.nextval, 'Skill', 'Skill', 1, 1);
insert into attribute_type (uniqueid, reference, label, conjunctive, required) values 
	(ref_table_seq.nextval, 'Qualification', 'Qualification', 0, 1);
insert into attribute_type (uniqueid, reference, label, conjunctive, required) values 
	(ref_table_seq.nextval, 'Cerfification', 'Cerfification', 0, 1);	

create table attribute (
	uniqueid number(20,0) constraint nn_attribute_uniqueid not null,
	code varchar2(20) constraint nn_attribute_code not null,
	name varchar2(60) constraint nn_attribute_name not null,
	type_id number(20,0) constraint nn_attribute_type not null,
	parent_id number(20,0),
	session_id number(20,0) constraint nn_attribute_session not null,
	department_id number(20,0)
);
alter table attribute add constraint pk_attribute primary key (uniqueid)

alter table attribute add constraint fk_attribute_type foreign key (type_id)
	references attribute_type (uniqueid) on delete cascade;

alter table attribute add constraint fk_attribute_session foreign key (session_id)
	references sessions (uniqueid) on delete cascade;

alter table attribute add constraint fk_attribute_department foreign key (department_id)
	references department (uniqueid) on delete cascade;

alter table attribute add constraint fk_attribute_parent foreign key (parent_id)
	references attribute (uniqueid) on delete set null;

create table instructor_attributes (
	attribute_id number(20,0) constraint nn_instrattributes_attribute not null,
	instructor_id number(20,0) constraint nn_instrattributes_instructor not null
);
alter table instructor_attributes add constraint pk_instructor_attributes primary key (attribute_id, instructor_id);

alter table instructor_attributes add constraint fk_instrattributes_attribute foreign key (attribute_id)
	references attribute (uniqueid) on delete cascade;

alter table instructor_attributes add constraint fk_instrattributes_instructor foreign key (instructor_id)
	references departmental_instructor (uniqueid) on delete cascade;

create table attribute_pref (
	uniqueid number(20,0) constraint nn_attribute_pref_uniqueid not null,
	owner_id number(20,0) constraint nn_attribute_pref_owner not null,
	pref_level_id number(20,0) constraint nn_attribute_pref_pref not null,
	attribute_id number(20,0) constraint nn_attribute_pref_attribute not null
);

alter table attribute_pref add constraint pk_attribute_pref primary key (uniqueid);

alter table attribute_pref add constraint fk_attribute_pref_pref foreign key (pref_level_id)
	references preference_level (uniqueid) on delete cascade;

alter table attribute_pref add constraint fk_attribute_pref_attribute foreign key (attribute_id)
	references attribute (uniqueid) on delete cascade;

alter table departmental_instructor add teaching_pref_id number(20,0);
alter table departmental_instructor add max_load float;
alter table departmental_instructor add constraint fk_dept_instr_teach_pref foreign key (teaching_pref_id)
	references preference_level (uniqueid) on delete set null;

create table course_pref (
	uniqueid number(20,0) constraint nn_course_pref_uniqueid not null,
	owner_id number(20,0) constraint nn_course_pref_owner not null,
	pref_level_id number(20,0) constraint nn_course_pref_pref not null,
	course_id number(20,0) constraint nn_course_pref_course not null
)
alter table course_pref add constraint pk_course_pref primary key (uniqueid);
alter table course_pref add constraint fk_course_pref_pref foreign key (pref_level_id)
	references preference_level (uniqueid) on delete cascade;
alter table course_pref add constraint fk_course_pref_course foreign key (course_id)
	references course_offering (uniqueid) on delete cascade;

alter table scheduling_subpart add teaching_load float;

alter table class_instructor add tentative number(1,0) default 0;
alter table class_instructor add constraint nn_class_instructor_tentative check (tentative is not null);

insert into rights (role_id, value)
	select distinct r.role_id, 'InstructorAttributeTypes'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'RoomFeatureTypes';

insert into rights (role_id, value)
	select distinct r.role_id, 'InstructorAttributeTypeEdit'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'RoomFeatureTypeEdit';

insert into rights (role_id, value)
	select distinct r.role_id, 'InstructorAssignmentPreferences'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'InstructorPreferences';

insert into rights (role_id, value)
	select distinct r.role_id, 'InstructorClearAssignmentPreferences'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'InstructorEditClearPreferences';

insert into rights (role_id, value)
	select distinct r.role_id, 'InstructorAttributes'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'Instructors';

insert into rights (role_id, value)
	select distinct r.role_id, 'InstructorAttributeAdd'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'InstructorAdd';

insert into rights (role_id, value)
	select distinct r.role_id, 'InstructorAttributeEdit'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'InstructorEdit';

insert into rights (role_id, value)
	select distinct r.role_id, 'InstructorAttributeDelete'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'InstructorDelete';

insert into rights (role_id, value)
	select distinct r.role_id, 'InstructorAttributeAssign'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'InstructorPreferences';

insert into rights (role_id, value)
	select distinct r.role_id, 'InstructorGlobalAttributeEdit'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'GlobalRoomFeatureEdit';

/*
 * Update database version
 */

update application_config set value='161' where name='tmtbl.db.version';

commit;