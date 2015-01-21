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

select 32767 * next_hi into @id from hibernate_unique_key;
select max(ord)+1 into @ord from solver_parameter_group;

insert into solver_parameter_group (uniqueid, name, description, ord, param_type) values
	(@id, 'StudentSctWeights', 'Student Weitghts', @ord, 2);

insert into solver_parameter_def
	(uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id) values
	(@id+1, 'StudentWeights.Priority', '0.5010', 'Priority', 'double', 0, 1, @id),
	(@id+2, 'StudentWeights.FirstAlternative', '0.5010', 'First alternative', 'double', 1, 1, @id),
	(@id+3, 'StudentWeights.SecondAlternative', '0.2510', 'Second alternative', 'double', 2, 1, @id),
	(@id+4, 'StudentWeights.DistanceConflict', '0.0100', 'Distance conflict', 'double', 3, 1, @id),
	(@id+5, 'StudentWeights.TimeOverlapFactor', '0.5000', 'Time overlap', 'double', 4, 1, @id),
	(@id+6, 'StudentWeights.TimeOverlapMaxLimit', '0.5000', 'Time overlap limit', 'double', 5, 0, @id),
	(@id+7, 'StudentWeights.BalancingFactor', '0.0050', 'Section balancing', 'double', 6, 1, @id),
	(@id+8, 'StudentWeights.AlternativeRequestFactor', '0.1260', 'Alternative request (equal weights)', 'double', 7, 1, @id),
	(@id+9, 'StudentWeights.LeftoverSpread', 'false', 'Spread leftover weight equaly', 'boolean', 8, 1, @id);
	
select uniqueid into @gid_basic from solver_parameter_group where name='StudentSctBasic';
select max(ord)+1 into @ord_basic from solver_parameter_def where solver_param_group_id=@gid_basic;

insert into solver_parameter_def
	(uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id) values
	(@id+10, 'StudentWeights.Mode', 'Priority', 'Student weights', 'enum(Priority,Equal,Legacy)', @ord_basic, 1, @gid_basic);

select uniqueid into @gid_general from solver_parameter_group where name='StudentSct';
select max(ord)+1 into @ord_general from solver_parameter_def where solver_param_group_id=@gid_general;
insert into solver_parameter_def
	(uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id) values
	(@id+11, 'StudentSct.TimeOverlaps', 'true', 'Use time overlaps', 'boolean', @ord_general, 1, @gid_general),
	(@id+12, 'Load.TweakLimits', 'false', 'Tweak class limits to fit all enrolled students', 'boolean', @ord_general+1, 1, @gid_general);

update hibernate_unique_key set next_hi=next_hi+1;

/**
 * Update database version
 */

update application_config set value='64' where name='tmtbl.db.version';

commit;
		