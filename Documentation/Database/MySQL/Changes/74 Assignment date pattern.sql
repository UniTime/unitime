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

alter table assignment add date_pattern_id decimal(20,0) null;

alter table assignment add constraint fk_assignment_date_pattern foreign key (date_pattern_id)
	references date_pattern(uniqueid) on delete set null;

update assignment a, class_ c
	set a.date_pattern_id = c.date_pattern_id
	where c.date_pattern_id is not null and c.uniqueid = a.class_id and a.date_pattern_id is null; 

update assignment a, scheduling_subpart s, class_ c
	set a.date_pattern_id = s.date_pattern_id
	where s.date_pattern_id is not null and s.uniqueid = c.subpart_id and c.uniqueid = a.class_id and a.date_pattern_id is null; 

update assignment a, solution x, solver_group g, sessions s
	set a.date_pattern_id = s.def_datepatt_id
	where a.solution_id = x.uniqueid and x.owner_id = g.uniqueid and g.session_id = s.uniqueid and a.date_pattern_id is null; 


/*
 * Update database version
 */

update application_config set value='74' where name='tmtbl.db.version';

commit;
