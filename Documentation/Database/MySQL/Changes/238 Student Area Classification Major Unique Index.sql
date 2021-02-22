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


alter table student_area_clasf_major drop foreign key fk_student_acmaj_student;
alter table student_area_clasf_major drop foreign key fk_student_acmaj_area;
alter table student_area_clasf_major drop foreign key fk_student_acmaj_clasf;
alter table student_area_clasf_major drop foreign key fk_student_acmaj_major;
alter table student_area_clasf_major drop foreign key fk_student_acmaj_conc;

alter table student_area_clasf_major drop index uk_student_area_clasf_major;

create unique index uk_student_area_clasf_major on student_area_clasf_major(student_id, acad_area_id, acad_clasf_id, major_id, concentration_id);

alter table student_area_clasf_major add constraint fk_student_acmaj_student foreign key (student_id)
	references student (uniqueid) on delete cascade;
alter table student_area_clasf_major add constraint fk_student_acmaj_area foreign key (acad_area_id)
	references academic_area (uniqueid) on delete cascade;
alter table student_area_clasf_major add constraint fk_student_acmaj_clasf foreign key (acad_clasf_id)
	references academic_classification (uniqueid) on delete cascade;
alter table student_area_clasf_major add constraint fk_student_acmaj_major foreign key (major_id)
	references pos_major (uniqueid) on delete cascade;
alter table student_area_clasf_major add constraint fk_student_acmaj_conc foreign key (concentration_id)
	references pos_major_conc (uniqueid) on delete set null;

/*
 * Update database version
 */

update application_config set value='238' where name='tmtbl.db.version';

commit;
