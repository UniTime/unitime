/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
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
	length number(10,0)  constraint nn_exam_period_length not null
);

alter table exam_period
  add constraint pk_exam_period primary key (uniqueid);
  
alter table exam_period
  add constraint fk_exam_period_session foreign key (session_id)
  references sessions (uniqueid) on delete cascade;
  
 
begin
	for s in (select * from sessions) loop
  
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length) 
             values (pref_group_seq.nextval, s.uniqueid, 0, 96, 24);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length) 
             values (pref_group_seq.nextval, s.uniqueid, 0, 124, 24);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length) 
             values (pref_group_seq.nextval, s.uniqueid, 0, 156, 24);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length) 
             values (pref_group_seq.nextval, s.uniqueid, 0, 184, 24);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length) 
             values (pref_group_seq.nextval, s.uniqueid, 0, 228, 24);

      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length) 
             values (pref_group_seq.nextval, s.uniqueid, 1, 96, 24);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length) 
             values (pref_group_seq.nextval, s.uniqueid, 1, 124, 24);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length) 
             values (pref_group_seq.nextval, s.uniqueid, 1, 156, 24);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length) 
             values (pref_group_seq.nextval, s.uniqueid, 1, 184, 24);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length) 
             values (pref_group_seq.nextval, s.uniqueid, 1, 228, 24);

      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length) 
             values (pref_group_seq.nextval, s.uniqueid, 2, 96, 24);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length) 
             values (pref_group_seq.nextval, s.uniqueid, 2, 124, 24);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length) 
             values (pref_group_seq.nextval, s.uniqueid, 2, 156, 24);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length) 
             values (pref_group_seq.nextval, s.uniqueid, 2, 184, 24);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length) 
             values (pref_group_seq.nextval, s.uniqueid, 2, 228, 24);

      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length) 
             values (pref_group_seq.nextval, s.uniqueid, 3, 96, 24);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length) 
             values (pref_group_seq.nextval, s.uniqueid, 3, 124, 24);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length) 
             values (pref_group_seq.nextval, s.uniqueid, 3, 156, 24);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length) 
             values (pref_group_seq.nextval, s.uniqueid, 3, 184, 24);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length) 
             values (pref_group_seq.nextval, s.uniqueid, 3, 228, 24);

      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length) 
             values (pref_group_seq.nextval, s.uniqueid, 4, 96, 24);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length) 
             values (pref_group_seq.nextval, s.uniqueid, 4, 124, 24);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length) 
             values (pref_group_seq.nextval, s.uniqueid, 4, 156, 24);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length) 
             values (pref_group_seq.nextval, s.uniqueid, 4, 184, 24);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length) 
             values (pref_group_seq.nextval, s.uniqueid, 4, 228, 24);

      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length) 
             values (pref_group_seq.nextval, s.uniqueid, 5, 96, 24);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length) 
             values (pref_group_seq.nextval, s.uniqueid, 5, 124, 24);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length) 
             values (pref_group_seq.nextval, s.uniqueid, 5, 156, 24);
      insert into exam_period(uniqueid, session_id, date_ofs, start_slot, length) 
             values (pref_group_seq.nextval, s.uniqueid, 5, 184, 24);
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

commit;
