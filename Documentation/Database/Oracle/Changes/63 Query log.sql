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


create table query_log (
	uniqueid number(20,0) constraint nn_query_log_uniqueid not null,
	time_stamp date constraint nn_query_log_time_stamp not null,
	time_spent number(20,0) constraint nn_query_log_time_spent not null,
	uri varchar2(255) constraint nn_query_log_uri not null,
	type decimal(10,0) constraint nn_query_log_type not null,
	session_id varchar2(32),
	userid varchar2(40),
	query clob,
	exception clob
);

alter table query_log add constraint pk_query_log primary key (uniqueid);

create index idx_query_log on query_log(time_stamp, type);

/**
 * Update database version
 */

update application_config set value='63' where name='tmtbl.db.version';

commit;
		