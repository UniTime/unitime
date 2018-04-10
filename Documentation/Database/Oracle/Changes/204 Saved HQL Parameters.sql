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

create table hql_parameter (
	hql_id number(20,0) constraint nn_script_param_id not null,
	name varchar2(128 char) constraint nn_script_param_name not null,
	label varchar2(256 char),
	type varchar2(2048 char) constraint nn_script_param_type not null,
	default_value varchar2(2048 char)
);
alter table hql_parameter add constraint pk_hql_parameter primary key (hql_id, name);

alter table hql_parameter add constraint fk_hql_parameter foreign key (hql_id) references saved_hql(uniqueid) on delete cascade;

/*
 * Update database version
 */

update application_config set value='204' where name='tmtbl.db.version';

commit;
