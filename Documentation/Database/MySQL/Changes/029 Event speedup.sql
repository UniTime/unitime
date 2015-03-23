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


/**
 * Add index for ClassEvent -> Class
 **/
 
create index idx_event_class on event(class_id);

/**
 * Add index for ExamEvent -> Exam
 **/

create index idx_event_exam on event(exam_id);

/**
 * Add indexes for rooms and non university locations on permanent and session ids (for Meeting.getLocation() method)
 **/

create index idx_room_permid on room(permanent_id, session_id);

create index idx_location_permid on non_university_location(permanent_id, session_id);

/**
 * Add indexe StudentClassEnrollment -> Course
 **/

create index idx_student_class_enrl_course on student_class_enrl(course_offering_id); 
 
/**
 * Update database version
 */

update application_config set value='29' where name='tmtbl.db.version';

commit;
