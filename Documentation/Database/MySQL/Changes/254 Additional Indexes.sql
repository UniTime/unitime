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

create index idx_sectioning_log_opstd on sectioning_log(operation, student);
create index idx_student_ext on student(session_id, external_uid, uniqueid);

create index idx_student_enrl_course_std on student_class_enrl(course_offering_id, student_id);
create index idx_meeting_date_room_stop on meeting(meeting_date, location_perm_id, stop_period);
create index idx_course_offering_ctrlcourse on course_offering(is_control, instr_offr_id, subject_area_id);
create index idx_manager_ext on timetable_manager(external_uid, uniqueid);
create index idx_meeting_date_room_apr on meeting(meeting_date, location_perm_id, event_id, approval_status);
create index idx_course_offering_rolled_from on course_offering(uid_rolled_fwd_from);
create index idx_event_contact_extid on event_contact(external_id);
create index idx_meeting_all on meeting(event_id, uniqueid, meeting_date, start_period, start_offset, stop_period, stop_offset, location_perm_id, class_can_override, approval_status, approval_date);
create index idx_session_initiative on sessions(academic_initiative);
/*
 * Update database version
 */

update application_config set value='254' where name='tmtbl.db.version';

commit;
