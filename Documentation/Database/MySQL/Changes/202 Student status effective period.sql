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

alter table sectioning_status add fallback_id decimal(20,0) default null;
alter table sectioning_status add start_date date default null;
alter table sectioning_status add stop_date date default null;
alter table sectioning_status add start_slot bigint(10) default null;
alter table sectioning_status add stop_slot bigint(10) default null;

alter table sectioning_status add constraint fk_sct_status_fallback foreign key (fallback_id)
	references sectioning_status (uniqueid) on delete set null;

/*
 * Update database version
 */

update application_config set value='202' where name='tmtbl.db.version';

commit;
