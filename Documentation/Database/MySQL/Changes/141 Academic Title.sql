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

alter table staff add acad_title varchar(50) null;
alter table departmental_instructor add acad_title varchar(50) null;
alter table event_contact add acad_title varchar(50) null;
alter table timetable_manager add acad_title varchar(50) null;

update settings set allowed_values = 'last-first,first-last,initial-last,last-initial,first-middle-last,short,title-first-middle-last,last-first-middle-title,title-initial-last,title-last-initial' where name = 'name'; 

insert into rights (role_id, value)
	select distinct r.role_id, 'EventCanEditAcademicTitle'
	from roles r, rights g where g.role_id = r.role_id and g.value like 'EventLookupContact';
	
/*
 * Update database version
 */

update application_config set value='141' where name='tmtbl.db.version';

commit;
