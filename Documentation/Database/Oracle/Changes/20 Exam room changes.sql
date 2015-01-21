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

alter table room add exam_type number(10,0) default 0;
alter table non_university_location add exam_type number(10,0) default 0;

update room set exam_type = 1 where exam_enable = 1;
update non_university_location set exam_type = 1 where exam_enable = 1;

alter table room drop column exam_enable;
alter table non_university_location drop column exam_enable;

alter table room drop column exam_pref;
alter table non_university_location drop column exam_pref;

create table exam_location_pref (
	uniqueid number(20,0) constraint nn_exam_location_pref_uniqueid not null,
	location_id number(20,0) constraint nn_exam_location_pref_owner not null,
	pref_level_id number(20,0) constraint nn_exam_location_pref_pref not null,
	period_id number(20,0) constraint nn_exam_location_pref_period not null
);

alter table exam_location_pref
  add constraint pk_exam_location_pref primary key (uniqueid);
  
alter table exam_location_pref
  add constraint fk_exam_location_pref_pref foreign key (pref_level_id)
  references preference_level (uniqueid) on delete cascade;
  
  alter table exam_location_pref
  add constraint fk_exam_location_pref_period foreign key (period_id)
  references exam_period (uniqueid) on delete cascade;
  
create index idx_exam_location_pref on exam_location_pref(location_id);
  
/*
 * Update database version
 */

update application_config set value='20' where name='tmtbl.db.version';

commit;
  
