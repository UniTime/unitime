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

alter table class_ add snapshot_limit bigint(10) default null;
alter table class_ add snapshot_limit_date datetime default null;

alter table instructional_offering add snapshot_limit bigint(10) default null;
alter table instructional_offering add snapshot_limit_date datetime default null;

alter table course_offering add snapshot_proj_demand bigint(19) default null;
alter table course_offering add snapshot_prj_dmd_date datetime default null;

alter table curriculum_rule add snapshot_proj float default null;
alter table curriculum_rule add snapshot_proj_date datetime default null;

alter table curriculum_clasf add snapshot_nr_students bigint(10) default null;
alter table curriculum_clasf add snapshot_nr_stu_date datetime default null;

alter table curriculum_course add snapshot_pr_share float default null;
alter table curriculum_course add snapshot_pr_shr_date datetime default null;

insert into rights (role_id, value)
select distinct r.role_id, 'LimitAndProjectionSnapshot'
from roles r, rights g where g.role_id = r.role_id and g.value = 'PointInTimeData';

insert into rights (role_id, value)
select distinct r.role_id, 'LimitAndProjectionSnapshotSave'
from roles r, rights g where g.role_id = r.role_id and g.value = 'PointInTimeDataEdit';

/*
 * Update database version
 */

update application_config set value='181' where name='tmtbl.db.version';

commit;
