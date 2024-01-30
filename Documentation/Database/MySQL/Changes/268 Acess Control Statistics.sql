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

create table access_stats (
	uniqueid decimal(20,0) primary key not null,
	time_stamp datetime not null,
	host varchar(50) not null,
	page varchar(50) not null,
	nbr_access decimal(10,0) not null,
	nbr_active decimal(10,0) not null,
	nbr_waiting decimal(10,0) not null,
	nbr_opened decimal(10,0) not null,
	nbr_tracking decimal(10,0) not null,
	nbr_active1m decimal(10,0) not null,
	nbr_active2m decimal(10,0) not null,
	nbr_active5m decimal(10,0) not null,
	nbr_active10m decimal(10,0) not null,
	nbr_active15m decimal(10,0) not null
) engine = INNODB;

create index idx_access_stats on access_stats(time_stamp, page);

/*
 * Update database version
 */
  
update application_config set value='268' where name='tmtbl.db.version';

commit;
