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

create table contact_category (
	uniqueid number(20,0) constraint nn_concat_id not null,
	reference varchar2(20 char) constraint nn_concat_ref not null,
	label varchar2(60 char) constraint nn_concat_label not null,
	message varchar2(2048 char),
	has_role number(1,0) constraint nn_concat_has_role not null,
	email varchar2(1000 char)
);
alter table contact_category add constraint pk_contact_category primary key (uniqueid);

insert into rights (role_id, value)
	select distinct r.role_id, 'ContactCategories'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'PreferenceLevels';

insert into rights (role_id, value)
	select distinct r.role_id, 'ContactCategoryEdit'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'PreferenceLevelEdit';

insert into contact_category (uniqueid, reference, label, message, has_role) values
	(ref_table_seq.nextval, '01-QUESTION', 'Ask a question', null, 0);
insert into contact_category (uniqueid, reference, label, message, has_role) values
	(ref_table_seq.nextval, '02-ERROR', 'Report an error', null, 0);
insert into contact_category (uniqueid, reference, label, message, has_role) values
	(ref_table_seq.nextval, '03-SUGGESTION', 'Make a suggestion', null, 0);
insert into contact_category (uniqueid, reference, label, message, has_role) values
	(ref_table_seq.nextval, '04-REQ-TIMEPAT', 'Request addition of a time pattern', '1. Specify the list of classes or scheduling subparts on which the requested time pattern need to be set (e.g., AB 100 Lec 1, AB 100 Lec 2) : '||u'\000D\000A\000D\000A'||'2. Specify the time pattern (number of meetings and number of minutes per meeting, e.g., 2x75) : '||u'\000D\000A\000D\000A'||'3. Specify the available days (e.g., MW, WF, MF, TTh) : '||u'\000D\000A\000D\000A'||'4. Specify the available starting times (e.g., 7:30am, 8:30am, 9:30am, ... 4:30pm) : '||u'\000D\000A', 1);
insert into contact_category (uniqueid, reference, label, message, has_role) values
	(ref_table_seq.nextval, '05-REQ-EXTIME', 'Request addition of an exact time', '1. Specify the list of classes on which the requested time pattern need to be set (e.g., AB 100 Lec 1, AB 100 Lec 2) : '||u'\000D\000A\000D\000A'||'2. Specify the time (e.g., MWF 8:15am - 10:20am) : '||u'\000D\000A', 1);
insert into contact_category (uniqueid, reference, label, message, has_role) values
	(ref_table_seq.nextval, '06-REQ-DATEPAT', 'Request addition of a date pattern', '1. Specify the list of classes or scheduling subparts on which the requested date pattern need to be set (e.g., AB 100 Lec 1, AB 100 Lec 2) : '||u'\000D\000A\000D\000A'||'2. Specify the list weeks for the date pattern (e.g., Weeks 2-8; alternatively, you can specify start and end date) : '||u'\000D\000A', 1);
insert into contact_category (uniqueid, reference, label, message, has_role) values
	(ref_table_seq.nextval, '07-REQ-CHOWN', 'Request a change of owner on class and/or scheduling subpart', '1. Specify the list of classes or scheduling subparts on which the requested date pattern need to be set (e.g., AB 100 Lec 1, AB 100 Lec 2) : '||u'\000D\000A\000D\000A'||'2. Specify the new owner (e.g., LLR) : '||u'\000D\000A', 1);
insert into contact_category (uniqueid, reference, label, message, has_role) values
	(ref_table_seq.nextval, '08-REQ-CROSSLIST', 'Request a course cross-listing', '1. Specify the controlling course (e.g., AB 100) : '||u'\000D\000A\000D\000A'||'2. Specify courses that should be cross-listed with the controlling course (e.g., CDFS 100) : '||u'\000D\000A', 1);
insert into contact_category (uniqueid, reference, label, message, has_role) values
	(ref_table_seq.nextval, '09-REQ-ROOMSHR', 'Request a room to be shared', '1. Specify the room that needs to be shared (e.g., GRIS 100) : '||u'\000D\000A\000D\000A'||'2. Specify the departments between which the room needs to be shared (e.g., 1282-Aeronautics '||u'\0026'||' Astronautics and 1287-Industrial Engineering) : '||u'\000D\000A\000D\000A'||'3. Specify the times when the room is to be allocated for one of the departments or when the room is not available (e.g., MWF for Aero, TTh for I E, not available after 3:00pm) : '||u'\000D\000A', 1);
insert into contact_category (uniqueid, reference, label, message, has_role) values
	(ref_table_seq.nextval, '10-REQ-ADMIN', 'Request any other administrative change', null, 1);
insert into contact_category (uniqueid, reference, label, message, has_role) values
	(ref_table_seq.nextval, '11-NOT-LLRDONE', 'LLR/LAB data entry is done', '1. Specify the managing department (e.g., LLR or LAB) : '||u'\000D\000A', 1);
insert into contact_category (uniqueid, reference, label, message, has_role) values
	(ref_table_seq.nextval, '12-OTHER', 'Other', null, 1);

/*
 * Update database version
 */

update application_config set value='221' where name='tmtbl.db.version';

commit;
