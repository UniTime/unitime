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

create table session_config (
		session_id number(20,0) constraint nn_session_config_id not null,
		name varchar2(255 char) constraint nn_session_config_name not null,
		value varchar2(4000 char),
		description varchar2(500 char)
	);

alter table session_config add constraint pk_session_config primary key (session_id, name);

alter table application_config modify description varchar2(500 char);

alter table session_config add constraint fk_session_config foreign key (session_id) references sessions (uniqueid) on delete cascade;

/*
 * Update database version
 */

update application_config set value='117' where name='tmtbl.db.version';

commit;
