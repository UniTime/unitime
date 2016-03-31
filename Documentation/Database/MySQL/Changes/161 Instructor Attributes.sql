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
	uniqueid decimal(20,0) primary key not null,
	reference varchar(20) not null,
	label varchar(60) not null,
	conjunctive int(1) not null,
	required int(1) not null
) engine = INNODB;

select 32767 * next_hi into @id from hibernate_unique_key;

insert into attribute_type (uniqueid, reference, label, conjunctive, required) values
	(@id+0, 'Performance', 'Performance Level', 0, 1),
	(@id+1, 'Skill', 'Skill', 1, 1),
	(@id+2, 'Qualification', 'Qualification', 0, 1),
	(@id+3, 'Certification', 'Certification', 0, 1);

update hibernate_unique_key set next_hi=next_hi+1;

create table attribute (
	uniqueid decimal(20,0) primary key not null,
	code varchar(20) not null,
	name varchar(60) not null,
	type_id decimal(20,0) not null,
	parent_id decimal(20,0),
	session_id decimal(20,0) not null,
	department_id decimal(20,0)
) engine = INNODB;

alter table attribute add constraint fk_attribute_type foreign key (type_id)
	references attribute_type (uniqueid) on delete cascade;

alter table attribute add constraint fk_attribute_session foreign key (session_id)
	references sessions (uniqueid) on delete cascade;

alter table attribute add constraint fk_attribute_department foreign key (department_id)
	references department (uniqueid) on delete cascade;

alter table attribute add constraint fk_attribute_parent foreign key (parent_id)
	references attribute (uniqueid) on delete set null;

create table instructor_attributes (
	attribute_id decimal(20,0) not null,
	instructor_id decimal(20,0) not null,
	primary key (attribute_id, instructor_id)
) engine = INNODB;

alter table instructor_attributes add constraint fk_instrattributes_attribute foreign key (attribute_id)
	references attribute (uniqueid) on delete cascade;

alter table instructor_attributes add constraint fk_instrattributes_instructor foreign key (instructor_id)
	references departmental_instructor (uniqueid) on delete cascade;

create table attribute_pref (
	uniqueid decimal(20,0) primary key not null,
	owner_id decimal(20,0) not null,
	pref_level_id decimal(20,0) not null,
	attribute_id decimal(20,0) not null
) engine = INNODB;

alter table attribute_pref add constraint fk_attribute_pref_pref foreign key (pref_level_id)
	references preference_level (uniqueid) on delete cascade;

alter table attribute_pref add constraint fk_attribute_pref_attribute foreign key (attribute_id)
	references attribute (uniqueid) on delete cascade;


alter table departmental_instructor add teaching_pref_id decimal(20,0);
alter table departmental_instructor add max_load float;
alter table departmental_instructor add constraint fk_dept_instr_teach_pref foreign key (teaching_pref_id)
	references preference_level (uniqueid) on delete set null;

create table course_pref (
	uniqueid decimal(20,0) primary key not null,
	owner_id decimal(20,0) not null,
	pref_level_id decimal(20,0) not null,
	course_id decimal(20,0) not null
) engine = INNODB;
alter table course_pref add constraint fk_course_pref_pref foreign key (pref_level_id)
	references preference_level (uniqueid) on delete cascade;
alter table course_pref add constraint fk_course_pref_course foreign key (course_id)
	references course_offering (uniqueid) on delete cascade;

alter table scheduling_subpart add teaching_load float;

alter table class_instructor add tentative int(1) not null default 0;

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
