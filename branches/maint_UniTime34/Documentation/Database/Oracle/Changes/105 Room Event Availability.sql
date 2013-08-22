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

alter table room add availability varchar2(2048 char);
alter table non_university_location add availability varchar2(2048 char);
		
insert into rights (role_id, value)
		select distinct r.role_id, 'RoomDetailEventAvailability'
		from roles r, rights g where g.role_id = r.role_id and g.value like 'RoomEditChangeEventProperties';

insert into rights (role_id, value)
	select distinct r.role_id, 'RoomEditEventAvailability'
	from roles r, rights g where g.role_id = r.role_id and g.value like 'RoomEditChangeEventProperties';

/*
 * Update database version
 */

update application_config set value='105' where name='tmtbl.db.version';

commit;
