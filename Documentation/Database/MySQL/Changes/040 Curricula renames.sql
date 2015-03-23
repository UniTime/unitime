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

-- rename curricula tables to curriculum

alter table curricula rename to curriculum;
alter table curricula_clasf rename to curriculum_clasf;
alter table curricula_course rename to curriculum_course;

alter table curriculum drop foreign key fk_curricula_dept; 
alter table curriculum drop foreign key fk_curricula_acad_area;
alter table curriculum_clasf drop foreign key fk_curricula_clasf_curricula;
alter table curriculum_clasf drop foreign key fk_curricula_clasf_acad_clasf;
alter table curriculum_course drop foreign key fk_curricula_course_clasf;
alter table curriculum_course drop foreign key fk_curricula_course_course;
	
alter table curriculum_clasf change column curricula_id curriculum_id decimal(20,0) not null;

alter table curriculum
	add constraint fk_curriculum_dept foreign key (dept_id)
	references department (uniqueid) on delete cascade; 
alter table curriculum
	add constraint fk_curriculum_acad_area foreign key (acad_area_id)
	references academic_area (uniqueid) on delete set null;
alter table curriculum_clasf
   add constraint fk_curriculum_clasf_curriculum foreign key (curriculum_id)
   references curriculum (uniqueid) on delete cascade;
alter table curriculum_clasf
	add constraint fk_curriculum_clasf_acad_clasf foreign key (acad_clasf_id)
	references academic_classification (uniqueid) on delete set null;
alter table curriculum_course
	add constraint fk_curriculum_course_clasf foreign key (cur_clasf_id)
	references curriculum_clasf (uniqueid) on delete cascade;
alter table curriculum_course
	add constraint fk_curriculum_course_course foreign key (course_id)
	references course_offering (uniqueid) on delete cascade;
	
--  Update database version
 
update application_config set value='40' where name='tmtbl.db.version';

commit;
