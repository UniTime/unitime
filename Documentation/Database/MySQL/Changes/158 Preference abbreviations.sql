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

alter table preference_level add pref_abbv varchar(10);

update preference_level set pref_abbv='Req' where pref_prolog='R';
update preference_level set pref_abbv='StrPref' where pref_prolog='-2';
update preference_level set pref_abbv='Pref' where pref_prolog='-1';
update preference_level set pref_abbv='' where pref_prolog='0';
update preference_level set pref_abbv='Disc' where pref_prolog='1';
update preference_level set pref_abbv='StrDisc' where pref_prolog='2';
update preference_level set pref_abbv='Proh' where pref_prolog='P';
update preference_level set pref_abbv='N/A' where pref_prolog='N';

insert into rights (role_id, value)
	select distinct r.role_id, 'PreferenceLevels'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'AttachementTypes';

insert into rights (role_id, value)
	select distinct r.role_id, 'PreferenceLevelEdit'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'AttachementTypeEdit';


/*
 * Update database version
 */

update application_config set value='158' where name='tmtbl.db.version';

commit;
