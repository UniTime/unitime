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

create table learn_mgmt_sys_info (
    uniqueid bigint NOT NULL,
    session_id bigint NOT NULL,
    reference varchar(20) NOT NULL,
    label varchar(60) NOT NULL,
	external_uid varchar(40),
    default_lms boolean NOT NULL
);

alter table only learn_mgmt_sys_info 
    add constraint pk_lms_info primary key (uniqueid);
    
create index idx_fk_time_pattern_session ON learn_mgmt_sys_info USING btree (session_id);

alter table only learn_mgmt_sys_info 
	add constraint fk_lms_code_session foreign key (session_id) references sessions (uniqueid) on delete cascade;
	
create unique index uk_lms_info on learn_mgmt_sys_info using btree (session_id, reference);

insert into rights (role_id, value)
	select distinct r.role_id, 'LearningManagementSystemInfos'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'Departments';

insert into rights (role_id, value)
	select distinct r.role_id, 'LearningManagementSystemInfoEdit'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'DepartmentEdit';

alter table class_ add lms_info_id bigint;

create index idx_fk_class_lms_info ON class_ USING btree (lms_info_id);

alter table only class_ add constraint fk_class_lms_info foreign key (lms_info_id)
	references learn_mgmt_sys_info (uniqueid) on delete set null;

/*
 * Update database version
 */

update application_config set value='225' where name='tmtbl.db.version';

commit;
