/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime.org
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
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
