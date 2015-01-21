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
 * Add average period to exam
 */
 
alter table exam add avg_period number(10);

/**
 * Add organized flag to itype_desc
 */
 
alter table itype_desc add organized number(1);

update itype_desc set organized = 0;
update itype_desc set organized = 1 where abbv like 'Lec%';
update itype_desc set organized = 1 where abbv like 'Rec%';
update itype_desc set organized = 1 where abbv like 'Prsn%';
update itype_desc set organized = 1 where abbv like 'Lab%';
update itype_desc set organized = 1 where abbv like 'LabP%';
update itype_desc set organized = 1 where abbv like 'Stdo%';
 
alter table itype_desc modify organized add constraint nn_itype_desc_organized check (organized is not null);
 
 /**
  * Add email to departmental instructor
  */

alter table departmental_instructor add email varchar2(200);
  
/*
 * Update database version
 */

update application_config set value='24' where name='tmtbl.db.version';

commit;  
