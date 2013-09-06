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

create table message_log (
		uniqueid number(20,0) constraint nn_message_log_id not null,
		time_stamp timestamp constraint nn_message_log_ts not null,
		log_level decimal(10,0) constraint nn_message_log_level not null,
		message clob,
		logger varchar2(255 char) constraint nn_message_log_logger not null,
		thread varchar2(255 char),
		ndc clob,
		exception clob
	);

alter table message_log add constraint pk_message_log primary key (uniqueid);

create index idx_message_log on message_log(time_stamp, log_level);

insert into saved_hql (uniqueid, name, description, query, type) values
	(pref_group_seq.nextval, 'Message Log', 'Display message log', 'select timeStamp as Time, (case level when 50000 then ''<font color="red">Fatal</font>'' when 40000 then ''<font color="red">Error</font>'' when 30000 then ''<font color="orange">Warning</font>'' when 20000 then ''Info'' when 10000 then ''Debug'' else ''Other'' end) as Level, logger as Logger, (case when exception is null then to_char(message) when message is null then to_char(exception) else (to_char(message) || ''\n'' || to_char(exception)) end) as Message, (case when ndc is null then thread else (thread || ''\n'' || to_char(ndc)) end) as Context from MessageLog order by timeStamp desc', 16);

insert into saved_hql (uniqueid, name, description, query, type) values
	(pref_group_seq.nextval, 'Query Log', 'Display query log', 'select m.lastName || '' '' || m.firstName as User, case when q.uri like ''%.gwt: %'' then substring(q.uri, instr(q.uri, '':'') + 1) else q.uri end as Query, case when q.uri like ''%.gwt: %'' and length(q.query) <= 130 + instr(q.query, ''|'', 1, 7) then substring(q.query, instr(q.query, ''|'', 1, 7) + 1, length(q.query) - instr(q.query, ''|'', 1, 7) - 3) when q.uri like ''%.gwt: %'' then (substring(q.query, instr(q.query, ''|'', 1, 7) + 1, 130) || ''...'') when q.query is null or length(q.query) < 130 then q.query else (substring(q.query, 1, 130) || ''...'') end as Parameters, q.timeStamp as Time_Stamp, q.timeSpent / 1000.0 as Time from QueryLog q, TimetableManager m where q.uid = m.externalUniqueId and q.uri not like ''menu.gwt%'' order by q.timeStamp desc',16);

/*
 * Update database version
 */

update application_config set value='115' where name='tmtbl.db.version';

commit;
