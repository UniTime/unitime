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

/*
 * Table add course id to exam owner
 */

alter table exam_owner add course_id decimal(20,0);

alter table exam_owner
  add constraint fk_exam_owner_course foreign key (course_id)
  references course_offering (uniqueid) on delete cascade;
  
update exam_owner o set o.course_id = 
  (select co.uniqueid from course_offering co where co.instr_offr_id=o.owner_id and co.is_control=1)
  where o.owner_type=0;

update exam_owner set course_id = owner_id where owner_type=1;

update exam_owner o set o.course_id = 
	(select co.uniqueid from instr_offering_config ioc, course_offering co 
   where ioc.uniqueid=o.owner_id and ioc.instr_offr_id=co.instr_offr_id and co.is_control=1)
	where o.owner_type=2;

update exam_owner o set o.course_id = 
	(select co.uniqueid from instr_offering_config ioc, course_offering co, scheduling_subpart ss, class_ c 
   where c.uniqueid=o.owner_id and c.subpart_id=ss.uniqueid and
   ss.config_id=ioc.uniqueid and ioc.instr_offr_id=co.instr_offr_id and co.is_control=1)
  where o.owner_type=3;

alter table exam_owner modify course_id decimal(20,0) not null;

create index idx_exam_owner_course on exam_owner(course_id);

/*
 * Update database version
 */

update application_config set value='13' where name='tmtbl.db.version';

commit;
