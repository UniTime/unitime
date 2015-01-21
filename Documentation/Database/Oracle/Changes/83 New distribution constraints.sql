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
	(ref_table_seq.nextval, 'MAX_HRS_DAY(5)', 'At Most 5 Hours A Day', 0, 43, '210R',
		'Classes are to be placed in a way that there is no more than five hours in any day.',
		'At Most 5 Hrs', 1, 0);

insert into distribution_type (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref) values 
	(ref_table_seq.nextval, 'BTB_PRECEDENCE', 'Back-To-Back Precedence', 0, 44, 'P43210R',
	'Given classes have to be taught in the given order, on the same days, and in adjacent time segments.<br>When prohibited or (strongly) discouraged: Given classes have to be taught in the given order, on the same days, but cannot be back-to-back.',
	'BTB Precede', 0, 0);

insert into distribution_type (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref) values 
	(ref_table_seq.nextval, 'SAME_D_T', 'Same Days-Time', 0, 45, 'P43210R',
	'Given classes must be taught at the same time of day and on the same days.<br>This constraint combines Same Days and Same Time distribution preferences.<br>When prohibited or (strongly) discouraged: Any pair of classes classes cannot be taught on the same days during the same time.',
	'Same Days-Time', 0, 0);

insert into distribution_type (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref) values 
	(ref_table_seq.nextval, 'SAME_D_R_T', 'Same Days-Room-Time', 0, 46, 'P43210R',
	'Given classes must be taught at the same time of day, on the same days and in the same room.<br>Note that this constraint is the same as Meet Together constraint, except it does not allow for room sharing. In other words, it is only useful when these classes are taught during non-overlapping date patterns.<br>When prohibited or (strongly) discouraged: Any pair of classes classes cannot be taught on the same days during the same time in the same room.',
	'Same Days-Room-Time', 0, 0);

insert into distribution_type (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref) values 
	(ref_table_seq.nextval, 'SAME_WEEKS', 'Same Weeks', 0, 47, 'P43210R',
	'Given classes must be taught during the same weeks (i.e., must have the same date pattern).<br>When prohibited or (strongly) discouraged: any two classes must have non overlapping date patterns.',
	'Same Weeks', 0, 0);

/*
 * Update database version
 */

update application_config set value='83' where name='tmtbl.db.version';

commit;
