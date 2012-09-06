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

alter table room add event_dept_id decimal(20,0);
alter table non_university_location add event_dept_id decimal(20,0);

alter table room add constraint fk_room_event_dept foreign key (event_dept_id)
	references department (uniqueid) on delete set null;

alter table non_university_location add constraint fk_loc_event_dept foreign key (event_dept_id)
	references department (uniqueid) on delete set null;

update room l set l.event_dept_id = (
		select distinct d.uniqueid 
		from room_dept rd, department d, timetable_manager m, dept_to_tt_mgr dm, roles r, tmtbl_mgr_to_roles mr
		where dm.timetable_mgr_id = m.uniqueid and dm.department_id = d.uniqueid and mr.manager_id = m.uniqueid and
		mr.role_id = r.role_id and r.reference = 'Event Mgr' and l.uniqueid = rd.room_id and rd.is_control = 1 and rd.department_id = d.uniqueid
	);

update non_university_location l set l.event_dept_id = (
		select distinct d.uniqueid 
		from room_dept rd, department d, timetable_manager m, dept_to_tt_mgr dm, roles r, tmtbl_mgr_to_roles mr
		where dm.timetable_mgr_id = m.uniqueid and dm.department_id = d.uniqueid and mr.manager_id = m.uniqueid and
		mr.role_id = r.role_id and r.reference = 'Event Mgr' and l.uniqueid = rd.room_id and rd.is_control = 1 and rd.department_id = d.uniqueid
	);

alter table department add allow_events int(1) null default 0;

update department d set d.allow_events = 1 where d.uniqueid in (
		select dm.department_id
		from timetable_manager m, dept_to_tt_mgr dm, roles r, tmtbl_mgr_to_roles mr
		where dm.timetable_mgr_id = m.uniqueid and mr.manager_id = m.uniqueid and mr.role_id = r.role_id and r.reference = 'Event Mgr'
	);

/*
 * Update database version
 */

update application_config set value='89' where name='tmtbl.db.version';

commit;
