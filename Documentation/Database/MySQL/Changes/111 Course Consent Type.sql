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

alter table course_offering add consent_type decimal(20,0);

alter table course_offering add constraint fk_course_consent_type foreign key (consent_type)
	references offr_consent_type (uniqueid) on delete cascade;

update course_offering set consent_type = (select o.consent_type from instructional_offering o where o.uniqueid = instr_offr_id);
		
alter table instructional_offering drop foreign key fk_instr_offr_consent_type;

alter table instructional_offering drop column consent_type;

/*
 * Update database version
 */

update application_config set value='111' where name='tmtbl.db.version';

commit;
