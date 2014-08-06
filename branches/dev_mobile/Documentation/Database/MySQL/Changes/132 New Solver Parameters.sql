/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/

select 32767 * next_hi into @id from hibernate_unique_key;
select max(ord) + 1 into @ord from solver_parameter_group;
select uniqueid into @ggen from solver_parameter_group where name='General';
select max(ord) into @ogen from solver_parameter_def where solver_param_group_id=@ggen;
select uniqueid into @gcmp from solver_parameter_group where name='Comparator';
select max(ord) into @ocmp from solver_parameter_def where solver_param_group_id=@gcmp;
select uniqueid into @gnei from solver_parameter_group where name='Neighbour';
select max(ord) into @onei from solver_parameter_def where solver_param_group_id=@gnei;
select uniqueid into @gsct from solver_parameter_group where name='StudentSct';
select max(ord) into @osct from solver_parameter_def where solver_param_group_id=@gsct;
select uniqueid into @gval from solver_parameter_group where name='Value';
select max(ord) into @oval from solver_parameter_def where solver_param_group_id=@gval;

insert into solver_parameter_group (uniqueid, name, description, ord, param_type) values
	(@id + 0, 'InstructorLunch', 'Instructor Lunch Breaks', @ord, 0),
	(@id + 1, 'Curriculum', 'Curriculum Conversion', @ord + 1, 0);

insert into solver_parameter_def
	(uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id) values
	(@id + 02, 'InstructorLunch.Enabled', 'false', 'Enable Instructor Lunch Breaks', 'boolean', 0, 1, @id),
	(@id + 03, 'InstructorLunch.Weight', '0.18', 'Weight', 'double', 1, 1, @id),
	(@id + 04, 'InstructorLunch.StartSlot', '132', 'Start slot', 'integer', 2, 1, @id),
	(@id + 05, 'InstructorLunch.EndSlot', '162', 'End slot', 'integer', 3, 1, @id),
	(@id + 06, 'InstructorLunch.Length', '6', 'Minum break length (in slots)', 'integer', 4, 1, @id),
	(@id + 07, 'InstructorLunch.Multiplier', '1.15', 'Violations multiplication factor', 'double', 5, 1, @id),
	(@id + 08, 'InstructorLunch.InfoShowViolations', 'false', 'Show violations in the solution info', 'boolean', 6, 1, @id),
			
	(@id + 09, 'Curriculum.Phases', 'HC,Deluge', 'Search Phases', 'text', 0, 0, @id + 1),
	(@id + 10, 'Curriculum.HC.MaxIdle', '1000', 'Hill Climber: max idle iterations', 'integer', 1, 1, @id + 1),
	(@id + 11, 'Curriculum.Deluge.Factor', '0.999999', 'Great Deluge: cooling rate', 'double', 2, 1, @id + 1),
	(@id + 12, 'Curriculum.Deluge.UpperBound', '1.25', 'Great Deluge: upper bound', 'double', 3, 1, @id + 1),
	(@id + 13, 'Curriculum.Deluge.LowerBound', '0.75', 'Great Deluge: lower bound', 'double', 4, 1, @id + 1),

	(@id + 14, 'Global.LoadStudentInstructorConflicts', 'false', 'Load student instructor conflicts', 'boolean', @ogen, 1, @ggen),
	(@id + 15, 'General.AutomaticHierarchicalConstraints', '', 'Automatic hierarchical constraints (comma separated list of a preference, a constraint name and optional date pattern)', 'text', @ogen + 1, 1, @ggen),
	(@id + 16, 'General.CompleteSolutionFixInterval', '1', 'Fix complete solution interval (min number of iteration from the previous best solution)', 'integer', @ogen + 2, 0, @ggen),
	(@id + 17, 'General.IncompleteSolutionFixInterval', '5000', 'Fix incomplete solution interval (min number of non improving iterations, -1 to disable)', 'integer', @ogen + 3, 0, @ggen),
	(@id + 18, 'General.SearchAlgorithm', 'Default', 'Search Algorithm', 'enum(Default,Experimental)', @ogen + 4, 1, @ggen),
	(@id + 19, 'General.DeterministicStudentSectioning', 'false', 'Deterministic student sectioning', 'boolean', @ogen + 5, 1, @ggen),
			
	(@id + 20, 'Comparator.RoomSizeWeight', '0.001', 'Excessive room size weight', 'double', @ocmp, 1, @gcmp),
	(@id + 21, 'Comparator.RoomSizeFactor', '1.05', 'Excessive room size factor', 'double', @ocmp + 1, 1, @gcmp),
	(@id + 22, 'FlexibleConstraint.Weight', '%Comparator.ContrPreferenceWeight%', 'Flexible constraint weight', 'double', @ocmp + 2, 0, @gcmp),
			
	(@id + 23, 'Construction.Class', 'net.sf.cpsolver.coursett.heuristics.NeighbourSelectionWithSuggestions', 'Construction: heuristics', 'text', @onei, 0, @gnei),
	(@id + 24, 'Construction.UntilComplete', 'false', 'Construction: use construction heuristic untill a complete solution is found', 'boolean', @onei + 1, 0, @gnei),
	(@id + 25, 'Search.GreatDeluge', 'true', 'Use great deluge (instead of simulated annealing)', 'boolean', @onei + 2, 1, @gnei),
	(@id + 26, 'HillClimber.MaxIdle', '10000', 'Hill Climber: max idle iterations', 'integer', @onei + 3, 1, @gnei),
	(@id + 27, 'HillClimber.AdditionalNeighbours', 'net.sf.cpsolver.coursett.neighbourhoods.TimeChange;net.sf.cpsolver.coursett.neighbourhoods.RoomChange;net.sf.cpsolver.coursett.neighbourhoods.TimeSwap@0.01;net.sf.cpsolver.coursett.neighbourhoods.RoomSwap@0.01',
				'Hill Climber: Additional neighbourhoods', 'text', @onei + 4, 0, @gnei),
	(@id + 28, 'GreatDeluge.CoolRate', '0.9999999', 'Great Deluge: cooling rate', 'double', @onei + 5, 1, @gnei),
	(@id + 29, 'GreatDeluge.UpperBoundRate', '1.05', 'Great Deluge: upper bound', 'double', @onei + 6, 1, @gnei),
	(@id + 30, 'GreatDeluge.LowerBoundRate', '0.95', 'Great Deluge: lower bound', 'double', @onei + 7, 1, @gnei),
	(@id + 31, 'GreatDeluge.AdditionalNeighbours', 'net.sf.cpsolver.coursett.neighbourhoods.TimeChange;net.sf.cpsolver.coursett.neighbourhoods.RoomChange;net.sf.cpsolver.coursett.neighbourhoods.TimeSwap@0.01;net.sf.cpsolver.coursett.neighbourhoods.RoomSwap@0.01',
				'Great Deluge: Additional neighbourhoods', 'text', @onei + 8, 0, @gnei),
	(@id + 32, 'SimulatedAnnealing.InitialTemperature', '1.5', 'Simulated Annealing: initial temperature', 'double', @onei + 9, 1, @gnei),
	(@id + 33, 'SimulatedAnnealing.TemperatureLength', '2500', 'Simulated Annealing: temperature length (number of iterations between temperature decrements)', 'integer', @onei + 10, 1, @gnei),
	(@id + 34, 'SimulatedAnnealing.CoolingRate', '0.95', 'Simulated Annealing: cooling rate', 'double', @onei + 11, 1, @gnei),
	(@id + 35, 'SimulatedAnnealing.ReheatLengthCoef', '5.0', 'Simulated Annealing: temperature re-heat length coefficient', 'double', @onei + 12, 1, @gnei),
	(@id + 36, 'SimulatedAnnealing.ReheatRate', '-1', 'Simulated Annealing: temperature re-heating rate (default (1/coolingRate)^(reheatLengthCoef*1.7))', 'double', @onei + 13, 0, @gnei),
	(@id + 37, 'SimulatedAnnealing.RestoreBestLengthCoef', '-1', 'Simulated Annealing: restore best length coefficient (default reheatLengthCoef^2)', 'double', @onei + 14, 0, @gnei),
	(@id + 38, 'SimulatedAnnealing.StochasticHC', 'false', 'Simulated Annealing: stochastic search acceptance', 'boolean', @onei + 15, 0, @gnei),
	(@id + 39, 'SimulatedAnnealing.RelativeAcceptance', 'true', 'Simulated Annealing: relative acceptance (compare with current solution, not the best one)', 'boolean', @onei + 16, 0, @gnei),
	(@id + 40, 'SimulatedAnnealing.AdditionalNeighbours', 'net.sf.cpsolver.coursett.neighbourhoods.TimeChange;net.sf.cpsolver.coursett.neighbourhoods.RoomChange;net.sf.cpsolver.coursett.neighbourhoods.TimeSwap@0.01;net.sf.cpsolver.coursett.neighbourhoods.RoomSwap@0.01',
				'Simulated Annealing: Additional neighbourhoods', 'text', @onei + 17, 0, @gnei),
				
	(@id + 41, 'Reservation.CanAssignOverTheLimit', 'true', 'Allow over limit for individual reservations', 'boolean', @osct, 1, @gsct),
			
	(@id + 42, 'Placement.FlexibleConstrPreferenceWeight1', '%Placement.ConstrPreferenceWeight1%', 'Flexible constraint preference weight (level 1)', 'double', @oval, 0, @gval),
	(@id + 43, 'Placement.FlexibleConstrPreferenceWeight2', '%FlexibleConstraint.Weight%', 'Flexible constraint preference weight (level 2)', 'double', @oval + 1, 0, @gval),
	(@id + 44, 'Placement.FlexibleConstrPreferenceWeight3', '%Placement.ConstrPreferenceWeight3%', 'Flexible constraint preference weight (level 3)', 'double', @oval + 2, 0, @gval),
	(@id + 45, 'ClassWeightProvider.Class', 'Default Class Weights', 'Class Weights', 'enum(Default Class Weights,Average Hours A Week Class Weights)', @ogen + 6, 1, @ggen);


update hibernate_unique_key set next_hi=next_hi+1;

create table param_dupl as
	select d.uniqueid from solver_parameter_def d where
	(select count(*) from solver_parameter_def x where x.name = d.name and x.solver_param_group_id = d.solver_param_group_id and x.uniqueid < d.uniqueId) > 0;
delete from solver_parameter_def where uniqueid in (select x.uniqueid from param_dupl x);
drop table param_dupl;

create table param_ord_fix as 
	select d.uniqueid as param_id, (select count(*) from solver_parameter_def x where x.solver_param_group_id = d.solver_param_group_id and (x.ord < d.ord or (x.ord = d.ord and x.uniqueid < d.uniqueid))) as new_ord
	from solver_parameter_def d order by d.solver_param_group_id, d.ord, d.uniqueid;
update solver_parameter_def set ord = (select new_ord from param_ord_fix where param_id = uniqueid);
drop table param_ord_fix;

/*
 * Update database version
 */

update application_config set value='132' where name='tmtbl.db.version';

commit;
