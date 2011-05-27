/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2011, UniTime LLC
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

create table sectioning_log (
	uniqueid number(20,0) constraint nn_section_log_uniqueid not null,
	time_stamp timestamp constraint nn_section_log_time_stamp not null,
	student varchar2(40)  constraint nn_section_log__student not null,
	session_id number(20,0)  constraint nn_section_log_session_id not null,
	operation varchar2(20)  constraint nn_section_log_operation not null,
	action blob constraint nn_section_log_action not null
);

alter table sectioning_log add constraint pk_sectioning_log primary key (uniqueid);

alter table sectioning_log add constraint fk_sectioning_log_session foreign key (session_id)
	references sessions (uniqueid) on delete cascade;
	
create index idx_sectioning_log on sectioning_log(time_stamp, student, session_id, operation);

/**
 * Update database version
 */

update application_config set value='70' where name='tmtbl.db.version';

commit;
		