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

create table sectioning_log (
	uniqueid number(20,0) constraint nn_section_log_uniqueid not null,
	time_stamp timestamp constraint nn_section_log_time_stamp not null,
	student varchar2(40)  constraint nn_section_log__student not null,
	session_id number(20,0)  constraint nn_section_log_session_id not null,
	operation varchar2(20)  constraint nn_section_log_operation not null,
	action blob constraint nn_section_log_action not null
);

alter table sectioning_log add constraint pk_sectioning_log primary key (uniqueid);

alter table sectioning_log add constraint fk_sectioning_log_session foreign key (session_id)
	references sessions (uniqueid) on delete cascade;
	
create index idx_sectioning_log on sectioning_log(time_stamp, student, session_id, operation);

/**
 * Update database version
 */

update application_config set value='70' where name='tmtbl.db.version';

commit;
		