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

create table date_pattern_pref (
	uniqueid number(20,0) constraint nn_date_patt_pref_uniqueid not null,
	owner_id number(20,0) constraint nn_date_patt_pref_owner not null,
	pref_level_id number(20,0) constraint nn_date_patt_pref_pref_level not null,
	date_pattern_id number(20,0) constraint nn_date_patt_pref_date_pat not null
	);
alter table date_pattern_pref add constraint pk_date_pattern_pref primary key (uniqueid);

create table date_pattern_parent (
	date_pattern_id number(20,0) constraint nn_datepattparent_datepatt_id not null,
	parent_id number(20,0) constraint nn_date_patt_parent_parent_id not null
	);

alter table date_pattern_parent add constraint pk_date_pattern_parent primary key (date_pattern_id,parent_id);

alter table date_pattern_pref add constraint fk_datepatt_pref_pref_level foreign key (pref_level_id)
	references preference_level (uniqueid) on delete cascade;

alter table date_pattern_pref add constraint fk_datepatt_pref_date_pat foreign key (date_pattern_id)
	references date_pattern (uniqueid) on delete cascade;

alter table date_pattern_parent add constraint fk_date_patt_parent_parent foreign key (parent_id)
	references date_pattern (uniqueid) on delete cascade;

alter table date_pattern_parent add constraint fk_date_patt_parent_date_patt foreign key (date_pattern_id)
	references date_pattern (uniqueid) on delete cascade;

/*
 * Update database version
 */

update application_config set value='85' where name='tmtbl.db.version';

commit;
