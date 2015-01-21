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

-- create event_type table
create table EVENT_TYPE
(
  UNIQUEID  NUMBER(20),
  REFERENCE VARCHAR2(20),
  LABEL     VARCHAR2(60)
);
alter table EVENT_TYPE
  add constraint PK_EVENT_TYPE primary key (UNIQUEID);
alter table EVENT_TYPE
  add constraint NN_EVENT_TYPE_LABEL
  check ("LABEL" IS NOT NULL);
alter table EVENT_TYPE
  add constraint NN_EVENT_TYPE_REFERENCE
  check ("REFERENCE" IS NOT NULL);
alter table EVENT_TYPE
  add constraint NN_EVENT_TYPE_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);

insert into event_type values(ref_table_seq.nextval, 'class', 'Class');
insert into event_type values(ref_table_seq.nextval, 'final', 'Final Exam');
insert into event_type values(ref_table_seq.nextval, 'evening', 'Evening Exam');
insert into event_type values(ref_table_seq.nextval, 'otherWithConflict', 'Other Course Event with Conflict Checking');
insert into event_type values(ref_table_seq.nextval, 'otherNoConflict', 'Other Course Event with No Conflict Checking');
insert into event_type values(ref_table_seq.nextval, 'special', 'Special Event');
 
 -- Create table event_contact
create table event_contact
(
  uniqueid    number(20) constraint nn_event_contact_uniqueid not null,
  external_id varchar2(40),
  email       varchar2(100)  constraint nn_event_contact_email not null,
  phone       varchar2(10)  constraint nn_event_contact_phone not null,
  firstName   varchar2(20),
  middleName  varchar2(20),
  lastName    varchar2(30)
)
;
-- Create/Recreate primary, unique and foreign key constraints 
alter table event_contact
  add constraint pk_event_contact_uniqueid primary key (UNIQUEID);

-- Create event table
create table event
(
  uniqueid       number(20) not null,
  event_type           number(20) not null,
  event_name           varchar2(100),
  min_capacity   number(10),
  max_capacity   number(10),
  sponsoring_org number(20),
  main_contact_id          number(20)
)
;
-- Create/Recreate primary, unique and foreign key constraints 
alter table event
  add constraint pk_event_uniqueid primary key (UNIQUEID);
alter table event
  add constraint fk_event_event_type foreign key (EVENT_TYPE)
  references event_type (UNIQUEID) on delete cascade;
alter table event
  add constraint fk_event_main_contact foreign key (MAIN_CONTACT_ID)
  references event_contact (UNIQUEID) on delete  set null;

-- Create table
create table event_join_event_contact
(
  event_id         number(20) constraint nn_event_join_event_id not null,
  event_contact_id number(20) constraint nn_event_join_event_contact_id not null
)
;
-- Create/Recreate primary, unique and foreign key constraints 
alter table event_join_event_contact
  add constraint fk_event_id_join foreign key (EVENT_ID)
  references event (UNIQUEID) on delete cascade;
alter table event_join_event_contact
  add constraint fk_event_contact_join foreign key (EVENT_CONTACT_ID)
  references event_contact (UNIQUEID) on delete cascade;
  
  
create table related_course_info (
  uniqueid number(20,0) constraint nn_rel_crs_info_unique_id not null,
  event_id number(20,0) constraint nn_rel_crs_info_event_id not null,
  owner_id number(20,0) constraint nn_rel_crs_info_owner_id not null,
  owner_type number(10,0) constraint nn_rel_crs_info_owner_type not null,
  course_id number(20,0) constraint nn_rel_crs_info_course_id not null
)
;

alter table related_course_info add constraint pk_related_crs_info primary key (uniqueid);
alter table related_course_info add constraint fk_event_owner_event foreign key (event_id) references event (uniqueid) on delete cascade;
alter table related_course_info add constraint fk_event_owner_course foreign key (course_id) references course_offering (uniqueid) on delete cascade;

create index idx_event_owner_event on related_course_info(event_id);
create index idx_event_owner_owner on related_course_info(owner_id, owner_type);

-- Create table
create table meeting
(
  uniqueid           number(20) constraint nn_meeting_uniqueid not null,
  event_id           number(20) constraint nn_meeting_event_id not null,
  event_type         number(20) constraint nn_meeting_event_type not null,
  meeting_date       date constraint nn_meeting_date not null,
  start_period       number(10) constraint nn_meeting_start_period not null,
  start_offset       number(10),
  stop_period        number(10) constraint nn_meeting_stop_period not null,
  stop_offset        number(10),
  location_perm_id   number(20),
  class_can_override number(1) constraint nn_meeting_override not null,
  approved_date      date
)
;
-- Create/Recreate primary, unique and foreign key constraints 
alter table meeting
  add constraint pk_meeting_uniqueid primary key (UNIQUEID);
alter table meeting
  add constraint fk_meeting_event foreign key (EVENT_ID)
  references event (UNIQUEID) on delete cascade;
alter table meeting
  add constraint fk_meeting_event_type foreign key (EVENT_TYPE)
  references event_type (UNIQUEID) on delete cascade;
  
 -- create event_note table
create table STANDARD_EVENT_NOTE
(
  UNIQUEID  NUMBER(20),
  REFERENCE VARCHAR2(20),
  NOTE     VARCHAR2(1000)
);
alter table STANDARD_EVENT_NOTE
  add constraint PK_STANDARD_EVENT_NOTE primary key (UNIQUEID);
alter table STANDARD_EVENT_NOTE
  add constraint NN_STD_EVENT_NOTE_NOTE
  check ("NOTE" IS NOT NULL);
alter table STANDARD_EVENT_NOTE
  add constraint NN_STD_EVENT_NOTE_REFERENCE
  check ("REFERENCE" IS NOT NULL);
alter table STANDARD_EVENT_NOTE
  add constraint NN_STD_EVENT_NOTE_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
 
-- Create table event_note
create table event_note
(
  uniqueid  number(20) constraint nn_event_note_uniqueid not null,
  event_id  number(20) constraint nn_event_note_event_uniqueid not null,
  note_id   number(20),
  text_note varchar2(1000)
)
;
-- Create/Recreate primary, unique and foreign key constraints 
alter table event_note
  add constraint pk_event_note_uniqueid primary key (UNIQUEID);
alter table event_note
  add constraint fk_event_note_event foreign key (EVENT_ID)
  references event (UNIQUEID) on delete cascade;
alter table event_note
  add constraint fk_event_note_std_note foreign key (NOTE_ID)
  references standard_event_note (UNIQUEID) on delete set null;
  
/*
 * Update database version
 */

update application_config set value='19' where name='tmtbl.db.version';


commit;
