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

alter table curriculum rename constraint nn_curricula_uniqueid to nn_curriculum_uniqueid;
alter table curriculum rename constraint nn_curricula_abbv to nn_curriculum_abbv;
alter table curriculum rename constraint nn_curricula_name to nn_curriculum_name;
alter table curriculum rename constraint nn_curricula_dept to nn_curriculum_dept;
alter table curriculum rename constraint pk_curricula to pk_curriculum;
alter table curriculum rename constraint fk_curricula_dept to fk_curriculum_dept;
alter table curriculum rename constraint fk_curricula_acad_area to fk_curriculum_acad_area;

alter table curriculum_clasf rename constraint nn_curricula_clasf_uniqueid to nn_curriculum_clasf_uniqueid;
alter table curriculum_clasf rename constraint nn_curricula_clasf_cur_id to nn_curriculum_clasf_cur_id;
alter table curriculum_clasf rename constraint nn_curricula_clasf_name to nn_curriculum_clasf_name;
alter table curriculum_clasf rename constraint nn_curricula_clasf_nr_students to nn_curriculum_clasf_nrstudents;
alter table curriculum_clasf rename constraint nn_curricula_clasf_ord to nn_curriculum_clasf_ord;
alter table curriculum_clasf rename constraint pk_curricula_clasf to pk_curriculum_clasf;
alter table curriculum_clasf rename constraint fk_curricula_clasf_curricula to fk_curriculum_clasf_curriculum;
alter table curriculum_clasf rename constraint fk_curricula_clasf_acad_clasf to fk_curriculum_clasf_acad_clasf;

alter table curriculum_course rename constraint nn_curricula_course_uniqueid to nn_curriculum_course_uniqueid;
alter table curriculum_course rename constraint nn_curricula_course_course_id to nn_curriculum_course_course_id;
alter table curriculum_course rename constraint nn_curricula_cur_clasf_id to nn_curriculum_cur_clasf_id;
alter table curriculum_course rename constraint nn_curricula_course_prsh to nn_curriculum_course_prsh;
alter table curriculum_course rename constraint nn_curricula_course_ord to nn_curriculum_course_ord;
alter table curriculum_course rename constraint pk_curricula_course to pk_curriculum_course;
alter table curriculum_course rename constraint fk_curricula_course_clasf to fk_curriculum_course_clasf;
alter table curriculum_course rename constraint fk_curricula_course_course to fk_curriculum_course_course;

alter table curriculum_clasf rename column curricula_id to curriculum_id;

--  Update database version
 
 update application_config set value='40' where name='tmtbl.db.version';

commit;
