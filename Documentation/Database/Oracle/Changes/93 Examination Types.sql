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

create table exam_type (
	uniqueid number(20,0) constraint nn_exam_type_uniqueid not null,
	reference varchar2(20) constraint nn_exam_type_reference not null,
	label varchar2(60) constraint nn_exam_type_label not null,
	xtype number(10,0) constraint nn_exam_type_type not null
	);

alter table exam_type add constraint pk_exam_type primary key (uniqueid);

alter table exam add exam_type_id number(20);

alter table exam_period add exam_type_id number(20);

create table room_exam_type (
	location_id number(20,0) constraint nn_room_extype_loc not null,
	exam_type_id number(20,0) constraint nn_room_extype_type not null
	);
alter table room_exam_type add constraint pk_room_exam_type primary key (location_id, exam_type_id);

alter table room_exam_type add constraint fk_room_exam_type foreign key (exam_type_id)
	references exam_type (uniqueid) on delete cascade;

insert into exam_type (uniqueid, reference, label, xtype) values
	(ref_table_seq.nextval, 'final', 'Final', 0);
update exam set exam_type_id = ref_table_seq.currval where exam_type = 0;
update exam_period set exam_type_id = ref_table_seq.currval where exam_type = 0;
insert into room_exam_type (location_id, exam_type_id)
	select uniqueid, ref_table_seq.currval from room where exam_type in (1, 3);
insert into room_exam_type (location_id, exam_type_id)
	select uniqueid, ref_table_seq.currval from non_university_location where exam_type in (1, 3);

insert into exam_type (uniqueid, reference, label, xtype) values
	(ref_table_seq.nextval, 'midterm', 'Midterm', 1);
update exam set exam_type_id = ref_table_seq.currval where exam_type = 1;
update exam_period set exam_type_id = ref_table_seq.currval where exam_type = 1;
insert into room_exam_type (location_id, exam_type_id)
	select uniqueid, ref_table_seq.currval from room where exam_type in (2, 3);
insert into room_exam_type (location_id, exam_type_id)
	select uniqueid, ref_table_seq.currval from non_university_location where exam_type in (2, 3);

alter table exam
  	add constraint fk_exam_type foreign key (exam_type_id)
  	references exam_type (uniqueid) on delete cascade;
alter table exam add constraint nn_exam_type check (exam_type_id is not null);
alter table exam drop column exam_type;

alter table exam_period
  	add constraint fk_exam_period_type foreign key (exam_type_id)
  	references exam_type (uniqueid) on delete cascade;
alter table exam_period add constraint nn_exam_period_type check (exam_type_id is not null);
alter table exam_period drop column exam_type;

alter table room drop column exam_type;
alter table non_university_location drop column exam_type;

insert into rights (select role_id, 'ExamTypes' as value from roles where reference = 'Administrator');
insert into rights (select role_id, 'ExamTypes' as value from roles where reference = 'Sysadmin');
insert into rights (select role_id, 'ExamTypeEdit' as value from roles where reference = 'Sysadmin');

insert into rights (role_id, value)
	select distinct r.role_id, 'EditRoomDepartmentsExams'
	from roles r, rights g where g.role_id = r.role_id and g.value like 'EditRoomDepartments%Exams';

/*
 * Update database version
 */

update application_config set value='93' where name='tmtbl.db.version';

commit;
