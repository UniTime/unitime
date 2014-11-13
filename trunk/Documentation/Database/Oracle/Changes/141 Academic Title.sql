/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2014, UniTime LLC
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

alter table staff add acad_title varchar2(50 char);
alter table departmental_instructor add acad_title varchar2(50 char);
alter table event_contact add acad_title varchar2(50 char);
alter table timetable_manager add acad_title varchar2(50 char);

update settings set allowed_values = 'last-first,first-last,initial-last,last-initial,first-middle-last,short,title-first-middle-last,last-first-middle-title,title-initial-last,title-last-initial' where name = 'name'; 

insert into rights (role_id, value)
	select distinct r.role_id, 'EventCanEditAcademicTitle'
	from roles r, rights g where g.role_id = r.role_id and g.value like 'EventLookupContact';


/*
 * Update database version
 */

update application_config set value='141' where name='tmtbl.db.version';

commit;
