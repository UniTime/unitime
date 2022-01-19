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

delete from waitlist;

alter table waitlist add changed_by varchar(40);
alter table waitlist add waitlisted boolean not null default false;
alter table waitlist add enrolled_course_id bigint;
alter table waitlist add demand_id bigint;
alter table waitlist add request varchar(255);
alter table waitlist add enrollment varchar(255);
alter table waitlist add waitlist_ts timestamp with time zone;

alter table waitlist add constraint fk_waitlist_enrolled_course foreign key (enrolled_course_id)
	references course_offering (uniqueid) on delete set null;

alter table waitlist add constraint fk_waitlist_course_demand foreign key (demand_id)
	references course_demand (uniqueid) on delete set null;

/*
 * Update database version
 */

update application_config set value='246' where name='tmtbl.db.version';

commit;
