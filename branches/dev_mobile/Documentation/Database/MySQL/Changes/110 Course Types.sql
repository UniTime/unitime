/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC
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

create table course_type (
	uniqueid decimal(20,0) primary key not null,
	reference varchar(20) not null,
	label varchar(60) not null
) engine = INNODB;

alter table course_offering add course_type_id decimal(20,0);

alter table course_offering add constraint fk_course_offering_type foreign key (course_type_id)
	references course_type (uniqueid) on delete set null;

create table sectioning_course_types (
	sectioning_status_id decimal(20,0) not null,
	course_type_id decimal(20,0) not null,
	primary key(sectioning_status_id, course_type_id)
) engine = INNODB;

alter table sectioning_course_types add constraint fk_sect_course_status foreign key (sectioning_status_id)
	references sectioning_status (uniqueid) on delete cascade;

alter table sectioning_course_types add constraint fk_sect_course_type foreign key (course_type_id)
	references course_type (uniqueid) on delete cascade;

insert into rights (role_id, value)
	select distinct r.role_id, 'CourseTypes'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'PositionTypes';

insert into rights (role_id, value)
	select distinct r.role_id, 'CourseTypeEdit'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'PositionTypeEdit';

/*
 * Update database version
 */

update application_config set value='110' where name='tmtbl.db.version';

commit;
