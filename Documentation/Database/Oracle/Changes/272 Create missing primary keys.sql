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

alter table solver_parameter add constraint pk_solver_parameter
	primary key (uniqueid);
	
alter table room_join_room_feature drop constraint uk_room_join_room_feat_rm_feat;

alter table room_join_room_feature add constraint pk_room_join_room_feature
	primary key (room_id, feature_id);

alter table event_join_event_contact add constraint pk_event_join_event_contact 
	primary key (event_id, event_contact_id);

alter table student_note add constraint pk_student_note
	primary key (uniqueid);


/*
 * Update database version
 */
  
update application_config set value='272' where name='tmtbl.db.version';

commit;
