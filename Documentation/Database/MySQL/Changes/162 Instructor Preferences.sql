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

create table instructor_pref (
	uniqueid decimal(20,0) primary key not null,
	owner_id decimal(20,0) not null,
	pref_level_id decimal(20,0) not null,
	instructor_id decimal(20,0) not null
) engine = INNODB;

alter table instructor_pref add constraint fk_instructor_pref_pref foreign key (pref_level_id)
	references preference_level (uniqueid) on delete cascade;

alter table instructor_pref add constraint fk_instructor_pref_instructor foreign key (instructor_id)
	references departmental_instructor (uniqueid) on delete cascade;

alter table scheduling_subpart add nbr_instructors int(4);

alter table class_ add nbr_instructors int(4);

alter table class_ add teaching_load float;

update scheduling_subpart set nbr_instructors = 1 where teaching_load is not null;

/*
 * Update database version
 */

update application_config set value='162' where name='tmtbl.db.version';

commit;
