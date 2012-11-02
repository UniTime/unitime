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
	uniqueid decimal(20,0) primary key not null,
	reference varchar(20) not null,
	label varchar(60) not null,
	xtype bigint(10) not null
	) engine = INNODB;

select 32767 * next_hi into @id from hibernate_unique_key;

update hibernate_unique_key set next_hi=next_hi+1;

insert into exam_type (uniqueid, reference, label, xtype) values
	(@id, 'final', 'Final', 0),
	(@id + 1, 'midterm', 'Midterm', 1);
	
alter table exam add exam_type_id decimal(20,0) null;
update exam set exam_type_id = @id where exam_type = 0;
update exam set exam_type_id = @id + 1 where exam_type = 1;

alter table exam_period add exam_type_id decimal(20,0) null;
update exam_period set exam_type_id = @id where exam_type = 0;
update exam_period set exam_type_id = @id + 1 where exam_type = 1;

create table room_exam_type (
	location_id decimal(20,0) not null,
	exam_type_id decimal(20,0) not null,
	primary key (location_id, exam_type_id)
	) engine = INNODB;

alter table room_exam_type add constraint fk_room_exam_type foreign key (exam_type_id)
	references exam_type (uniqueid) on delete cascade;

insert into room_exam_type (location_id, exam_type_id)
	select uniqueid, @id from room where exam_type in (1, 3);
insert into room_exam_type (location_id, exam_type_id)
	select uniqueid, @id + 1 from room where exam_type in (2, 3);
insert into room_exam_type (location_id, exam_type_id)
	select uniqueid, @id from non_university_location where exam_type in (1, 3);
insert into room_exam_type (location_id, exam_type_id)
	select uniqueid, @id + 1 from non_university_location where exam_type in (2, 3);

alter table exam
  	add constraint fk_exam_type foreign key (exam_type_id)
  	references exam_type (uniqueid) on delete cascade;
alter table exam modify exam_type_id decimal(20,0) not null;
alter table exam drop column exam_type;

alter table exam_period
  	add constraint fk_exam_period_type foreign key (exam_type_id)
  	references exam_type (uniqueid) on delete cascade;
alter table exam_period modify exam_type_id decimal(20,0) not null;
alter table exam_period drop column exam_type;

alter table room drop column exam_type;
alter table non_university_location drop column exam_type;

select role_id into @r1 from roles where reference = 'Sysadmin';
select role_id into @r2 from roles where reference = 'Administrator';
insert into rights (role_id, value) values
	(@r1, 'ExamTypes'),
	(@r1, 'ExamTypeEdit'),
	(@r2, 'ExamTypes');

insert into rights (role_id, value)
	select distinct r.role_id, 'EditRoomDepartmentsExams'
	from roles r, rights g where g.role_id = r.role_id and g.value like 'EditRoomDepartments%Exams';

/*
 * Update database version
 */

update application_config set value='93' where name='tmtbl.db.version';

commit;
