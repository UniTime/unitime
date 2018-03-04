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

insert into pos_major (uniqueid, code, name, external_uid, session_id)
select pos_major_seq.nextval, '-', 'No Major', a.uniqueid, a.session_id
	from academic_area a
	where a.uniqueid in (select sa.acad_area_id from student_acad_area sa where (select count(*) from student_major m where m.student_id = sa.student_id) = 0);

insert into pos_acad_area_major (major_id, academic_area_id)
select m.uniqueid, m.external_uid from pos_major m
	where m.code = '-' and m.name = 'No Major' and m.external_uid in (select a.uniqueid from academic_area a);

insert into student_major(student_id, major_id)
select distinct s.uniqueid, m.uniqueid
	from student_acad_area a, student s, pos_major m
	where s.uniqueid = a.student_id and (select count(*) from student_major m where m.student_id = s.uniqueid) = 0
	and m.external_uid = a.acad_area_id and m.code = '-';
    
update pos_major m set m.external_uid = '-' where m.code = '-' and m.name = 'No Major' and m.external_uid in (select a.uniqueid from academic_area a);

create table student_area_clasf_major (
	uniqueid number(20,0) constraint nn_student_acmaj_id not null,
	student_id number(20,0) constraint nn_student_acmaj_student not null,
	acad_area_id number(20,0) constraint nn_student_acmaj_area not null,
	acad_clasf_id number(20,0) constraint nn_student_acmaj_clasf not null,
	major_id number(20,0) constraint nn_student_acmaj_major not null
);
alter table student_area_clasf_major add constraint pk_student_area_clasf_major primary key (uniqueid)

create unique index uk_student_area_clasf_major on student_area_clasf_major(student_id, acad_area_id, acad_clasf_id, major_id);

alter table student_area_clasf_major add constraint fk_student_acmaj_student foreign key (student_id)
	references student (uniqueid) on delete cascade;
alter table student_area_clasf_major add constraint fk_student_acmaj_area foreign key (acad_area_id)
	references academic_area (uniqueid) on delete cascade;
alter table student_area_clasf_major add constraint fk_student_acmaj_clasf foreign key (acad_clasf_id)
	references academic_classification (uniqueid) on delete cascade;
alter table student_area_clasf_major add constraint fk_student_acmaj_major foreign key (major_id)
	references pos_major (uniqueid) on delete cascade;

insert into student_area_clasf_major (uniqueid, student_id, acad_area_id, acad_clasf_id, major_id)
	select pref_group_seq.nextval, a.student_id, a.acad_area_id, a.acad_clasf_id, m.major_id
	from student_acad_area a, student_major m, pos_acad_area_major am
	where m.student_id = a.student_id and am.academic_area_id = a.acad_area_id and am.major_id = m.major_id;

create table student_area_clasf_minor (
	uniqueid number(20,0) constraint nn_student_acmin_id not null,
	student_id number(20,0) constraint nn_student_acmin_student not null,
	acad_area_id number(20,0) constraint nn_student_acmin_area not null,
	acad_clasf_id number(20,0) constraint nn_student_acmin_clasf not null,
	minor_id number(20,0) constraint nn_student_acmin_minor not null
);
alter table student_area_clasf_minor add constraint pk_student_area_clasf_minor primary key (uniqueid);

create unique index uk_student_area_clasf_minor on student_area_clasf_minor(student_id, acad_area_id, acad_clasf_id, minor_id);

alter table student_area_clasf_minor add constraint fk_student_acmin_student foreign key (student_id)
	references student (uniqueid) on delete cascade;
alter table student_area_clasf_minor add constraint fk_student_acmin_area foreign key (acad_area_id)
	references academic_area (uniqueid) on delete cascade;
alter table student_area_clasf_minor add constraint fk_student_acmin_clasf foreign key (acad_clasf_id)
	references academic_classification (uniqueid) on delete cascade;
alter table student_area_clasf_minor add constraint fk_student_acmin_minor foreign key (minor_id)
	references pos_minor (uniqueid) on delete cascade;

insert into student_area_clasf_minor (uniqueid, student_id, acad_area_id, acad_clasf_id, minor_id)
	select pref_group_seq.nextval, a.student_id, a.acad_area_id, a.acad_clasf_id, m.minor_id
	from student_acad_area a, student_minor m, pos_acad_area_minor am
	where m.student_id = a.student_id and am.academic_area_id = a.acad_area_id and am.minor_id = m.minor_id;

/*
 * Update database version
 */

update application_config set value='179' where name='tmtbl.db.version';

commit;
