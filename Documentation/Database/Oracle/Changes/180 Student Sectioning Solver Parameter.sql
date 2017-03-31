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

update solver_parameter_def set
	name = 'General.StudentSectioning',
	default_value='Default',
	description = 'Student Sectioning',
	type = 'enum(Default,Deterministic,Branch & Bound,Local Search,B&B Groups)'
	where name = 'General.DeterministicStudentSectioning';

update solver_parameter set value='Default'
	where solver_param_def_id = (select uniqueid from solver_parameter_def where name = 'General.StudentSectioning')
	and value = 'false';

update solver_parameter set value='Deterministic'
	where solver_param_def_id = (select uniqueid from solver_parameter_def where name = 'General.StudentSectioning')
	and value = 'true';

/*
 * Update database version
 */

update application_config set value='180' where name='tmtbl.db.version';

commit;
