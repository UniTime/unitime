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

insert into rights (role_id, value)
	select distinct r.role_id, 'StudentSchedulingMassCancel'
	from roles r, rights g where g.role_id = r.role_id and g.value like 'StudentSchedulingAdmin';

insert into rights (role_id, value)
	select distinct r.role_id, 'StudentSchedulingEmailStudent'
	from roles r, rights g where g.role_id = r.role_id and g.value like 'StudentSchedulingAdmin';

insert into rights (role_id, value)
	select distinct r.role_id, 'StudentSchedulingChangeStudentStatus'
	from roles r, rights g where g.role_id = r.role_id and g.value like 'StudentSchedulingAdmin';

update sectioning_status set status = 215 where reference = 'Enabled';
update sectioning_status set status = 20 where reference = 'Disabled';
update sectioning_status set status = 151 where reference = 'Not Available';
update sectioning_status set status = 211 where reference = 'No Email';

/*
 * Update database version
 */

update application_config set value='138' where name='tmtbl.db.version';

commit;
