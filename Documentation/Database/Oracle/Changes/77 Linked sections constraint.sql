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

insert into distribution_type (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref) values 
	(ref_table_seq.nextval, 'LINKED_SECTIONS', 'Linked Classes', 0, 42, 'R', 'Classes (of different courses) are to be attended by the same students. For instance, if class A1 (of a course A) and class B1 (of a course B) are linked, a student requesting both courses must attend A1 if and only if he also attends B1. This is a student sectioning constraint that is interpreted as Same Students constraint during course timetabling.', 'Linked', 0, 0);


/*
 * Update database version
 */

update application_config set value='77' where name='tmtbl.db.version';

commit;
