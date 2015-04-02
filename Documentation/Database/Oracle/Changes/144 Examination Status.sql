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
create table exam_status (
	session_id number(20,0) constraint nn_xstatus_session not null,
	type_id number(20,0) constraint nn_xstatus_type not null,
	status_id number(20,0)
	);

alter table exam_status add constraint pk_exam_status primary key (session_id, type_id);

alter table exam_status add constraint fk_xstatus_session foreign key (session_id)
	references sessions (uniqueid) on delete cascade;
	
alter table exam_status add constraint fk_xstatus_type foreign key (type_id)
	references exam_type (uniqueid) on delete cascade;
	
alter table exam_status add constraint fk_xstatus_status foreign key (status_id)
	references dept_status_type (uniqueid) on delete set null;

create table exam_managers (
	session_id number(20,0) constraint nn_xmanagers_session not null,
	type_id number(20,0) constraint nn_xmanagers_type not null,
	manager_id number(20,0) constraint nn_xmanagers_manager not null
	);

alter table exam_managers add constraint pk_exam_managers primary key (session_id, type_id, manager_id);

alter table exam_managers add constraint fk_xmanagers_status foreign key (session_id, type_id)
	references exam_status (session_id, type_id) on delete cascade;

alter table exam_managers add constraint fk_xmanagers_manager foreign key (manager_id)
	references timetable_manager (uniqueid) on delete cascade;

insert into dept_status_type
	(uniqueid, reference, label, status, apply, ord) values
	(ref_table_seq.nextval, 'exam_disabled', 'Examination Disabled', 0, 4, 1000);

insert into dept_status_type
	(uniqueid, reference, label, status, apply, ord) values
	(ref_table_seq.nextval, 'exam_edit', 'Examination Data Entry', 1536, 4, 1001);

insert into dept_status_type
	(uniqueid, reference, label, status, apply, ord) values
	(ref_table_seq.nextval, 'exam_timetabling', 'Examination Timetabling', 3584, 4, 1002);

insert into dept_status_type
	(uniqueid, reference, label, status, apply, ord) values
	(ref_table_seq.nextval, 'exam_publish', 'Examination Published', 12800, 4, 1003);

insert into rights (role_id, value)
	select distinct r.role_id, 'ExaminationStatuses'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'CourseTypes';

insert into rights (role_id, value)
	select distinct r.role_id, 'ExaminationStatusEdit'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'CourseTypeEdit';
	
insert into rights (role_id, value)
	select distinct r.role_id, 'ExaminationView'
	from roles r, rights g where g.role_id = r.role_id and g.value in ('Examinations', 'ExaminationSchedule');

merge into dept_status_type t
	using (select uniqueid, row_number() over (order by ord, uniqueid) rnum from dept_status_type) s
	on (t.uniqueid = s.uniqueid)
	when matched then update set t.ord = s.rnum;

/*
 * Update database version
 */

update application_config set value='144' where name='tmtbl.db.version';

commit;
