/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2011, UniTime LLC
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
select uniqueid into @gid from solver_parameter_group where name='StudentSctBasic';
select max(ord)+1 into @ord from solver_parameter_def where solver_param_group_id=@gid;

insert into solver_parameter_def
	(uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id) values
	(@id, 'StudentSct.ProjectedCourseDemadsClass', 'None', 'Projected student course demands',
	'enum(None,Last Like Student Course Demands,Projected Student Course Demands,Curricula Course Demands,Curricula Last Like Course Demands,Student Course Requests,Enrolled Student Course Demands)', @ord, 1, @gid);

update hibernate_unique_key set next_hi=next_hi+1;

update solver_parameter_def set
	type = 'enum(Initial,MPP,Projection)'
	where name = 'StudentSctBasic.Mode';

/**
 * Update database version
 */

update application_config set value='67' where name='tmtbl.db.version';

commit;
		