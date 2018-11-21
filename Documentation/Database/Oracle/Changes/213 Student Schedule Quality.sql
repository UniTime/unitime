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

insert into solver_parameter_def (select
	solver_parameter_def_seq.nextval as uniqueid,
	'Distances.ShortDistanceAccommodationReference' as name,
	'SD' as default_value,
	'Need short distances accommodation reference' as description,
	'text' as type,
	(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'StudentSct') as ord,
	1 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSct');

insert into solver_parameter_def (select
	solver_parameter_def_seq.nextval as uniqueid,
	'StudentLunch.StartSlot' as name,
	'132' as default_value,
	'Student Lunch Breeak: first time slot' as description,
	'integer' as type,
	(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'StudentSct') as ord,
	1 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSct');

insert into solver_parameter_def (select
	solver_parameter_def_seq.nextval as uniqueid,
	'StudentLunch.EndStart' as name,
	'156' as default_value,
	'Student Lunch Breeak: last time slot' as description,
	'integer' as type,
	(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'StudentSct') as ord,
	1 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSct');

insert into solver_parameter_def (select
	solver_parameter_def_seq.nextval as uniqueid,
	'StudentLunch.Length' as name,
	'6' as default_value,
	'Student Lunch Breeak: time for lunch (number of slots)' as description,
	'integer' as type,
	(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'StudentSct') as ord,
	1 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSct');

insert into solver_parameter_def (select
	solver_parameter_def_seq.nextval as uniqueid,
	'TravelTime.MaxTravelGap' as name,
	'12' as default_value,
	'Travel Time: max travel gap (number of slots)' as description,
	'integer' as type,
	(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'StudentSct') as ord,
	1 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSct');

insert into solver_parameter_def (select
	solver_parameter_def_seq.nextval as uniqueid,
	'WorkDay.WorkDayLimit' as name,
	'72' as default_value,
	'Work Day: initial allowance (number of slots)' as description,
	'integer' as type,
	(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'StudentSct') as ord,
	1 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSct');

insert into solver_parameter_def (select
	solver_parameter_def_seq.nextval as uniqueid,
	'WorkDay.EarlySlot' as name,
	'102' as default_value,
	'Work Day: early morning time slot' as description,
	'integer' as type,
	(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'StudentSct') as ord,
	1 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSct');

insert into solver_parameter_def (select
	solver_parameter_def_seq.nextval as uniqueid,
	'WorkDay.LateSlot' as name,
	'210' as default_value,
	'Work Day: late evening time slot' as description,
	'integer' as type,
	(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'StudentSct') as ord,
	1 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSct');

insert into solver_parameter_def (select
	solver_parameter_def_seq.nextval as uniqueid,
	'StudentWeights.BackToBackDistance' as name,
	'6' as default_value,
	'Work Day: max back-to-back distance (number of slots)' as description,
	'integer' as type,
	(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'StudentSct') as ord,
	1 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSct');

insert into solver_parameter_def (select
	solver_parameter_def_seq.nextval as uniqueid,
	'StudentWeights.ShortDistanceConflict' as name,
	'0.2000' as default_value,
	'Distance conflict (students needed short distances)' as description,
	'double' as type,
	(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'StudentSctWeights') as ord,
	1 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSctWeights');

insert into solver_parameter_def (select
	solver_parameter_def_seq.nextval as uniqueid,
	'StudentWeights.LunchBreakFactor' as name,
	'0.0050' as default_value,
	'Lunch break conflict' as description,
	'double' as type,
	(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'StudentSctWeights') as ord,
	1 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSctWeights');

insert into solver_parameter_def (select
	solver_parameter_def_seq.nextval as uniqueid,
	'StudentWeights.TravelTimeFactor' as name,
	'0.0010' as default_value,
	'Travel time conflict (multiplied by distance in minutes)' as description,
	'double' as type,
	(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'StudentSctWeights') as ord,
	1 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSctWeights');

insert into solver_parameter_def (select
	solver_parameter_def_seq.nextval as uniqueid,
	'StudentWeights.BackToBackFactor' as name,
	'-0.0010' as default_value,
	'Back-to-back conflict (negative value: prefer no gaps)' as description,
	'double' as type,
	(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'StudentSctWeights') as ord,
	1 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSctWeights');

insert into solver_parameter_def (select
	solver_parameter_def_seq.nextval as uniqueid,
	'StudentWeights.WorkDayFactor' as name,
	'0.0100' as default_value,
	'Work-day conflict (multiplied by the number of hours over the allowance)' as description,
	'double' as type,
	(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'StudentSctWeights') as ord,
	1 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSctWeights');

insert into solver_parameter_def (select
	solver_parameter_def_seq.nextval as uniqueid,
	'StudentWeights.TooEarlyFactor' as name,
	'0.0500' as default_value,
	'Too early conflict' as description,
	'double' as type,
	(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'StudentSctWeights') as ord,
	1 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSctWeights');

insert into solver_parameter_def (select
	solver_parameter_def_seq.nextval as uniqueid,
	'StudentWeights.TooLateFactor' as name,
	'0.0250' as default_value,
	'Too late conflict' as description,
	'double' as type,
	(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'StudentSctWeights') as ord,
	1 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSctWeights');

update solver_parameter_def set default_value = '0.0500'
	where name = 'StudentWeights.DistanceConflict' and default_value = '0.0100';

update solver_parameter_def set default_value = '1.0000'
	where name = 'StudentWeights.TimeOverlapFactor' and default_value = '0.5000';

update solver_parameter_def set default_value = '0.3750'
	where name = 'StudentWeights.SelectionFactor' and default_value = '0.125';

/*
 * Update database version
 */

update application_config set value='213' where name='tmtbl.db.version';

commit;
