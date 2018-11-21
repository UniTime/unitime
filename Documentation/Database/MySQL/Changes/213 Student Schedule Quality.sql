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
select uniqueid into @ggen from solver_parameter_group where name='StudentSct';
select max(ord) into @ogen from solver_parameter_def where solver_param_group_id=@ggen;
select uniqueid into @gsw from solver_parameter_group where name='StudentSctWeights';
select max(ord) into @osw from solver_parameter_def where solver_param_group_id=@gsw;

insert into solver_parameter_def
	(uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id) values
	(@id +  0, 'Distances.ShortDistanceAccommodationReference', 'SD', 'Need short distances accommodation reference', 'text', @ogen + 1, 1, @ggen),
	(@id +  1, 'StudentLunch.StartSlot', '132', 'Student Lunch Breeak: first time slot', 'integer',  @ogen + 2, 1, @ggen),
	(@id +  2, 'StudentLunch.EndStart', '156', 'Student Lunch Breeak: last time slot', 'integer',  @ogen + 3, 1, @ggen),
	(@id +  3, 'StudentLunch.Length', '6', 'Student Lunch Breeak: time for lunch (number of slots)', 'integer',  @ogen + 4, 1, @ggen),
	(@id +  4, 'TravelTime.MaxTravelGap', '12', 'Travel Time: max travel gap (number of slots)', 'integer',  @ogen + 5, 1, @ggen),
	(@id +  5, 'WorkDay.WorkDayLimit', '72', 'Work Day: initial allowance (number of slots)', 'integer',  @ogen + 6, 1, @ggen),
	(@id +  6, 'WorkDay.EarlySlot', '102', 'Work Day: early morning time slot', 'integer',  @ogen + 7, 1, @ggen),
	(@id +  7, 'WorkDay.LateSlot', '210', 'Work Day: late evening time slot', 'integer',  @ogen + 8, 1, @ggen),
	(@id +  8, 'StudentWeights.BackToBackDistance', '6', 'Work Day: max back-to-back distance (number of slots)', 'integer',  @ogen + 9, 1, @ggen),
	(@id +  9, 'StudentWeights.ShortDistanceConflict', '0.2000', 'Distance conflict (students needed short distances)', 'double', @osw + 1, 1, @gsw),
	(@id + 10, 'StudentWeights.LunchBreakFactor', '0.0050', 'Lunch break conflict', 'double', @osw + 2, 1, @gsw),
	(@id + 11, 'StudentWeights.TravelTimeFactor', '0.0010', 'Travel time conflict (multiplied by distance in minutes)', 'double', @osw + 3, 1, @gsw),
	(@id + 12, 'StudentWeights.BackToBackFactor', '-0.0010', 'Back-to-back conflict (negative value: prefer no gaps)', 'double', @osw + 4, 1, @gsw),
	(@id + 13, 'StudentWeights.WorkDayFactor', '0.0100', 'Work-day conflict (multiplied by the number of hours over the allowance)', 'double', @osw + 5, 1, @gsw),
	(@id + 14, 'StudentWeights.TooEarlyFactor', '0.0500', 'Too early conflict', 'double', @osw + 6, 1, @gsw),
	(@id + 15, 'StudentWeights.TooLateFactor', '0.0250', 'Too late conflict', 'double', @osw + 7, 1, @gsw);

update hibernate_unique_key set next_hi=next_hi+1;

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
