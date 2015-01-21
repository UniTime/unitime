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

create table curriculum_major (
	curriculum_id number(20,0) constraint nn_curriculum_major_cur_id not null,
	major_id number(20,0) constraint nn_curriculum_major_maj_id not null
);

alter table curriculum_major add constraint pk_curriculum_major primary key (curriculum_id, major_id);

alter table curriculum_major add constraint fk_curriculum_major_curriculum foreign key (curriculum_id)
	references curriculum (uniqueid) on delete cascade;

alter table curriculum_major add constraint fk_curriculum_major_major foreign key (major_id)
	references pos_major (uniqueid) on delete cascade;

/**
 * Update database version
 */

update application_config set value='50' where name='tmtbl.db.version';

commit;
