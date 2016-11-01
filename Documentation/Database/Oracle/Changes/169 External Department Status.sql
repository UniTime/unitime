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

create table ext_dept_status (
	ext_dept_id number(20,0) constraint nn_dept_status_ext not null,
	department_id number(20,0)  constraint nn_dept_status_dep not null,
	status_type number(20,0)  constraint nn_dept_status_type not null
);
alter table ext_dept_status add constraint pk_ext_dept_status primary key (ext_dept_id, department_id);

alter table ext_dept_status add constraint fk_dept_status_ext foreign key (ext_dept_id)
	references department (uniqueid) on delete cascade;

alter table ext_dept_status add constraint fk_dept_status_dep foreign key (department_id)
	references department (uniqueid) on delete cascade;

alter table ext_dept_status add constraint fk_dept_status_type foreign key (status_type)
	references dept_status_type (uniqueid) on delete cascade;

/*
 * Update database version
 */

update application_config set value='169' where name='tmtbl.db.version';

commit;
