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
 * Added exam-conflict table and its M:N relations to exams and students
 **/
 
create table xconflict (
	uniqueid number(20,0) constraint nn_xconflict_uniqueid not null,
	conflict_type number(10,0) constraint nn_xconflict_type not null,
	distance float
);

alter table xconflict
  add constraint pk_xconflict primary key (uniqueid);
  
create table xconflict_exam (
	conflict_id number(20,0) constraint nn_xconflict_ex_conf not null,
	exam_id number(20,0) constraint nn_xconflict_ex_exam not null
);

alter table xconflict_exam
  add constraint pk_xconflict_exam primary key (conflict_id, exam_id);

alter table xconflict_exam
  add constraint fk_xconflict_ex_conf foreign key (conflict_id)
  references xconflict (uniqueid) on delete cascade;

alter table xconflict_exam
  add constraint fk_xconflict_ex_exam  foreign key (exam_id)
  references exam (uniqueid) on delete cascade;

create table xconflict_student (
	conflict_id number(20,0) constraint nn_xconflict_st_conf  not null,
	student_id number(20,0) constraint nn_xconflict_st_student  not null
);

alter table xconflict_student
  add constraint pk_xconflict_student primary key (conflict_id, student_id);

alter table xconflict_student
  add constraint fk_xconflict_st_conf foreign key (conflict_id)
  references xconflict (uniqueid) on delete cascade;

alter table xconflict_student
  add constraint fk_xconflict_st_student foreign key (student_id)
  references student (uniqueid) on delete cascade;
  
create index idx_xconflict_exam on xconflict_exam(exam_id);
	
/*
 * Update database version
 */

update application_config set value='15' where name='tmtbl.db.version';

commit;
