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

/**
 * Event table -- add new_event_type, class_id, exam_id columns (and appropriate foreign keys) 
 */
 
alter table event add class_id number(20,0);
alter table event add exam_id number(20,0);
alter table event add new_event_type number(10,0);
alter table event add req_attd number(1,0); 
 
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
 
alter table event drop constraint fk_event_event_type;
alter table event drop column event_type;
alter table event rename column new_event_type to event_type;
alter table event add constraint nn_event_type check (event_type is not null);
alter table meeting drop constraint fk_meeting_event_type;
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
 
alter table assignment drop constraint fk_assignment_event;
alter table assignment drop column event_id;
alter table exam drop constraint fk_exam_event;
alter table exam drop column event_id;

/*
 * Update database version
 */

update application_config set value='26' where name='tmtbl.db.version';

commit;  
