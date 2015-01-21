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
	uniqueid decimal(20,0) primary key not null,
	time_stamp datetime not null,
	time_spent decimal(20,0) not null,
	uri varchar(255) not null,
	type decimal(10,0) not null,
	session_id varchar(32) null,
	userid varchar(40) null,
	query longtext binary null,
	exception longtext binary null
) engine = INNODB;

create index idx_query_log on query_log(time_stamp);

/**
 * Update database version
 */

update application_config set value='63' where name='tmtbl.db.version';

commit;
		