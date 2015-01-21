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

alter table standard_event_note add discriminator varchar2(10 char) default 'global';
alter table standard_event_note add session_id number(20,0);
alter table standard_event_note add department_id number(20,0);

alter table standard_event_note add constraint fk_stdevt_note_session foreign key (session_id)
	references sessions (uniqueid) on delete cascade;
alter table standard_event_note add constraint fk_stdevt_note_dept foreign key (department_id)
	references department (uniqueid) on delete cascade;

insert into rights (role_id, value)
	select distinct r.role_id, 'StandardEventNotesGlobalEdit'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'StandardEventNotes';
insert into rights (role_id, value)
	select distinct r.role_id, 'StandardEventNotesSessionEdit'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'IsAdmin';
insert into rights (role_id, value)
	select distinct r.role_id, 'StandardEventNotesDepartmentEdit'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'EventMeetingApprove';
delete from rights where value = 'StandardEventNotes';
insert into rights (role_id, value)
	select distinct r.role_id, 'StandardEventNotes'
	from roles r, rights g where g.role_id = r.role_id and g.value in ('StandardEventNotesGlobalEdit', 'StandardEventNotesSessionEdit', 'StandardEventNotesDepartmentEdit');

/*
 * Update database version
 */

update application_config set value='109' where name='tmtbl.db.version';

commit;
