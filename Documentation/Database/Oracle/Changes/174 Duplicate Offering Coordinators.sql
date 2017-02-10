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

alter table offering_coordinator add uniqueid number(20);
update offering_coordinator set uniqueid = pref_group_seq.nextval;
alter table offering_coordinator add constraint nn_offering_coord_id check (uniqueid is not null);
alter table offering_coordinator drop constraint pk_offering_coordinator;
alter table offering_coordinator add constraint pk_offering_coordinator primary key (uniqueid);

alter table offering_coordinator add percent_share number(3,0);
update offering_coordinator set percent_share = 0;
alter table offering_coordinator add constraint nn_offering_coord_share (percent_share is not null);

alter table teaching_request add percent_share number(3,0);
update teaching_request set percent_share = 0;
alter table teaching_request add constraint nn_teachreq_share (percent_share is not null);

/*
 * Update database version
 */

update application_config set value='174' where name='tmtbl.db.version';

commit;
