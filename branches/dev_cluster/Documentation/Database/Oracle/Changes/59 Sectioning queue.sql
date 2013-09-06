/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC
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

create table sectioning_queue (
	uniqueid number(20,0) constraint nn_sect_queue_uniqueid not null,
	session_id number(20,0) constraint nn_sect_queue_session not null,
	type number(10) constraint nn_sect_queue_type not null,
	time_stamp timestamp constraint nn_sect_queue_ts not null,
	message clob
);

alter table sectioning_queue add constraint pk_sect_queue primary key (uniqueid);

create index idx_sect_queue_session_ts on sectioning_queue(session_id, time_stamp);
		
/**
 * Update database version
 */

update application_config set value='59' where name='tmtbl.db.version';

commit;
		