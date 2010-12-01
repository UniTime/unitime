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

/**
 * Event table -- add new_event_type, class_id, exam_id columns (and appropriate foreign keys) 
 */
 
alter table event add class_id decimal(20,0);
alter table event add exam_id decimal(20,0);
alter table event add new_event_type bigint(10);
alter table event add req_attd int(1); 
 
alter table event
  add constraint fk_event_class foreign key (class_id)
  references class_ (uniqueid) on delete cascade;
  
 alter table event
  add constraint fk_event_exam foreign key (exam_id)
  references exam (uniqueid) on delete cascade;
  
/**
 * Populate new_event_type
 **/ 
 
update event e set e.new_event_type = 0 where e.event_type = (select t.uniqueid from event_type t where t.reference = 'class'); 
update event e set e.new_event_type = 1 where e.event_type = (select t.uniqueid from event_type t where t.reference = 'final');
update event e set e.new_event_type = 2 where e.event_type = (select t.uniqueid from event_type t where t.reference = 'evening');
update event e set e.new_event_type = 3, e.req_attd=1 where e.event_type = (select t.uniqueid from event_type t where t.reference = 'otherWithConflict');
update event e set e.new_event_type = 3, e.req_attd=0 where e.event_type = (select t.uniqueid from event_type t where t.reference = 'otherNoConflict');
update event e set e.new_event_type = 4 where e.event_type = (select t.uniqueid from event_type t where t.reference = 'special');

/**
 * Swap new_event_type for event_type and drop event_type table
 **/
 
alter table event drop foreign key fk_event_event_type;
alter table event drop column event_type;
alter table event change column new_event_type event_type bigint(10) not null;
alter table meeting drop foreign key fk_meeting_event_type;
alter table meeting drop column event_type;
drop table event_type;

/**
 * Populate exam id for examination events
 **/
 
update event e set e.exam_id = (select x.uniqueid from exam x where x.event_id=e.uniqueid) where e.event_type in (1, 2);
update event e set e.class_id = (select a.class_id from assignment a where a.event_id=e.uniqueid) where e.event_type = 0;

/**
 * Delete redundant related course info
 **/

delete from related_course_info where event_id in (select e.uniqueid from event e where e.event_type != 3);

/** 
 * Delete event_id from exams and assignments 
 **/
 
alter table assignment drop foreign key fk_assignment_event;
alter table assignment drop column event_id;
alter table exam drop foreign key fk_exam_event;
alter table exam drop column event_id;

/*
 * Update database version
 */

update application_config set value='26' where name='tmtbl.db.version';

commit;  
