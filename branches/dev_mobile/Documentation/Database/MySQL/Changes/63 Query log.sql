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


create table query_log (
	uniqueid decimal(20,0) primary key not null,
	time_stamp datetime not null,
	time_spent decimal(20,0) not null,
	uri varchar(255) not null,
	type decimal(10,0) not null,
	session_id varchar(32) null,
	userid varchar(40) null,
	query longtext binary null,
	exception longtext binary null
) engine = INNODB;

create index idx_query_log on query_log(time_stamp);

/**
 * Update database version
 */

update application_config set value='63' where name='tmtbl.db.version';

commit;
		