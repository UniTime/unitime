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
 * Add room type table
 **/
 
create table room_type (
	uniqueid number(20,0) constraint nn_room_type_uid not null,
	reference varchar2(20) constraint nn_room_type_ref not null,
	label varchar2(60) constraint nn_room_type_label not null,
	ord number(10,0) constraint nn_room_type_ord not null
);

alter table room_type
  add constraint pk_room_type primary key (uniqueid);


/**
 * Populate room types
 **/

insert into room_type(uniqueid, reference, label, ord) values (ref_table_seq.nextval, 'genClassroom', 'Classrooms', 0);
insert into room_type(uniqueid, reference, label, ord) values (ref_table_seq.nextval, 'computingLab', 'Computing Laboratories', 1);
insert into room_type(uniqueid, reference, label, ord) values (ref_table_seq.nextval, 'departmental', 'Additional Instructional Rooms', 2);
insert into room_type(uniqueid, reference, label, ord) values (ref_table_seq.nextval, 'specialUse', 'Special Use Rooms', 3);

/**
 * Update rooms
 **/
 
alter table room add room_type number(20,0);
 
update room r set r.room_type = (select t.uniqueid from room_type t where t.reference=r.scheduled_room_type);
 
alter table room drop column scheduled_room_type;

alter table room add constraint nn_room_type check  (room_type is not null);
 
alter table room add constraint fk_room_type foreign key (room_type)
  references room_type (uniqueid) on delete cascade;
  
 
/**
 * Create room_type attribute of external_room table
 **/
 
alter table external_room add room_type number(20,0);
 
update external_room r set r.room_type = (select t.uniqueid from room_type t where t.reference=r.scheduled_room_type);
 
alter table external_room drop column scheduled_room_type;

alter table external_room add constraint nn_external_room_type check  (room_type is not null);
 
alter table external_room add constraint fk_external_room_type foreign key (room_type)
  references room_type (uniqueid) on delete cascade;
   
/**
 * Add room_type_options table (session related)
 **/
 
create table room_type_option (
	room_type number(20,0) constraint nn_rtype_opt_type not null,
	session_id number(20,0) constraint nn_rtype_opt_session not null,
	status number(10,0) constraint nn_rtype_opt_status not null,
	message varchar2(200)
);

alter table room_type_option
  add constraint pk_room_type_option primary key (room_type, session_id);



alter table room_type_option add constraint fk_rtype_option_type foreign key (room_type)
  references room_type (uniqueid) on delete cascade;
  
alter table room_type_option add constraint fk_rtype_option_session foreign key (session_id)
  references sessions (uniqueid) on delete cascade;


/**
 * Update database version
 */

update application_config set value='30' where name='tmtbl.db.version';

commit;
