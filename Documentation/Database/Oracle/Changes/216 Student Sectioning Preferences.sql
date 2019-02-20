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

create table sect_pref (
	uniqueid number(20,0) constraint nn_sect_pref_id not null,
	preference_type number(10,0) constraint nn_sect_pref_type not null,
	request_id number(20,0) constraint nn_sect_pref_request not null,
	required number(1) constraint nn_sect_pref_requred not null,
	class_id number(20,0),
	instr_mthd_id number(20,0)
);

alter table sect_pref add constraint pk_sect_pref primary key (uniqueid);

alter table sect_pref add constraint fk_sct_pref_request foreign key (request_id) references course_request (uniqueid) on delete cascade;
alter table sect_pref add constraint fk_sct_pref_class foreign key (class_id) references class_ (uniqueid) on delete cascade;
alter table sect_pref add constraint fk_sct_pref_im foreign key (instr_mthd_id) references instructional_method (uniqueid) on delete cascade;

/*
 * Update database version
 */

update application_config set value='216' where name='tmtbl.db.version';

commit;
