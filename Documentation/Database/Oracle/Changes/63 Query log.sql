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
	uniqueid number(20,0) constraint nn_query_log_uniqueid not null,
	time_stamp date constraint nn_query_log_time_stamp not null,
	time_spent number(20,0) constraint nn_query_log_time_spent not null,
	uri varchar2(255) constraint nn_query_log_uri not null,
	type decimal(10,0) constraint nn_query_log_type not null,
	session_id varchar2(32),
	userid varchar2(40),
	query clob,
	exception clob
);

alter table query_log add constraint pk_query_log primary key (uniqueid);

create index idx_query_log on query_log(time_stamp, type);

/**
 * Update database version
 */

update application_config set value='63' where name='tmtbl.db.version';

commit;
		