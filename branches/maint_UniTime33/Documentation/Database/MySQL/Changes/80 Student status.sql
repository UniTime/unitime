/*
 * UniTime 3.3 (University Timetabling Application)
 * Copyright (C) 2008 - 2011, UniTime LLC
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

create table sectioning_status (
		uniqueid decimal(20,0) primary key not null,
		reference varchar(20) not null,
		label varchar(60) not null,
		status bigint(10) not null,
		message varchar(200)
	) engine = INNODB;

alter table student add sect_status decimal(20,0) null;

alter table student
  	add constraint fk_student_sect_status foreign key (sect_status)
  	references sectioning_status (uniqueid) on delete set null;

alter table sessions add sect_status decimal(20,0) null

alter table sessions
  	add constraint fk_session_sect_status foreign key (sect_status)
  	references sectioning_status (uniqueid) on delete set null;

alter table sessions add wk_enroll bigint(10) not null default 1;

alter table sessions add wk_change bigint(10) not null default 1;

alter table sessions add wk_drop bigint(10) not null default 1;

alter table instructional_offering add wk_enroll bigint(10);

alter table instructional_offering add wk_change bigint(10);

alter table instructional_offering add wk_drop bigint(10);

alter table course_demand add changed_by varchar(40) null;

alter table student_class_enrl add changed_by varchar(40) null;

alter table student add schedule_emailed datetime null;

select 32767 * next_hi into @id from hibernate_unique_key;

insert into sectioning_status (uniqueid, reference, label, status, message) values
	(@id + 0, 'Enabled', 'Access enabled', 7, null),
	(@id + 1, 'Disabled', 'Access disabled', 4, null),
	(@id + 2, 'Not Available', 'Temporarily not available', 6, 'Access is temporarily disabled. Please try again later...'),
	(@id + 3, 'No Email', 'Access enabled, no email notification', 3, null);

update hibernate_unique_key set next_hi=next_hi+1;

alter table student drop foreign key fk_student_status_student;
alter table student drop column status_type_id;
alter table student drop column status_change_date;
drop table student_status_type;

/*
 * Update database version
 */

update application_config set value='80' where name='tmtbl.db.version';

commit;
