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

create table std_schd_disclaimer (
	uniqueid number(20,0) constraint nn_std_schd_disclaimer_id not null,
	reference varchar2(20 char) constraint nn_std_schd_disclaimer_ref not null,
	label varchar2(60 char) constraint nn_std_schd_disclaimer_label not null,
	disclaimer varchar2(2000 char) constraint nn_std_schd_disclaimer_disc not null
);
alter table std_schd_disclaimer add constraint pk_std_schd_disclaimer primary key (uniqueid);

insert into rights (role_id, value)
	select distinct r.role_id, 'SchedulingDisclaimers'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'CourseTypes';

insert into rights (role_id, value)
	select distinct r.role_id, 'SchedulingDisclaimerEdit'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'CourseTypeEdit';
/*
 * Update database version
 */
  
update application_config set value='276' where name='tmtbl.db.version';

commit;
