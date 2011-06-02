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
	uniqueid decimal(20,0) primary key not null,
	time_stamp datetime not null,
	student varchar(40) not null,
	session_id decimal(20,0) not null,
	operation varchar(20) not null,
	action longblob not null
) engine = INNODB;

alter table sectioning_log add constraint fk_sectioning_log_session foreign key (session_id)
	references sessions (uniqueid) on delete cascade;
	
create index idx_sectioning_log on sectioning_log(time_stamp, student, session_id, operation);

/**
 * Update database version
 */

update application_config set value='70' where name='tmtbl.db.version';

commit;
		