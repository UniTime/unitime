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

create table travel_time (
		uniqueid number(20,0) constraint nn_trvltime_id not null,
		session_id number(20,0) constraint nn_trvltime_session not null,
		loc1_id number(20,0) constraint nn_trvltime_loc1 not null,
		loc2_id number(20,0) constraint nn_trvltime_loc2 not null,
		distance number(10,0)  constraint nn_trvltime_distance not null
	);

alter table travel_time add constraint pk_travel_time primary key (uniqueid);

alter table travel_time add constraint fk_trvltime_session foreign key (session_id)
	references sessions (uniqueid) on delete cascade;

/*
 * Update database version
 */

update application_config set value='84' where name='tmtbl.db.version';

commit;
