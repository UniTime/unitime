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
		uniqueid decimal(20,0) primary key not null,
		time_stamp datetime not null,
		log_level decimal(10,0) not null,
		message longtext binary null,
		logger varchar(255) not null,
		thread varchar(100) null,
		ndc longtext binary null,
		exception longtext binary null
	) engine = INNODB;

create index idx_message_log on message_log(time_stamp, level);

select 32767 * next_hi into @id from hibernate_unique_key;

insert into saved_hql (uniqueid, name, description, query, type) values
	(@id, 'Message Log', 'Display message log', 'select\n  timeStamp as Time,\n  (case level when 50000 then \'<font color=\"red\">Fatal</font>\' when 40000 then \'<font color=\"red\">Error</font>\' when 30000 then \'<font color=\"orange\">Warning</font>\' when 20000 then \'Info\' when 10000 then \'Debug\' else \'Other\' end) as Level,\n  logger as Logger,\n  (case when exception is null then message when message is null then exception else (message || \'\\\\n\' || exception) end) as Message,\n  (case when ndc is null then thread else (thread || \'\\\\n\' || ndc) end) as Context\nfrom MessageLog order by timeStamp desc', 16),
	(@id + 1, 'Query Log', 'Display query log', 'select\n  m.lastName || \' \' || m.firstName as User,\n  case\n    when q.uri like \'%.gwt: %\' then substring(q.uri, instr(q.uri, \':\') + 1)\n    else q.uri end as Query,\n  case\n    when q.uri like \'%.gwt: %\' and length(q.query) <= 165 + instr(q.query, \'org.unitime.timetable.gwt.services.\')\n      then substring(q.query, instr(q.query, \'org.unitime.timetable.gwt.services.\') + 35)\n    when q.uri like \'%.gwt: %\'\n      then (substring(substring(q.query, instr(q.query, \'org.unitime.timetable.gwt.services.\') + 35), 1, 130) || \'...\')\n    when q.query is null or length(q.query) < 130 then q.query\n    else (substring(q.query, 1, 130) || \'...\') end as Parameters,\n  q.timeStamp as Time_Stamp,\n  q.timeSpent / 1000.0 as Time\nfrom QueryLog q, TimetableManager m\nwhere \n  q.uid = m.externalUniqueId and q.uri not like \'menu.gwt%\'\norder by q.timeStamp desc',16);

update hibernate_unique_key set next_hi=next_hi+1;

/*
 * Update database version
 */

update application_config set value='115' where name='tmtbl.db.version';

commit;
