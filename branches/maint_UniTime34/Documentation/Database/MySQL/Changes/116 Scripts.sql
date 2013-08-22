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

create table script (
		uniqueid decimal(20,0) primary key not null,
		name varchar(128) not null,
		description varchar(1024) null,
		engine varchar(32) not null,
		permission varchar(128) null,
		script longtext binary not null
	) engine = INNODB;

create table script_parameter (
		script_id decimal(20,0) not null,
		name varchar(128) not null,
		label varchar(256) null,
		type varchar(2048) not null,
		default_value varchar(2048) null,
		primary key (script_id, name)
	) engine = INNODB;

alter table script_parameter add constraint fk_script_parameter foreign key (script_id) references script(uniqueid) on delete cascade;

insert into rights (role_id, value)
	select distinct r.role_id, 'ScriptEdit' from roles r, rights g where g.role_id = r.role_id and g.value like 'PermissionEdit';

insert into rights (role_id, value)
	select distinct r.role_id, 'Scripts' from roles r, rights g where g.role_id = r.role_id and g.value like 'Permissions';

/*
 * Update database version
 */

update application_config set value='116' where name='tmtbl.db.version';

commit;
