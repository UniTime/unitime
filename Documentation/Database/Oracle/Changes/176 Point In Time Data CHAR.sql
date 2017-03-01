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

alter table point_in_time_data modify name varchar2(100 char);
alter table point_in_time_data modify note varchar2(1000 char);
alter table pit_instr_offering modify external_uid varchar2(40 char);
alter table pit_course_offering modify course_nbr varchar2(40 char);
alter table pit_course_offering modify perm_id varchar2(20 char);
alter table pit_course_offering modify title varchar2(200 char);
alter table pit_course_offering modify external_uid varchar2(40 char);
alter table pit_instr_offer_config modify name varchar2(10 char);
alter table pit_sched_subpart modify subpart_suffix varchar2(5 char);
alter table pit_class modify class_suffix varchar2(10 char);
alter table pit_class modify external_uid varchar2(40 char);
alter table pit_student modify external_uid varchar2(40 char);
alter table pit_student modify first_name varchar2(100 char);
alter table pit_student modify middle_name varchar2(100 char);
alter table pit_student modify last_name varchar2(100 char);
alter table pit_student modify email varchar2(200 char);
alter table pit_student_class_enrl modify changed_by varchar2(40 char);
alter table pit_dept_instructor modify external_uid varchar2(40 char);
alter table pit_dept_instructor modify career_acct varchar2(20 char);
alter table pit_dept_instructor modify lname varchar2(100 char);
alter table pit_dept_instructor modify fname varchar2(100 char);
alter table pit_dept_instructor modify mname varchar2(100 char);
alter table pit_dept_instructor modify email varchar2(200 char);
alter table pit_class_event modify event_name varchar2(100 char);


/*
 * Update database version
 */

update application_config set value='176' where name='tmtbl.db.version';

commit;
