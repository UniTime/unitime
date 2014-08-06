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

insert into solver_parameter_group (uniqueid, name, description, condition, ord, param_type) values
	(solver_parameter_group_seq.nextval, 'InstructorLunch', 'Instructor Lunch Breaks', '', -1, 0);
update solver_parameter_group g set g.ord = ( select max(x.ord)+1 from solver_parameter_group x ) where g.name='InstructorLunch';

insert into solver_parameter_group (uniqueid, name, description, condition, ord, param_type) values
	(solver_parameter_group_seq.nextval, 'Curriculum', 'Curriculum Conversion', '', -1, 0);
update solver_parameter_group g set g.ord = ( select max(x.ord)+1 from solver_parameter_group x ) where g.name='Curriculum';

insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'InstructorLunch.Enabled' as name,
		'false' as default_value,
		'Enable Instructor Lunch Breaks' as description,
		'boolean' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'InstructorLunch') as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'InstructorLunch');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'InstructorLunch.Weight' as name,
		'0.18' as default_value,
		'Weight' as description,
		'double' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'InstructorLunch') as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'InstructorLunch');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'InstructorLunch.StartSlot' as name,
		'132' as default_value,
		'Start slot' as description,
		'integer' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'InstructorLunch') as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'InstructorLunch');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'InstructorLunch.EndSlot' as name,
		'162' as default_value,
		'End slot' as description,
		'integer' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'InstructorLunch') as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'InstructorLunch');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'InstructorLunch.Length' as name,
		'6' as default_value,
		'Minum break length (in slots)' as description,
		'integer' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'InstructorLunch') as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'InstructorLunch');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'InstructorLunch.Multiplier' as name,
		'1.15' as default_value,
		'Violations multiplication factor' as description,
		'double' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'InstructorLunch') as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'InstructorLunch');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'InstructorLunch.InfoShowViolations' as name,
		'false' as default_value,
		'Show violations in the solution info' as description,
		'boolean' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'InstructorLunch') as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'InstructorLunch');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Curriculum.Phases' as name,
		'HC,Deluge' as default_value,
		'Search Phases' as description,
		'text' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'Curriculum') as ord,
		0 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'Curriculum');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Curriculum.HC.MaxIdle' as name,
		'1000' as default_value,
		'Hill Climber: max idle iterations' as description,
		'integer' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'Curriculum') as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'Curriculum');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Curriculum.Deluge.Factor' as name,
		'0.999999' as default_value,
		'Great Deluge: cooling rate' as description,
		'double' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'Curriculum') as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'Curriculum');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Curriculum.Deluge.UpperBound' as name,
		'1.25' as default_value,
		'Great Deluge: upper bound' as description,
		'double' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'Curriculum') as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'Curriculum');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Curriculum.Deluge.LowerBound' as name,
		'0.75' as default_value,
		'Great Deluge: lower bound' as description,
		'double' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'Curriculum') as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'Curriculum');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Global.LoadStudentInstructorConflicts' as name,
		'false' as default_value,
		'Load student instructor conflicts' as description,
		'boolean' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'General') as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'General');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'General.AutomaticHierarchicalConstraints' as name,
		'' as default_value,
		'Automatic hierarchical constraints (comma separated list of a preference, a constraint name and optional date pattern)' as description,
		'text' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'General') as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'General');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'General.CompleteSolutionFixInterval' as name,
		'1' as default_value,
		'Fix complete solution interval (min number of iteration from the previous best solution)' as description,
		'integer' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'General') as ord,
		0 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'General');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'General.IncompleteSolutionFixInterval' as name,
		'5000' as default_value,
		'Fix incomplete solution interval (min number of non improving iterations, -1 to disable' as description,
		'integer' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'General') as ord,
		0 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'General');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'General.SearchAlgorithm' as name,
		'Default' as default_value,
		'Search Algorithm' as description,
		'enum(Default,Experimental)' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'General') as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'General');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'General.DeterministicStudentSectioning' as name,
		'false' as default_value,
		'Deterministic student sectioning' as description,
		'boolean' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'General') as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'General');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Comparator.RoomSizeWeight' as name,
		'0.001' as default_value,
		'Excessive room size weight' as description,
		'double' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'Comparator') as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'Comparator');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Comparator.RoomSizeFactor' as name,
		'1.05' as default_value,
		'Excessive room size factor' as description,
		'double' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'Comparator') as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'Comparator');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'FlexibleConstraint.Weight' as name,
		'%Comparator.ContrPreferenceWeight%' as default_value,
		'Flexible constraint weight' as description,
		'double' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'Comparator') as ord,
		0 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'Comparator');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Construction.Class' as name,
		'net.sf.cpsolver.coursett.heuristics.NeighbourSelectionWithSuggestions' as default_value,
		'Construction: heuristics' as description,
		'text' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'Neighbour') as ord,
		0 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'Neighbour');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Construction.UntilComplete' as name,
		'false' as default_value,
		'Construction: use construction heuristic untill a complete solution is found' as description,
		'boolean' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'Neighbour') as ord,
		0 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'Neighbour');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Search.GreatDeluge' as name,
		'true' as default_value,
		'Use great deluge (instead of simulated annealing)' as description,
		'boolean' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'Neighbour') as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'Neighbour');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'HillClimber.MaxIdle' as name,
		'10000' as default_value,
		'Hill Climber: max idle iterations' as description,
		'integer' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'Neighbour') as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'Neighbour');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'HillClimber.AdditionalNeighbours' as name,
		'net.sf.cpsolver.coursett.neighbourhoods.TimeChange;net.sf.cpsolver.coursett.neighbourhoods.RoomChange;net.sf.cpsolver.coursett.neighbourhoods.TimeSwap@0.01;net.sf.cpsolver.coursett.neighbourhoods.RoomSwap@0.01' as default_value,
		'Hill Climber: Additional neighbourhoods' as description,
		'text' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'Neighbour') as ord,
		0 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'Neighbour');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'GreatDeluge.CoolRate' as name,
		'0.9999999' as default_value,
		'Great Deluge: cooling rate' as description,
		'double' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'Neighbour') as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'Neighbour');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'GreatDeluge.UpperBoundRate' as name,
		'1.05' as default_value,
		'Great Deluge: upper bound' as description,
		'double' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'Neighbour') as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'Neighbour');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'GreatDeluge.LowerBoundRate' as name,
		'0.95' as default_value,
		'Great Deluge: lower bound' as description,
		'double' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'Neighbour') as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'Neighbour');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'GreatDeluge.AdditionalNeighbours' as name,
		'net.sf.cpsolver.coursett.neighbourhoods.TimeChange;net.sf.cpsolver.coursett.neighbourhoods.RoomChange;net.sf.cpsolver.coursett.neighbourhoods.TimeSwap@0.01;net.sf.cpsolver.coursett.neighbourhoods.RoomSwap@0.01' as default_value,
		'Great Deluge: Additional neighbourhoods' as description,
		'text' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'Neighbour') as ord,
		0 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'Neighbour');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'SimulatedAnnealing.InitialTemperature' as name,
		'1.5' as default_value,
		'Simulated Annealing: initial temperature' as description,
		'double' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'Neighbour') as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'Neighbour');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'SimulatedAnnealing.TemperatureLength' as name,
		'2500' as default_value,
		'Simulated Annealing: temperature length (number of iterations between temperature decrements)' as description,
		'integer' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'Neighbour') as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'Neighbour');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'SimulatedAnnealing.CoolingRate' as name,
		'0.95' as default_value,
		'Simulated Annealing: cooling rate' as description,
		'double' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'Neighbour') as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'Neighbour');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'SimulatedAnnealing.ReheatLengthCoef' as name,
		'5.0' as default_value,
		'Simulated Annealing: temperature re-heat length coefficient' as description,
		'double' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'Neighbour') as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'Neighbour');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'SimulatedAnnealing.ReheatRate' as name,
		'-1' as default_value,
		'Simulated Annealing: temperature re-heating rate (default (1/coolingRate)^(reheatLengthCoef*1.7))' as description,
		'double' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'Neighbour') as ord,
		0 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'Neighbour');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'SimulatedAnnealing.RestoreBestLengthCoef' as name,
		'-1' as default_value,
		'Simulated Annealing: restore best length coefficient (default reheatLengthCoef^2)' as description,
		'double' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'Neighbour') as ord,
		0 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'Neighbour');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'SimulatedAnnealing.StochasticHC' as name,
		'false' as default_value,
		'Simulated Annealing: stochastic search acceptance' as description,
		'boolean' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'Neighbour') as ord,
		0 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'Neighbour');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'SimulatedAnnealing.RelativeAcceptance' as name,
		'true' as default_value,
		'Simulated Annealing: relative acceptance (compare with current solution, not the best one)' as description,
		'boolean' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'Neighbour') as ord,
		0 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'Neighbour');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'SimulatedAnnealing.AdditionalNeighbours' as name,
		'net.sf.cpsolver.coursett.neighbourhoods.TimeChange;net.sf.cpsolver.coursett.neighbourhoods.RoomChange;net.sf.cpsolver.coursett.neighbourhoods.TimeSwap@0.01;net.sf.cpsolver.coursett.neighbourhoods.RoomSwap@0.01' as default_value,
		'Simulated Annealing: Additional neighbourhoods' as description,
		'text' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'Neighbour') as ord,
		0 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'Neighbour');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Reservation.CanAssignOverTheLimit' as name,
		'true' as default_value,
		'Allow over limit for individual reservations' as description,
		'boolean' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'StudentSct') as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSct');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Placement.FlexibleConstrPreferenceWeight1' as name,
		'%Placement.ConstrPreferenceWeight1%' as default_value,
		'Flexible constraint preference weight (level 1)' as description,
		'double' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'Value') as ord,
		0 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'Value');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Placement.FlexibleConstrPreferenceWeight2' as name,
		'%FlexibleConstraint.Weight%' as default_value,
		'Flexible constraint preference weight (level 2)' as description,
		'double' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'Value') as ord,
		0 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'Value');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Placement.FlexibleConstrPreferenceWeight3' as name,
		'%Placement.ConstrPreferenceWeight3%' as default_value,
		'Flexible constraint preference weight (level 3)' as description,
		'double' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'Value') as ord,
		0 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'Value');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'ClassWeightProvider.Class' as name,
		'Default Class Weights' as default_value,
		'Class Weights' as description,
		'enum(Default Class Weights,Average Hours A Week Class Weights)' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'General') as ord,
		0 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'General');		

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
