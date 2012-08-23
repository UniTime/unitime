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


alter table roles add manager int(1) default 1;
alter table roles add enabled int(1) default 1;

create table rights (
	role_id decimal(20,0) not null,
	value varchar(200) binary not null,
	primary key (role_id, value)
) engine = INNODB;
			
alter table rights add constraint fk_rights_role foreign key (role_id)
	references roles (role_id) on delete cascade;

select 32767 * next_hi into @id from hibernate_unique_key;

update hibernate_unique_key set next_hi=next_hi+1;

insert into roles (role_id, reference, abbv, manager, enabled) values 
			(@id + 0, 'No Role', 'No Role', 0, 1),
			(@id + 1, 'Student', 'Student', 0, 1),
			(@id + 2, 'Instructor', 'Instructor', 0, 1);

/*
 * Update database version
 */

update application_config set value='86' where name='tmtbl.db.version';

commit;
