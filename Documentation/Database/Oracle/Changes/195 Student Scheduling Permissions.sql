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
	select distinct r.role_id, 'StudentSchedulingAdvisorCanModifyMyStudents'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'StudentSchedulingAdvisor';

insert into rights (role_id, value)
	select distinct r.role_id, 'StudentSchedulingAdvisorCanModifyAllStudents'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'StudentSchedulingAdvisor';

insert into rights (role_id, value)
	select distinct r.role_id, 'StudentSchedulingCanRegister'
	from roles r, rights g where g.role_id = r.role_id and g.value in ('StudentSchedulingAdmin','StudentSchedulingAdvisor');

insert into rights (select role_id, 'StudentSchedulingCanRegister' as value from roles where reference = 'Student');

insert into rights (role_id, value)
	select distinct r.role_id, 'StudentSchedulingCanEnroll'
	from roles r, rights g where g.role_id = r.role_id and g.value in ('StudentSchedulingAdmin','StudentSchedulingAdvisor');

insert into rights (select role_id, 'StudentSchedulingCanEnroll' as value from roles where reference = 'Student');

update sectioning_status set status = status + 3584 where bitand(status, 256) = 256 and bitand(status, 3584) = 0;

/*
 * Update database version
 */

update application_config set value='194' where name='tmtbl.db.version';

commit;
