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
 * Add settings to control the display of different notes on the classes
 **/
 
insert into settings (uniqueid, name, default_value, allowed_values, description)
	values(settings_seq.nextval, 'printNoteDisplay', 'icon', 'icon,shortened text,full text', 'Display an icon or shortened text when a class has a schedule print note.');
insert into settings (uniqueid, name, default_value, allowed_values, description)
	values(settings_seq.nextval, 'crsOffrNoteDisplay', 'icon', 'icon,shortened text,full text', 'Display an icon or shortened text when a course offering has a schedule note.');
insert into settings (uniqueid, name, default_value, allowed_values, description)
	values(settings_seq.nextval, 'mgrNoteDisplay', 'icon', 'icon,shortened text,full text', 'Display an icon or shortened text when a class has a note to the schedule manager.');

/**
 * Increase size of sched_print_note column in class_ table
 **/
alter table class_ modify sched_print_note varchar2(2000);


/*
 * Update database version
 */

update application_config set value='45' where name='tmtbl.db.version';

commit;
