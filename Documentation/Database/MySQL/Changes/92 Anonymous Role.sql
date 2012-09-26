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

select 32767 * next_hi into @id from hibernate_unique_key;

update hibernate_unique_key set next_hi=next_hi+1;

insert into roles (role_id, reference, abbv, manager, enabled) values (@id, 'Anonymous', 'Anonymous', 0, 1);

insert into rights (role_id, value) values
			(@id, 'Events'),
			(@id, 'SchedulingAssistant');
/*
 * Update database version
 */

update application_config set value='92' where name='tmtbl.db.version';

commit;
