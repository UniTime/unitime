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

/*
 * Table DISTRIBUTION_TYPE, add column exam_pref (boolean)
 */
 
alter table distribution_type add exam_pref number(1) default 0;

/*
 * Added few exam distribution types (same period, same room, precedence)
 */

insert into distribution_type (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref) values 
	(ref_table_seq.nextval, 'EX_SAME_PER', 'Same Period', 0, 36, 'P43210R', 'Exams are to be placed at the same period. <BR>When prohibited or (strongly) discouraged: exams are to be placed at different periods.', 'Same Per', 0, 1); 

insert into distribution_type (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref) values 
	(ref_table_seq.nextval, 'EX_SAME_ROOM', 'Same Room', 0, 37, 'P43210R', 'Exams are to be placed at the same room(s). <BR>When prohibited or (strongly) discouraged: exams are to be placed at different rooms.', 'Same Room', 0, 1); 
 
insert into distribution_type (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref) values 
	(ref_table_seq.nextval, 'EX_PRECEDENCE', 'Precedence', 1, 38, 'P43210R', 'Exams are to be placed in the given order. <BR>When prohibited or (strongly) discouraged: exams are to be placed in the order reverse to the given one.', 'Precede', 0, 1); 

 /*
 * Add exam start date to sessions
 */
 
alter table sessions add exam_begin_date date;

update sessions set exam_begin_date = session_begin_date_time+112;

alter table sessions add constraint nn_sessions_exam_begin_date check (exam_begin_date is not null);
 
/*
 * Create table exam_period
 */

create table exam_period (
	uniqueid number(20,0) constraint nn_exam_period_uniqueid not null,
	session_id number(20,0) constraint nn_exam_period_session not null,
	date_ofs number(10,0)  constraint nn_exam_period_date_ofs not null,
	start_slot number(10,0)  constraint nn_exam_period_start_slot not null,
	length number(10,0)  constraint nn_exam_period_length not null,
	pref_level_id number(20,0) constraint nn_exam_period_pref not null
);

alter table exam_period
  add constraint pk_exam_period primary key (uniqueid);
  
alter table exam_period
  add constraint fk_exam_period_session foreign key (session_id)
  references sessions (uniqueid) on delete cascade;
  
alter table exam_period
  add constraint fk_exam_period_pref foreign key (pref_level_id)
  references preference_level (uniqueid) on delete cascade;
 
begin
	for s in (select * from sessions) loop
  
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length, pref_level_id) 
             values (pref_group_seq.nextval, s.uniqueid, 0, 96, 24, 4);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length, pref_level_id) 
             values (pref_group_seq.nextval, s.uniqueid, 0, 124, 24, 4);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length, pref_level_id) 
             values (pref_group_seq.nextval, s.uniqueid, 0, 156, 24, 4);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length, pref_level_id) 
             values (pref_group_seq.nextval, s.uniqueid, 0, 184, 24, 4);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length, pref_level_id) 
             values (pref_group_seq.nextval, s.uniqueid, 0, 228, 24, 4);

      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length, pref_level_id) 
             values (pref_group_seq.nextval, s.uniqueid, 1, 96, 24, 4);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length, pref_level_id) 
             values (pref_group_seq.nextval, s.uniqueid, 1, 124, 24, 4);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length, pref_level_id) 
             values (pref_group_seq.nextval, s.uniqueid, 1, 156, 24, 4);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length, pref_level_id) 
             values (pref_group_seq.nextval, s.uniqueid, 1, 184, 24, 4);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length, pref_level_id) 
             values (pref_group_seq.nextval, s.uniqueid, 1, 228, 24, 4);

      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length, pref_level_id) 
             values (pref_group_seq.nextval, s.uniqueid, 2, 96, 24, 4);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length, pref_level_id) 
             values (pref_group_seq.nextval, s.uniqueid, 2, 124, 24, 4);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length, pref_level_id) 
             values (pref_group_seq.nextval, s.uniqueid, 2, 156, 24, 4);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length, pref_level_id) 
             values (pref_group_seq.nextval, s.uniqueid, 2, 184, 24, 4);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length, pref_level_id) 
             values (pref_group_seq.nextval, s.uniqueid, 2, 228, 24, 4);

      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length, pref_level_id) 
             values (pref_group_seq.nextval, s.uniqueid, 3, 96, 24, 4);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length, pref_level_id) 
             values (pref_group_seq.nextval, s.uniqueid, 3, 124, 24, 4);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length, pref_level_id) 
             values (pref_group_seq.nextval, s.uniqueid, 3, 156, 24, 4);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length, pref_level_id) 
             values (pref_group_seq.nextval, s.uniqueid, 3, 184, 24, 4);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length, pref_level_id) 
             values (pref_group_seq.nextval, s.uniqueid, 3, 228, 24, 4);

      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length, pref_level_id) 
             values (pref_group_seq.nextval, s.uniqueid, 4, 96, 24, 4);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length, pref_level_id) 
             values (pref_group_seq.nextval, s.uniqueid, 4, 124, 24, 4);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length, pref_level_id) 
             values (pref_group_seq.nextval, s.uniqueid, 4, 156, 24, 4);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length, pref_level_id) 
             values (pref_group_seq.nextval, s.uniqueid, 4, 184, 24, 4);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length, pref_level_id) 
             values (pref_group_seq.nextval, s.uniqueid, 4, 228, 24, 4);

      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length, pref_level_id) 
             values (pref_group_seq.nextval, s.uniqueid, 5, 96, 24, 4);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length, pref_level_id) 
             values (pref_group_seq.nextval, s.uniqueid, 5, 124, 24, 4);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length, pref_level_id) 
             values (pref_group_seq.nextval, s.uniqueid, 5, 156, 24, 4);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length, pref_level_id) 
             values (pref_group_seq.nextval, s.uniqueid, 5, 184, 24, 4);
  end loop;
end;
/
  
/*
  * Rooms, and non-university locations -- added columns exam_enable (boolean), exam_capacity (exam seating capacity, int), exam_pref (exam period preferences)
  */
  
alter table room add exam_enable number(1) default 0;
alter table room add exam_capacity number(10) default 0;
alter table room add exam_pref varchar2(1000);

update room set exam_capacity = capacity /2;

alter table non_university_location add exam_enable number(1) default 0;
alter table non_university_location add exam_capacity number(10) default 0;
alter table non_university_location add exam_pref varchar2(1000);


/*
 * Create exam manager role
 */

insert into roles (role_id, reference, abbv) values
	(ROLE_SEQ.nextval, 'Exam Mgr', 'Examination Timetabling Manager');

/*
 * Create table exam
 */

create table exam (
	uniqueid number(20,0) constraint nn_exam_uniqueid not null,
	session_id number(20,0) constraint nn_exam_session not null,
	name varchar2(100),
	note varchar2(1000),
	length number(10,0) constraint nn_exam_length not null,
	max_nbr_rooms number(10,0) default 1 constraint nn_exam_nbr_rooms not null,
	seating_type number(10,0) constraint nn_exam_seating not null,
	assigned_period number (20,0)
);

alter table exam
  add constraint pk_exam primary key (uniqueid);
  
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
	uniqueid number(20,0) constraint nn_exam_owner_unique_id not null,
	exam_id number(20,0) constraint nn_exam_owner_exam_id not null,
	owner_id number(20,0) constraint nn_exam_owner_owner_id not null,
	owner_type number(10,0) constraint nn_exam_owner_owner_type not null
);

alter table exam_owner
  add constraint pk_exam_owner primary key (uniqueid);
  
alter table exam_owner
  add constraint fk_exam_owner_exam foreign key (exam_id)
  references exam (uniqueid) on delete cascade;

create index idx_exam_owner_exam on exam_owner(exam_id);

create index idx_exam_owner_owner on exam_owner(owner_id, owner_type);

/*
 * Create table exam_room_assignment (relation exam - location)
 */

create table exam_room_assignment (
	exam_id number(20,0) constraint nn_exam_room_exam_id not null,
	location_id number(20,0) constraint nn_exam_room_location_id not null
);

alter table exam_room_assignment
  add constraint pk_exam_room_assignment primary key (exam_id, location_id);
  
alter table exam_room_assignment
  add constraint fk_exam_room_exam foreign key (exam_id)
  references exam (uniqueid) on delete cascade;

/*
 * Added exam period preferences
 */

create table exam_period_pref (
	uniqueid number(20,0) constraint nn_exam_period_pref_uniqueid not null,
	owner_id number(20,0) constraint nn_exam_period_pref_owner not null,
	pref_level_id number(20,0) constraint nn_exam_period_pref_pref not null,
	period_id number(20,0) constraint nn_exam_period_pref_period not null
);

alter table exam_period_pref
  add constraint pk_exam_period_pref primary key (uniqueid);
  
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
	exam_id number(20,0) constraint nn_exam_instructor_exam not null,
	instructor_id number(20,0) constraint nn_exam_instructor_instructor not null
);

alter table exam_instructor
  add constraint pk_exam_instructor primary key (exam_id, instructor_id);

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
