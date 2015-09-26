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

insert into rights (role_id, value)
	select distinct r.role_id, 'CanLookupStudents'
	from roles r, rights g where g.role_id = r.role_id and g.value in ('EventLookupContact', 'EventLookupSchedule', 'InstructorAdd', 'InstructorEdit', 'StudentGroupEdit', 'ReservationAdd', 'ReservationEdit', 'Users', 'StudentSchedulingAdvisor', 'StudentSchedulingAdmin', 'ApiRetrieveEvents');

insert into rights (role_id, value)
	select distinct r.role_id, 'CanLookupInstructors'
	from roles r, rights g where g.role_id = r.role_id and g.value in ('EventLookupContact', 'EventLookupSchedule', 'InstructorAdd', 'InstructorEdit', 'Users', 'ApiRetrieveEvents');
	
insert into rights (role_id, value)
	select distinct r.role_id, 'CanLookupManagers'
	from roles r, rights g where g.role_id = r.role_id and g.value in ('EventLookupContact', 'EventLookupSchedule', 'InstructorAdd', 'InstructorEdit', 'TimetableManagerAdd', 'TimetableManagerEdit', 'Users');

insert into rights (role_id, value)
	select distinct r.role_id, 'CanLookupStaff'
	from roles r, rights g where g.role_id = r.role_id and g.value in ('EventLookupContact', 'EventLookupSchedule', 'InstructorAdd', 'InstructorEdit', 'TimetableManagerAdd', 'TimetableManagerEdit', 'Users');
	
insert into rights (role_id, value)
	select distinct r.role_id, 'CanLookupEventContacts'
	from roles r, rights g where g.role_id = r.role_id and g.value in ('EventLookupContact', 'EventLookupSchedule', 'ApiRetrieveEvents');

insert into rights (role_id, value)
	select distinct r.role_id, 'CanLookupLdap'
	from roles r, rights g where g.role_id = r.role_id and g.value in ('EventLookupContact', 'EventLookupSchedule', 'InstructorAdd', 'InstructorEdit', 'TimetableManagerAdd', 'TimetableManagerEdit', 'Users');

/*
 * Update database version
 */

update application_config set value='151' where name='tmtbl.db.version';

commit;
