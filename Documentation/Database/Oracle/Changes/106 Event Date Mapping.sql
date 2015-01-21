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

create table date_mapping (
	uniqueid number(20,0) constraint nn_date_map_id not null,
	session_id number(20,0) constraint nn_date_map_session not null,
	class_date number(10,0) constraint nn_date_map_class not null,
	event_date number(10,0) constraint nn_date_map_event not null,
	note varchar2(1000 char)
	);
alter table date_mapping add constraint pk_date_mapping primary key (uniqueid);

alter table date_mapping add constraint fk_event_date_session foreign key (session_id)
	references sessions (uniqueid) on delete cascade;

/*
 * Update database version
 */

update application_config set value='106' where name='tmtbl.db.version';

commit;
