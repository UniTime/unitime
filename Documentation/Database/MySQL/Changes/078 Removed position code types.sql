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

alter table staff add pos_type decimal(20,0) null;

alter table staff add constraint fk_staff_pos_type foreign key (pos_type)
	references position_type(uniqueid) on delete set null;

update staff s set s.pos_type =
	(select c.pos_code_type from position_code_to_type c where s.pos_code = c.position_code)
	where s.pos_type is not null;

drop table position_code_to_type;

alter table offr_consent_type add abbv varchar(20);

update offr_consent_type set reference = 'IN' where reference = 'instructor';
update offr_consent_type set reference = 'DP' where reference = 'department';
update offr_consent_type set abbv = 'Instructor' where reference = 'instructor' or reference = 'IN';
update offr_consent_type set abbv = 'Department' where reference = 'department' or reference = 'DP';

alter table offr_consent_type add constraint nn_offr_consent_abbv check (abbv is not null);

/*
 * Update database version
 */

update application_config set value='78' where name='tmtbl.db.version';

commit;
