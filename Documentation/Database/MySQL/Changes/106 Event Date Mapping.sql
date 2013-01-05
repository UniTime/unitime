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
	uniqueid decimal(20,0) primary key not null,
	session_id decimal(20,0) not null,
	class_date bigint(10)  not null,
	event_date bigint(10)  not null,
	note varchar(1000)
) engine = INNODB;

alter table date_mapping add constraint fk_event_date_session foreign key (session_id)
	references sessions (uniqueid) on delete cascade;

/*
 * Update database version
 */

update application_config set value='106' where name='tmtbl.db.version';

commit;
