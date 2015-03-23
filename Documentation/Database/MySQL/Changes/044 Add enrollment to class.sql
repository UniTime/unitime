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
 * Add an enrollment column to the class table.
 **/
alter table class_ add enrollment  int(4);

/**
 * Initialize enrollment column in the class table.
 **/

update class_ c
set c.enrollment = (select count(distinct sce.student_id) 
        from student_class_enrl sce
        where sce.class_id = c.uniqueid);

/**
 * Increase size of meetings column in event note table
 **/
 alter table event_note modify meetings varchar(2000);

/**
 * Add an enrollment column to the course_offering table.
 **/
alter table course_offering add enrollment int(10);

/**
 * Initialize enrollment column in the course_offering table.
 **/
 
update course_offering co
set co.enrollment = (select count(distinct sce.student_id) 
        from student_class_enrl sce
        where sce.course_offering_id = co.uniqueid);

/*
 * Update database version
 */

update application_config set value='44' where name='tmtbl.db.version';

commit;
