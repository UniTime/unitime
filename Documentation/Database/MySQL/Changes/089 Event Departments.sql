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
