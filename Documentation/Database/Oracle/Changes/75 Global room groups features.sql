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

update room_group set session_id = (select min(uniqueid) from sessions) where global = 1 and session_id is null;

insert into room_group select 
	room_group_seq.nextval, s.uniqueid as session_id,
	g.name, g.description, g.global, g.default_group, g.department_id, g.abbv
from room_group g, sessions s where g.global = 1 and not exists
	(select * from room_group x where x.session_id = s.uniqueid and x.name = g.name);

update room_group_room x set x.room_group_id =
	(select n.uniqueid from room_group n, room_group g, room r where
	x.room_group_id = g.uniqueid and x.room_id = r.uniqueid and n.name = g.name and n.session_id = r.session_id)
where x.room_group_id in
	(select g.uniqueid from room_group g, room r where
	x.room_group_id = g.uniqueid and x.room_id = r.uniqueid and g.global = 1 and g.session_id != r.session_id);

update room_group_room x set x.room_group_id =
	(select n.uniqueid from room_group n, room_group g, non_university_location r where
	x.room_group_id = g.uniqueid and x.room_id = r.uniqueid and n.name = g.name and n.session_id = r.session_id)
where x.room_group_id in
	(select g.uniqueid from room_group g, non_university_location r where
	x.room_group_id = g.uniqueid and x.room_id = r.uniqueid and g.global = 1 and g.session_id != r.session_id);

update room_group_pref p set p.room_group_id = 
	(select n.uniqueid from room_group g, room_group n, class_ c, scheduling_subpart t, instr_offering_config e, instructional_offering o
	where g.uniqueid = p.room_group_id and n.session_id = o.session_id and n.name = g.name and p.owner_id = c.uniqueid and c.subpart_id = t.uniqueid and t.config_id = e.uniqueid and e.instr_offr_id = o.uniqueid)
where p.room_group_id in (
	select g.uniqueid from room_group g, class_ c, scheduling_subpart t, instr_offering_config e, instructional_offering o
	where g.uniqueid = p.room_group_id and g.session_id != o.session_id and g.global = 1 and p.owner_id = c.uniqueid and c.subpart_id = t.uniqueid and t.config_id = e.uniqueid and e.instr_offr_id = o.uniqueid);

update room_group_pref p set p.room_group_id = 
	(select n.uniqueid from room_group g, room_group n, scheduling_subpart t, instr_offering_config e, instructional_offering o
	where g.uniqueid = p.room_group_id and n.session_id = o.session_id and n.name = g.name and p.owner_id = t.uniqueid and t.config_id = e.uniqueid and e.instr_offr_id = o.uniqueid)
where p.room_group_id in (
	select g.uniqueid from room_group g, class_ c, scheduling_subpart t, instr_offering_config e, instructional_offering o
	where g.uniqueid = p.room_group_id and g.session_id != o.session_id and g.global = 1 and p.owner_id = t.uniqueid and t.config_id = e.uniqueid and e.instr_offr_id = o.uniqueid);

update room_group_pref p set p.room_group_id = 
	(select n.uniqueid from room_group g, room_group n, sessions s
	where g.uniqueid = p.room_group_id and n.session_id = s.uniqueid and n.name = g.name and p.owner_id = s.uniqueid)
where p.room_group_id in (
	select g.uniqueid from room_group g, sessions s
	where g.uniqueid = p.room_group_id and g.session_id != s.uniqueid and g.global = 1 and p.owner_id = s.uniqueid);

update room_group_pref p set p.room_group_id = 
	(select n.uniqueid from room_group g, room_group n, exam x
	where g.uniqueid = p.room_group_id and n.session_id = x.session_id and n.name = g.name and p.owner_id = x.uniqueid)
where p.room_group_id in (
	select g.uniqueid from room_group g, exam x
	where g.uniqueid = p.room_group_id and g.session_id != x.session_id and g.global = 1 and p.owner_id = x.uniqueid);

update room_group_pref p set p.room_group_id = 
	(select n.uniqueid from room_group g, room_group n, departmental_instructor i, department d
	where g.uniqueid = p.room_group_id and n.session_id = d.session_id and n.name = g.name and p.owner_id = i.uniqueid and i.department_uniqueid = d.uniqueid)
where p.room_group_id in (
	select g.uniqueid from room_group g, departmental_instructor i, department d
	where g.uniqueid = p.room_group_id and g.session_id != d.session_id and g.global = 1 and p.owner_id = i.uniqueid and i.department_uniqueid = d.uniqueid);

alter table room_feature add session_id number(20);

alter table room_feature add constraint fk_room_feature_session foreign key (session_id) references sessions (uniqueid) on delete cascade;

update room_feature set session_id = (select min(uniqueid) from sessions) where discriminator = 'global';

insert into room_feature select 
	room_feature_seq.nextval as uniqueid,
	f.discriminator, f.label, f.sis_reference, f.sis_value, f.department_id, f.abbv, s.uniqueid as session_id
from room_feature f, sessions s
where f.discriminator = 'global' and not exists
	(select * from room_feature x where x.session_id = s.uniqueid and x.label = f.label);

update room_join_room_feature x set x.feature_id =
	(select n.uniqueid from room_feature n, room_feature f, room r where
	x.feature_id = f.uniqueid and x.room_id = r.uniqueid and n.label = f.label and n.session_id = r.session_id)
where x.feature_id in
	(select f.uniqueid from room_feature f, room r where
	x.feature_id = f.uniqueid and x.room_id = r.uniqueid and f.discriminator = 'global' and f.session_id != r.session_id);

update room_join_room_feature x set x.feature_id =
	(select n.uniqueid from room_feature n, room_feature f, non_university_location r where
	x.feature_id = f.uniqueid and x.room_id = r.uniqueid and n.label = f.label and n.session_id = r.session_id)
where x.feature_id in
	(select f.uniqueid from room_feature f, non_university_location r where
	x.feature_id = f.uniqueid and x.room_id = r.uniqueid and f.discriminator = 'global' and f.session_id != r.session_id);

update room_feature_pref p set p.room_feature_id = 
	(select n.uniqueid from room_feature g, room_feature n, class_ c, scheduling_subpart t, instr_offering_config e, instructional_offering o
	where g.uniqueid = p.room_feature_id and n.session_id = o.session_id and n.label = g.label and p.owner_id = c.uniqueid and c.subpart_id = t.uniqueid and t.config_id = e.uniqueid and e.instr_offr_id = o.uniqueid)
where p.room_feature_id in (
	select g.uniqueid from room_feature g, class_ c, scheduling_subpart t, instr_offering_config e, instructional_offering o
	where  g.uniqueid = p.room_feature_id and g.session_id != o.session_id and g.discriminator = 'global' and p.owner_id = c.uniqueid and c.subpart_id = t.uniqueid and t.config_id = e.uniqueid and e.instr_offr_id = o.uniqueid);

update room_feature_pref p set p.room_feature_id = 
	(select n.uniqueid from room_feature g, room_feature n, scheduling_subpart t, instr_offering_config e, instructional_offering o
	where g.uniqueid = p.room_feature_id and n.session_id = o.session_id and n.label = g.label and p.owner_id = t.uniqueid and t.config_id = e.uniqueid and e.instr_offr_id = o.uniqueid)
where p.room_feature_id in (
	select g.uniqueid from room_feature g, class_ c, scheduling_subpart t, instr_offering_config e, instructional_offering o
	where  g.uniqueid = p.room_feature_id and g.session_id != o.session_id and g.discriminator = 'global'  and p.owner_id = t.uniqueid and t.config_id = e.uniqueid and e.instr_offr_id = o.uniqueid);

update room_feature_pref p set p.room_feature_id = 
	(select n.uniqueid from room_feature g, room_feature n, sessions s
	where g.uniqueid = p.room_feature_id and n.session_id = s.uniqueid and n.label = g.label and p.owner_id = s.uniqueid)
where p.room_feature_id in (
	select g.uniqueid from room_feature g, sessions s
	where  g.uniqueid = p.room_feature_id and g.session_id != s.uniqueid and g.discriminator = 'global' and p.owner_id = s.uniqueid);

update room_feature_pref p set p.room_feature_id = 
	(select n.uniqueid from room_feature g, room_feature n, exam x
	where g.uniqueid = p.room_feature_id and n.session_id = x.session_id and n.label = g.label and p.owner_id = x.uniqueid)
where p.room_feature_id in (
	select g.uniqueid from room_feature g, exam x
	where  g.uniqueid = p.room_feature_id and g.session_id != x.session_id and g.discriminator = 'global' and p.owner_id = x.uniqueid);

update room_feature_pref p set p.room_feature_id = 
	(select n.uniqueid from room_feature g, room_feature n, departmental_instructor i, department d
	where g.uniqueid = p.room_feature_id and n.session_id = d.session_id and n.label = g.label and p.owner_id = i.uniqueid and i.department_uniqueid = d.uniqueid)
where p.room_feature_id in (
	select g.uniqueid from room_feature g, departmental_instructor i, department d
	where  g.uniqueid = p.room_feature_id and g.session_id != d.session_id and g.discriminator = 'global' and p.owner_id = i.uniqueid and i.department_uniqueid = d.uniqueid);


/*
 * Update database version
 */

update application_config set value='75' where name='tmtbl.db.version';

commit;
