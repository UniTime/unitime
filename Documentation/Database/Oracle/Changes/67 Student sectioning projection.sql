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

insert into solver_parameter_def
	(select solver_parameter_def_seq.nextval as uniqueid,
	'StudentSct.ProjectedCourseDemadsClass' as name, 
	'None' as default_value, 
	'Projected student course demands' as description, 
	'enum(None,Last Like Student Course Demands,Projected Student Course Demands,Curricula Course Demands,Curricula Last Like Course Demands,Student Course Requests,Enrolled Student Course Demands)' as type, 
	3 as ord, 
	1 as visible, 
	uniqueid as solver_param_group_id from solver_parameter_group where name='StudentSctBasic');

update solver_parameter_def set
	type = 'enum(Initial,MPP,Projection)'
	where name = 'StudentSctBasic.Mode';

/**
 * Update database version
 */

update application_config set value='67' where name='tmtbl.db.version';

commit;
		