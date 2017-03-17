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

create table pit_offering_coord (
	uniqueid number(20,0) primary key not null,
	pit_offering_id number(20,0) not null,
	pit_dept_instr_id number(20,0) not null,
	responsibility_id number(20,0) default null,
	percent_share number(3,0) not null
);

alter table pit_offering_coord add constraint fk_pit_ofr_coord_pit_offr foreign key (pit_offering_id)
	references pit_instr_offering (uniqueid) on delete cascade;
alter table pit_offering_coord add constraint fk_pit_ofr_coord_pit_dept_inst foreign key (pit_dept_instr_id)
	references pit_dept_instructor (uniqueid) on delete cascade;
alter table pit_offering_coord add constraint fk_pit_coord_resp foreign key (responsibility_id)
	references teaching_responsibility (uniqueid) on delete set null;
	
/*
 * Update database version
 */

update application_config set value='178' where name='tmtbl.db.version';

commit;
