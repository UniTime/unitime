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
 * disallow null in curriculum.acad_area_id
 */
alter table curriculum drop foreign key fk_curriculum_acad_area;

alter table curriculum modify acad_area_id decimal(20,0) not null;

alter table curriculum add constraint fk_curriculum_acad_area foreign key (acad_area_id)
	references academic_area (uniqueid) on delete cascade;

/**
 * disallow null in curriculum_clasf.acad_clasf_id
 */
alter table curriculum_clasf drop foreign key fk_curriculum_clasf_acad_clasf;

alter table curriculum_clasf modify acad_clasf_id decimal(20,0) not null;

alter table curriculum_clasf add constraint fk_curriculum_clasf_acad_clasf foreign key (acad_clasf_id)
	references academic_classification (uniqueid) on delete cascade;

/**
 * drop unused columns
 */

alter table curriculum_clasf drop column ll_students;
alter table curriculum_course drop column ll_share;

/**
 * create curriculum_rule table
 */

create table curriculum_rule
	(
	  uniqueid decimal(20,0) primary key not null,
	  acad_area_id decimal(20,0) not null,
	  major_id decimal(20,0),
	  acad_clasf_id decimal(20,0) not null,
	  projection float not null
	) engine = INNODB;

create index idx_cur_rule_areadept on curriculum_rule(acad_area_id, acad_clasf_id);

alter table curriculum_rule add constraint fk_cur_rule_acad_area foreign key (acad_area_id)
	references academic_area (uniqueid) on delete cascade;

alter table curriculum_rule add constraint fk_cur_rule_major foreign key (major_id)
	references pos_major (uniqueid) on delete cascade;

alter table curriculum_rule add constraint fk_cur_rule_acad_clasf foreign key (acad_clasf_id)
	references academic_classification (uniqueid) on delete cascade;

/**
 * Update database version
 */

update application_config set value='55' where name='tmtbl.db.version';

commit;
