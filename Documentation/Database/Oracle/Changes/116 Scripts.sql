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
		uniqueid number(20,0) constraint nn_script_id not null,
		name varchar2(128 char) constraint nn_script_name not null,
		description varchar2(1024 char),
		engine varchar2(32 char) constraint nn_script_engine not null,
		permission varchar2(128 char),
		script clob
	);

alter table script add constraint pk_script primary key (uniqueid);

create table script_parameter (
		script_id number(20,0) constraint nn_script_param_id not null,
		name varchar2(128 char) constraint nn_script_param_name not null,
		label varchar2(256 char),
		type varchar2(2048 char) constraint nn_script_param_type not null,
		default_value varchar2(2048 char)
	);

alter table script_parameter add constraint pk_script_parameter primary key (script_id, name);

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
