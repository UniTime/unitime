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

alter table room add break_time number(10);
alter table non_university_location add break_time number(10);
		
alter table room add event_status number(10);
alter table non_university_location add event_status number(10);
		
alter table room add note varchar2(2048 char);
alter table non_university_location add note varchar2(2048 char);
		
alter table roles add instructor number(1) default 0;
		
alter table departmental_instructor add role_id number(20,0);

alter table departmental_instructor add constraint fk_instructor_role foreign key (role_id)
	references roles (role_id) on delete set null;

insert into rights (role_id, value)
	select distinct r.role_id, 'InstructorRoles'
	from roles r, rights g where g.role_id = r.role_id and g.value like 'EventRoomTypes';
insert into rights (role_id, value)
	select distinct r.role_id, 'InstructorRoleEdit'
	from roles r, rights g where g.role_id = r.role_id and g.value like 'EventRoomTypeEdit';
insert into rights (role_id, value)
	select distinct r.role_id, 'RoomEditChangeEventProperties'
	from roles r, rights g where g.role_id = r.role_id and g.value like 'EventRoomTypeEdit';

/*
 * Update database version
 */

update application_config set value='103' where name='tmtbl.db.version';

commit;
