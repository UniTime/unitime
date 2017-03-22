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

select 32767 * next_hi into @id from hibernate_unique_key;

insert into pos_major (uniqueid, code, name, external_uid, session_id)
select @uid:=@uid+1, '-', 'No Major', a.uniqueid, a.session_id
	from academic_area a cross join (select @uid:=@id) x
	where a.uniqueid in (select sa.acad_area_id from student_acad_area sa where (select count(*) from student_major m where m.student_id = sa.student_id) = 0);

select greatest(ifnull(1 + max(uniqueid), @id), @id) into @id from pos_major;

insert into pos_acad_area_major (major_id, academic_area_id)
select m.uniqueid, m.external_uid from pos_major m
	where m.code = '-' and m.name = 'No Major' and m.external_uid in (select a.uniqueid from academic_area a);

insert into student_major(student_id, major_id)
select s.uniqueid, m.uniqueid
	from student_acad_area a, student s, pos_major m
	where s.uniqueid = a.student_id and (select count(*) from student_major m where m.student_id = s.uniqueid) = 0
	and m.external_uid = a.acad_area_id and m.code = '-';
    
update pos_major m set m.external_uid = '-' where m.code = '-' and m.name = 'No Major' and m.external_uid in (select a.uniqueid from academic_area a);

create table student_area_clasf_major (
	uniqueid decimal(20,0) primary key not null,
	student_id decimal(20,0) not null,
	acad_area_id decimal(20,0) not null,
	acad_clasf_id decimal(20,0) not null,
	major_id decimal(20,0) not null
) engine = INNODB;

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
	select @uid:=@uid+1 unique_id, a.student_id, a.acad_area_id, a.acad_clasf_id, m.major_id
	from student_acad_area a, student_major m, pos_acad_area_major am cross join (select @uid:=@id) x
	where m.student_id = a.student_id and am.academic_area_id = a.acad_area_id and am.major_id = m.major_id;

select ifnull(1 + max(uniqueid), @id) into @id from student_area_clasf_major;

create table student_area_clasf_minor (
	uniqueid decimal(20,0) primary key not null,
	student_id decimal(20,0) not null,
	acad_area_id decimal(20,0) not null,
	acad_clasf_id decimal(20,0) not null,
	minor_id decimal(20,0) not null
) engine = INNODB;

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
	select @uid:=@uid+1 unique_id, a.student_id, a.acad_area_id, a.acad_clasf_id, m.minor_id
	from student_acad_area a, student_minor m, pos_acad_area_minor am cross join (select @uid:=@id) x
	where m.student_id = a.student_id and am.academic_area_id = a.acad_area_id and am.minor_id = m.minor_id;

	
select ifnull(1 + max(uniqueid), @id) into @id from student_area_clasf_minor;
update hibernate_unique_key set next_hi = 1 + (@id div 32767);

/*
 * Update database version
 */

update application_config set value='179' where name='tmtbl.db.version';

commit;
