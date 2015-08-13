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

create table hashed_queries (
	query_hash varchar2(48 char) constraint nn_hashed_queries_hash not null,
	query_text varchar2(2048 char) constraint nn_hashed_queries_query not null,
	ts_create timestamp constraint nn_hashed_queries_create not null,
	nbr_use number(20) default 0,
	ts_use timestamp constraint nn_hashed_queries_use not null
);

alter table hashed_queries add constraint pk_hashed_queries primary key (query_hash);

/*
 * Update database version
 */

update application_config set value='148' where name='tmtbl.db.version';

commit;
