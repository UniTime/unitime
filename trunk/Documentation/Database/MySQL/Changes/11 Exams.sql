/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC
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

use timetable;

/*
 * Table DISTRIBUTION_TYPE, add column exam_pref (boolean)
 */

alter table distribution_type add exam_pref int(1) default 0;

/*
 * Added few exam distribution types (same period, same room, precedence)
 */
select 32767 * next_hi into @id from hibernate_unique_key;

insert into distribution_type (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref) values 
	(@id, 'EX_SAME_PER', 'Same Period', 0, 36, 'P43210R', 'Exams are to be placed at the same period. <BR>When prohibited or (strongly) discouraged: exams are to be placed at different periods.', 'Same Per', 0, 1), 
	(@id+1, 'EX_SAME_ROOM', 'Same Room', 0, 37, 'P43210R', 'Exams are to be placed at the same room(s). <BR>When prohibited or (strongly) discouraged: exams are to be placed at different rooms.', 'Same Room', 0, 1), 
	(@id+2, 'EX_PRECEDENCE', 'Precedence', 1, 38, 'P43210R', 'Exams are to be placed in the given order. <BR>When prohibited or (strongly) discouraged: exams are to be placed in the order reverse to the given one.', 'Precede', 0, 1); 

update hibernate_unique_key set next_hi=next_hi+1;

 /*
 * Add exam start date to sessions
 */
 
alter table sessions add exam_begin_date datetime;

update sessions set exam_begin_date = adddate(session_begin_date_time,112);

alter table sessions add constraint nn_sessions_exam_begin_date check (exam_begin_date is not null);
 
/*
 * Create table exam_period
 */

create table exam_period (
	uniqueid decimal(20,0) primary key not null,
	session_id decimal(20,0) not null,
	date_ofs bigint(10)  not null,
	start_slot bigint(10)  not null,
	length bigint(10)  not null,
	pref_level_id decimal(20,0) not null
) engine = INNODB;
  
alter table exam_period
  add constraint fk_exam_period_session foreign key (session_id)
  references sessions (uniqueid) on delete cascade;
  
alter table exam_period
  add constraint fk_exam_period_pref foreign key (pref_level_id)
  references preference_level (uniqueid) on delete cascade;
 
/*
  * Rooms, and non-university locations -- added columns exam_enable (boolean), exam_capacity (exam seating capacity, int), exam_pref (exam period preferences)
  */
  
alter table room add exam_enable int(1) default 0;
alter table room add exam_capacity bigint(10) default 0;
alter table room add exam_pref varchar(1000);

update room set exam_capacity = capacity /2;

alter table non_university_location add exam_enable int(1) default 0;
alter table non_university_location add exam_capacity bigint(10) default 0;
alter table non_university_location add exam_pref varchar(1000);


/*
 * Create exam manager role
 */

insert into roles (role_id, reference, abbv) values
	(@id+3, 'Exam Mgr', 'Examination Timetabling Manager');

/*
 * Create table exam
 */

create table exam (
	uniqueid decimal(20,0) primary key not null,
	session_id decimal(20,0) not null,
	name varchar(100),
	note varchar(1000),
	length bigint(10) not null,
	max_nbr_rooms bigint(10) default 1 not null,
	seating_type bigint(10) not null,
	assigned_period decimal(20,0)
) engine = INNODB;

alter table exam
  add constraint fk_exam_session foreign key (session_id)
  references sessions (uniqueid) on delete cascade;

alter table exam
  add constraint fk_exam_period foreign key (assigned_period)
  references exam_period (uniqueid) on delete cascade;
 

/*
 * Create relation between exams and other objects (classes, configs, courses etc.)
 */

create table exam_owner (
	uniqueid decimal(20,0) primary key not null,
	exam_id decimal(20,0) not null,
	owner_id decimal(20,0) not null,
	owner_type bigint(10) not null
) engine = INNODB;
  
alter table exam_owner
  add constraint fk_exam_owner_exam foreign key (exam_id)
  references exam (uniqueid) on delete cascade;

create index idx_exam_owner_exam on exam_owner(exam_id);

create index idx_exam_owner_owner on exam_owner(owner_id, owner_type);

/*
 * Create table exam_room_assignment (relation exam - location)
 */

create table exam_room_assignment (
	exam_id decimal(20,0) not null,
	location_id decimal(20,0) not null,
	primary key (exam_id, location_id)
) engine = INNODB;

alter table exam_room_assignment
  add constraint fk_exam_room_exam foreign key (exam_id)
  references exam (uniqueid) on delete cascade;

/*
 * Added exam period preferences
 */

create table exam_period_pref (
	uniqueid decimal(20,0) primary key not null,
	owner_id decimal(20,0) not null,
	pref_level_id decimal(20,0) not null,
	period_id decimal(20,0) not null
) engine = INNODB;

alter table exam_period_pref
  add constraint fk_exam_period_pref_pref foreign key (pref_level_id)
  references preference_level (uniqueid) on delete cascade;

alter table exam_period_pref
  add constraint fk_exam_period_pref_period foreign key (period_id)
  references exam_period (uniqueid) on delete cascade;
  
/*
 * Instructor assignment
 */
 
create table exam_instructor (
	exam_id decimal(20,0) primary key not null,
	instructor_id decimal(20,0) not null
) engine = INNODB;

alter table exam_instructor
  add constraint fk_exam_instructor_exam foreign key (exam_id)
  references exam (uniqueid) on delete cascade;
  
alter table exam_instructor
  add constraint fk_exam_instructor_instructor foreign key (instructor_id)
  references departmental_instructor (uniqueid) on delete cascade;

/*
 * Update database version
 */

update application_config set value='11' where name='tmtbl.db.version';

commit;
