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
		
create table duration_type (
	uniqueid decimal(20,0) primary key not null,
	reference varchar(20) not null,
	abbreviation varchar(20) not null,
	label varchar(60) not null,
	implementation varchar(255) not null,
	parameter varchar(200),
	visible int(1) not null default 1
) engine = INNODB;

alter table instr_offering_config add duration_type_id decimal(20,0);

alter table instr_offering_config add constraint fk_ioconfig_durtype foreign key (duration_type_id)
	references duration_type (uniqueid) on delete set null;
	
alter table sessions add duration_type_id decimal(20,0);

alter table sessions add constraint fk_session_durtype foreign key (duration_type_id)
	references duration_type (uniqueid) on delete set null;
	
select 32767 * next_hi into @id from hibernate_unique_key;

insert into duration_type
	(uniqueid, reference, abbreviation, label, implementation, parameter, visible) values
	(@id+0, 'MIN_PER_WEEK', 'Mins', 'Minutes per Week', 'org.unitime.timetable.util.duration.MinutesPerWeek', null, 1),
	(@id+1, 'WEEKLY_MIN', 'Wk Mins', 'Average Weekly Minutes', 'org.unitime.timetable.util.duration.WeeklyMinutes', null, 1),
	(@id+2, 'SEMESTER_MIN', 'Sem Mins', 'Semester Minutes', 'org.unitime.timetable.util.duration.SemesterMinutes', null, 1),
	(@id+3, 'SEMESTER_HRS', 'Sem Hrs', 'Semester Hours', 'org.unitime.timetable.util.duration.SemesterHours', '50', 1),
	(@id+4, 'MEETING_MIN', 'Mtg Mins', 'Meeting Minutes', 'org.unitime.timetable.util.duration.MeetingMinutes', '0.95,1.10', 1),
	(@id+5, 'MEETING_HRS', 'Mtg Hrs', 'Meeting Hours', 'org.unitime.timetable.util.duration.MeetingHours', '50,0.95,1.10', 1);

update sessions set duration_type_id = @id;

update hibernate_unique_key set next_hi=next_hi+1;

insert into rights (role_id, value)
	select distinct r.role_id, 'DurationTypes'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'CourseTypes';

insert into rights (role_id, value)
	select distinct r.role_id, 'DurationTypeEdit'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'CourseTypeEdit';

insert into preference_level(pref_id, pref_prolog, pref_name, uniqueid) values (8, 'N', 'Not Available', 8);
	
/*
 * Update database version
 */

update application_config set value='143' where name='tmtbl.db.version';

commit;
