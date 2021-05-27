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

alter table department add external_funding_dept number(1);

alter table subject_area add funding_dept_id number(20,0);
alter table subject_area add constraint fk_sa_fund_dept foreign key (funding_dept_id)
			references department (uniqueid) on delete set null;
			
alter table course_offering add funding_dept_id number(20,0);
alter table course_offering add constraint fk_co_fund_dept foreign key (funding_dept_id)
			references department (uniqueid) on delete set null;

alter table class_ add funding_dept_id number(20,0);
alter table class_ add constraint fk_class_fund_dept foreign key (funding_dept_id)
			references department (uniqueid) on delete set null;

alter table pit_class add funding_dept_id number(20,0);
alter table pit_class add constraint fk_pit_class_fund_dept foreign key (funding_dept_id)
			references department (uniqueid) on delete set null;

/*
 * Update database version
 */

update application_config set value='240' where name='tmtbl.db.version';

commit;
