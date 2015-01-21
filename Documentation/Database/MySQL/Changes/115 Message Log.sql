/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
