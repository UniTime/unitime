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

alter table instructional_offering add req_reservation int(1) not null default 0;

create table offering_coordinator (
	offering_id decimal(20,0) not null,
	instructor_id decimal(20,0) not null,
	primary key (offering_id, instructor_id)
) engine = INNODB;

alter table offering_coordinator add constraint fk_offering_coord_offering foreign key (offering_id)
	references instructional_offering (uniqueid) on delete cascade;

alter table offering_coordinator add constraint fk_offering_coord_instructor foreign key (instructor_id)
	references departmental_instructor (uniqueid) on delete cascade;

alter table student_class_enrl add approved_date datetime null;

alter table student_class_enrl add approved_by varchar(40) null;

/*
 * Update database version
 */

update application_config set value='76' where name='tmtbl.db.version';

commit;
