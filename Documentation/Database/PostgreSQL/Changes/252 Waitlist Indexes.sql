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

create index idx_course_demand_course_swap on course_demand using btree (wl_course_swap_id);
create index idx_sect_pref_im on sect_pref using btree (instr_mthd_id);
create index idx_sect_pref_class on sect_pref using btree (class_id);
create index idx_sect_pref_request on sect_pref using btree (request_id);

create index idx_waitlist_course_demand on waitlist using btree (demand_id);
create index idx_waitlist_enrolled_course on waitlist using btree (enrolled_course_id);
create index idx_waitlist_swap_course on waitlist using btree (swap_course_id);

/*
 * Update database version
 */

update application_config set value='252' where name='tmtbl.db.version';

commit;
