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
	(ref_table_seq.nextval, 'NO_CONFLICT', 'Ignore Student Conflicts', 0, 49, '2R', 'All student conflicts between the given classes are to be ignored.', 'No Conflicts', 0, 0);

/*
 * Update database version
 */

update application_config set value='113' where name='tmtbl.db.version';

commit;
