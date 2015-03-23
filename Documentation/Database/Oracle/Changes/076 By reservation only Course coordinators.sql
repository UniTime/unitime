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

alter table instructional_offering add req_reservation number(1) default 0;
alter table instructional_offering add constraint nn_instr_offering_by_reserv check (req_reservation is not null);

create table offering_coordinator (
 	offering_id number(20,0) constraint nn_offering_coord_offering not null,
 	instructor_id number(20,0) constraint nn_offering_coord_instructor not null
);
alter table offering_coordinator add constraint pk_offering_coordinator primary key (offering_id, instructor_id);

alter table offering_coordinator add constraint fk_offering_coord_offering foreign key (offering_id)
	references instructional_offering (uniqueid) on delete cascade;

alter table offering_coordinator add constraint fk_offering_coord_instructor foreign key (instructor_id)
	references departmental_instructor (uniqueid) on delete cascade;

alter table student_class_enrl add approved_date date;

alter table student_class_enrl add approved_by varchar2(40);

/*
 * Update database version
 */

update application_config set value='76' where name='tmtbl.db.version';

commit;
