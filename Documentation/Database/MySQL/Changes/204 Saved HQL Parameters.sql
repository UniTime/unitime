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
	hql_id decimal(20,0) not null,
	name varchar(128) not null,
	label varchar(256) null,
	type varchar(2048) not null,
	default_value varchar(2048) null,
	primary key (hql_id, name)
) engine = INNODB;

alter table hql_parameter add constraint fk_hql_parameter foreign key (hql_id) references saved_hql(uniqueid) on delete cascade;

/*
 * Update database version
 */

update application_config set value='204' where name='tmtbl.db.version';

commit;
