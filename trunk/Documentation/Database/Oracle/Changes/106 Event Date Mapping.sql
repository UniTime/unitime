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

create table date_mapping (
	uniqueid number(20,0) constraint nn_date_map_id not null,
	session_id number(20,0) constraint nn_date_map_session not null,
	class_date number(10,0) constraint nn_date_map_class not null,
	event_date number(10,0) constraint nn_date_map_event not null,
	note varchar2(1000 char)
	);
alter table date_mapping add constraint pk_date_mapping primary key (uniqueid);

alter table date_mapping add constraint fk_event_date_session foreign key (session_id)
	references sessions (uniqueid) on delete cascade;

/*
 * Update database version
 */

update application_config set value='106' where name='tmtbl.db.version';

commit;
