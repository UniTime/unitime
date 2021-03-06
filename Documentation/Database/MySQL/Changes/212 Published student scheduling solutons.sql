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

create table sct_solution_log (
	uniqueid decimal(20,0) primary key not null,
	session_id decimal(20,0) not null,
	owner_id decimal(20,0) not null,
	time_stamp datetime not null,
	data longblob not null,
	info varchar(2000) not null
) engine = INNODB;
	
alter table sct_solution_log add constraint fk_sct_sol_log_session foreign key (session_id) references sessions (uniqueid) on delete cascade;
alter table sct_solution_log add constraint fk_sct_sol_log_owner foreign key (owner_id) references timetable_manager (uniqueid) on delete cascade;

/*
 * Update database version
 */

update application_config set value='212' where name='tmtbl.db.version';

commit;
